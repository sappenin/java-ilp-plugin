package org.interledger.plugin.lpiv2.events;

import org.interledger.plugin.lpiv2.Plugin;

import com.google.common.collect.Maps;
import org.immutables.value.Value.Default;

import java.util.Map;

/**
 * A parent interface for all LPIv2 events.
 */
public interface PluginEvent {

  /**
   * Accessor for the Plugin that emitted this event.
   */
  Plugin<?> getPlugin();

  /**
   * Custom properties that can be added to any Plugin event.
   *
   * @return
   */
  @Default
  default Map<String, Object> getCustomSettings() {
    return Maps.newConcurrentMap();
  }

}