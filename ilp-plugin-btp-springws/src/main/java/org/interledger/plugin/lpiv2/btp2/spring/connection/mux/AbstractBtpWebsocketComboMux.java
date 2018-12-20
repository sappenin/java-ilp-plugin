package org.interledger.plugin.lpiv2.btp2.spring.connection.mux;

import static java.util.concurrent.CompletableFuture.anyOf;
import static org.interledger.plugin.lpiv2.btp2.subprotocols.BtpSubProtocolHandlerRegistry.BTP_SUB_PROTOCOL_AUTH;
import static org.interledger.plugin.lpiv2.btp2.subprotocols.BtpSubProtocolHandlerRegistry.BTP_SUB_PROTOCOL_AUTH_TOKEN;
import static org.interledger.plugin.lpiv2.btp2.subprotocols.BtpSubProtocolHandlerRegistry.BTP_SUB_PROTOCOL_AUTH_USERNAME;

import org.interledger.btp.BtpError;
import org.interledger.btp.BtpErrorCode;
import org.interledger.btp.BtpMessage;
import org.interledger.btp.BtpPacket;
import org.interledger.btp.BtpPacketMapper;
import org.interledger.btp.BtpResponse;
import org.interledger.btp.BtpRuntimeException;
import org.interledger.btp.BtpSession;
import org.interledger.btp.BtpSessionCredentials;
import org.interledger.btp.BtpSubProtocol;
import org.interledger.btp.BtpSubProtocol.ContentType;
import org.interledger.btp.BtpSubProtocols;
import org.interledger.btp.BtpTransfer;
import org.interledger.plugin.connections.mux.AbstractBilateralComboMux;
import org.interledger.plugin.lpiv2.btp2.spring.BtpSessionUtils;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BinaryMessageToBtpPacketConverter;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BtpConversionException;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BtpPacketToBinaryMessageConverter;
import org.interledger.plugin.lpiv2.btp2.subprotocols.AbstractBtpSubProtocolHandler;
import org.interledger.plugin.lpiv2.btp2.subprotocols.BtpSubProtocolHandlerRegistry;

import com.google.common.collect.Maps;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * <p>An extension of {@link AbstractBilateralComboMux} that accepts incoming Websocket connections from various hosts
 * that speak BTP. Once authenticated, each host can connect to a plugin for actual handling.</p>
 */
public abstract class AbstractBtpWebsocketComboMux extends AbstractBilateralComboMux {

  protected final BinaryMessageToBtpPacketConverter binaryMessageToBtpPacketConverter;
  protected final BtpPacketToBinaryMessageConverter btpPacketToBinaryMessageConverter;

  // TODO: Use WeakHashMap?
  // When the client sends a request out to a peer, it will wait for an async response from that peer. When that
  // response comes back, it will be combined with a pending response.
  protected final Map<Long, CompletableFuture<Optional<BtpResponse>>> pendingResponses;

  private final BtpSubProtocolHandlerRegistry btpSubProtocolHandlerRegistry;

  private final Random random;

  // Starts life as `empty`. There will only be a single connection in this MUX, and thus a single WS Session.
  protected Optional<WebSocketSession> webSocketSession = Optional.empty();

