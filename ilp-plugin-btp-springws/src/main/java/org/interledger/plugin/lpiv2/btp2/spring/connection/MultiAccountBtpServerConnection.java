package org.interledger.plugin.lpiv2.btp2.spring.connection;

import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.connections.BilateralConnection;
import org.interledger.plugin.lpiv2.btp2.spring.connection.mux.ServerBtpWebsocketMux;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * A {@link AbstractMultiAccountBtpConnection} that uses a Websocket Server to communicate with a bilateral peer. A
 * {@link BilateralConnection} allows for the sender and receiver to be different things entirely (e.g., BPP). However,
 * this implementation uses the same WebSocket session to both send and receive on.
 */
public final class MultiAccountBtpServerConnection extends AbstractMultiAccountBtpConnection<ServerBtpWebsocketMux>
    implements WebSocketHandler {

  /**
   * Required-args Constructor.
   *
   * @param operatorAddress       The {@link InterledgerAddress} of the operator of this connection.
   * @param serverBtpWebsocketMux An {@link ServerBtpWebsocketMux} that provides both sender and receiver
   */
  public MultiAccountBtpServerConnection(
      final InterledgerAddress operatorAddress, final ServerBtpWebsocketMux serverBtpWebsocketMux
  ) {
    super(operatorAddress, serverBtpWebsocketMux);
  }

  ///////////////////
  // WebSocketHandler
  ///////////////////

  /**
   * Forward to the combo mux, which is the actual {@link WebSocketHandler}.
   */
  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    this.getComboMux().afterConnectionEstablished(session);
  }

  /**
   * Forward to the combo mux, which is the actual {@link WebSocketHandler}.
   */
  @Override
  public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
    this.getComboMux().handleMessage(session, message);
  }

  /**
   * Forward to the combo mux, which is the actual {@link WebSocketHandler}.
   */
  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) {
    this.getComboMux().handleTransportError(session, exception);
  }

  /**
   * Forward to the combo mux, which is the actual {@link WebSocketHandler}.
   */
  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
    this.getComboMux().afterConnectionClosed(session, closeStatus);
  }

  /**
   * Forward to the combo mux, which is the actual {@link WebSocketHandler}.
   */
  @Override
  public boolean supportsPartialMessages() {
    return this.getComboMux().supportsPartialMessages();
  }

//  /**
//   * Handle an incoming {@link BinaryMessage} by translating it into BTP, forwarding to the appropriate plugin, and then
//   * returning a response on the Websocket.
//   *
//   * @param webSocketSession
//   * @param binaryMessage
//   */
//  @VisibleForTesting
//  protected void handleBinaryMessage(
//      final WebSocketSession webSocketSession, final BinaryMessage binaryMessage
//  ) {
//
//    Objects.requireNonNull(webSocketSession);
//    Objects.requireNonNull(binaryMessage);
//
//    // The first message in a WebSocketSession MUST be the auth protocol. Thus, as long as the BtpSession is not
//    // authenticated, then we should attempt to perform the auth sub_protocol.
//    final BtpSession btpSession = BtpSessionUtils.getBtpSessionFromWebSocketSession(webSocketSession);
//
//    final Optional<BinaryMessage> response;
//    if (!btpSession.isAuthenticated()) {
//      // Do Auth. This will merely initialize the credentials into the BTPSession and return an Ack.
//      response = this.onIncomingBinaryAuthMessage(webSocketSession, binaryMessage);
//    } else {
//      // The authUsername will not be present in the BtpSession until _after_ the `auth` sub_protocol has completed,
//      // so we cannot attempt to resolve a Receiver (i.e., a plugin) until the caller is authenticated.
//      response = btpSession.getBtpSessionCredentials().get()
//          .map(BtpSessionCredentials::getAuthUsername)
//          .filter(Optional::isPresent)
//          .map(Optional::get)
//          .map(InterledgerAddress::of)
//          .map(authUserName -> this.getBilateralReceiverMux().handleBinaryMessage(webSocketSession, binaryMessage))
//          .orElseThrow(() -> new RuntimeException(String
//              .format("BtpSession `%s` was not authenticated (no auth_username found in credentials)",
//                  btpSession.getWebsocketSessionId())));
//    }
//
//    // Return the response to the caller, if there is a response.
//    response.ifPresent($ -> {
//      try {
//        // TODO: What does the other side of the WebSocket see if there's an exception here?
//        webSocketSession.sendMessage($);
//      } catch (IOException e) {
//        throw new RuntimeException(e);
//      }
//    });
//  }

//  /**
//   * Handle an incoming BinaryMessage from a Websocket by assuming it's a BTP Auth message. This method is used
//   * internally to establish an authenticated BTP session, correlating that to a Websocket Session.
//   *
//   * @param webSocketSession
//   * @param incomingBinaryMessage
//   *
//   * @return A {@link BinaryMessage} that can immediately be returned to the caller (this response will contain
//   *     everything required to be eligible as a BTP response), or nothing if the response is {@link Optional#empty()}.
//   */
//  @VisibleForTesting
//  protected Optional<BinaryMessage> onIncomingBinaryAuthMessage(
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
//      final BtpSession btpSession = BtpSessionUtils.getBtpSessionFromWebSocketSession(webSocketSession);
//      return serverAuthBtpSubprotocolHandler.handleSubprotocolMessage(btpSession, incomingBtpPacket)
//          .thenApply(btpSubProtocol -> {
//            // If there's no exception, then reaching here means the btp_auth SubProtocol succeeded.
//            // Ack the response...
//            final BtpSubProtocols responses = new BtpSubProtocols();
//            btpSubProtocol.ifPresent(responses::add);
//            final BtpResponse btpResponse = BtpResponse.builder()
//                .requestId(incomingBtpPacket.getRequestId())
//                .subProtocols(responses)
//                .build();
//            final Optional<BinaryMessage> response = Optional
//                .of(btpPacketToBinaryMessageConverter.convert(btpResponse));
//
//            // If auth succeeded, there will be an Account in the BTP session. If there is no session, or if auth
//            // did not succeed, then this event will not be propagated, and nothing will happen (i.e., no plugin will
//            // be registered).
//            btpSession.getAccountAddress().ifPresent(accountAddress -> {
//              // Emit this event so any listeners can react to this account connecting on this connection...
//              this.getEventEmitter().emitEvent(
//                  ImmutableAccountConnectedEvent.builder().accountAddress(accountAddress).build()
//              );
//            });
//
//            return response;
//          })
//          .join();
//    } catch (BtpConversionException btpConversionException) {
//      logger.error(btpConversionException.getMessage(), btpConversionException);
//      throw btpConversionException;
//    }
//  }
//
//
}
