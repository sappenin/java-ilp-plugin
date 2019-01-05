package org.interledger.plugin.lpiv2.btp2.spring;

import org.interledger.btp.BtpMessage;
import org.interledger.btp.BtpPacket;
import org.interledger.btp.BtpResponse;
import org.interledger.btp.BtpSession;
import org.interledger.btp.BtpSessionCredentials;
import org.interledger.btp.BtpSubProtocols;
import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.AbstractConnectedPluginsManager;
import org.interledger.plugin.lpiv2.btp2.BtpServerPluginSettings;
import org.interledger.plugin.lpiv2.btp2.ImmutableBtpServerPluginSettings;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BinaryMessageToBtpPacketConverter;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BtpConversionException;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BtpPacketToBinaryMessageConverter;
import org.interledger.plugin.lpiv2.btp2.subprotocols.auth.ServerAuthBtpSubprotocolHandler;
import org.interledger.plugin.lpiv2.events.PluginConnectedEvent;
import org.interledger.plugin.lpiv2.events.PluginDisconnectedEvent;
import org.interledger.plugin.lpiv2.events.PluginErrorEvent;
import org.interledger.plugin.lpiv2.events.PluginEventListener;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * An extension of {@link AbstractConnectedPluginsManager} that supports incoming BTP connections over a single
 * WebSocket.
 */
