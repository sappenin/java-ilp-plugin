package org.interledger.plugin.lpiv2.btp2.spring;

import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.link.mux.AbstractPluginMux;
import org.interledger.plugin.lpiv2.btp2.BtpServerPluginSettings;

public class BtpPluginMux extends AbstractPluginMux<AbstractWebsocketBtpPlugin<? extends BtpServerPluginSettings>> {

  /**
   * Required-args Constructor.
   *
   * @param operatorAddress The {@link InterledgerAddress} of the node operating this MUX.
   */
  public BtpPluginMux(InterledgerAddress operatorAddress) {
    super(operatorAddress);
  }

}
