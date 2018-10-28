package org.interledger.plugin.lpiv2.events;

import org.interledger.plugin.lpiv2.Plugin;

/**
 * Defines how a plugin should emit events. Note that a given {@link Plugin} has only a single event-emitter.
 */
public interface PluginEventEmitter {

  void emitEvent(final PluginConnectedEvent event);

  void emitEvent(final PluginDisconnectedEvent event);

  void emitEvent(final PluginErrorEvent event);
}
