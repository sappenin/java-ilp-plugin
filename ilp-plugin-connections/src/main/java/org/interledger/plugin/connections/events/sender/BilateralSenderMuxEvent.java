package org.interledger.plugin.connections.events.sender;

import org.interledger.plugin.connections.mux.BilateralSenderMux;

/**
 * A parent interface for all Bilateral Sender events.
 */
public interface BilateralSenderMuxEvent {

  /**
   * Accessor for the MUX that emitted this event.
   */
  BilateralSenderMux bilateralSenderMux();

}