package org.interledger.plugin.lpiv2.btp2.spring.connection;

import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.connections.AbstractBilateralConnection;
import org.interledger.plugin.connections.BilateralConnection;
import org.interledger.plugin.connections.mux.AbstractBilateralComboMux;

/**
 * A {@link BilateralConnection} that uses enhanced BTP to support multiple accounts per Websocket connection (i.e., the
 * `auth_username` is used to identify the account of an incoming connection, and  the `auth_token` is used for
 * authentication).
 */
public abstract class AbstractMultiAccountBtpConnection<T extends AbstractBilateralComboMux>
    extends AbstractBilateralConnection<T, T> {

  /**
   * Required-args Constructor.
   *
   * @param operatorAddress The {@link InterledgerAddress} of the operator of this connection.
   * @param comboMux        An {@link AbstractBilateralComboMux} that provides both sender and receiver functionality.
   */
  public AbstractMultiAccountBtpConnection(
      final InterledgerAddress operatorAddress,
      final T comboMux
  ) {
    super(operatorAddress, comboMux, comboMux);
  }

  /**
   * For a BTP connections, we know the sender and receiver will be the same instance (i.e., a {@link
   * AbstractBilateralComboMux}, so we can return either the sender or receiver to conform to this interface.
   *
   * @return
   */
  T getComboMux() {
    return this.getBilateralSenderMux();
  }
}
