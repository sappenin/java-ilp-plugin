/**
 * <p>Contains all implementations of connections, muxes, and plugins to support BTP over Websockets.</p>
 *
 * <p>From the perspective of an ILP connector, BTP client-accounts will be configured at runtime with proper
 * connection parameters, and will then instantiate a new {@link org.interledger.plugin.lpiv2.btp2.spring.BtpClientPlugin}
 * for each connection.</p>
 *
 * <p>From the perspective of an ILP connector, BTP server-accounts have two flavors: single-account BTP and
 * multi-account BTP. Single-account BTP is configured at runtime by constructing an instance of {@link
 * org.interledger.plugin.lpiv2.btp2.spring.connection.SingleAccountBtpServerConnection}. Likewise, a multi-account
 * Connection is established by constructing an instance of {@link org.interledger.plugin.lpiv2.btp2.spring.connection.MultiAccountBtpServerConnection}.
 * From here, plugins can be obtained from a MUX, which can then be used to obtain a bilateral sender/receiver, which is
 * typically a {@link org.interledger.plugin.lpiv2.Plugin}.</p>
 */

package org.interledger.plugin.lpiv2.btp2.spring;