package org.interledger.plugin.lpiv2.btp2;

import org.interledger.plugin.lpiv2.PluginSettings;

import org.immutables.value.Value;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public interface BtpPluginSettings extends PluginSettings {

  /**
   * A typed key for the BTP Auth shared secret that both the client and server will use to authenticate a BTP session.
   */
  String KEY_SECRET = "secret";

  /**
   * A typed key for the BTP Auth shared secret that both the client and server will use to authenticate a BTP session.
   */
  String KEY_USER_NAME = "username";

  /**
   * Populate a supplied builder with the correct custom settings, as found in {@code customSettings}.
   *
   * @param builder
   * @param customSettings
   *
   * @return
   */
  static ImmutableBtpPluginSettings.Builder applyCustomSettings(
      final ImmutableBtpPluginSettings.Builder builder, Map<String, Object> customSettings
  ) {
    Objects.requireNonNull(builder);
    Objects.requireNonNull(customSettings);

    Optional.ofNullable(customSettings.get(KEY_USER_NAME))
        .map(Object::toString)
        .ifPresent(builder::authUsername);

    return builder
        .secret(
            Objects.requireNonNull(customSettings.get(KEY_SECRET), "`secret` not found in customSettings!").toString()
        );
  }

  /**
   * <p>The `auth_username` for a BTP client. Enables multiple accounts over a single BTP WebSocket connection.</p>
   *
   * @return
   */
  Optional<String> getAuthUsername();

  /**
   * The `auth_token` for a BTP client, as specified in IL-RFC-23.
   *
   * @return
   *
   * @see "https://github.com/interledger/rfcs/blob/master/0023-bilateral-transfer-protocol/0023-bilateral-transfer-protocol.md#authentication"
   */
  String getSecret();

  @Value.Immutable
  abstract class AbstractBtpPluginSettings implements BtpPluginSettings {

  }
}
