package org.interledger.plugin.connections.events.receiver;

/**
 * An abstract implementation of {@link BilateralReceiverMuxEventListener} that no-ops all methods, leaving
 * implementations to define any overrides.
 */
public abstract class AbstractBilateralReceiverMuxEventListener implements BilateralReceiverMuxEventListener {

  @Override
  public void onConnect(BilateralReceiverMuxConnectedEvent event) {

  }

  @Override
  public void onDisconnect(BilateralReceiverMuxDisconnectedEvent event) {

  }

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
