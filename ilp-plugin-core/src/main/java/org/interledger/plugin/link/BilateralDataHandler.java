package org.interledger.plugin.link;

import org.interledger.core.InterledgerAddress;
import org.interledger.core.InterledgerFulfillPacket;
import org.interledger.core.InterledgerPreparePacket;
import org.interledger.core.InterledgerRejectPacket;
import org.interledger.core.InterledgerResponsePacket;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Handles an incoming {@link InterledgerPreparePacket} for a single plugin, sent from a remote peer.
 */
@FunctionalInterface
public interface BilateralDataHandler {

  /**
   * Handles an incoming {@link InterledgerPreparePacket} received from a connected peer, but that may have originated
   * from any Interledger sender in the network.
   *
   * @param sourceAccountAddress  An {@link InterledgerAddress} for the remote peer (directly connected to this
   *                              receiver) that immediately sent the packet.
   * @param incomingPreparePacket A {@link InterledgerPreparePacket} containing data about an incoming payment.
   *
   * @return A {@link CompletableFuture} that resolves to an optionally-present {@link InterledgerResponsePacket}, which
   *     will be of concrete type {@link InterledgerFulfillPacket} or {@link InterledgerRejectPacket}, if present.
   */
  // TODO: Remove the sourceAddress from this interface. It's an implementation detail of the actual bilateral relationship (i.e.,
  CompletableFuture<Optional<InterledgerResponsePacket>> handleIncomingData(
      //InterledgerAddress sourceAccountAddress,
      InterledgerPreparePacket incomingPreparePacket
  );
}
