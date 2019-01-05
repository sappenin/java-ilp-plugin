package org.interledger.plugin;

import java.util.Optional;

/**
 * <p>A receiver of Interledger financial data in a two-way link between two peers (i.e.,
 * org.interledger.bilateral).</p>
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
public interface BilateralReceiver extends Connectable {

//  /**
//   * Accessor for the currently registered (though optionally-present) {@link DataHandler}.
//   *
//   * @return The currently registered {@link DataHandler}.
//   */
//  Optional<DataHandler> getDataHandler();
//
//  /**
//   * Accessor for the currently registered (though optionally-present) {@link DataHandler}. Throws a {@link
//   * RuntimeException} if no handler is registered, because callers should not be trying to access the handler if none
//   * is registered (in other words, a Plugin is not in a valid state until it has handlers registered)
//   *
//   * @return The currently registered {@link DataHandler}.
//   */
//  default DataHandler safeGetDataHandler() {
//    return this.getDataHandler()
//        .orElseThrow(() -> new RuntimeException("You MUST register an IlpDataHandler before using this plugin!"));
//  }
//
//  /**
//   * Accessor for the currently registered (though optionally-present) {@link MoneyHandler}.
//   *
//   * @return The currently registered {@link MoneyHandler}, if present.
//   */
//  Optional<MoneyHandler> getMoneyHandler();
//
//  /**
//   * Accessor for the currently registered (though optionally-present) {@link MoneyHandler}. Throws a {@link
//   * RuntimeException} if no handler is registered, because callers should not be trying to access the handler if none
//   * is registered (in other words, a Plugin is not in a valid state until it has handlers registered)
//   *
//   * @return The currently registered {@link MoneyHandler}.
//   */
//  default MoneyHandler safeGetMoneyHandler() {
//    return this.getMoneyHandler()
//        .orElseThrow(
//            () -> new RuntimeException("You MUST register an MoneyHandler before using this plugin!"));
//  }


}
