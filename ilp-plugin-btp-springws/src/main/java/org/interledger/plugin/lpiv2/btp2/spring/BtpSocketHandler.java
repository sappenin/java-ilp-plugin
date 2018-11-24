package org.interledger.plugin.lpiv2.btp2.spring;

import org.interledger.btp.BtpPacket;
import org.interledger.btp.BtpResponse;
import org.interledger.btp.BtpSession;
import org.interledger.btp.BtpSessionCredentials;
import org.interledger.btp.BtpSubProtocols;
import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.lpiv2.Plugin;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BinaryMessageToBtpPacketConverter;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BtpConversionException;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BtpPacketToBinaryMessageConverter;
import org.interledger.plugin.lpiv2.btp2.subprotocols.auth.AuthBtpSubprotocolHandler;
import org.interledger.plugin.lpiv2.events.PluginConnectedEvent;
import org.interledger.plugin.lpiv2.events.PluginDisconnectedEvent;
import org.interledger.plugin.lpiv2.events.PluginErrorEvent;
import org.interledger.plugin.lpiv2.events.PluginEventListener;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * <p>An extension of {@link BinaryWebSocketHandler} that translates between Spring's {@link BinaryMessage} into BTP's
 * concrete types, and then delegates to an instance of {@link ServerWebsocketBtpPlugin} based upon a Websocket Session
 * identifier.</p>
 *
 * <p>Note that all messages handled by this handler are processing using BTP, although multiple callers/sessions may
 * exist and be handled by this same handler.</p>
 *
 * @deprecated Will be replaced by {@link BtpBinaryWebSocketHandler}.
 **/
@Deprecated
public class BtpSocketHandler extends BinaryWebSocketHandler implements PluginEventListener {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  // Each Http request will create its own Socket, which WebSockets will continue to use after upgrading the HTTP
  // connection. However, until the BTP auth message is encountered, these WebSocketSessions cannot be connected to an
  // actual plugin. Thus, we store a Map of WebSocketSessions keys by the WebSocketSession identifier.
  private final AuthBtpSubprotocolHandler authBtpSubprotocolHandler;
  private final BinaryMessageToBtpPacketConverter binaryMessageToBtpPacketConverter;
  private final BtpPacketToBinaryMessageConverter btpPacketToBinaryMessageConverter;

  // This BtpSocketHandler allows multiple BTP plugins to use the same WebSocket Server, with each session
  // getting connected to the right plugin only after the BTP Auth subprotocol has completed successfully.
  //
  // Key: auth_username that this plugin requires.
  // Value: The actual ServerWebsocketBtpPlugin
  private Map<String, ServerWebsocketBtpPlugin> registeredServerPlugins;
  // Once the Auth SubProtocol completes, an Account address for a plugin will be added to this set to indicate that this
  // plugin is currently authenticated.
  private Set<InterledgerAddress> authenticatedPluginAccounts;

  /**
   * Required-args Constructor.
   */
  public BtpSocketHandler(
      final AuthBtpSubprotocolHandler authBtpSubprotocolHandler,
      final BinaryMessageToBtpPacketConverter binaryMessageToBtpPacketConverter,
      final BtpPacketToBinaryMessageConverter btpPacketToBinaryMessageConverter
  ) {
    this.authBtpSubprotocolHandler = Objects.requireNonNull(authBtpSubprotocolHandler);
    this.binaryMessageToBtpPacketConverter = Objects.requireNonNull(binaryMessageToBtpPacketConverter);
    this.btpPacketToBinaryMessageConverter = Objects.requireNonNull(btpPacketToBinaryMessageConverter);
    this.registeredServerPlugins = Maps.newConcurrentMap();
  }

  @Override
  public void afterConnectionEstablished(final WebSocketSession session) {
    logger.debug("Incoming WS Client Connection Established: {}", session);

    // Add a new BtpSession into the WebsocketSession...it will be initialized after the `auth` sub_protocol is
    // executed.
    BtpSessionUtils.setBtpSessionIntoWebsocketSession(session, new BtpSession(session.getId()));
  }

