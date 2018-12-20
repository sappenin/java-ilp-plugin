package org.interledger.plugin.connections.mux;

import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.BilateralSender;
import org.interledger.plugin.connections.BilateralConnection;
import org.interledger.plugin.connections.events.bilateral.BilateralConnectionEventListener;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * <p>A multiplexed variant of {@link BilateralSender}. This interface enables many bilateral senders to operate over
 * a common link, such as a network transport like gRPC.</p>
 *
 * <p>Because a MUX is meant to be utilized by a {@link BilateralConnection}, a MUX must always listen for events from
 * a  {@link BilateralConnection} by implementing {@link BilateralConnectionEventListener}.</p>
 */
public interface BilateralSenderMux extends ConnectableSender {

  // TODO: Introduce SenderMuxSettings? Connection settings will be performed on a Sender/Receiver basis -- e.g.,
  // there may be a different underlying connection for the sender than for the receiver, packaged into a single BilateralConnection.
  // Alternatively, such as in the ComboMux, there may only be a single underlying connnection, in which case the
  // sender and receiver settings may be the same, but may only need to be added to one or the other.

  /**
   * Accessor for the bilateral-sender associated with the indicated {@code sourceAccountAddress}.
   *
   * @param sourceAccountAddress The {@link InterledgerAddress} of the account this call is operating on behalf of
   *                             (i.e., the account address of the plugin that emitted this call).
   *
   * @return
   */
  Optional<BilateralSender> getBilateralSender(InterledgerAddress sourceAccountAddress);

  void registerBilateralSender(InterledgerAddress accountAddress, BilateralSender sender);

  void unregisterBilateralSender(InterledgerAddress accountAddress);

  default CompletableFuture<Void> disconnectSender(final InterledgerAddress accountAddress) {
    Objects.requireNonNull(accountAddress);
    return getBilateralSender(accountAddress)
        .map(BilateralSender::disconnect)
        .orElse(CompletableFuture.completedFuture(null));
  }

}
