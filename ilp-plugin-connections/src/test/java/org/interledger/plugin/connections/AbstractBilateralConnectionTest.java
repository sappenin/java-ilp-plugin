package org.interledger.plugin.connections;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.when;

import org.interledger.plugin.connections.mux.BilateralReceiverMux;
import org.interledger.plugin.connections.mux.BilateralSenderMux;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CompletableFuture;

/**
 * Unit tests for {@link AbstractBilateralConnection} where all dependencies are mocked.
 */
public class AbstractBilateralConnectionTest extends AbstractConnectionTestHelper {

  @Mock
  private BilateralSenderMux senderMuxMock;

  @Mock
  private BilateralReceiverMux receiverMuxMock;

  private AbstractBilateralConnection abstractBilateralConnection;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    when(senderMuxMock.connect()).thenReturn(CompletableFuture.completedFuture(null));
    when(senderMuxMock.disconnect()).thenReturn(CompletableFuture.completedFuture(null));

    when(receiverMuxMock.connect()).thenReturn(CompletableFuture.completedFuture(null));
    when(receiverMuxMock.disconnect()).thenReturn(CompletableFuture.completedFuture(null));

    this.abstractBilateralConnection = new AbstractBilateralConnection(
        OPERATOR_ADDRESS, senderMuxMock, receiverMuxMock
    ) {
    };
  }

  @Test
  public void getOperatorAddress() {
    assertThat(abstractBilateralConnection.getOperatorAddress(), is(OPERATOR_ADDRESS));
  }

//  @Test
//  public void connect() {
//    assertThat(abstractBilateralConnection.isConnected(), is(false));
//    abstractBilateralConnection.connect();
//    assertThat(abstractBilateralConnection.isConnected(), is(true));
//  }
//
//  @Test
//  public void close() {
//    assertThat(abstractBilateralConnection.isConnected(), is(false));
//    abstractBilateralConnection.connect();
//    assertThat(abstractBilateralConnection.isConnected(), is(true));
//    abstractBilateralConnection.close();
//    assertThat(abstractBilateralConnection.isConnected(), is(false));
//  }
//
//  @Test
//  public void disconnect() {
//    assertThat(abstractBilateralConnection.isConnected(), is(false));
//    abstractBilateralConnection.connect();
//    assertThat(abstractBilateralConnection.isConnected(), is(true));
//    abstractBilateralConnection.disconnect();
//    assertThat(abstractBilateralConnection.isConnected(), is(false));
//  }
//
//  @Test
//  public void isConnected() {
//    assertThat(abstractBilateralConnection.isConnected(), is(false));
//    abstractBilateralConnection.connect();
//    assertThat(abstractBilateralConnection.isConnected(), is(true));
//    abstractBilateralConnection.disconnect();
//    assertThat(abstractBilateralConnection.isConnected(), is(false));
//  }

  @Test
  public void getBilateralReceiverMux_Connected() {
    abstractBilateralConnection.connect();
    assertThat(abstractBilateralConnection.getBilateralReceiverMux(), is(receiverMuxMock));
  }

  @Test
  public void getBilateralReceiverMux_Disconnected() {
    abstractBilateralConnection.disconnect();
    assertThat(abstractBilateralConnection.getBilateralReceiverMux(), is(not(nullValue())));
  }

  @Test
  public void getBilateralSenderMux_Connected() {
    abstractBilateralConnection.connect();
    assertThat(abstractBilateralConnection.getBilateralReceiverMux(), is(receiverMuxMock));
  }

  @Test
  public void getBilateralSenderMux_Disconnected() {
    abstractBilateralConnection.disconnect();
    assertThat(abstractBilateralConnection.getBilateralSenderMux(), is(not(nullValue())));
  }

}