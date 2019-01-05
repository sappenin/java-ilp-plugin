package org.interledger.plugin.lpiv2.btp2.subprotocols;

import org.interledger.core.InterledgerAddress;

import java.util.Objects;

/**
 * A service for authenticating BTP sessions using an `auth_token` as specified by the BTP protocol.
 */
public interface BtpAuthenticator {

  /**
   * Checks to see if the provided authentication credentials are valid. This variant is used for BTP Plugins that
   * support only a single counterparty.
   *
   * @param authToken A {@link String} containing an `auth_token` for a BtpSession.
   *
   * @return {@code true} if the auth_token is valid; {@code false} otherwise.
   */
  boolean isValidAuthToken(String authToken);

  /**
   * <p>For the supplied {@code authUsername}, attempt to locate the corresponding {@link InterledgerAddress}.
   * Regardless of the authentication mechanism, some credential will be supplied, and MUST be translatable to an
   * address.</p>
   *
   * <p>Note that for single-account plugins, this method should return the bilateral account address, regardless of
   * the supplied input.</p>
   *
   * @return
   */
  InterledgerAddress getAccountAddress();

  /**
   * No-op implementation of {@link BtpAuthenticator} that always returns {@code true} to simulate valid BTP messages.
   */
  class AlwaysAllowedBtpAuthenticator implements BtpAuthenticator {

    private final InterledgerAddress accountAddress;

    /**
     * Required-args Constructor.
     *
     * @param accountAddress The {@link InterledgerAddress} of the account this authenticator will allow.
     */
    public AlwaysAllowedBtpAuthenticator(final InterledgerAddress accountAddress) {
      this.accountAddress = Objects.requireNonNull(accountAddress);
    }

    @Override
    public boolean isValidAuthToken(String authToken) {
      return true;
    }

    @Override
    public InterledgerAddress getAccountAddress() {
      return accountAddress;
    }
  }
}
