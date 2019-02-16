package org.interledger.lpiv2.blast;

import org.interledger.lpiv2.blast.ImmutableBlastPluginSettings.Builder;
import org.interledger.plugin.PluginFactory;
import org.interledger.plugin.lpiv2.Plugin;
import org.interledger.plugin.lpiv2.PluginSettings;
import org.interledger.plugin.lpiv2.PluginType;

import org.springframework.web.client.RestTemplate;

import java.util.Objects;

/**
 * An implementation of {@link PluginFactory} for creating BTP Plugins.
 */
public class BlastPluginFactory implements PluginFactory {

  private final RestTemplate restTemplate;

  public BlastPluginFactory(final RestTemplate restTemplate) {
    this.restTemplate = Objects.requireNonNull(restTemplate);
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

    // Translate from Plugin.customSettings, being sure to apply custom settings from the incoming plugin.
    final Builder builder = BlastPluginSettings.builder().from(pluginSettings);
    final BlastPluginSettings blastPluginSettings =
        BlastPluginSettings.applyCustomSettings(builder, pluginSettings.getCustomSettings()).build();

    final BlastPlugin blastPlugin = new BlastPlugin(
        ModifiableBlastPluginSettings.create().from(blastPluginSettings), // Modifiable for testing
        restTemplate
    );

    // TODO
    //        // Alert any plugin listeners that a new plugin was constructed...
//        eventPublisher.publishEvent(PluginConstructedEvent.builder()
//          .message(String.format("Plugin constructed for `%s`", pluginSettings.getAccountAddress().getValue()))
//          .object(plugin)
//          .build());

    return blastPlugin;
  }

  @Override
  public boolean supports(PluginType pluginType) {
    return BlastPlugin.PLUGIN_TYPE.equals(pluginType);
  }

}
