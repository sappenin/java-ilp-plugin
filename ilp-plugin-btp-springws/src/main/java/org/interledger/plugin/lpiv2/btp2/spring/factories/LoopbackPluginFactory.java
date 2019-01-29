package org.interledger.plugin.lpiv2.btp2.spring.factories;

import org.interledger.plugin.PluginFactory;
import org.interledger.plugin.lpiv2.LoopbackPlugin;
import org.interledger.plugin.lpiv2.Plugin;
import org.interledger.plugin.lpiv2.PluginSettings;
import org.interledger.plugin.lpiv2.PluginType;

import java.util.Objects;

/**
 * An implementation of {@link PluginFactory} for creating BTP Plugins.
 */
public class LoopbackPluginFactory implements PluginFactory {

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

    final Plugin<?> plugin;
    switch (pluginSettings.getPluginType().value()) {
      case LoopbackPlugin.PLUGIN_TYPE_STRING: {
        plugin = new LoopbackPlugin(pluginSettings.getOperatorAddress());
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

  @Override
  public boolean supports(PluginType pluginType) {
    return LoopbackPlugin.PLUGIN_TYPE.equals(pluginType);
  }

}
