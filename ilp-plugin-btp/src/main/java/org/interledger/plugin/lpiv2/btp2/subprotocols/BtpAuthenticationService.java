package org.interledger.plugin.lpiv2.btp2.subprotocols;

public interface BtpAuthenticationService {

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
   *
   * @param incomingAuthToken    A {@link String} containing an `auth_token` for a BtpSession.
   * @return {@code true} if the auth_token is valid; {@code false} otherwise.
   */
  boolean isValidAuthToken(String incomingAuthUsername, String incomingAuthToken);

  /**
   * No-op implementation of {@link BtpAuthenticationService} that always returns {@code true} to simulate valid BTP
   * messages.
   */
  class NoOpBtpAuthenticationService implements BtpAuthenticationService {

    @Override
    public boolean isValidAuthToken(String incomingAuthToken) {
      return true;
    }

    @Override
    public boolean isValidAuthToken(String incomingAuthUsername, String incomingAuthToken) {
      return true;
    }
  }
}
