package org.interledger.plugin;

import org.interledger.plugin.lpiv2.Plugin;
import org.interledger.plugin.lpiv2.PluginSettings;
import org.interledger.plugin.lpiv2.PluginType;

/**
 * A factory for constructing instances of {@link Plugin} based upon configured settings.
 */
public interface PluginFactory {

  /**
   * Construct a new instance of {@link Plugin} using the supplied inputs.
   *
   * @return A newly constructed instance of {@link Plugin}.
   */
  Plugin<?> constructPlugin(PluginSettings pluginSettings);

  /**
   * Helper method to apply custom settings on a per-plugin-type basis.
   *
   * @param pluginSettings
   *
   * @return
   */
  default PluginSettings applyCustomSettings(PluginSettings pluginSettings) {
    return pluginSettings;
  }

  /**
   * Determines if this factory support a particular type of {@link PluginType}.
   *
   * @param pluginType A {@link PluginType} to check compatibility for.
   *
   * @return {@code true} if this factory supports the specified pluginType; {@code false} otherwise.
   */
  boolean supports(PluginType pluginType);

  /**
   * Construct a new instance of {@link Plugin} using the supplied inputs.
   *
   * @return A newly constructed instance of {@link Plugin}.
   */
  default <PS extends PluginSettings, P extends Plugin<PS>> P constructPlugin(
      final Class<P> $, final PS pluginSettings
  ) {
    return (P) this.constructPlugin(pluginSettings);
  }
}
