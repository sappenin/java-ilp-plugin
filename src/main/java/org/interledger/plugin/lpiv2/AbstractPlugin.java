package org.interledger.plugin.lpiv2;

import org.interledger.core.InterledgerFulfillPacket;
import org.interledger.core.InterledgerPreparePacket;
import org.interledger.core.InterledgerProtocolException;
import org.interledger.plugin.lpiv2.events.ImmutablePluginConnectedEvent;
import org.interledger.plugin.lpiv2.events.ImmutablePluginDisconnectedEvent;
import org.interledger.plugin.lpiv2.events.PluginConnectedEvent;
import org.interledger.plugin.lpiv2.events.PluginDisconnectedEvent;
import org.interledger.plugin.lpiv2.events.PluginErrorEvent;
import org.interledger.plugin.lpiv2.handlers.PluginEventHandler;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An abstract implementation of a {@link Plugin} that does directly connects emitted ledger events to proper handlers.
 */
public abstract class AbstractPlugin<T extends PluginSettings> implements Plugin<T> {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  /**
   * A typed representation of the configuration options passed-into this ledger plugin.
   */
  private final T pluginSettings;

  /**
   * Any registered event handlers for this plugin.
   */
  private final Map<UUID, PluginEventHandler> ledgerEventHandlers = Maps.newConcurrentMap();

  // The emitter used by this plugin.
  private PluginEventEmitter pluginEventEmitter;

  private AtomicBoolean connected = new AtomicBoolean(NOT_CONNECTED);

  /**
   * Required-args Constructor which utilizes a default {@link PluginEventEmitter} that synchronously connects to any
   * event handlers.
   *
   * @param pluginSettings A {@link T} that specified ledger plugin options.
   */
  protected AbstractPlugin(final T pluginSettings) {
    this.pluginSettings = Objects.requireNonNull(pluginSettings);
    this.pluginEventEmitter = new SyncPluginEventEmitter(this.ledgerEventHandlers);
  }

  /**
   * Required-args Constructor.
   *
   * @param pluginSettings     A {@link T} that specified ledger plugin options.
   * @param pluginEventEmitter A {@link PluginEventEmitter} that is used to emit events from this plugin.
   */
  protected AbstractPlugin(
      final T pluginSettings,
      final PluginEventEmitter pluginEventEmitter
  ) {
    this.pluginSettings = Objects.requireNonNull(pluginSettings);
    this.pluginEventEmitter = Objects.requireNonNull(pluginEventEmitter);
  }

  @Override
  public final void connect() {
    logger.info("[{}] `{}` connecting to `{}`...", this.pluginSettings.pluginTypeId(),
        this.pluginSettings.localNodeAddress(), this.getPluginSettings().peerAccount());

    try {
      if (!this.isConnected()) {
        this.connected.compareAndSet(NOT_CONNECTED, CONNECTED);
        this.doConnect();
        this.pluginEventEmitter.emitEvent(ImmutablePluginConnectedEvent.builder()
            .peerAccount(this.getPluginSettings().peerAccount())
            .build());

        logger.info("[{}] `{}` connected to `{}`", this.getPluginSettings().pluginTypeId(),
            this.pluginSettings.localNodeAddress(), this.getPluginSettings().peerAccount());

      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);

      // If we can't connect, then disconnect this account in order to trigger any listeners.
      this.disconnect();
    }

  }

  /**
   * Perform the logic of actually connecting to the remote peer.
   */
  public abstract void doConnect();

  @Override
  public final void disconnect() {
    logger.info("[{}] `{}` disconnecting from `{}`...", this.pluginSettings.pluginTypeId(),
        this.pluginSettings.localNodeAddress(), this.getPluginSettings().peerAccount());

    try {
      if (this.connected.compareAndSet(CONNECTED, NOT_CONNECTED)) {
        this.doDisconnect();
      }
      // In either case above, emit the disconnect event.
      this.pluginEventEmitter.emitEvent(ImmutablePluginDisconnectedEvent.builder()
          .peerAccount(this.getPluginSettings().peerAccount())
          .build());
    } catch (RuntimeException e) {
      // Even if an exception is thrown above, be sure to emit the disconnected event.
      this.pluginEventEmitter.emitEvent(ImmutablePluginDisconnectedEvent.builder()
          .peerAccount(this.getPluginSettings().peerAccount())
          .build());
      throw e;
    }

    logger.info("[{}] `{}` disconnected from `{}`.", this.pluginSettings.pluginTypeId(),
        this.pluginSettings.localNodeAddress(), this.getPluginSettings().peerAccount());
  }

