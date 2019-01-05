package org.interledger.plugin.lpiv2.btp2.subprotocols;

import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.lpiv2.btp2.subprotocols.BtpAuthenticator.AlwaysAllowedBtpAuthenticator;

import java.util.Objects;
import java.util.Optional;

/**
 * Provides support for `auth_username` in BTP Auth, which allows multiple clients to connect to a single BTP websocket
 * connection.
 */
public interface BtpMultiAuthenticator {

  /**
   * Return the BTP Authenticator for the specified account.
   *
   * @param authUsername The `auth_username` supplied as part of the BTP Auth protocol.
   *
   * @return A {@link BtpAuthenticator}.
   */
  Optional<BtpAuthenticator> getBtpAuthenticator(String authUsername);

  /**
   * No-op implementation of {@link BtpAuthenticator} that always returns {@code true} to simulate valid BTP messages.
   */
  class AlwaysAllowedBtpMultiAuthenticator implements BtpMultiAuthenticator {

    // An ILP address of this node to root all child accounts under.
    private final InterledgerAddress nodeIlpAddress;

    public AlwaysAllowedBtpMultiAuthenticator(final InterledgerAddress nodeIlpAddress) {
      Objects.requireNonNull(nodeIlpAddress);
      this.nodeIlpAddress = Objects.requireNonNull(nodeIlpAddress);
    }

    @Override
    public Optional<BtpAuthenticator> getBtpAuthenticator(final String username) {
      Objects.requireNonNull(username);

      final InterledgerAddress accountAddress = this.usernameToIlpAddress(username);
      return Optional.of(new AlwaysAllowedBtpAuthenticator(accountAddress));
    }

    /**
     * Create a child-account for this connector using the supplied username.
     *
     * @param username A username source from the `auth_username` via the BTP Auth protocol.
     *
     * @return
     */
    private InterledgerAddress usernameToIlpAddress(final String username) {
      Objects.requireNonNull(username);
      return this.nodeIlpAddress.with(username);
    }

  }
}
