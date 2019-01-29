package org.interledger.plugin;

import org.interledger.plugin.lpiv2.Plugin;
import org.interledger.plugin.lpiv2.PluginId;
import org.interledger.plugin.lpiv2.PluginSettings;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Tracks plugins that have successfully connected, whether client plugins making outbound connections or server plugins
 * that are created in response to an incoming server request.
 */
public interface ConnectedPluginsManager {

  /**
   * Accessor for an optionally-present Plugin that support the specified account address.
   *
   * @param pluginId The unique identifier for a Plugin.
   *
   * @return An optionally-present {@link Plugin}.
   */
  Optional<Plugin<?>> getConnectedPlugin(PluginId pluginId);

  /**
   * Accessor for an optionally-present Plugin that supports the specified account address.
   *
   * @param pluginId The unique identifier for a Plugin.
   *
   * @return An optionally-present {@link Plugin}.
   */
  default <PS extends PluginSettings, P extends Plugin<PS>> Optional<P> getConnectedPlugin(
      final Class<P> $, final PluginId pluginId
  ) {
    Objects.requireNonNull(pluginId);
    return this.getConnectedPlugin(pluginId).map(plugin -> (P) plugin);
  }

  Plugin<?> putConnectedPlugin(final Plugin<?> plugin);

  CompletableFuture<Void> removeConnectedPlugin(final PluginId pluginId);
}
