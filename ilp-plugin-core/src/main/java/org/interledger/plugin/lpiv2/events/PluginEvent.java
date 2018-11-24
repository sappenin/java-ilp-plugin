package org.interledger.plugin.lpiv2.events;

import org.interledger.plugin.lpiv2.Plugin;

/**
 * A parent interface for all LPIv2 events.
 */
public interface PluginEvent {

  /**
   * Accessor for the Plugin that emitted this event.
   */
  Plugin<?> getPlugin();

}