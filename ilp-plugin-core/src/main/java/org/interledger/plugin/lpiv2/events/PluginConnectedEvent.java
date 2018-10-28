package org.interledger.plugin.lpiv2.events;

import org.immutables.value.Value;

/**
 * Emitted after a lpi2 connects to a remote peer.
 */
public interface PluginConnectedEvent extends PluginEvent {

  @Value.Immutable
  abstract class AbstractPluginConnectedEvent implements PluginConnectedEvent {

  }

}