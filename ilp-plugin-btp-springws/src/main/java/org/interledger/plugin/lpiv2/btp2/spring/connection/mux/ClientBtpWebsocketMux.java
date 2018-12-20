package org.interledger.plugin.lpiv2.btp2.spring.connection.mux;

import static org.interledger.plugin.lpiv2.btp2.subprotocols.BtpSubProtocolHandlerRegistry.BTP_SUB_PROTOCOL_AUTH;

import org.interledger.btp.BtpMessage;
import org.interledger.btp.BtpPacket;
import org.interledger.btp.BtpResponse;
import org.interledger.btp.BtpSession;
import org.interledger.btp.BtpSessionCredentials;
import org.interledger.btp.BtpSubProtocol.ContentType;
import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.connections.mux.AbstractBilateralComboMux;
import org.interledger.plugin.lpiv2.btp2.spring.BtpSessionUtils;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BinaryMessageToBtpPacketConverter;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BtpConversionException;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BtpPacketToBinaryMessageConverter;
import org.interledger.plugin.lpiv2.btp2.subprotocols.AbstractBtpSubProtocolHandler;
import org.interledger.plugin.lpiv2.btp2.subprotocols.BtpSubProtocolHandlerRegistry;

import com.google.common.io.BaseEncoding;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * <p>An extension of {@link AbstractBilateralComboMux} that connects to a remote peer using Websockets and BTP Auth.
 * Incoming requests are forwarded to the proper registered plugin based upon the Auth settings.</p>
 *
 * <p>Because this is a client-initiated Websocket connection, this MUX can only operate on a single ILP account, which
 * means there is only a single auth username/password allowed.</p>
 */
public class ClientBtpWebsocketMux extends AbstractBtpWebsocketComboMux {

  // TODO: Consider moving to a settings? Note: Settings should have username/token, not a sessioncredentials.
  private final String remotePeerScheme;
  private final String remotePeerHostname;
  private final String remotePeerPort;

  private final InterledgerAddress accountAddress;
  private final BtpSessionCredentials btpSessionCredentials;

  // Incoming and outgoing messages are transmitted via this client.
  private final StandardWebSocketClient wsClient;

  public ClientBtpWebsocketMux(
      final BinaryMessageToBtpPacketConverter binaryMessageToBtpPacketConverter,
      final BtpPacketToBinaryMessageConverter btpPacketToBinaryMessageConverter,
      final BtpSubProtocolHandlerRegistry btpSubProtocolHandlerRegistry,
      final InterledgerAddress accountAddress,
      final Optional<String> authUserName,
      final String authToken,
      final String remotePeerScheme,
      final String remotePeerHostname,
      final String remotePeerPort,
      final StandardWebSocketClient wsClient
  ) {
    super(binaryMessageToBtpPacketConverter, btpPacketToBinaryMessageConverter, btpSubProtocolHandlerRegistry);

    this.accountAddress = Objects.requireNonNull(accountAddress);

    this.btpSessionCredentials = BtpSessionCredentials.builder()
        .authUsername(authUserName)
        .authToken(authToken)
        .build();

    this.remotePeerScheme = Objects.requireNonNull(remotePeerScheme);
    this.remotePeerHostname = Objects.requireNonNull(remotePeerHostname);
    this.remotePeerPort = Objects.requireNonNull(remotePeerPort);

    this.wsClient = Objects.requireNonNull(wsClient);
  }

  @Override
  public CompletableFuture<Void> doConnectTransport() {
    // Connect and initialize the WebSocketSession...
    try {
      final WebSocketSession webSocketSession =
          wsClient.doHandshake(
              new BinaryWebSocketHandler() {
                @Override
                public void afterConnectionEstablished(WebSocketSession session) {
                  // Initialize a new BTP Session...
                  final BtpSession btpSession = new BtpSession(
                      session.getId(),
                      accountAddress,
                      btpSessionCredentials
                  );
                  BtpSessionUtils.setBtpSessionIntoWebsocketSession(session, btpSession);
                }

                @Override
                protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
                  ClientBtpWebsocketMux.this.handleBinaryMessage(session, message);
                }
              },
              "{scheme}://{localhost}:{port}/btp",
              remotePeerScheme,
              remotePeerHostname,
              remotePeerPort)
              // TODO: Make configurable.
              .get(5, TimeUnit.SECONDS);

      // AUTH over the new Websocket Session...
      final long requestId = nextRequestId();
      final BtpMessage btpAuthMessage = this.constructAuthMessage(requestId, btpSessionCredentials);
      final BinaryMessage binaryAuthMessage = btpPacketToBinaryMessageConverter.convert(btpAuthMessage);
      logger.debug(
          "Websocket Auth BinaryMessage Bytes: {}",
          BaseEncoding.base16().encode(binaryAuthMessage.getPayload().array())
      );

      return this.sendMessageWithPendingRepsonse(requestId, webSocketSession, binaryAuthMessage)
          .handle((response, error) -> {
            if (error != null) {
              // the pending response timed out or otherwise had a problem...
              logger.error(error.getMessage(), error);
            } else {
              // The pending response completed successfully...
              logger.debug("Auth completed successfully!");
            }
            // To conform to `Void` return type.
            return null;
          });
    } catch (Exception e) {
      this.disconnect().join();
      throw new RuntimeException(e.getMessage(), e);
    }
  }


  /**
   * Handle an incoming BinaryMessage from a Websocket, assuming it's a BTP Auth message. This implementation override
   * the default server-variant because the client must handle Auth differently from the Server.
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

    final AbstractBtpSubProtocolHandler btpAuthSubprotocolHandler = this.getBtpSubProtocolHandlerRegistry()
        .getHandler(BTP_SUB_PROTOCOL_AUTH, ContentType.MIME_APPLICATION_OCTET_STREAM)
        .orElseThrow(() -> new RuntimeException("No BTP AuthSubprotocol Handler registered!"));

    // If there's a problem de-serializing the BtpPacket from the BinaryMessage, then close the connection and
    // return empty. This is one of the "tricky cases" as defined in the BTP spec where we don't want to get into
    // an infinite loop.
    final BtpPacket incomingBtpPacket;
    try {
      incomingBtpPacket = this.binaryMessageToBtpPacketConverter.convert(incomingBinaryMessage);
      final BtpSession btpSession = BtpSessionUtils.getBtpSessionFromWebSocketSession(webSocketSession)
          .orElseThrow(() -> new RuntimeException("BtpSession is required!"));
      return btpAuthSubprotocolHandler.handleSubprotocolMessage(btpSession, incomingBtpPacket)
          .thenApply($ -> {
            // If there's no exception, then reaching here means the btp_auth SubProtocol succeeded, so return an
            // empty BTP response to a pendingResponse so that an awaiting client can get an ultimate response.

            // Join the response from the AuthHandler back to the pendingResponse.
            this.joinPendingResponse(
                BtpResponse.builder()
                    .requestId(incomingBtpPacket.getRequestId())
                    .build()
            );

            // Return empty so that the caller of handleBinaryAuthMessage doesn't return any response back to the sender
            // of the actual incomingBinaryMessage.
            return Optional.<BtpResponse>empty();
          })
          .join();
    } catch (BtpConversionException btpConversionException) {
      logger.error(btpConversionException.getMessage(), btpConversionException);
      throw btpConversionException;
    }
  }

  @Override
  public CompletableFuture<Void> doDisconnectTransport() {
    synchronized (webSocketSession) {
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
    return CompletableFuture.completedFuture(null);
  }
}