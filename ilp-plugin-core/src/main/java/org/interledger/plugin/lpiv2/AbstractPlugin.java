package org.interledger.plugin.lpiv2;

import org.interledger.plugin.lpiv2.events.PluginConnectedEvent;
import org.interledger.plugin.lpiv2.events.PluginDisconnectedEvent;
import org.interledger.plugin.lpiv2.events.PluginErrorEvent;
import org.interledger.plugin.lpiv2.events.PluginEventEmitter;
import org.interledger.plugin.lpiv2.events.PluginEventListener;
import org.interledger.plugin.lpiv2.exceptions.DataHandlerAlreadyRegisteredException;
import org.interledger.plugin.lpiv2.exceptions.DataSenderAlreadyRegisteredException;
import org.interledger.plugin.lpiv2.exceptions.MoneyHandlerAlreadyRegisteredException;
import org.interledger.plugin.lpiv2.exceptions.MoneySenderAlreadyRegisteredException;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An abstract implementation of a {@link Plugin} that provides scaffolding for all plugin implementations.
 */
public abstract class AbstractPlugin<T extends PluginSettings> implements Plugin<T> {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  /**
   * A typed representation of the configuration options passed-into this ledger plugin.
   */
  private final T pluginSettings;

  private final AtomicBoolean connected = new AtomicBoolean(NOT_CONNECTED);

  // The emitter used by this plugin.
  private PluginEventEmitter pluginEventEmitter;

  private AtomicReference<DataSender> dataSenderAtomicReference = new AtomicReference<>();

  // TODO: Use a no-op MoneyHandler by default, and remove checks in connect/disconnect.
  private AtomicReference<MoneySender> moneySenderAtomicReference = new AtomicReference<>();

  private AtomicReference<DataHandler> dataHandlerAtomicReference = new AtomicReference<>();

  // TODO: Use a no-op MoneyHandler by default, and remove checks in connect/disconnect.
  private AtomicReference<MoneyHandler> moneyHandlerAtomicReference = new AtomicReference<>();

  /**
   * Required-args Constructor which utilizes a default {@link PluginEventEmitter} that synchronously connects to any
   * event handlers.
   *
   * @param pluginSettings A {@link T} that specified ledger plugin options.
   */
  protected AbstractPlugin(final T pluginSettings) {
    this(pluginSettings, new SyncPluginEventEmitter());
  }

  /**
   * Required-args Constructor.
   *
   * @param pluginSettings     A {@link T} that specified ledger plugin options.
   * @param pluginEventEmitter A {@link PluginEventEmitter} that is used to emit events from this plugin.
   */
  protected AbstractPlugin(final T pluginSettings, final PluginEventEmitter pluginEventEmitter) {
    this.pluginSettings = Objects.requireNonNull(pluginSettings);
    this.pluginEventEmitter = Objects.requireNonNull(pluginEventEmitter);
  }

