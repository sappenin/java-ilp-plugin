package org.interledger.plugin;

import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.lpiv2.Plugin;
import org.interledger.plugin.lpiv2.PluginSettings;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Tracks multiple plugins over a single network transport.
 */
public interface ConnectedPluginsManager {

  /**
   * Accessor for an optionally-present Plugin that support the specified account address.
   *
   * @param accountAddress
   *
   * @return An optionally-present {@link Plugin}.
   */
  Optional<Plugin<?>> getConnectedPlugin(InterledgerAddress accountAddress);

  /**
   * Accessor for an optionally-present Plugin that support the specified account address.
   *
   * @param accountAddress
   *
   * @return An optionally-present {@link Plugin}.
   */
  default <PS extends PluginSettings, P extends Plugin<PS>> Optional<P> getConnectedPlugin(
      final Class<P> $, final InterledgerAddress accountAddress
  ) {
    Objects.requireNonNull(accountAddress);
    return this.getConnectedPlugin(accountAddress).map(plugin -> (P) plugin);
  }

  /**
   * @param accountAddress
   * @param plugin
   *
   * @return
   */
  Plugin<?> putConnectedPlugin(final InterledgerAddress accountAddress, final Plugin<?> plugin);

  /**
   * @param accountAddress
   *
   * @return
   */
  CompletableFuture<Void> removeConnectedPlugin(final InterledgerAddress accountAddress);
}
