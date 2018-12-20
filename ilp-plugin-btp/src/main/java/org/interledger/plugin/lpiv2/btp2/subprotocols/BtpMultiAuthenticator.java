package org.interledger.plugin.lpiv2.btp2.subprotocols;

import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.lpiv2.btp2.subprotocols.BtpAuthenticator.AlwaysAllowedBtpAuthenticator;

import java.util.Optional;

/**
 * Provides support for `auth_username` in BTP Auth, which allows multiple clients to connect to a single BTP websocket
 * connection.
 */
public interface BtpMultiAuthenticator {

  /**
   * Return the BTP Authenticator for the specified account.
   *
   * @return A {@link BtpAuthenticator}.
   */
  Optional<BtpAuthenticator> getBtpAuthenticator(InterledgerAddress accountAddress);

  /**
   * Transform the username into an {@link InterledgerAddress}.
   *
   * @return A {@link InterledgerAddress}.
   */
  default InterledgerAddress usernameToIlpAddress(String username) {
    return InterledgerAddress.of("test.alwaysauthenticated").with(username);
  }

  /**
   * No-op implementation of {@link BtpAuthenticator} that always returns {@code true} to simulate valid BTP messages.
   */
  class AlwaysAllowedBtpMultiAuthenticator implements BtpMultiAuthenticator {

    @Override
    public Optional<BtpAuthenticator> getBtpAuthenticator(final InterledgerAddress accountAddress) {
      return Optional.of(new AlwaysAllowedBtpAuthenticator(accountAddress));
    }

  }

}
