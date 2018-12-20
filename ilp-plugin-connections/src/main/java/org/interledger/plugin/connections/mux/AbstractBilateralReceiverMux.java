package org.interledger.plugin.connections.mux;

import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.BilateralReceiver;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * An abstract implementation of {@link BilateralReceiverMux}.
 */
public abstract class AbstractBilateralReceiverMux extends AbstractMux implements BilateralReceiverMux {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  private Map<InterledgerAddress, BilateralReceiver> bilateralReceivers;

  public AbstractBilateralReceiverMux() {
    this.bilateralReceivers = Maps.newConcurrentMap();
  }

  @Override
  public Optional<BilateralReceiver> getBilateralReceiver(final InterledgerAddress sourceAccountAddress) {
    Objects.requireNonNull(sourceAccountAddress);
    return Optional.ofNullable(bilateralReceivers.get(sourceAccountAddress));
  }

  @Override
  public void registerBilateralReceiver(final InterledgerAddress accountAddress, final BilateralReceiver receiver) {
    Objects.requireNonNull(accountAddress);
    Objects.requireNonNull(receiver);

    // If the plugin being added has not been added to this Connection, then register this Connection as an
    // event-listener for the plugin.
    if (this.bilateralReceivers.put(accountAddress, receiver) == null) {
      bilateralReceivers.put(accountAddress, receiver);

      // Only connect the plugin if the MUX is connected...
      if (this.isConnected()) {
        receiver.connect();
      }
    }
  }

  @Override
  public void unregisterBilateralReceiver(final InterledgerAddress accountAddress) {
    Objects.requireNonNull(accountAddress);
    final BilateralReceiver removedReceiver = this.bilateralReceivers.remove(accountAddress);
    if (removedReceiver != null) {
      if (this.isConnected()) {
        removedReceiver.disconnect();
      }
    }
  }

  /**
   * Perform the logic of actually connecting to the remote peer.
   */
  @Override
  public CompletableFuture<Void> doConnectMux() {
    // Connection tracking is tracked by the super-class, which triggers this call.
    return this.doConnectTransport().thenCompose(($) -> {
      // Disconnect all senders...events are not used because a BilateralSender should not be aware that it is being MUXed...
      final CompletableFuture[] arrayOfConnectFutures = bilateralReceivers.values().stream()
          .map(BilateralReceiver::connect)
          .collect(Collectors.toList())
          .toArray(new CompletableFuture[0]);

      return CompletableFuture.allOf(arrayOfConnectFutures);
    });

  }

  /**
   * Perform the logic of connecting the actual transport underneath this MUX.
   */
  public abstract CompletableFuture<Void> doConnectTransport();

  /**
   * Perform the logic of disconnecting from the remote peer.
   */
  @Override
  public CompletableFuture<Void> doDisconnectMux() {
    // Connection tracking is tracked by the super-class, which triggers this call.
    return this.doDisconnectTransport().thenCompose(($) -> {
      // Disconnect all senders...events are not used because a BilateralSender should not be aware that it is being MUXed...
      final CompletableFuture[] arrayOfDisconnectFutures = bilateralReceivers.values().stream()
          .map(BilateralReceiver::disconnect)
          .collect(Collectors.toList())
          .toArray(new CompletableFuture[0]);

      return CompletableFuture.allOf(arrayOfDisconnectFutures);
    });
  }

  /**
   * Perform the logic of disconnecting the actual transport underneath this MUX.
   */
  public abstract CompletableFuture<Void> doDisconnectTransport();
}
