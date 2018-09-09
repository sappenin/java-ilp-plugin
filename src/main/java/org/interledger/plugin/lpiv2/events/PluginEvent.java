package org.interledger.plugin.lpiv2.events;

import org.interledger.core.InterledgerAddress;

/**
 * A parent interface for all lpi2 events.
 */
public interface PluginEvent {

  /**
   * The ILP Address-prefix of the LPIv2 plugin that emitted this event.
   */
  InterledgerAddress getPeerAccount();
}