package org.interledger.plugin.lpiv2;

import org.interledger.core.InterledgerAddress;

/**
 * Configuration information relating to a {@link Plugin}.
 */
public interface PluginSettings {

  /**
   * The type of this ledger plugin.
   */
  PluginType pluginTypeId();

  /**
   * The ILP Address for remote peer account this Plugin is connecting to...
   */
  InterledgerAddress peerAccount();

  /**
   * The ILP address of the ILP Node operating this plugin.
   */
  InterledgerAddress localNodeAddress();

}
