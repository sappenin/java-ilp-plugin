package org.interledger.plugin.link;

import java.util.Optional;

/**
 * <p>A receiver of Interledger financial data in a two-way link between two peers (i.e., bilateral).</p>
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
public interface BilateralReceiver {

  /**
   * Accessor for the currently registered (though optionally-present) {@link BilateralDataHandler}.
   *
   * @return The currently registered {@link BilateralDataHandler}.
   */
  Optional<BilateralDataHandler> getDataHandler();

  /**
   * Accessor for the currently registered (though optionally-present) {@link BilateralMoneyHandler}.
   *
   * @return The currently registered {@link BilateralMoneyHandler}, if present.
   */
  Optional<BilateralMoneyHandler> getMoneyHandler();

}
