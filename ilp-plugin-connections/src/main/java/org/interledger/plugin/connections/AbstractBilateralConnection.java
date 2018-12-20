package org.interledger.plugin.connections;

import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.connections.mux.BilateralReceiverMux;
import org.interledger.plugin.connections.mux.BilateralSenderMux;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * <p>An abstract implementation of {@link BilateralConnection}. By default, Connections support multiple
 * Accounts/Plugins, although sub-classes may restrict this behavior in order to support certain single-account
 * protocols like vanilla BTP, which only supports `auth_token` and assumes only a single account per Websocket
 * connection.</p>
 *
 * <p>Generally, however, this mechanism enables distinct sender and receiver MUXes that can operate on distinct
 * network transports. For example, BPP utilizes different network connections for outgoing data vs incoming data.</p>
 *
 * <p>Note that the actual network transport connection handling always occurs in the MUX level, and never at the
 * Bilateral Connection level. This ensures a uniform design regardless of if a connection has a single underlying
 * transport, or two transports (one in the senderMux and one in the receiverMux).</p>
 */
public class AbstractBilateralConnection<SM extends BilateralSenderMux, RM extends BilateralReceiverMux>
    implements BilateralConnection<SM, RM> {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  //private final UUID bilateralConnectionId = UUID.randomUUID();
  private final InterledgerAddress operatorAddress;

  // E.g., a gRPC client or server.
  private final SM bilateralSenderMux;
  private final RM bilateralReceiverMux;

  //private final BilateralConnectionTracker connectionTracker;

  // NOTE: A Bilateral Connection should emit sender and receiver specific MUX events instead of its own events because
  // anyone using this BC abstraction will want to be able to tell the difference between the sender and receiver connecting.

  // The emitter used by this plugin.
  //private BilateralConnectionEventEmitter eventEmitter;

//  /**
//   * Required-args Constructor.
//   */
//  public AbstractBilateralConnection(
//      final InterledgerAddress operatorAddress, final SM bilateralSenderMux, final RM bilateralReceiverMux
//  ) {
//    this(operatorAddress, bilateralSenderMux, bilateralReceiverMux, new SyncBilateralConnectionEventEmitter()
//    );
//  }

  /**
   * Required-args Constructor.
   */
  public AbstractBilateralConnection(
      final InterledgerAddress operatorAddress, final SM bilateralSenderMux, final RM bilateralReceiverMux
      //final BilateralConnectionEventEmitter eventEmitter
  ) {
    this.operatorAddress = Objects.requireNonNull(operatorAddress);
    this.bilateralSenderMux = Objects.requireNonNull(bilateralSenderMux);
    this.bilateralReceiverMux = Objects.requireNonNull(bilateralReceiverMux);

    // TODO: Use EventBus instead
    //this.eventEmitter = Objects.requireNonNull(eventEmitter);
  }

  @Override
  public InterledgerAddress getOperatorAddress() {
    return this.operatorAddress;
  }

//  @Override
//  public final CompletableFuture<Void> connect() {
//    logger.debug("Connecting BilateralConnection: `{}`...", bilateralConnectionId);
//    return this.connectionTracker.connect();
//  }
//
//  @Override
//  public final void close() {
//    this.disconnect().join();
//  }
//
//  @Override
//  public final CompletableFuture<Void> disconnect() {
//    logger.debug("Disconnecting BilateralConnection: `{}`...", bilateralConnectionId);
//    return this.connectionTracker.disconnect();
//  }

//  @Override
//  public final boolean isConnected() {
//    return this.connectionTracker.isConnected();
//  }

  @Override
  public RM getBilateralReceiverMux() {
    return this.bilateralReceiverMux;
  }

//  @Override
//  public void addConnectionEventListener(
//      final UUID eventListenerId, final BilateralConnectionEventListener eventListener
//  ) {
//    Objects.requireNonNull(eventListenerId);
//    Objects.requireNonNull(eventListener);
//    this.eventEmitter.addEventListener(eventListenerId, eventListener);
//  }
//
//  @Override
//  public void removeConnectionEventListener(UUID eventListenerId) {
//    Objects.requireNonNull(eventListenerId);
//    this.eventEmitter.removeEventListener(eventListenerId);
//  }

  @Override
  public SM getBilateralSenderMux() {
    return this.bilateralSenderMux;
  }

//  @Override
//  public void onConnect(final BilateralConnectionConnectedEvent event) {
//    Objects.requireNonNull(event);
//    logger.debug("BilateralSenderMuxConnectedEvent: {}", event);
//
//    // Each MUX is already listening to BC events, so this is a no-op.
//  }
//
//  @Override
//  public void onDisconnect(final BilateralConnectionDisconnectedEvent event) {
//    Objects.requireNonNull(event);
//    logger.debug("BilateralSenderMuxDisconnectedEvent: {}", event);
//
//    // Each MUX is already listening to BC events, so this is a no-op.
//  }

//  protected BilateralConnectionEventEmitter getEventEmitter() {
//    return eventEmitter;
//  }
//
//  /**
//   * Called to handle an {@link BilateralReceiverMuxConnectedEvent}.
//   *
//   * @param event A {@link BilateralReceiverMuxConnectedEvent}.
//   */
//  @Override
//  public void onConnect(BilateralReceiverMuxConnectedEvent event) {
//    throw new RuntimeException("TODO");
//  }
//
//  /**
//   * Called to handle an {@link BilateralReceiverMuxDisconnectedEvent}.
//   *
//   * @param event A {@link BilateralReceiverMuxDisconnectedEvent}.
//   */
//  @Override
//  public void onDisconnect(BilateralReceiverMuxDisconnectedEvent event) {
//    throw new RuntimeException("TODO");
//  }
//
//  /**
//   * Called to handle an {@link BilateralSenderMuxConnectedEvent}.
//   *
//   * @param event A {@link BilateralSenderMuxConnectedEvent}.
//   */
//  @Override
//  public void onConnect(BilateralSenderMuxConnectedEvent event) {
//    throw new RuntimeException("TODO");
//  }
//
//  /**
//   * Called to handle an {@link BilateralSenderMuxDisconnectedEvent}.
//   *
//   * @param event A {@link BilateralSenderMuxDisconnectedEvent}.
//   */
//  @Override
//  public void onDisconnect(BilateralSenderMuxDisconnectedEvent event) {
//    throw new RuntimeException("TODO");
//  }

//  /**
//   * An example {@link BilateralConnectionEventEmitter} that allows events to be synchronously emitted to any
//   * listeners.
//   *
//   * @deprecated Transition this to EventBus.
//   */
//  @Deprecated
//  public static class SyncBilateralConnectionEventEmitter implements BilateralConnectionEventEmitter {
//
//    private final Map<UUID, BilateralConnectionEventListener> eventListeners;
//
//    public SyncBilateralConnectionEventEmitter() {
//      this.eventListeners = Maps.newConcurrentMap();
//    }
//
//    /////////////////
//    // Event Emitters
//    /////////////////
//
//    @Override
//    public void emitEvent(final BilateralConnectionConnectedEvent event) {
//      Objects.requireNonNull(event);
//      this.eventListeners.values().stream().forEach(handler -> handler.onConnect(event));
//    }
//
//    @Override
//    public void emitEvent(final BilateralConnectionDisconnectedEvent event) {
//      Objects.requireNonNull(event);
//      this.eventListeners.values().stream().forEach(handler -> handler.onDisconnect(event));
//    }
//
//    @Override
//    public void addEventListener(
//        final UUID eventListenerId, final BilateralConnectionEventListener eventListener
//    ) {
//      Objects.requireNonNull(eventListenerId);
//      Objects.requireNonNull(eventListener);
//      this.eventListeners.put(eventListenerId, eventListener);
//    }
//
//    @Override
//    public void removeEventListener(final UUID eventListenerId) {
//      Objects.requireNonNull(eventListenerId);
//      this.eventListeners.remove(eventListenerId);
//    }
//  }

}
