package org.interledger.plugin.lpiv2.btp2.subprotocols;

import org.interledger.btp.BtpError;
import org.interledger.btp.BtpMessage;
import org.interledger.btp.BtpPacket;
import org.interledger.btp.BtpPacketMapper;
import org.interledger.btp.BtpResponse;
import org.interledger.btp.BtpRuntimeException;
import org.interledger.btp.BtpSession;
import org.interledger.btp.BtpSubProtocol;
import org.interledger.btp.BtpTransfer;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Handles incoming sub-protocol data from a BTP connection. Sub-protocol data is contained in the data portion of a
 * particular {@link BtpSubProtocol}, as found in {@link BtpMessage#getSubProtocols()}.
 */
public abstract class AbstractBtpSubProtocolHandler {

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

    return new BtpPacketMapper<CompletableFuture<Optional<BtpSubProtocol>>>() {

      @Override
      protected CompletableFuture<Optional<BtpSubProtocol>> mapBtpMessage(final BtpMessage btpMessage) {
        Objects.requireNonNull(btpMessage);

        return AbstractBtpSubProtocolHandler.this.handleSubprotocolDataForBtpMessage(btpSession, btpMessage);
      }

      @Override
      protected CompletableFuture<Optional<BtpSubProtocol>> mapBtpTransfer(final BtpTransfer btpTransfer) {
        Objects.requireNonNull(btpTransfer);

        return AbstractBtpSubProtocolHandler.this.handleSubprotocolDataForBtpTransfer(btpSession, btpTransfer);
      }

      @Override
      protected CompletableFuture<Optional<BtpSubProtocol>> mapBtpError(final BtpError btpError) {
        Objects.requireNonNull(btpError);

        return AbstractBtpSubProtocolHandler.this
            .handleSubprotocolDataForBtpError(btpSession, btpError)
            .thenApply(($) -> Optional.empty());
      }

      @Override
      protected CompletableFuture<Optional<BtpSubProtocol>> mapBtpResponse(final BtpResponse btpResponse) {
        Objects.requireNonNull(btpResponse);

        return AbstractBtpSubProtocolHandler.this
            .handleSubprotocolDataForBtpResponse(btpSession, btpResponse)
            .thenApply(($) -> Optional.empty());
      }
    }.map(incomingBtpPacket);

  }

  /**
   * <p>Handle a primary an incoming {@link BtpMessage}, which is a BTP request from a remote peer.</p>
   *
   * <p>Incoming BTP messages can be either requests or responses, so sub-classes should be careful to handle both
   * types of message, especially when bridging to synchronous API contracts, where it may become confusing as to
   * whether an incoming message corresponds to the input our output portion of a particular synchronous method.</p>
   *
   * @param btpSession         A {@link BtpSession} with information about the current BTP Session.
   * @param incomingBtpMessage A {@link BtpMessage} sent from a remote peer that contains the data for the BTP
   *                           sub-protocol to be handled.
   *
   * @return A {@link BtpSubProtocol} containing properly encoded response data from this handler. Note that per BTP,
   *     only primary sub-protocols should send a response. If a secondary sub-protocol needs a response, a separate BTP
   *     message should be used.
   *
   * @throws BtpRuntimeException If anything goes wrong at the BTP level.
   */
  public abstract CompletableFuture<Optional<BtpSubProtocol>> handleSubprotocolDataForBtpMessage(
      final BtpSession btpSession, final BtpMessage incomingBtpMessage
  ) throws BtpRuntimeException;

  /**
   * <p>Handle a primary an incoming {@link BtpTransfer}, which is a BTP request from a remote peer.</p>
   *
   * <p>Incoming BTP messages can be either requests or responses, so sub-classes should be careful to handle both
   * types of message, especially when bridging to synchronous API contracts, where it may become confusing as to
   * whether an incoming message corresponds to the input our output portion of a particular synchronous method.</p>
   *
   * @param btpSession          A {@link BtpSession} with information about the current BTP Session.
   * @param incomingBtpTransfer A {@link BtpTransfer} that contains the data for the BTP sub-protocol to be handled.
   *
   * @return A {@link BtpSubProtocol} containing properly encoded response data from this handler. Note that per BTP,
   *     only primary sub-protocols should send a response. If a secondary sub-protocol needs a response, a separate BTP
   *     message should be used.
   *
   * @throws BtpRuntimeException If anything goes wrong at the BTP level.
   */
  public abstract CompletableFuture<Optional<BtpSubProtocol>> handleSubprotocolDataForBtpTransfer(
      final BtpSession btpSession, final BtpTransfer incomingBtpTransfer
  ) throws BtpRuntimeException;

  /**
   * <p>Handle a primary an incoming {@link BtpResponse}, which is a BTP response from a remote peer relating to a
   * previously sent request (i.e., a Message or a Transfer).</p>
   *
   * <p>Incoming BTP messages can be either requests or responses, so sub-classes should be careful to handle both
   * types of message, especially when bridging to synchronous API contracts, where it may become confusing as to
   * whether an incoming message corresponds to the input our output portion of a particular synchronous method.</p>
   *
   * @param btpSession          A {@link BtpSession} with information about the current BTP Session.
   * @param incomingBtpResponse A {@link BtpResponse} that contains the data for the BTP sub-protocol to be handled.
   *
   * @return A {@link BtpSubProtocol} containing properly encoded response data from this handler. Note that per BTP,
   *     only primary sub-protocols should send a response. If a secondary sub-protocol needs a response, a separate BTP
   *     message should be used.
   *
   * @throws BtpRuntimeException If anything goes wrong at the BTP level.
   */
  public abstract CompletableFuture<Void> handleSubprotocolDataForBtpResponse(
      final BtpSession btpSession, final BtpResponse incomingBtpResponse
  ) throws BtpRuntimeException;

  /**
   * <p>Handle a primary an incoming {@link BtpError}, which is a BTP error-response from a remote peer relating to a
   * previously sent request (i.e., a Message or a Transfer).</p>
   *
   * <p>Incoming BTP messages can be either requests or responses, so sub-classes should be careful to handle both
   * types of message, especially when bridging to synchronous API contracts, where it may become confusing as to
   * whether an incoming message corresponds to the input our output portion of a particular synchronous method.</p>
   *
   * @param btpSession       A {@link BtpSession} with information about the current BTP Session.
   * @param incomingBtpError A {@link BtpError} that contains the data for the BTP sub-protocol to be handled.
   *
   * @return A {@link BtpSubProtocol} containing properly encoded response data from this handler. Note that per BTP,
   *     only primary sub-protocols should send a response. If a secondary sub-protocol needs a response, a separate BTP
   *     message should be used.
   *
   * @throws BtpRuntimeException If anything goes wrong at the BTP level.
   */
  public abstract CompletableFuture<Void> handleSubprotocolDataForBtpError(
      final BtpSession btpSession, final BtpError incomingBtpError
  ) throws BtpRuntimeException;

}
