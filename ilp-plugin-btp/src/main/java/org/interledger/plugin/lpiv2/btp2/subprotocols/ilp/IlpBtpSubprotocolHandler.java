package org.interledger.plugin.lpiv2.btp2.subprotocols.ilp;

import org.interledger.btp.BtpError;
import org.interledger.btp.BtpErrorCode;
import org.interledger.btp.BtpMessage;
import org.interledger.btp.BtpResponse;
import org.interledger.btp.BtpRuntimeException;
import org.interledger.btp.BtpSession;
import org.interledger.btp.BtpSubProtocol;
import org.interledger.btp.BtpSubProtocols;
import org.interledger.btp.BtpTransfer;
import org.interledger.core.InterledgerAddress;
import org.interledger.core.InterledgerPacket;
import org.interledger.core.InterledgerPreparePacket;
import org.interledger.core.InterledgerResponsePacket;
import org.interledger.core.asn.framework.InterledgerCodecContextFactory;
import org.interledger.encoding.asn.framework.CodecContext;
import org.interledger.plugin.DataHandler;
import org.interledger.plugin.lpiv2.Plugin;
import org.interledger.plugin.lpiv2.btp2.subprotocols.AbstractBtpSubProtocolHandler;
import org.interledger.plugin.lpiv2.exceptions.DataHandlerAlreadyRegisteredException;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An extension of {@link AbstractBtpSubProtocolHandler} for handling incoming <tt>ILP</tt> sub-protocol messages
 * received over BTP. This implementation essentially converts from an incoming BTP message into an ILPv4 primitive and
 * exposes methods that can be implemented to handle these messages at the ILP-layer.
 */
public class IlpBtpSubprotocolHandler extends AbstractBtpSubProtocolHandler {

  private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

  private final CodecContext ilpCodecContext;

  // To avoid circular-dependencies, this handler MAY need to be set _after_ the Connector server has started...
  private final AtomicReference<DataHandler> dataHandlerAtomicReference;

  /**
   * No-args Constructor.
   */
  public IlpBtpSubprotocolHandler() {
    this(InterledgerCodecContextFactory.oer());
  }

  /**
   * Required-args Constructor.
   *
   * @param ilpCodecContext A {@link CodecContext} that can handle encoding/decoding of ILP Packets.
   */
  public IlpBtpSubprotocolHandler(final CodecContext ilpCodecContext) {
    this(ilpCodecContext, new AtomicReference<>());
  }

  /**
   * Required-args Constructor.
   *
   * @param ilpCodecContext            A {@link CodecContext} that can handle encoding/decoding of ILP Packets.
   * @param dataHandlerAtomicReference An {@link AtomicReference} containting an optionally-present instance of  {@link
   *                                   DataHandler} that actually handles incoming ILP prepare packets.
   */
  public IlpBtpSubprotocolHandler(
      final CodecContext ilpCodecContext, final AtomicReference<DataHandler> dataHandlerAtomicReference
  ) {
    this.ilpCodecContext = Objects.requireNonNull(ilpCodecContext);
    this.dataHandlerAtomicReference = Objects.requireNonNull(dataHandlerAtomicReference);
  }

