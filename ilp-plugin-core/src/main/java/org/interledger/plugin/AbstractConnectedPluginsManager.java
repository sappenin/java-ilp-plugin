package org.interledger.plugin;

import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.lpiv2.Plugin;
import org.interledger.plugin.lpiv2.events.PluginDisconnectedEvent;
import org.interledger.plugin.lpiv2.events.PluginEventListener;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * An abstract implementation of {@link ConnectedPluginsManager}.
 */
public abstract class AbstractConnectedPluginsManager implements ConnectedPluginsManager, PluginEventListener {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  private Map<InterledgerAddress, Plugin<?>> connectedPlugins;

  public AbstractConnectedPluginsManager() {
    this.connectedPlugins = Maps.newConcurrentMap();
  }

  @Override
  public Plugin<?> putConnectedPlugin(final InterledgerAddress accountAddress, final Plugin<?> plugin) {
    plugin.addPluginEventListener(UUID.randomUUID(), this);
    return this.connectedPlugins.putIfAbsent(accountAddress, plugin);
  }

  @Override
  public Optional<Plugin<?>> getConnectedPlugin(final InterledgerAddress accountAddress) {
    Objects.requireNonNull(accountAddress);
    return Optional.of(connectedPlugins.get(accountAddress));
  }

  public Stream<InterledgerAddress> getAllConnectedPluginAddresses() {
    return this.connectedPlugins.keySet().stream();
  }

  @Override
  public CompletableFuture<Void> removeConnectedPlugin(final InterledgerAddress accountAddress) {
    Objects.requireNonNull(accountAddress);

    return this.getConnectedPlugin(accountAddress)
        .map(plugin -> plugin.disconnect().thenAccept(($) -> connectedPlugins.remove(accountAddress)))
        .orElseGet(() -> CompletableFuture.completedFuture(null));
  }

  @Override
  public void onDisconnect(final PluginDisconnectedEvent event) {
    Objects.requireNonNull(event);
    this.removeConnectedPlugin(event.getPlugin().getPluginSettings().getAccountAddress());
  }
}
