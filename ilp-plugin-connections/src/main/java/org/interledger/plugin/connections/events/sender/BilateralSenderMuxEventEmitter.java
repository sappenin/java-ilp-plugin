package org.interledger.plugin.connections.events.sender;

import org.interledger.plugin.connections.mux.BilateralSenderMux;

import java.util.UUID;

/**
 * Defines how a {@link BilateralSenderMux} should emit events.
 */
public interface BilateralSenderMuxEventEmitter {

  void emitEvent(final BilateralSenderMuxConnectedEvent event);

  void emitEvent(final BilateralSenderMuxDisconnectedEvent event);

//  void emitEvent(final AccountConnectedEvent event);
//
//  void emitEvent(final AccountDisconnectedEvent event);

  void addEventListener(UUID eventListenerId, BilateralSenderMuxEventListener eventHandler);

  void removeEventListener(UUID eventListenerId);
}
