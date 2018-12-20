package org.interledger.plugin.lpiv2.btp2.subprotocols;

import org.interledger.core.InterledgerAddress;

import java.util.Objects;

public interface BtpAuthenticator {

  /**
   * Checks to see if the provided authentication credentials are valid. This variant is used for BTP Plugins that
   * support only a single counterparty.
   *
   * @param incomingAuthToken A {@link String} containing an `auth_token` for a BtpSession.
   *
   * @return {@code true} if the auth_token is valid; {@code false} otherwise.
   */
  boolean isValidAuthToken(String incomingAuthToken);

  /**
   * Checks to see if the provided authentication credentials are valid. This variant is used for BTP plugins that
   * support multiple accounts, thus simulating multiple BTP sessions over a single mux.
   *
   * @param incomingAuthUsername A {@link String} containing an `auth_username` for a BtpSession.
   * @param incomingAuthToken    A {@link String} containing an `auth_token` for a BtpSession.
   *
   * @return {@code true} if the auth_token is valid; {@code false} otherwise.
   */
  boolean isValidAuthToken(String incomingAuthUsername, String incomingAuthToken);

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
   * No-op implementation of {@link org.interledger.plugin.lpiv2.btp2.subprotocols.BtpAuthenticator} that always returns
   * {@code true} to simulate valid BTP messages.
   */
  class AlwaysAllowedBtpAuthenticator implements BtpAuthenticator {

    private final InterledgerAddress interledgerAddress;

    public AlwaysAllowedBtpAuthenticator(final InterledgerAddress interledgerAddress) {
      this.interledgerAddress = Objects.requireNonNull(interledgerAddress);
    }

    @Override
    public boolean isValidAuthToken(String incomingAuthToken) {
      return true;
    }

    @Override
    public boolean isValidAuthToken(String incomingAuthUsername, String incomingAuthToken) {
      return true;
    }

    @Override
    public InterledgerAddress getAccountAddress() {
      return interledgerAddress;
    }
  }
}
