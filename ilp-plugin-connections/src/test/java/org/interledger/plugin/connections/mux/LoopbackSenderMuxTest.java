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
 * Unit tests for {@link LoopbackSenderMux}.
 */
public class LoopbackSenderMuxTest {

  private LoopbackPlugin loopbackPlugin;
  private LoopbackSenderMux loopbackSenderMux;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    this.loopbackPlugin = new LoopbackPlugin(InterledgerAddress.of("test.node"));
    this.loopbackSenderMux = new LoopbackSenderMux();
    loopbackSenderMux.registerBilateralSender(LOOPBACK_ADDRESS, loopbackPlugin);
  }

  @Test
  public void doConnectTransport() {
    assertThat(loopbackSenderMux.doConnectTransport().join(), is(nullValue()));
  }

  @Test
  public void doDisconnectTransport() {
    assertThat(loopbackSenderMux.doDisconnectTransport().join(), is(nullValue()));
  }
}