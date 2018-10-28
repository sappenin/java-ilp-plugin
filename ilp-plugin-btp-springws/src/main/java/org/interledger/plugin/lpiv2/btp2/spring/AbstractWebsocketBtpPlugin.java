package org.interledger.plugin.lpiv2.btp2.spring;

import static java.util.concurrent.CompletableFuture.anyOf;

import org.interledger.btp.BtpError;
import org.interledger.btp.BtpErrorCode;
import org.interledger.btp.BtpMessage;
import org.interledger.btp.BtpPacket;
import org.interledger.btp.BtpPacketHandler;
import org.interledger.btp.BtpResponse;
import org.interledger.btp.BtpRuntimeException;
import org.interledger.btp.BtpSession;
import org.interledger.btp.BtpSessionCredentials;
import org.interledger.btp.BtpTransfer;
import org.interledger.btp.ImmutableBtpSessionCredentials;
import org.interledger.core.InterledgerPreparePacket;
import org.interledger.encoding.asn.framework.CodecContext;
import org.interledger.plugin.lpiv2.btp2.AbstractBtpPlugin;
import org.interledger.plugin.lpiv2.btp2.BtpPluginSettings;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BinaryMessageToBtpPacketConverter;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BtpConversionException;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BtpPacketToBinaryMessageConverter;
import org.interledger.plugin.lpiv2.btp2.subprotocols.BtpSubProtocolHandlerRegistry;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * <p>An extension of {@link AbstractBtpPlugin} that operates over a Websocket transport with a Websocket
 * implementation provided by the Spring Framework. This class is abstract because the implementation that uses a
 * Websocket Client is different from the Websocket implementation that operates using a Websocket server, though this
 * class contains all logic and functionality that is common between the two.</p>
 */
