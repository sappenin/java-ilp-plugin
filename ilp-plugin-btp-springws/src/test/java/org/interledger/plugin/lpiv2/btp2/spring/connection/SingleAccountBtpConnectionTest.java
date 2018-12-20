package org.interledger.plugin.lpiv2.btp2.spring.connection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.interledger.btp.asn.framework.BtpCodecContextFactory;
import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.lpiv2.btp2.spring.connection.SingleAccountBtpConnectionTest.TestServerConfig;
import org.interledger.plugin.lpiv2.btp2.spring.connection.mux.ClientBtpWebsocketMux;
import org.interledger.plugin.lpiv2.btp2.spring.connection.mux.ServerBtpWebsocketMux;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BinaryMessageToBtpPacketConverter;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BtpPacketToBinaryMessageConverter;
import org.interledger.plugin.lpiv2.btp2.subprotocols.BtpAuthenticator;
import org.interledger.plugin.lpiv2.btp2.subprotocols.BtpAuthenticator.AlwaysAllowedBtpAuthenticator;
import org.interledger.plugin.lpiv2.btp2.subprotocols.BtpMultiAuthenticator;
import org.interledger.plugin.lpiv2.btp2.subprotocols.BtpSubProtocolHandlerRegistry;
import org.interledger.plugin.lpiv2.btp2.subprotocols.auth.ClientAuthBtpSubprotocolHandler;
import org.interledger.plugin.lpiv2.btp2.subprotocols.auth.ServerAuthBtpSubprotocolHandler;

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

import java.util.Optional;

/**
 * Unit tests to validate {@link SingleAccountBtpServerConnection} using a {@link SingleAccountBtpClientConnection}.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {TestServerConfig.class})
public class SingleAccountBtpConnectionTest {

  private static final InterledgerAddress CLIENT_OPERATOR_ADDRESS = InterledgerAddress.of("test.client");
  private static final InterledgerAddress SERVER_OPERATOR_ADDRESS = InterledgerAddress.of("test.server");

  // Simluate the server is authoritative for the account...
  private static final InterledgerAddress ACCOUNT_ADDRESS = InterledgerAddress.of("test.server.client.usd");
  @Rule
  public final TestName testName = new TestName();
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  @Rule
  public ErrorCollector collector = new ErrorCollector();

  @LocalServerPort
  private int port;

  private ClientBtpWebsocketMux clientBtpWebsocketMux;
  private SingleAccountBtpClientConnection bilateralConnection;

  @Before
  public void setup() {

    // Enable debug mode...
    //((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.DEBUG);

    MockitoAnnotations.initMocks(this);

    logger.debug("Setting up before '" + this.testName.getMethodName() + "'");

    //////////////////////////
    // Bilatearal Connection Setup
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

    this.clientBtpWebsocketMux = new ClientBtpWebsocketMux(
        binaryMessageToBtpPacketConverter,
        btpPacketToBinaryMessageConverter,
        btpSubProtocolHandlerRegistry,
        CLIENT_OPERATOR_ADDRESS,
        Optional.empty(),
        "shh",
        "ws",
        "localhost",
        port + "",
        new StandardWebSocketClient()
    );

    this.bilateralConnection = new SingleAccountBtpClientConnection(
        CLIENT_OPERATOR_ADDRESS, ACCOUNT_ADDRESS, clientBtpWebsocketMux
    );
  }

  @Test
  public void testComboAccessors() {
    assertThat(bilateralConnection.getAccountAddress(), is(ACCOUNT_ADDRESS));
    assertThat(bilateralConnection.getOperatorAddress(), is(CLIENT_OPERATOR_ADDRESS));
    assertThat(bilateralConnection.getOperatorAddress(), is(CLIENT_OPERATOR_ADDRESS));

    assertThat(bilateralConnection.getBilateralReceiverMux(), is(clientBtpWebsocketMux));
    assertThat(bilateralConnection.getBilateralSenderMux(), is(clientBtpWebsocketMux));
    assertThat(bilateralConnection.getComboMux(), is(clientBtpWebsocketMux));
  }

  @Test
  public void testBtpPluginConnectAndDisconnect() {
    this.bilateralConnection.disconnect().join();
    assertThat(bilateralConnection.getComboMux().isConnected(), is(false));
    this.bilateralConnection.disconnect().join();
    assertThat(bilateralConnection.getComboMux().isConnected(), is(false));

    this.bilateralConnection.connect().join();
    assertThat(bilateralConnection.getComboMux().isConnected(), is(true));
    this.bilateralConnection.connect().join();
    assertThat(bilateralConnection.getComboMux().isConnected(), is(true));

    this.bilateralConnection.disconnect().join();
    assertThat(bilateralConnection.getComboMux().isConnected(), is(false));
    this.bilateralConnection.disconnect().join();
    assertThat(bilateralConnection.getComboMux().isConnected(), is(false));

    this.bilateralConnection.connect().join();
    assertThat(bilateralConnection.getComboMux().isConnected(), is(true));

    this.bilateralConnection.disconnect().join();
    assertThat(bilateralConnection.getComboMux().isConnected(), is(false));
  }

  @Configuration
  @EnableAutoConfiguration
  @EnableWebSocket
  protected static class TestServerConfig implements WebMvcConfigurer, WebSocketConfigurer {

    @Autowired
    private BinaryMessageToBtpPacketConverter binaryMessageToBtpPacketConverter;
    @Autowired
    private BtpPacketToBinaryMessageConverter btpPacketToBinaryMessageConverter;
    @Autowired
    private BtpSubProtocolHandlerRegistry btpSubProtocolHandlerRegistry;

    @Bean
    BinaryMessageToBtpPacketConverter binaryMessageToBtpPacketConverter() {
      return new BinaryMessageToBtpPacketConverter(BtpCodecContextFactory.oer());
    }

    @Bean
    BtpPacketToBinaryMessageConverter btpPacketToBinaryMessageConverter() {
      return new BtpPacketToBinaryMessageConverter(BtpCodecContextFactory.oer());
    }

    @Bean
    BtpSubProtocolHandlerRegistry btpSubProtocolHandlerRegistry() {
      final BtpAuthenticator singleAccountAuthenticator = new AlwaysAllowedBtpAuthenticator(ACCOUNT_ADDRESS);
      final BtpMultiAuthenticator noOpAuthenticator = accountAddress -> {
        throw new RuntimeException("BtpMultiAuthenticator should never be called in a Single-account BTP server!");
      };
      final ServerAuthBtpSubprotocolHandler btpAuthHandler = new ServerAuthBtpSubprotocolHandler(
          singleAccountAuthenticator, noOpAuthenticator
      );
      return new BtpSubProtocolHandlerRegistry(btpAuthHandler);
    }

    /**
     * Simulate a server that operates a single-account BTP server.
     */
    @Bean
    WebSocketHandler websocketHandler() {
      final ServerBtpWebsocketMux serverBtpWebsocketMux = new ServerBtpWebsocketMux(
          binaryMessageToBtpPacketConverter, btpPacketToBinaryMessageConverter, btpSubProtocolHandlerRegistry
      );

      return new SingleAccountBtpServerConnection(
          SERVER_OPERATOR_ADDRESS,
          ACCOUNT_ADDRESS,
          serverBtpWebsocketMux
      );
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
      registry.addHandler(websocketHandler(), "/btp");
    }
  }

}