  @Override
  public final CompletableFuture<Void> connect() {
    try {
      if (this.connected.compareAndSet(NOT_CONNECTED, CONNECTED)) {
        logger.debug("[{}] `{}` connecting to `{}`...", this.pluginSettings.getPluginType(),
            this.pluginSettings.getLocalNodeAddress(), this.getPluginSettings().getPeerAccountAddress());

        return this.doConnect()
            .whenComplete(($, error) -> {
              if (error == null) {
                // Emit a connected event...
                this.pluginEventEmitter.emitEvent(PluginConnectedEvent.of(this));

                logger.debug("[{}] `{}` connected to `{}`", this.getPluginSettings().getPluginType(),
                    this.pluginSettings.getLocalNodeAddress(), this.getPluginSettings().getPeerAccountAddress());
              } else {
                final String errorMessage = String.format("[%s] `%s` error while trying to connect to `%s`",
                    this.pluginSettings.getPluginType(),
                    this.pluginSettings.getLocalNodeAddress(), this.getPluginSettings().getPeerAccountAddress()
                );
                logger.error(errorMessage, error);
              }
            });
      } else {
        logger.debug("[{}] `{}` already connected to `{}`...", this.pluginSettings.getPluginType(),
            this.pluginSettings.getLocalNodeAddress(), this.getPluginSettings().getPeerAccountAddress());
        // No-op: We're already expectedCurrentState...
        return CompletableFuture.completedFuture(null);
      }
    } catch (RuntimeException e) {
      // If we can't connect, then disconnect this account in order to trigger any listeners.
      this.disconnect().join();
      throw e;
    } catch (Exception e) {
      // If we can't connect, then disconnect this account in order to trigger any listeners.
      this.disconnect().join();
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Perform the logic of actually connecting to the remote peer.
   */
  public abstract CompletableFuture<Void> doConnect();

  @Override
  public void close() {
    this.disconnect().join();
  }

  @Override
  public final CompletableFuture<Void> disconnect() {
    try {
      if (this.connected.compareAndSet(CONNECTED, NOT_CONNECTED)) {
        logger.debug("[{}] `{}` disconnecting from `{}`...", this.pluginSettings.getPluginType(),
            this.pluginSettings.getLocalNodeAddress(), this.getPluginSettings().getPeerAccountAddress());

        return this.doDisconnect()
            .whenComplete(($, error) -> {
              if (error == null) {
                // emit disconnected event.
                this.pluginEventEmitter.emitEvent(PluginDisconnectedEvent.of(this));

                logger.debug("[{}] `{}` disconnected from `{}`.", this.pluginSettings.getPluginType(),
                    this.pluginSettings.getLocalNodeAddress(), this.getPluginSettings().getPeerAccountAddress());
              } else {
                final String errorMessage = String.format("[%s] `%s` error while trying to disconnect from `%s`",
                    this.pluginSettings.getPluginType(),
                    this.pluginSettings.getLocalNodeAddress(), this.getPluginSettings().getPeerAccountAddress()
                );
                logger.error(errorMessage, error);
              }
            })
            .thenAccept(($) -> {
              logger.debug("[{}] `{}` disconnected from `{}`...", this.pluginSettings.getPluginType(),
                  this.pluginSettings.getLocalNodeAddress(), this.getPluginSettings().getPeerAccountAddress());
            });
      } else {
        logger.debug("[{}] `{}` already disconnected from `{}`...", this.pluginSettings.getPluginType(),
            this.pluginSettings.getLocalNodeAddress(), this.getPluginSettings().getPeerAccountAddress());
        // No-op: We're already expectedCurrentState...
        return CompletableFuture.completedFuture(null);
      }
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Perform the logic of disconnecting from the remote peer.
   */
  public abstract CompletableFuture<Void> doDisconnect();

  @Override
  public boolean isConnected() {
    return this.connected.get();
  }

  protected PluginEventEmitter getPluginEventEmitter() {
    return this.pluginEventEmitter;
  }

  @Override
  public void addPluginEventListener(final UUID listenerId, final PluginEventListener pluginEventListener) {
    Objects.requireNonNull(pluginEventListener);
    this.pluginEventEmitter.addPluginEventListener(listenerId, pluginEventListener);
  }

  @Override
  public void removePluginEventListener(final UUID listenerId) {
    Objects.requireNonNull(listenerId);
    this.pluginEventEmitter.removePluginEventListener(listenerId);
  }

  @Override
  public T getPluginSettings() {
    return this.pluginSettings;
  }

  @Override
  public void registerDataSender(final DataSender dataSender)
      throws DataHandlerAlreadyRegisteredException {
    Objects.requireNonNull(dataSender, "dataSender must not be null!");
    if (!this.dataSenderAtomicReference.compareAndSet(null, dataSender)) {
      throw new DataSenderAlreadyRegisteredException(
          "DataSender may not be registered twice. Call unregisterDataSender first!",
          this.getPluginSettings().getLocalNodeAddress()
      );
    }
  }

  @Override
  public Optional<DataSender> getDataSender() {
    return Optional.ofNullable(this.dataSenderAtomicReference.get());
  }

  @Override
  public void unregisterDataSender() {
    this.dataSenderAtomicReference.set(null);
  }

  @Override
  public void registerDataHandler(final DataHandler ilpDataHandler)
      throws DataHandlerAlreadyRegisteredException {
    Objects.requireNonNull(ilpDataHandler, "ilpDataHandler must not be null!");
    if (!this.dataHandlerAtomicReference.compareAndSet(null, ilpDataHandler)) {
      throw new DataHandlerAlreadyRegisteredException(
          "DataHandler may not be registered twice. Call unregisterDataHandler first!",
          this.getPluginSettings().getLocalNodeAddress()
      );
    }
  }

  @Override
  public Optional<DataHandler> getDataHandler() {
    return Optional.ofNullable(this.dataHandlerAtomicReference.get());
  }

  @Override
  public void unregisterDataHandler() {
    this.dataHandlerAtomicReference.set(null);
  }

  @Override
  public Optional<MoneyHandler> getMoneyHandler() {
    return Optional.ofNullable(moneyHandlerAtomicReference.get());
  }

  @Override
  public void registerMoneyHandler(final MoneyHandler moneyHandler)
      throws MoneyHandlerAlreadyRegisteredException {
    Objects.requireNonNull(moneyHandler, "moneyHandler must not be null!");
    if (!this.moneyHandlerAtomicReference.compareAndSet(null, moneyHandler)) {
      throw new MoneyHandlerAlreadyRegisteredException(
          "MoneyHandler may not be registered twice. Call unregisterMoneyHandler first!",
          this.getPluginSettings().getLocalNodeAddress()
      );
    }
  }

  @Override
  public void unregisterMoneyHandler() {
    this.moneyHandlerAtomicReference.set(null);
  }

  @Override
  public Optional<MoneySender> getMoneySender() {
    return Optional.ofNullable(moneySenderAtomicReference.get());
  }

  @Override
  public void registerMoneySender(final MoneySender moneySender)
      throws MoneySenderAlreadyRegisteredException {
    Objects.requireNonNull(moneySender, "moneySender must not be null!");
    if (!this.moneySenderAtomicReference.compareAndSet(null, moneySender)) {
      throw new MoneyHandlerAlreadyRegisteredException(
          "MoneySender may not be registered twice. Call unregisterMoneySender first!",
          this.getPluginSettings().getLocalNodeAddress()
      );
    }
  }

  @Override
  public void unregisterMoneySender() {
    this.moneySenderAtomicReference.set(null);
  }

  /**
   * An example {@link PluginEventEmitter} that allows events to be synchronously emitted into a {@link Plugin}.
   *
   * @deprecated Transition this to EventBus.
   */
  @Deprecated
  public static class SyncPluginEventEmitter implements PluginEventEmitter {

    private final Map<UUID, PluginEventListener> pluginEventListeners;

    public SyncPluginEventEmitter() {
      this.pluginEventListeners = Maps.newConcurrentMap();
    }

    /////////////////
    // Event Emitters
    /////////////////

    @Override
    public void emitEvent(final PluginConnectedEvent event) {
      this.pluginEventListeners.values().stream().forEach(handler -> handler.onConnect(event));
    }

    @Override
    public void emitEvent(final PluginDisconnectedEvent event) {
      this.pluginEventListeners.values().stream().forEach(handler -> handler.onDisconnect(event));
    }

    @Override
    public void emitEvent(final PluginErrorEvent event) {
      this.pluginEventListeners.values().stream().forEach(handler -> handler.onError(event));
    }


    @Override
    public void addPluginEventListener(final UUID listenerId, final PluginEventListener pluginEventListener) {
      Objects.requireNonNull(pluginEventListener);
      this.pluginEventListeners.put(listenerId, pluginEventListener);
    }

    @Override
    public void removePluginEventListener(final UUID pluginEventHandler) {
      Objects.requireNonNull(pluginEventHandler);
      this.pluginEventListeners.remove(pluginEventHandler);
    }
  }
}
