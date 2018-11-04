package org.interledger.plugin.lpiv2;

import org.interledger.core.InterledgerPreparePacket;
import org.interledger.core.InterledgerResponsePacket;
import org.interledger.plugin.lpiv2.events.ImmutablePluginConnectedEvent;
import org.interledger.plugin.lpiv2.events.ImmutablePluginDisconnectedEvent;
import org.interledger.plugin.lpiv2.events.PluginConnectedEvent;
import org.interledger.plugin.lpiv2.events.PluginDisconnectedEvent;
import org.interledger.plugin.lpiv2.events.PluginErrorEvent;
import org.interledger.plugin.lpiv2.events.PluginEventEmitter;
import org.interledger.plugin.lpiv2.events.PluginEventHandler;
import org.interledger.plugin.lpiv2.exceptions.DataHandlerAlreadyRegisteredException;
import org.interledger.plugin.lpiv2.exceptions.MoneyHandlerAlreadyRegisteredException;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An abstract implementation of a {@link Plugin} that does directly connects emitted ledger events to proper handlers.
 */
public abstract class AbstractPlugin<T extends PluginSettings> implements Plugin<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPlugin.class);

  /**
   * A typed representation of the configuration options passed-into this ledger plugin.
   */
  private final T pluginSettings;

  /**
   * Any registered event handlers for this plugin.
   */
  private final Map<UUID, PluginEventHandler> pluginEventHandlers = Maps.newConcurrentMap();

  // The emitter used by this plugin.
  private PluginEventEmitter pluginEventEmitter;

  private AtomicBoolean connected = new AtomicBoolean(NOT_CONNECTED);

  private AtomicReference<IlpDataHandler> dataHandlerAtomicReference = new AtomicReference<>();
  private AtomicReference<IlpMoneyHandler> moneyHandlerAtomicReference = new AtomicReference<>();

  /**
   * Required-args Constructor which utilizes a default {@link PluginEventEmitter} that synchronously connects to any
   * event handlers.
   *
   * @param pluginSettings A {@link T} that specified ledger plugin options.
   */
  protected AbstractPlugin(final T pluginSettings) {
    this.pluginSettings = Objects.requireNonNull(pluginSettings);
    this.pluginEventEmitter = new SyncPluginEventEmitter(this.pluginEventHandlers);
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
  public final CompletableFuture<Void> connect() {
    LOGGER.info("[{}] `{}` connecting to `{}`...", this.pluginSettings.getPluginType(),
        this.pluginSettings.getLocalNodeAddress(), this.getPluginSettings().getPeerAccountAddress());

    try {
      if (!this.isConnected()) {
        // Do this first as a thread-safe gate.
        this.connected.compareAndSet(NOT_CONNECTED, CONNECTED);

        // Can't connect without handlers
        if (this.dataHandlerAtomicReference.get() == null) {
          throw new RuntimeException("You MUST register a dataHandler before connecting this plugin!");
        }
        if (this.moneyHandlerAtomicReference.get() == null) {
          throw new RuntimeException("You MUST register a moneyHandler before connecting this plugin!");
        }

        return this.doConnect().whenComplete((result, error) -> {
          this.pluginEventEmitter.emitEvent(ImmutablePluginConnectedEvent.builder()
              .peerAccountAddress(this.getPluginSettings().getPeerAccountAddress())
              .build());
          LOGGER.info("[{}] `{}` connected to `{}`", this.getPluginSettings().getPluginType(),
              this.pluginSettings.getLocalNodeAddress(), this.getPluginSettings().getPeerAccountAddress());
        });
      } else {
        // Nothing todo, we're already connected...
        return CompletableFuture.completedFuture(null);
      }
    } catch (RuntimeException e) {
      // If we can't connect, then disconnect this account in order to trigger any listeners.
      this.disconnect().join();
      throw e;
    }
  }

  /**
   * Perform the logic of actually connecting to the remote peer.
   */
  public abstract CompletableFuture<Void> doConnect();

  @Override
  public final CompletableFuture<Void> disconnect() {
    LOGGER.info("[{}] `{}` disconnecting from `{}`...", this.pluginSettings.getPluginType(),
        this.pluginSettings.getLocalNodeAddress(), this.getPluginSettings().getPeerAccountAddress());

    try {
      if (this.connected.compareAndSet(CONNECTED, NOT_CONNECTED)) {
        return this.doDisconnect().thenAccept(($) -> {
          // In either case above, emit the disconnect event.
          this.pluginEventEmitter.emitEvent(ImmutablePluginDisconnectedEvent.builder()
              .peerAccountAddress(this.getPluginSettings().getPeerAccountAddress())
              .build());

          LOGGER.info("[{}] `{}` disconnected from `{}`.", this.pluginSettings.getPluginType(),
              this.pluginSettings.getLocalNodeAddress(), this.getPluginSettings().getPeerAccountAddress());
        });
      } else {
        return CompletableFuture.completedFuture(null);
      }
    } catch (RuntimeException e) {
      // Even if an exception is thrown above, be sure to emit the disconnected event.
      this.pluginEventEmitter.emitEvent(ImmutablePluginDisconnectedEvent.builder()
          .peerAccountAddress(this.getPluginSettings().getPeerAccountAddress())
          .build());
      throw e;
    }
  }

  /**
   * Perform the logic of disconnecting from the remote peer.
   */
  public abstract CompletableFuture<Void> doDisconnect();

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
    this.pluginEventHandlers.put(handlerId, pluginEventHandler);

    return handlerId;
  }

  @Override
  public void removePluginEventHandler(final UUID pluginEventHandler) {
    Objects.requireNonNull(pluginEventHandler);
    this.pluginEventHandlers.remove(pluginEventHandler);
  }

  @Override
  public T getPluginSettings() {
    return this.pluginSettings;
  }

  /**
   * Delegates to {@link #doSendData(InterledgerPreparePacket)} so that implementations don't need to worry about async
   * behavior.
   */
  @Override
  public final CompletableFuture<InterledgerResponsePacket> sendData(final InterledgerPreparePacket preparePacket) {
    Objects.requireNonNull(preparePacket);

    LOGGER.debug("[{}] sendData: {}", this.pluginSettings.getPluginType(), preparePacket);

    return this.doSendData(preparePacket);
  }

  /**
   * Perform the logic of sending a packet to a remote peer.
   */
  public abstract CompletableFuture<InterledgerResponsePacket> doSendData(final InterledgerPreparePacket preparePacket);

  @Override
  public void registerDataHandler(final IlpDataHandler ilpDataHandler) throws DataHandlerAlreadyRegisteredException {
    Objects.requireNonNull(ilpDataHandler, "ilpDataHandler must not be null!");
    if (!this.dataHandlerAtomicReference.compareAndSet(null, ilpDataHandler)) {
      throw new DataHandlerAlreadyRegisteredException(
          "IlpDataHandler may not be registered twice. Call unregisterDataHandler first!",
          this.getPluginSettings().getLocalNodeAddress()
      );
    }
  }

  @Override
  public IlpDataHandler getDataHandler() {
    return dataHandlerAtomicReference.get();
  }


  @Override
  public IlpMoneyHandler getMoneyHandler() {
    return moneyHandlerAtomicReference.get();
  }

  /**
   * Removes the currently used {@link IlpDataHandler}. This has the same effect as if {@link
   * #registerDataHandler(IlpDataHandler)} had never been called. If no data handler is currently set, this method does
   * nothing.
   */
  @Override
  public void unregisterDataHandler() {
    this.dataHandlerAtomicReference.set(null);
  }

  @Override
  public final CompletableFuture<Void> sendMoney(final BigInteger amount) {
    Objects.requireNonNull(amount);
    LOGGER.info("[{}] settling {} units via {}!",
        this.pluginSettings.getPluginType(), amount, pluginSettings.getPeerAccountAddress()
    );
    // Handles checked and unchecked exceptions properly.
    //return Completions.supplyAsync(() -> this.doSendMoney(amount)).toCompletableFuture();
    return this.doSendMoney(amount);
  }

  /**
   * Perform the logic of settling with a remote peer.
   */
  protected abstract CompletableFuture<Void> doSendMoney(final BigInteger amount);

  /**
   * <p>Set the callback which is used to handle incoming money. The callback should expect one parameter (the amount)
   * and return a {@link CompletableFuture}. If an error occurs, the callback MAY throw an exception. In general, the
   * callback should behave as {@link #sendMoney(BigInteger)} does.</p>
   *
   * <p>If a money handler is already set, this method throws a {@link MoneyHandlerAlreadyRegisteredException}. In
   * order to change the money handler, the old handler must first be removed via {@link #unregisterMoneyHandler()}.
   * This is to ensure that handlers are not overwritten by accident.</p>
   *
   * <p>If incoming money is received by the plugin, but no handler is registered, the plugin SHOULD return an error
   * (and MAY return the money.)</p>
   *
   * @param ilpMoneyHandler An instance of {@link IlpMoneyHandler}.
   */
  @Override
  public void registerMoneyHandler(final IlpMoneyHandler ilpMoneyHandler)
      throws MoneyHandlerAlreadyRegisteredException {
    Objects.requireNonNull(ilpMoneyHandler, "ilpMoneyHandler must not be null!");
    if (!this.moneyHandlerAtomicReference.compareAndSet(null, ilpMoneyHandler)) {
      throw new MoneyHandlerAlreadyRegisteredException(
          "IlpMoneyHandler may not be registered twice. Call unregisterMoneyHandler first!",
          this.getPluginSettings().getLocalNodeAddress()
      );
    }
  }

  /**
   * Removes the currently used money handler. This has the same effect as if {@link
   * #registerMoneyHandler(IlpMoneyHandler)} had never been called. If no money handler is currently set, this method
   * does nothing.
   */
  @Override
  public void unregisterMoneyHandler() {
    this.moneyHandlerAtomicReference.set(null);
  }

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
