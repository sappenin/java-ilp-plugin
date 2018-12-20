package org.interledger.plugin.connections.events.bilateral;

import org.interledger.plugin.lpiv2.events.PluginEventListener;

/**
 * An abstract implementation of {@link PluginEventListener} that no-ops all methods except those relating to {@link
 * PluginEventListener}, which are left to the implementation to define.
 */
public abstract class AbstractBilateralConnectionEventListener implements BilateralConnectionEventListener {

  @Override
  public void onConnect(BilateralConnectionConnectedEvent event) {

  }

  @Override
  public void onDisconnect(BilateralConnectionDisconnectedEvent event) {

  }
}
