package org.interledger.plugin.connections.loopback;

import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.connections.AbstractBilateralConnection;
import org.interledger.plugin.connections.BilateralConnection;
import org.interledger.plugin.connections.BilateralConnectionType;
import org.interledger.plugin.connections.mux.BilateralReceiverMux;
import org.interledger.plugin.connections.mux.BilateralSenderMux;
import org.interledger.plugin.connections.mux.LoopbackReceiverMux;
import org.interledger.plugin.connections.mux.LoopbackSenderMux;

/**
 * An implementation of {@link BilateralConnection} that provides loopback functionality, meaning it always returns a
 * response as-if the connection were connected..
 */
public class LoopbackConnection
    extends AbstractBilateralConnection<BilateralSenderMux, BilateralReceiverMux>
    implements BilateralConnection<BilateralSenderMux, BilateralReceiverMux> {

  public static final String CONNECTION_TYPE_STRING = "LoopbackConnection";
  public static final BilateralConnectionType CONNECTION_TYPE = BilateralConnectionType.of(CONNECTION_TYPE_STRING);

  /**
   * Required-args Constructor.
   */
  public LoopbackConnection(final InterledgerAddress operatorAddress) {
    super(operatorAddress, new LoopbackSenderMux(), new LoopbackReceiverMux());
  }

}