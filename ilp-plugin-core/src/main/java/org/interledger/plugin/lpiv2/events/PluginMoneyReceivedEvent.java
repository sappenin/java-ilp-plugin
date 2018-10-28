package org.interledger.plugin.lpiv2.events;

import org.immutables.value.Value;

/**
 * Emitted after a Plugin receives Money.
 */
public interface PluginMoneyReceivedEvent extends PluginEvent {

  @Value.Immutable
  abstract class AbstractPluginMoneyReceivedEvent implements PluginMoneyReceivedEvent {

  }

}