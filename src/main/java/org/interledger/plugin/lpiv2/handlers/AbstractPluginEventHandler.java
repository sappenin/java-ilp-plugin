package org.interledger.plugin.lpiv2.handlers;

import org.interledger.plugin.lpiv2.events.PluginConnectedEvent;
import org.interledger.plugin.lpiv2.events.PluginDisconnectedEvent;
import org.interledger.plugin.lpiv2.events.PluginErrorEvent;

/**
 * An abstract implementation of {@link PluginEventHandler} that no-ops all methods except those relating to {@link
 * PluginEventHandler}, which are left to the implementation to define.
 */
public abstract class AbstractPluginEventHandler implements PluginEventHandler {

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
