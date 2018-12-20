package org.interledger.plugin.lpiv2.btp2.spring.connection;

import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.lpiv2.btp2.spring.connection.mux.ServerBtpWebsocketMux;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * <p>A {@link AbstractSingleAccountBtpConnection} that uses a Websocket server to communicate with a bilateral peer, allowing
 * only a single plugin account to operate over the connection. Because this is a BTP connection, we use a combo-mux,
 * which means the sender and receiver Muxes are the same instance.</p>
 *
 * <p>Even though this class implements {@link WebSocketHandler}, all messages are forwarded to the MUX, which operates
 * the entire network transport.</p>
 */
public class SingleAccountBtpServerConnection extends AbstractSingleAccountBtpConnection<ServerBtpWebsocketMux>
    implements WebSocketHandler {

  /**
   * Required-args Constructor.
   *
   * @param operatorAddress       The {@link InterledgerAddress} of the operator of this connection.
   * @param accountAddress        The {@link InterledgerAddress} of the account that this BTP connection supports.
   * @param serverBtpWebsocketMux An {@link ServerBtpWebsocketMux} that provides both sender and receiver functionality
   *                              using a WebSocket client.
   */
  public SingleAccountBtpServerConnection(
      final InterledgerAddress operatorAddress,
      final InterledgerAddress accountAddress,
      final ServerBtpWebsocketMux serverBtpWebsocketMux
  ) {
    super(operatorAddress, accountAddress, serverBtpWebsocketMux);
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
}
