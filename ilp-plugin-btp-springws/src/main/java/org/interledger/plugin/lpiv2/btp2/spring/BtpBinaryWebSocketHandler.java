package org.interledger.plugin.lpiv2.btp2.spring;

import org.interledger.btp.BtpPacket;
import org.interledger.btp.BtpResponse;
import org.interledger.btp.BtpSession;
import org.interledger.btp.BtpSessionCredentials;
import org.interledger.btp.BtpSubProtocols;
import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.link.mux.PluginMux;
import org.interledger.plugin.lpiv2.Plugin;
import org.interledger.plugin.lpiv2.btp2.BtpServerPluginSettings;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BinaryMessageToBtpPacketConverter;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BtpConversionException;
import org.interledger.plugin.lpiv2.btp2.spring.converters.BtpPacketToBinaryMessageConverter;
import org.interledger.plugin.lpiv2.btp2.subprotocols.auth.AuthBtpSubprotocolHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * An extension of {@link BinaryWebSocketHandler} that handles BTP messages for a single BTP connection.
 */
public class BtpBinaryWebSocketHandler extends BinaryWebSocketHandler {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final AuthBtpSubprotocolHandler authBtpSubprotocolHandler;
  private final BinaryMessageToBtpPacketConverter binaryMessageToBtpPacketConverter;
  private final BtpPacketToBinaryMessageConverter btpPacketToBinaryMessageConverter;

  // All traffic is forwarded into and out of the MUX.
  private final PluginMux<AbstractWebsocketBtpPlugin<? extends BtpServerPluginSettings>> pluginMux;

  /**
   * Required-args Constructor.
   */
  public BtpBinaryWebSocketHandler(
      final InterledgerAddress operatorAddress,
      final AuthBtpSubprotocolHandler authBtpSubprotocolHandler,
      final BinaryMessageToBtpPacketConverter binaryMessageToBtpPacketConverter,
      final BtpPacketToBinaryMessageConverter btpPacketToBinaryMessageConverter
  ) {
    this.authBtpSubprotocolHandler = Objects.requireNonNull(authBtpSubprotocolHandler);
    this.binaryMessageToBtpPacketConverter = Objects.requireNonNull(binaryMessageToBtpPacketConverter);
    this.btpPacketToBinaryMessageConverter = Objects.requireNonNull(btpPacketToBinaryMessageConverter);
    this.pluginMux = new BtpPluginMux(operatorAddress);
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
      response = this.onIncomingBinaryBtpAuthMessage(webSocketSession, binaryMessage);
    } else {
      final InterledgerAddress authUserName = btpSession.getBtpSessionCredentials().get()
          .map(BtpSessionCredentials::getAuthUsername)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .map(InterledgerAddress::of)
          .orElseThrow(() -> new RuntimeException(String
              .format("BtpSession `%s` was not authenticated (no auth_username found in credentials)",
                  btpSession.getWebsocketSessionId())));

      response = this.pluginMux.getPlugin(authUserName)
          .map(plugin -> plugin.onIncomingBinaryMessage(webSocketSession, binaryMessage))
          .orElseThrow(() -> new RuntimeException(
              String.format("No Plugin found for auth_username `%s`!", authUserName.getValue()))
          );
    }

    //////////////////////
    // Return the BinaryResponse to the caller, if there is a response.
    //////////////////////
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

    // Attempt to safely disconnect the Plugin in the MUX for the indicated WebSocketSessionId.
    BtpSessionUtils.getBtpSessionFromWebSocketSession(session)
        .getBtpSessionCredentials().get()
        .map(BtpSessionCredentials::getAuthUsername)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(InterledgerAddress::of)
        .map(pluginMux::getPlugin)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(Plugin::disconnect)
        .ifPresent(CompletableFuture::join);
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
  public Optional<BinaryMessage> onIncomingBinaryBtpAuthMessage(
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

      // TODO: After a successful Auth, consider just adding a new WebsocketBtpPlugin to the MUX.

    } catch (BtpConversionException btpConversionException) {
      logger.error(btpConversionException.getMessage(), btpConversionException);
      throw btpConversionException;
    }
  }

  /**
   * Accessor for the plugin multiplexer that a plugin can be registered with.
   *
   * @return
   */
  public PluginMux<AbstractWebsocketBtpPlugin<? extends BtpServerPluginSettings>> getPluginMux() {
    return this.pluginMux;
  }
}
