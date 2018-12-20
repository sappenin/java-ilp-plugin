package org.interledger.plugin.connections;

import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.connections.mux.BilateralReceiverMux;
import org.interledger.plugin.connections.mux.BilateralSenderMux;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;

/**
 * <p>A Bilateral Connection is a network connection between two ILP nodes, allowing multiple Plugins to multiplex
 * over a single connection.</p>
 *
 * <p>In a multiplexed scenario, traffic will flow bidirectionally over the following components:</p>
 * <pre>
 * ┌─────────┐                ┌───────┐                                ┌───────┐                ┌─────────┐
 * │         │                │       │                                │       │                │         │
 * │ Client  │                │       │                                │       │                │   Bob   │
 * │Plugin 1 │◁ ─Account 1─ ─▷│       │                                │       │◁─ ─Account 1─ ▷│Plugin 1 │
 * │         │                │       ├───────────┐        ┌───────────┤       │                │         │
 * └─────────┘                │Client │           │        │           │Client │                └─────────┘
 *                            │Plugin │ WebSocket │        │ WebSocket │Plugin │
 * ┌─────────┐                │ Conn  │  Client   │◁─HTTP─▷│  Server   │  Conn │                ┌─────────┐
 * │         │                │       │           │        │           │       │                │         │
 * │ Client  │                │       ├───────────┘        └───────────┤       │                │ Client  │
 * │Plugin 2 │◁ ─Account 2─ ─▷│       │                                │       │◁─ ─Account 2─ ▷│Plugin 2 │
 * │         │                │       │                                │       │                │         │
 * └─────────┘                └───────┘                                │       │                └─────────┘
 *                                                                     └───────┘
 * </pre>
 *
 * <p>While the above illustrates a potential server-to-server configuration, the following diagram represents a more
 * typical client-server configuration:</p>
 *
 * <pre>
 *                                                                     ┌───────┐                ┌─────────┐
 *                                                                     │       │                │         │
 *                                                                     │       │                │Plugin 1 │
 *                                                                     │       │◁─ ─Account 1─ ▷│         │
 * ┌─────────┐                        ┌───────────┐        ┌───────────┤       │                │         │
 * │         │                        │           │        │           │Client │                └─────────┘
 * │ Client  │                        │ WebSocket │        │ WebSocket │Plugin │
 * │Plugin 1 │◁ ─ ─ ─Account 1─ ─ ─ ─▷│  Client   │◁─HTTP─▷│  Server   │ Conn  │                ┌─────────┐
 * │         │                        │           │        │           │       │                │         │
 * └─────────┘                        └───────────┘        └───────────┤       │                │Plugin N │
 *                                                                     │       │◁─ ─Account N─ ▷│         │
 *                                                                     │       │                │         │
 *                                                                     │       │                └─────────┘
 *                                                                     └───────┘
 * </pre>
 *
 * <p>In general, a plugin defines a bilateral relationship between two participants, sharing a single
 * Interledger account (identified by an {@link InterledgerAddress}).</p>
 *
 * <p>While this paradigm works well for plugins that operate on only a single account, this paradigm presents
 * challenges for a server that wants to support multiple incoming accounts/plugins. This is because a plugin is
 * currently tightly-coupled to its transport and assumes only a single account per-connection. For example, per
 * IL-RFC-23, an ILSP server that speaks BTP over a WebSocket server connection is generally only allowed to support a
 * single account per HTTP port (at least if that server wants to technically stay compatible with the BTP
 * specification). Operating a new WebSocket server on a new port for _every_ incoming BTP connection is an unusual way
 * to scale HTTP mux because HTTP allows multiple mux on a single port.</p>
 *
 * <p>Thus, while it might be tempting to conceive of a bilateral connection that supports multiple accounts as simply
 * being a "mini-Connector", this is not actually the case because a Connector will bridge one account to another (i.e.,
 * 4 plugins across two accounts), whereas a bilateral connection would only bridge one plugin to another plugin (i.e.,
 * 2 plugins across one account).</p>
 *
 * <p>To reiterate, a Connector bridges two accounts, but multiple accounts can communicate over a single
 * connection.</p>
 *
 * @param <RM> A {@link BilateralReceiverMux} that is capable of de-multiplexing incoming packets.
 * @param <SM> A {@link BilateralSenderMux} that is capable of multiplexing outgoing packets.
 *
 * @see "https://github.com/interledger/rfcs/tree/master/0023-bilateral-transfer-protocol"
 */
public interface BilateralConnection<SM extends BilateralSenderMux, RM extends BilateralReceiverMux> extends
    //BilateralReceiverMuxEventListener, BilateralSenderMuxEventListener,
    Closeable {

  /**
   * Accessor for the {@link InterledgerAddress} of the operator of this mux.
   *
   * @return An instance of {@link InterledgerAddress}.
   */
  InterledgerAddress getOperatorAddress();

//  /**
//   * The {@link InterledgerAddress} of the remote node that this bilateral connection is connecting to.
//   *
//   * @return An instance of {@link InterledgerAddress}.
//   */
//  InterledgerAddress getRemoteAddress();

  // TODO: Javadoc.
  SM getBilateralSenderMux();

  RM getBilateralReceiverMux();

//  /**
//   * Add a bilateral connection event listener to this connection..
//   *
//   * Care should be taken when adding multiple handlers to ensure that they perform distinct operations, otherwise
//   * duplicate functionality might be unintentionally introduced.
//   *
//   * @param eventListenerId A {@link UUID} that uniquely identifies the listener to be added.
//   * @param eventListener   A {@link BilateralConnectionEventListener} that can handle various types of events emitted
//   *                        by this bilateral connection.
//   *
//   * @return A {@link UUID} representing the unique identifier of the listener, as seen by this connection.
//   */
//  void addConnectionEventListener(UUID eventListenerId, BilateralConnectionEventListener eventListener);
//
//  /**
//   * Removes the event listener from the collection of listeners registered with this connection.
//   *
//   * @param eventListenerId A {@link UUID} representing the unique identifier of the listener to remove.
//   */
//  void removeConnectionEventListener(UUID eventListenerId);

//  /**
//   * The role that the connection is playing with regard to the remote.
//   */
//  enum ConnectionRole {
//    /**
//     * This connection is a client connecting to a single remote server.
//     */
//    CLIENT,
//
//    /**
//     * This connection is a server, allowing many incoming mux from potentially distinct remote clients.
//     */
//    SERVER
//  }

  /**
   * <p>Connect to the remote peer.</p>
   */
  default CompletableFuture<Void> connect() {
    return this.getBilateralSenderMux().connect().thenCompose(($) -> getBilateralReceiverMux().connect());
  }

  /**
   * Disconnect from the remote peer.
   */
  default CompletableFuture<Void> disconnect() {
    return this.getBilateralSenderMux().disconnect().thenCompose(($) -> getBilateralReceiverMux().disconnect());
  }

  @Override
  default void close() {
    disconnect().join();
  }
}
