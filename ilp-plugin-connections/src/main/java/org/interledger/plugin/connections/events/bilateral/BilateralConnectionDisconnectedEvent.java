package org.interledger.plugin.connections.events.bilateral;

import org.interledger.plugin.connections.BilateralConnection;

import org.immutables.value.Value;

/**
 * Emitted after a {@link BilateralConnection} disconnects from its remote peer.
 */
public interface BilateralConnectionDisconnectedEvent extends BilateralConnectionEvent {

  static BilateralConnectionDisconnectedEvent of(final BilateralConnection bilateralConnection) {
    return ImmutableBilateralConnectionDisconnectedEvent.builder().bilateralConnection(bilateralConnection).build();
  }

  @Value.Immutable
  abstract class AbstractBilateralConnectionDisconnectedEvent implements BilateralConnectionDisconnectedEvent {

  }

}