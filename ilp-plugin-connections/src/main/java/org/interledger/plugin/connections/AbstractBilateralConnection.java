package org.interledger.plugin.connections;

import org.interledger.plugin.connections.settings.BilateralConnectionSettings;

/**
 * <p>An abstract implementation of {@link BilateralConnection}. By default, Connections support multiple
 * Accounts/Plugins, although sub-classes may restrict this behavior in order to support certain single-account
 * protocols like vanilla BTP, which only supports `auth_token` and assumes only a single account per Websocket
 * connection.</p>
 *
 * <p>The actual network transport connections are contained in sub-classes of this class, and are handed-off to
 * plugins when the plugins are instantiated. This ensures a uniform design regardless of if a connection has a single
 * underlying transport, or two transports, such as in the BPP protocol.
 */
public abstract class AbstractBilateralConnection<CS extends BilateralConnectionSettings> {
  // implementsBilateralConnection<CS> {

//  private final AtomicBoolean connected = new AtomicBoolean(NOT_CONNECTED);
//  private final CS connectionSettings;
//
//  private final PluginFactory pluginFactory;
//  protected Logger logger = LoggerFactory.getLogger(this.getClass());
//
//  /**
//   * Required-args Constructor.
//   */
//  public AbstractBilateralConnection(
//      final CS connectionSettings,
//      final PluginFactory pluginFactory
//  ) {
//    // TODO: Use EventBus instead
//    //this.eventEmitter = Objects.requireNonNull(eventEmitter);
//
//    this.connectionSettings = Objects.requireNonNull(connectionSettings);
//    this.pluginFactory = Objects.requireNonNull(pluginFactory);
//  }
//
//  // TODO: Connection Events!
//
////  /**
////   * An example {@link BilateralConnectionEventEmitter} that allows events to be synchronously emitted to any
////   * listeners.
////   *
////   * @deprecated Transition this to EventBus.
////   */
////  @Deprecated
////  public static class SyncBilateralConnectionEventEmitter implements BilateralConnectionEventEmitter {
////
////    private final Map<UUID, BilateralConnectionEventListener> eventListeners;
////
////    public SyncBilateralConnectionEventEmitter() {
////      this.eventListeners = Maps.newConcurrentMap();
////    }
////
////    /////////////////
////    // Event Emitters
////    /////////////////
////
////    @Override
////    public void emitEvent(final BilateralConnectionConnectedEvent event) {
////      Objects.requireNonNull(event);
////      this.eventListeners.values().stream().forEach(handler -> handler.onConnect(event));
////    }
////
////    @Override
////    public void emitEvent(final BilateralConnectionDisconnectedEvent event) {
////      Objects.requireNonNull(event);
////      this.eventListeners.values().stream().forEach(handler -> handler.onDisconnect(event));
////    }
////
////    @Override
////    public void addEventListener(
////        final UUID eventListenerId, final BilateralConnectionEventListener eventListener
////    ) {
////      Objects.requireNonNull(eventListenerId);
////      Objects.requireNonNull(eventListener);
////      this.eventListeners.put(eventListenerId, eventListener);
////    }
////
////    @Override
////    public void removeEventListener(final UUID eventListenerId) {
////      Objects.requireNonNull(eventListenerId);
////      this.eventListeners.remove(eventListenerId);
////    }
////  }
//
//  @Override
//  public boolean isConnected() {
//    return this.connected.get();
//  }
//
//  /**
//   * <p>Connect to the remote peer.</p>
//   */
//  @Override
//  public final CompletableFuture<Void> connect() {
//    // Try to connect, but no-op if already connected.
//    if (this.connected.compareAndSet(NOT_CONNECTED, CONNECTED)) {
//      return this.doConnectTransport().thenApply($ -> {
//        logger.info("Connected to {}.", getConnectionSettings().getRemoteAddress());
//        return null;
//      });
//    } else {
//      logger.warn("Connection already connected to {}!", getConnectionSettings().getRemoteAddress());
//      return CompletableFuture.completedFuture(null);
//    }
//  }
//
//  /**
//   * Perform the logic of connecting the actual transport(s) supporting this bilateral connection.
//   */
//  public abstract CompletableFuture<Void> doConnectTransport();
//
//  /**
//   * Disconnect from the remote peer.
//   */
//  @Override
//  public final CompletableFuture<Void> disconnect() {
//    // Try to disconnect, but no-op if already disconnected.
//    if (this.connected.compareAndSet(CONNECTED, NOT_CONNECTED)) {
//      return this.doDisconnectTransport().thenApply($ -> {
//        logger.info("Disconnected from {}.", getConnectionSettings().getRemoteAddress());
//        return null;
//      });
//    } else {
//      logger.warn("Connection already disconnected from {}!", getConnectionSettings().getRemoteAddress());
//      return CompletableFuture.completedFuture(null);
//    }
//  }
//
//  /**
//   * Perform the logic of disconnecting the actual transport(s) supporting this bilateral connection.
//   */
//  public abstract CompletableFuture<Void> doDisconnectTransport();
//
//  @Override
//  public CS getConnectionSettings() {
//    return this.connectionSettings;
//  }
//
//  public PluginFactory getPluginFactory() {
//    return pluginFactory;
//  }
//

}
