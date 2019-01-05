package org.interledger.plugin.lpiv2.btp2.spring;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.interledger.plugin.lpiv2.btp2.spring.connection.TestHelpers.FULFILLMENT;
import static org.junit.Assert.fail;

import org.interledger.btp.asn.framework.BtpCodecContextFactory;
import org.interledger.core.InterledgerAddress;
import org.interledger.core.InterledgerFulfillPacket;
import org.interledger.core.InterledgerPreparePacket;
import org.interledger.core.InterledgerRejectPacket;
import org.interledger.core.InterledgerResponsePacket;
import org.interledger.core.InterledgerResponsePacketHandler;
import org.interledger.core.asn.framework.InterledgerCodecContextFactory;
import org.interledger.plugin.MoneyHandler;
import org.interledger.plugin.lpiv2.LoopbackPlugin;
import org.interledger.plugin.lpiv2.Plugin;
import org.interledger.plugin.lpiv2.btp2.spring.BtpClientPluginTest.TestServerConfig;
import org.interledger.plugin.lpiv2.btp2.spring.connection.TestHelpers;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BinaryMessageToBtpPacketConverter;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BtpPacketToBinaryMessageConverter;
import org.interledger.plugin.lpiv2.btp2.subprotocols.BtpAuthenticator;
import org.interledger.plugin.lpiv2.btp2.subprotocols.BtpAuthenticator.AlwaysAllowedBtpAuthenticator;
import org.interledger.plugin.lpiv2.btp2.subprotocols.BtpMultiAuthenticator;
import org.interledger.plugin.lpiv2.btp2.subprotocols.BtpMultiAuthenticator.AlwaysAllowedBtpMultiAuthenticator;
import org.interledger.plugin.lpiv2.btp2.subprotocols.BtpSubProtocolHandlerRegistry;
import org.interledger.plugin.lpiv2.btp2.subprotocols.auth.ClientAuthBtpSubprotocolHandler;
import org.interledger.plugin.lpiv2.btp2.subprotocols.auth.ServerAuthBtpSubprotocolHandler;

import ch.qos.logback.classic.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests to validate {@link BtpClientPlugin} operating against a real WebSocket server that uses a {@link
 * LoopbackPlugin} for all responses.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {TestServerConfig.class})
public class BtpClientPluginTest {

  private static final InterledgerAddress CLIENT_OPERATOR_ADDRESS = InterledgerAddress.of("test.client");
  private static final InterledgerAddress SERVER_OPERATOR_ADDRESS = InterledgerAddress.of("test.server");

  // Simulate the server is authoritative for the account...
  private static final InterledgerAddress ACCOUNT_ADDRESS = SERVER_OPERATOR_ADDRESS.with("client.usd");
  private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(30);

  @Rule
  public final TestName testName = new TestName();

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  // Collects all money values...
  private final AtomicInteger balanceTracker = new AtomicInteger();

  @Rule
  public ErrorCollector collector = new ErrorCollector();

  @LocalServerPort
  private int port;

  private Plugin<BtpClientPluginSettings> btpClientPlugin;

  @Before
  public void setup() {

    // Enable debug mode...
    ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.DEBUG);

    MockitoAnnotations.initMocks(this);
    this.balanceTracker.set(0);

    logger.debug("Setting up before '" + this.testName.getMethodName() + "'");

    //////////////////////////
    // Bilateral Connection Setup
    //////////////////////////
    final BinaryMessageToBtpPacketConverter binaryMessageToBtpPacketConverter = new BinaryMessageToBtpPacketConverter(
        BtpCodecContextFactory.oer()
    );
    final BtpPacketToBinaryMessageConverter btpPacketToBinaryMessageConverter = new BtpPacketToBinaryMessageConverter(
        BtpCodecContextFactory.oer()
    );

