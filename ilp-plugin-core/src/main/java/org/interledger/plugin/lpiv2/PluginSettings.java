package org.interledger.plugin.lpiv2;

import org.interledger.core.InterledgerAddress;

import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Redacted;

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
   * Additional, custom settings that any plugin can define.
   */
  Map<String, Object> getCustomSettings();

  @Immutable
  abstract class AbstractPluginSettings implements PluginSettings {

    /**
     * Additional, custom settings that any plugin can define. Redacted to prevent credential leakage in log files.
     */
    @Redacted
    public abstract Map<String, Object> getCustomSettings();

  }

}
