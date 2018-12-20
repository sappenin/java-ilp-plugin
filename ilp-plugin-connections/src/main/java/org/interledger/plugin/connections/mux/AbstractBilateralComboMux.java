package org.interledger.plugin.connections.mux;

import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.BilateralReceiver;
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
 * A combo sender/receiver MUX, for scenarios where the sender and receiver utilize the same underlying transport.
 */
public abstract class AbstractBilateralComboMux extends AbstractMux
    implements BilateralSenderMux, BilateralReceiverMux {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final Map<InterledgerAddress, BilateralSender> bilateralSenders;
  private final Map<InterledgerAddress, BilateralReceiver> bilateralReceivers;

  public AbstractBilateralComboMux() {
    this.bilateralSenders = Maps.newConcurrentMap();
    this.bilateralReceivers = Maps.newConcurrentMap();
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

    // If the plugin being added has not been added to this Connection, then register this Connection as an event-listener for the plugin.
    if (this.bilateralSenders.put(accountAddress, sender) == null) {
      bilateralSenders.put(accountAddress, sender);
      sender.connect();
    }
  }

  @Override
  public void unregisterBilateralSender(final InterledgerAddress accountAddress) {
    Objects.requireNonNull(accountAddress);
    final BilateralSender removedSender = this.bilateralSenders.remove(accountAddress);
    if (removedSender != null) {
      removedSender.disconnect();
    }
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
      receiver.connect();
    }
  }

  @Override
  public void unregisterBilateralReceiver(final InterledgerAddress accountAddress) {
    Objects.requireNonNull(accountAddress);
    final BilateralReceiver removeReceiver = this.bilateralReceivers.remove(accountAddress);
    if (removeReceiver != null) {
      removeReceiver.disconnect();
    }
  }

  /**
   * Perform the logic of actually connecting to the remote peer.
   */
  @Override
  public CompletableFuture<Void> doConnectMux() {

    // Call the sub-class implementation to actually connect to a remote, and then connect all senders...
    return this.doConnectTransport()
        .thenCompose(($) -> {

          // Connect all senders...events are not used because a BilateralSender should not be aware that it is being MUXed...
          final CompletableFuture[] arrayOfConnectFutures = bilateralSenders.values().stream()
              .map(BilateralSender::connect)
              .collect(Collectors.toList())
              .toArray(new CompletableFuture[0]);

          return CompletableFuture.allOf(arrayOfConnectFutures);
        })
        .thenCompose(($) -> {
          // Connect all receviers...events are not used because a BilateralSender should not be aware that it is being MUXed...
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
    // Call the sub-class implementation to actually disconnect from a remote, and then disconnect all senders...
    return this.doDisconnectTransport()
        .thenCompose(($) -> {
          // Disconnect all senders...events are not used because a BilateralSender should not be aware that it is being MUXed...
          final CompletableFuture[] arrayOfConnectFutures = bilateralSenders.values().stream()
              .map(BilateralSender::disconnect)
              .collect(Collectors.toList())
              .toArray(new CompletableFuture[0]);

          return CompletableFuture.allOf(arrayOfConnectFutures);
        })
        .thenCompose(($) -> {
          // Disconnect all receviers...events are not used because a BilateralSender should not be aware that it is being MUXed...
          final CompletableFuture[] arrayOfConnectFutures = bilateralReceivers.values().stream()
              .map(BilateralReceiver::disconnect)
              .collect(Collectors.toList())
              .toArray(new CompletableFuture[0]);

          return CompletableFuture.allOf(arrayOfConnectFutures);
        });
  }

  /**
   * Perform the logic of disconnecting the actual transport underneath this MUX.
   */
  public abstract CompletableFuture<Void> doDisconnectTransport();

  protected Map<InterledgerAddress, BilateralSender> getBilateralSenders() {
    return bilateralSenders;
  }

  protected Map<InterledgerAddress, BilateralReceiver> getBilateralReceivers() {
    return bilateralReceivers;
  }
}
