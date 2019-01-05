package org.interledger.plugin.lpiv2.btp2.spring;

import org.interledger.encoding.asn.framework.CodecContext;
import org.interledger.plugin.lpiv2.PluginType;
import org.interledger.plugin.lpiv2.btp2.BtpReceiver;
import org.interledger.plugin.lpiv2.btp2.BtpSender;
import org.interledger.plugin.lpiv2.btp2.BtpServerPluginSettings;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BinaryMessageToBtpPacketConverter;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BtpPacketToBinaryMessageConverter;
import org.interledger.plugin.lpiv2.btp2.subprotocols.BtpSubProtocolHandlerRegistry;

import org.springframework.web.socket.WebSocketSession;

import java.math.BigInteger;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An extension of {@link AbstractBtpPlugin} for when the plugin is accepting incoming connections over a Websocket
 * server.
 */
public class BtpServerPlugin extends AbstractBtpPlugin<BtpServerPluginSettings> implements BtpSender, BtpReceiver {

  public static final String PLUGIN_TYPE_STRING = "BtpServerPlugin";
  public static final PluginType PLUGIN_TYPE = PluginType.of(PLUGIN_TYPE_STRING);

  // A server BTP plugin MUST not be created until after a client authenticates, so this session will always be non-null.
  private AtomicReference<WebSocketSession> webSocketSession;

  /**
   * Required-args Constructor.
   */
  public BtpServerPlugin(
      final BtpServerPluginSettings pluginSettings,
      final CodecContext ilpCodecContext,
      final BinaryMessageToBtpPacketConverter binaryMessageToBtpPacketConverter,
      final BtpPacketToBinaryMessageConverter btpPacketToBinaryMessageConverter,
      final BtpSubProtocolHandlerRegistry btpSubProtocolHandlerRegistry
  ) {
    super(
        pluginSettings,
        ilpCodecContext,
        binaryMessageToBtpPacketConverter,
        btpPacketToBinaryMessageConverter,
        btpSubProtocolHandlerRegistry
    );

    webSocketSession = new AtomicReference<>();
  }

  /**
   * Required-args Constructor.
   */
  public BtpServerPlugin(
      final BtpServerPluginSettings pluginSettings,
      final CodecContext ilpCodecContext,
      final BinaryMessageToBtpPacketConverter binaryMessageToBtpPacketConverter,
      final BtpPacketToBinaryMessageConverter btpPacketToBinaryMessageConverter,
      final BtpSubProtocolHandlerRegistry btpSubProtocolHandlerRegistry,
      final WebSocketSession webSocketSession
  ) {
    super(
        pluginSettings,
        ilpCodecContext,
        binaryMessageToBtpPacketConverter,
        btpPacketToBinaryMessageConverter,
        btpSubProtocolHandlerRegistry
    );

    this.setWebSocketSession(webSocketSession);
  }

  @Override
  public CompletableFuture<Void> doConnect() {
    // No-op. Plugins are instantiated in response to an incoming connection, so this method doesn't need to do anything
    // extra.
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> doDisconnect() {
    // No-op. The Manager that created this plugin, if any, will remove it from its tracking via event notifications.
    return CompletableFuture.completedFuture(null);
  }

  @Override
  protected WebSocketSession getWebSocketSession() {
    return this.webSocketSession.get();
  }

  @Override
  public void doSendMoney(BigInteger amount) {
    // This is a no-op by default. Sub-classes should override this method to actually send money to the remote peer.
  }

  // TODO: Consider this a bit more. Either the session should only be set once, in which case we should consider a WeakReference.
  // Else, it can be set whenever connect() is called, but this feels a bit strange (i.e., a server plugin should probably
  // just go away if the connection closes).

  /**
   * @param webSocketSession
   */
  public void setWebSocketSession(final WebSocketSession webSocketSession) {
    Objects.requireNonNull(webSocketSession);
    if (!this.webSocketSession.compareAndSet(null, webSocketSession)) {
      throw new RuntimeException("Can't set a WebSocket session into a BtpServerPlugin more than once!");
    }
  }
}
