package org.interledger.plugin.lpiv2.btp2.spring.factories;

import org.interledger.btp.BtpResponsePacket;
import org.interledger.encoding.asn.framework.CodecContext;
import org.interledger.plugin.PluginFactory;
import org.interledger.plugin.lpiv2.Plugin;
import org.interledger.plugin.lpiv2.PluginSettings;
import org.interledger.plugin.lpiv2.PluginType;
import org.interledger.plugin.lpiv2.btp2.spring.BtpServerPlugin;
import org.interledger.plugin.lpiv2.btp2.spring.BtpServerPluginSettings;
import org.interledger.plugin.lpiv2.btp2.spring.PendingResponseManager;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BinaryMessageToBtpPacketConverter;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BtpPacketToBinaryMessageConverter;
import org.interledger.plugin.lpiv2.btp2.subprotocols.BtpSubProtocolHandlerRegistry;

import java.util.Objects;

/**
 * An implementation of {@link PluginFactory} for creating BTP Plugins.
 */
public class BtpServerPluginFactory implements PluginFactory {

  private final CodecContext ilpCodecContext;
  private final BinaryMessageToBtpPacketConverter binaryMessageToBtpPacketConverter;
  private final BtpPacketToBinaryMessageConverter btpPacketToBinaryMessageConverter;
  private final BtpSubProtocolHandlerRegistry btpSubProtocolHandlerRegistry;
  private final PendingResponseManager<BtpResponsePacket> pendingResponseManager;

  public BtpServerPluginFactory(
      final CodecContext ilpCodecContext,
      final BinaryMessageToBtpPacketConverter binaryMessageToBtpPacketConverter,
      final BtpPacketToBinaryMessageConverter btpPacketToBinaryMessageConverter,
      final BtpSubProtocolHandlerRegistry btpSubProtocolHandlerRegistry,
      final PendingResponseManager<BtpResponsePacket> pendingResponseManager
  ) {
    this.ilpCodecContext = Objects.requireNonNull(ilpCodecContext);
    this.binaryMessageToBtpPacketConverter = Objects.requireNonNull(binaryMessageToBtpPacketConverter);
    this.btpPacketToBinaryMessageConverter = Objects.requireNonNull(btpPacketToBinaryMessageConverter);
    this.btpSubProtocolHandlerRegistry = Objects.requireNonNull(btpSubProtocolHandlerRegistry);
    this.pendingResponseManager = Objects.requireNonNull(pendingResponseManager);
  }

  /**
   * Construct a new instance of {@link Plugin} using the supplied inputs.
   *
   * @return A newly constructed instance of {@link Plugin}.
   */
  public Plugin<?> constructPlugin(final PluginSettings pluginSettings) {
    Objects.requireNonNull(pluginSettings);

    if (!this.supports(pluginSettings.getPluginType())) {
      throw new RuntimeException(
          String.format("PluginType `%s` not supported by this factory!", pluginSettings.getPluginType())
      );
    }

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
        btpSubProtocolHandlerRegistry,
        pendingResponseManager
    );

//        // Alert any plugin listeners that a new plugin was constructed...
//        eventPublisher.publishEvent(PluginConstructedEvent.builder()
//          .message(String.format("Plugin constructed for `%s`", pluginSettings.getAccountAddress().getValue()))
//          .object(plugin)
//          .build());

    return btpServerPlugin;
  }

  @Override
  public boolean supports(PluginType pluginType) {
    return BtpServerPlugin.PLUGIN_TYPE.equals(pluginType);
  }
}
