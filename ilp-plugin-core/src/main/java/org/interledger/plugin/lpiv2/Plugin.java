package org.interledger.plugin.lpiv2;

import org.interledger.core.InterledgerAddress;
import org.interledger.core.InterledgerFulfillPacket;
import org.interledger.core.InterledgerPreparePacket;
import org.interledger.core.InterledgerProtocolException;
import org.interledger.core.InterledgerResponsePacket;
import org.interledger.plugin.lpiv2.events.PluginEventHandler;
import org.interledger.plugin.lpiv2.exceptions.DataHandlerAlreadyRegisteredException;
import org.interledger.plugin.lpiv2.exceptions.MoneyHandlerAlreadyRegisteredException;

import java.math.BigInteger;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * <p>An abstraction for communicating with a remote Interledger peer using a single account.</p>
 *
 * <p>The overall flow of funds in ILPv4 is as follows:
 *
 * <pre>
 * Sender --sendData-> Connector 1 --sendData-> Connector 2 --sendData-> Receiver
 *    |                        |                        |
 *    `----sendMoney->            `----sendMoney->            `----sendMoney->
 * </pre>
 *
 * Sender/Connector's call <tt>sendData</tt>, wait for a fulfillment, and then call
 * <tt>sendMoney</tt> (possibly infrequently or even only eventually for bulk settlement) if the fulfillment is
 * valid.</p>
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
  CompletableFuture<Void> connect();

  /**
   * Disconnect from the remote peer.
   */
  CompletableFuture<Void> disconnect();

  /**
   * Determines if a plugin is connected or not.
   *
   * @return {@code true} if the plugin is connected; {@code false} otherwise.
   */
  boolean isConnected();

//  /**
//   * Sends an ILP request packet to the peer and returns the response packet (this method correlates with
//   * <tt>sendData</tt> in the Javascript connector).
//   *
//   * @param preparePacket The ILP packet to send to the peer.
//   *
//   * @return A {@link CompletableFuture} that resolves to the ILP response from the peer.
//   *
//   * @throws InterledgerProtocolException if the request is rejected by the peer.
//   */
  //CompletableFuture<InterledgerFulfillPacket> sendData(InterledgerPreparePacket preparePacket)
  //    throws InterledgerProtocolException;

  /**
   * Sends an ILP request packet to the peer and returns the response packet (this method correlates with
   * <tt>sendData</tt> in the Javascript connector).
   *
   * @param preparePacket An {@link InterledgerPreparePacket} to send to the remote peer.
   *
   * @return A {@link CompletableFuture} that resolves to the ILP response from the peer as an instance of {@link
   *     InterledgerResponsePacket}.
   */
  CompletableFuture<InterledgerResponsePacket> sendData(InterledgerPreparePacket preparePacket);

  /**
   * Settle an outstanding ILP balance with a counterparty by transferring {@code amount} units of value from this ILP
   * node to the counterparty of the account used by this plugin (this method correlates to <tt>sendMoney</tt> in the
   * Javascript Connector).
   *
   * @param amount The amount of "money" to transfer.
   */
  CompletableFuture<Void> sendMoney(BigInteger amount);

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
   * <p>Set the callback which is used to handle incoming prepared data packets. The handler should expect one
   * parameter (an ILP Prepare Packet) and return a CompletableFuture for the resulting response. If an error occurs,
   * the callback MAY throw an exception. In general, the callback should behave as {@link
   * #sendData(InterledgerPreparePacket)} does.</p>
   *
   * <p>If a data handler is already set, this method throws a {@link DataHandlerAlreadyRegisteredException}. In order
   * to change the data handler, the old handler must first be removed via {@link #unregisterDataHandler()}. This is to
   * ensure that handlers are not overwritten by accident.</p>
   *
   * <p>If an incoming packet is received by the plugin, but no handler is registered, the plugin SHOULD respond with
   * an error.</p>
   *
   * @param ilpDataHandler An instance of {@link IlpDataHandler}.
   */
  void registerDataHandler(IlpDataHandler ilpDataHandler) throws DataHandlerAlreadyRegisteredException;

  /**
   * Accessor for the currently registered {@link IlpDataHandler}. Throws a {@link RuntimeException} if no handler is
   * registered, because callers should not be trying to access the handler if none is registered (in other words, a
   * Plugin is not in a valid state until it has handlers registered).
   *
   * @return The currently registered {@link IlpDataHandler}.
   *
   * @throws {@link RuntimeException} if no handler is registered.
   */
  IlpDataHandler getDataHandler();

  /**
   * Removes the currently used {@link IlpDataHandler}. This has the same effect as if {@link
   * #registerDataHandler(IlpDataHandler)} had never been called. If no data handler is currently set, this method does
   * nothing.
   */
  void unregisterDataHandler();

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
  void registerMoneyHandler(IlpMoneyHandler ilpMoneyHandler) throws MoneyHandlerAlreadyRegisteredException;

  /**
   * Removes the currently used money handler. This has the same effect as if {@link
   * #registerMoneyHandler(IlpMoneyHandler)} had never been called. If no money handler is currently set, this method
   * does nothing.
   */
  void unregisterMoneyHandler();

  /**
   * Accessor for the currently registered {@link IlpMoneyHandler}. Throws a {@link RuntimeException} if no handler is
   * registered, * because callers should not be trying to access the handler if none is registered (in other words, a
   * Plugin is not * in a valid state until it has handlers registered).
   *
   * @return The currently registered {@link IlpMoneyHandler}.
   */
  IlpMoneyHandler getMoneyHandler();

  /**
   * Handles an incoming {@link InterledgerPreparePacket} for a single plugin, sent from a remote peer.
   */
  @FunctionalInterface
  interface IlpDataHandler {

    /**
     * Handles an incoming ILP fulfill packet.
     *
     * @param sourceAccountAddress
     * @param sourcePreparePacket
     *
     * @return A {@link CompletableFuture} that resolves to an {@link InterledgerFulfillPacket}.
     */
    CompletableFuture<InterledgerResponsePacket> handleIncomingData(
        InterledgerAddress sourceAccountAddress, InterledgerPreparePacket sourcePreparePacket
    );
  }

  /**
   * Handles an incoming message to settle an account with a remote peer, managed by a plugin.
   */
  @FunctionalInterface
  interface IlpMoneyHandler {

    /**
     * Handles an incoming ILP fulfill packet.
     *
     * @param amount
     *
     * @return A {@link CompletableFuture} that resolves to {@link Void}.
     */
    CompletableFuture<Void> handleIncomingMoney(BigInteger amount) throws InterledgerProtocolException;
  }

}
