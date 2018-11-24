package org.interledger.plugin.lpiv2;

import org.interledger.core.InterledgerPreparePacket;
import org.interledger.plugin.link.BilateralDataHandler;
import org.interledger.plugin.link.BilateralMoneyHandler;
import org.interledger.plugin.link.BilateralPinger;
import org.interledger.plugin.link.BilateralReceiver;
import org.interledger.plugin.link.BilateralSender;
import org.interledger.plugin.lpiv2.events.PluginEventListener;
import org.interledger.plugin.lpiv2.exceptions.DataHandlerAlreadyRegisteredException;
import org.interledger.plugin.lpiv2.exceptions.MoneyHandlerAlreadyRegisteredException;
import org.interledger.plugin.lpiv2.settings.PluginSettings;

import java.io.Closeable;
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
 *    `----sendMoney->         `----sendMoney->         `----sendMoney->
 * </pre>
 *
 * Sender/Connector's call <tt>sendData</tt>, wait for a fulfillment, and then call <tt>sendMoney</tt> (possibly
 * infrequently or even only eventually for bulk settlement) if the fulfillment is valid.</p>
 */
public interface Plugin<T extends PluginSettings> extends BilateralPinger, BilateralSender, BilateralReceiver,
    Closeable {

  boolean CONNECTED = true;
  boolean NOT_CONNECTED = false;

  /**
   * The settings for this Plugin.
   */
  T getPluginSettings();

  /**
   * <p>Connect to the remote peer.</p>
   */
  CompletableFuture<Void> connect();

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
   * Add a  plugin event handler to this plugin.
   *
   * Care should be taken when adding multiple handlers to ensure that they perform distinct operations, otherwise
   * duplicate functionality might be unintentionally introduced.
   *
   * @param eventHandler A {@link PluginEventListener} that can handle various types of events emitted by this ledger
   *                     plugin.
   *
   * @return A {@link UUID} representing the unique identifier of the handler, as seen by this ledger plugin.
   */
  void addPluginEventListener(UUID eventHandlerId, PluginEventListener eventHandler);

  /**
   * Removes an event handler from the collection of handlers registered with this ledger plugin.
   *
   * @param eventHandlerId A {@link UUID} representing the unique identifier of the handler, as seen by this ledger
   *                       plugin.
   */
  void removePluginEventListener(UUID eventHandlerId);

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
   * @param dataHandler An instance of {@link BilateralDataHandler}.
   */
  void registerDataHandler(BilateralDataHandler dataHandler) throws DataHandlerAlreadyRegisteredException;

  /**
   * Accessor for the currently registered (though optionally-present) {@link BilateralDataHandler}. Throws a {@link
   * RuntimeException} if no handler is registered, because callers should not be trying to access the handler if none
   * is registered (in other words, a Plugin is not in a valid state until it has handlers registered)
   *
   * @return The currently registered {@link BilateralDataHandler}.
   */
  default BilateralDataHandler safeGetDataHandler() {
    return this.getDataHandler()
        .orElseThrow(() -> new RuntimeException("You MUST register an IlpDataHandler before using this plugin!"));
  }

  /**
   * Removes the currently used {@link BilateralDataHandler}. This has the same effect as if {@link
   * #registerDataHandler(BilateralDataHandler)} had never been called. If no data handler is currently set, this method
   * does nothing.
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
   * @param moneyHandler An instance of {@link BilateralMoneyHandler}.
   */
  void registerMoneyHandler(BilateralMoneyHandler moneyHandler) throws MoneyHandlerAlreadyRegisteredException;

  /**
   * Removes the currently used money handler. This has the same effect as if {@link
   * #registerMoneyHandler(BilateralMoneyHandler)} had never been called. If no money handler is currently set, this
   * method does nothing.
   */
  void unregisterMoneyHandler();

  /**
   * Accessor for the currently registered (though optionally-present) {@link BilateralMoneyHandler}. Throws a {@link
   * RuntimeException} if no handler is registered, because callers should not be trying to access the handler if none
   * is registered (in other words, a Plugin is not in a valid state until it has handlers registered)
   *
   * @return The currently registered {@link BilateralMoneyHandler}.
   */
  default BilateralMoneyHandler safeGetMoneyHandler() {
    return this.getMoneyHandler()
        .orElseThrow(
            () -> new RuntimeException("You MUST register an BilateralMoneyHandler before using this plugin!"));
  }
}