  /**
   * Perform the logic of disconnecting from the remote peer.
   */
  public abstract void doDisconnect();

  /**
   * Query whether the plugin is currently connected.
   *
   * @return {@code true} if the plugin is connected, {@code false} otherwise.
   */
  @Override
  public boolean isConnected() {
    return this.connected.get();
  }

  protected PluginEventEmitter getPluginEventEmitter() {
    return this.pluginEventEmitter;
  }

  @Override
  public UUID addPluginEventHandler(final PluginEventHandler pluginEventHandler) {
    Objects.requireNonNull(pluginEventHandler);

    final UUID handlerId = UUID.randomUUID();
    this.ledgerEventHandlers.put(handlerId, pluginEventHandler);

    return handlerId;
  }

  @Override
  public void removePluginEventHandler(final UUID pluginEventHandler) {
    Objects.requireNonNull(pluginEventHandler);
    this.ledgerEventHandlers.remove(pluginEventHandler);
  }

  @Override
  public T getPluginSettings() {
    return this.pluginSettings;
  }

  /**
   * Delegates to {@link #doSendPacket(InterledgerPreparePacket)} so that implementations don't need to worry about
   * async behavior.
   */
  @Override
  public final CompletableFuture<InterledgerFulfillPacket> sendPacket(final InterledgerPreparePacket preparePacket)
      throws InterledgerProtocolException {
    Objects.requireNonNull(preparePacket);
    logger.debug("[{}] sendPacket: {}",
        this.pluginSettings.pluginTypeId(), preparePacket
    );
    // Handles checked and unchecked exceptions properly.
    return Completions.supplyAsync(() -> this.doSendPacket(preparePacket)).toCompletableFuture();
  }

  /**
   * Perform the logic of sending a packet to a remote peer.
   */
  public abstract InterledgerFulfillPacket doSendPacket(final InterledgerPreparePacket preparePacket)
      throws InterledgerProtocolException;

  @Override
  public final CompletableFuture<InterledgerFulfillPacket> handleIncomingPacket(
      final InterledgerPreparePacket preparePacket)
      throws InterledgerProtocolException {
    Objects.requireNonNull(preparePacket);
    logger.debug("[{}] sendPacket: {}",
        this.pluginSettings.pluginTypeId(), preparePacket
    );
    // Handles checked and unchecked exceptions properly.
    return Completions.supplyAsync(() -> this.doHandleIncomingPacket(preparePacket)).toCompletableFuture();
  }

  public abstract InterledgerFulfillPacket doHandleIncomingPacket(final InterledgerPreparePacket preparePacket)
      throws InterledgerProtocolException;

  @Override
  public final CompletableFuture<Void> settle(final BigInteger amount) {
    Objects.requireNonNull(amount);
    logger.info("[{}] settling {} units via {}!",
        this.pluginSettings.pluginTypeId(), amount, pluginSettings.peerAccount()
    );
    // Handles checked and unchecked exceptions properly.
    return Completions.supplyAsync(() -> this.doSettle(amount)).toCompletableFuture();
  }

  /**
   * Perform the logic of settling with a remote peer.
   */
  protected abstract void doSettle(BigInteger amount);

  /**
   * An example {@link PluginEventEmitter} that allows events to be synchronously emitted into a {@link Plugin}.
   */
  public static class SyncPluginEventEmitter implements PluginEventEmitter {

    private final Map<UUID, PluginEventHandler> ledgerEventHandlers;

    public SyncPluginEventEmitter(final Map<UUID, PluginEventHandler> ledgerEventHandlers) {
      this.ledgerEventHandlers = Objects.requireNonNull(ledgerEventHandlers);
    }

    /////////////////
    // Event Emitters
    /////////////////

    @Override
    public void emitEvent(final PluginConnectedEvent event) {
      this.ledgerEventHandlers.values().stream().forEach(handler -> handler.onConnect(event));
    }

    @Override
    public void emitEvent(final PluginDisconnectedEvent event) {
      this.ledgerEventHandlers.values().stream().forEach(handler -> handler.onDisconnect(event));
    }

    @Override
    public void emitEvent(final PluginErrorEvent event) {
      this.ledgerEventHandlers.values().stream().forEach(handler -> handler.onError(event));
    }
  }
}
