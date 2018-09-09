package org.interledger.plugin.lpiv2.events;

import org.immutables.value.Value;

/**
 * Emitted after a lpi2 connects to a remote peer.
 */
public interface PluginErrorEvent extends PluginEvent {

  /**
   * @return An error that the plugin emitted.
   */
  Exception getError();

  @Value.Immutable
  abstract class AbstractPluginErrorEvent implements PluginErrorEvent {

  }

}