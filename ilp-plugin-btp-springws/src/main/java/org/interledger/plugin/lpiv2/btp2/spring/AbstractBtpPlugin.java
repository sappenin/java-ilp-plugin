package org.interledger.plugin.lpiv2.btp2.spring;

import static org.interledger.plugin.lpiv2.btp2.subprotocols.BtpSubProtocolHandlerRegistry.BTP_SUB_PROTOCOL_AUTH;
import static org.interledger.plugin.lpiv2.btp2.subprotocols.BtpSubProtocolHandlerRegistry.BTP_SUB_PROTOCOL_AUTH_TOKEN;
import static org.interledger.plugin.lpiv2.btp2.subprotocols.BtpSubProtocolHandlerRegistry.BTP_SUB_PROTOCOL_AUTH_USERNAME;

import org.interledger.btp.BtpError;
import org.interledger.btp.BtpErrorCode;
import org.interledger.btp.BtpMessage;
import org.interledger.btp.BtpPacket;
import org.interledger.btp.BtpPacketMapper;
import org.interledger.btp.BtpResponse;
import org.interledger.btp.BtpResponsePacket;
import org.interledger.btp.BtpRuntimeException;
import org.interledger.btp.BtpSession;
import org.interledger.btp.BtpSessionCredentials;
import org.interledger.btp.BtpSubProtocol;
import org.interledger.btp.BtpSubProtocol.ContentType;
import org.interledger.btp.BtpSubProtocols;
import org.interledger.btp.BtpTransfer;
import org.interledger.core.InterledgerPreparePacket;
import org.interledger.core.InterledgerResponsePacket;
import org.interledger.encoding.asn.framework.CodecContext;
import org.interledger.plugin.DataHandler;
import org.interledger.plugin.lpiv2.AbstractPlugin;
import org.interledger.plugin.lpiv2.btp2.BtpPluginSettings;
import org.interledger.plugin.lpiv2.btp2.BtpReceiver;
import org.interledger.plugin.lpiv2.btp2.BtpResponsePacketMapper;
import org.interledger.plugin.lpiv2.btp2.BtpSender;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BinaryMessageToBtpPacketConverter;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BtpConversionException;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BtpPacketToBinaryMessageConverter;
import org.interledger.plugin.lpiv2.btp2.subprotocols.AbstractBtpSubProtocolHandler;
import org.interledger.plugin.lpiv2.btp2.subprotocols.BtpSubProtocolHandlerRegistry;
import org.interledger.plugin.lpiv2.btp2.subprotocols.ilp.IlpBtpConverter;
import org.interledger.plugin.lpiv2.exceptions.DataHandlerAlreadyRegisteredException;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public abstract class AbstractBtpPlugin<PS extends BtpPluginSettings> extends AbstractPlugin<PS>
    implements BtpSender, BtpReceiver {

  private final CodecContext ilpCodecContext;
  private final Random random;

  // The credentials to use for authenticating the BTP session this plugin is being used for.
  private final BtpSessionCredentials btpSessionCredentials;

  private final BinaryMessageToBtpPacketConverter binaryMessageToBtpPacketConverter;
  private final BtpPacketToBinaryMessageConverter btpPacketToBinaryMessageConverter;
  private final BtpSubProtocolHandlerRegistry btpSubProtocolHandlerRegistry;
  private final PendingResponseManager<BtpResponsePacket> pendingResponseManager;

  /**
   * Required-args Constructor.
   */
  public AbstractBtpPlugin(
      final PS pluginSettings,
      final CodecContext ilpCodecContext,
      final BinaryMessageToBtpPacketConverter binaryMessageToBtpPacketConverter,
      final BtpPacketToBinaryMessageConverter btpPacketToBinaryMessageConverter,
      final BtpSubProtocolHandlerRegistry btpSubProtocolHandlerRegistry
  ) {
    super(pluginSettings);

    this.ilpCodecContext = Objects.requireNonNull(ilpCodecContext);

    this.binaryMessageToBtpPacketConverter = Objects.requireNonNull(binaryMessageToBtpPacketConverter);
    this.btpPacketToBinaryMessageConverter = Objects.requireNonNull(btpPacketToBinaryMessageConverter);
    this.btpSubProtocolHandlerRegistry = Objects.requireNonNull(btpSubProtocolHandlerRegistry);
    this.pendingResponseManager = new PendingResponseManager<>(BtpResponsePacket.class);

    this.random = new SecureRandom();

    this.btpSessionCredentials = BtpSessionCredentials.builder()
        .authUsername(pluginSettings.getAuthUsername())
        .authToken(getPluginSettings().getSecret())
        .build();
  }

  @Override
  public Optional<DataHandler> getDataHandler() {
    return Optional.of(this.btpSubProtocolHandlerRegistry.getIlpSubProtocolHandler().getDataHandler());
  }

  /**
   * Delegate all calls to the ILP BTP Subprotocol Handler.
   *
   * // By default, an abstract BTP plugin will send BTP messages to the proper subprotocol handler. ILP-subprotocol //
   * messages will find their way to the ILP Subprotocol handler, which needs its own DataHandler. When a plugin caller
   * // call this method, we connect the externally registered handler to the ILP subprotocol handler via this call.
   */
  @Override
  public void registerDataHandler(DataHandler ilpDataHandler) throws DataHandlerAlreadyRegisteredException {
    this.btpSubProtocolHandlerRegistry.getIlpSubProtocolHandler().registerDataHandler(
        this.getPluginSettings().getOperatorAddress(), ilpDataHandler
    );
  }

  /**
   * Delegate all calls to the ILP BTP Subprotocol Handler.
   */
  @Override
  public void unregisterDataHandler() {
    this.btpSubProtocolHandlerRegistry.getIlpSubProtocolHandler().unregisterDataHandler();
  }

  /**
   * Accessor for a WebSocket session. A client will not create a session until after `connect` is called, whereas a
   * server should always have a session available.
   *
   * @return An instance of {@link WebSocketSession}.
   *
   * @throws RuntimeException if the session is null.
   */
  protected abstract WebSocketSession getWebSocketSession();

  ////////////////////
  // BtpSender Methods
  ////////////////////

  // Send the preparePacket out over the webSocketSession using BTP encoding...
  @Override
  public CompletableFuture<Optional<InterledgerResponsePacket>> sendData(final InterledgerPreparePacket preparePacket) {
    Objects.requireNonNull(preparePacket);

    final BtpSubProtocols btpSubProtocols = new BtpSubProtocols();
    final BtpSubProtocol ilpPrepare = IlpBtpConverter.toBtpSubprotocol(preparePacket, ilpCodecContext);
    btpSubProtocols.add(ilpPrepare);

    return this
        /////////////////////////
        // Send the BTP Message out over the WebSocket session.
        /////////////////////////
        .sendBtpMessage(
            BtpMessage.builder()
                .requestId(nextRequestId())
                .subProtocols(btpSubProtocols)
                .build(),
            // TODO: Make the expiry time-buffer configurable.
            Duration.between(Instant.now(), preparePacket.getExpiresAt().minusSeconds(2))
        )
        /////////////////////////
        // Expect a response, but handle the Connection-error condition as well...
        /////////////////////////
        .handle((btpResponsePacket, error) -> {
          if (error != null) {
            logger.error(error.getMessage(), error);
            return Optional.empty();
          } else {
            // Convert the BtpResponse to the proper ILP response.
            return new BtpResponsePacketMapper<Optional<InterledgerResponsePacket>>() {
              @Override
              protected Optional<InterledgerResponsePacket> handleBtpError(final BtpError btpError) {
                Objects.requireNonNull(btpError);
                logger.error("BTP Error while attempting to sendBtpMessage: {}", btpError);
                return Optional.empty();
              }

              @Override
              protected Optional<InterledgerResponsePacket> handleBtpResponse(final BtpResponse btpResponse) {
                Objects.requireNonNull(btpResponse);
                return Optional.of(IlpBtpConverter.toIlpPacket(btpResponse, ilpCodecContext));
              }
            }.map(btpResponsePacket);
          }
        });
  }

  /**
   * Converts {@code btpMessage} into the proper binary format and then sends the payload out over the websocket,
   * waiting for a response.
   */
  @Override
  public CompletableFuture<BtpResponsePacket> sendBtpMessage(final BtpMessage btpMessage, final Duration waitTime) {
    Objects.requireNonNull(btpMessage);
    return this.sendBtpPacket(btpMessage, waitTime);
  }

  @Override
  public CompletableFuture<Void> sendMoney(BigInteger amount) {
    return this
        /////////////////////////
        // Send the BTP Transfer out over the WebSocket session.
        /////////////////////////
        .sendBtpTransfer(
            BtpTransfer.builder()
                .requestId(nextRequestId())
                .amount(amount)
                .build(),
            // TODO: Make the Money expiry time-buffer configurable to accomadate settlement processes that might take
            //  varying amounts of time.
            Duration.of(60, ChronoUnit.SECONDS)
        )
        /////////////////////////
        // Expect a Void response, but handle the Connection-error condition as well...
        /////////////////////////
        .handle((btpResponsePacket, error) -> {
          if (error != null) {
            logger.error(error.getMessage(), error);
            return null;
          } else {
            // Convert the BtpResponse to the proper ILP response.
            return new BtpResponsePacketMapper<Void>() {
              @Override
              protected Void handleBtpError(final BtpError btpError) {
                Objects.requireNonNull(btpError);
                logger.error("BTP Error while attempting to sendBtpMessage: {}", btpError);
                return null;
              }

              @Override
              protected Void handleBtpResponse(final BtpResponse btpResponse) {
                Objects.requireNonNull(btpResponse);
                // Delegate to the sub-class to actually "send" the money.
                doSendMoney(amount);
                return null;
              }
            }.map(btpResponsePacket);
          }
        });
  }

  /**
   * Allows sub-classes to implement any sendMoney logic.
   *
   * @param amount
   */
  public abstract void doSendMoney(BigInteger amount);

  @Override
  public CompletableFuture<BtpResponsePacket> sendBtpTransfer(final BtpTransfer btpTransfer, final Duration waitTime) {
    Objects.requireNonNull(btpTransfer);
    return this.sendBtpPacket(btpTransfer, waitTime);
  }

  private final CompletableFuture<BtpResponsePacket> sendBtpPacket(final BtpPacket btpPacket, final Duration waitTime) {
    Objects.requireNonNull(btpPacket);
    final WebSocketSession webSocketSession = getWebSocketSession();

    // Translate the btpMessage to a BinaryMessage, and send out using the WebSocketSession.
    final BinaryMessage binaryMessage = this.btpPacketToBinaryMessageConverter.convert(btpPacket);
    return this.sendMessageWithPendingRepsonse(btpPacket.getRequestId(), webSocketSession, binaryMessage, waitTime)
        .handle((response, error) -> {
          if (error != null) {
            // the pending response timed out or otherwise had a problem...
            throw new RuntimeException(error.getMessage(), error);
          } else {
            // Might be an error or a response
            return response;
          }
        });
  }

  //////////////////////
  // BtpReceiver Methods
  //////////////////////

  /**
   * Handle an incoming BTP Packet. The payload may be a BTP Request type, or it may be a BTP response type, so
   * implementations must handle all BTP packet types approporiately.
   *
   * @param incomingBtpPacket A {@link BtpPacket} sent from the bilateral BTP peer.
   *
   * @return An optionally-present response of type {@link BtpResponsePacket}.
   */
  @Override
  public Optional<BtpResponsePacket> handleBtpPacket(final BtpSession btpSession, final BtpPacket incomingBtpPacket) {
    Objects.requireNonNull(btpSession, "BtpSession is required!");
    Objects.requireNonNull(incomingBtpPacket);

    try {
      // If incomingBtpMessage is a BTPResponse, we need to connect it to a pending sendData. If this is a
      // BtpMessage, we can simply handle it...
      return new BtpPacketMapper<Optional<BtpResponsePacket>>() {
        @Override
        protected Optional<BtpResponsePacket> mapBtpMessage(final BtpMessage incomingBtpMessage) {
          Objects.requireNonNull(incomingBtpMessage);
          logger.trace("incomingBtpMessage: {}", incomingBtpMessage);

          // A WebSocketSession always has a BtpSession, but it may not be authenticated...
          final BtpResponse btpResponse = handleIncomingBtpMessage(btpSession, incomingBtpMessage);
          return Optional.of(btpResponse);
        }

        @Override
        protected Optional<BtpResponsePacket> mapBtpTransfer(final BtpTransfer incomingBtpTransfer) {
          Objects.requireNonNull(incomingBtpTransfer);
          logger.trace("incomingBtpMessage: {}", incomingBtpTransfer);

          final BtpResponse btpResponse = handleIncomingBtpTransfer(btpSession, incomingBtpTransfer);
          return Optional.of(btpResponse);
        }

        @Override
        protected Optional<BtpResponsePacket> mapBtpError(BtpError incomingBtpError) {
          Objects.requireNonNull(incomingBtpError);
          logger.error(
              "Incoming BtpError from `{}` with message `{}`",
              btpSession.getBtpSessionCredentials().get(), new String(incomingBtpError.getErrorData())
          );

          // The incoming message was a BtpError, so don't return a response to the peer.
          return Optional.empty();
        }

        @Override
        protected Optional<BtpResponsePacket> mapBtpResponse(final BtpResponse incomingBtpResponse) {
          Objects.requireNonNull(incomingBtpResponse);

          logger.trace("IncomingBtpResponse: {} ", incomingBtpResponse);

          // Generally, BTP always returns a response to the caller, even under error conditions. There are two
          // exceptions, however, listed as "tricky cases" in the BTP specification:
          //
          // 1. An unexpected BTP packet is received
          // 2. An unreadable BTP packet is received
          //
          // If the packet was unreadable, then this method will have never been called, so we can ignore this
          // case here. However, if an unexpected packet is encountered, we need to emit this error, but then return
          // null to the caller of this method so that no response is returned to the BTP peer.

          pendingResponseManager.joinPendingResponse(incomingBtpPacket.getRequestId(), incomingBtpResponse);
          return Optional.empty();
        }
      }.map(incomingBtpPacket);
    } catch (BtpRuntimeException e) {
      logger.error(e.getMessage(), e);
      // If anything throws a BTP Exception, then return a BTP Error on the channel...
      final BtpError btpError = e.toBtpError(incomingBtpPacket.getRequestId());
      return Optional.of(btpError);
    }
  }

  /**
   * Handle an incoming BinaryMessage from a Websocket by converting it into a {@link BtpMessage} and forwarding it to
   * the appropriate receiver (i.e., plugin).
   *
   * @param webSocketSession
   * @param incomingBinaryMessage
   *
   * @return A {@link BinaryMessage} that can immediately be returned to the caller (this response will contain
   *     everything required to be eligible as a BTP response), or nothing if the response is {@link Optional#empty()}.
   */
  protected void handleBinaryMessage(
      final WebSocketSession webSocketSession, final BinaryMessage incomingBinaryMessage
  ) {
    Objects.requireNonNull(webSocketSession);
    Objects.requireNonNull(incomingBinaryMessage);

    // The first message in a WebSocketSession MUST be the auth protocol. Thus, as long as the BtpSession is not
    // authenticated, then we should attempt to perform the auth sub_protocol.
    final BtpSession btpSession = BtpSessionUtils.getBtpSessionFromWebSocketSession(webSocketSession)
        .orElseThrow(() -> new RuntimeException("BtpSession is required!"));

    final Optional<BinaryMessage> response;
    if (!btpSession.isAuthenticated()) {
      // Do Auth. This will merely initialize the credentials into the BTPSession and return an Ack.
      response = this.handleBinaryAuthMessage(webSocketSession, incomingBinaryMessage)
          .map(foo -> btpPacketToBinaryMessageConverter.convert(foo));
    } else {
      // If the auth subprotocol completes successfully, then we'll end up here with an authenticated Websocket Session.
      try {
        // If there's a problem de-serializing the BtpPacket from the BinaryMessage, then close the connection and
        // return empty. This is one of the "tricky cases" as defined in the BTP spec where we don't want to get into
        // an infinite loop.
        final BtpPacket incomingBtpPacket = this.binaryMessageToBtpPacketConverter.convert(incomingBinaryMessage);
        final Optional<BtpResponsePacket> btpResponse = this.handleBtpPacket(btpSession, incomingBtpPacket);
        if (btpResponse.isPresent()) {
          response = Optional.of(btpPacketToBinaryMessageConverter.convert(btpResponse.get()));
        } else {
          response = Optional.empty();
        }
      } catch (BtpConversionException btpConversionException) {
        logger.error("Unable to deserialize BtpPacket from incomingBinaryMessage: {}", btpConversionException);
        this.disconnect().join();
        return;
      }
    }

    // Return the response to the caller, if there is a response.
    response.ifPresent($ -> {
      try {
        // TODO: What does the other side of the WebSocket see if there's an exception here?
        webSocketSession.sendMessage($);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });

  }

  /**
   * Handle an incoming BinaryMessage from a Websocket, assuming it's a BTP Auth message.
   *
   * @param webSocketSession
   * @param incomingBinaryMessage
   *
   * @return A {@link BinaryMessage} that can immediately be returned to the caller (this response will contain
   *     everything required to be eligible as a BTP response), or nothing if the response is {@link Optional#empty()}.
   */
  protected Optional<BtpResponse> handleBinaryAuthMessage(
      final WebSocketSession webSocketSession, final BinaryMessage incomingBinaryMessage
  ) {
    Objects.requireNonNull(webSocketSession);
    Objects.requireNonNull(incomingBinaryMessage);

    final AbstractBtpSubProtocolHandler btpAuthSubprotocolHandler = this.btpSubProtocolHandlerRegistry
        .getHandler(BTP_SUB_PROTOCOL_AUTH, ContentType.MIME_APPLICATION_OCTET_STREAM)
        .orElseThrow(() -> new RuntimeException("No Auth Subprotocol Handler registered!"));

    // If there's a problem de-serializing the BtpPacket from the BinaryMessage, then close the connection and
    // return empty. This is one of the "tricky cases" as defined in the BTP spec where we don't want to get into
    // an infinite loop.
    final BtpPacket incomingBtpPacket;
    try {
      incomingBtpPacket = this.binaryMessageToBtpPacketConverter.convert(incomingBinaryMessage);
      final BtpSession btpSession = BtpSessionUtils.getBtpSessionFromWebSocketSession(webSocketSession)
          .orElseThrow(() -> new RuntimeException("BtpSession is required!"));
      return btpAuthSubprotocolHandler.handleSubprotocolMessage(btpSession, incomingBtpPacket)
          .thenApply(btpSubProtocol -> btpSubProtocol
              .map($ -> {
                // If there's no exception, then reaching here means the btp_auth SubProtocol succeeded.
                // Ack the response, but only if
                final BtpSubProtocols responses = new BtpSubProtocols();
                responses.add($);
                final BtpResponse response = BtpResponse.builder()
                    .requestId(incomingBtpPacket.getRequestId())
                    .subProtocols(responses)
                    .build();
                return response;
              })
              //.map(btpPacketToBinaryMessageConverter::convert)
              .map(Optional::of)
              // If there is no sub-protocol to return, then return empty so no response is sent back over the connection.
              .orElse(Optional.empty())
          )
          .join();
    } catch (BtpConversionException btpConversionException) {
      logger.error(btpConversionException.getMessage(), btpConversionException);
      throw btpConversionException;
    }
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
  protected BtpResponse handleIncomingBtpMessage(final BtpSession btpSession, final BtpMessage incomingBtpMessage)
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
   * <p>BTP Transfer is used to send proof of payment, payment channel claims, or other settlement information to the
   * other connector. The amount should indicate the additional value of this settlement state (compared to the previous
   * settlement state), in a unit that was agreed out-of-band.</p>
   *
   * @param btpSession          The {@link BtpSession} for this incoming BTP transfer payload.
   * @param incomingBtpTransfer An instance of {@link BtpTransfer} indicating the counterparty of this account has made
   *                            some sort of settlement payment.
   *
   * @return
   */
  protected BtpResponse handleIncomingBtpTransfer(
      final BtpSession btpSession, final BtpTransfer incomingBtpTransfer
  ) throws BtpRuntimeException {

    Objects.requireNonNull(btpSession);
    Objects.requireNonNull(incomingBtpTransfer);

    return this.getMoneyHandler()
        // Delegate to the registered handler...
        .map(registeredMoneyHandler -> {
          // Block until the handler finishes...
          registeredMoneyHandler.handleIncomingMoney(incomingBtpTransfer.getAmount()).join();
          // Response is returned if this peer acknowledges the Transfer to the other side of this BTP connection.
          return BtpResponse.builder().requestId(incomingBtpTransfer.getRequestId()).build();
        })
        .orElseThrow(() -> new RuntimeException("No MoneyHandler registered!"));
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
  protected BtpMessage constructAuthMessage(
      final long requestId, final BtpSessionCredentials btpSessionCredentials
  ) {
    Objects.requireNonNull(btpSessionCredentials);

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
        .data(btpSessionCredentials.getAuthToken().getBytes(StandardCharsets.UTF_8))
        .build();
    btpSubProtocols.add(authTokenSubprotocol);

    btpSessionCredentials.getAuthUsername().ifPresent($ -> {
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

  protected CompletableFuture<BtpResponsePacket> sendMessageWithPendingRepsonse(
      final long requestId, final WebSocketSession webSocketSession, final WebSocketMessage webSocketMessage,
      final Duration waitTime
  ) {
    Objects.requireNonNull(webSocketMessage);
    try {
      // Register the pending response first, just in-case the Websocket returns faster than this method can complete.

      // The amount of time to wait for a response is a function of `expiresAt`, which is when the incoming ILP packet
      // expires. Thus, we should not wait so long that we allow this packet to expire. Instead, we want to reject the
      // packet in time, so we want to make sure our process expires at least 2 seconds before the ILP packet does.
      final CompletableFuture<BtpResponsePacket> pendingResponse = this.pendingResponseManager
          .registerPendingResponse(requestId, waitTime.getSeconds(), TimeUnit.SECONDS);
      webSocketSession.sendMessage(webSocketMessage);
      return pendingResponse;
    } catch (IOException e) {
      try {
        this.disconnect().get();
      } catch (Exception e1) {
        throw new RuntimeException(e1);
      }
      throw new RuntimeException(e);
    }
  }

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

  public BinaryMessageToBtpPacketConverter getBinaryMessageToBtpPacketConverter() {
    return binaryMessageToBtpPacketConverter;
  }

  public BtpPacketToBinaryMessageConverter getBtpPacketToBinaryMessageConverter() {
    return btpPacketToBinaryMessageConverter;
  }

  public BtpSessionCredentials getBtpSessionCredentials() {
    return this.btpSessionCredentials;
  }

  public PendingResponseManager<BtpResponsePacket> getPendingResponseManager() {
    return pendingResponseManager;
  }
}
