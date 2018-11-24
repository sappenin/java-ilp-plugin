package org.interledger.plugin.lpiv2.events;

import org.interledger.plugin.lpiv2.Plugin;

import java.util.UUID;

/**
 * Defines how a plugin should emit events. Note that a given {@link Plugin} has only a single event-emitter.
 */
public interface PluginEventEmitter {

  void emitEvent(final PluginConnectedEvent event);

  void emitEvent(final PluginDisconnectedEvent event);

  void emitEvent(final PluginErrorEvent event);

  /**
   * Add a  plugin event handler to this plugin.
   *
   * Care should be taken when adding multiple handlers to ensure that they perform distinct operations, otherwise
   * duplicate functionality might be unintentionally introduced.
   *
   * @param eventHandler A {@link PluginEventListener} that can handle various types of events emitted by this ledger
   *                     plugin.
   *
   * @return A {@link UUID} representing the unique identifier of the handler, as seen by this ledger plugin.
   */
  void addPluginEventListener(UUID eventHandlerId, PluginEventListener eventHandler);

  /**
   * Removes an event handler from the collection of handlers registered with this ledger plugin.
   *
   * @param eventHandlerId A {@link UUID} representing the unique identifier of the handler, as seen by this ledger
   *                       plugin.
   */
  void removePluginEventListener(UUID eventHandlerId);
}