public class BtpConnectedPluginsManager extends AbstractConnectedPluginsManager
    implements WebSocketHandler, PluginEventListener {

  // Each Http request will create its own Socket, which WebSockets will continue to use after upgrading the HTTP
  // connection. However, until the BTP auth message is encountered, these WebSocketSessions cannot be connected to an
  // actual plugin.

  // The ILP address of the node operating this Manager.
  private final InterledgerAddress operatorAddress;

  private final BtpServerPluginFactory btpServerPluginFactory;

  private final ServerAuthBtpSubprotocolHandler authBtpSubprotocolHandler;
  private final BinaryMessageToBtpPacketConverter binaryMessageToBtpPacketConverter;
  private final BtpPacketToBinaryMessageConverter btpPacketToBinaryMessageConverter;

  public BtpConnectedPluginsManager(
      final InterledgerAddress operatorAddress,
      final BtpServerPluginFactory btpServerPluginFactory,
      final BinaryMessageToBtpPacketConverter binaryMessageToBtpPacketConverter,
      final BtpPacketToBinaryMessageConverter btpPacketToBinaryMessageConverter,
      final ServerAuthBtpSubprotocolHandler authBtpSubprotocolHandler
  ) {
    this.operatorAddress = Objects.requireNonNull(operatorAddress);
    this.btpServerPluginFactory = Objects.requireNonNull(btpServerPluginFactory);
    this.binaryMessageToBtpPacketConverter = Objects.requireNonNull(binaryMessageToBtpPacketConverter);
    this.btpPacketToBinaryMessageConverter = Objects.requireNonNull(btpPacketToBinaryMessageConverter);
    this.authBtpSubprotocolHandler = Objects.requireNonNull(authBtpSubprotocolHandler);
  }

  /**
   * Handle an incoming BinaryMessage from a Websocket by converting it into a {@link BtpMessage} and forwarding it to
   * the appropriate plugin.
   *
   * @param webSocketSession
   * @param incomingBinaryMessage
   *
   * @return A {@link BinaryMessage} that can immediately be returned to the caller (this response will contain
   *     everything required to be eligible as a BTP response), or nothing if the response is {@link Optional#empty()}.
   */
  public void handleBinaryMessage(
      final WebSocketSession webSocketSession, final BinaryMessage incomingBinaryMessage
  ) {
    Objects.requireNonNull(webSocketSession);
    Objects.requireNonNull(incomingBinaryMessage);

    // The first message in a WebSocketSession MUST be the auth protocol. Thus, as long as the BtpSession is _not_
    // authenticated, then we should attempt to perform the auth sub_protocol.
    final BtpSession btpSession = BtpSessionUtils.getBtpSessionFromWebSocketSession(webSocketSession)
        .orElseThrow(() -> new RuntimeException("BtpSession is required!"));

    final Optional<BinaryMessage> response;

    if (!btpSession.isAuthenticated()) {
      // Do Auth. This will merely initialize the credentials into the BTPSession and return an Ack.
      this.handleBinaryAuthMessage(webSocketSession, incomingBinaryMessage)
          .map(foo -> btpPacketToBinaryMessageConverter.convert(foo))
          .ifPresent(authResponse -> {
            // If we get here, it means Authentication succeeded, so send that BTP response back to the caller.
            try {
              webSocketSession.sendMessage(authResponse);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          });
    } else {
      // If the auth subprotocol completes successfully, then we'll end up here with an authenticated Websocket Session,
      // in which case we can simply delegate to a connected plugin....
      final InterledgerAddress accountAddress = btpSession.getAccountAddress()
          .orElseThrow(() -> new RuntimeException("No account in BTP Session!"));
      final BtpServerPlugin plugin = this.getConnectedPlugin(BtpServerPlugin.class, accountAddress)
          .orElseThrow(() -> new RuntimeException("No Plugin registered for Account: " + accountAddress));

      plugin.handleBinaryMessage(webSocketSession, incomingBinaryMessage);
    }
  }

  /**
   * Handle an incoming BinaryMessage from a Websocket, assuming it's a BTP Auth message.
   *
   * @param webSocketSession
   * @param incomingBinaryMessage
   *
   * @return A {@link BinaryMessage} that can immediately be returned to the caller (this response will contain
   *     everything required to be eligible as a BTP response), or nothing if the response is {@link Optional#empty()}.
   */
  public Optional<BtpResponse> handleBinaryAuthMessage(
      final WebSocketSession webSocketSession, final BinaryMessage incomingBinaryMessage
  ) {
    Objects.requireNonNull(webSocketSession);
    Objects.requireNonNull(incomingBinaryMessage);

    // If there's a problem de-serializing the BtpPacket from the BinaryMessage, then close the connection and
    // return empty. This is one of the "tricky cases" as defined in the BTP spec where we don't want to get into
    // an infinite loop.
    final BtpPacket incomingBtpPacket;
    try {
      incomingBtpPacket = this.binaryMessageToBtpPacketConverter.convert(incomingBinaryMessage);
      final BtpSession btpSession = BtpSessionUtils.getBtpSessionFromWebSocketSession(webSocketSession)
          .orElseThrow(() -> new RuntimeException("BtpSession is required!"));
      return authBtpSubprotocolHandler.handleSubprotocolMessage(btpSession, incomingBtpPacket)
          .thenApply(btpSubProtocol -> btpSubProtocol
              .map($ -> {
                // If there's no exception, then reaching here means the btp_auth SubProtocol succeeded.
                // Construct a new BtpServerPlugin and register it with this Manager.

                // Get Auth from BTPSession.
                final InterledgerAddress accountAddress = btpSession.getAccountAddress()
                    .orElseThrow(() -> new RuntimeException("BtpSession not authenticated!"));
                final BtpSessionCredentials sessionCredentials = btpSession.getBtpSessionCredentials()
                    .orElseThrow(() -> new RuntimeException("BtpSession not authenticated!"));

                final BtpServerPluginSettings btpPluginSettings = ImmutableBtpServerPluginSettings.builder()
                    .pluginType(BtpServerPlugin.PLUGIN_TYPE)
                    .operatorAddress(this.operatorAddress)
                    .accountAddress(accountAddress)
                    .authUsername(sessionCredentials.getAuthUsername())
                    .secret(sessionCredentials.getAuthToken())
                    .build();
                final BtpServerPlugin newPlugin = this.btpServerPluginFactory
                    .constructPlugin(BtpServerPlugin.class, btpPluginSettings);

                // Assign the WebSocketSession to the newly-created Plugin...
                newPlugin.setWebSocketSession(webSocketSession);

                // We add the plugin here just to be safe. When plugin.connect is called, the system will attempt to add
                // this plugin again, but this will be a no-op due to the Map function used.
                this.putConnectedPlugin(accountAddress, newPlugin);

                // Ack the response
                final BtpSubProtocols responses = new BtpSubProtocols();
                responses.add($);
                final BtpResponse response = BtpResponse.builder()
                    .requestId(incomingBtpPacket.getRequestId())
                    .subProtocols(responses)
                    .build();
                return response;
              })
              .map(Optional::of)
              // If there is no sub-protocol to return, then return empty so no response is sent back over the connection.
              .orElse(Optional.empty())
          )
          .join();
    } catch (BtpConversionException btpConversionException) {
      logger.error(btpConversionException.getMessage(), btpConversionException);
      throw btpConversionException;
    }
  }

  ///////////////////
  // WebSocketHandler
  ///////////////////

  /**
   * Invoked after WebSocket negotiation has succeeded and the WebSocket connection is opened and ready for use.
   *
   * @param session
   *
   * @throws Exception this method can handle or propagate exceptions; see class-level Javadoc for details.
   */
  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    logger.debug("Incoming WS Client Connection Established: {}", session);

    // Add a new BtpSession into the WebsocketSession...it will be initialized after the `auth` sub_protocol is
    // executed.
    BtpSessionUtils.setBtpSessionIntoWebsocketSession(session, new BtpSession(session.getId()));
  }

  /**
   * Invoked when a new incoming WebSocket message arrives.
   *
   * @param session
   * @param message
   *
   * @throws Exception this method can handle or propagate exceptions; see class-level Javadoc for details.
   */
  @Override
  public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
    if (message instanceof BinaryMessage) {
      this.handleBinaryMessage(session, (BinaryMessage) message);
    } else {
      throw new IllegalStateException("Unexpected WebSocket message type: " + message);
    }
  }

  /**
   * Handle an error from the underlying WebSocket message transport.
   *
   * @param session
   * @param exception
   *
   * @throws Exception this method can handle or propagate exceptions; see class-level Javadoc for details.
   */
  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) {
    // Don't premptively close any connections. It's possible this is just a transient error, and we can wait until the
    // actual connection closes to disconnect this MUX.
    logger.error(exception.getMessage(), exception);
  }

  /**
   * Invoked after the WebSocket connection has been closed by either side, or after a transport error has occurred.
   * Although the session may technically still be open, depending on the underlying implementation, sending messages at
   * this point is discouraged and most likely will not succeed.
   *
   * @param session
   * @param closeStatus
   *
   * @throws Exception this method can handle or propagate exceptions; see class-level Javadoc for details.
   */
  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
    logger.debug("WS Client Connection Closed: {}", session);

    final CompletableFuture[] disconnectFutures = this.getAllConnectedPluginAddresses()
        .map(this::removeConnectedPlugin)
        .collect(Collectors.toList())
        .toArray(new CompletableFuture[0]);

    // Wait up to 30 seconds for all disconnects to occur...
    try {
      CompletableFuture.allOf(disconnectFutures).get(30, TimeUnit.SECONDS);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public boolean supportsPartialMessages() {
    return false;
  }

  //////////////////////////
  // Plugin Event Listener
  //////////////////////////

  @Override
  public void onConnect(final PluginConnectedEvent event) {
    Objects.requireNonNull(event);
    // If a plugin connects, it means that auth succeeded, so move that plugin into the connected Map.
    this.putConnectedPlugin(
        event.getPlugin().getPluginSettings().getAccountAddress(),
        (BtpServerPlugin) event.getPlugin()
    );
  }

  @Override
  public void onDisconnect(final PluginDisconnectedEvent event) {
    Objects.requireNonNull(event);
    // When a plugin disconnects, remove it from this Manager so it no longer accepts calls.
    this.removeConnectedPlugin(event.getPlugin().getPluginSettings().getAccountAddress());
  }

  @Override
  public void onError(final PluginErrorEvent event) {
    Objects.requireNonNull(event);
    logger.error(event.getError().getMessage(), event.getError());
  }
}
