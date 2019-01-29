package org.interledger.plugin.lpiv2;

import org.immutables.value.Value.Immutable;

import java.util.Map;

/**
 * Configuration information relating to any default settings that a plugin factory should use when instantiating a
 * {@link Plugin}.
 */
public interface DefaultPluginSettings {

  static ImmutableDefaultPluginSettings.Builder builder() {
    return ImmutableDefaultPluginSettings.builder();
  }

  /**
   * Additional, custom settings that any plugin can define.
   */
  Map<String, Object> getCustomSettings();

  @Immutable
  abstract class AbstractDefaultPluginSettings implements DefaultPluginSettings {

  }

}
