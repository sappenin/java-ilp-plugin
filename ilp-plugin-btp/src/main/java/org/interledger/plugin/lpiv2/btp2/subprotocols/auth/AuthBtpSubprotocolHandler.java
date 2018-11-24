package org.interledger.plugin.lpiv2.btp2.subprotocols.auth;

import static org.interledger.btp.BtpErrorCode.F00_NotAcceptedError;
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
import org.interledger.plugin.lpiv2.btp2.subprotocols.AbstractBtpSubProtocolHandler;
import org.interledger.plugin.lpiv2.btp2.subprotocols.BtpAuthenticationService;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * An extension of {@link AbstractBtpSubProtocolHandler} for handling the <tt>auth</tt> sub-protocol as defined in
 * IL-RFC-23.
 *
 * @see "https://github.com/interledger/rfcs/blob/master/0023-bilateral-transfer-protocol/0023-bilateral-transfer-protocol.md#authentication"
 */
public class AuthBtpSubprotocolHandler extends AbstractBtpSubProtocolHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthBtpSubprotocolHandler.class);

  private final BtpAuthenticationService btpAuthenticationService;

  public AuthBtpSubprotocolHandler(final BtpAuthenticationService btpAuthenticationService) {
    this.btpAuthenticationService = Objects.requireNonNull(btpAuthenticationService);
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

  @Override
  public CompletableFuture<Optional<BtpSubProtocol>> handleSubprotocolDataForBtpMessage(
      final BtpSession btpSession, final BtpMessage incomingBtpMessage
  ) throws BtpRuntimeException {
    Objects.requireNonNull(btpSession);
    Objects.requireNonNull(incomingBtpMessage);

    LOGGER.debug("Incoming Auth Message: {}", incomingBtpMessage);

    // Before anything else, when a client connects to a server, it sends a special Message request. Its primary
    // protocolData entry MUST have name 'auth', content type MIME_APPLICATION_OCTET_STREAM, and empty data.
    final String primarySubProtocolName = incomingBtpMessage.getPrimarySubProtocol().getProtocolName();
    Preconditions.checkArgument(
        BTP_SUB_PROTOCOL_AUTH.equals(primarySubProtocolName),
        String.format("Expected BTP SubProtocol `%s` but encountered `%s` instead.", BTP_SUB_PROTOCOL_AUTH,
            primarySubProtocolName)
    );

    // `auth_username` is optional...
    final Optional<String> auth_username = incomingBtpMessage.getSubProtocol(BTP_SUB_PROTOCOL_AUTH_USERNAME)
        .map(BtpSubProtocol::getDataAsString);

    // ...among the secondary entries, there MUST be a UTF-8 'auth_token' entry
    final String auth_token = incomingBtpMessage.getSubProtocol(BTP_SUB_PROTOCOL_AUTH_TOKEN)
        .map(BtpSubProtocol::getDataAsString)
        .orElseThrow(
            () -> new BtpRuntimeException(BtpErrorCode.F00_NotAcceptedError,
                String.format("Expected BTP SubProtocol with Id: %s", BTP_SUB_PROTOCOL_AUTH_TOKEN))
        );

    final boolean authenticated;
    authenticated = btpAuthenticationService.isValidAuthToken(auth_token, auth_username.get());
    if (authenticated) {
      this.storeAuthInBtpSession(btpSession, auth_token, auth_username);
    }

    if (authenticated) {
      // SUCCESS! Respond with an empty Ack message...
      return CompletableFuture.supplyAsync(() -> Optional.of(AuthBtpSubprotocolHandler.authResponse()))
          .toCompletableFuture();
    } else {
      throw new BtpRuntimeException(BtpErrorCode.F00_NotAcceptedError,
          String.format("invalid %s", BTP_SUB_PROTOCOL_AUTH_TOKEN)
      );
    }
  }

  @Override
  public CompletableFuture<Optional<BtpSubProtocol>> handleSubprotocolDataForBtpTransfer(
      final BtpSession btpSession, final BtpTransfer incomingBtpTransfer
  ) throws BtpRuntimeException {
    Objects.requireNonNull(btpSession);
    Objects.requireNonNull(incomingBtpTransfer);

    LOGGER.debug("Incoming Auth Subprotocol BtpTransfer: {}", incomingBtpTransfer);

    return CompletableFuture.completedFuture(Optional.empty());
  }

  @Override
  public CompletableFuture<Void> handleSubprotocolDataForBtpResponse(
      final BtpSession btpSession, final BtpResponse incomingBtpResponse
  ) throws BtpRuntimeException {

    Objects.requireNonNull(btpSession);
    Objects.requireNonNull(incomingBtpResponse);

    LOGGER.debug("Incoming Auth Subprotocol BtpResponse: {}", incomingBtpResponse);
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> handleSubprotocolDataForBtpError(
      final BtpSession btpSession, final BtpError incomingBtpError
  ) throws BtpRuntimeException {

    Objects.requireNonNull(btpSession);
    Objects.requireNonNull(incomingBtpError);

    LOGGER.error("Incoming Auth Subprotocol BtpError: {}", incomingBtpError);
    return CompletableFuture.completedFuture(null);
  }

  //////////////////
  // Private Helpers
  //////////////////

//  /**
//   * Checks to see if the provided <tt>incomingAuthToken</tt> matches the secret configured for this peer.
//   *
//   * @param incomingAuthToken An optionally-present {@link String} containing an auth_token for this session.
//   *
//   * @return {@code true} if the auth_token is valid; {@code false} otherwise.
//   */
//  private boolean isValidAuthToken(
//      final Optional<String> incomingAuthUser,
//      final String incomingAuthToken
//  ) {
//    Objects.requireNonNull(incomingAuthToken, "incomingAuthToken must not be null!");
//
//    if (incomingAuthToken.equals("")) {
//      return false;
//    }
//    // TODO: Compare the presented auth_token with a secret configured for this plugin. As a simplistic example, a
//    // plugin might be configured on a particular port, and will have a `secret` defined. However, this is surprising
//    // since it seems like we would want to run multiple BTP plugins on the same port. For example, plugin1 and
//    // plugin2. For that to work, we would need some sort of identifier to link a connection (WsSession) to the
//    // plugin instance. However, it seems like this is not the current design of the Plugin interface. Instead, LPIv2
//    // seems to assume that only a given channel will run on a given Websocket port. If this is the case, then we
//    // would need to configure a new listener/port combination for each plugin in the SpringBtpConfig. If that holds,
//    // then a particular BtpSocketHandler would have only a single secret, which can be found here. However, this
//    // doesn't feel right -- adding a new port per plugin seems like a difficult system to scale in a production
//    // environment, so more research is required before implementing that.
//
//    //    if (incomingAuthUser.equals(this.expectedCredentials.username()) && incomingAuthToken.equals
//    // (this.expectedCredentials.token())) {
//    //      return true;
//    //    } else {
//    //      return false;
//    //    }
//    return true;
//  }

  /**
   * Store the username and token into this Websocket session.
   *
   * @param token An authorization token used to authenticate the indicated user.
   */
  private void storeAuthInBtpSession(final BtpSession btpSession, final String token) {
    Objects.requireNonNull(btpSession);
    Objects.requireNonNull(token);

    final BtpSessionCredentials credentials = ImmutableBtpSessionCredentials.builder()
        .authToken(token)
        .build();
    btpSession.setValidAuthentication(credentials);
  }

  /**
   * Store the username and token into this Websocket session.
   *
   * @param token    An authorization token used to authenticate the indicated user.
   * @param username The username of the signed-in account.
   */
  private void storeAuthInBtpSession(final BtpSession btpSession, final String token, final Optional<String> username) {
    Objects.requireNonNull(btpSession);
    Objects.requireNonNull(token);
    Objects.requireNonNull(username);

    final BtpSessionCredentials credentials = ImmutableBtpSessionCredentials.builder()
        .authUsername(username)
        .authToken(token)
        .build();
    btpSession.setValidAuthentication(credentials);
  }

  /**
   * Construct a {@link BtpError} for the supplied request-id that can be returned when authentication is invalid.
   *
   * @param requestId
   *
   * @return A newly constructed {@link BtpError}.
   */
  private BtpError constructAuthError(final long requestId, final String errorMessage) {
    return BtpError.builder()
        .requestId(requestId)
        .errorCode(F00_NotAcceptedError)
        .errorData(errorMessage.getBytes())
        .build();
  }
}