    final ClientAuthBtpSubprotocolHandler btpAuthHandler = new ClientAuthBtpSubprotocolHandler();
    final BtpSubProtocolHandlerRegistry btpSubProtocolHandlerRegistry =
        new BtpSubProtocolHandlerRegistry(btpAuthHandler);
    final BtpClientPluginSettings pluginSettings = ImmutableBtpClientPluginSettings.builder()
        .accountAddress(ACCOUNT_ADDRESS)
        .operatorAddress(CLIENT_OPERATOR_ADDRESS)
        .authUsername(Optional.of("anything")) // The No-Op Authenticator doesn't care for this test.
        .secret("shh")
        .remotePeerScheme("ws")
        .remotePeerHostname("localhost")
        .remotePeerPort(port + "")
        .build();

    this.btpClientPlugin = new BtpClientPlugin(
        pluginSettings,
        InterledgerCodecContextFactory.oer(),
        binaryMessageToBtpPacketConverter,
        btpPacketToBinaryMessageConverter,
        btpSubProtocolHandlerRegistry,
        new StandardWebSocketClient()
    ) {
      @Override
      public void doSendMoney(BigInteger amount) {
        balanceTracker.getAndAdd((amount.intValue() * -1));
      }
    };

    // DATAHANDLER: When the plugin receives Data, it should process the incoming message using Loopback functionality.
    final LoopbackPlugin loopbackPlugin = new LoopbackPlugin(SERVER_OPERATOR_ADDRESS);
    btpClientPlugin.registerDataHandler(loopbackPlugin.getDataHandler().get());

    // MONEYHANDLER: When the plugin receives Money, it should decrement the account balance.
    this.btpClientPlugin.unregisterMoneyHandler();
    this.btpClientPlugin.registerMoneyHandler(amount -> CompletableFuture.supplyAsync(() -> {
      balanceTracker.getAndAdd(amount.intValue());
      return null;
    }, EXECUTOR_SERVICE));