public abstract class AbstractWebsocketBtpPlugin<S extends BtpPluginSettings> extends AbstractBtpPlugin<S> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractWebsocketBtpPlugin.class);
  protected final BinaryMessageToBtpPacketConverter binaryMessageToBtpPacketConverter;
  protected final BtpPacketToBinaryMessageConverter btpPacketToBinaryMessageConverter;
  // When the client sends a request out to a peer, it will wait for an async response from that peer. When that
  // response comes back, it will be combined with a pending response.
  protected final Map<Long, CompletableFuture<BtpResponse>> pendingResponses;

  // Starts life as `empty`. For a BTP plugin acting as a server, this will be populated once the WebSocket server is
  // turned on (Note that this implementation only supports a single authenticated webSocketSession). In the case of
  // a Websocket client, there will only have a single outbound connection, and thus a single session.
  protected Optional<WebSocketSession> webSocketSession = Optional.empty();

  /**
   * Required-args Constructor.
   */
  public AbstractWebsocketBtpPlugin(
      final S pluginSettings,
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
   * Handle an incoming BinaryMessage from a Websocket by converting it into a {@link BtpMessage}and forwarding it to a
   * BTP processor.
   *
   * @param webSocketSession
   * @param incomingBinaryMessage
   *
   * @return A {@link BinaryMessage} that can immediately be returned to the caller (this response will contain
   *     everything required to be eligible as a BTP response), or nothing if the response is {@link Optional#empty()}.
   */
  public Optional<BinaryMessage> onIncomingBinaryMessage(
      final WebSocketSession webSocketSession, final BinaryMessage incomingBinaryMessage
  ) {
    Objects.requireNonNull(webSocketSession);
    Objects.requireNonNull(incomingBinaryMessage);

    // TODO: Is the underlying map already synchronized?
    final BtpSession btpSession;
    synchronized (webSocketSession) {
      btpSession = (BtpSession) webSocketSession.getAttributes()
          .getOrDefault("btp-session", new BtpSession(this.getPluginSettings().getPeerAccountAddress()));
    }

    // If there's a problem deserializing the BtpPacket from the BinaryMessage, then close the connection and
    // return empty. This is one of the "tricky cases" as defined in the BTP spec where we don't want to get into
    // an infinite loop.
    final BtpPacket incomingBtpPacket;
    try {
      incomingBtpPacket = this.binaryMessageToBtpPacketConverter.convert(incomingBinaryMessage);
    } catch (BtpConversionException btpConversionException) {
      LOGGER.error("Unable to deserialize BtpPacket from incomingBinaryMessage: {}", btpConversionException);
      try {
        this.disconnect().get();
      } catch (Exception e) {
        LOGGER.error(e.getMessage(), e);
      }
      return Optional.empty();
    }

    // If incomingBtpMessage is a BTPResponse, we need to connect it to a pending sendData. If this is a
    // BtpMessage, we can simply handle it...
    return new BtpPacketHandler<Optional<BinaryMessage>>() {
      @Override
      protected Optional<BinaryMessage> handleBtpMessage(final BtpMessage incomingBtpMessage) {
        Objects.requireNonNull(incomingBtpMessage);
        LOGGER.trace("incomingBtpMessage: {}", incomingBtpMessage);

        // If incomingBtpMessage is a BtpMessage...?
        final BtpResponse btpResponse = onIncomingBtpMessage(btpSession, incomingBtpMessage);
        return Optional.of(btpPacketToBinaryMessageConverter.convert(btpResponse));
      }

      @Override
      protected Optional<BinaryMessage> handleBtpTransfer(final BtpTransfer incomingBtpTransfer) {
        Objects.requireNonNull(incomingBtpTransfer);
        LOGGER.trace("incomingBtpMessage: {}", incomingBtpTransfer);
        throw new RuntimeException("Not yet implemented!");
      }

      @Override
      protected Optional<BinaryMessage> handleBtpError(BtpError incomingBtpError) {
        Objects.requireNonNull(incomingBtpError);

        LOGGER.error("Incoming BtpError from `{}` with message `{}`",
            btpSession.getPeerAccountAddress(),
            new String(incomingBtpError.getErrorData())
        );

        // The incoming message was a BtpError, so don't return a response to the peer.
        return Optional.empty();
      }

      @Override
      protected Optional<BinaryMessage> handleBtpResponse(final BtpResponse incomingBtpResponse) {
        Objects.requireNonNull(incomingBtpResponse);

        LOGGER.trace("IncomingBtpResponse: {} ", incomingBtpResponse);

        // Generally, BTP always returns a response to the caller, even under error conditions. There are two
        // exceptions, however, listed as "tricky cases" in the BTP specification:
        //
        // 1. An unexpected BTP packet is received
        // 2. An unreadable BTP packet is received
        //
        // If the packet was unreadable, then this method will have never been called, so we can ignore this
        // case here. However, if an unexpected packet is encountered, we need to emit this error, but then return
        // null to the caller of this method so that no response is returned to the BTP peer.

        final CompletableFuture<BtpResponse> pendingResponse = pendingResponses.get(incomingBtpResponse.getRequestId());

        // If there's a pending response, then connect the incoming response to the pending response. If there is no
        // pending response, it means that

        if (pendingResponse == null) {

          //Should we call onIncomingBtpResponse here?

          //LOGGER.error("No PendingResponse available to connect to incomingBtpResponse: {}", incomingBtpResponse);
          return Optional.empty();
        } else {
          try {
            // The pendingResponse has been previously returned to a caller, who is waiting for it to be completed or
            // to timeout. So, if such a thing exists (getting here implies that it does exist), then we need to
            // complete the pendingResponse with the value found in `incomingBtpResponse`.

            // TODO: Try acceptEither instead (http://www.deadcoderising
            // .com/java8-writing-asynchronous-code-with-completablefuture/)

            final Object result = anyOf(pendingResponse, CompletableFuture.completedFuture(incomingBtpResponse))
                .handle((response, error) -> {
                  /////////////////
                  // The Exception case..
                  if (error != null) {
                    if (error instanceof BtpRuntimeException) {
                      final BtpRuntimeException btpRuntimeException = (BtpRuntimeException) error;
                      final BtpError btpError = constructBtpError(
                          incomingBtpPacket.getRequestId(), btpRuntimeException.getMessage(),
                          btpRuntimeException.getTriggeredAt(), btpRuntimeException.getCode()
                      );
                      return Optional.of(btpPacketToBinaryMessageConverter.convert(btpError));
                    } else {
                      // There was an error processing, so return a BtpError response back to the waiting caller.
                      final BtpError btpError = constructBtpError(
                          incomingBtpPacket.getRequestId(), error.getMessage(), Instant.now(),
                          BtpErrorCode.T00_UnreachableError
                      );
                      return Optional.of(btpPacketToBinaryMessageConverter.convert(btpError));
                    }
                  }
                  /////////////////
                  // The Happy Path...
                  else {
                    // Getting here means that there is a response to be handled, so connect it to the pendingResponse.

                    // TODO: FIXME!
                    BtpSessionCredentials btpSessionCredentials = ImmutableBtpSessionCredentials.builder()
                        .authToken(btpSession.getPeerAccountAddress().getValue()).build();
                    btpSession.setValidAuthentication(btpSessionCredentials);

                    pendingResponse.complete(incomingBtpResponse);

                    //final BtpResponse btpResponse = (BtpResponse) btpResponseToConnectAsObject;
                    //return btpPacketToBinaryMessageConverter.convert(btpResponse);
                    return (BinaryMessage) null;
                  }

                }).get();

            // Convert to BinaryMessage since anyOf uses Object...
            return Optional.ofNullable((BinaryMessage) result);

            //               .exceptionally(ex -> {
            //                  if (ex instanceof BtpRuntimeException) {
            //                    final BtpRuntimeException btpRuntimeException = (BtpRuntimeException) ex;
            //                    final BtpError btpError = constructBtpError(
            //                      incomingBtpPacket.getRequestId(), btpRuntimeException.getMessage(),
            //                      btpRuntimeException.getTriggeredAt(), btpRuntimeException.getCode()
            //                    );
            //                    return Optional.of(btpPacketToBinaryMessageConverter.convert(btpError));
            //                  } else {
            //                    // There was an error processing, so return a BtpError response back to the waiting caller.
            //                    final BtpError btpError = constructBtpError(
            //                      incomingBtpPacket.getRequestId(), ex.getMessage(), Instant.now(),
            //                      BtpErrorCode.T00_UnreachableError
            //                    );
            //                    return Optional.of(btpPacketToBinaryMessageConverter.convert(btpError));
            //                  }
            //                })
            //                .thenApply((btpResponseToConnectAsObject) -> {
            //                  // Getting here means that
            //
            //                  // Client: Create a BTPSession (maybe handle in the registry?)
            //                  BtpSessionCredentials btpSessionCredentials = ImmutableBtpSessionCredentials.builder()
            //                    .name(btpSession.getPeerAccountAddress().getValue()).build();
            //                  btpSession.setValidAuthentication(btpSessionCredentials);
            //
            //                  //final BtpResponse btpResponse = (BtpResponse) btpResponseToConnectAsObject;
            //                  //return btpPacketToBinaryMessageConverter.convert(btpResponse);
            //                  return (BinaryMessage) null;
            //                })
            //                .get()
            // );

          } catch (CompletionException | InterruptedException | ExecutionException e) {
            if (e.getCause() instanceof BtpRuntimeException) {
              throw (BtpRuntimeException) e.getCause();
            } else {
              throw new RuntimeException(e);
            }
          }
        }
      }
    }.handle(incomingBtpPacket);
  }

  /**
   * Finish the {@link #sendData(InterledgerPreparePacket)} operation by using the {@link BtpSession} riding on top of a
   * {@link WebSocketSession}. This method is typically called by the plugin in response to .
   *
   * @param btpPacket A {@link BtpPacket} that should be transmitted to the remote peer via BTP.
   *
   * @return A {@link CompletableFuture} that eventually yields either a {@link BtpResponse} or a {@link
   *     BtpRuntimeException}.
   */
  @Override
  protected CompletableFuture<BtpResponse> doSendDataOverBtp(final BtpPacket btpPacket) {
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
  protected CompletableFuture<BtpResponse> sendMessageWithPendingRepsonse(final long requestId,
      final WebSocketMessage webSocketMessage) {
    Objects.requireNonNull(webSocketMessage);

    return webSocketSession
        .map(session -> {
          try {
            // TODO: Check for "isConnected"?
            session.sendMessage(webSocketMessage);
            return registerPendingResponse(requestId);
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
   * │          │             Reponse (Uncompleted             │          │
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
  protected final CompletableFuture<BtpResponse> registerPendingResponse(final long requestId) {

    final CompletableFuture<BtpResponse> pendingResponse = CompletableFuture.supplyAsync(
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
