package org.interledger.plugin.lpiv2.btp2.spring.connection;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Unit tests to validate {@link MultiAccountBtpServerConnection} using a {@link SingleAccountBtpClientConnection}.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ContextConfiguration(classes = {TestServerConfig.class})
public class MultiAccountBtpConnectionTest {

//  private static final InterledgerAddress CLIENT1_OPERATOR_ADDRESS = InterledgerAddress.of("test.client1");
//  private static final InterledgerAddress CLIENT2_OPERATOR_ADDRESS = InterledgerAddress.of("test.client2");
//
//  private static final InterledgerAddress SERVER_OPERATOR_ADDRESS = InterledgerAddress.of("test.server");
//
//  // Simluate the server is authoritative for the account...
//  private static final InterledgerAddress ACCOUNT_ADDRESS1 = InterledgerAddress.of("test.server.client1.usd");
//  private static final InterledgerAddress ACCOUNT_ADDRESS2 = InterledgerAddress.of("test.server.client2.usd");
//
//  @Rule
//  public final TestName testName = new TestName();
//  private final Logger logger = LoggerFactory.getLogger(this.getClass());
//  @Rule
//  public ErrorCollector collector = new ErrorCollector();
//
//  @LocalServerPort
//  private int port;
//
//  private ClientBtpWebsocketMux clientBtpWebsocketMux1;
//  private ClientBtpWebsocketMux clientBtpWebsocketMux2;
//
//  private SingleAccountBtpClientConnection bilateralConnection1;
//  private SingleAccountBtpClientConnection bilateralConnection2;

//  @Before
//  public void setup() {
//
//    // Enable debug mode...
//    //((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.DEBUG);
//
//    MockitoAnnotations.initMocks(this);
//
//    logger.debug("Setting up before '" + this.testName.getMethodName() + "'");
//
//    //////////////////////////
//    // Account1 Connection Setup
//    //////////////////////////
//    final BinaryMessageToBtpPacketConverter binaryMessageToBtpPacketConverter = new BinaryMessageToBtpPacketConverter(
//        BtpCodecContextFactory.oer()
//    );
//    final BtpPacketToBinaryMessageConverter btpPacketToBinaryMessageConverter = new BtpPacketToBinaryMessageConverter(
//        BtpCodecContextFactory.oer()
//    );
//
//    {
//      final ClientAuthBtpSubprotocolHandler btpAuthHandler = new ClientAuthBtpSubprotocolHandler();
//      final BtpSubProtocolHandlerRegistry btpSubProtocolHandlerRegistry =
//          new BtpSubProtocolHandlerRegistry(btpAuthHandler);
//
//      this.clientBtpWebsocketMux1 = new ClientBtpWebsocketMux(
//          binaryMessageToBtpPacketConverter,
//          btpPacketToBinaryMessageConverter,
//          btpSubProtocolHandlerRegistry,
//          CLIENT1_OPERATOR_ADDRESS,
//          Optional.of(ACCOUNT_ADDRESS1.getValue()),
//          "shh",
//          "ws",
//          "localhost",
//          port + "",
//          new StandardWebSocketClient()
//      );
//
//      this.bilateralConnection1 = new SingleAccountBtpClientConnection(
//          CLIENT1_OPERATOR_ADDRESS, ACCOUNT_ADDRESS1, clientBtpWebsocketMux1
//      );
//    }
//
//    //////////////////////////
//    // Account2 Connection Setup
//    //////////////////////////
//    {
//      final ClientAuthBtpSubprotocolHandler btpAuthHandler = new ClientAuthBtpSubprotocolHandler();
//      final BtpSubProtocolHandlerRegistry btpSubProtocolHandlerRegistry =
//          new BtpSubProtocolHandlerRegistry(btpAuthHandler);
//
//      this.clientBtpWebsocketMux2 = new ClientBtpWebsocketMux(
//          binaryMessageToBtpPacketConverter,
//          btpPacketToBinaryMessageConverter,
//          btpSubProtocolHandlerRegistry,
//          CLIENT2_OPERATOR_ADDRESS,
//          Optional.of(ACCOUNT_ADDRESS2.getValue()),
//          "shh",
//          "ws",
//          "localhost",
//          port + "",
//          new StandardWebSocketClient()
//      );
//
//      this.bilateralConnection2 = new SingleAccountBtpClientConnection(
//          CLIENT2_OPERATOR_ADDRESS, ACCOUNT_ADDRESS2, clientBtpWebsocketMux2
//      );
//    }
//  }
//
//  @Test
//  public void testComboAccessors() {
//    assertThat(bilateralConnection1.getOperatorAddress(), is(CLIENT1_OPERATOR_ADDRESS));
//    assertThat(bilateralConnection2.getOperatorAddress(), is(CLIENT2_OPERATOR_ADDRESS));
//
//    assertThat(bilateralConnection1.getBilateralReceiverMux(), is(clientBtpWebsocketMux1));
//    assertThat(bilateralConnection1.getBilateralSenderMux(), is(clientBtpWebsocketMux1));
//    assertThat(bilateralConnection1.getComboMux(), is(clientBtpWebsocketMux1));
//
//    assertThat(bilateralConnection2.getBilateralReceiverMux(), is(clientBtpWebsocketMux2));
//    assertThat(bilateralConnection2.getBilateralSenderMux(), is(clientBtpWebsocketMux2));
//    assertThat(bilateralConnection2.getComboMux(), is(clientBtpWebsocketMux2));
//  }
//
//  @Test
//  public void testBtpPluginConnectAndDisconnect() {
//    assertThat(bilateralConnection1.getComboMux().isConnected(), is(false));
//    assertThat(bilateralConnection2.getComboMux().isConnected(), is(false));
//
//    // Connect 1
//    this.bilateralConnection1.connect().join();
//    assertThat(bilateralConnection1.getComboMux().isConnected(), is(true));
//    assertThat(bilateralConnection2.getComboMux().isConnected(), is(false));
//
//    // Connect 2
//    this.bilateralConnection2.connect().join();
//    assertThat(bilateralConnection1.getComboMux().isConnected(), is(true));
//    assertThat(bilateralConnection2.getComboMux().isConnected(), is(true));
//
//    // Disconnect 2
//    this.bilateralConnection2.disconnect().join();
//    assertThat(bilateralConnection1.getComboMux().isConnected(), is(true));
//    assertThat(bilateralConnection2.getComboMux().isConnected(), is(false));
//
//    // Disconnect 1
//    this.bilateralConnection1.disconnect().join();
//    assertThat(bilateralConnection1.getComboMux().isConnected(), is(false));
//    assertThat(bilateralConnection2.getComboMux().isConnected(), is(false));
//  }
//
//  @Configuration
//  @EnableAutoConfiguration
//  @EnableWebSocket
//  protected static class TestServerConfig implements WebMvcConfigurer, WebSocketConfigurer {
//
//    @Autowired
//    private BinaryMessageToBtpPacketConverter binaryMessageToBtpPacketConverter;
//    @Autowired
//    private BtpPacketToBinaryMessageConverter btpPacketToBinaryMessageConverter;
//    @Autowired
//    private BtpSubProtocolHandlerRegistry btpSubProtocolHandlerRegistry;
//
//    @Bean
//    BinaryMessageToBtpPacketConverter binaryMessageToBtpPacketConverter() {
//      return new BinaryMessageToBtpPacketConverter(BtpCodecContextFactory.oer());
//    }
//
//    @Bean
//    BtpPacketToBinaryMessageConverter btpPacketToBinaryMessageConverter() {
//      return new BtpPacketToBinaryMessageConverter(BtpCodecContextFactory.oer());
//    }
//
//    @Bean
//    BtpSubProtocolHandlerRegistry btpSubProtocolHandlerRegistry() {
//      final BtpAuthenticator noOpAuthenticator = new BtpAuthenticator() {
//        @Override
//        public boolean isValidAuthToken(String authToken) {
//          return false;
//        }
//
//        @Override
//        public InterledgerAddress getAccountAddress() {
//          throw new RuntimeException("This implementation should never be used!");
//        }
//      };
//      final BtpMultiAuthenticator btpMultiAuthenticator = new AlwaysAllowedBtpMultiAuthenticator(
//          SERVER_OPERATOR_ADDRESS
//      );
//      final ServerAuthBtpSubprotocolHandler btpAuthHandler = new ServerAuthBtpSubprotocolHandler(
//          noOpAuthenticator, btpMultiAuthenticator
//      );
//      return new BtpSubProtocolHandlerRegistry(btpAuthHandler);
//    }
//
//    /**
//     * Simulate a server that operates a single-account BTP server.
//     */
//    @Bean
//    WebSocketHandler websocketHandler() {
//      final ServerBtpWebsocketMux serverBtpWebsocketMux = new ServerBtpWebsocketMux(
//          binaryMessageToBtpPacketConverter, btpPacketToBinaryMessageConverter, btpSubProtocolHandlerRegistry
//      );
//
//      return new MultiAccountBtpServerConnection(SERVER_OPERATOR_ADDRESS, serverBtpWebsocketMux);
//    }
//
//    @Override
//    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//      registry.addHandler(websocketHandler(), "/btp");
//    }
//  }

}