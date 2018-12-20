package org.interledger.plugin;

import org.interledger.core.InterledgerFulfillPacket;
import org.interledger.core.InterledgerPreparePacket;
import org.interledger.core.InterledgerRejectPacket;
import org.interledger.core.InterledgerResponsePacket;
import org.interledger.core.InterledgerResponsePacketHandler;
import org.interledger.core.InterledgerResponsePacketMapper;
import org.interledger.plugin.BilateralReceiver.DataHandler;

import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * <p>A sender of Interledger financial data to the other side of a bilateral releationship (i.e., the other party
 * operating a single account with the operator of this sender).</p>
 *
 * <p>Peers in the Interledger Protocol require a way to communicate securely with one another. Since most existing
 * ledgers do not implement two-way authenticated communication between account holders, we need Link protocols to
 * provide this functionality. Link protocols generally convey two types of information:</p>
 *
 * <pre>
 *   <ul>
 *    <li>Packets of Interledger Protocol data.</li>
 *    <li>Information on the settling of outstanding balances.</li>
 *  </ul>
 * </pre>
 *
 * <p>There are various flavors of Bilateral Transfer Protocol (BTP), including one over Websockets and one over
 * gRpc. Thus, this interface refers to BTP in the generic sense, as opposed to being strictly related to
 * IL-RFC-23.</p>
 *
 * @see "https://github.com/interledger/rfcs/blob/master/0001-interledger-architecture/"
 * @see "https://github.com/interledger/rfcs/blob/master/0023-bilateral-transfer-protocol/"
 */
public interface BilateralSender extends Connectable {

  Optional<DataSender> getDataSender();

  /**
   * Accessor for the currently registered (though optionally-present) {@link DataHandler}. Throws a {@link
   * RuntimeException} if no handler is registered, because callers should not be trying to access the handler if none
   * is registered (in other words, a Plugin is not in a valid state until it has handlers registered)
   *
   * @return The currently registered {@link DataHandler}.
   */
  default DataSender safeGetDataSender() {
    return this.getDataSender()
        .orElseThrow(() -> new RuntimeException("You MUST register a DataSender before accessing it!"));
  }

  Optional<MoneySender> getMoneySender();

  /**
   * Accessor for the currently registered (though optionally-present) {@link MoneySender}. Throws a {@link
   * RuntimeException} if no handler is registered, because callers should not be trying to access the handler if none
   * is registered (in other words, a Plugin is not in a valid state until it has handlers registered)
   *
   * @return The currently registered {@link DataHandler}.
   */
  default MoneySender safeGetMoneySender() {
    return this.getMoneySender()
        .orElseThrow(() -> new RuntimeException("You MUST register a MoneySender before accessing it!"));
  }

  /**
   * Defines how to send data to the other side of a bilateral connection (i.e., the other party * operating a single
   * account in tandem with the operator of this sender).
   */
  @FunctionalInterface
  interface DataSender {

    /**
     * <p>Sends an ILPv4 request packet to a connected peer and returns the response packet (if one is returned).</p>
     *
     * <p>This method supports one of three responses, which can be handled by using utility classes such as {@link
     * InterledgerResponsePacketMapper} or {@link InterledgerResponsePacketHandler}:
     * </p>
     *
     * <pre>
     * <ol>
     *   <ul>An instance of {@link InterledgerFulfillPacket}, which means the packet was fulfilled by the receiver.</ul>
     *   <ul>An instance of {@link InterledgerRejectPacket}, which means the packet was rejected by one of the nodes in
     *   the payment path.
     *   </ul>
     *   <ul>An instance of {@link Optional#empty()}, which means the request expired before a response was received.
     *   Note that this type of response does _not_ mean the request wasn't fulfilled or rejected. Instead, it simply
     *   means a response was not received in-time from the remote peer. Because of this, senders should not assume what
     *   actually happened on the org.interledger.bilateral receivers side of this link request.</ul>
     * </ol>
     * </pre>
     *
     * @param preparePacket An {@link InterledgerPreparePacket} to send to the remote peer.
     *
     * @return A {@link CompletableFuture} that resolves to an optionally-present {@link InterledgerResponsePacket},
     *     which will be of concrete type {@link InterledgerFulfillPacket} or {@link InterledgerRejectPacket}, if
     *     present.
     */
    CompletableFuture<Optional<InterledgerResponsePacket>> sendData(InterledgerPreparePacket preparePacket);

  }

  /**
   * Defines how to send money to (i.e., settle with) the other side of a bilateral connection (i.e., the other party
   * operating a single account in tandem with the operator of this sender).
   */
  @FunctionalInterface
  interface MoneySender {

    /**
     * Settle an outstanding ILP balance with a counterparty by transferring {@code amount} units of value from this ILP
     * node to the counterparty of the account used by this plugin (this method correlates to <tt>sendMoney</tt> in the
     * Javascript Connector).
     *
     * @param amount The amount of "money" to transfer.
     */
    CompletableFuture<Void> sendMoney(BigInteger amount);

  }
}
