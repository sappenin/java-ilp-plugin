package org.interledger.plugin;

import org.interledger.core.InterledgerAddress;
import org.interledger.core.InterledgerCondition;
import org.interledger.core.InterledgerPreparePacket;
import org.interledger.core.InterledgerResponsePacket;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;
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
@Deprecated
public interface BilateralSender extends Connectable {

//  InterledgerCondition PING_PROTOCOL_CONDITION =
//      InterledgerCondition.of(Base64.getDecoder().decode("Zmh6rfhivXdsj8GLjp+OIAiXFIVu4jOzkCpZHQ1fKSU="));

//  Optional<DataSender> getDataSender();
//
//  /**
//   * Accessor for the currently registered (though optionally-present) {@link DataHandler}. Throws a {@link
//   * RuntimeException} if no handler is registered, because callers should not be trying to access the handler if none
//   * is registered (in other words, a Plugin is not in a valid state until it has handlers registered)
//   *
//   * @return The currently registered {@link DataHandler}.
//   */
//  default DataSender safeGetDataSender() {
//    return this.getDataSender()
//        .orElseThrow(() -> new RuntimeException("You MUST register a DataSender before accessing it!"));
//  }
//
//  Optional<MoneySender> getMoneySender();
//
//  /**
//   * Accessor for the currently registered (though optionally-present) {@link MoneySender}. Throws a {@link
//   * RuntimeException} if no handler is registered, because callers should not be trying to access the handler if none
//   * is registered (in other words, a Plugin is not in a valid state until it has handlers registered)
//   *
//   * @return The currently registered {@link DataHandler}.
//   */
//  default MoneySender safeGetMoneySender() {
//    return this.getMoneySender()
//        .orElseThrow(() -> new RuntimeException("You MUST register a MoneySender before accessing it!"));
//  }
//
//  /**
//   * Send a 0-value payment to the destination and expect an ILP fulfillment, which demonstrates this sender has
//   * send-data connectivity to the indicated destination address.
//   *
//   * @param destinationAddress
//   */
//  default CompletableFuture<Optional<InterledgerResponsePacket>> ping(final InterledgerAddress destinationAddress) {
//    Objects.requireNonNull(destinationAddress);
//
//    final InterledgerPreparePacket pingPacket = InterledgerPreparePacket.builder()
//        .executionCondition(PING_PROTOCOL_CONDITION)
//        // TODO: Make this timeout configurable!
//        .expiresAt(Instant.now().plusSeconds(30))
//        .amount(BigInteger.ZERO)
//        .destination(destinationAddress)
//        .build();
//
//    return this.safeGetDataSender().sendData(pingPacket);
//  }

}
