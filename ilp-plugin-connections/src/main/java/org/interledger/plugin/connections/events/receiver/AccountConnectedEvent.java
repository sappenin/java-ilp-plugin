package org.interledger.plugin.connections.events.receiver;

import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.BilateralReceiver;

import org.immutables.value.Value;

/**
 * Emitted after an Account connects on a {@link BilateralReceiver}.
 */
// TODO: Remove if unused.
@Deprecated
public interface AccountConnectedEvent {//extends BilateralReceiverMuxEvent {
//
//  static AccountConnectedEvent of(final InterledgerAddress accountAddress) {
//    return ImmutableAccountConnectedMuxEvent.builder().accountAddress(accountAddress).build();
//  }
//
//  /**
//   * The address of the account that just connected.
//   */
//  InterledgerAddress accountAddress();
//
//  @Value.Immutable
//  abstract class AbstractAccountConnectedEvent implements AccountConnectedEvent {
//
//  }

}