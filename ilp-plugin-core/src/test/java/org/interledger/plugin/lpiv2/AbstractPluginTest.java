package org.interledger.plugin.lpiv2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.interledger.plugin.lpiv2.Plugin.CONNECTED;
import static org.interledger.plugin.lpiv2.Plugin.NOT_CONNECTED;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.interledger.core.InterledgerPreparePacket;
import org.interledger.core.InterledgerProtocolException;
import org.interledger.core.InterledgerResponsePacket;
import org.interledger.plugin.lpiv2.TestHelpers.ExtendedPluginSettings;
import org.interledger.plugin.lpiv2.events.PluginEventListener;

import ch.qos.logback.classic.Level;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * An abstract class that provides a common test functionality for any plugins defined in this project.
 */
public class AbstractPluginTest {

  @Mock
  protected PluginEventListener pluginEventListenerMock;

  protected AbstractPlugin<ExtendedPluginSettings> abstractPlugin;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    // Enable debug mode...
    ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.DEBUG);

    final ExtendedPluginSettings extendedPluginSettings = TestHelpers.newPluginSettings();
    this.abstractPlugin = new AbstractPlugin<ExtendedPluginSettings>(extendedPluginSettings) {

      @Override
      public CompletableFuture<Void> doConnect() {
        return CompletableFuture.completedFuture(null);
      }

      @Override
      public CompletableFuture<Void> doDisconnect() {
        return CompletableFuture.completedFuture(null);
      }
    };

    abstractPlugin.registerDataSender((preparePacket) -> CompletableFuture.completedFuture(Optional.empty()));
    abstractPlugin.registerMoneySender((amount) -> CompletableFuture.completedFuture(null));
    abstractPlugin.registerDataHandler((sourcePreparePacket) ->
        CompletableFuture.supplyAsync(() -> Optional.of(TestHelpers.getSendDataFulfillPacket())
    ));
    abstractPlugin.registerMoneyHandler((amount -> CompletableFuture.supplyAsync(() -> null)));

    abstractPlugin.addPluginEventListener(UUID.randomUUID(), pluginEventListenerMock);
  }

  @Test
  public void testDisconnectAndConnect() {
    // The test connects the plugin by default, so no need to call it again here...
    this.abstractPlugin.disconnect().join();
    assertThat(abstractPlugin.isConnected(), is(NOT_CONNECTED));
    verifyZeroInteractions(pluginEventListenerMock);

    this.abstractPlugin.connect().join();
    assertThat(abstractPlugin.isConnected(), is(CONNECTED));
    verify(pluginEventListenerMock).onConnect(any());
    verifyNoMoreInteractions(pluginEventListenerMock);

    this.abstractPlugin.disconnect().join();
    assertThat(abstractPlugin.isConnected(), is(NOT_CONNECTED));
    verify(pluginEventListenerMock).onDisconnect(any());
    verifyNoMoreInteractions(pluginEventListenerMock);
  }

  // TODO: Cover Money and DataHandlers

}