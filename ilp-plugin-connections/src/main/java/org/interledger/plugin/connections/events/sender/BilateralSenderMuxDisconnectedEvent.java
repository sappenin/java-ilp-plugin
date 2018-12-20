package org.interledger.plugin.connections.events.sender;

import org.interledger.plugin.BilateralSender;
import org.interledger.plugin.connections.mux.BilateralSenderMux;

import org.immutables.value.Value;

/**
 * Emitted after a {@link BilateralSender} disconnects from its remote peer.
 */
public interface BilateralSenderMuxDisconnectedEvent extends BilateralSenderMuxEvent {

  static BilateralSenderMuxDisconnectedEvent of(final BilateralSenderMux bilateralSenderMux) {
    return ImmutableBilateralSenderMuxDisconnectedEvent.builder().bilateralSenderMux(bilateralSenderMux).build();
  }

  @Value.Immutable
  abstract class AbstractBilateralSenderMuxDisconnectedEvent implements
      BilateralSenderMuxDisconnectedEvent {

  }

}