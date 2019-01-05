package org.interledger.plugin.connections.mux;

import org.interledger.plugin.lpiv2.LoopbackPlugin;

import java.util.concurrent.CompletableFuture;

/**
 * An implementation of {@link AbstractBilateralSenderMux} that provides loopback functionality using a {@link
 * LoopbackPlugin}.
 */
public class LoopbackSenderMux extends AbstractBilateralSenderMux {

//  /**
//   * Perform the logic of connecting the actual transport underneath this MUX.
//   */
//  @Override
//  public CompletableFuture<Void> doConnectTransport() {
//    // No-op for loopback.
//    return CompletableFuture.completedFuture(null);
//  }
//
//  /**
//   * Perform the logic of disconnecting the actual transport underneath this MUX.
//   */
//  @Override
//  public CompletableFuture<Void> doDisconnectTransport() {
//    // No-op for loopback.
//    return CompletableFuture.completedFuture(null);
//  }
}
