package org.interledger.plugin.link.mux;

import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.lpiv2.Plugin;
import org.interledger.plugin.lpiv2.events.PluginEventListener;

import java.io.Closeable;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * <p>Defines a mapping between a mux and one or more plugins in order to de-couple each plugin from the actual
 * mux being used. This allows traffic multiplexing of multiple plugins over a single mux, such as a WebSocket or gRPC
 * connection.</p>
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
 * ┌─────────┐                │  Mux  │  Client   │◁─HTTP─▷│  Server   │  Mux  │                ┌─────────┐
 * │         │                │       │           │        │           │       │                │         │
 * │ Client  │                │       ├───────────┘        └───────────┤       │                │ Client  │
 * │Plugin 2 │◁ ─Account 2─ ─▷│       │                                │       │◁─ ─Account 2─ ▷│Plugin 2 │
 * │         │                │       │                                │       │                │         │
 * └─────────┘                └───────┘                                │       │                └─────────┘
 *                                                                     └───────┘
 * </pre>
 *
 * <p>While the above illustrates a potential server-to-server configuration, the following diagram represens a more
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
 * │Plugin 1 │◁ ─ ─ ─Account 1─ ─ ─ ─▷│  Client   │◁─HTTP─▷│  Server   │  Mux  │                ┌─────────┐
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
 * currently tightly-coupled to its mux, and assumes only a single account per-connection. For example, per IL-RFC-23,
 * an ILSP server that speaks BTP over a WebSocket server connection is generally only allowed to support a single
 * account per HTTP port (at least if that server wants to technically stay compatible with the BTP specification).
 * Operating a new WebSocket server on a new port for _every_ incoming BTP connection is an unusual way to scale HTTP
 * connections because HTTP allows multiple connections on a single port.</p>
 *
 * <p>Thus, while it might be tempting to conceive of a plugin  that supports multiple account as simply being a
 * "mini-Connector", this is not actually true because a Connector will bridge one account to another (i.e., 4 plugins
 * across two accounts), where-as a multi-account plugin would only bridge one plugin to another plugin (i.e., 2 plugins
 * across one account). In this way, a mini-accounts plugin is actually much closer to a Plugin
 * Multiplexer/Demultiplexer, otherwise known as a MUX.</p>
 *
 * <p>To reiterate, a Connector bridges two accounts, whereas as MUX simply allows multiple individual accounts to
 * utilize the same mux (e.g., a WebSocket or a gRPC connection) while maintaining the {@link Plugin} contract per
 * IL-RFC-23.</p>
 *
 * @param <P> The type of Plugin that this Mux will support. Generally, a MUX should support the same type of plugins in
 *            order to properly mux and de-mux channels.
 *
 * @see "https://github.com/interledger/rfcs/tree/master/0023-bilateral-transfer-protocol"
 */
public interface PluginMux<P extends Plugin<?>> extends PluginEventListener, Closeable {

  boolean CONNECTED = true;
  boolean NOT_CONNECTED = false;

  /**
   * Accessor for the {@link InterledgerAddress} of the operator of this mux.
   *
   * @return An instance of {@link InterledgerAddress}.
   */
  InterledgerAddress getOperatorAddress();

  /**
   * <p>Connect to the remote peer.</p>
   */
  CompletableFuture<Void> connect();

  default void close() {
    this.disconnect().join();
  }

  /**
   * Disconnect from the remote peer.
   */
  CompletableFuture<Void> disconnect();

  /**
   * <p>Determines if a plugin is connected to a remote peer or note. If authentication is required, this method will
   * return false until an authenticated session is opened.</p>
   *
   * @return {@code true} if the plugin is connected; {@code false} otherwise.
   */
  boolean isConnected();

  /**
   * Get a plugin that is associated with this MUX based upon its account address.
   */
  Optional<P> getPlugin(final InterledgerAddress account);

  /**
   * Associate a plugin with this MUX.
   */
  void registerPlugin(final InterledgerAddress account, final P plugin);

  /**
   * Remove a plugin from this MUX.
   */
  void unregisterPlugin(final InterledgerAddress account);
}
