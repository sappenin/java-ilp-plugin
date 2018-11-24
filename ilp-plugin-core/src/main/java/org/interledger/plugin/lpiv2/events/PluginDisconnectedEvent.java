package org.interledger.plugin.lpiv2.events;

import org.interledger.plugin.lpiv2.Plugin;

import org.immutables.value.Value;

/**
 * Emitted after a ledger lpi2 disconnects from its peer.
 */
public interface PluginDisconnectedEvent extends PluginEvent {

  static PluginDisconnectedEvent of(final Plugin<?> plugin){
    return ImmutablePluginDisconnectedEvent.builder().plugin(plugin).build();
  }

  @Value.Immutable
  abstract class AbstractPluginDisconnectedEvent implements PluginDisconnectedEvent {

  }

}