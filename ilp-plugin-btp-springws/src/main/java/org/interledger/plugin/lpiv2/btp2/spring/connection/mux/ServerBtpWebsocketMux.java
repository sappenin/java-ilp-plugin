package org.interledger.plugin.lpiv2.btp2.spring.connection.mux;

import org.interledger.btp.BtpSession;
import org.interledger.plugin.BilateralReceiver;
import org.interledger.plugin.connections.mux.AbstractBilateralComboMux;
import org.interledger.plugin.lpiv2.btp2.spring.BtpSessionUtils;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BinaryMessageToBtpPacketConverter;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BtpPacketToBinaryMessageConverter;
import org.interledger.plugin.lpiv2.btp2.subprotocols.BtpSubProtocolHandlerRegistry;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>An extension of {@link AbstractBilateralComboMux} that accepts incoming Websocket connections from various hosts.
 * Once authenticated, each host can connect to a plugin for actual handling.</p>
 */
public class ServerBtpWebsocketMux extends AbstractBtpWebsocketComboMux implements WebSocketHandler {

  public ServerBtpWebsocketMux(
      final BinaryMessageToBtpPacketConverter binaryMessageToBtpPacketConverter,
      final BtpPacketToBinaryMessageConverter btpPacketToBinaryMessageConverter,
      final BtpSubProtocolHandlerRegistry btpSubProtocolHandlerRegistry
  ) {
    super(binaryMessageToBtpPacketConverter, btpPacketToBinaryMessageConverter, btpSubProtocolHandlerRegistry);
  }

  @Override
  public CompletableFuture<Void> doConnectTransport() {
    // This is a no-op. All connection logic is in AbstractBtpWebsocketComboMux.
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> doDisconnectTransport() {
    // This is a no-op. All connection logic is in AbstractBtpWebsocketComboMux.
    return CompletableFuture.completedFuture(null);
  }

  ///////////////////
  // WebSocketHandler
  ///////////////////

  /**
   * Invoked after WebSocket negotiation has succeeded and the WebSocket connection is opened and ready for use.
   *
   * @param session
   *
   * @throws Exception this method can handle or propagate exceptions; see class-level Javadoc for details.
   */
  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    logger.debug("Incoming WS Client Connection Established: {}", session);

    // Add a new BtpSession into the WebsocketSession...it will be initialized after the `auth` sub_protocol is
    // executed.
    BtpSessionUtils.setBtpSessionIntoWebsocketSession(session, new BtpSession(session.getId()));
  }

  /**
   * Invoked when a new incoming WebSocket message arrives.
   *
   * @param session
   * @param message
   *
   * @throws Exception this method can handle or propagate exceptions; see class-level Javadoc for details.
   */
  @Override
  public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
    if (message instanceof BinaryMessage) {
      this.handleBinaryMessage(session, (BinaryMessage) message);
    } else {
      throw new IllegalStateException("Unexpected WebSocket message type: " + message);
    }
  }

  /**
   * Handle an error from the underlying WebSocket message transport.
   *
   * @param session
   * @param exception
   *
   * @throws Exception this method can handle or propagate exceptions; see class-level Javadoc for details.
   */
  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) {
    // Don't premptively close any connections. It's possible this is just a transient error, and we can wait until the
    // actual connection closes to disconnect this MUX.
    logger.error(exception.getMessage(), exception);
  }

  /**
   * Invoked after the WebSocket connection has been closed by either side, or after a transport error has occurred.
   * Although the session may technically still be open, depending on the underlying implementation, sending messages at
   * this point is discouraged and most likely will not succeed.
   *
   * @param session
   * @param closeStatus
   *
   * @throws Exception this method can handle or propagate exceptions; see class-level Javadoc for details.
   */
  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
    logger.debug("WS Client Connection Closed: {}", session);

    // BilateralReceivers (typically plugins) aren't aware that they're being MUX'd, so this MUX must disconnect each
    // one.
    this.disconnect();


//    final CompletableFuture[] disconnectFutures = this.getBilateralReceivers().values().stream()
//        .map(BilateralReceiver::disconnect)
//        .collect(Collectors.toList())
//        .toArray(new CompletableFuture[0]);

//    // Wait up to 30 seconds for all disconnects to occur...
//    try {
//      CompletableFuture.allOf(disconnectFutures).get(30, TimeUnit.SECONDS);
//    } catch (Exception e) {
//      throw new RuntimeException(e.getMessage(), e);
//    }
  }

  @Override
  public boolean supportsPartialMessages() {
    return false;
  }


}