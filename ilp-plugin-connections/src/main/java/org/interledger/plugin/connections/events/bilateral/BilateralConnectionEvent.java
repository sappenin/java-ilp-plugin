package org.interledger.plugin.connections.events.bilateral;

import org.interledger.plugin.connections.BilateralConnection;

/**
 * A parent interface for all Bilateral Connection events.
 */
public interface BilateralConnectionEvent {

  /**
   * Accessor for the Plugin that emitted this event.
   */
  BilateralConnection bilateralConnection();

}