package org.interledger.plugin.lpiv2.btp2.spring;

import org.interledger.plugin.lpiv2.PluginType;
import org.interledger.plugin.lpiv2.btp2.BtpPluginSettings;

import org.immutables.value.Value.Default;
import org.immutables.value.Value.Immutable;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public interface BtpServerPluginSettings extends BtpPluginSettings {

  String SEND_MONEY_WAIT_TIME_KEY = "sendMoneyWaitTime";

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

    Optional.ofNullable(customSettings.get(SEND_MONEY_WAIT_TIME_KEY))
        .map(Object::toString)
        .map(Integer::valueOf)
        .map(millis -> Duration.of(millis, ChronoUnit.MILLIS))
        .ifPresent(builder::sendMoneyWaitTime);

    return builder;
  }

  @Override
  default PluginType getPluginType() {
    return BtpServerPlugin.PLUGIN_TYPE;
  }

  @Immutable
  abstract class AbstractBtpServerPluginSettings implements BtpServerPluginSettings {

    @Override
    @Default
    public Duration getMinMessageWindow() {
      return Duration.of(1000, ChronoUnit.MILLIS);
    }

    @Override
    @Default
    public Duration getSendMoneyWaitTime() {
      return Duration.of(30, ChronoUnit.SECONDS);
    }
  }

}
