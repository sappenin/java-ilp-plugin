package org.interledger.plugin.lpiv2.btp2.spring;

import org.interledger.encoding.asn.framework.CodecContext;
import org.interledger.plugin.PluginFactory;
import org.interledger.plugin.lpiv2.LoopbackPlugin;
import org.interledger.plugin.lpiv2.Plugin;
import org.interledger.plugin.lpiv2.PluginSettings;
import org.interledger.plugin.lpiv2.btp2.BtpServerPluginSettings;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BinaryMessageToBtpPacketConverter;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BtpPacketToBinaryMessageConverter;
import org.interledger.plugin.lpiv2.btp2.subprotocols.BtpSubProtocolHandlerRegistry;

import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * An implementation of {@link PluginFactory} for creating BTP Plugins.
 */
public class BtpServerPluginFactory implements PluginFactory {

  private final CodecContext ilpCodecContext;
  private final BinaryMessageToBtpPacketConverter binaryMessageToBtpPacketConverter;
  private final BtpPacketToBinaryMessageConverter btpPacketToBinaryMessageConverter;
  private final BtpSubProtocolHandlerRegistry btpSubProtocolHandlerRegistry;

  public BtpServerPluginFactory(
      final CodecContext ilpCodecContext,
      final BinaryMessageToBtpPacketConverter binaryMessageToBtpPacketConverter,
      final BtpPacketToBinaryMessageConverter btpPacketToBinaryMessageConverter,
      final BtpSubProtocolHandlerRegistry btpSubProtocolHandlerRegistry
  ) {
    this.ilpCodecContext = Objects.requireNonNull(ilpCodecContext);
    this.binaryMessageToBtpPacketConverter = Objects.requireNonNull(binaryMessageToBtpPacketConverter);
    this.btpPacketToBinaryMessageConverter = Objects.requireNonNull(btpPacketToBinaryMessageConverter);
    this.btpSubProtocolHandlerRegistry = Objects.requireNonNull(btpSubProtocolHandlerRegistry);
  }

  /**
   * Construct a new instance of {@link Plugin} using the supplied inputs.
   *
   * @return A newly constructed instance of {@link Plugin}.
   */
  public Plugin<?> constructPlugin(final PluginSettings pluginSettings) {
    Objects.requireNonNull(pluginSettings);

    final Plugin<?> plugin;
    switch (pluginSettings.getPluginType().value()) {
      case LoopbackPlugin.PLUGIN_TYPE_STRING: {
        plugin = new LoopbackPlugin(pluginSettings.getAccountAddress());
        break;
      }
      case BtpServerPlugin.PLUGIN_TYPE_STRING: {
        plugin = this.constructServerBtpPlugin(pluginSettings);
        break;
      }
      case BtpClientPlugin.PLUGIN_TYPE_STRING: {
        plugin = this.constructClientBtpPlugin(pluginSettings);
        break;
      }
      default: {
        throw new RuntimeException(String.format("Unsupported PluginType: %s", pluginSettings.getPluginType()));
      }
    }

//        // Alert any plugin listeners that a new plugin was constructed...
//        eventPublisher.publishEvent(PluginConstructedEvent.builder()
//          .message(String.format("Plugin constructed for `%s`", pluginSettings.getAccountAddress().getValue()))
//          .object(plugin)
//          .build());

    return plugin;

  }

  /////////////////
  // Helper Methods
  /////////////////

  /**
   * Construct a new plugin to handle all BTP requests using a Websocket server.
   *
   * @param pluginSettings An instance of {@link PluginSettings} that can be used to configure the newly constructed
   *                       plugin.
   *
   * @return A newly constructed instance of {@link BtpServerPlugin}.
   */
  private BtpServerPlugin constructServerBtpPlugin(final PluginSettings pluginSettings) {
    Objects.requireNonNull(pluginSettings);

    // Translate from Plugin.customSettings into typed BtpServerPluginSettings...
    final BtpServerPluginSettings serverPluginSettings = BtpServerPluginSettings.
        applyCustomSettings(
            BtpServerPluginSettings.builder().from(pluginSettings), pluginSettings.getCustomSettings()
        )
        .build();

    final BtpServerPlugin btpServerPlugin = new BtpServerPlugin(
        serverPluginSettings,
        ilpCodecContext,
        binaryMessageToBtpPacketConverter,
        btpPacketToBinaryMessageConverter,
        btpSubProtocolHandlerRegistry
    );

    // By default, Server plugins no-op SendMoney calls.
    btpServerPlugin.registerMoneyHandler((amount -> CompletableFuture.completedFuture(null)));

    return btpServerPlugin;
  }

  /**
   * Construct a new plugin to handle all BTP requests using a Websocket client.
   *
   * @param pluginSettings An instance of {@link PluginSettings} that can be used to configure the newly constructed
   *                       plugin.
   *
   * @return A newly constructed instance of {@link BtpClientPlugin}.
   */
  private BtpClientPlugin constructClientBtpPlugin(final PluginSettings pluginSettings) {
    Objects.requireNonNull(pluginSettings);

    // Translate from Plugin.customSettings, being sure to apply custom settings from the incoming plugin.
    final ImmutableBtpClientPluginSettings.Builder builder = BtpClientPluginSettings.builder();
    final BtpClientPluginSettings clientPluginSettings =
        BtpClientPluginSettings.applyCustomSettings(builder, pluginSettings.getCustomSettings()).build();

    return new BtpClientPlugin(
        clientPluginSettings,
        ilpCodecContext,
        binaryMessageToBtpPacketConverter,
        btpPacketToBinaryMessageConverter,
        btpSubProtocolHandlerRegistry,
        new StandardWebSocketClient()
    );
  }
}
