package org.interledger.plugin.lpiv2.btp2.spring;

import org.interledger.btp.BtpMessage;
import org.interledger.btp.BtpPacket;
import org.interledger.btp.BtpResponse;
import org.interledger.btp.BtpResponsePacket;
import org.interledger.btp.BtpSession;
import org.interledger.encoding.asn.framework.CodecContext;
import org.interledger.plugin.lpiv2.PluginType;
import org.interledger.plugin.lpiv2.btp2.BtpReceiver;
import org.interledger.plugin.lpiv2.btp2.BtpSender;
import org.interledger.plugin.lpiv2.btp2.spring.PendingResponseManager.NoPendingResponseException;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BinaryMessageToBtpPacketConverter;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BtpConversionException;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BtpPacketToBinaryMessageConverter;
import org.interledger.plugin.lpiv2.btp2.subprotocols.BtpSubProtocolHandlerRegistry;

import com.google.common.io.BaseEncoding;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * An extension of {@link AbstractBtpPlugin} that connects to a remote peer using BTP, but merely logs all responses.
 */
public class BtpClientPlugin extends AbstractBtpPlugin<BtpClientPluginSettings> implements BtpSender, BtpReceiver {

  public static final String PLUGIN_TYPE_STRING = "BtpClientPlugin";
  public static final PluginType PLUGIN_TYPE = PluginType.of(PLUGIN_TYPE_STRING);

  // Incoming and outgoing messages are transmitted via this client.
  private final StandardWebSocketClient webSocketClient;

  // The session is initialliy empty, but once connect is called, a session is created.
  private Optional<WebSocketSession> webSocketSession = Optional.empty();

  /**
   * Required-args Constructor.
   */
  public BtpClientPlugin(
      final BtpClientPluginSettings pluginSettings,
      final CodecContext ilpCodecContext,
      final BinaryMessageToBtpPacketConverter binaryMessageToBtpPacketConverter,
      final BtpPacketToBinaryMessageConverter btpPacketToBinaryMessageConverter,
      final BtpSubProtocolHandlerRegistry btpSubProtocolHandlerRegistry,
      final StandardWebSocketClient webSocketClient
  ) {
    super(
        pluginSettings,
        ilpCodecContext,
        binaryMessageToBtpPacketConverter,
        btpPacketToBinaryMessageConverter,
        btpSubProtocolHandlerRegistry,
        new PendingResponseManager<>(BtpResponsePacket.class)
    );

    this.webSocketClient = Objects.requireNonNull(webSocketClient);
  }

