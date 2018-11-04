package org.interledger.plugin.lpiv2.btp2.subprotocols.ilp;

import org.interledger.core.InterledgerAddress;
import org.interledger.core.InterledgerFulfillPacket;
import org.interledger.core.InterledgerPacket;
import org.interledger.core.InterledgerPacketMapper;
import org.interledger.core.InterledgerPreparePacket;
import org.interledger.core.InterledgerProtocolException;
import org.interledger.core.InterledgerRejectPacket;
import org.interledger.encoding.asn.framework.CodecContext;
import org.interledger.plugin.lpiv2.Plugin;
import org.interledger.plugin.lpiv2.Plugin.IlpDataHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * <p>An extension of {@link AbstractIlpBtpSubprotocolHandler} for handling the ILP sub-protocol by forwardig to an
 * instance of {@link IlpDataHandler}</p>
 */
public class DefaultIlpBtpSubprotocolHandler extends AbstractIlpBtpSubprotocolHandler {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  // To avoid circular-dependencies, this handler must be set _after_ the Connector server has started...
  private Plugin.IlpDataHandler ilpPluginDataHandler;

  public DefaultIlpBtpSubprotocolHandler(final CodecContext ilpCodecContext) {
    super(ilpCodecContext);
  }

  @Override
  public CompletableFuture<Optional<InterledgerPacket>> handleIlpPacketSubprotocolData(
      final InterledgerAddress sourceAccountId, final InterledgerPacket ilpPacket
  ) throws InterledgerProtocolException {

    // The ILP subprotocol defines what types of ILP packet are allowed in each type of BTP request/response.
    return new InterledgerPacketMapper<CompletableFuture<Optional<InterledgerPacket>>>() {

      // If the Packet is a prepare packet, then we forward to the ilpPlugin Data Handler because that is what will
      // connect into the ILP Connector Switch, which will then forward the packet to the correct place.
      // This connects the incoming BTP message with the ILP layer, which acts as if its receiving an incoming ILP
      // Preapre packet.
      @Override
      protected CompletableFuture<Optional<InterledgerPacket>> mapPreparePacket(
          InterledgerPreparePacket interledgerPreparePacket) {
        // DataHandler delegates to the ILP Packet Switch run by this connector...
        return ilpPluginDataHandler
            .handleIncomingData(sourceAccountId, interledgerPreparePacket)
            .thenApply(fulfillPacket1 -> (InterledgerPacket) fulfillPacket1)
            .thenApply(Optional::of);
      }

      // If the Packet is a fulfill packet, then we forward to the ilpPlugin Data Handler registered with this class.
      // This connects the incoming BTP message with the ILP layer, which acts as if its receiving an incoming ILP
      // Preapre packet.
      @Override
      protected CompletableFuture<Optional<InterledgerPacket>> mapFulfillPacket(
          final InterledgerFulfillPacket interledgerFulfillPacket
      ) {
        // This mapper should never encounter a FulfillPacket in this way. This is because when a Prepare packet
        // comes into the Connector, a CompletableFuture is always constructed to accept the response and return it
        // to the original caller. Thus, if a Fulfill packet makes it into this location, it's an error.
        logger.error("Encountered errant InterledgerFulfillPacket but should not have: {}", interledgerFulfillPacket);
        return CompletableFuture.completedFuture(Optional.empty());
      }

      @Override
      protected CompletableFuture<Optional<InterledgerPacket>> mapRejectPacket(
          final InterledgerRejectPacket interledgerRejectPacket
      ) {
        // This mapper should never encounter a RejectPacket in this way. This is because when a Prepare packet
        // comes into the Connector, a CompletableFuture is always constructed to accept the response and return it
        // to the original caller. Thus, if a Reject packet makes it into this location, it's an error.
        logger.error("Encountered errant interledgerRejectPacket but should not have: {}", interledgerRejectPacket);
        return CompletableFuture.completedFuture(Optional.empty());
      }
    }.map(ilpPacket);

  }

  public void setIlpPluginDataHandler(final Plugin.IlpDataHandler ilpPluginDataHandler) {
    this.ilpPluginDataHandler = Objects.requireNonNull(ilpPluginDataHandler);
  }

}