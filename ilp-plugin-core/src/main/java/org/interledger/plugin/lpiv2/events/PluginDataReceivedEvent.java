package org.interledger.plugin.lpiv2.events;

import org.interledger.core.InterledgerPreparePacket;

import org.immutables.value.Value;

/**
 * Emitted after a Plugin receives data.
 */
public interface PluginDataReceivedEvent extends PluginEvent {

  /**
   * The data payload that was received via an incoming {@link InterledgerPreparePacket}.
   *
   * @return A byte-array containing the raw data bits received from an incoming ILP prepare operation.
   */
  byte[] getData();

  @Value.Immutable
  abstract class AbstractPluginDataReceivedEvent implements PluginDataReceivedEvent {

  }

}