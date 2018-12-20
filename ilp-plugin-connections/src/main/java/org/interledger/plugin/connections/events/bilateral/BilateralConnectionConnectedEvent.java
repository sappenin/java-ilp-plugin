package org.interledger.plugin.connections.events.bilateral;

import org.interledger.plugin.connections.BilateralConnection;

import org.immutables.value.Value;

/**
 * Emitted after a {@link BilateralConnection} connects to its remote peer.
 */
public interface BilateralConnectionConnectedEvent extends BilateralConnectionEvent {

  static BilateralConnectionConnectedEvent of(final BilateralConnection bilateralConnection) {
    return ImmutableBilateralConnectionConnectedEvent.builder().bilateralConnection(bilateralConnection).build();
  }

  @Value.Immutable
  abstract class AbstractBilateralConnectionConnectedEvent implements BilateralConnectionConnectedEvent {

  }

}