package org.interledger.plugin.lpiv2;

import org.interledger.plugin.lpiv2.events.PluginConnectedEvent;
import org.interledger.plugin.lpiv2.events.PluginDisconnectedEvent;
import org.interledger.plugin.lpiv2.events.PluginErrorEvent;

/**
 * Defines how a plugin should emit events. Note that a given {@link Plugin} has only a single event-emitter.
 */
public interface PluginEventEmitter {

    void emitEvent(final PluginConnectedEvent event);

    void emitEvent(final PluginDisconnectedEvent event);

    void emitEvent(final PluginErrorEvent event);
}
