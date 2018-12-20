package org.interledger.plugin.lpiv2.btp2.spring;

/**
 * An extension of {@link AbstractWebsocketBtpPlugin} that connects to a remote peer using BTP, but merely logs all
 * responses.
 */
public class ClientWebsocketBtpPlugin { //extends AbstractWebsocketBtpPlugin<BtpClientPluginSettings> {

//  public static final String PLUGIN_TYPE_STRING = "ClientWebsocketBtpPlugin";
//  public static final PluginType PLUGIN_TYPE = PluginType.of(PLUGIN_TYPE_STRING);
//
//  private final StandardWebSocketClient wsClient;
//
//  /**
//   * Required-args Constructor.
//   */
//  public ClientWebsocketBtpPlugin(final BtpClientPluginSettings settings) {
//    this(settings, InterledgerCodecContextFactory.oer(), BtpCodecContextFactory.oer());
//  }
//
//  /**
//   * Required-args Constructor.
//   */
//  public ClientWebsocketBtpPlugin(
//      final BtpClientPluginSettings settings,
//      final CodecContext ilpCodecContext,
//      final CodecContext btpCodecContext
//  ) {
//    this(
//        settings,
//        ilpCodecContext,
//        btpCodecContext,
//        // Clients don't need to authenticate the BtpSession...
//        new BtpSubProtocolHandlerRegistry(new AlwaysAllowedBtpAuthenticationService(settings.getPeerAccountAddress())),
//        new BinaryMessageToBtpPacketConverter(btpCodecContext),
//        new BtpPacketToBinaryMessageConverter(btpCodecContext),
//        new StandardWebSocketClient()
//    );
//  }
//
//  /**
//   * Required-args Constructor.
//   */
//  public ClientWebsocketBtpPlugin(
//      final BtpClientPluginSettings settings,
//      final CodecContext ilpCodecContext,
//      final CodecContext btpCodecContext,
//      final BtpSubProtocolHandlerRegistry btpSubProtocolHandlerRegistry,
//      final BinaryMessageToBtpPacketConverter binaryMessageToBtpPacketConverter,
//      final BtpPacketToBinaryMessageConverter btpPacketToBinaryMessageConverter,
//      final StandardWebSocketClient wsClient
//  ) {
//    super(settings, ilpCodecContext, btpCodecContext, btpSubProtocolHandlerRegistry, binaryMessageToBtpPacketConverter,
//        btpPacketToBinaryMessageConverter);
//
//    this.wsClient = Objects.requireNonNull(wsClient);
//  }
//
//  /**
//   * Override the plugin-type in the supplied plugin settings.
//   *
//   * @param pluginSettings
//   *
//   * @return
//   */
//  private static final BtpClientPluginSettings setPluginType(final BtpClientPluginSettings pluginSettings) {
//    return BtpClientPluginSettings.builder().from(pluginSettings).pluginType(PLUGIN_TYPE).build();
//  }
//
//
//  @Override
//  public CompletableFuture<Void> doConnectSenderMux() {
//    // Connect and initialize the WebSocketSession...
//    try {
//      this.webSocketSession = Optional.ofNullable(
//          wsClient.doHandshake(
//              new BinaryWebSocketHandler() {
//                @Override
//                protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
//                  // TODO: What does the other side of the Websocket see if there's an exception here?
//                  handleBinaryMessage(session, message).ifPresent(response -> {
//                    try {
//                      session.sendMessage(response);
//                    } catch (IOException e) {
//                      throw new RuntimeException(e);
//                    }
//                  });
//                }
//              },
//              "{scheme}://{localhost}:{port}/btp",
//              getPluginSettings().getRemotePeerScheme(),
//              getPluginSettings().getRemotePeerHostname(),
//              getPluginSettings().getRemotePeerPort())
//              .get()
//      );
//    } catch (Exception e) {
//      this.disconnect().join();
//      throw new RuntimeException(e.getMessage(), e);
//    }
//
//    // AUTH
//    final long requestId = nextRequestId();
//    final Optional<String> authUserName = getPluginSettings().getAuthUsername();
//    final String authToken = getPluginSettings().getSecret();
//    final BtpMessage btpAuthMessage = this.constructAuthMessage(requestId, authToken, authUserName);
//    final BinaryMessage binaryAuthMessage = btpPacketToBinaryMessageConverter.convert(btpAuthMessage);
//    logger.debug(
//        "Websocket Auth BinaryMessage Bytes: {}",
//        BaseEncoding.base16().encode(binaryAuthMessage.getPayload().array())
//    );
//    return this.sendMessageWithPendingRepsonse(requestId, binaryAuthMessage)
//        .thenAccept((response) -> {
//          // Convert to Void response...
//        });
//  }
//
//  @Override
//  public CompletableFuture<Void> doDisconnect() {
//    synchronized (webSocketSession) {
//      // Block on the Disconnect so that only one thread can operate at a time...
//      webSocketSession.ifPresent(webSocketSession -> {
//        try {
//          webSocketSession.close();
//          this.webSocketSession = Optional.empty();
//        } catch (IOException e) {
//          throw new RuntimeException(e);
//        }
//      });
//    }
//    return CompletableFuture.completedFuture(null);
//  }
}