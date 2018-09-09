package org.interledger.plugin.lpiv2.events;

import org.interledger.plugin.lpiv2.Plugin;
import org.interledger.plugin.lpiv2.events.PluginConnectedEvent;
import org.interledger.plugin.lpiv2.events.PluginDisconnectedEvent;
import org.interledger.plugin.lpiv2.events.PluginErrorEvent;

/**
 * Handler interface that defines all events related to a {@link Plugin}.
 */
public interface PluginEventHandler {

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
