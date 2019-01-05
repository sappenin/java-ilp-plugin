package org.interledger.plugin;

import org.interledger.plugin.lpiv2.Plugin;
import org.interledger.plugin.lpiv2.PluginSettings;

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
