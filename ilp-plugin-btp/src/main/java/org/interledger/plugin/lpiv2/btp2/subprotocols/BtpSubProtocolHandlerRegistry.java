package org.interledger.plugin.lpiv2.btp2.subprotocols;

import org.interledger.btp.BtpSubProtocol;
import org.interledger.btp.BtpSubProtocol.ContentType;

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

  private Map<String, AbstractBtpSubProtocolHandler> jsonHandlers = new HashMap<>();
  private Map<String, AbstractBtpSubProtocolHandler> textHandlers = new HashMap<>();
  private Map<String, AbstractBtpSubProtocolHandler> octetStreamHandlers = new HashMap<>();

  /**
   * Accessor for the BTP Handler identified by {@code subProtocolName}.
   *
   * @param subProtocolName A {@link String} that uniquely identifies the handler being added.
   *
   * @return A {@link AbstractBtpSubProtocolHandler} corresponding to the supplied sub-protocol name.
   */
//  public Optional<AbstractBtpSubProtocolHandler> getHandler(final String subProtocolName) {
//    Objects.requireNonNull(subProtocolName, "subProtocolName must not be null!");
//    return this.getHandler(subProtocolName, ContentType.MIME_APPLICATION_OCTET_STREAM);
//  }

  /**
   * Accessor for the BTP Handler identified by {@code subProtocolName}.
   *
   * @param subProtocolName A {@link String} that uniquely identifies the handler being added.
   * @param contentType     The {@link ContentType} of the handler to return.
   *
   * @return A {@link AbstractBtpSubProtocolHandler} corresponding to the supplied sub-protocol name.
   */
  public Optional<AbstractBtpSubProtocolHandler> getHandler(
      final String subProtocolName, final ContentType contentType
  ) {
    Objects.requireNonNull(subProtocolName, "subProtocolName must not be null!");
    Objects.requireNonNull(subProtocolName, "contentType must not be null!");

    switch (contentType) {
      case MIME_TEXT_PLAIN_UTF8: {
        return Optional.ofNullable(textHandlers.get(subProtocolName));
      }

      case MIME_APPLICATION_JSON: {
        return Optional.ofNullable(jsonHandlers.get(subProtocolName));
      }

      case MIME_APPLICATION_OCTET_STREAM: {
        return Optional.ofNullable(octetStreamHandlers.get(subProtocolName));
      }
      default: {
        throw new RuntimeException("Unsupported ContextType: " + contentType);
      }
    }
  }

  /**
   * Add a subprotocol handler to this registry.
   *
   * @param subProtocolName A {@link String} that uniquely identifies the handler being added.
   * @param contentType     The {@link ContentType} that the supplied {@code handler} can handle.
   * @param handler         A {@link AbstractBtpSubProtocolHandler} to handle a BTP sub-protocol.
   *
   * @return The added {@link BtpSubProtocol}.
   */
  public AbstractBtpSubProtocolHandler putHandler(
      final String subProtocolName,
      final ContentType contentType,
      final AbstractBtpSubProtocolHandler handler
  ) {
    Objects.requireNonNull(subProtocolName, "subProtocolName must not be null!");
    Objects.requireNonNull(contentType, "contentType must not be null!");
    Objects.requireNonNull(handler, "handler must not be null!");

    switch (contentType) {
      case MIME_TEXT_PLAIN_UTF8: {
        return textHandlers.put(subProtocolName, handler);
      }
      case MIME_APPLICATION_JSON: {
        return jsonHandlers.put(subProtocolName, handler);
      }

      case MIME_APPLICATION_OCTET_STREAM: {
        return octetStreamHandlers.put(subProtocolName, handler);
      }
      default: {
        throw new RuntimeException("Unsupported ContextType: " + contentType);
      }
    }
  }
}
