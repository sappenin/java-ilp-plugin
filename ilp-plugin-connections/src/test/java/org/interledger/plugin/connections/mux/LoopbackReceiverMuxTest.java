package org.interledger.plugin.connections.mux;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.interledger.plugin.lpiv2.LoopbackPlugin.LOOPBACK_ADDRESS;

import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.lpiv2.LoopbackPlugin;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for {@link LoopbackReceiverMux}.
 */
public class LoopbackReceiverMuxTest {

  private LoopbackPlugin loopbackPlugin;
  private LoopbackReceiverMux loopbackReceiverMux;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    this.loopbackPlugin = new LoopbackPlugin(InterledgerAddress.of("test.node"));
    this.loopbackReceiverMux = new LoopbackReceiverMux();
    loopbackReceiverMux.registerBilateralReceiver(LOOPBACK_ADDRESS, loopbackPlugin);
  }

  @Test
  public void doConnectTransport() {
    assertThat(loopbackReceiverMux.doConnectTransport().join(), is(nullValue()));
  }

  @Test
  public void doDisconnectTransport() {
    assertThat(loopbackReceiverMux.doDisconnectTransport().join(), is(nullValue()));
  }

}