package org.interledger.plugin.lpiv2.btp2.spring.connection;

import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.lpiv2.btp2.spring.connection.mux.ClientBtpWebsocketMux;

/**
 * A {@link AbstractSingleAccountBtpConnection} that uses a Websocket Client to communicate with a bilateral peer.
 */
public class SingleAccountBtpClientConnection extends AbstractSingleAccountBtpConnection<ClientBtpWebsocketMux> {

  /**
   * Required-args Constructor.
   *
   * @param operatorAddress       The {@link InterledgerAddress} of the operator of this connection.
   * @param accountAddress        The {@link InterledgerAddress} of the account that this BTP connection supports.
   * @param clientBtpWebsocketMux An {@link ClientBtpWebsocketMux} that provides both sender and receiver functionality
   *                              using a WebSocket server.
   */
  public SingleAccountBtpClientConnection(
      final InterledgerAddress operatorAddress,
      final InterledgerAddress accountAddress,
      final ClientBtpWebsocketMux clientBtpWebsocketMux
  ) {
    super(operatorAddress, accountAddress, clientBtpWebsocketMux);
  }

}
