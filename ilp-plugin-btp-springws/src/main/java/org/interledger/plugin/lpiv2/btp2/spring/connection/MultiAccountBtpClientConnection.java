package org.interledger.plugin.lpiv2.btp2.spring.connection;

import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.lpiv2.btp2.spring.connection.mux.ClientBtpWebsocketMux;

/**
 * A {@link AbstractSingleAccountBtpConnection} that uses a Websocket Client to communicate with a bilateral peer.
 *
 * @deprecated This class will likely be removed in a future release. It's unclear if this implementation is required.
 *     In general, to support multiple client connections, a Connector should consider simply creating new instances of
 *     a AbstractSingleAccountBtpConnection.
 */
@Deprecated
public final class MultiAccountBtpClientConnection extends AbstractMultiAccountBtpConnection<ClientBtpWebsocketMux> {

  /**
   * Required-args Constructor.
   *
   * @param operatorAddress    The {@link InterledgerAddress} of the operator of this connection.
   * @param serverWebsocketMux An {@link ClientBtpWebsocketMux} that provides both sender and receiver functionality
   *                           using a WebSocket client.
   */
  public MultiAccountBtpClientConnection(
      final InterledgerAddress operatorAddress,
      final ClientBtpWebsocketMux serverWebsocketMux
  ) {
    super(operatorAddress, serverWebsocketMux);
  }
}
