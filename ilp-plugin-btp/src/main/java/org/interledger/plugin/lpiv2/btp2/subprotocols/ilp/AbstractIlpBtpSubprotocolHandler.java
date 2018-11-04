package org.interledger.plugin.lpiv2.btp2.subprotocols.ilp;

import org.interledger.btp.BtpError;
import org.interledger.btp.BtpMessage;
import org.interledger.btp.BtpResponse;
import org.interledger.btp.BtpRuntimeException;
import org.interledger.btp.BtpSession;
import org.interledger.btp.BtpSubProtocol;
import org.interledger.btp.BtpSubProtocols;
import org.interledger.btp.BtpTransfer;
import org.interledger.core.InterledgerAddress;
import org.interledger.core.InterledgerPacket;
import org.interledger.core.InterledgerProtocolException;
import org.interledger.encoding.asn.framework.CodecContext;
import org.interledger.plugin.lpiv2.btp2.subprotocols.AbstractBtpSubProtocolHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * An extension of {@link AbstractBtpSubProtocolHandler} for handling incoming <tt>ILP</tt> sub-protocol messages
 * received over BTP. This implementation essentially converts from an incoming BTP message into an ILPv4 primitive and
 * exposes methods that can be implemented to handle these messages at the ILP-layer.
 */
public abstract class AbstractIlpBtpSubprotocolHandler extends AbstractBtpSubProtocolHandler {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

  private final CodecContext ilpCodecContext;

  /**
   * Required-args Constructor.
   *
   * @param ilpCodecContext A {@link CodecContext} that can handle encoding/decoding of ILP Packets.
   */
  public AbstractIlpBtpSubprotocolHandler(final CodecContext ilpCodecContext) {
    this.ilpCodecContext = Objects.requireNonNull(ilpCodecContext);
  }

  /**
   * Construct a {@link BtpSubProtocol} instance for using the <tt>ILP</tt> packet data.
   *
   * @param ilpPacket       An {@link InterledgerPacket} that can be a prepare, fulfill, or error packet.
   * @param ilpCodecContext
   *
   * @return A {@link BtpSubProtocol}
   */
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
   */
  public static InterledgerPacket toIlpPacket(
      BtpResponse btpResponse, final CodecContext ilpCodecContext
  ) {
    Objects.requireNonNull(btpResponse);
    Objects.requireNonNull(ilpCodecContext);

    try {
      final ByteArrayInputStream inputStream =
          new ByteArrayInputStream(btpResponse.getPrimarySubProtocol().getData());
      return ilpCodecContext.read(InterledgerPacket.class, inputStream);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Sends an ILP request packet to the peer and returns the response packet (this method correlates with
   * <tt>sendData</tt> in the Javascript connector).
   *
   * @param sourceAccountId The remote peer account that sent this ILP Message.
   * @param ilpPacket       An {@link InterledgerPacket} for a corresponding BTP sub-protocol payload. Note that this
   *                        may be an ILP request (prepare packet) or a response (i.e., fulfillment or rejection).
   *
   * @return A {@link CompletableFuture} that resolves to the ILP response from the peer.
   *
   * @throws InterledgerProtocolException if the request is rejected by the peer.
   */
  public abstract CompletableFuture<Optional<InterledgerPacket>> handleIlpPacketSubprotocolData(
      final InterledgerAddress sourceAccountId, final InterledgerPacket ilpPacket
  ) throws InterledgerProtocolException;

  @Override
  public CompletableFuture<Optional<BtpSubProtocol>> handleSubprotocolDataForBtpMessage(
      final BtpSession btpSession, final BtpMessage btpMessage
  ) {
    Objects.requireNonNull(btpSession, "btpSession must not be null!");
    Objects.requireNonNull(btpMessage, "btpMessage must not be null!");

    if (logger.isDebugEnabled()) {
      logger.debug("Incoming ILP Subprotocol BtpMessage: {}", btpMessage);
    }

    // TODO: Check for authentication. If not authenticated, then throw an exception! The Auth sub-protocol should
    // have been the first thing called for this BtpSession.
    // btpSession.isAuthenticated?

    try {
      // Convert to an ILP Prepare packet.
      final InterledgerPacket incomingIlpPacket = ilpCodecContext
          .read(InterledgerPacket.class, new ByteArrayInputStream(btpMessage.getPrimarySubProtocol().getData()));

      // For now, we assume that a peer will operate a lpi2 for each account that it owns, and so will initiate a BTP
      // connection for each account transmitting value because the the protocol is designed to have a single to/from in
      // it. To explore this further, consider the to/from sub-protocols proposed by Michiel, although these may not
      // be necessary, depending on actual deployment models.
      final InterledgerAddress sourceAccountId = btpSession.getPeerAccountAddress();

      // Handle the BTP sub-protocol as an ILP Packet...
      return this.handleIlpPacketSubprotocolData(sourceAccountId, incomingIlpPacket)
          // and convert back to BTP...
          .thenApply(
              packet -> packet
                  .map(p -> AbstractIlpBtpSubprotocolHandler.toBtpSubprotocol(p, ilpCodecContext))
                  // If there's no packet, then return null so no response is returned...
                  .orElse(null)
          )
          .thenApply(Optional::ofNullable);

    } catch (IOException e) {
      // Per RFC-23, ILP packets are attached under the protocol name "toBtpSubprotocol" with content-type
      // "application/octet-stream". If an unreadable BTP packet is received, no response should be sent. An unreadable
      // BTP packet is one which is structurally invalid, i.e. terminates before length prefixes dictate or contains
      // illegal characters.
      throw new RuntimeException(e);
    } catch (InterledgerProtocolException e) {
      // If there's an exception, then return it as a BTP packet...
      return CompletableFuture.supplyAsync(() -> toBtpSubprotocol(e.getInterledgerRejectPacket(), ilpCodecContext))
          .thenApply(Optional::of)
          .toCompletableFuture();
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
}
