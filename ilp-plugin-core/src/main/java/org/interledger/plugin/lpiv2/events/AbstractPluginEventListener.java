package org.interledger.plugin.lpiv2.events;

/**
 * An abstract implementation of {@link PluginEventListener} that no-ops all methods except those relating to {@link
 * PluginEventListener}, which are left to the implementation to define.
 */
public abstract class AbstractPluginEventListener implements PluginEventListener {

  @Override
  public void onConnect(PluginConnectedEvent event) {

  }

  @Override
  public void onDisconnect(PluginDisconnectedEvent event) {

  }

  @Override
  public void onError(PluginErrorEvent event) {

  }
}
