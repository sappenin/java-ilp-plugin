package org.interledger.plugin.connections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.interledger.plugin.BilateralReceiver;
import org.interledger.plugin.BilateralSender;
import org.interledger.plugin.connections.loopback.LoopbackConnection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CompletableFuture;

/**
 * Unit tests for {@link LoopbackConnection} that is operating two bilateral sender/receivers, one representing a USD
 * account and another representing an EUR account.
 */
public class LoopbackConnectionWithMockedMuxesTest extends AbstractConnectionTestHelper {

  @Mock
  BilateralSender usdBilateralSenderMock;

  @Mock
  BilateralSender eurBilateralSenderMock;

  @Mock
  BilateralReceiver usdBilateralReceiverMock;

  @Mock
  BilateralReceiver eurBilateralReceiverMock;

  private LoopbackConnection connection;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    connection = new LoopbackConnection(OPERATOR_ADDRESS);

    when(usdBilateralSenderMock.connect()).thenReturn(CompletableFuture.completedFuture(null));
    when(usdBilateralSenderMock.disconnect()).thenReturn(CompletableFuture.completedFuture(null));
    connection.getBilateralSenderMux().registerBilateralSender(USD_ACCOUNT_ADDRESS, usdBilateralSenderMock);

    when(eurBilateralSenderMock.connect()).thenReturn(CompletableFuture.completedFuture(null));
    when(eurBilateralSenderMock.disconnect()).thenReturn(CompletableFuture.completedFuture(null));
    connection.getBilateralSenderMux().registerBilateralSender(EUR_ACCOUNT_ADDRESS, eurBilateralSenderMock);

    when(usdBilateralReceiverMock.connect()).thenReturn(CompletableFuture.completedFuture(null));
    when(usdBilateralReceiverMock.disconnect()).thenReturn(CompletableFuture.completedFuture(null));
    connection.getBilateralReceiverMux().registerBilateralReceiver(USD_ACCOUNT_ADDRESS, usdBilateralReceiverMock);

    when(eurBilateralReceiverMock.connect()).thenReturn(CompletableFuture.completedFuture(null));
    when(eurBilateralReceiverMock.disconnect()).thenReturn(CompletableFuture.completedFuture(null));
    connection.getBilateralReceiverMux().registerBilateralReceiver(EUR_ACCOUNT_ADDRESS, eurBilateralReceiverMock);
  }

  //////////////////
  // Connection Tests
  //////////////////

  @Test
  public void testConnection_disconnectWhenDisconnected() {
    connection.disconnect();
    // No call to disconnect on any plugins because the Mux was never connected...
    verifyNoMoreInteractions(usdBilateralSenderMock);
    verifyNoMoreInteractions(eurBilateralSenderMock);
    verifyNoMoreInteractions(usdBilateralReceiverMock);
    verifyNoMoreInteractions(eurBilateralReceiverMock);
  }

  @Test
  public void testConnection_disconnectTwice() {
    // Calling disconnect twice should have no effect.
    connection.disconnect();
    connection.disconnect();
    connection.disconnect();
    verifyNoMoreInteractions(usdBilateralSenderMock);
    verifyNoMoreInteractions(eurBilateralSenderMock);
    verifyNoMoreInteractions(usdBilateralReceiverMock);
    verifyNoMoreInteractions(eurBilateralReceiverMock);
  }


  @Test
  public void testConnection_connectWhenDisconnected() {
    // Connect when disconnected.
    connection.disconnect();
    connection.connect();
    verify(usdBilateralSenderMock).connect();
    verify(eurBilateralSenderMock).connect();
    verify(usdBilateralReceiverMock).connect();
    verify(eurBilateralReceiverMock).connect();
    verifyNoMoreInteractions(usdBilateralSenderMock);
    verifyNoMoreInteractions(eurBilateralSenderMock);
    verifyNoMoreInteractions(usdBilateralReceiverMock);
    verifyNoMoreInteractions(eurBilateralReceiverMock);
  }

  @Test
  public void testConnection_connectWhenConnected() {
    // Connect when already connected.
    connection.connect();
    connection.connect();
    connection.connect();
    verify(usdBilateralSenderMock).connect();
    verify(eurBilateralSenderMock).connect();
    verify(usdBilateralReceiverMock).connect();
    verify(eurBilateralReceiverMock).connect();
    verifyNoMoreInteractions(usdBilateralSenderMock);
    verifyNoMoreInteractions(eurBilateralSenderMock);
    verifyNoMoreInteractions(usdBilateralReceiverMock);
    verifyNoMoreInteractions(eurBilateralReceiverMock);
  }

  @Test
  public void testConnection_disconnectWhenConnected() {
    // Connect when already connected.
    connection.connect();
    connection.disconnect();
    //connection.disconnect();
    verify(usdBilateralSenderMock).connect();
    verify(eurBilateralSenderMock).connect();
    verify(usdBilateralReceiverMock).connect();
    verify(eurBilateralReceiverMock).connect();
    verify(usdBilateralSenderMock).disconnect();
    verify(eurBilateralSenderMock).disconnect();
    verify(usdBilateralReceiverMock).disconnect();
    verify(eurBilateralReceiverMock).disconnect();
    verifyNoMoreInteractions(usdBilateralSenderMock);
    verifyNoMoreInteractions(eurBilateralSenderMock);
    verifyNoMoreInteractions(usdBilateralReceiverMock);
    verifyNoMoreInteractions(eurBilateralReceiverMock);
  }
}