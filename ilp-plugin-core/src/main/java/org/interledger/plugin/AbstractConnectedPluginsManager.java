package org.interledger.plugin;

import org.interledger.plugin.lpiv2.Plugin;
import org.interledger.plugin.lpiv2.PluginId;
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
 * An abstract implementation of {@link ConnectedPluginsManager} that holds onto a plugin as long as its connected, and
 * removes the plugin upon a disconnect.
 */
public abstract class AbstractConnectedPluginsManager implements ConnectedPluginsManager, PluginEventListener {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  private Map<PluginId, Plugin<?>> connectedPlugins;

  public AbstractConnectedPluginsManager() {
    this.connectedPlugins = Maps.newConcurrentMap();
  }

  @Override
  public Plugin<?> putConnectedPlugin(final Plugin<?> plugin) {
    plugin.addPluginEventListener(UUID.randomUUID(), this);
    return this.connectedPlugins.putIfAbsent(plugin.getPluginId().get(), plugin);
  }

  @Override
  public Optional<Plugin<?>> getConnectedPlugin(final PluginId pluginId) {
    Objects.requireNonNull(pluginId);
    return Optional.ofNullable(connectedPlugins.get(pluginId));
  }

  public Stream<PluginId> getAllConnectedPluginIds() {
    return this.connectedPlugins.keySet().stream();
  }

  @Override
  public CompletableFuture<Void> removeConnectedPlugin(final PluginId pluginId) {
    Objects.requireNonNull(pluginId);

    return this.getConnectedPlugin(pluginId)
        .map(plugin -> plugin.disconnect().thenAccept(($) -> connectedPlugins.remove(pluginId)))
        .orElseGet(() -> CompletableFuture.completedFuture(null));
  }

  @Override
  public void onDisconnect(final PluginDisconnectedEvent event) {
    Objects.requireNonNull(event);
    this.removeConnectedPlugin(event.getPlugin().getPluginId().get());
  }
}
