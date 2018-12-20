package org.interledger.plugin.connections.events.receiver;

import org.interledger.plugin.BilateralReceiver;
import org.interledger.plugin.connections.mux.BilateralReceiverMux;

import org.immutables.value.Value;

/**
 * Emitted after a {@link BilateralReceiver} connects to its remote peer.
 */
public interface BilateralReceiverMuxConnectedEvent extends BilateralReceiverMuxEvent {

  static BilateralReceiverMuxConnectedEvent of(final BilateralReceiverMux bilateralReceiverMux) {
    return ImmutableBilateralReceiverMuxConnectedEvent.builder().bilateralReceiverMux(bilateralReceiverMux).build();
  }

  @Value.Immutable
  abstract class AbstractBilateralReceiverMuxConnectedEvent implements BilateralReceiverMuxConnectedEvent {

  }

}