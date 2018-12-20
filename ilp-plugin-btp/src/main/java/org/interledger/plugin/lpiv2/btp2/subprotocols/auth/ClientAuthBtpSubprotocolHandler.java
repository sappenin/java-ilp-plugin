package org.interledger.plugin.lpiv2.btp2.subprotocols.auth;

import static org.interledger.btp.BtpErrorCode.F00_NotAcceptedError;

import org.interledger.btp.BtpError;
import org.interledger.btp.BtpMessage;
import org.interledger.btp.BtpResponse;
import org.interledger.btp.BtpRuntimeException;
import org.interledger.btp.BtpSession;
import org.interledger.btp.BtpSessionCredentials;
import org.interledger.btp.BtpSubProtocol;
import org.interledger.btp.BtpTransfer;
import org.interledger.btp.ImmutableBtpSessionCredentials;
import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.lpiv2.btp2.subprotocols.AbstractBtpSubProtocolHandler;
import org.interledger.plugin.lpiv2.btp2.subprotocols.BtpAuthenticator;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.security.auth.login.AccountNotFoundException;

/**
 * An extension of {@link AbstractBtpSubProtocolHandler} for handling the <tt>auth</tt> sub-protocol as defined in
 * IL-RFC-23, from the perspective of a BTP Client. BTP clients send an authentication request as a sub-protocol inside
 * of a BTP packet, and expect an empty response to indicate that auth succeeded.
 *
 * @see "https://github.com/interledger/rfcs/blob/master/0023-bilateral-transfer-protocol/0023-bilateral-transfer-protocol.md#authentication"
 */
public class ClientAuthBtpSubprotocolHandler extends AuthBtpSubprotocolHandler {

  //private final BtpAuthenticator btpAuthenticator;

//  public ClientAuthBtpSubprotocolHandler(final BtpAuthenticator btpAuthenticator) {
//    this.btpAuthenticator = Objects.requireNonNull(btpAuthenticator);
//  }

  /**
   * When this handler is operating in a BTP client, if this method returns a valid, empty {@link BtpResponse} with
   * proper subprotocol and content-type (which is the only way this method would be called), then we consider the
   * BTPSession to be authenticated. The addition of credentials, etc, is handled by a higher-level concern, likely a
   * BTP Client that has been initialized with BTP credentials that would have been a pre-requisite for BTP Auth.
   */
  @Override
  public CompletableFuture<Void> handleSubprotocolDataForBtpResponse(
      final BtpSession btpSession, final BtpResponse incomingBtpResponse
  ) throws BtpRuntimeException {
    Objects.requireNonNull(btpSession);
    Objects.requireNonNull(incomingBtpResponse);

    logger.debug("BTP Auth (Incoming BtpTransfer): {}", incomingBtpResponse);
    return CompletableFuture.runAsync(() -> btpSession.setAuthenticated())
        .thenAccept($ -> logger.info("BtpSession Authenticated: {}", btpSession));
  }

  /**
   * For a BTP Client, this method is a no-op. A BTP client should never encounter this message type during BTP Auth.
   */
  @Override
  public CompletableFuture<Optional<BtpSubProtocol>> handleSubprotocolDataForBtpMessage(
      final BtpSession btpSession, final BtpMessage incomingBtpMessage
  ) throws BtpRuntimeException {
    logger.debug("BTP Auth (Incoming BtpMessage): {}", incomingBtpMessage);
    return CompletableFuture.completedFuture(Optional.empty());
  }

  /**
   * For a BTP Client, this method is a no-op. A BTP client should never encounter this message type during BTP Auth.
   */
  @Override
  public CompletableFuture<Optional<BtpSubProtocol>> handleSubprotocolDataForBtpTransfer(
      final BtpSession btpSession, final BtpTransfer incomingBtpTransfer
  ) throws BtpRuntimeException {
    logger.debug("BTP Auth (Incoming BtpTransfer): {}", incomingBtpTransfer);
    return CompletableFuture.completedFuture(Optional.empty());
  }

  /**
   * If BTP Auth does not complete successfully, this method logs the error, but otherwise does nothing. The {@code
   * btpSession} will remain unauthenticated.
   */
  @Override
  public CompletableFuture<Void> handleSubprotocolDataForBtpError(
      final BtpSession btpSession, final BtpError incomingBtpError
  ) throws BtpRuntimeException {
    Objects.requireNonNull(btpSession);
    Objects.requireNonNull(incomingBtpError);

    logger.error("BTP Auth (Incoming BtpError): {}", incomingBtpError);
    return CompletableFuture.completedFuture(null);
  }

//  /**
//   * After a successful BTP Authentication flow, update the {@code btpSession} to contain the proper data, such as a
//   * decoded username, the corresponding accountAddress, and the auth state.
//   *
//   * @param btpSession The {@link BtpSession} to update.
//   * @param token      An authorization token used to authenticate the indicated user.
//   * @param username   The optionally-present username for the signed-in account.
//   */
//  protected void storeAuthInBtpSession(final BtpSession btpSession, final String token,
//      final Optional<String> username) {
//    Objects.requireNonNull(btpSession);
//    Objects.requireNonNull(token);
//    Objects.requireNonNull(username);
//
//    // The account that the BTP session is operating upon...
//    final InterledgerAddress accountAddress;
//    try {
//      accountAddress = this.btpAuthenticator.getAccountAddress(username);
//
//    } catch (AccountNotFoundException e) {
//      throw new BtpRuntimeException(F00_NotAcceptedError, "BTP Account not found!", e);
//    }
//    btpSession.setAccountAddress(accountAddress);
//
//    final BtpSessionCredentials credentials = ImmutableBtpSessionCredentials.builder()
//        .authUsername(username)
//        .authToken(token)
//        .build();
//    btpSession.setBtpSessionCredentials(credentials);
//
//    btpSession.setAuthenticated();
//  }
}
