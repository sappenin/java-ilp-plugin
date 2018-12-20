package org.interledger.plugin.lpiv2.btp2.spring;

import org.interledger.btp.BtpErrorCode;
import org.interledger.btp.BtpPacket;
import org.interledger.btp.BtpResponse;
import org.interledger.btp.BtpRuntimeException;
import org.interledger.btp.BtpSession;
import org.interledger.encoding.asn.framework.CodecContext;
import org.interledger.plugin.lpiv2.btp2.AbstractBtpPlugin;
import org.interledger.plugin.lpiv2.btp2.BtpPluginSettings;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BinaryMessageToBtpPacketConverter;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BtpPacketToBinaryMessageConverter;
import org.interledger.plugin.lpiv2.btp2.subprotocols.BtpSubProtocolHandlerRegistry;

import com.google.common.collect.Maps;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * <p>An extension of {@link AbstractBtpPlugin} that operates over a Websocket mux with a Websocket
 * implementation provided by the Spring Framework. This class is abstract because the implementation that uses a
 * Websocket Client is different from the Websocket implementation that operates using a Websocket server, though this
 * class contains all logic and functionality that is common between the two.</p>
 */
public abstract class AbstractWebsocketBtpPlugin<PS extends BtpPluginSettings> extends AbstractBtpPlugin<PS> {

  protected final BinaryMessageToBtpPacketConverter binaryMessageToBtpPacketConverter;
  protected final BtpPacketToBinaryMessageConverter btpPacketToBinaryMessageConverter;

  // TODO: Use WeakHashMap?
  // When the client sends a request out to a peer, it will wait for an async response from that peer. When that
  // response comes back, it will be combined with a pending response.
  protected final Map<Long, CompletableFuture<Optional<BtpResponse>>> pendingResponses;

  // Starts life as `empty`. For a BTP plugin acting as a server, this will be populated once the WebSocket server is
  // turned on (Note that this implementation only supports a single authenticated webSocketSession). In the case of
  // a Websocket client, there will only have a single outbound connection, and thus a single session.
  protected Optional<WebSocketSession> webSocketSession = Optional.empty();

  /**
   * Required-args Constructor.
   */
  public AbstractWebsocketBtpPlugin(
      final PS pluginSettings,
      final CodecContext ilpCodecContext,
      final CodecContext btpCodecContext,
      final BtpSubProtocolHandlerRegistry btpSubProtocolHandlerRegistry,
      final BinaryMessageToBtpPacketConverter binaryMessageToBtpPacketConverter,
      final BtpPacketToBinaryMessageConverter btpPacketToBinaryMessageConverter
  ) {
    super(pluginSettings, ilpCodecContext, btpCodecContext, btpSubProtocolHandlerRegistry);
    this.binaryMessageToBtpPacketConverter = binaryMessageToBtpPacketConverter;
    this.btpPacketToBinaryMessageConverter = btpPacketToBinaryMessageConverter;
    this.pendingResponses = Maps.newConcurrentMap();
  }

