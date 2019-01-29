package org.interledger.plugin.lpiv2.btp2.spring;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.interledger.plugin.lpiv2.btp2.spring.TestHelpers.FULFILLMENT;
import static org.junit.Assert.fail;

import org.interledger.btp.BtpResponsePacket;
import org.interledger.btp.asn.framework.BtpCodecContextFactory;
import org.interledger.core.InterledgerAddress;
import org.interledger.core.InterledgerFulfillPacket;
import org.interledger.core.InterledgerPreparePacket;
import org.interledger.core.InterledgerRejectPacket;
import org.interledger.core.InterledgerResponsePacket;
import org.interledger.core.InterledgerResponsePacketHandler;
import org.interledger.core.asn.framework.InterledgerCodecContextFactory;
import org.interledger.plugin.PluginFactory;
import org.interledger.plugin.lpiv2.DefaultPluginSettings;
import org.interledger.plugin.lpiv2.LoopbackPlugin;
import org.interledger.plugin.lpiv2.Plugin;
import org.interledger.plugin.lpiv2.PluginId;
import org.interledger.plugin.lpiv2.PluginSettings;
import org.interledger.plugin.lpiv2.btp2.spring.BtpClientServerPluginTest.TestServerConfig;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BinaryMessageToBtpPacketConverter;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BtpPacketToBinaryMessageConverter;
import org.interledger.plugin.lpiv2.btp2.spring.factories.BtpServerPluginFactory;
import org.interledger.plugin.lpiv2.btp2.spring.factories.LoopbackPluginFactory;
import org.interledger.plugin.lpiv2.btp2.subprotocols.BtpMultiAuthenticator;
import org.interledger.plugin.lpiv2.btp2.subprotocols.BtpMultiAuthenticator.AlwaysAllowedBtpMultiAuthenticator;
import org.interledger.plugin.lpiv2.btp2.subprotocols.BtpSubProtocolHandlerRegistry;
import org.interledger.plugin.lpiv2.btp2.subprotocols.auth.ClientAuthBtpSubprotocolHandler;
import org.interledger.plugin.lpiv2.btp2.subprotocols.auth.ServerAuthBtpSubprotocolHandler;

import ch.qos.logback.classic.Level;
import com.google.common.eventbus.EventBus;
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
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
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
public class BtpClientServerPluginTest {

  private static final InterledgerAddress CLIENT_OPERATOR_ADDRESS = InterledgerAddress.of("test.client");
  private static final InterledgerAddress SERVER_OPERATOR_ADDRESS = InterledgerAddress.of("test.server");
  private static final String CLIENT_ACCOUNT = "the-client-account";

  // Simulate the server is authoritative for the account...
  public static final InterledgerAddress DESTINATION_ADDRESS = SERVER_OPERATOR_ADDRESS.with(CLIENT_ACCOUNT);

  private static final PluginId PLUGIN_ID = PluginId.of(SERVER_OPERATOR_ADDRESS.with(CLIENT_ACCOUNT).getValue());
  private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(30);
  // Collects all money values...
  private static final AtomicInteger clientBalanceTracker = new AtomicInteger();
  private static final AtomicInteger serverBalanceTracker = new AtomicInteger();

  private static CountDownLatch sendDataLatch;
  private static CountDownLatch sendMoneyLatch;

  @Rule
  public final TestName testName = new TestName();
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  @Rule
  public ErrorCollector collector = new ErrorCollector();
  @Autowired
  BtpConnectedPluginsManager btpConnectedPluginsManager;
  @LocalServerPort
  private int port;
  private Plugin<BtpClientPluginSettings> btpClientPlugin;

  @Before
  public void setup() {

    // Enable debug mode...
    ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.DEBUG);