    this.btpClientPlugin.connect().join();
    assertThat(btpClientPlugin.isConnected(), is(true));
  }

  @After
  public void cleanup() throws InterruptedException, ExecutionException, TimeoutException {
    this.btpClientPlugin.disconnect().get(2, TimeUnit.SECONDS);
  }

  @Test
  public void testPluginSettings() {
    assertThat(btpClientPlugin.getPluginSettings().getAccountAddress(), is(ACCOUNT_ADDRESS));
    assertThat(btpClientPlugin.getPluginSettings().getOperatorAddress(), is(CLIENT_OPERATOR_ADDRESS));
    assertThat(btpClientPlugin.getPluginSettings().getSecret(), is("shh"));
  }

  @Test
  public void testBtpPluginConnectAndDisconnect() {
    this.btpClientPlugin.disconnect().join();
    assertThat(btpClientPlugin.isConnected(), is(false));

    this.btpClientPlugin.disconnect().join();
    assertThat(btpClientPlugin.isConnected(), is(false));

    this.btpClientPlugin.connect().join();
    assertThat(btpClientPlugin.isConnected(), is(true));

    this.btpClientPlugin.connect().join();
    assertThat(btpClientPlugin.isConnected(), is(true));

    this.btpClientPlugin.disconnect().join();
    assertThat(btpClientPlugin.isConnected(), is(false));

    this.btpClientPlugin.disconnect().join();
    assertThat(btpClientPlugin.isConnected(), is(false));

    this.btpClientPlugin.connect().join();
    assertThat(btpClientPlugin.isConnected(), is(true));

    this.btpClientPlugin.disconnect().join();
    assertThat(btpClientPlugin.isConnected(), is(false));
  }

  @Test
  public void testDataSender() {
    // A prepare packet with a data-payload that contains the preimage of FULFILLMENT.
    final InterledgerPreparePacket PREPARE_PACKET = TestHelpers.constructSendDataPreparePacket(ACCOUNT_ADDRESS);

    final Optional<InterledgerResponsePacket> result = btpClientPlugin.sendData(PREPARE_PACKET).join();

    new InterledgerResponsePacketHandler() {
      @Override
      protected void handleFulfillPacket(InterledgerFulfillPacket interledgerFulfillPacket) {
        assertThat(interledgerFulfillPacket.getFulfillment(), is(FULFILLMENT));
      }

      @Override
      protected void handleRejectPacket(InterledgerRejectPacket interledgerRejectPacket) {
        fail("Should not reject!");
      }

      @Override
      protected void handleExpiredPacket() {
        fail("Should not expire!");
      }
    }.handle(result);

  }

  @Test
  public void testDataReceiver() throws InterruptedException, ExecutionException, TimeoutException {
    // A prepare packet with a data-payload that contains the preimage of FULFILLMENT.
    final InterledgerPreparePacket PREPARE_PACKET = TestHelpers.constructSendDataPreparePacket(ACCOUNT_ADDRESS);

    // The DataHandler for this plugin is a Loopback plugin.
    final Optional<InterledgerResponsePacket> result = this.btpClientPlugin.safeGetDataHandler()
        .handleIncomingData(PREPARE_PACKET).get(1, TimeUnit.SECONDS);

    new InterledgerResponsePacketHandler() {
      @Override
      protected void handleFulfillPacket(InterledgerFulfillPacket interledgerFulfillPacket) {
        assertThat(interledgerFulfillPacket.getFulfillment(), is(FULFILLMENT));
      }

      @Override
      protected void handleRejectPacket(InterledgerRejectPacket interledgerRejectPacket) {
        fail("Should not reject!");
      }

      @Override
      protected void handleExpiredPacket() {
        fail("Should not expire!");
      }
    }.handle(result);

  }

  @Test
  public void testMoneySender() {
    btpClientPlugin.sendMoney(BigInteger.TEN).join();
    assertThat(balanceTracker.get(), is(-10));
    btpClientPlugin.sendMoney(BigInteger.TEN).join();
    assertThat(balanceTracker.get(), is(-20));
    btpClientPlugin.sendMoney(BigInteger.TEN).join();
    assertThat(balanceTracker.get(), is(-30));
    btpClientPlugin.sendMoney(BigInteger.TEN).join();
    assertThat(balanceTracker.get(), is(-40));
  }

  @Test
  public void testMoneyReceiver() {
    btpClientPlugin.getMoneyHandler().get().handleIncomingMoney(BigInteger.TEN).join();
    assertThat(balanceTracker.get(), is(10));
    btpClientPlugin.getMoneyHandler().get().handleIncomingMoney(BigInteger.TEN).join();
    assertThat(balanceTracker.get(), is(20));
    btpClientPlugin.getMoneyHandler().get().handleIncomingMoney(BigInteger.TEN).join();
    assertThat(balanceTracker.get(), is(30));
    btpClientPlugin.getMoneyHandler().get().handleIncomingMoney(BigInteger.TEN).join();
    assertThat(balanceTracker.get(), is(40));
  }

  @Test
  public void testSendAndReceiveMoney() throws InterruptedException, ExecutionException, TimeoutException {
    btpClientPlugin.sendMoney(BigInteger.TEN).get(10, TimeUnit.MINUTES);
    assertThat(balanceTracker.get(), is(-10));
    MoneyHandler handler = btpClientPlugin.getMoneyHandler().get();
    CompletableFuture<Void> cf = handler.handleIncomingMoney(BigInteger.TEN);
    cf.get(1, TimeUnit.MILLISECONDS);
    assertThat(balanceTracker.get(), is(0));

    btpClientPlugin.getMoneyHandler().get().handleIncomingMoney(BigInteger.TEN).get(1, TimeUnit.SECONDS);
    assertThat(balanceTracker.get(), is(10));
    btpClientPlugin.sendMoney(BigInteger.TEN).get(1, TimeUnit.SECONDS);
    assertThat(balanceTracker.get(), is(0));

    btpClientPlugin.sendMoney(BigInteger.TEN).get(1, TimeUnit.SECONDS);
    assertThat(balanceTracker.get(), is(-10));
    btpClientPlugin.getMoneyHandler().get().handleIncomingMoney(BigInteger.TEN).get(1, TimeUnit.SECONDS);
    assertThat(balanceTracker.get(), is(0));

    btpClientPlugin.sendMoney(BigInteger.TEN).get(1, TimeUnit.SECONDS);
    btpClientPlugin.sendMoney(BigInteger.TEN).get(1, TimeUnit.SECONDS);
    assertThat(balanceTracker.get(), is(-20));
    btpClientPlugin.getMoneyHandler().get().handleIncomingMoney(BigInteger.TEN).get(1, TimeUnit.SECONDS);
    btpClientPlugin.getMoneyHandler().get().handleIncomingMoney(BigInteger.TEN).get(1, TimeUnit.SECONDS);
    assertThat(balanceTracker.get(), is(0));
  }

  @Configuration
  @EnableAutoConfiguration
  @EnableWebSocket
  protected static class TestServerConfig implements WebMvcConfigurer, WebSocketConfigurer {

    @Autowired
    BinaryMessageToBtpPacketConverter binaryMessageToBtpPacketConverter;

    @Autowired
    BtpPacketToBinaryMessageConverter btpPacketToBinaryMessageConverter;

    @Autowired
    ServerAuthBtpSubprotocolHandler serverAuthBtpSubprotocolHandler;

    @Autowired
    BtpSubProtocolHandlerRegistry btpSubProtocolHandlerRegistry;

    @Autowired
    BtpServerPluginFactory btpServerPluginFactory;

    @Bean
    BinaryMessageToBtpPacketConverter binaryMessageToBtpPacketConverter() {
      return new BinaryMessageToBtpPacketConverter(BtpCodecContextFactory.oer());
    }

    @Bean
    BtpPacketToBinaryMessageConverter btpPacketToBinaryMessageConverter() {
      return new BtpPacketToBinaryMessageConverter(BtpCodecContextFactory.oer());
    }

    @Bean
    ServerAuthBtpSubprotocolHandler serverAuthBtpSubprotocolHandler() {
      final BtpAuthenticator singleAccountAuthenticator = new AlwaysAllowedBtpAuthenticator(ACCOUNT_ADDRESS);
      final BtpMultiAuthenticator multiAuthenticator = new AlwaysAllowedBtpMultiAuthenticator(SERVER_OPERATOR_ADDRESS);
      return new ServerAuthBtpSubprotocolHandler(singleAccountAuthenticator, multiAuthenticator);
    }

    @Bean
    BtpSubProtocolHandlerRegistry btpSubProtocolHandlerRegistry() {
      return new BtpSubProtocolHandlerRegistry(serverAuthBtpSubprotocolHandler);
    }

    @Bean
    BtpServerPluginFactory btpServerPluginFactory() {
      final BtpServerPluginFactory factory = new BtpServerPluginFactory(
          InterledgerCodecContextFactory.oer(),
          binaryMessageToBtpPacketConverter,
          btpPacketToBinaryMessageConverter,
          btpSubProtocolHandlerRegistry
      );

      // For testing purposes, all server plugins should use a Loopback plugin as their underlying DataHandler and
      // MoneyHandler implementations.
      final LoopbackPlugin loopbackPlugin = new LoopbackPlugin(SERVER_OPERATOR_ADDRESS);
      btpSubProtocolHandlerRegistry.getIlpSubProtocolHandler()
          .registerDataHandler(SERVER_OPERATOR_ADDRESS, (incomingPreparePacket) ->
              loopbackPlugin.getDataHandler().get().handleIncomingData(incomingPreparePacket)
          );

      return factory;
    }

    /**
     * Simulate a WebSocketHandler that always accepts BTP authentication.
     */
    @Bean
    WebSocketHandler websocketHandler() {
      return new BtpConnectedPluginsManager(
          SERVER_OPERATOR_ADDRESS,
          btpServerPluginFactory,
          binaryMessageToBtpPacketConverter,
          btpPacketToBinaryMessageConverter,
          serverAuthBtpSubprotocolHandler
      );
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
      registry.addHandler(websocketHandler(), "/btp");
    }
  }

}