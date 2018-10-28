package org.interledger.plugin.lpiv2.btp2.subprotocols;

import org.interledger.btp.BtpError;
import org.interledger.btp.BtpMessage;
import org.interledger.btp.BtpPacket;
import org.interledger.btp.BtpPacketHandler;
import org.interledger.btp.BtpResponse;
import org.interledger.btp.BtpRuntimeException;
import org.interledger.btp.BtpSession;
import org.interledger.btp.BtpSubProtocol;
import org.interledger.btp.BtpSubProtocol.ContentType;
import org.interledger.btp.BtpTransfer;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Handles incoming sub-protocol data from a BTP connection. Sub-protocol data is contained in the data portion of a
 * particular {@link BtpSubProtocol}, as found in {@link BtpMessage#getSubProtocols()}.
 *
 * @param <T> The type of object contained in the BTP Subprotocol payload.
 */
public abstract class AbstractBtpSubProtocolHandler<T> {

  /**
   * <p>Handle a primary {@link BtpSubProtocol} whose data payload should be treated as binary data.</p>
   *
   * <p>Incoming BTP messages can be either requests or responses, so sub-classes should be careful to handle both
   * types of message, especially when bridging to synchronous API contracts, where it may become confusing as to
   * whether an incoming message corresponds to the input our output portion of a particular synchronous method.</p>
   *
   * @param btpSession        A {@link BtpSession} with information about the current BTP Session.
   * @param incomingBtpPacket A {@link BtpPacket} that contains the data for the BTP sub-protocol to be handled.
   *
   * @return A {@link BtpSubProtocol} containing properly encoded response data from this handler. Note that per BTP,
   *     only primary sub-protocols should send a response. If a secondary sub-protocol needs a response, a separate BTP
   *     message should be used.
   *
   * @throws BtpRuntimeException If anything goes wrong at the BTP level.
   */
  public CompletableFuture<Optional<BtpSubProtocol>> handleSubprotocolMessage(
      BtpSession btpSession, BtpPacket incomingBtpPacket
  ) throws BtpRuntimeException {

    Objects.requireNonNull(btpSession);
    Objects.requireNonNull(incomingBtpPacket);

    return new BtpPacketHandler<CompletableFuture<Optional<BtpSubProtocol>>>() {

      @Override
      protected CompletableFuture<Optional<BtpSubProtocol>> handleBtpMessage(final BtpMessage btpMessage) {
        Objects.requireNonNull(btpMessage);

        final T decodedSubProtocolData = decodeBtpSubProtocols(
            btpMessage.getPrimarySubProtocol().getContentType(), btpMessage.getPrimarySubProtocol().getData()
        );

        return AbstractBtpSubProtocolHandler.this.handleSubprotocolDataForBtpMessage(btpSession, decodedSubProtocolData)
            .thenApply(Optional::of);
      }

      @Override
      protected CompletableFuture<Optional<BtpSubProtocol>> handleBtpTransfer(final BtpTransfer btpTransfer) {
        Objects.requireNonNull(btpTransfer);

        final T decodedSubProtocolData = decodeBtpSubProtocols(
            btpTransfer.getPrimarySubProtocol().getContentType(), btpTransfer.getPrimarySubProtocol().getData()
        );

        return AbstractBtpSubProtocolHandler.this.handleBtpTransfer(btpSession, decodedSubProtocolData)
            .thenApply(Optional::of);
      }

      @Override
      protected CompletableFuture<Optional<BtpSubProtocol>> handleBtpError(final BtpError btpError) {
        Objects.requireNonNull(btpError);

        final T decodedSubProtocolData = decodeBtpSubProtocols(
            btpError.getPrimarySubProtocol().getContentType(), btpError.getPrimarySubProtocol().getData()
        );

        AbstractBtpSubProtocolHandler.this.handleBtpError(btpSession, decodedSubProtocolData);

        return CompletableFuture.completedFuture(Optional.empty());
      }

      @Override
      protected CompletableFuture<Optional<BtpSubProtocol>> handleBtpResponse(final BtpResponse btpResponse) {
        Objects.requireNonNull(btpResponse);

        final T decodedSubProtocolData = decodeBtpSubProtocols(
            btpResponse.getPrimarySubProtocol().getContentType(), btpResponse.getPrimarySubProtocol().getData()
        );

        AbstractBtpSubProtocolHandler.this.handleBtpResponse(btpSession, decodedSubProtocolData);
        return CompletableFuture.completedFuture(Optional.empty());
      }
    }.handle(incomingBtpPacket);

  }

  /**
   * <p>Handle a primary an incoming {@link BtpMessage}, which is a BTP request from a remote peer.</p>
   *
   * <p>Incoming BTP messages can be either requests or responses, so sub-classes should be careful to handle both
   * types of message, especially when bridging to synchronous API contracts, where it may become confusing as to
   * whether an incoming message corresponds to the input our output portion of a particular synchronous method.</p>
   *
   * @param btpSession      A {@link BtpSession} with information about the current BTP Session.
   * @param subProtocolData A {@link T} that contains the data for the BTP sub-protocol to be handled inside of a {@link
   *                        BtpMessage}.
   *
   * @return A {@link BtpSubProtocol} containing properly encoded response data from this handler. Note that per BTP,
   *     only primary sub-protocols should send a response. If a secondary sub-protocol needs a response, a separate BTP
   *     message should be used.
   *
   * @throws BtpRuntimeException If anything goes wrong at the BTP level.
   */
  public abstract CompletableFuture<BtpSubProtocol> handleSubprotocolDataForBtpMessage(
      final BtpSession btpSession, final T subProtocolData
  ) throws BtpRuntimeException;

  /**
   * <p>Handle a primary an incoming {@link BtpTransfer}, which is a BTP request from a remote peer.</p>
   *
   * <p>Incoming BTP messages can be either requests or responses, so sub-classes should be careful to handle both
   * types of message, especially when bridging to synchronous API contracts, where it may become confusing as to
   * whether an incoming message corresponds to the input our output portion of a particular synchronous method.</p>
   *
   * @param btpSession      A {@link BtpSession} with information about the current BTP Session.
   * @param subProtocolData A {@link T} that contains the data for the BTP sub-protocol to be handled inside of a {@link
   *                        BtpTransfer}.
   *
   * @return A {@link BtpSubProtocol} containing properly encoded response data from this handler. Note that per BTP,
   *     only primary sub-protocols should send a response. If a secondary sub-protocol needs a response, a separate BTP
   *     message should be used.
   *
   * @throws BtpRuntimeException If anything goes wrong at the BTP level.
   */
  public abstract CompletableFuture<BtpSubProtocol> handleBtpTransfer(
      final BtpSession btpSession, final T subProtocolData
  ) throws BtpRuntimeException;

  /**
   * <p>Handle a primary an incoming {@link BtpResponse}, which is a BTP response from a remote peer relating to a
   * previously sent request (i.e., a Message or a Transfer).</p>
   *
   * <p>Incoming BTP messages can be either requests or responses, so sub-classes should be careful to handle both
   * types of message, especially when bridging to synchronous API contracts, where it may become confusing as to
   * whether an incoming message corresponds to the input our output portion of a particular synchronous method.</p>
   *
   * @param btpSession      A {@link BtpSession} with information about the current BTP Session.
   * @param subProtocolData A {@link T} that contains the data for the BTP sub-protocol to be handled inside of a {@link
   *                        BtpTransfer}.
   *
   * @return A {@link BtpSubProtocol} containing properly encoded response data from this handler. Note that per BTP,
   *     only primary sub-protocols should send a response. If a secondary sub-protocol needs a response, a separate BTP
   *     message should be used.
   *
   * @throws BtpRuntimeException If anything goes wrong at the BTP level.
   */
  public abstract void handleBtpResponse(
      final BtpSession btpSession, final T subProtocolData
  ) throws BtpRuntimeException;

  /**
   * <p>Handle a primary an incoming {@link BtpError}, which is a BTP error-response from a remote peer relating to a
   * previously sent request (i.e., a Message or a Transfer).</p>
   *
   * <p>Incoming BTP messages can be either requests or responses, so sub-classes should be careful to handle both
   * types of message, especially when bridging to synchronous API contracts, where it may become confusing as to
   * whether an incoming message corresponds to the input our output portion of a particular synchronous method.</p>
   *
   * @param btpSession      A {@link BtpSession} with information about the current BTP Session.
   * @param subProtocolData A {@link T} that contains the data for the BTP sub-protocol to be handled inside of a {@link
   *                        BtpTransfer}.
   *
   * @return A {@link BtpSubProtocol} containing properly encoded response data from this handler. Note that per BTP,
   *     only primary sub-protocols should send a response. If a secondary sub-protocol needs a response, a separate BTP
   *     message should be used.
   *
   * @throws BtpRuntimeException If anything goes wrong at the BTP level.
   */
  public abstract void handleBtpError(
      final BtpSession btpSession, final T subProtocolData
  ) throws BtpRuntimeException;


  public T decodeBtpSubProtocols(
      final ContentType btpSubProtocolContentType, final byte[] btpSubProtocolData
  ) {
    Objects.requireNonNull(btpSubProtocolContentType);
    Objects.requireNonNull(btpSubProtocolData);

    switch (btpSubProtocolContentType) {
      case MIME_TEXT_PLAIN_UTF8: {
        return this.decodeBtpSubProtocolDataFromText(btpSubProtocolData);
      }

      case MIME_APPLICATION_JSON: {
        return this.decodeBtpSubProtocolDataFromJson(btpSubProtocolData);
      }

      case MIME_APPLICATION_OCTET_STREAM: {
        return this.decodeBtpSubProtocolDataFromOctetStream(btpSubProtocolData);
      }
      default: {
        throw new RuntimeException("Unsupported ContentType: " + btpSubProtocolContentType);
      }
    }
  }

  protected abstract T decodeBtpSubProtocolDataFromText(byte[] btpSubProtocolData);

  protected abstract T decodeBtpSubProtocolDataFromJson(byte[] btpSubProtocolData);

  protected abstract T decodeBtpSubProtocolDataFromOctetStream(byte[] btpSubProtocolData);


}
