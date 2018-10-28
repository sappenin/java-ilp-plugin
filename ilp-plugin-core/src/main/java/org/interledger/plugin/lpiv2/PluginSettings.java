package org.interledger.plugin.lpiv2;

import org.interledger.core.InterledgerAddress;

import org.immutables.value.Value.Immutable;

import java.util.Map;

/**
 * Configuration information relating to a {@link Plugin}.
 */
public interface PluginSettings {

  /**
   * The type of this ledger plugin.
   */
  PluginType getPluginType();

  /**
   * The ILP Address for remote peer account this Plugin is connecting to...
   */
  InterledgerAddress getPeerAccountAddress();

  /**
   * The ILP address of the ILP Node operating this plugin.
   */
  InterledgerAddress getLocalNodeAddress();

  /**
   * Additional, custom settings that any plugin can define.
   */
  Map<String, Object> getCustomSettings();

  @Immutable
  abstract class AbstractPluginSettings implements PluginSettings {

  }

}