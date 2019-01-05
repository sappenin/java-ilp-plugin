package org.interledger.plugin.lpiv2.btp2.subprotocols.auth;

import static org.interledger.plugin.lpiv2.btp2.subprotocols.BtpSubProtocolHandlerRegistry.BTP_SUB_PROTOCOL_AUTH;
import static org.interledger.plugin.lpiv2.btp2.subprotocols.BtpSubProtocolHandlerRegistry.BTP_SUB_PROTOCOL_AUTH_TOKEN;
import static org.interledger.plugin.lpiv2.btp2.subprotocols.BtpSubProtocolHandlerRegistry.BTP_SUB_PROTOCOL_AUTH_USERNAME;

import org.interledger.btp.BtpError;
import org.interledger.btp.BtpErrorCode;
import org.interledger.btp.BtpMessage;
import org.interledger.btp.BtpResponse;
import org.interledger.btp.BtpRuntimeException;
import org.interledger.btp.BtpSession;
import org.interledger.btp.BtpSessionCredentials;
import org.interledger.btp.BtpSubProtocol;
import org.interledger.btp.BtpSubProtocols;
import org.interledger.btp.BtpTransfer;
import org.interledger.btp.ImmutableBtpSessionCredentials;
import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.lpiv2.btp2.subprotocols.AbstractBtpSubProtocolHandler;
import org.interledger.plugin.lpiv2.btp2.subprotocols.BtpAuthenticator;
import org.interledger.plugin.lpiv2.btp2.subprotocols.BtpMultiAuthenticator;

import com.google.common.base.Preconditions;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * An extension of {@link AbstractBtpSubProtocolHandler} for handling the <tt>auth</tt> sub-protocol as defined in
 * IL-RFC-23. This implementation supports both BTP Multi-auth as well as BTP single-auth.
 *
 * @see "https://github.com/interledger/rfcs/blob/master/0023-bilateral-transfer-protocol/0023-bilateral-transfer-protocol.md#authentication"
 */
public class ServerAuthBtpSubprotocolHandler extends AuthBtpSubprotocolHandler {

  private final BtpAuthenticator btpSingleAuthenticator;
  private final BtpMultiAuthenticator btpMultiAuthenticator;

  public ServerAuthBtpSubprotocolHandler(
      final BtpAuthenticator btpSingleAuthenticator,
      final BtpMultiAuthenticator btpMultiAuthenticator
  ) {
    this.btpSingleAuthenticator = Objects.requireNonNull(btpSingleAuthenticator);
    this.btpMultiAuthenticator = Objects.requireNonNull(btpMultiAuthenticator);
  }

  /**
   * Construct a {@link BtpSubProtocol} response for using the <tt>auth</tt> sub-protocol.
   *
   * @return A {@link BtpSubProtocol}.
   */
  static BtpSubProtocol authResponse() {
    return BtpSubProtocol.builder()
        .protocolName(BtpSubProtocols.AUTH)
        .contentType(BtpSubProtocol.ContentType.MIME_APPLICATION_OCTET_STREAM)
        .build();
  }

