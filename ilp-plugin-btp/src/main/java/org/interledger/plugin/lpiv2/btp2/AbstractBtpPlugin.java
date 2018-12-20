package org.interledger.plugin.lpiv2.btp2;

import static org.interledger.btp.BtpSubProtocol.ContentType.MIME_APPLICATION_OCTET_STREAM;
import static org.interledger.plugin.lpiv2.btp2.subprotocols.BtpSubProtocolHandlerRegistry.BTP_SUB_PROTOCOL_AUTH;
import static org.interledger.plugin.lpiv2.btp2.subprotocols.BtpSubProtocolHandlerRegistry.BTP_SUB_PROTOCOL_AUTH_TOKEN;
import static org.interledger.plugin.lpiv2.btp2.subprotocols.BtpSubProtocolHandlerRegistry.BTP_SUB_PROTOCOL_AUTH_USERNAME;
import static org.interledger.plugin.lpiv2.btp2.subprotocols.BtpSubProtocolHandlerRegistry.BTP_SUB_PROTOCOL_ILP;

import org.interledger.btp.BtpErrorCode;
import org.interledger.btp.BtpMessage;
import org.interledger.btp.BtpPacket;
import org.interledger.btp.BtpResponse;
import org.interledger.btp.BtpRuntimeException;
import org.interledger.btp.BtpSession;
import org.interledger.btp.BtpSubProtocol;
import org.interledger.btp.BtpSubProtocols;
import org.interledger.core.InterledgerErrorCode;
import org.interledger.core.InterledgerFulfillPacket;
import org.interledger.core.InterledgerPacket;
import org.interledger.core.InterledgerProtocolException;
import org.interledger.core.InterledgerRejectPacket;
import org.interledger.core.InterledgerResponsePacket;
import org.interledger.encoding.asn.framework.CodecContext;
import org.interledger.plugin.lpiv2.AbstractPlugin;
import org.interledger.plugin.lpiv2.Plugin;
import org.interledger.plugin.lpiv2.btp2.subprotocols.AbstractBtpSubProtocolHandler;
import org.interledger.plugin.lpiv2.btp2.subprotocols.BtpSubProtocolHandlerRegistry;
import org.interledger.plugin.lpiv2.btp2.subprotocols.ilp.IlpBtpSubprotocolHandler;
import org.interledger.plugin.lpiv2.exceptions.DataHandlerAlreadyRegisteredException;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * <p>An extension of {@link AbstractPlugin} that is capable of representing a data channel with no money involved.
 * This class takes care of most of the work translating between BTP and the ledger plugin interface (LPI), and will
 * send BTP messages with no knowledge of the data within, so it can be used for ILP packets. The implementation
 * returned by {@link #getMoneySender()} is a no-op because, by default, there is no system involved in handling money
 * with BTP.</p>
 *
 * <p>Two features must be defined in order for this Plugin to handle money. The first is
 * {@link #getMoneySender()}, which sends an amount of units to the peer for this Plugin. This should be done via a BTP
 * <tt>TRANSFER</tt> call. The second method is {@link #getMoneyHandler()}, which is called
 * on an incoming BTP <tt>TRANSFER</tt> message.</p>
 *
 * <p>
 * BtpSubProtocol
 * </p>
 *
 * <p>The main use of this Plugin, however, is as a building block for plugins that _do_ have an underlying ledger.</p>
 */
public abstract class AbstractBtpPlugin<T extends BtpPluginSettings> extends AbstractPlugin<T> implements Plugin<T> {

  private final CodecContext ilpCodecContext;
  private final CodecContext btpCodecContext;
  private final Random random;
  private final BtpSubProtocolHandlerRegistry btpSubProtocolHandlerRegistry;

  /**
   * Required-args Constructor.
   */
  public AbstractBtpPlugin(
      final T pluginSettings,
      final CodecContext ilpCodecContext,
      final CodecContext btpCodecContext,
      final BtpSubProtocolHandlerRegistry btpSubProtocolHandlerRegistry
  ) {
    super(pluginSettings);
    this.ilpCodecContext = Objects.requireNonNull(ilpCodecContext);
    this.btpCodecContext = Objects.requireNonNull(btpCodecContext);
    this.btpSubProtocolHandlerRegistry = btpSubProtocolHandlerRegistry;
    this.random = new SecureRandom();

    // No need to do anything with the data-handler becuase it's overridden below.
    // Peg the dataSender to this class's implementation so that it always translates to BTP in order to send.
    super.registerDataSender(this::sendData);

    // No-op because there is no money-handling in default BTP.
    super.registerMoneyHandler((amount) -> CompletableFuture.completedFuture(null));
    super.registerMoneySender((amount) -> CompletableFuture.completedFuture(null));
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
          this.btpSubProtocolHandlerRegistry
              .getHandler(primarySubprotocol.getProtocolName(), primarySubprotocol.getContentType())
              .orElseThrow(() -> new BtpRuntimeException(
                  BtpErrorCode.F00_NotAcceptedError,
                  String.format("No BTP Handler registered for BTP SubProtocol: %s",
                      primarySubprotocol.getProtocolName()))
              );

      final CompletableFuture<Optional<BtpSubProtocol>> btpSubProtocolResponse = handler
          .handleSubprotocolMessage(btpSession, incomingBtpMessage);

      // Add the response, but only if it's present.
      btpSubProtocolResponse.get().ifPresent(responses::add);

      // Now that there's a proper response, send it back to the connected client...
      return BtpResponse.builder()
          .requestId(incomingBtpMessage.getRequestId())
          .subProtocols(responses)
          .build();
    } catch (Exception e) {
      throw new BtpRuntimeException(BtpErrorCode.T00_UnreachableError, e.getMessage(), e);
    }
  }

  /**
   * Allows a sub-class to implement the actual logic of sending a {@link BtpPacket} over the appropriate mux, such we
   * Websockets.
   *
   * @param btpMessage A {@link BtpMessage}.
   *
   * @return A {@link CompletableFuture} that yields a {@link BtpResponse}.
   */
  protected abstract CompletableFuture<Optional<BtpResponse>> doSendDataOverBtp(final BtpPacket btpMessage)
      throws BtpRuntimeException;

  protected CodecContext getIlpCodecContext() {
    return ilpCodecContext;
  }

  protected CodecContext getBtpCodecContext() {
    return btpCodecContext;
  }

  /**
   * Returns the next random request id using a PRNG.
   */
  protected long nextRequestId() {
    return Math.abs(random.nextInt());
  }

  /**
   * An authentication message must have as its primary <tt>protocolData</tt> entry must have the name of 'auth',
   * content type <tt>MIME_APPLICATION_OCTET_STREAM</tt>, and empty data, and among the secondary entries, there MUST be
   * a UTF-8 'auth_token' entry.
   *
   * @return
   */
  public BtpMessage constructAuthMessage(
      final long requestId, final String authToken, final Optional<String> authUserName
  ) {
    Objects.requireNonNull(authToken);
    Objects.requireNonNull(authUserName);

    final BtpSubProtocol authSubProtocol = BtpSubProtocol.builder()
        .protocolName(BTP_SUB_PROTOCOL_AUTH)
        .contentType(BtpSubProtocol.ContentType.MIME_APPLICATION_OCTET_STREAM)
        .build();
    final BtpSubProtocols btpSubProtocols = BtpSubProtocols.fromPrimarySubProtocol(authSubProtocol);

    // In situations where no authentication is needed, the 'auth_token' data can be set to the empty string,
    // but it cannot be omitted.
    final BtpSubProtocol authTokenSubprotocol = BtpSubProtocol.builder()
        .protocolName(BTP_SUB_PROTOCOL_AUTH_TOKEN)
        .contentType(BtpSubProtocol.ContentType.MIME_APPLICATION_OCTET_STREAM)
        .data(authToken.getBytes(StandardCharsets.UTF_8))
        .build();
    btpSubProtocols.add(authTokenSubprotocol);

    authUserName.ifPresent($ -> {
      final BtpSubProtocol authUsernameSubprotocol = BtpSubProtocol.builder()
          .protocolName(BTP_SUB_PROTOCOL_AUTH_USERNAME)
          .contentType(BtpSubProtocol.ContentType.MIME_APPLICATION_OCTET_STREAM)
          .data($.getBytes(StandardCharsets.UTF_8))
          .build();
      btpSubProtocols.add(authUsernameSubprotocol);
    });

    return BtpMessage.builder()
        .requestId(requestId)
        .subProtocols(btpSubProtocols)
        .build();
  }

//  private BtpError constructBtpError(final long requestId, final BtpRuntimeException btpRuntimeException) {
//    Objects.requireNonNull(btpRuntimeException);
//    return BtpError.builder()
//        .requestId(requestId)
//        .errorCode(btpRuntimeException.getCode())
//        .triggeredAt(btpRuntimeException.getTriggeredAt())
//        .errorData(btpRuntimeException.getMessage().getBytes(Charset.forName("UTF-8")))
//        .build();
//  }

  //////////////////////
  // Helper Methods
  //////////////////////

//  protected final BtpError constructBtpError(final long requestId, final String errorData) {
//    Objects.requireNonNull(errorData);
//
//    // Respond with a BTP Error on the websocket session.
//    return this.constructBtpError(requestId, errorData, Instant.now(), BtpErrorCode.F00_NotAcceptedError);
//  }

//  protected final BtpError constructBtpError(final long requestId, final String errorData,
//      final Instant triggeredAt, final BtpErrorCode btpErrorCode) {
//    Objects.requireNonNull(errorData);
//
//    // Respond with a BTP Error on the websocket session.
//    return BtpError.builder()
//        .requestId(requestId)
//        .triggeredAt(triggeredAt)
//        .errorCode(btpErrorCode)
//        .errorData(errorData.getBytes(Charset.forName("UTF-8")))
//        .build();
//  }

  public BtpSubProtocolHandlerRegistry getBtpSubProtocolHandlerRegistry() {
    return btpSubProtocolHandlerRegistry;
  }

  // All BTP Plugins forward to the BtpSubprotocol registry to handle data.
  @Override
  public Optional<DataHandler> getDataHandler() {
    // When this plugin receives a new DataHandler, it must be connected to the BtpSubProtocol registered in the registry,
    // so we always just return that handler, if present.
    return this.getBtpSubProtocolHandlerRegistry()
        .getHandler(BTP_SUB_PROTOCOL_ILP, MIME_APPLICATION_OCTET_STREAM)
        .map(ilpHandler -> (IlpBtpSubprotocolHandler) ilpHandler)
        .map(IlpBtpSubprotocolHandler::getDataHandler);
  }

  // All BTP Plugins forward to the BtpSubprotocol registry to handle data.
  @Override
  public void unregisterDataHandler() {
    final IlpBtpSubprotocolHandler handler = this.getBtpSubProtocolHandlerRegistry()
        .getHandler(BTP_SUB_PROTOCOL_ILP, MIME_APPLICATION_OCTET_STREAM)
        .map(abstractHandler -> (IlpBtpSubprotocolHandler) abstractHandler)
        .orElseThrow(() -> new RuntimeException(
            String.format("BTP subprotocol handler with name `%s` MUST be registered!", BTP_SUB_PROTOCOL_ILP)));

    handler.unregisterDataHandler();
  }

  // All BTP Plugins forward to the BtpSubprotocol registry to handle data.
  @Override
  public void registerDataHandler(final DataHandler ilpDataHandler) throws DataHandlerAlreadyRegisteredException {
    // The BilateralDataHandler for Btp Plugins is always the ILP handler registered with the BtpProtocolRegistry, so setting
    // a handler here should overwrite the handler there.

    final IlpBtpSubprotocolHandler handler = this.getBtpSubProtocolHandlerRegistry()
        .getHandler(BTP_SUB_PROTOCOL_ILP, MIME_APPLICATION_OCTET_STREAM)
        .map(abstractHandler -> (IlpBtpSubprotocolHandler) abstractHandler)
        .orElseThrow(() -> new RuntimeException(
            String.format("BTP subprotocol handler with name `%s` MUST be registered!", BTP_SUB_PROTOCOL_ILP)));

    handler.registerDataHandler(getPluginSettings().getLocalNodeAddress(), ilpDataHandler);
  }

  /**
   * Translates an ILP Prepare Packet into BTP for further processing.
   *
   * @param preparePacket
   *
   * @return
   */
  private final CompletableFuture<Optional<InterledgerResponsePacket>> sendData(final InterledgerPacket preparePacket) {

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
    final BtpSubProtocol ilpSubProtocol = IlpBtpSubprotocolHandler
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

    final CompletableFuture<Optional<InterledgerResponsePacket>> response = this.doSendDataOverBtp(btpMessage)
        .thenApply(btpResponse -> btpResponse
            .map($ -> IlpBtpSubprotocolHandler.toIlpPacket($, ilpCodecContext))
            .map(Optional::of)
            .orElse(Optional.empty())
        )
        .thenApply(ilpPacket -> ilpPacket
            .map(p -> {
              // Convert the ilpPacket into either a fulfill or an exception.
              // TODO Use InterlederPacketHandler if this sticks around...
              if (InterledgerFulfillPacket.class.isAssignableFrom(p.getClass())) {
                return (InterledgerFulfillPacket) p;
              } else {
                return (InterledgerRejectPacket) p;
              }
            })
            .map(Optional::of)
            .orElse(Optional.empty())
        );

    // NOTE: Request/Response matching is a function of Websockets and being able to
    return response;
  }

  @Override
  public final void registerDataSender(final DataSender dataSender) throws DataHandlerAlreadyRegisteredException {
    throw new RuntimeException(
        "BTP Plugins may not register a customer DataSender because all send operations flow over BTP!");
  }


}
