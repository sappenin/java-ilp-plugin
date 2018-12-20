package org.interledger.plugin.connections.mux;

import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.BilateralReceiver;
import org.interledger.plugin.connections.BilateralConnection;
import org.interledger.plugin.connections.events.bilateral.BilateralConnectionEventListener;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * <p>A multiplexed variant of {@link BilateralReceiver}. This interface enables many bilateral receievers to operate
 * over a common link, such as a network transport like gRPC.</p>
 *
 * <p>Because a MUX is meant to be utilized by a {@link BilateralConnection}, a MUX must always listen for events from
 * a {@link BilateralConnection} by implementing {@link BilateralConnectionEventListener}.</p>
 */
public interface BilateralReceiverMux extends ConnectableReceiver {

  /**
   * Accessor for the bilateral-sender associated with the indicated {@code sourceAccountAddress}.
   *
   * @param sourceAccountAddress The {@link InterledgerAddress} of the account this call is operating on behalf of
   *                             (i.e., the account address of the plugin that emitted this call).
   *
   * @return
   */
  Optional<BilateralReceiver> getBilateralReceiver(InterledgerAddress sourceAccountAddress);

  void registerBilateralReceiver(InterledgerAddress accountAddress, BilateralReceiver sender);

  void unregisterBilateralReceiver(InterledgerAddress accountAddress);

  default CompletableFuture<Void> disconnectReceiver(final InterledgerAddress accountAddress) {
    Objects.requireNonNull(accountAddress);
    return getBilateralReceiver(accountAddress)
        .map(BilateralReceiver::disconnect)
        .orElse(CompletableFuture.completedFuture(null));
  }
}
