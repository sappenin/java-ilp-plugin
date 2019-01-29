package org.interledger.plugin.lpiv2.btp2.subprotocols;

import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.lpiv2.btp2.subprotocols.BtpAuthenticator.AlwaysAllowedBtpAuthenticator;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

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

    // An ILP address of this node to root all child accounts under. Modeled as a Supplier to allow late-binding and
    // potentially runtime changes to the operator.
    private final Supplier<InterledgerAddress> nodeIlpAddressSupplier;

    public AlwaysAllowedBtpMultiAuthenticator(final Supplier<InterledgerAddress> nodeIlpAddressSupplier) {
      Objects.requireNonNull(nodeIlpAddressSupplier);
      this.nodeIlpAddressSupplier = Objects.requireNonNull(nodeIlpAddressSupplier);
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
      return this.nodeIlpAddressSupplier.get().with(username);
    }

  }
}
