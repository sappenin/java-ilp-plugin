package org.interledger.plugin.connections;

import org.interledger.plugin.Connectable;
import org.interledger.plugin.connections.events.bilateral.BilateralConnectionEventListener;

import java.util.UUID;

/**
 * Defines how to connect and disconnect.
 */
public interface ConnectableConnection extends Connectable {

  /**
   * Add a plugin event listener.
   *
   * Care should be taken when adding multiple handlers to ensure that they perform distinct operations, otherwise
   * duplicate functionality might be unintentionally introduced.
   *
   * @param eventListenerId A unique identifier for the event listener.
   * @param eventListener   A {@link BilateralConnectionEventListener} that can handle various types of events emitted
   *                        by this ledger plugin.
   *
   * @return A {@link UUID} representing the unique identifier of the handler, as seen by this ledger plugin.
   */
  void addPluginEventListener(final UUID eventListenerId, final BilateralConnectionEventListener eventListener);

  /**
   * Removes an event listener.
   *
   * @param eventListenerId A {@link UUID} representing the unique identifier of the handler, as seen by this ledger
   *                        plugin.
   */
  void removePluginEventListener(final UUID eventListenerId);
}
