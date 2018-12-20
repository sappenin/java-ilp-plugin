package org.interledger.plugin.connections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.interledger.plugin.lpiv2.LoopbackPlugin.LOOPBACK_ADDRESS;

import org.interledger.plugin.connections.loopback.LoopbackConnection;
import org.interledger.plugin.lpiv2.LoopbackPlugin;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for {@link LoopbackConnection} that is operating two bilateral sender/receivers, one representing a USD
 * account and another representing an EUR account.
 */
public class LoopbackConnectionTest extends AbstractConnectionTestHelper {


  private LoopbackPlugin loopbackPlugin;

  private LoopbackConnection connection;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    loopbackPlugin = new LoopbackPlugin(OPERATOR_ADDRESS);
    connection = new LoopbackConnection(OPERATOR_ADDRESS);

    connection.getBilateralSenderMux().registerBilateralSender(LOOPBACK_ADDRESS, loopbackPlugin);
    connection.getBilateralReceiverMux().registerBilateralReceiver(LOOPBACK_ADDRESS, loopbackPlugin);
  }

  @Test
  public void testConnectAndDisconnect() {
    for (int i = 0; i < 10; i++) {
      connection.disconnect().join();
      assertThat(connection.getBilateralSenderMux().isConnected(), is(false));
      assertThat(connection.getBilateralReceiverMux().isConnected(), is(false));
      connection.disconnect().join();
      assertThat(connection.getBilateralSenderMux().isConnected(), is(false));
      assertThat(connection.getBilateralReceiverMux().isConnected(), is(false));

      connection.connect().join();
      assertThat(connection.getBilateralSenderMux().isConnected(), is(true));
      assertThat(connection.getBilateralReceiverMux().isConnected(), is(true));
      connection.connect().join();
      assertThat(connection.getBilateralSenderMux().isConnected(), is(true));
      assertThat(connection.getBilateralReceiverMux().isConnected(), is(true));

      connection.disconnect().join();
      assertThat(connection.getBilateralSenderMux().isConnected(), is(false));
      assertThat(connection.getBilateralReceiverMux().isConnected(), is(false));
      connection.disconnect().join();
      assertThat(connection.getBilateralSenderMux().isConnected(), is(false));
      assertThat(connection.getBilateralReceiverMux().isConnected(), is(false));
    }
  }

  @Test
  public void testGetSenderMux() {
    assertThat(connection.getBilateralSenderMux().getBilateralSender(LOOPBACK_ADDRESS).get(), is(loopbackPlugin));
  }

  @Test
  public void testGetReceiverMux() {
    assertThat(connection.getBilateralReceiverMux().getBilateralReceiver(LOOPBACK_ADDRESS).get(), is(loopbackPlugin));
  }

}