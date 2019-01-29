package org.interledger.plugin.lpiv2.btp2;

import org.interledger.plugin.lpiv2.Plugin;
import org.interledger.plugin.lpiv2.PluginSettings;

import java.math.BigInteger;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
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

//  /**
//   * Populate a supplied builder with the correct custom settings, as found in {@code customSettings}.
//   *
//   * @param builder
//   * @param customSettings
//   *
//   * @return
//   */
//  static ImmutableBtpPluginSettings.Builder applyCustomSettings(
//      final ImmutableBtpPluginSettings.Builder builder, Map<String, Object> customSettings
//  ) {
//    Objects.requireNonNull(builder);
//    Objects.requireNonNull(customSettings);
//
//    Optional.ofNullable(customSettings.get(KEY_USER_NAME))
//        .map(Object::toString)
//        .ifPresent(builder::authUsername);
//
//    Optional.ofNullable(customSettings.get(KEY_SECRET))
//        .map(Object::toString)
//        .ifPresent(builder::secret);
//
//    return builder;
//  }

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

  /**
   * The maximum amount of time that {@link Plugin#sendMoney(BigInteger)} should wait for a response before timing out.
   *
   * @return
   */
  default Duration getSendMoneyWaitTime() {
    return Duration.of(30, ChronoUnit.SECONDS);
  }

  /**
   * <p>The minimum amount of time (in milliseconds) to budget for receiving a response message from an account.</p>
   *
   * <p>Especially useful for ILP packets, if a packet expires in 30 seconds, then a plugin should only wait 29 seconds
   * before timing out so that it can generally be sure to reject the request (as opposed to merely allowing a timeout
   * to occur, because timeouts are ambiguous).</p>
   *
   * @return A {@link Duration}.
   */
  default Duration getMinMessageWindow() {
    return Duration.of(1000, ChronoUnit.MILLIS);
  }

//  @Value.Immutable
//  abstract class AbstractBtpPluginSettings implements BtpPluginSettings {
//
//    @Override
//    @Default
//    public Duration getMinMessageWindow() {
//      return Duration.of(1000, ChronoUnit.MILLIS);
//    }
//
//  }
}
