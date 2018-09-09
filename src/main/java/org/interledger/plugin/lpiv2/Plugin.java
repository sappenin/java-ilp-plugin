package org.interledger.plugin.lpiv2;

import org.interledger.core.InterledgerFulfillPacket;
import org.interledger.core.InterledgerPreparePacket;
import org.interledger.core.InterledgerProtocolException;
import org.interledger.plugin.lpiv2.handlers.PluginEventHandler;

import java.math.BigInteger;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * <p>An abstraction for communicating with a remote Interledger peer using a single account.</p>
 *
 * <p>The overall flow of funds in ILPv4 is as follows:
 *
 * <pre>
 * Sender --sendPacket-> Connector 1 --sendPacket-> Connector 2 --sendPacket-> Receiver
 *    |                        |                        |
 *    `----settle->            `----settle->            `----settle->
 * </pre>
 *
 * Sender/Connector's call <tt>sendData</tt>, wait for a fulfillment, and then call
 * <tt>settle</tt> (possibly infrequently or even only eventually for bulk settlement) if the fulfillment is valid.</p>
 */
public interface Plugin<T extends PluginSettings> {

  boolean CONNECTED = true;
  boolean NOT_CONNECTED = false;

  /**
   * The settings for this Plugin.
   */
  T getPluginSettings();

  /**
   * Connect to the remote peer.
   */
  void connect();

  /**
   * Disconnect from the remote peer.
   */
  void disconnect();

  /**
   * Determines if a plugin is connected or not.
   *
   * @return {@code true} if the plugin is connected; {@code false} otherwise.
   */
  boolean isConnected();

  /**
   * Sends an ILP request packet to the peer and returns the response packet (this method correlates with
   * <tt>sendData</tt> in the Javascript connector).
   *
   * @param preparePacket The ILP packet to send to the peer.
   *
   * @return A {@link CompletableFuture} that resolves to the ILP response from the peer.
   *
   * @throws InterledgerProtocolException if the request is rejected by the peer.
   */
  CompletableFuture<InterledgerFulfillPacket> sendPacket(InterledgerPreparePacket preparePacket)
      throws InterledgerProtocolException;

  /**
   * Handle an incoming Interledger data packets. If an error occurs, this method MAY throw an exception. In general,
   * the callback should behave as sendData does.
   *
   * @param preparePacket The ILP packet sent from a remote peer.
   *
   * @throws InterledgerProtocolException if the request is rejected by the peer.
   */
  CompletableFuture<InterledgerFulfillPacket> handleIncomingPacket(InterledgerPreparePacket preparePacket)
      throws InterledgerProtocolException;

  /**
   * Settle an outstanding ILP balance with a counterparty by transferring {@code amount} units of value from this ILP
   * node to the counterparty of the account used by this plugin (this method correlates to <tt>sendMoney</tt> in the
   * Javascript Connector).
   *
   * @param amount The amount of "money" to transfer.
   */
  CompletableFuture<Void> settle(BigInteger amount);

  /**
   * Handle a request to settle an outstanding balance.
   *
   * @param amount The amount of "money" to transfer.
   */
  CompletableFuture<Void> handleIncomingSettle(BigInteger amount);

  /**
   * Add a  plugin event handler to this plugin.
   *
   * Care should be taken when adding multiple handlers to ensure that they perform distinct operations, otherwise
   * duplicate functionality might be unintentionally introduced.
   *
   * @param eventHandler A {@link PluginEventHandler} that can handle various types of events emitted by this ledger
   *                     plugin.
   *
   * @return A {@link UUID} representing the unique identifier of the handler, as seen by this ledger plugin.
   */
  UUID addPluginEventHandler(PluginEventHandler eventHandler);

  /**
   * Removes an event handler from the collection of handlers registered with this ledger plugin.
   *
   * @param eventHandlerId A {@link UUID} representing the unique identifier of the handler, as seen by this ledger
   *                       plugin.
   */
  void removePluginEventHandler(UUID eventHandlerId);

  /**
   * Accessor the emitter so that external actors can emit events to this plugin.
   */
  //PluginEventEmitter getPluginEventEmitter();


}
