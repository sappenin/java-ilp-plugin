package org.interledger.plugin.lpiv2.btp2;

import org.immutables.value.Value;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public interface BtpServerPluginSettings extends BtpPluginSettings {

  static ImmutableBtpServerPluginSettings.Builder builder() {
    return ImmutableBtpServerPluginSettings.builder();
  }

  /**
   * Populate a supplied builder with the correct custom settings, as found in {@code customSettings}.
   *
   * @param builder
   * @param customSettings
   *
   * @return
   */
  static ImmutableBtpServerPluginSettings.Builder applyCustomSettings(
      final ImmutableBtpServerPluginSettings.Builder builder, Map<String, Object> customSettings
  ) {
    Objects.requireNonNull(builder);
    Objects.requireNonNull(customSettings);

    Optional.ofNullable(customSettings.get(KEY_USER_NAME))
        .map(Object::toString)
        .ifPresent(builder::authUsername);

    Optional.ofNullable(customSettings.get(KEY_SECRET))
        .map(Object::toString)
        .ifPresent(builder::secret);

    return builder;
  }

  @Value.Immutable
  abstract class AbstractBtpServerPluginSettings implements BtpServerPluginSettings {

  }

}
