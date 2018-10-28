package org.interledger.plugin.lpiv2.btp2.spring;

import org.interledger.encoding.asn.framework.CodecContext;
import org.interledger.plugin.lpiv2.PluginType;
import org.interledger.plugin.lpiv2.btp2.BtpServerPluginSettings;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BinaryMessageToBtpPacketConverter;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BtpPacketToBinaryMessageConverter;
import org.interledger.plugin.lpiv2.btp2.subprotocols.BtpSubProtocolHandlerRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * An extension of {@link AbstractWebsocketBtpPlugin} for when the plugin is operating the Websocket server, accepting
 * connections from a remote BTP Plugin.
 */
public class ServerWebsocketBtpPlugin extends AbstractWebsocketBtpPlugin<BtpServerPluginSettings> {

  public static final String PLUGIN_TYPE_STRING = "BTP_SERVER";
  public static final PluginType PLUGIN_TYPE = PluginType.of(PLUGIN_TYPE_STRING);

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  /**
   * Required-args Constructor.
   */
  public ServerWebsocketBtpPlugin(
      final BtpServerPluginSettings pluginSettings,
      final CodecContext ilpCodecContext,
      final CodecContext btpCodecContext,
      final BtpSubProtocolHandlerRegistry btpSubProtocolHandlerRegistry,
      final BinaryMessageToBtpPacketConverter binaryMessageToBtpPacketConverter,
      final BtpPacketToBinaryMessageConverter btpPacketToBinaryMessageConverter
  ) {
    super(
        pluginSettings, ilpCodecContext, btpCodecContext, btpSubProtocolHandlerRegistry,
        binaryMessageToBtpPacketConverter, btpPacketToBinaryMessageConverter
    );
  }

  /**
   * This method will be called for all plugins. This is a listening-only variant (i.e., no external connection needs to
   * be made), so this is a no-op.
   */
  @Override
  public CompletableFuture<Void> doConnect() {
    // Grab BtpSocketHandler from the Context, and call setServerWebsocketBtpPlugin(this) in order to connect the two.
    //this.applicationContext.getBean(BtpSocketHandler.class).setServerWebsocketBtpPlugin(this);

    // There is else todo to connect this plugin to the server...if a client successfully connects via Auth, then a
    // webSocketSession will be attached.
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> doDisconnect() {
    return CompletableFuture.runAsync(() -> {
      // Close the webSocketSession...
      this.webSocketSession.ifPresent(presentSession -> {
        try {
          presentSession.close();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });

      this.webSocketSession = Optional.empty();
    });
  }

  /////////////////
  // Helper Methods
  /////////////////

  /**
   * Method for setter-based dependency injection.
   */
  public void setWebSocketSession(final WebSocketSession session) {
    this.webSocketSession = Optional.of(session);
  }
}
