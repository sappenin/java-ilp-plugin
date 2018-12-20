package org.interledger.plugin.connections.events.receiver;

import org.interledger.plugin.BilateralReceiver;

import java.util.UUID;

/**
 * Defines how a {@link BilateralReceiver} should emit events.
 */
public interface BilateralReceiverMuxEventEmitter {

  void emitEvent(final BilateralReceiverMuxConnectedEvent event);

  void emitEvent(final BilateralReceiverMuxDisconnectedEvent event);
//
//  void emitEvent(final AccountConnectedEvent event);
//
//  void emitEvent(final AccountDisconnectedEvent event);

  void addEventListener(UUID eventListenerId, BilateralReceiverMuxEventListener eventHandler);

  void removeEventListener(UUID eventListenerId);
}
