package org.interledger.plugin.link.mux;

import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.lpiv2.Plugin;
import org.interledger.plugin.lpiv2.events.PluginConnectedEvent;
import org.interledger.plugin.lpiv2.events.PluginDisconnectedEvent;
import org.interledger.plugin.lpiv2.events.PluginErrorEvent;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * An abstract implementation of {@link PluginMux}.
 */
public abstract class AbstractPluginMux<P extends Plugin<?>> implements PluginMux<P> {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());
  protected final Map<InterledgerAddress, P> plugins;

  private final UUID muxUniqueId = UUID.randomUUID();
  private final InterledgerAddress operatorAddress;
  private AtomicBoolean connected = new AtomicBoolean(NOT_CONNECTED);

  /**
   * Required-args Constructor.
   *
   * @param operatorAddress The {@link InterledgerAddress} of the node operating this MUX.
   */
  public AbstractPluginMux(final InterledgerAddress operatorAddress) {
    this.operatorAddress = Objects.requireNonNull(operatorAddress);
    this.plugins = Maps.newConcurrentMap();
  }

  @Override
  public InterledgerAddress getOperatorAddress() {
    return this.operatorAddress;
  }

  @Override
  public final CompletableFuture<Void> connect() {
    logger.info("Connecting all MUXed Account Plugins for MUX...");
    try {
      if (this.connected.compareAndSet(NOT_CONNECTED, CONNECTED)) {
        logger.info("Connecting All MUXed Plugins...");

        // Connect each plugin in this MUX...but only if they're not already connected...
        final List<CompletableFuture<Void>> result = this.plugins.values().stream()
            .filter(plugin -> !plugin.isConnected())
            .map(P::connect)
            .collect(Collectors.toList());
        return CompletableFuture.allOf(result.toArray(new CompletableFuture[0]))
            .thenApply(voidResult -> {
              logger.info("All MUXed Account Plugins Connected!");
              return null;
            });
      } else {
        // No-op because we're already connected...
        logger.warn("Connect() called on an already connected MUX!");
        return CompletableFuture.completedFuture(null);
      }
    } catch (RuntimeException e) {
      // If we can't connect, then disconnect this account in order to trigger any listeners.
      this.disconnect().join();
      throw e;
    }
  }

  @Override
  public void close() {
    this.disconnect().join();
  }

  @Override
  public final CompletableFuture<Void> disconnect() {
    logger.info("Disconnecting all MUXed Account Plugins...");

    if (this.connected.compareAndSet(CONNECTED, NOT_CONNECTED)) {

      // Disconnect each plugin in this MUX...but only if they're not already connected...
      final List<CompletableFuture<Void>> result = this.plugins.values().stream()
          .filter(P::isConnected)
          .map(P::disconnect)
          .collect(Collectors.toList());

      return CompletableFuture.allOf(result.toArray(new CompletableFuture[0]))
          .thenApply(voidResult -> {
            logger.info("All MUXed Account Plugins Disconnected!");
            return null;
          });
    } else {
      // No-op because we're already connected...
      logger.warn("Disconnect() called on an already disconnected MUX!");
      return CompletableFuture.completedFuture(null);
    }

  }

  /**
   * Query whether the plugin is currently connected.
   *
   * @return {@code true} if the plugin is connected, {@code false} otherwise.
   */
  @Override
  public boolean isConnected() {
    return this.connected.get();
  }

  @Override
  public Optional<P> getPlugin(final InterledgerAddress account) {
    Objects.requireNonNull(account);
    return Optional.ofNullable(this.plugins.get(account));
  }

  @Override
  public void registerPlugin(final InterledgerAddress account, final P plugin) {
    Objects.requireNonNull(account);
    Objects.requireNonNull(plugin);

    // If the plugin being added has not been added to this MUX, then register this MUX as an event-listener for the plugin.
    if (this.plugins.put(account, plugin) == null) {
      plugin.addPluginEventListener(this.muxUniqueId, this);
    }

    // If the MUX mux is connected, then eagerly attempt to connect the new plugin...
    if (this.isConnected() && !plugin.isConnected()) {
      plugin.connect();
    }
  }

  @Override
  public void unregisterPlugin(final InterledgerAddress account) {
    this.getPlugin(account).ifPresent(plugin -> {
      plugin.removePluginEventListener(this.muxUniqueId);
      plugin.disconnect();
    });

    this.plugins.remove(account);
  }

  /**
   * When a plugin connects, add a reference to it in this plugin.
   */
  @Override
  public void onConnect(final PluginConnectedEvent event) {
    // If a plugin connects, it means that auth succeeded, so move that plugin into the authenticated Map.
    this.registerPlugin(event.getPlugin().getPluginSettings().getPeerAccountAddress(), (P) event.getPlugin());
  }

  /**
   * Remove the plugin from this MUX if it's disconnected.
   */
  @Override
  public void onDisconnect(final PluginDisconnectedEvent event) {
    this.unregisterPlugin(event.getPlugin().getPluginSettings().getPeerAccountAddress());
  }

  @Override
  public void onError(final PluginErrorEvent event) {
    // No-op.
  }
}
