package org.interledger.plugin.lpiv2.btp2.spring;

import org.interledger.plugin.lpiv2.PluginType;
import org.interledger.plugin.lpiv2.btp2.BtpPluginSettings;
import org.interledger.plugin.lpiv2.btp2.spring.ImmutableBtpClientPluginSettings.Builder;

import com.google.common.base.Preconditions;
import org.immutables.value.Value;
import org.immutables.value.Value.Default;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Value.Immutable
public interface BtpClientPluginSettings extends BtpPluginSettings {

  /**
   * The scheme for the remote BTP peer connection.
   */
  String KEY_REMOTE_PEER_SCHEME = "remotePeerScheme";

  /**
   * The hostname for the remote BTP peer.
   */
  String KEY_REMOTE_PEER_HOSTNAME = "remotePeerHostName";

  /**
   * The port for the remote BTP peer.
   */
  String KEY_REMOTE_PEER_PORT = "remotePeerPort";

  /**
   * Construct a new Builder.
   *
   * @return A new instance of {@link Builder}.
   */
  static Builder builder() {
    return ImmutableBtpClientPluginSettings.builder();
  }

  /**
   * Construct a new Builder.
   *
   * @return A new instance of {@link ImmutableBtpClientPluginSettings.Builder}.
   */
  static ImmutableBtpClientPluginSettings.Builder builder(final Map<String, Object> customSettings) {
    Objects.requireNonNull(customSettings);

    return applyCustomSettings(builder(), customSettings);
  }

  /**
   * Populate a supplied builder with the correct custom settings, as found in {@code customSettings}.
   *
   * @param builder
   * @param customSettings
   *
   * @return
   */
  static ImmutableBtpClientPluginSettings.Builder applyCustomSettings(
      final ImmutableBtpClientPluginSettings.Builder builder, Map<String, Object> customSettings
  ) {
    Objects.requireNonNull(builder);
    Objects.requireNonNull(customSettings);

    Optional.ofNullable(customSettings.get(KEY_REMOTE_PEER_SCHEME))
        .map(Object::toString)
        .ifPresent(builder::remotePeerScheme);

    Optional.ofNullable(customSettings.get(KEY_REMOTE_PEER_HOSTNAME))
        .map(Object::toString)
        .ifPresent(builder::remotePeerHostname);

    Optional.ofNullable(customSettings.get(KEY_REMOTE_PEER_PORT))
        .map(Object::toString)
        .map(Integer::valueOf)
        .ifPresent(builder::remotePeerPort);

    Optional.ofNullable(customSettings.get(KEY_USER_NAME))
        .map(Object::toString)
        .ifPresent(builder::authUsername);

    Optional.ofNullable(customSettings.get(KEY_USER_NAME))
        .map(Object::toString)
        .ifPresent(builder::authUsername);

    Optional.ofNullable(customSettings.get(KEY_SECRET))
        .map(Object::toString)
        .ifPresent(builder::secret);

    return builder;
  }

  @Override
  default PluginType getPluginType() {
    return BtpClientPlugin.PLUGIN_TYPE;
  }

  /**
   * The scheme for the remote peer connection. Currently only "ws" and "wss" are supported.
   *
   * @return
   */
  @Value.Default
  default String getRemotePeerScheme() {
    return "wss";
  }

  /**
   * The hostname for the remote BTP peer.
   */
  @Value.Default
  default String getRemotePeerHostname() {
    return "localhost";
  }

  /**
   * The port for the remote BTP peer.
   */
  @Value.Default
  default int getRemotePeerPort() {
    return 6666;
  }

  @Override
  @Default
  default Duration getMinMessageWindow() {
    return Duration.of(1000, ChronoUnit.MILLIS);
  }

  @Override
  @Default
  default Duration getSendMoneyWaitTime() {
    return Duration.of(30, ChronoUnit.SECONDS);
  }

  @Value.Check
  default void check() {
    Preconditions.checkArgument("ws".equals(this.getRemotePeerScheme()) || "wss".equals(this.getRemotePeerScheme()),
        "Remote Peer scheme must be either `ws` or `wss`");
  }
}
