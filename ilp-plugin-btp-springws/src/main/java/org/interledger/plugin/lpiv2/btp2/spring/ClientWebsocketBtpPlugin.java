package org.interledger.plugin.lpiv2.btp2.spring;

import static org.interledger.btp.BtpSubProtocol.ContentType.MIME_APPLICATION_OCTET_STREAM;
import static org.interledger.plugin.lpiv2.btp2.subprotocols.BtpSubProtocolHandlerRegistry.BTP_SUB_PROTOCOL_AUTH;
import static org.interledger.plugin.lpiv2.btp2.subprotocols.BtpSubProtocolHandlerRegistry.BTP_SUB_PROTOCOL_ILP;

import org.interledger.btp.BtpMessage;
import org.interledger.btp.asn.framework.BtpCodecContextFactory;
import org.interledger.core.asn.framework.InterledgerCodecContextFactory;
import org.interledger.encoding.asn.framework.CodecContext;
import org.interledger.plugin.lpiv2.PluginType;
import org.interledger.plugin.lpiv2.btp2.BtpClientPluginSettings;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BinaryMessageToBtpPacketConverter;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BtpPacketToBinaryMessageConverter;
import org.interledger.plugin.lpiv2.btp2.subprotocols.BtpSubProtocolHandlerRegistry;

import com.google.common.io.BaseEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * An extension of {@link AbstractWebsocketBtpPlugin} that connects to a remote peer using BTP, but merely logs all
 * responses.
 */
public class ClientWebsocketBtpPlugin extends AbstractWebsocketBtpPlugin<BtpClientPluginSettings> {

  public static final String PLUGIN_TYPE_STRING = "ClientWebsocketBtpPlugin";
  public static final PluginType PLUGIN_TYPE = PluginType.of(PLUGIN_TYPE_STRING);

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final StandardWebSocketClient wsClient;

  /**
   * Required-args Constructor.
   */
  public ClientWebsocketBtpPlugin(final BtpClientPluginSettings settings) {
    this(settings, InterledgerCodecContextFactory.oer(), BtpCodecContextFactory.oer());
  }

  /**
   * Required-args Constructor.
   */
  public ClientWebsocketBtpPlugin(
      final BtpClientPluginSettings settings,
      final CodecContext ilpCodecContext,
      final CodecContext btpCodecContext
  ) {
    this(
        settings,
        ilpCodecContext,
        btpCodecContext,
        new BtpSubProtocolHandlerRegistry(),
        new BinaryMessageToBtpPacketConverter(btpCodecContext),
        new BtpPacketToBinaryMessageConverter(btpCodecContext),
        new StandardWebSocketClient()
    );
  }

  /**
   * Required-args Constructor.
   */
  public ClientWebsocketBtpPlugin(
      final BtpClientPluginSettings settings,
      final CodecContext ilpCodecContext,
      final CodecContext btpCodecContext,
      final BtpSubProtocolHandlerRegistry btpSubProtocolHandlerRegistry,
      final BinaryMessageToBtpPacketConverter binaryMessageToBtpPacketConverter,
      final BtpPacketToBinaryMessageConverter btpPacketToBinaryMessageConverter,
      final StandardWebSocketClient wsClient
  ) {
    super(settings, ilpCodecContext, btpCodecContext, btpSubProtocolHandlerRegistry, binaryMessageToBtpPacketConverter,
        btpPacketToBinaryMessageConverter);

    this.wsClient = Objects.requireNonNull(wsClient);
  }

  /**
   * Override the plugin-type in the supplied plugin settings.
   *
   * @param pluginSettings
   *
   * @return
   */
  private static final BtpClientPluginSettings setPluginType(final BtpClientPluginSettings pluginSettings) {
    return BtpClientPluginSettings.builder().from(pluginSettings).pluginType(PLUGIN_TYPE).build();
  }


  @Override
  public CompletableFuture<Void> doConnect() {

    try {
      // Connect and initialize the Session...
      this.webSocketSession = Optional.ofNullable(
          wsClient.doHandshake(
              new BinaryWebSocketHandler() {
                @Override
                protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
                  // TODO: What does the other side of the Websocket see if there's an exception here?
                  onIncomingBinaryMessage(session, message)
                      .ifPresent(response -> {
                        try {
                          session.sendMessage(response);
                        } catch (IOException e) {
                          throw new RuntimeException(e);
                        }
                      });
                }
              },
              "{scheme}://{localhost}:{port}/btp",
              getPluginSettings().getRemotePeerScheme(),
              getPluginSettings().getRemotePeerHostname(),
              getPluginSettings().getRemotePeerPort())
              .get()
      );

      // AUTH
      final long requestId = nextRequestId();
      final String authToken = getPluginSettings().getSecret();
      final BtpMessage btpAuthMessage = this.constructAuthMessage(requestId, authToken);
      final BinaryMessage binaryAuthMessage = btpPacketToBinaryMessageConverter.convert(btpAuthMessage);
      logger.debug(
          "Websocket Auth BinaryMessage Bytes: {}",
          BaseEncoding.base16().encode(binaryAuthMessage.getPayload().array())
      );
      return this.sendMessageWithPendingRepsonse(requestId, binaryAuthMessage)
          .thenAccept((response) -> {
            // Convert to Void response...
          });
    } catch (InterruptedException | ExecutionException e) {
      logger.error(e.getMessage(), e);
      return this.disconnect();
    }

  }

  @Override
  public CompletableFuture<Void> doDisconnect() {
    synchronized (webSocketSession) {
      // Block on the Disconnect so that only one thread can operate at a time...
      webSocketSession.ifPresent(session -> {
        try {
          session.close();
          webSocketSession = Optional.empty();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });
    }
    return CompletableFuture.completedFuture(null);
  }

  /**
   * Perform the logic of settling with a remote peer.
   *
   * @param amount
   */
  @Override
  protected CompletableFuture<Void> doSendMoney(BigInteger amount) {
    throw new RuntimeException("FIXME!");
  }

}