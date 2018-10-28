package org.interledger.plugin.lpiv2.btp2;

import static org.interledger.plugin.lpiv2.btp2.subprotocols.BtpSubProtocolHandlerRegistry.BTP_SUB_PROTOCOL_AUTH;
import static org.interledger.plugin.lpiv2.btp2.subprotocols.BtpSubProtocolHandlerRegistry.BTP_SUB_PROTOCOL_AUTH_TOKEN;

import org.interledger.btp.BtpError;
import org.interledger.btp.BtpErrorCode;
import org.interledger.btp.BtpMessage;
import org.interledger.btp.BtpPacket;
import org.interledger.btp.BtpResponse;
import org.interledger.btp.BtpRuntimeException;
import org.interledger.btp.BtpSession;
import org.interledger.btp.BtpSubProtocol;
import org.interledger.btp.BtpSubProtocols;
import org.interledger.btp.BtpTransfer;
import org.interledger.core.InterledgerErrorCode;
import org.interledger.core.InterledgerFulfillPacket;
import org.interledger.core.InterledgerPreparePacket;
import org.interledger.core.InterledgerProtocolException;
import org.interledger.core.InterledgerRejectPacket;
import org.interledger.encoding.asn.framework.CodecContext;
import org.interledger.plugin.lpiv2.AbstractPlugin;
import org.interledger.plugin.lpiv2.Plugin;
import org.interledger.plugin.lpiv2.btp2.subprotocols.AbstractBtpSubProtocolHandler;
import org.interledger.plugin.lpiv2.btp2.subprotocols.BtpSubProtocolHandlerRegistry;
import org.interledger.plugin.lpiv2.btp2.subprotocols.ilp.AbstractIlpBtpSubprotocolHandler;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * <p>An extension of {@link Plugin} that is capable of representing a data channel with no money involved. This class
 * takes care of most of the work translating between BTP and the ledger plugin interface (LPI), and will send BTP
 * messages with no knowledge of the data within, so it can be used for ILP packets. The {@link #sendMoney(BigInteger)}
 * function is a no-op because, by default, there is no system involved in handling money with BTP.</p>
 *
 * <p>Two features must be defined in order for this Plugin to handle money. The first is
 * {@link #sendMoney(BigInteger)}, which sends an amount of units to the peer for this Plugin. This should be done via a
 * BTP <tt>TRANSFER</tt> call. The second method is {@link #onIncomingMoney(BigInteger)}, which is called on an incoming
 * BTP <tt>TRANSFER</tt> message.</p>
 *
 * <p>
 * BtpSubProtocol
 * </p>
 *
 * <p>The main use of this Plugin, however, is as a building block for plugins that _do_ have an underlying ledger.</p>
 */
public abstract class AbstractBtpPlugin<S extends BtpPluginSettings> extends AbstractPlugin<S> implements Plugin<S> {

  private final CodecContext ilpCodecContext;
  private final CodecContext btpCodecContext;
  private final Random random;
  private final BtpSubProtocolHandlerRegistry btpSubProtocolHandlerRegistry;

  // Because it would be too slow to atomically save all requestIds that are processed, they are not idempotent. It is
  // the responsibility of the requestor to make sure they don't duplicate requestIds. The implementation should ensure
  // that no two in-flight requests are sent out with the same requestId. The responder should always send back a
  // response to a request with the same requestId.
  // If a request is found in the Map, it is at lease pending. If it has a value of <tt>true</tt> then the request
  // has been acknowledged already.

  // See Javadoc for #registerPendingResponse for more details.
  //private final Map<Long, CompletableFuture<BtpResponse>> pendingResponses;

  /**
   * Required-args Constructor.
   */
  public AbstractBtpPlugin(
      final S pluginSettings,
      final CodecContext ilpCodecContext,
      final CodecContext btpCodecContext,
      final BtpSubProtocolHandlerRegistry btpSubProtocolHandlerRegistry
  ) {
    super(pluginSettings);
    this.ilpCodecContext = Objects.requireNonNull(ilpCodecContext);
    this.btpCodecContext = Objects.requireNonNull(btpCodecContext);
    this.btpSubProtocolHandlerRegistry = btpSubProtocolHandlerRegistry;
    this.random = new SecureRandom();
    //this.pendingResponses = Maps.newConcurrentMap();
  }

  public BtpResponse onIncomingBtpError(final BtpSession btpSession, final BtpError btpError)
      throws BtpRuntimeException {
    throw new RuntimeException("Not yet implemented!");
  }

  public BtpResponse onIncomingBtpTransfer(final BtpSession btpSession, final BtpTransfer btpTransfer)
      throws BtpRuntimeException {
    throw new RuntimeException("Not yet implemented!");
  }

  // TODO: Void?
  public BtpResponse onIncomingBtpResponse(final BtpSession btpSession, final BtpResponse btpResponse)
      throws BtpRuntimeException {
    throw new RuntimeException("Not yet implemented!");
  }

  /**
   * <p>Handles an incoming {@link BtpMessage} by delegating it to a registered handler.</p>
   *
   * <p>There are also a couple of tricky cases to handle:</p>
   *
   * <ul>
   * <li>If an unexpected BTP packet is received, no response should be sent. An unexpected BTP packet is a
   * response for which a request was not sent, or a response for a request which has already been responded to.</li>
   * <li>If an unreadable BTP packet is received, no response should be sent. An unreadable BTP packet is one which
   * is structurally invalid, i.e. terminates before length prefixes dictate or contains illegal characters.</li>
   * </ul>
   *
   * <p>Incoming BTP messages can be either requests or responses, so sub-classes should be careful to handle both
   * types of message, especially when bridging to synchronous API contracts, where it may become confusing as to
   * whether an incoming message corresponds to the input our output portion of a particular synchronous method.</p>
   *
   * @param btpSession
   * @param incomingBtpMessage
   *
   * @return
   */
  // TODO: Use future?
  public BtpResponse onIncomingBtpMessage(final BtpSession btpSession, final BtpMessage incomingBtpMessage)
      throws BtpRuntimeException {

    Objects.requireNonNull(btpSession);
    Objects.requireNonNull(incomingBtpMessage);

    try {
      final BtpSubProtocols responses = new BtpSubProtocols();

      // Only the primary sub-protocol should be handled. The secondary sub-protocols should not request additional
      // actions or information. If multiple actions or pieces of information are required, multiple separate Messages
      // should be sent. The secondary sub-protocols should only modify the request made in the primary sub-protocol, or
      // provide additional contextual data which can be consumed in a readonly way (without affecting the result).
      final BtpSubProtocol primarySubprotocol = incomingBtpMessage.getPrimarySubProtocol();
      final AbstractBtpSubProtocolHandler handler =
          this.btpSubProtocolHandlerRegistry.getHandler(primarySubprotocol.getProtocolName())
              .orElseThrow(() -> new BtpRuntimeException(
                  BtpErrorCode.F00_NotAcceptedError,
                  String.format("No BTP Handler registered for BTP SubProtocol: %s",
                      primarySubprotocol.getProtocolName()))
              );

      final CompletableFuture<Optional<BtpSubProtocol>> btpSubProtocolResponse = handler
          .handleSubprotocolMessage(btpSession, incomingBtpMessage);


      // TODO: Perhaps resurect this code if a Plugin will handle both binary and JSON, as an example. Hunch is that no sub-protocol does that,
      // so it's probably not worth doing.

//      switch (primarySubprotocol.getContentType()) {
//        case MIME_TEXT_PLAIN_UTF8: {
//          btpSubProtocolResponse = handler.handleTextSubprotocolMessage(btpSession, incomingBtpMessage);
//          break;
//        }
//
//        case MIME_APPLICATION_JSON: {
//          btpSubProtocolResponse = handler.handleJsonSubprotocolMessage(btpSession, incomingBtpMessage);
//          break;
//        }
//
//        case MIME_APPLICATION_OCTET_STREAM:
//        default: {
//          btpSubProtocolResponse = handler.handleBinarySubprotocolMessage(btpSession, incomingBtpMessage);
//          break;
//        }
//      }

      // Add the response, but only if it's present.
      btpSubProtocolResponse.get().ifPresent(responses::add);

      ////////////////////////
      // Now that there's a proper response, send it back to the connected client...
      ////////////////////////
      return BtpResponse.builder()
          .requestId(incomingBtpMessage.getRequestId())
          .subProtocols(responses)
          .build();
    } catch (Exception e) {
      throw new BtpRuntimeException(BtpErrorCode.T00_UnreachableError, e.getMessage(), e);
    }
  }

  /**
   * Perform the logic of sending an ILP prepare-packet to a remote peer using BTP. This method is transport-agnostic,
   * so implementations must define an implementation of the actual transport, such as Websocket or Http/2.
   *
   * @param preparePacket
   */
  @Override
  public CompletableFuture<InterledgerFulfillPacket> doSendData(final InterledgerPreparePacket preparePacket)
      throws InterledgerProtocolException {
    Objects.requireNonNull(preparePacket);

    // TODO: Implement re-connection logic, but only if this is a BTP Client. Servers simply have to wait to be
    // connected...
    // If the plugin is not connected, then throw an exception...
    if (!this.isConnected()) {
      throw new InterledgerProtocolException(
          InterledgerRejectPacket.builder()
              .message("Plugin not connected!")
              .triggeredBy(getPluginSettings().getLocalNodeAddress())
              .code(InterledgerErrorCode.T01_LEDGER_UNREACHABLE)
              .build()
      );
    }

    //if(this.get)

    // From JS...
    //      type: BtpPacket.TYPE_MESSAGE,
    //        requestId: await _requestId(),
    //      data: { protocolData: [{
    //      protocolName: 'ilp',
    //        contentType: BtpPacket.MIME_APPLICATION_OCTET_STREAM,
    //        data: buffer
    //    }] }
    //    }

    // This is just a translation layer. Transmit the above `preparePacket` to a remote peer via BTP.
    final BtpSubProtocol ilpSubProtocol = AbstractIlpBtpSubprotocolHandler
        .toBtpSubprotocol(preparePacket, ilpCodecContext);
    final BtpMessage btpMessage = BtpMessage.builder()
        .requestId(nextRequestId())
        .subProtocols(BtpSubProtocols.fromPrimarySubProtocol(ilpSubProtocol))
        .build();

    // This is synchronized by the Map...
    //    if (this.acknowledgedRequests.putIfAbsent(btpMessage.getRequestId(), false) == false) {
    //      // The request is already pending, so throw an exception.
    //      throw new RuntimeException(
    //        String.format("Encountered duplicate requestId: `%s`", btpMessage.getRequestId()));
    //    }

    // TODO: FIXME per https://stackoverflow.com/questions/33913193/completablefuture-waiting-for-first-one-normally-return

    final CompletableFuture<InterledgerFulfillPacket> response = this.doSendDataOverBtp(btpMessage)
        .thenApply(btpResponse -> AbstractIlpBtpSubprotocolHandler.toIlpPacket(btpResponse, ilpCodecContext))
        .thenApply(ilpPacket -> {
          // Convert the ilpPacket into either a fulfill or an exception.
          // TODO Use InterlederPacketHandler if this sticks around...
          if (InterledgerFulfillPacket.class.isAssignableFrom(ilpPacket.getClass())) {
            return (InterledgerFulfillPacket) ilpPacket;
          } else {
            // TODO: Completions?
            throw new InterledgerProtocolException((InterledgerRejectPacket) ilpPacket);
          }
        });

    // NOTE: Request/Response matching is a function of Websockets and being able to
    return response;
  }

  /**
   * Allows a sub-class to implement the actual logic of sending a {@link BtpPacket} over the appropriate transport,
   * such we Websockets.
   *
   * @param btpMessage A {@link BtpMessage}.
   *
   * @return A {@link CompletableFuture} that yields a {@link BtpResponse}.
   */
  protected abstract CompletableFuture<BtpResponse> doSendDataOverBtp(final BtpPacket btpMessage)
      throws BtpRuntimeException;

  protected CodecContext getIlpCodecContext() {
    return ilpCodecContext;
  }

  protected CodecContext getBtpCodecContext() {
    return btpCodecContext;
  }

  //  /**
  //   * This method is only ever called by the {@link AbstractIlpBtpSubprotocolHandler} as part of the BTP Websock machinery.
  //   */
  //  @Override
  //  public final CompletableFuture<InterledgerFulfillPacket> handleIncomingPacket(final InterledgerPreparePacket preparePacket)
  //    throws InterledgerProtocolException {
  //
  //    Objects.requireNonNull(preparePacket);
  //
  //    if (logger.isDebugEnabled()) {
  //      logger.debug("Handling InterledgerPreparePacket from {}", this.getAccountAddress());
  //    }
  //
  //    //    final Account sourceAccount = this.accountManager.getAccount(session.getAccountId())
  //    //      .orElseThrow(() -> new RuntimeException(
  //    //        String.format("No Account found for Interledger Address: %s", session.getAccountId()))
  //    //      );
  //
  //    // If local...
  //
  //
  //    // else, forward...
  //
  //    // TODO: Grab the proper lpi2 here from the routing table..
  //    Plugin plugin = null;
  //
  //    // TODO: Adjust the packet, if needed (expiry, amount, etc?)
  //    return plugin.sendPacket(preparePacket);
  //  }

  /**
   * Perform the logic of settling with a remote peer.
   *
   * @param amount
   */
  @Override
  protected CompletableFuture<Void> doSendMoney(BigInteger amount) {
    // No-op in vanilla BTP. Can be extended by an ILP Plugin.
    return CompletableFuture.completedFuture(null);
  }

  //  /**
  //   * Store the username and token into this Websocket session.
  //   *
  //   * @param username The username of the signed-in account.
  //   * @param token    An authorization token used to authenticate the indicated user.
  //   */
  //  private void storeAuthInWebSocketSession(
  //    final WebSocketSession webSocketSession, final String username, final String token
  //  ) {
  //    Objects.requireNonNull(username);
  //    Objects.requireNonNull(token);
  //
  //    Objects.requireNonNull(webSocketSession).getAttributes().putEntry(BtpSession.CREDENTIALS_KEY, username + ":" + token);
  //  }
  //
  //  private boolean isAuthenticated(final WebSocketSession webSocketSession) {
  //    return Objects.requireNonNull(webSocketSession).getAttributes().containsKey(BtpSession.CREDENTIALS_KEY);
  //  }

  /**
   * Returns the next random request id using a PRNG.
   */
  protected long nextRequestId() {
    return Math.abs(random.nextInt());
  }

  /**
   * Accessor the the BTP Plugin type of this BTP Plugin.
   */
  //public abstract BtpPluginType getBtpPluginType();

  /**
   * An authentication message must have as its primary <tt>protocolData</tt> entry must have the name of 'auth',
   * content type <tt>MIME_APPLICATION_OCTET_STREAM</tt>, and empty data, and among the secondary entries, there MUST be
   * a UTF-8 'auth_token' entry.
   *
   * @return
   */
  public BtpMessage constructAuthMessage(final long requestId, final String authToken) {

    final BtpSubProtocol authSubProtocol = BtpSubProtocol.builder()
        .protocolName(BTP_SUB_PROTOCOL_AUTH)
        .contentType(BtpSubProtocol.ContentType.MIME_APPLICATION_OCTET_STREAM)
        .build();

    //      final BtpSubProtocol authUsernameSubprotocol = BtpSubProtocol.builder()
    //        .protocolName(BTP_SUB_PROTOCOL_AUTH_USERNAME)
    //        .contentType(BtpSubProtocolContentType.MIME_TEXT_PLAIN_UTF8)
    //        .data(username.getBytes(StandardCharsets.UTF_8))
    //        .build();

    // In situations where no authentication is needed, the 'auth_token' data can be set to the empty string,
    // but it cannot be omitted.
    final BtpSubProtocol authTokenSubprotocol = BtpSubProtocol.builder()
        .protocolName(BTP_SUB_PROTOCOL_AUTH_TOKEN)
        .contentType(BtpSubProtocol.ContentType.MIME_APPLICATION_OCTET_STREAM)
        .data(authToken.getBytes(StandardCharsets.UTF_8))
        .build();

    final BtpSubProtocols btpSubProtocols = BtpSubProtocols.fromPrimarySubProtocol(authSubProtocol);
    //btpSubProtocols.add(authUsernameSubprotocol);
    btpSubProtocols.add(authTokenSubprotocol);

    return BtpMessage.builder()
        .requestId(requestId)
        .subProtocols(btpSubProtocols)
        .build();
  }

  private BtpError constructBtpError(final long requestId, final BtpRuntimeException btpRuntimeException) {
    Objects.requireNonNull(btpRuntimeException);
    return BtpError.builder()
        .requestId(requestId)
        .errorCode(btpRuntimeException.getCode())
        .triggeredAt(btpRuntimeException.getTriggeredAt())
        .errorData(btpRuntimeException.getMessage().getBytes(Charset.forName("UTF-8")))
        .build();
  }

  //////////////////////
  // Helper Methods
  //////////////////////

  protected final BtpError constructBtpError(final long requestId, final String errorData) {
    Objects.requireNonNull(errorData);

    // Respond with a BTP Error on the websocket session.
    return this.constructBtpError(requestId, errorData, Instant.now(), BtpErrorCode.F00_NotAcceptedError);
  }

  protected final BtpError constructBtpError(final long requestId, final String errorData,
      final Instant triggeredAt, final BtpErrorCode btpErrorCode) {
    Objects.requireNonNull(errorData);

    // Respond with a BTP Error on the websocket session.
    return BtpError.builder()
        .requestId(requestId)
        .triggeredAt(triggeredAt)
        .errorCode(btpErrorCode)
        .errorData(errorData.getBytes(Charset.forName("UTF-8")))
        .build();
  }

//  /**
//   * Indicates whether or not an instance of {@link AbstractBtpPlugin} is the client or the server in a websock session
//   * used for BTP communications.
//   */
//  public enum BtpPluginType {
//
//    /**
//     * This lpi2 is operating as a websocket <tt>client</tt>, and must connect to a remote server in order to begin a
//     * BTP session.
//     */
//    WS_CLIENT,
//
//    /**
//     * This lpi2 is operating as the <tt>server</tt> in a websocket connection, waiting for remote peers to connect to
//     * it in order to begin a BTP session
//     */
//    WS_SERVER;
//  }
}
