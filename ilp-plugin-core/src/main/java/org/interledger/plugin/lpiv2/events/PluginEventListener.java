package org.interledger.plugin.lpiv2.events;

import org.interledger.plugin.lpiv2.Plugin;

/**
 * Handler interface that defines events related to internal system operations of a {@link Plugin}.
 */
public interface PluginEventListener {

  /**
   * Called to handle an {@link PluginConnectedEvent}.
   *
   * @param event A {@link PluginConnectedEvent}.
   */
  void onConnect(PluginConnectedEvent event);

  /**
   * Called to handle an {@link PluginDisconnectedEvent}.
   *
   * @param event A {@link PluginDisconnectedEvent}.
   */
  void onDisconnect(PluginDisconnectedEvent event);

  /**
   * Called to handle an {@link PluginErrorEvent}.
   *
   * @param event A {@link PluginErrorEvent}.
   */
  void onError(PluginErrorEvent event);

}