  public AbstractBtpWebsocketComboMux(
      final BinaryMessageToBtpPacketConverter binaryMessageToBtpPacketConverter,
      final BtpPacketToBinaryMessageConverter btpPacketToBinaryMessageConverter,
      final BtpSubProtocolHandlerRegistry btpSubProtocolHandlerRegistry
  ) {
    this.binaryMessageToBtpPacketConverter = Objects.requireNonNull(binaryMessageToBtpPacketConverter);
    this.btpPacketToBinaryMessageConverter = Objects.requireNonNull(btpPacketToBinaryMessageConverter);
    this.btpSubProtocolHandlerRegistry = Objects.requireNonNull(btpSubProtocolHandlerRegistry);

    this.pendingResponses = Maps.newConcurrentMap();
    this.random = new SecureRandom();
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
  public void handleBinaryMessage(
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
//
//       This functionality needs to be different between client and server...
//      client should never echo responses back to the server, so response MUSt always be empty.
      // A client response will
//      if(btpResponse.get().getSubProtocols().size() == 0){
//
//      }

      //On the client, we need to rejoin the pending response...

          // On the client, a pendingResponse will be waiting to complete the auth properly....we need to join the empty response to it.
          //.map(btpResponse -> this.joinPendingResponse(btpResponse))
          //.map($ -> Optional.<BinaryMessage>empty())
          //.orElse(Optional.empty());
    } else {
      // If the auth subprotocol completes successfully, then we'll end up here with an authenticated Websocket Session.
      final BtpPacket incomingBtpPacket;
      try {
        // If there's a problem de-serializing the BtpPacket from the BinaryMessage, then close the connection and
        // return empty. This is one of the "tricky cases" as defined in the BTP spec where we don't want to get into
        // an infinite loop.
        incomingBtpPacket = this.binaryMessageToBtpPacketConverter.convert(incomingBinaryMessage);
      } catch (BtpConversionException btpConversionException) {
        logger.error("Unable to deserialize BtpPacket from incomingBinaryMessage: {}", btpConversionException);
        this.disconnect().join();
        return;
      }

      response = this.handleBtpPacket(webSocketSession, incomingBtpPacket);
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
  public Optional<BtpResponse> handleBinaryAuthMessage(
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
   * Handle an incoming BTP Packet by detecting its actual type, and then forwarding it appropriately to the correct BTP
   * handling facility.
   *
   * @param webSocketSession
   * @param incomingBtpPacket
   *
   * @return
   */
  public Optional<BinaryMessage> handleBtpPacket(
      final WebSocketSession webSocketSession, final BtpPacket incomingBtpPacket
  ) {
    Objects.requireNonNull(webSocketSession);
    Objects.requireNonNull(incomingBtpPacket);

    try {
      // If incomingBtpMessage is a BTPResponse, we need to connect it to a pending sendData. If this is a
      // BtpMessage, we can simply handle it...
      return new BtpPacketMapper<Optional<BinaryMessage>>() {
        @Override
        protected Optional<BinaryMessage> mapBtpMessage(final BtpMessage incomingBtpMessage) {
          Objects.requireNonNull(incomingBtpMessage);
          logger.trace("incomingBtpMessage: {}", incomingBtpMessage);

          // A WebSocketSession always has a BtpSession, but it may not be authenticated...
          final BtpSession btpSession = BtpSessionUtils.getBtpSessionFromWebSocketSession(webSocketSession)
              .orElseThrow(() -> new RuntimeException("BtpSession is required!"));
          final BtpResponse btpResponse = handleIncomingBtpMessage(btpSession, incomingBtpMessage);
          return Optional.of(btpPacketToBinaryMessageConverter.convert(btpResponse));
        }

        @Override
        protected Optional<BinaryMessage> mapBtpTransfer(final BtpTransfer incomingBtpTransfer) {
          Objects.requireNonNull(incomingBtpTransfer);
          logger.trace("incomingBtpMessage: {}", incomingBtpTransfer);
          throw new RuntimeException("Not yet implemented!");
        }

        @Override
        protected Optional<BinaryMessage> mapBtpError(BtpError incomingBtpError) {
          Objects.requireNonNull(incomingBtpError);

          final BtpSession session = BtpSessionUtils.getBtpSessionFromWebSocketSession(webSocketSession)
              .orElseThrow(() -> new RuntimeException("BtpSession is required!"));
          logger.error(
              "Incoming BtpError from `{}` with message `{}`",
              session.getBtpSessionCredentials().get(), new String(incomingBtpError.getErrorData())
          );

          // The incoming message was a BtpError, so don't return a response to the peer.
          return Optional.empty();
        }

        @Override
        protected Optional<BinaryMessage> mapBtpResponse(final BtpResponse incomingBtpResponse) {
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

          // TODO: If auth fails on the server, we might need to send a response from the join....otherwise, just
          // return Optional.empty();
          if (joinPendingResponse(incomingBtpResponse)) {
            return Optional.empty();
          } else {
            return Optional.empty();
          }
        }
      }.map(incomingBtpPacket);
    } catch (BtpRuntimeException e) {
      logger.error(e.getMessage(), e);
      // If anything throws a BTP Exception, then return a BTP Error on the channel...
      final BtpError btpError = e.toBtpError(incomingBtpPacket.getRequestId());
      return Optional.ofNullable(btpPacketToBinaryMessageConverter.convert(btpError));
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
  // TODO: Use future?
  public BtpResponse handleIncomingBtpMessage(final BtpSession btpSession, final BtpMessage incomingBtpMessage)
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

  protected CompletableFuture<Optional<BtpResponse>> sendMessageWithPendingRepsonse(
      final long requestId, final WebSocketSession webSocketSession, final WebSocketMessage webSocketMessage
  ) {
    Objects.requireNonNull(webSocketMessage);
    try {
      // Register the pending response first, just in-cae the Websocket returns faster than this method can complete.
      final CompletableFuture<Optional<BtpResponse>> pendingResponse = registerPendingResponse(requestId);
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

  /**
   * <p>Register and return a "pending response", mapping it to the supplied {@code requestId}. This mechanism works by
   * returning a completed future to a caller, who then waits for the future to be completed. The receiver processes the
   * request, and eventually returns a response by completing the appropriate <tt>pending respsonse</tt>.</p>
   *
   * <p>The following diagram illustrates this flow:</p>
   *
   * <pre>
   * ┌──────────┐                                              ┌──────────┐
   * │          │────────────Request (Object)─────────────────▷│          │
   * │          │                                              │          │
   * │          │             Response (Uncompleted            │          │
   * │          │◁─────────────CompletableFuture)───△──────────┤          │
   * │          │                                   │          │          │
   * │          │                                   │          │          │
   * │          │                                   │          │          │
   * │          │                                   │          │          │
   * │  Sender  │                                   │ Complete │ Receiver │
   * │          │                                   └or Timeout┤          │
   * │          │                                              │          │
   * │          │                                              │          │
   * │          │                                              │          │
   * │          │                                              │          │
   * │          │                                              │          │
   * │          │                                              │          │
   * │          │                                              │          │
   * └──────────┘                                              └──────────┘
   * </pre>
   *
   * @param requestId The unique identifier of the request that should receive a response, but only once that response
   *                  can be returned.
   *
   * @return
   */
  protected final CompletableFuture<Optional<BtpResponse>> registerPendingResponse(final long requestId) {

    // TODO: Use WeakReferences here to prevent memory leaks...

    // This response will expire in the alotted time (see below). This response is immediately returned to the caller,
    // but nothing happens until this CF expires, or the CF is completed from a different thread (by passing-in an
    // incoming message, which is actually a response).
    final CompletableFuture<Optional<BtpResponse>> pendingResponse = CompletableFuture.supplyAsync(
        () -> {
          // TODO: Configure this amount as a property.
          // TODO: Move back to seconds and set a default of 15.
          LockSupport.parkNanos(TimeUnit.MINUTES.toNanos(15));
          throw new BtpRuntimeException(BtpErrorCode.F00_NotAcceptedError, "BTP SendData operation timed-out!");
        }
    );

    if (this.pendingResponses.putIfAbsent(requestId, pendingResponse) == null) {
      return pendingResponse;
    } else {
      // TODO: Just log an error and ignore?
      throw new RuntimeException("Encountered BTP message twice!");
    }
  }


  /**
   * Join a response from a remote server to a pending response that has been previously returned to a caller who is
   * waiting for it to be completed or to timeout.
   *
   * @param incomingBtpResponse A {@link BtpResponse} from a remote server that should be used to complete a pending
   *                            response future. Note that this value is never <tt>Optional</tt> because the system
   *                            either gets a response from a remote BTP connection, or else the pending future
   *                            times-out.
   *
   * @return {@code true} if this invocation caused a pending response to transition to a completed state, else {@code
   *     false}.
   */
  protected boolean joinPendingResponse(final BtpResponse incomingBtpResponse) {
    Objects.requireNonNull(incomingBtpResponse,
        "incomingBtpResponse must not be null in order to correlate to a pending response identifier!");

    final CompletableFuture<Optional<BtpResponse>> pendingResponse = pendingResponses
        .get(incomingBtpResponse.getRequestId());

    // If there's no pending response, then return empty, and log an error.
    if (pendingResponse == null) {
      logger.error("No PendingResponse available to connect to incomingBtpResponse: {}", incomingBtpResponse);
      return false;
    } else {
      // Always connect the `responseToReturn` to a pendingResponse, which has been previously returned to a caller
      // who is waiting for it to be completed or to timeout. If a pendingResponse exists (getting here implies that it
      // does exist), then we need to complete the pendingResponse with the value found in `responseToReturn`.

      try {
        // TODO: Consider acceptEither instead
        //  (http://www.deadcoderising.com/java8-writing-asynchronous-code-with-completablefuture/)

        // First, call `anyOf` on the pendingResponse and a new CF. If `pendingResponse` has timed-out, it will trigger
        // this future to return with an error. Otherwise, if `pendingResponse` has not timed-out, then the new CF will
        // be returned. Note that there is a race-condition here that if both CF's are completed, it's ambiguous which
        // one will be returned. However, this should rarely occur, and either result is tolerable.
        anyOf(pendingResponse, CompletableFuture.completedFuture(incomingBtpResponse))
            .handle((response, error) -> {
              if (error != null) {
                logger.error(error.getMessage(), error);
//               if (error instanceof BtpRuntimeException) {
//                 final BtpRuntimeException btpRuntimeException = (BtpRuntimeException) error;
//                 final BtpError btpError = constructBtpError(
//                     incomingBtpPacket.getRequestId(), btpRuntimeException.getMessage(),
//                     btpRuntimeException.getTriggeredAt(), btpRuntimeException.getCode()
//                 );
//                 return Optional.of(btpPacketToBinaryMessageConverter.convert(btpError));
//               } else {
//                 // There was an error processing, so return a BtpError response back to the waiting caller.
//                 final BtpError btpError = constructBtpError(
//                     incomingBtpPacket.getRequestId(), error.getMessage(), Instant.now(),
//                     BtpErrorCode.T00_UnreachableError
//                 );
//                 return Optional.of(btpPacketToBinaryMessageConverter.convert(btpError));
//               }
                return false;
              } else {
                // Pipe the incomingBtpResponse into the pending response so that whatever caller is waiting for it will
                // receive it.
                return pendingResponse.complete(Optional.of(incomingBtpResponse));
              }
            })
            // Join is preferable here as opposed to get(timeout) because the anyOf (above) combines a timeout future,
            // so no need to timeout again...
            .join();

        return true;
      } catch (CompletionException e) {
        if (e.getCause() instanceof BtpRuntimeException) {
          throw (BtpRuntimeException) e.getCause();
        } else {
          throw new RuntimeException(e);
        }
      }
    }
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

  public BtpSubProtocolHandlerRegistry getBtpSubProtocolHandlerRegistry() {
    return btpSubProtocolHandlerRegistry;
  }

}