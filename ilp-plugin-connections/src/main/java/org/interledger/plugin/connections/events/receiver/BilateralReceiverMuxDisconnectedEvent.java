package org.interledger.plugin.connections.events.receiver;

import org.interledger.plugin.BilateralReceiver;
import org.interledger.plugin.connections.mux.BilateralReceiverMux;

import org.immutables.value.Value;

/**
 * Emitted after a {@link BilateralReceiver} disconnects from its remote peer.
 */
public interface BilateralReceiverMuxDisconnectedEvent extends BilateralReceiverMuxEvent {

  static BilateralReceiverMuxDisconnectedEvent of(final BilateralReceiverMux bilateralReceiverMux) {
    return ImmutableBilateralReceiverMuxDisconnectedEvent.builder().bilateralReceiverMux(bilateralReceiverMux).build();
  }

  @Value.Immutable
  abstract class AbstractBilateralReceiverMuxDisconnectedEvent implements BilateralReceiverMuxDisconnectedEvent {

  }

}