  /**
   * When this handler is operating in a BTP server, then actual authentication of a connecting BTP client occurs in
   * this method.
   */
  @Override
  public CompletableFuture<Optional<BtpSubProtocol>> handleSubprotocolDataForBtpMessage(
      final BtpSession btpSession, final BtpMessage incomingBtpMessage
  ) throws BtpRuntimeException {

    Objects.requireNonNull(btpSession);
    Objects.requireNonNull(incomingBtpMessage);

    logger.debug("Incoming Auth Message: {}", incomingBtpMessage);

    // Before anything else, when a client connects to a server, it sends a special Message request. Its primary
    // protocolData entry MUST have name 'auth', content type MIME_APPLICATION_OCTET_STREAM, and empty data.
    final String primarySubProtocolName = incomingBtpMessage.getPrimarySubProtocol().getProtocolName();
    Preconditions.checkArgument(
        BTP_SUB_PROTOCOL_AUTH.equals(primarySubProtocolName),
        String.format("Expected BTP SubProtocol `%s` but encountered `%s` instead.", BTP_SUB_PROTOCOL_AUTH,
            primarySubProtocolName)
    );

    // `auth_username` is optional...if it's not specified, then this handler should assume a single account. If it
    // is specified, this handler should assume multi-accounts.
    final Optional<String> auth_username = incomingBtpMessage.getSubProtocol(BTP_SUB_PROTOCOL_AUTH_USERNAME)
        .map(BtpSubProtocol::getDataAsString);

    // ...among the secondary entries, there MUST be a UTF-8 'auth_token' entry
    final String auth_token = incomingBtpMessage.getSubProtocol(BTP_SUB_PROTOCOL_AUTH_TOKEN)
        .map(BtpSubProtocol::getDataAsString)
        .orElseThrow(
            () -> new BtpRuntimeException(BtpErrorCode.F00_NotAcceptedError,
                String.format("Expected BTP SubProtocol with Id: %s", BTP_SUB_PROTOCOL_AUTH_TOKEN))
        );

    final BtpAuthenticator btpAuthenticator = auth_username
        .map($ -> btpMultiAuthenticator.getBtpAuthenticator($)
            .orElseThrow(() -> new BtpAuthenticationException("Invalid BTP Auth Credentials for " + auth_username))
        )
        .orElse(btpSingleAuthenticator);

    // TODO: Put all of this into the CF.
    final boolean authenticated = btpAuthenticator.isValidAuthToken(auth_token);
    if (authenticated) {
      // SUCCESS! Respond with an empty Ack message...
      return CompletableFuture.supplyAsync(() -> {
        this.storeAuthInBtpSession(btpSession, btpAuthenticator.getAccountAddress(), auth_token, auth_username);
        return Optional.of(ServerAuthBtpSubprotocolHandler.authResponse());
      });
    } else {
      throw new BtpRuntimeException(BtpErrorCode.F00_NotAcceptedError,
          String.format("invalid %s", BTP_SUB_PROTOCOL_AUTH_TOKEN)
      );
    }
  }

  /**
   * BtpAuth doesn't do anything with instances of {@link BtpTransfer}.
   *
   * @return A completed future with {@link Optional#empty()}.
   */
  @Override
  public CompletableFuture<Optional<BtpSubProtocol>> handleSubprotocolDataForBtpTransfer(
      final BtpSession btpSession, final BtpTransfer incomingBtpTransfer
  ) throws BtpRuntimeException {
    Objects.requireNonNull(btpSession);
    Objects.requireNonNull(incomingBtpTransfer);

    logger.error("BTP Auth Handler encountered incoming BtpTransfer but should not have: {}", incomingBtpTransfer);
    return CompletableFuture.completedFuture(Optional.empty());
  }

  /**
   * When this handler is operating in a BTP client, then actual authentication of the session occurs in this method
   * because. This handler is only engaged on a BTP client because, per the BTP Auth flow, a server is never going to be
   * expecting an Auth response from a client.
   */
  @Override
  public CompletableFuture<Void> handleSubprotocolDataForBtpResponse(
      final BtpSession btpSession, final BtpResponse incomingBtpResponse
  ) throws BtpRuntimeException {

    Objects.requireNonNull(btpSession);
    Objects.requireNonNull(incomingBtpResponse);

    logger.debug("Incoming Auth Subprotocol BtpResponse: {}", incomingBtpResponse);
    return CompletableFuture.runAsync(() -> btpSession.setAuthenticated())
        .thenAccept($ -> logger.info("BtpSession Authenticated: {}", btpSession));
  }

  /**
   * BtpAuth doesn't do anything with instances of {@link BtpError}.
   *
   * @return A completed future with {@link Optional#empty()}.
   */
  @Override
  public CompletableFuture<Void> handleSubprotocolDataForBtpError(
      final BtpSession btpSession, final BtpError incomingBtpError
  ) throws BtpRuntimeException {

    Objects.requireNonNull(btpSession);
    Objects.requireNonNull(incomingBtpError);

    logger.error("Incoming Auth Subprotocol BtpError: {}", incomingBtpError);
    return CompletableFuture.completedFuture(null);
  }

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

    btpSession.setAccountAddress(accountAddress);

    final BtpSessionCredentials credentials = ImmutableBtpSessionCredentials.builder()
        .authUsername(username)
        .authToken(token)
        .build();
    btpSession.setBtpSessionCredentials(credentials);
    btpSession.setAuthenticated();
  }

}
