package org.interledger.plugin.lpiv2.events;

import org.interledger.plugin.lpiv2.Plugin;

import org.immutables.value.Value;

/**
 * Emitted after a lpi2 connects to a remote peer.
 */
public interface PluginConnectedEvent extends PluginEvent {

  static PluginConnectedEvent of(final Plugin<?> plugin) {
    return ImmutablePluginConnectedEvent.builder().plugin(plugin).build();
  }
  
  @Value.Immutable
  abstract class AbstractPluginConnectedEvent implements PluginConnectedEvent {

  }

}