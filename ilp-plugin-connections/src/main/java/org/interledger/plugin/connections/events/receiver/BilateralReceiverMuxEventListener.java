package org.interledger.plugin.connections.events.receiver;

import org.interledger.plugin.connections.mux.BilateralReceiverMux;

/**
 * Handler interface that defines events related to internal system operations of a {@link BilateralReceiverMux}.
 */
public interface BilateralReceiverMuxEventListener {

  /**
   * Called to handle an {@link BilateralReceiverMuxConnectedEvent}.
   *
   * @param event A {@link BilateralReceiverMuxConnectedEvent}.
   */
  void onConnect(BilateralReceiverMuxConnectedEvent event);

  /**
   * Called to handle an {@link BilateralReceiverMuxDisconnectedEvent}.
   *
   * @param event A {@link BilateralReceiverMuxDisconnectedEvent}.
   */
  void onDisconnect(BilateralReceiverMuxDisconnectedEvent event);

//  /**
//   * Called when an account connects to a bilateral connection.
//   *
//   * @param event A {@link AccountConnectedEvent}.
//   */
//  void onConnect(AccountConnectedEvent event);
//
//  /**
//   * Called when an account disconnects from a bilateral connection.
//   *
//   * @param event A {@link AccountDisconnectedEvent}.
//   */
//  void onDisconnect(AccountDisconnectedEvent event);

}