  /**
   * Required-args Constructor.
   */
  public AbstractWebsocketBtpPlugin(
      final PS pluginSettings,
      final CodecContext ilpCodecContext,
      final CodecContext btpCodecContext,
      final BtpSubProtocolHandlerRegistry btpSubProtocolHandlerRegistry,
      final BinaryMessageToBtpPacketConverter binaryMessageToBtpPacketConverter,
      final BtpPacketToBinaryMessageConverter btpPacketToBinaryMessageConverter,
      final WebSocketSession webSocketSession
  ) {
    this(
        pluginSettings,
        ilpCodecContext,
        btpCodecContext,
        btpSubProtocolHandlerRegistry,
        binaryMessageToBtpPacketConverter,
        btpPacketToBinaryMessageConverter
    );

    this.webSocketSession = Optional.of(webSocketSession);
  }

//  /**
//   * Handle an incoming BinaryMessage from a Websocket by converting it into a {@link BtpMessage}and forwarding it to a
//   * BTP processor.
//   *
//   * @param webSocketSession
//   * @param incomingBinaryMessage
//   *
//   * @return A {@link BinaryMessage} that can immediately be returned to the caller (this response will contain
//   *     everything required to be eligible as a BTP response), or nothing if the response is {@link Optional#empty()}.
//   */
//  public Optional<BinaryMessage> handleBinaryMessage(
//      final WebSocketSession webSocketSession, final BinaryMessage incomingBinaryMessage
//  ) {
//    Objects.requireNonNull(webSocketSession);
//    Objects.requireNonNull(incomingBinaryMessage);
//
//    // If there's a problem de-serializing the BtpPacket from the BinaryMessage, then close the connection and
//    // return empty. This is one of the "tricky cases" as defined in the BTP spec where we don't want to get into
//    // an infinite loop.
//    final BtpPacket incomingBtpPacket;
//    try {
//      incomingBtpPacket = this.binaryMessageToBtpPacketConverter.convert(incomingBinaryMessage);
//    } catch (BtpConversionException btpConversionException) {
//      logger.error("Unable to deserialize BtpPacket from incomingBinaryMessage: {}", btpConversionException);
//      try {
//        this.disconnect().get();
//      } catch (Exception e) {
//        logger.error(e.getMessage(), e);
//      }
//      return Optional.empty();
//    }
//
//    try {
//      // If incomingBtpMessage is a BTPResponse, we need to connect it to a pending sendData. If this is a
//      // BtpMessage, we can simply handle it...
//      return new BtpPacketMapper<Optional<BinaryMessage>>() {
//        @Override
//        protected Optional<BinaryMessage> mapBtpMessage(final BtpMessage incomingBtpMessage) {
//          Objects.requireNonNull(incomingBtpMessage);
//          logger.trace("incomingBtpMessage: {}", incomingBtpMessage);
//
//          // A WebSocketSession always has a BtpSession, but it may not be authenticated...
//          final BtpSession btpSession = BtpSessionUtils.getBtpSessionFromWebSocketSession(webSocketSession);
//          final BtpResponse btpResponse = handleIncomingBtpMessage(btpSession, incomingBtpMessage);
//          return Optional.of(btpPacketToBinaryMessageConverter.convert(btpResponse));
//        }
//
//        @Override
//        protected Optional<BinaryMessage> mapBtpTransfer(final BtpTransfer incomingBtpTransfer) {
//          Objects.requireNonNull(incomingBtpTransfer);
//          logger.trace("incomingBtpMessage: {}", incomingBtpTransfer);
//          throw new RuntimeException("Not yet implemented!");
//        }
//
//        @Override
//        protected Optional<BinaryMessage> mapBtpError(BtpError incomingBtpError) {
//          Objects.requireNonNull(incomingBtpError);
//
//          logger.error("Incoming BtpError from `{}` with message `{}`",
//              getPluginSettings().getPeerAccountAddress(),
//              new String(incomingBtpError.getErrorData())
//          );
//
//          // The incoming message was a BtpError, so don't return a response to the peer.
//          return Optional.empty();
//        }
//
//        @Override
//        protected Optional<BinaryMessage> mapBtpResponse(final BtpResponse incomingBtpResponse) {
//          Objects.requireNonNull(incomingBtpResponse);
//
//          logger.trace("IncomingBtpResponse: {} ", incomingBtpResponse);
//
//          // Generally, BTP always returns a response to the caller, even under error conditions. There are two
//          // exceptions, however, listed as "tricky cases" in the BTP specification:
//          //
//          // 1. An unexpected BTP packet is received
//          // 2. An unreadable BTP packet is received
//          //
//          // If the packet was unreadable, then this method will have never been called, so we can ignore this
//          // case here. However, if an unexpected packet is encountered, we need to emit this error, but then return
//          // null to the caller of this method so that no response is returned to the BTP peer.
//
//          final CompletableFuture<Optional<BtpResponse>> pendingResponse = pendingResponses
//              .get(incomingBtpResponse.getRequestId());
//
//          // If there's a pending response, then connect the incoming response to the pending response. If there is no
//          // pending response, it means that
//
//          if (pendingResponse == null) {
//
//            //Should we call onIncomingBtpResponse here?
//
//            logger.error("No PendingResponse available to connect to incomingBtpResponse: {}", incomingBtpResponse);
//            return Optional.empty();
//          } else {
//            try {
//              // The pendingResponse has been previously returned to a caller, who is waiting for it to be completed or
//              // to timeout. So, if such a thing exists (getting here implies that it does exist), then we need to
//              // complete the pendingResponse with the value found in `incomingBtpResponse`.
//
//              // TODO: Try acceptEither instead (http://www.deadcoderising
//              // .com/java8-writing-asynchronous-code-with-completablefuture/)
//
//              final Object result = anyOf(pendingResponse, CompletableFuture.completedFuture(incomingBtpResponse))
//                  .handle((response, error) -> {
//                    /////////////////
//                    // The Exception case..
//                    if (error != null) {
//                      if (error instanceof BtpRuntimeException) {
//                        final BtpRuntimeException btpRuntimeException = (BtpRuntimeException) error;
//                        final BtpError btpError = constructBtpError(
//                            incomingBtpPacket.getRequestId(), btpRuntimeException.getMessage(),
//                            btpRuntimeException.getTriggeredAt(), btpRuntimeException.getCode()
//                        );
//                        return Optional.of(btpPacketToBinaryMessageConverter.convert(btpError));
//                      } else {
//                        // There was an error processing, so return a BtpError response back to the waiting caller.
//                        final BtpError btpError = constructBtpError(
//                            incomingBtpPacket.getRequestId(), error.getMessage(), Instant.now(),
//                            BtpErrorCode.T00_UnreachableError
//                        );
//                        return Optional.of(btpPacketToBinaryMessageConverter.convert(btpError));
//                      }
//                    }
//                    /////////////////
//                    // The Happy Path...
//                    else {
//                      // Getting here means that there is a response to be handled, so connect it to the pendingResponse.
//                      pendingResponse.complete(Optional.of(incomingBtpResponse));
//                      // The response is wired back through the pending-response, so return null here so that nothing
//                      // happens on this thread.
//                      return (BinaryMessage) null;
//                    }
//
//                  }).get();
//
//              // Convert to BinaryMessage since anyOf uses Object...generally, this should always return Optional#empty
//              // because BTP responses don't result in another response to the peer who sent the original response.
//              return Optional.ofNullable((BinaryMessage) result);
//
//              //               .exceptionally(ex -> {
//              //                  if (ex instanceof BtpRuntimeException) {
//              //                    final BtpRuntimeException btpRuntimeException = (BtpRuntimeException) ex;
//              //                    final BtpError btpError = constructBtpError(
//              //                      incomingBtpPacket.getRequestId(), btpRuntimeException.getMessage(),
//              //                      btpRuntimeException.getTriggeredAt(), btpRuntimeException.getCode()
//              //                    );
//              //                    return Optional.of(btpPacketToBinaryMessageConverter.convert(btpError));
//              //                  } else {
//              //                    // There was an error processing, so return a BtpError response back to the waiting caller.
//              //                    final BtpError btpError = constructBtpError(
//              //                      incomingBtpPacket.getRequestId(), ex.getMessage(), Instant.now(),
//              //                      BtpErrorCode.T00_UnreachableError
//              //                    );
//              //                    return Optional.of(btpPacketToBinaryMessageConverter.convert(btpError));
//              //                  }
//              //                })
//              //                .thenApply((btpResponseToConnectAsObject) -> {
//              //                  // Getting here means that
//              //
//              //                  // Client: Create a BTPSession (maybe handle in the registry?)
//              //                  BtpSessionCredentials btpSessionCredentials = ImmutableBtpSessionCredentials.builder()
//              //                    .name(btpSession.getPeerAccountAddress().getValue()).build();
//              //                  btpSession.setValidAuthentication(btpSessionCredentials);
//              //
//              //                  //final BtpResponse btpResponse = (BtpResponse) btpResponseToConnectAsObject;
//              //                  //return btpPacketToBinaryMessageConverter.convert(btpResponse);
//              //                  return (BinaryMessage) null;
//              //                })
//              //                .get()
//              // );
//
//            } catch (CompletionException | InterruptedException | ExecutionException e) {
//              if (e.getCause() instanceof BtpRuntimeException) {
//                throw (BtpRuntimeException) e.getCause();
//              } else {
//                throw new RuntimeException(e);
//              }
//            }
//          }
//        }
//      }.map(incomingBtpPacket);
//    } catch (BtpRuntimeException e) {
//      logger.error(e.getMessage(), e);
//      // If anything throws a BTP Exception, then return a BTP Error on the channel...
//      final BtpError btpError = e.toBtpError(incomingBtpPacket.getRequestId());
//      return Optional.ofNullable(btpPacketToBinaryMessageConverter.convert(btpError));
//    }
//  }

