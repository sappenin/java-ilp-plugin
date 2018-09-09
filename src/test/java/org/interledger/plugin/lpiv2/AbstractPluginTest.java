package org.interledger.plugin.lpiv2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.interledger.plugin.lpiv2.Plugin.CONNECTED;
import static org.interledger.plugin.lpiv2.Plugin.NOT_CONNECTED;
import static org.interledger.plugin.lpiv2.TestHelpers.LOCAL_NODE_ADDRESS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.interledger.core.InterledgerFulfillPacket;
import org.interledger.core.InterledgerPreparePacket;
import org.interledger.core.InterledgerProtocolException;
import org.interledger.plugin.lpiv2.TestHelpers.ExtendedPluginSettings;
import org.interledger.plugin.lpiv2.events.PluginEventHandler;

import ch.qos.logback.classic.Level;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

/**
 * An abstract class that provides a common test functionality for any plugins defined in this project.
 */
public class AbstractPluginTest {

  @Mock
  protected PluginEventHandler pluginEventHandlerMock;

  protected AbstractPlugin<ExtendedPluginSettings> abstractPlugin;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    // Enable debug mode...
    ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.DEBUG);

    final ExtendedPluginSettings extendedPluginSettings = TestHelpers.newPluginSettings();
    this.abstractPlugin = new AbstractPlugin<ExtendedPluginSettings>(extendedPluginSettings) {

      @Override
      public CompletableFuture<Void> handleIncomingSettle(BigInteger amount) {
        return null;
      }

      @Override
      protected void doHandleIncomingSettle(BigInteger amount) {

      }

      @Override
      public void doConnect() {

      }

      @Override
      public void doDisconnect() {

      }

      @Override
      protected void doSettle(BigInteger amount) {

      }

      @Override
      public InterledgerFulfillPacket doSendPacket(InterledgerPreparePacket preparePacket)
          throws InterledgerProtocolException {
        return null;
      }

      @Override
      public InterledgerFulfillPacket doHandleIncomingPacket(InterledgerPreparePacket preparePacket)
          throws InterledgerProtocolException {
        return null;
      }

    };
    this.abstractPlugin.addPluginEventHandler(pluginEventHandlerMock);
  }

  @Test
  public void testGetConnectorAccount_Disconnected() {
    this.abstractPlugin.disconnect();
    assertThat(abstractPlugin.isConnected(), is(NOT_CONNECTED));
    verify(pluginEventHandlerMock).onDisconnect(any());
    verifyNoMoreInteractions(pluginEventHandlerMock);
  }

  @Test
  public void testGetConnectorAccount_Connected() {
    this.abstractPlugin.connect();
    verify(pluginEventHandlerMock).onConnect(any());
    assertThat(abstractPlugin.isConnected(), is(CONNECTED));
    assertThat(this.abstractPlugin.getPluginSettings().localNodeAddress(), is(LOCAL_NODE_ADDRESS));
    verifyNoMoreInteractions(pluginEventHandlerMock);
  }

  @Test
  public void testConnect() {
    // The test connects the plugin to the ledger by default, so disconnect first.
    this.abstractPlugin.disconnect();
    Mockito.reset(pluginEventHandlerMock);

    this.abstractPlugin.connect();
    assertThat(abstractPlugin.isConnected(), is(CONNECTED));
    verify(pluginEventHandlerMock).onConnect(any());

    verify(pluginEventHandlerMock).onConnect(any());
    verifyNoMoreInteractions(pluginEventHandlerMock);
  }

  @Test
  public void testDisconnect() {
    // The test connects the plugin by default, so no need to call it again here...
    this.abstractPlugin.disconnect();
    assertThat(abstractPlugin.isConnected(), is(NOT_CONNECTED));

    verify(pluginEventHandlerMock).onDisconnect(any());
    verifyNoMoreInteractions(pluginEventHandlerMock);
  }

}