package org.interledger.plugin.lpiv2.btp2.subprotocols.auth;

import org.interledger.btp.BtpSession;
import org.interledger.btp.BtpSessionCredentials;
import org.interledger.btp.ImmutableBtpSessionCredentials;
import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.lpiv2.btp2.subprotocols.AbstractBtpSubProtocolHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

/**
 * An extension of {@link AbstractBtpSubProtocolHandler} for handling the <tt>auth</tt> sub-protocol as defined in
 * IL-RFC-23, from the perspective of a BTP Client. BTP clients send an authentication request as a sub-protocol inside
 * of a BTP packet, and expect an empty response to indicate that auth succeeded.
 *
 * @see "https://github.com/interledger/rfcs/blob/master/0023-bilateral-transfer-protocol/0023-bilateral-transfer-protocol.md#authentication"
 */
public abstract class AuthBtpSubprotocolHandler extends AbstractBtpSubProtocolHandler {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  //////////////////
  // Private Helpers
  //////////////////

  /**
   * After a successful BTP Authentication flow, update the {@code btpSession} to contain the proper data, such as a
   * decoded username, the corresponding accountAddress, and the auth state.
   *
   * @param btpSession The {@link BtpSession} to update.
   * @param token      An authorization token used to authenticate the indicated user.
   * @param username   The optionally-present username for the signed-in account.
   */
  protected void storeAuthInBtpSession(
      final BtpSession btpSession, final InterledgerAddress accountAddress,
      final String token, final Optional<String> username
  ) {
    Objects.requireNonNull(btpSession);
    Objects.requireNonNull(accountAddress);
    Objects.requireNonNull(token);
    Objects.requireNonNull(username);

    final BtpSessionCredentials credentials = ImmutableBtpSessionCredentials.builder()
        .authUsername(username)
        .authToken(token)
        .build();
    btpSession.setBtpSessionCredentials(credentials);
    btpSession.setAuthenticated();
  }

}