  /**
   * Construct a {@link BtpSubProtocol} instance for using the <tt>ILP</tt> packet data.
   *
   * @param ilpPacket       An {@link InterledgerPacket} that can be a prepare, fulfill, or error packet.
   * @param ilpCodecContext A {@link CodecContext} that can operate on ILP primitives.
   *
   * @return A {@link BtpSubProtocol}
   *
   * @deprecated Replace with {@link IlpBtpConverter}
   */
  @Deprecated
  public static BtpSubProtocol toBtpSubprotocol(
      final InterledgerPacket ilpPacket, final CodecContext ilpCodecContext
  ) {
    Objects.requireNonNull(ilpPacket);
    Objects.requireNonNull(ilpCodecContext);
    try {
      final ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ilpCodecContext.write(ilpPacket, baos);

      return BtpSubProtocol.builder()
          .protocolName(BtpSubProtocols.INTERLEDGER)
          .contentType(BtpSubProtocol.ContentType.MIME_APPLICATION_OCTET_STREAM)
          .data(baos.toByteArray())
          .build();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Construct an {@link InterledgerPacket} using data in the supplied BTP packet.
   *
   * @param btpResponse     A {@link BtpResponse} containing fulfill or reject packet.
   * @param ilpCodecContext A {@link CodecContext} that can read ILP Packets.
   *
   * @return An newly constructed {@link InterledgerPacket}.
   *
   * @deprecated Replace with {@link IlpBtpConverter}
   */
  @Deprecated
  public static InterledgerResponsePacket toIlpPacket(
      BtpResponse btpResponse, final CodecContext ilpCodecContext
  ) {
    Objects.requireNonNull(btpResponse);
    Objects.requireNonNull(ilpCodecContext);

    try {
      final ByteArrayInputStream inputStream =
          new ByteArrayInputStream(btpResponse.getPrimarySubProtocol().getData());
      return ilpCodecContext.read(InterledgerResponsePacket.class, inputStream);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public CompletableFuture<Optional<BtpSubProtocol>> handleSubprotocolDataForBtpMessage(
      final BtpSession btpSession, final BtpMessage incomingBtpMessage
  ) {
    Objects.requireNonNull(btpSession, "btpSession must not be null!");
    Objects.requireNonNull(incomingBtpMessage, "incomingBtpMessage must not be null!");

    if (logger.isDebugEnabled()) {
      logger.debug("Incoming ILP Subprotocol BtpMessage: {}", incomingBtpMessage);
    }

    Preconditions.checkArgument(dataHandlerAtomicReference.get() != null,
        "ilpPluginDataHandler must be set before using this handler!");

    // Throws if there's an auth problem.
    if (!btpSession.isAuthenticated()) {
      throw new BtpRuntimeException(BtpErrorCode.F00_NotAcceptedError, "BtpSession not authenticated!");
    }

    try {
      // Convert to an ILP Prepare packet.
      final InterledgerPacket incomingIlpPacket = ilpCodecContext
          .read(InterledgerPacket.class,
              new ByteArrayInputStream(incomingBtpMessage.getPrimarySubProtocol().getData()));

      // If the Packet is a prepare packet, then we forward to the ilpPlugin Data Handler which bridges to the software
      // that is listening for incoming ILP packet (typically this is the ILP Connector Switch, but might be client software).
      // This mapper should never encounter a Fulfill/Reject packet in this way. This is because when a Prepare packet
      // is sent out through a BTP session, a CompletableFuture is always constructed to accept the response and return it
      // to the original caller. Thus, if a Fulfill/Reject packet makes it into this location, it's an error.
      if (InterledgerPreparePacket.class.isAssignableFrom(incomingIlpPacket.getClass())) {
        return dataHandlerAtomicReference.get()
            // Handle the incoming ILP Data packet.
            .handleIncomingData(
                //sourceAccountAddress,
                (InterledgerPreparePacket) incomingIlpPacket)
            // and convert back to BTP...
            .thenApply((responsePacket) -> responsePacket
                .map($ -> IlpBtpSubprotocolHandler.toBtpSubprotocol($, ilpCodecContext))
                .map(Optional::ofNullable)
                .orElseGet(Optional::empty)
            );
      } else {
        logger.error("Encountered errant InterledgerResponsePacket but should not have: {}", incomingIlpPacket);
        throw new RuntimeException(
            String.format("Unsupported InterledgerPacket Type: %s", incomingIlpPacket.getClass()));
      }
    } catch (IOException e) {
      // Per RFC-23, ILP packets are attached under the protocol name "ilp" with content-type
      // "application/octet-stream". If an unreadable BTP packet is received, no response should be sent. An unreadable
      // BTP packet is one which is structurally invalid, i.e. terminates before length prefixes dictate or contains
      // illegal characters.
      logger.error("Unable to process incoming incomingBtpMessage as an InterledgerPacket: {}", incomingBtpMessage);
      return CompletableFuture.completedFuture(Optional.empty());
    }
  }

  @Override
  public CompletableFuture<Optional<BtpSubProtocol>> handleSubprotocolDataForBtpTransfer(
      final BtpSession btpSession, final BtpTransfer incomingBtpTransfer
  ) throws BtpRuntimeException {
    Objects.requireNonNull(btpSession);
    Objects.requireNonNull(incomingBtpTransfer);

    logger.debug("Incoming ILP Subprotocol BtpTransfer: {}", incomingBtpTransfer);
    return CompletableFuture.completedFuture(Optional.empty());
  }

  @Override
  public CompletableFuture<Void> handleSubprotocolDataForBtpResponse(
      final BtpSession btpSession, final BtpResponse incomingBtpResponse
  ) throws BtpRuntimeException {

    Objects.requireNonNull(btpSession);
    Objects.requireNonNull(incomingBtpResponse);

    logger.debug("Incoming ILP Subprotocol BtpResponse: {}", incomingBtpResponse);
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> handleSubprotocolDataForBtpError(
      final BtpSession btpSession, final BtpError incomingBtpError
  ) throws BtpRuntimeException {

    Objects.requireNonNull(btpSession);
    Objects.requireNonNull(incomingBtpError);

    logger.error("Incoming ILP Subprotocol BtpError: {}", incomingBtpError);
    return CompletableFuture.completedFuture(null);
  }

  /**
   * <p>Set the callback which is used to handle incoming prepared data packets. The handler should expect one
   * parameter (an ILP Prepare Packet) and return a CompletableFuture for the resulting response. If an error occurs,
   * the callback MAY throw an exception. In general, the callback should behave as {@link
   * Plugin#getDataSender#sendData(InterledgerPreparePacket)} does.</p>
   *
   * <p>If a data handler is already set, this method throws a {@link DataHandlerAlreadyRegisteredException}. In order
   * to change the data handler, the old handler must first be removed via {@link #unregisterDataHandler()}. This is to
   * ensure that handlers are not overwritten by accident.</p>
   *
   * <p>If an incoming packet is received by the plugin, but no handler is registered, the plugin SHOULD respond with
   * an error.</p>
   *
   * @param operatorAddress The {@link InterledgerAddress} of the node operating this handler.
   * @param dataHandler     An instance of {@link DataHandler}.
   */
  public void registerDataHandler(final InterledgerAddress operatorAddress, final DataHandler dataHandler)
      throws DataHandlerAlreadyRegisteredException {

    Objects.requireNonNull(operatorAddress, "operatorAddress must not be null!");
    Objects.requireNonNull(dataHandler, "dataHandler must not be null!");
    if (!this.dataHandlerAtomicReference.compareAndSet(null, dataHandler)) {
      throw new DataHandlerAlreadyRegisteredException(
          "DataHandler may not be registered twice. Call unregisterDataHandler first!", operatorAddress
      );
    }
  }

  /**
   * Accessor for the currently registered {@link DataHandler}. Throws a {@link RuntimeException} if no handler is
   * registered, because callers should not be trying to access the handler if none is registered (in other words, a
   * Plugin is not in a valid state until it has handlers registered).
   *
   * @return The currently registered {@link DataHandler}.
   *
   * @throws {@link RuntimeException} if no handler is registered.
   */
  public DataHandler getDataHandler() {
    final DataHandler handler = this.dataHandlerAtomicReference.get();
    if (handler == null) {
      throw new RuntimeException("DataHandler MUST be registered before being accessed!");
    } else {
      return handler;
    }
  }

  /**
   * Removes the currently used {@link DataHandler}. This has the same effect as if {@link
   * #registerDataHandler(InterledgerAddress, DataHandler)} had never been called. If no data handler is currently set,
   * this method does nothing.
   */
  public void unregisterDataHandler() {
    this.dataHandlerAtomicReference.set(null);
  }
}
