package org.interledger.plugin.lpiv2.btp2.spring;

import org.interledger.btp.BtpSession;

import org.springframework.web.socket.WebSocketSession;

import java.util.Objects;
import java.util.Optional;

public class BtpSessionUtils {

  public static final String BTP_SESSION = "btp-session";

  /**
   * Accessor for the BTP Session, if it exists.
   *
   * @return
   */
  public static Optional<BtpSession> getBtpSessionFromWebSocketSession(final WebSocketSession webSocketSession) {
    Objects.requireNonNull(webSocketSession);

    // It is an error if no BtpSession exists in a WebSocketSession!
    return Optional.ofNullable(webSocketSession.getAttributes().get(BTP_SESSION))
        .map($ -> (BtpSession) $);
  }

  /**
   * Associate a {@link BtpSession} with the supplied {@link WebSocketSession}.
   *
   * @return the previous value associated with <tt>webSocketSession</tt>, or {@link Optional#empty()} if there was no
   *     mapping for <tt>webSocketSession</tt>.
   */
  public static Optional<BtpSession> setBtpSessionIntoWebsocketSession(
      final WebSocketSession webSocketSession, final BtpSession btpSession
  ) {
    return Optional.ofNullable((BtpSession) webSocketSession.getAttributes().put(BTP_SESSION, btpSession));
  }

  public static boolean isPresent(final WebSocketSession webSocketSession) {
    return getBtpSessionFromWebSocketSession(webSocketSession).isPresent();
  }
}
