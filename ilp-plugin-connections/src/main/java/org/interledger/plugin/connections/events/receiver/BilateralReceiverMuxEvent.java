package org.interledger.plugin.connections.events.receiver;

import org.interledger.plugin.connections.mux.BilateralReceiverMux;

/**
 * A parent interface for all Bilateral Receiver events.
 */
public interface BilateralReceiverMuxEvent {

  /**
   * Accessor for the Plugin that emitted this event.
   */
  BilateralReceiverMux bilateralReceiverMux();

}