  @Override
  public void handleBinaryMessage(final WebSocketSession webSocketSession, final BinaryMessage binaryMessage) {
    // The first message in a WebSocketSession MUST be the auth protocol. Thus, as long as the BtpSession is not
    // authenticated, then we should attempt to perform the auth sub_protocol.
    final BtpSession btpSession = BtpSessionUtils.getBtpSessionFromWebSocketSession(webSocketSession);

    final Optional<BinaryMessage> response;
    if (!btpSession.isAuthenticated()) {
      // Do Auth. This will merely initialize the credentials into the BTPSession and return an Ack.
      response = this.onIncomingBinaryAuthMessage(webSocketSession, binaryMessage);
    } else {
      // The authUsername will not be present in the BtpSession until _after_ the `auth` sub_protocol has completed,
      // so we cannot attempt to resolve a plugin until the caller is authenticated.
      response = btpSession.getBtpSessionCredentials().get()
          .map(BtpSessionCredentials::getAuthUsername)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .map(authUserName -> Optional.ofNullable(this.registeredServerPlugins.get(authUserName))
              .map(plugin -> plugin.onIncomingBinaryMessage(webSocketSession, binaryMessage))
              .orElseThrow(
                  () -> new RuntimeException(String.format("No Plugin found for auth_username `%s`!", authUserName)))
          )
          .orElseThrow(() -> new RuntimeException(
              String.format("BtpSession `%s` was not authenticated (no auth_username found in credentials)",
                  btpSession.getWebsocketSessionId())));
    }

    // Return the response to the caller, if there is a response.
    response.ifPresent($ -> {
      try {
        // TODO: What does the other side of the WebSocket see if there's an exception here?
        webSocketSession.sendMessage($);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    logger.debug("WS Client Connection Closed: {}", session);

    // Attempt to safely disconnect the Plugin for the indicated WebSocketSessionId.
    BtpSessionUtils.getBtpSessionFromWebSocketSession(session)
        .getBtpSessionCredentials().get()
        .map(BtpSessionCredentials::getAuthUsername)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(registeredServerPlugins::get)
        .map(Plugin::disconnect)
        .ifPresent(CompletableFuture::join);
  }

  /**
   * <p>Register a {@link ServerWebsocketBtpPlugin} with this class. The BTP Auth SubProtocol will, if successfully
   * completed, associate a WebSocketSession to a registered plugin using the {@code authUserName} as a correlation
   * key.</p>
   *
   * @param authUserName           A {@link String} that uniquely identifies the principal of a BTP session. This value
   *                               is expected to be passed into the server via the `auth_username` using the BTP Auth
   *                               SubProtocol.
   * @param authenticatedBtpPlugin A {@link ServerWebsocketBtpPlugin} that should correlate to a particular BTP
   *                               Session.
   */
  public void registerPlugin(
      final String authUserName, final ServerWebsocketBtpPlugin authenticatedBtpPlugin
  ) {
    Objects.requireNonNull(authUserName);
    Objects.requireNonNull(authenticatedBtpPlugin);
    this.registeredServerPlugins.put(authUserName, authenticatedBtpPlugin);
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
  public Optional<BinaryMessage> onIncomingBinaryAuthMessage(
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
      final BtpSession btpSession = BtpSessionUtils.getBtpSessionFromWebSocketSession(webSocketSession);
      return authBtpSubprotocolHandler.handleSubprotocolMessage(btpSession, incomingBtpPacket)
          .thenApply(btpSubProtocol -> {
            // If there's no exception, then reaching here means the btp_auth SubProtocol succeeded.
            // Ack the response...
            final BtpSubProtocols responses = new BtpSubProtocols();
            btpSubProtocol.ifPresent(responses::add);
            final BtpResponse btpResponse = BtpResponse.builder()
                .requestId(incomingBtpPacket.getRequestId())
                .subProtocols(responses)
                .build();

            return Optional.ofNullable(btpPacketToBinaryMessageConverter.convert(btpResponse));
          })
          .join();
    } catch (BtpConversionException btpConversionException) {
      logger.error(btpConversionException.getMessage(), btpConversionException);
      throw btpConversionException;
    }
  }

  @Override
  public void onConnect(PluginConnectedEvent event) {
    // If a plugin connects, it means that auth succeeded, so move that plugin into the authenticated Map.
    this.authenticatedPluginAccounts.add(event.getPlugin().getPluginSettings().getPeerAccountAddress());
  }

  @Override
  public void onDisconnect(PluginDisconnectedEvent event) {
    this.authenticatedPluginAccounts.remove(event.getPlugin().getPluginSettings().getPeerAccountAddress());
  }

  @Override
  public void onError(PluginErrorEvent event) {
    this.authenticatedPluginAccounts.remove(event.getPlugin().getPluginSettings().getPeerAccountAddress());
  }
}