  /**
   * Finish the `sendData(InterledgerPreparePacket)` operation by using the {@link BtpSession} riding on top of a {@link
   * WebSocketSession}.
   *
   * @param btpPacket A {@link BtpPacket} that should be transmitted to the remote peer via BTP.
   *
   * @return A {@link CompletableFuture} that eventually yields either a {@link BtpResponse} or a {@link
   *     BtpRuntimeException}.
   */
  @Override
  protected CompletableFuture<Optional<BtpResponse>> doSendDataOverBtp(final BtpPacket btpPacket) {
    Objects.requireNonNull(btpPacket);

    // Send the BtpMessage to the remote peer using the websocket client....but first translate the BtpMessage to a
    // binary message.
    final BinaryMessage binaryMessage = this.btpPacketToBinaryMessageConverter.convert(btpPacket);
    return sendMessageWithPendingRepsonse(btpPacket.getRequestId(), binaryMessage);
  }

  /**
   * In addition to all super-class checks, there MUST be a webSocketSession in order for this plugin to be connected.
   */
  @Override
  public boolean isConnected() {
    return super.isConnected() && this.webSocketSession.isPresent();
  }

  /**
   * Helper method to send a message with a pending response that can be completed later once an incoming message is
   * received from a remote peer. This method bridges the async Websocket response received by an entirely different
   * thread to the Completable Future returned by this method to the original caller.
   *
   * @param requestId
   * @param webSocketMessage
   *
   * @return
   */
  protected CompletableFuture<Optional<BtpResponse>> sendMessageWithPendingRepsonse(
      final long requestId, final WebSocketMessage webSocketMessage
  ) {
    Objects.requireNonNull(webSocketMessage);

    return webSocketSession
        .map(session -> {
          try {
            // Register the pending response first, just in-cae the Websocket returns faster than this method can complete.
            final CompletableFuture<Optional<BtpResponse>> pendingResponse = registerPendingResponse(requestId);
            session.sendMessage(webSocketMessage);
            return pendingResponse;
          } catch (IOException e) {
            try {
              this.disconnect().get();
            } catch (Exception e1) {
              throw new RuntimeException(e1);
            }
            throw new RuntimeException(e);
          }
        })
        .orElseThrow(() -> new RuntimeException("Unable to send WebSocketMessage due to lack of a webSocketSession!"));
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
}
