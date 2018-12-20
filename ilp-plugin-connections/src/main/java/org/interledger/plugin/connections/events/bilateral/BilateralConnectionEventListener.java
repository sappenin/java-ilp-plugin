package org.interledger.plugin.connections.events.bilateral;

import org.interledger.plugin.connections.BilateralConnection;

/**
 * Handler interface that defines events related to internal system operations of a {@link BilateralConnection}.
 */
public interface BilateralConnectionEventListener {

  /**
   * Called to handle an {@link BilateralConnectionConnectedEvent}.
   *
   * @param event A {@link BilateralConnectionConnectedEvent}.
   */
  void onConnect(BilateralConnectionConnectedEvent event);

  /**
   * Called to handle an {@link BilateralConnectionDisconnectedEvent}.
   *
   * @param event A {@link BilateralConnectionDisconnectedEvent}.
   */
  void onDisconnect(BilateralConnectionDisconnectedEvent event);

}
