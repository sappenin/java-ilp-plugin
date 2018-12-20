package org.interledger.plugin.connections.events.bilateral;

import org.interledger.plugin.connections.BilateralConnection;

import java.util.UUID;

/**
 * Defines how a {@link BilateralConnection} should emit events. Note that a given {@link BilateralConnection} has only
 * a single event-emitter.
 */
public interface BilateralConnectionEventEmitter {

  void emitEvent(final BilateralConnectionConnectedEvent event);

  void emitEvent(final BilateralConnectionDisconnectedEvent event);

  void addEventListener(UUID eventListenerId, BilateralConnectionEventListener eventHandler);

  void removeEventListener(UUID eventListenerId);
}
