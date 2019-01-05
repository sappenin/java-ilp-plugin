package org.interledger.plugin.connections;

import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.Connectable;
import org.interledger.plugin.connections.settings.BilateralConnectionSettings;
import org.interledger.plugin.lpiv2.Plugin;

/**
 * <p>A relationship between two bilateral peers that supports one or more Account relationships over a single
 * network connection.</p>
 *
 * <p>Sometimes, these account relationships are multiplexed, and in other scenarios there is only a single account
 * over a single connection.</p>
 *
 * <p>In a multiplexed scenario, traffic will flow bidirectionally over the following components:</p>
 * <pre>
 * ┌─────────┐                ┌───────┐                                ┌───────┐                ┌─────────┐
 * │         │                │       │                                │       │                │         │
 * │ Client  │                │       │                                │       │                │   Bob   │
 * │Plugin 1 │◁ ─Account 1─ ─▷│       │                                │       │◁─ ─Account 1─ ▷│Plugin 1 │
 * │         │                │       ├───────────┐        ┌───────────┤       │                │         │
 * └─────────┘                │Client │           │        │           │Server │                └─────────┘
 *                            │ Conn  │ WebSocket │        │ WebSocket │ Conn  │
 * ┌─────────┐                │       │  Client   │◁─HTTP─▷│  Server   │       │                ┌─────────┐
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
 * // TODO: Fix the diagram below to always show a Connection.
 *
 * <pre>
 *                                                                     ┌───────┐                ┌─────────┐
 *                                                                     │       │                │         │
 *                                                                     │       │                │Plugin 1 │
 *                                                                     │       │◁─ ─Account 1─ ▷│         │
 * ┌─────────┐                        ┌───────────┐        ┌───────────┤       │                │         │
 * │         │                        │           │        │           │Server │                └─────────┘
 * │ Client  │                        │ WebSocket │        │ WebSocket │ Conn  │
 * │Plugin 1 │◁ ─ ─ ─Account 1─ ─ ─ ─▷│  Client   │◁─HTTP─▷│  Server   │       │                ┌─────────┐
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
 * @param <CS> A {@link BilateralConnectionSettings} that defines all connection-level settings.
 *
 * @see "https://github.com/interledger/rfcs/tree/master/0023-bilateral-transfer-protocol"
 * @deprecated No longer used.
 */
@Deprecated
public interface BilateralConnection<CS extends BilateralConnectionSettings> extends Connectable {
  //SM extends BilateralSenderMux, RM extends BilateralReceiverMux> extends Closeable {

//  /**
//   * Accessor for this connection's settings.
//   *
//   * @return An instance of {@link CS}.
//   */
//  CS getConnectionSettings();
//
//  //PluginFactory getPluginFactory();
//
//  /**
//   * Obtain the {@link Plugin} for the specified {@code accountAddress}.
//   *
//   * @param accountAddress The {@link InterledgerAddress} of the account to retrieve a plugin for.
//   *
//   * @return An instance of {@link Plugin}.
//   */
//  Plugin<?> getPlugin(InterledgerAddress accountAddress);

//  /**
//   * Accessor for the bilateral-sender associated with the indicated {@code sourceAccountAddress}.
//   *
//   * @param sourceAccountAddress The {@link InterledgerAddress} of the account this call is operating on behalf of
//   *                             (i.e., the account address of the plugin that emitted this call).
//   *
//   * @return An optionally-present {@link BilateralSender}.
//   */
//  Optional<BilateralSender> getBilateralSender(InterledgerAddress sourceAccountAddress);
//
//  /**
//   * Accessor for the bilateral-sender associated with the indicated {@code sourceAccountAddress}.
//   *
//   * @param sourceAccountAddress The {@link InterledgerAddress} of the account this call is operating on behalf of
//   *                             (i.e., the account address of the plugin that emitted this call).
//   *
//   * @return An optionally-present {@link BilateralReceiver}.
//   */
//  Optional<BilateralReceiver> getBilateralReceiver(InterledgerAddress sourceAccountAddress);

  /**
   * Accessor for the plugin supporting the specified {@code accountAddress}.
   *
   * @param accountAddress The ILP address of the account to obtain a plugin for.
   *
   * @return An instance of {@link Plugin}.
   */
  //Plugin<?> getConnectedPlugin(InterledgerAddress accountAddress);

  // TODO: Consider a provide mechanism?
  //AccountProvider getAccountProvider(InterledgerAddress accountAddress);
  // AccountProvider maybe has access to the plugin?

//    // Provides account-level
//    SM getBilateralSenderMux();
//
//    // TODO: Javadoc.
//    RM getBilateralReceiverMux();

}
