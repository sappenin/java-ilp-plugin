package org.interledger.plugin.connections.events.receiver;

import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.BilateralReceiver;

import org.immutables.value.Value;

/**
 * Emitted after an Account disconnects from a {@link BilateralReceiver}.
 */
// TODO: Remove if unused.
@Deprecated
public interface AccountDisconnectedEvent { //extends BilateralReceiverMuxEvent {
//
//  static AccountDisconnectedEvent of(final InterledgerAddress accountAddress) {
//    return ImmutableAccountDisconnectedMuxEvent.builder().accountAddress(accountAddress).build();
//  }
//
//  /**
//   * The address of the account that just connected.
//   */
//  InterledgerAddress accountAddress();
//
//  @Value.Immutable
//  abstract class AbstractAccountDisconnectedEvent implements AccountDisconnectedEvent {
//
//  }

}