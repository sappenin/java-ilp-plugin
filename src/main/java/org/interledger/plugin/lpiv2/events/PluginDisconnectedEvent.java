package org.interledger.plugin.lpiv2.events;

import org.immutables.value.Value;

/**
 * Emitted after a ledger lpi2 disconnects from its peer.
 */
public interface PluginDisconnectedEvent extends PluginEvent {

  @Value.Immutable
  abstract class AbstractPluginDisconnectedEvent implements PluginDisconnectedEvent {

  }

}