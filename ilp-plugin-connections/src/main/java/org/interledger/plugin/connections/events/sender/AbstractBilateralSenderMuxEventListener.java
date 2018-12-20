package org.interledger.plugin.connections.events.sender;

/**
 * An abstract implementation of {@link BilateralSenderMuxEventListener} that no-ops all methods, leaving implementations
 * to define any overrides.
 */
public abstract class AbstractBilateralSenderMuxEventListener implements BilateralSenderMuxEventListener {

  @Override
  public void onConnect(BilateralSenderMuxConnectedEvent event) {

  }

  @Override
  public void onDisconnect(BilateralSenderMuxDisconnectedEvent event) {

  }
//
//  @Override
//  public void onConnect(AccountConnectedEvent event) {
//
//  }
//
//  @Override
//  public void onDisconnect(AccountDisconnectedEvent event) {
//
//  }
}