  @Override
  public CompletableFuture<Void> doConnect() {
    // Connect and initialize the WebSocketSession...
    try {
      final WebSocketSession webSocketSession =
          webSocketClient.doHandshake(
              new BinaryWebSocketHandler() {
                @Override
                public void afterConnectionEstablished(WebSocketSession session) {
                  // Initialize a new BTP Session...
                  final BtpSession btpSession = new BtpSession(session.getId(), getBtpSessionCredentials());
                  BtpSessionUtils.setBtpSessionIntoWebsocketSession(session, btpSession);
                }

                @Override
                protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
                  BtpClientPlugin.this.handleBinaryMessage(session, message);
                }
              },
              "{scheme}://{localhost}:{port}/btp",
              getPluginSettings().getRemotePeerScheme(),
              getPluginSettings().getRemotePeerHostname(),
              getPluginSettings().getRemotePeerPort())
              // TODO: Make configurable.
              .get(5, TimeUnit.SECONDS);

      // AUTH over the new Websocket Session...
      final long requestId = nextRequestId();
      final BtpMessage btpAuthMessage = this.constructAuthMessage(requestId, getBtpSessionCredentials());
      final BinaryMessage binaryAuthMessage = getBtpPacketToBinaryMessageConverter().convert(btpAuthMessage);
      logger.debug(
          "Websocket Auth BinaryMessage Bytes: {}",
          BaseEncoding.base16().encode(binaryAuthMessage.getPayload().array())
      );

      // We wait 5 seconds for Auth Messages to succeed, and then fail.
      return this
          .sendMessageWithPendingRepsonse(
              requestId, "BTP Auth", webSocketSession, binaryAuthMessage, Duration.of(5, ChronoUnit.SECONDS)
          )
          .handle((response, error) -> {
            if (error != null) {
              // the pending response timed out or otherwise had a problem...
              //logger.error(error.getMessage(), error);
              throw new RuntimeException("Unable to authenticate with BTP Server!", error);
            } else {
              // The pending response completed successfully...
              logger.debug("Auth completed successfully!");
            }
            // To conform to `Void` return type.
            return null;
          });
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }

  }

  @Override
  public CompletableFuture<Void> doDisconnect() {
    return CompletableFuture.supplyAsync(() -> {
      synchronized (this) {
        // Block on the Disconnect so that only one thread can operate at a time...
        webSocketSession.ifPresent(webSocketSession -> {
          try {
            webSocketSession.close();
            this.webSocketSession = Optional.empty();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
      }
      return null;
    });
  }

  @Override
  protected WebSocketSession getWebSocketSession() {
    return this.webSocketSession
        .orElseThrow(
            () -> new RuntimeException("No WebSocket Session. You must call connect() before calling this method."));
  }

  @Override
  public void doSendMoney(BigInteger amount) {
    // This is a no-op by default. Sub-classes should override this method to actually send money to the remote peer.
  }

  /**
   * Handle an incoming BinaryMessage from a Websocket, assuming it's a BTP Auth result message.
   *
   * @return A {@link BinaryMessage} that can immediately be returned to the caller (this response will contain
   *     everything required to be eligible as a BTP response), or nothing if the response is {@link Optional#empty()}.
   */
  @Override
  public Optional<BtpResponse> handleBinaryAuthMessage(
      final WebSocketSession webSocketSession, final BinaryMessage incomingBinaryMessage
  ) {
    Objects.requireNonNull(webSocketSession);
    Objects.requireNonNull(incomingBinaryMessage);

    // If there's a problem de-serializing the BtpPacket from the BinaryMessage, then close the connection and
    // return empty. This is one of the "tricky cases" as defined in the BTP spec where we don't want to get into
    // an infinite loop.
    final BtpPacket incomingBtpPacket;
    try {
      incomingBtpPacket = this.getBinaryMessageToBtpPacketConverter().convert(incomingBinaryMessage);
    } catch (BtpConversionException btpConversionException) {
      logger.error(btpConversionException.getMessage(), btpConversionException);
      throw btpConversionException;
    }

    // If we get a response from the server here, assume that Auth succeeded...
    final BtpSession btpSession = BtpSessionUtils.getBtpSessionFromWebSocketSession(webSocketSession)
        .orElseThrow(() -> new RuntimeException("BtpSession is required!"));

    // There is a pendingResponse that is waiting to be fulfilled (this was the original auth call from the client).
    // It has a Void return type, so there's nothing to to with it other than "join" it. However, if that join fails,
    // then auth should fail.
    try {

      // TODO: only do this if auth succeeded.
      this.getPendingResponseManager()
          .joinPendingResponse(
              incomingBtpPacket.getRequestId(),
              BtpResponse.builder().requestId(incomingBtpPacket.getRequestId()).build()
          )
          .getJoinableResponseFuture()
          .handle((result, error) -> {
            if (error != null) {
              logger.error("Unable to complete BtpClientAuth: {}", error);
              return error;
            } else {
              this.webSocketSession = Optional.ofNullable(webSocketSession);
              btpSession.setAuthenticated();
              return result;
            }
          }).join();
    } catch (NoPendingResponseException e) {
      logger.error(e.getMessage(), e);
    }

    return Optional.empty();
  }
}