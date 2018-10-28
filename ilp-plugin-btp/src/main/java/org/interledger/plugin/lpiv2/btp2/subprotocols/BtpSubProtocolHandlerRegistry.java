package org.interledger.plugin.lpiv2.btp2.subprotocols;

import org.interledger.btp.BtpSubProtocol;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Holds BTP Sub-protocol filters for this Connector.
 */
public class BtpSubProtocolHandlerRegistry {

  public static final String BTP_SUB_PROTOCOL_ILP = "ilp";
  public static final String BTP_SUB_PROTOCOL_AUTH = "auth";
  public static final String BTP_SUB_PROTOCOL_AUTH_TOKEN = "auth_token";
  public static final String BTP_SUB_PROTOCOL_AUTH_USERNAME = "auth_username";

  private Map<String, AbstractBtpSubProtocolHandler> handlers = new HashMap<>();

  /**
   * Accessor for the BTP Handler identified by {@code subProtocolName}.
   *
   * @param subProtocolName A {@link String} that uniquely identifies the handler being added.
   *
   * @return A {@link AbstractBtpSubProtocolHandler} corresponding to the supplied sub-protocol name.
   */
  public Optional<AbstractBtpSubProtocolHandler> getHandler(final String subProtocolName) {
    Objects.requireNonNull(subProtocolName, "subProtocolName must not be null!");
    return Optional.ofNullable(handlers.get(subProtocolName));
  }

  /**
   * Add a subprotocol handler to this registry.
   *
   * @param subProtocolName A {@link String} that uniquely identifies the handler being added.
   * @param handler         A {@link AbstractBtpSubProtocolHandler} to handle a BTP sub-protocol.
   *
   * @return The added {@link BtpSubProtocol}.
   */
  public AbstractBtpSubProtocolHandler putHandler(final String subProtocolName, final AbstractBtpSubProtocolHandler handler) {
    Objects.requireNonNull(subProtocolName, "subProtocolName must not be null!");
    Objects.requireNonNull(handler, "handler must not be null!");
    return handlers.put(subProtocolName, handler);
  }
}
