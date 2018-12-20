package org.interledger.plugin.connections.events.sender;

import org.interledger.plugin.BilateralSender;
import org.interledger.plugin.connections.mux.BilateralSenderMux;

import org.immutables.value.Value;

/**
 * Emitted after a {@link BilateralSender} connects to its remote peer.
 */
public interface BilateralSenderMuxConnectedEvent extends BilateralSenderMuxEvent {

  static BilateralSenderMuxConnectedEvent of(final BilateralSenderMux bilateralSenderMux) {
    return ImmutableBilateralSenderMuxConnectedEvent.builder().bilateralSenderMux(bilateralSenderMux).build();
  }

  @Value.Immutable
  abstract class AbstractBilateralSenderMuxConnectedEvent implements BilateralSenderMuxConnectedEvent {

  }

}