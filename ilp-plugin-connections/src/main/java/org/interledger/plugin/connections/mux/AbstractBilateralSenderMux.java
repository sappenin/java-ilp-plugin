package org.interledger.plugin.connections.mux;

import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.BilateralSender;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * An abstract implementation of {@link BilateralSenderMux}.
 */
public abstract class AbstractBilateralSenderMux extends AbstractMux implements BilateralSenderMux {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final Map<InterledgerAddress, BilateralSender> bilateralSenders;

  public AbstractBilateralSenderMux() {
    this.bilateralSenders = Maps.newConcurrentMap();
  }

  @Override
  public Optional<BilateralSender> getBilateralSender(final InterledgerAddress sourceAccountAddress) {
    Objects.requireNonNull(sourceAccountAddress);
    return Optional.ofNullable(bilateralSenders.get(sourceAccountAddress));
  }

  @Override
  public void registerBilateralSender(final InterledgerAddress accountAddress, final BilateralSender sender) {
    Objects.requireNonNull(accountAddress);
    Objects.requireNonNull(sender);

    // If the plugin being added has not been added to this Connection, then register this Connection as an
    // event-listener for the plugin.
    if (this.bilateralSenders.put(accountAddress, sender) == null) {
      bilateralSenders.put(accountAddress, sender);

      // Only connect the plugin if the MUX is connected...
      if (this.isConnected()) {
        sender.connect();
      }
    }
  }

  @Override
  public void unregisterBilateralSender(final InterledgerAddress accountAddress) {
    Objects.requireNonNull(accountAddress);
    final BilateralSender removedSender = this.bilateralSenders.remove(accountAddress);
    if (removedSender != null) {
      if (this.isConnected()) {
        removedSender.disconnect();
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
      final CompletableFuture[] arrayOfConnectFutures = bilateralSenders.values().stream()
          .map(BilateralSender::connect)
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
      final CompletableFuture[] arrayOfDisconnectFutures = bilateralSenders.values().stream()
          .map(BilateralSender::disconnect)
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