    MockitoAnnotations.initMocks(this);
    clientBalanceTracker.set(0);
    serverBalanceTracker.set(0);
    sendDataLatch = new CountDownLatch(10);
    sendMoneyLatch = new CountDownLatch(10);

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
    final BtpClientPluginSettings pluginSettings = BtpClientPluginSettings.builder()
        .operatorAddress(CLIENT_OPERATOR_ADDRESS)
        .authUsername(Optional.of(CLIENT_ACCOUNT)) // The No-Op Authenticator doesn't care for this test.
        .secret("shh")
        .remotePeerScheme("ws")
        .remotePeerHostname("localhost")
        .remotePeerPort(port)
        .sendMoneyWaitTime(Duration.of(5, ChronoUnit.SECONDS))
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
        clientBalanceTracker.getAndAdd((amount.intValue()));
      }
    };

    // DATAHANDLER: When the plugin receives Data, it should process the incoming message using Loopback functionality.
    final LoopbackPlugin loopbackPlugin = new LoopbackPlugin(SERVER_OPERATOR_ADDRESS);
    btpClientPlugin.registerDataHandler(loopbackPlugin.getDataHandler().get());

    // MONEYHANDLER: When the plugin receives Money, it should decrement the account balance.
    this.btpClientPlugin.unregisterMoneyHandler();
    this.btpClientPlugin.registerMoneyHandler(amount -> CompletableFuture.supplyAsync(() -> {
      clientBalanceTracker.getAndAdd(amount.intValue() * -1);
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
    assertThat(btpClientPlugin.getPluginSettings().getOperatorAddress(), is(CLIENT_OPERATOR_ADDRESS));
    assertThat(btpClientPlugin.getPluginSettings().getSecret(), is("shh"));
  }

  //////////////////////////
  // BTP Client Plugin Tests
  //////////////////////////

  @Test
  public void testBtpClientPluginConnectAndDisconnect() {
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
  public void testClientDataSender() {
    // A prepare packet with a data-payload that contains the preimage of FULFILLMENT.
    final InterledgerPreparePacket PREPARE_PACKET = TestHelpers.constructSendDataPreparePacket(DESTINATION_ADDRESS);

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
  public void testClientDataReceiver() throws InterruptedException, ExecutionException, TimeoutException {
    // A prepare packet with a data-payload that contains the preimage of FULFILLMENT.
    final InterledgerPreparePacket PREPARE_PACKET = TestHelpers.constructSendDataPreparePacket(DESTINATION_ADDRESS);

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
  public void testClientMoneySender() {
    btpClientPlugin.sendMoney(BigInteger.TEN).join();
    assertThat(clientBalanceTracker.get(), is(10));
    assertThat(serverBalanceTracker.get(), is(-10));

    btpClientPlugin.sendMoney(BigInteger.TEN).join();
    assertThat(clientBalanceTracker.get(), is(20));
    assertThat(serverBalanceTracker.get(), is(-20));

    btpClientPlugin.sendMoney(BigInteger.TEN).join();
    assertThat(clientBalanceTracker.get(), is(30));
    assertThat(serverBalanceTracker.get(), is(-30));

    btpClientPlugin.sendMoney(BigInteger.TEN).join();
    assertThat(clientBalanceTracker.get(), is(40));
    assertThat(serverBalanceTracker.get(), is(-40));
  }

  @Test
  public void testClientMoneyReceiver() {
    btpClientPlugin.getMoneyHandler().get().handleIncomingMoney(BigInteger.TEN).join();
    assertThat(clientBalanceTracker.get(), is(-10));
    btpClientPlugin.getMoneyHandler().get().handleIncomingMoney(BigInteger.TEN).join();
    assertThat(clientBalanceTracker.get(), is(-20));
    btpClientPlugin.getMoneyHandler().get().handleIncomingMoney(BigInteger.TEN).join();
    assertThat(clientBalanceTracker.get(), is(-30));
    btpClientPlugin.getMoneyHandler().get().handleIncomingMoney(BigInteger.TEN).join();
    assertThat(clientBalanceTracker.get(), is(-40));
  }

  //////////////////////////
  // BTP Server Plugin Tests
  //////////////////////////

  @Test
  public void testBtpServerPluginConnectAndDisconnect() {
    final BtpServerPlugin btpServerPlugin = getBtpServerPlugin();
    assertThat(btpServerPlugin.isConnected(), is(true));

    btpServerPlugin.disconnect().join();
    assertThat(btpServerPlugin.isConnected(), is(false));
    assertThat(btpConnectedPluginsManager.getAllConnectedPluginIds().count(), is(0L));

    btpServerPlugin.disconnect().join();
    assertThat(btpServerPlugin.isConnected(), is(false));
    assertThat(btpConnectedPluginsManager.getAllConnectedPluginIds().count(), is(0L));

    btpServerPlugin.connect().join();
    assertThat(btpServerPlugin.isConnected(), is(true));
    assertThat(btpConnectedPluginsManager.getAllConnectedPluginIds().count(), is(1L));

    btpServerPlugin.connect().join();
    assertThat(btpServerPlugin.isConnected(), is(true));
    assertThat(btpConnectedPluginsManager.getAllConnectedPluginIds().count(), is(1L));

    btpServerPlugin.disconnect().join();
    assertThat(btpServerPlugin.isConnected(), is(false));
    assertThat(btpConnectedPluginsManager.getAllConnectedPluginIds().count(), is(0L));

    btpServerPlugin.disconnect().join();
    assertThat(btpServerPlugin.isConnected(), is(false));
    assertThat(btpConnectedPluginsManager.getAllConnectedPluginIds().count(), is(0L));

    btpServerPlugin.connect().join();
    assertThat(btpServerPlugin.isConnected(), is(true));
    assertThat(btpConnectedPluginsManager.getAllConnectedPluginIds().count(), is(1L));

    btpServerPlugin.disconnect().join();
    assertThat(btpServerPlugin.isConnected(), is(false));
    assertThat(btpConnectedPluginsManager.getAllConnectedPluginIds().count(), is(0L));
  }

  @Test
  public void testServerDataSender() {
    final BtpServerPlugin btpServerPlugin = getBtpServerPlugin();
    assertThat(btpServerPlugin.isConnected(), is(true));

    // A prepare packet with a data-payload that contains the preimage of FULFILLMENT.
    final InterledgerPreparePacket PREPARE_PACKET = TestHelpers.constructSendDataPreparePacket(DESTINATION_ADDRESS);

    final Optional<InterledgerResponsePacket> result = btpServerPlugin.sendData(PREPARE_PACKET).join();

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
  public void testServerDataReceiver() throws InterruptedException, ExecutionException, TimeoutException {
    final BtpServerPlugin btpServerPlugin = getBtpServerPlugin();
    assertThat(btpServerPlugin.isConnected(), is(true));

    // A prepare packet with a data-payload that contains the preimage of FULFILLMENT.
    final InterledgerPreparePacket PREPARE_PACKET = TestHelpers.constructSendDataPreparePacket(DESTINATION_ADDRESS);

    // The DataHandler for this plugin is a Loopback plugin.
    final Optional<InterledgerResponsePacket> result = btpServerPlugin.safeGetDataHandler()
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
  public void testServerMoneySender() {
    final BtpServerPlugin btpServerPlugin = getBtpServerPlugin();
    assertThat(btpServerPlugin.isConnected(), is(true));

    btpServerPlugin.sendMoney(BigInteger.TEN).join();
    assertThat(clientBalanceTracker.get(), is(-10));
    assertThat(serverBalanceTracker.get(), is(10));

    btpServerPlugin.sendMoney(BigInteger.TEN).join();
    assertThat(clientBalanceTracker.get(), is(-20));
    assertThat(serverBalanceTracker.get(), is(20));

    btpServerPlugin.sendMoney(BigInteger.TEN).join();
    assertThat(clientBalanceTracker.get(), is(-30));
    assertThat(serverBalanceTracker.get(), is(30));

    btpServerPlugin.sendMoney(BigInteger.TEN).join();
    assertThat(clientBalanceTracker.get(), is(-40));
    assertThat(serverBalanceTracker.get(), is(40));
  }

  @Test
  public void testServerMoneyReceiver() {
    final BtpServerPlugin btpServerPlugin = getBtpServerPlugin();
    assertThat(btpServerPlugin.isConnected(), is(true));

    btpServerPlugin.getMoneyHandler().get().handleIncomingMoney(BigInteger.TEN).join();
    assertThat(serverBalanceTracker.get(), is(-10));
    btpServerPlugin.getMoneyHandler().get().handleIncomingMoney(BigInteger.TEN).join();
    assertThat(serverBalanceTracker.get(), is(-20));
    btpServerPlugin.getMoneyHandler().get().handleIncomingMoney(BigInteger.TEN).join();
    assertThat(serverBalanceTracker.get(), is(-30));
    btpServerPlugin.getMoneyHandler().get().handleIncomingMoney(BigInteger.TEN).join();
    assertThat(serverBalanceTracker.get(), is(-40));
  }

  //////////////
  // Combo Tests
  //////////////

  @Test
  public void testSendMoneyBetweenClientAndServer() throws InterruptedException, ExecutionException, TimeoutException {
    final BtpServerPlugin btpServerPlugin = getBtpServerPlugin();

    btpClientPlugin.sendMoney(BigInteger.TEN).get(10, TimeUnit.MINUTES);
    assertThat(clientBalanceTracker.get(), is(10));
    assertThat(serverBalanceTracker.get(), is(-10));

    btpClientPlugin.sendMoney(BigInteger.TEN).get(1, TimeUnit.SECONDS);
    assertThat(clientBalanceTracker.get(), is(20));
    assertThat(serverBalanceTracker.get(), is(-20));

    btpServerPlugin.sendMoney(BigInteger.TEN).get(10, TimeUnit.MINUTES);
    assertThat(clientBalanceTracker.get(), is(10));
    assertThat(serverBalanceTracker.get(), is(-10));

    btpServerPlugin.sendMoney(BigInteger.TEN).get(1, TimeUnit.SECONDS);
    assertThat(clientBalanceTracker.get(), is(0));
    assertThat(serverBalanceTracker.get(), is(0));

    btpServerPlugin.sendMoney(BigInteger.TEN).get(10, TimeUnit.MINUTES);
    assertThat(clientBalanceTracker.get(), is(-10));
    assertThat(serverBalanceTracker.get(), is(10));

    btpServerPlugin.sendMoney(BigInteger.TEN).get(1, TimeUnit.SECONDS);
    assertThat(clientBalanceTracker.get(), is(-20));
    assertThat(serverBalanceTracker.get(), is(20));

    btpClientPlugin.sendMoney(BigInteger.TEN).get(10, TimeUnit.MINUTES);
    assertThat(clientBalanceTracker.get(), is(-10));
    assertThat(serverBalanceTracker.get(), is(10));

    btpClientPlugin.sendMoney(BigInteger.TEN).get(1, TimeUnit.SECONDS);
    assertThat(clientBalanceTracker.get(), is(0));
    assertThat(serverBalanceTracker.get(), is(0));
  }

  @Test
  public void testSendMoneyMultiThreaded() throws InterruptedException {
    final BtpServerPlugin btpServerPlugin = getBtpServerPlugin();
    assertThat(btpClientPlugin.isConnected(), is(true));
    assertThat(btpServerPlugin.isConnected(), is(true));

    final int numReps = 10;

    final CountDownLatch countDownLatch = new CountDownLatch(numReps);

    final BigInteger clientSend = BigInteger.TEN;
    final BigInteger serverSend = BigInteger.TEN;

    // Schedule `numReps` sendMoney...
    for (int i = 0; i < numReps; i++) {
      final Instant now = Instant.now();
      btpClientPlugin.sendMoney(clientSend).thenAccept($ -> {
        countDownLatch.countDown();
        logger.info("CLIENT-SENDMONEY COMPLETED in {}s",
            TimeUnit.NANOSECONDS.toMillis(Duration.between(now, Instant.now()).getNano()));
      }).join();
    }

    for (int i = 0; i < numReps; i++) {
      final Instant now = Instant.now();
      btpServerPlugin.sendMoney(serverSend).thenAccept($ -> {
        countDownLatch.countDown();
        logger.info("SERVER-SENDMONEY COMPLETED in {}ns",
            TimeUnit.NANOSECONDS.toMillis(Duration.between(now, Instant.now()).getNano()));
      }).join();
    }

    if (countDownLatch.await(5, TimeUnit.SECONDS) == false) {
      throw new RuntimeException(
          "Countdown latch timed out instead of reaching 0! Count: " + countDownLatch.getCount());
    }

    final BigInteger totalClientSent = clientSend.multiply(BigInteger.valueOf(numReps));
    final BigInteger totalServerSent = serverSend.multiply(BigInteger.valueOf(numReps));

    assertThat(clientBalanceTracker.get(), is(totalClientSent.subtract(totalServerSent).intValue()));
    assertThat(serverBalanceTracker.get(), is(totalServerSent.subtract(totalClientSent).intValue()));
  }

  private BtpServerPlugin getBtpServerPlugin() {
    final PluginId pluginId = this.btpConnectedPluginsManager.getAllConnectedPluginIds().findFirst().get();
    return this.btpConnectedPluginsManager.getConnectedPlugin(BtpServerPlugin.class, pluginId).get();
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
    PendingResponseManager<BtpResponsePacket> pendingResponseManager;
    @Autowired
    BtpConnectedPluginsManager btpConnectedPluginsManager;

    @Bean
    EventBus eventBus() {
      return new EventBus();
    }

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
      final BtpMultiAuthenticator multiAuthenticator = new AlwaysAllowedBtpMultiAuthenticator(
          () -> SERVER_OPERATOR_ADDRESS);
      return new ServerAuthBtpSubprotocolHandler(multiAuthenticator);
    }

    @Bean
    BtpSubProtocolHandlerRegistry btpSubProtocolHandlerRegistry() {
      return new BtpSubProtocolHandlerRegistry(serverAuthBtpSubprotocolHandler);
    }

    @Bean
    PendingResponseManager<BtpResponsePacket> pendingResponseManager() {
      return new PendingResponseManager<>(BtpResponsePacket.class);
    }

    @Bean
    @Qualifier("BTP")
    PluginFactory btpServerPluginFactory() {
      final BtpServerPluginFactory factory = new BtpServerPluginFactory(
          InterledgerCodecContextFactory.oer(),
          binaryMessageToBtpPacketConverter,
          btpPacketToBinaryMessageConverter,
          btpSubProtocolHandlerRegistry,
          pendingResponseManager
      ) {
        /**
         * Always return a Server Plugin so we can test it properly as well.
         */
        @Override
        public <PS extends PluginSettings, P extends Plugin<PS>> P constructPlugin(Class<P> $, PS pluginSettings) {
          final BtpServerPluginSettings pluginSettingsInstead = BtpServerPluginSettings.builder()
              .operatorAddress(SERVER_OPERATOR_ADDRESS)
              .secret("shh")
              .sendMoneyWaitTime(Duration.of(5, ChronoUnit.SECONDS))
              .build();
          final P plugin = (P) new BtpServerPlugin(
              pluginSettingsInstead,
              InterledgerCodecContextFactory.oer(),
              binaryMessageToBtpPacketConverter,
              btpPacketToBinaryMessageConverter,
              btpSubProtocolHandlerRegistry,
              pendingResponseManager
          ) {
            @Override
            public CompletableFuture<Optional<InterledgerResponsePacket>> sendData(
                InterledgerPreparePacket preparePacket) {
              serverBalanceTracker.addAndGet(preparePacket.getAmount().intValue() * -1);
              sendDataLatch.countDown();
              return super.sendData(preparePacket);
            }

            @Override
            public void doSendMoney(BigInteger amount) {
              serverBalanceTracker.addAndGet(amount.intValue());
              sendMoneyLatch.countDown();
            }
          };

          // For testing purposes, all server plugins should use a Loopback plugin as their underlying DataHandler and
          // MoneyHandler implementations.
          final LoopbackPlugin loopbackPlugin = new LoopbackPlugin(SERVER_OPERATOR_ADDRESS);
          plugin.unregisterDataHandler();
          plugin.registerDataHandler(incomingPreparePacket -> {
            serverBalanceTracker.addAndGet(incomingPreparePacket.getAmount().intValue());
            return loopbackPlugin.getDataHandler().get().handleIncomingData(incomingPreparePacket);
          });

          plugin.registerMoneyHandler(amount -> {
            serverBalanceTracker.addAndGet(amount.intValue() * -1);
            return CompletableFuture.completedFuture(null);
          });

          return plugin;
        }
      };

      return factory;
    }

    // TODO: Look more closely here and see if we need all the custom overrides...
    @Bean
    @Qualifier(LoopbackPlugin.PLUGIN_TYPE_STRING)
    LoopbackPluginFactory loopbackPluginFactory() {
      final LoopbackPluginFactory factory = new LoopbackPluginFactory() {
        /**
         * Always return a Server Plugin so we can test it properly as well.
         */
        @Override
        public <PS extends PluginSettings, P extends Plugin<PS>> P constructPlugin(Class<P> $, PS pluginSettings) {
          final BtpServerPluginSettings pluginSettingsInstead = BtpServerPluginSettings.builder()
              .operatorAddress(SERVER_OPERATOR_ADDRESS)
              .secret("shh")
              .sendMoneyWaitTime(Duration.of(5, ChronoUnit.SECONDS))
              .build();
          final P plugin = (P) new BtpServerPlugin(
              pluginSettingsInstead,
              InterledgerCodecContextFactory.oer(),
              binaryMessageToBtpPacketConverter,
              btpPacketToBinaryMessageConverter,
              btpSubProtocolHandlerRegistry,
              pendingResponseManager
          ) {
            @Override
            public CompletableFuture<Optional<InterledgerResponsePacket>> sendData(
                InterledgerPreparePacket preparePacket) {
              serverBalanceTracker.addAndGet(preparePacket.getAmount().intValue() * -1);
              sendDataLatch.countDown();
              return super.sendData(preparePacket);
            }

            @Override
            public void doSendMoney(BigInteger amount) {
              serverBalanceTracker.addAndGet(amount.intValue());
              sendMoneyLatch.countDown();
            }
          };

          // For testing purposes, all server plugins should use a Loopback plugin as their underlying DataHandler and
          // MoneyHandler implementations.
          final LoopbackPlugin loopbackPlugin = new LoopbackPlugin(SERVER_OPERATOR_ADDRESS);
          plugin.unregisterDataHandler();
          plugin.registerDataHandler(incomingPreparePacket -> {
            serverBalanceTracker.addAndGet(incomingPreparePacket.getAmount().intValue());
            return loopbackPlugin.getDataHandler().get().handleIncomingData(incomingPreparePacket);
          });

          plugin.registerMoneyHandler(amount -> {
            serverBalanceTracker.addAndGet(amount.intValue() * -1);
            return CompletableFuture.completedFuture(null);
          });

          return plugin;
        }
      };

      return factory;
    }

    /**
     * Simulate a WebSocketHandler that always accepts BTP authentication. The bean returned from this methos is also an
     * instance of {@link WebSocketHandler}.
     */
    @Bean
    public BtpConnectedPluginsManager connectedPluginsManager(EventBus eventBus) {
      return new BtpConnectedPluginsManager(
          () -> SERVER_OPERATOR_ADDRESS, DefaultPluginSettings.builder()
              // By default, wait 5 seconds for the sendMoney operation.
              .putCustomSettings(BtpServerPluginSettings.SEND_MONEY_WAIT_TIME_KEY, "5000") // millis
              .build(), btpServerPluginFactory(), binaryMessageToBtpPacketConverter, btpPacketToBinaryMessageConverter,
          serverAuthBtpSubprotocolHandler, eventBus
      );
    }

    private WebSocketHandler websocketHandler() {
      return this.btpConnectedPluginsManager;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
      registry.addHandler(websocketHandler(), "/btp");
    }
  }

}