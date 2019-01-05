package org.interledger.plugin.lpiv2;

import org.interledger.core.InterledgerAddress;

import org.immutables.value.Value.Immutable;

import java.util.Map;

/**
 * Configuration information relating to a {@link Plugin}.
 */
public interface PluginSettings {

  static ImmutablePluginSettings.Builder builder() {
    return ImmutablePluginSettings.builder();
  }

  /**
   * The type of this ledger plugin.
   */
  PluginType getPluginType();

  /**
   * The ILP address of the ILP Node operating this plugin.
   */
  InterledgerAddress getOperatorAddress();

  /**
   * The ILP Address for the Account this Plugin is operating upon.
   */
  InterledgerAddress getAccountAddress();

  /**
   * Additional, custom settings that any plugin can define.
   */
  Map<String, Object> getCustomSettings();

  @Immutable
  abstract class AbstractPluginSettings implements PluginSettings {

  }

}
