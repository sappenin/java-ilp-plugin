package org.interledger.plugin.lpiv2;

import org.immutables.value.Value.Immutable;

import java.util.Map;

/**
 * Configuration information relating to the {@link Plugin} that should be used for any account in a particular
 * connection. This interface tracks default settings, independent of any single plugin, so that new plugins can be
 * initialized during a connection based upon a pre-configured set of values (e.g., configured in a properties file).
 */
public interface DefaultPluginSettings extends PluginSettings {

  static ImmutablePluginSettings.Builder builder() {
    return ImmutablePluginSettings.builder();
  }

  /**
   * The type of this ledger plugin.
   */
  PluginType getPluginType();

  /**
   * Additional, custom settings that any plugin can define.
   */
  Map<String, Object> getCustomSettings();

  @Immutable
  abstract class AbstractDefaultPluginSettings implements DefaultPluginSettings {

  }

}
