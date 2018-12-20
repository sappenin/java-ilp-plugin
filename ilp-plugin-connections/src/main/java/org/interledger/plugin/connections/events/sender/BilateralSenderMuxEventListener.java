package org.interledger.plugin.connections.events.sender;

import org.interledger.plugin.connections.mux.BilateralSenderMux;

/**
 * Handler interface that defines events related to internal system operations of a {@link BilateralSenderMux}.
 */
public interface BilateralSenderMuxEventListener {

  /**
   * Called to handle an {@link BilateralSenderMuxConnectedEvent}.
   *
   * @param event A {@link BilateralSenderMuxConnectedEvent}.
   */
  void onConnect(BilateralSenderMuxConnectedEvent event);

  /**
   * Called to handle an {@link BilateralSenderMuxDisconnectedEvent}.
   *
   * @param event A {@link BilateralSenderMuxDisconnectedEvent}.
   */
  void onDisconnect(BilateralSenderMuxDisconnectedEvent event);

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
