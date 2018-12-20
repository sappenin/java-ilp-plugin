package org.interledger.plugin.lpiv2.btp2.spring.connection;

import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.connections.AbstractBilateralConnection;
import org.interledger.plugin.connections.BilateralConnection;
import org.interledger.plugin.connections.mux.AbstractBilateralComboMux;

import java.util.Objects;

/**
 * A {@link BilateralConnection} that uses vanilla BTP specified in IL-RFC-23, meaning that this implementation only
 * supports a single account per Websocket connection (i.e., only the `auth_token` is considered when accepting an
 * incoming connection, thus limiting this implementation to a single Account).
 */
public abstract class AbstractSingleAccountBtpConnection<T extends AbstractBilateralComboMux>
    extends AbstractBilateralConnection<T, T> {

  // TODO: Transition to BilateralConnectionSettings?
  private final InterledgerAddress accountAddress;

  /**
   * Required-args Constructor.
   *
   * @param operatorAddress The {@link InterledgerAddress} of the operator of this connection.
   * @param accountAddress  The {@link InterledgerAddress} of the account that this BTP connection supports.
   * @param comboMux        An {@link AbstractBilateralComboMux} that provides both sender and receiver functionality.
   */
  public AbstractSingleAccountBtpConnection(
      final InterledgerAddress operatorAddress,
      final InterledgerAddress accountAddress,
      final T comboMux
  ) {
    super(operatorAddress, comboMux, comboMux);
    this.accountAddress = Objects.requireNonNull(accountAddress);
  }

  public InterledgerAddress getAccountAddress() {
    return accountAddress;
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
