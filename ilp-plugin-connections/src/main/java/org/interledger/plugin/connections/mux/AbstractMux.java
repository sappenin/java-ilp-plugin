package org.interledger.plugin.connections.mux;

import org.interledger.plugin.Connectable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An abstract MUX implementation that provides basic connection tracking.
 *
 * @deprecated Will be replaced by a BilateralConnection.
 */
@Deprecated
public abstract class AbstractMux {//implements Connectable {

//  private final AtomicBoolean connected = new AtomicBoolean(NOT_CONNECTED);
//  protected Logger logger = LoggerFactory.getLogger(this.getClass());
//
//  @Override
//  public boolean isConnected() {
//    return this.connected.get();
//  }
//
//  /**
//   * <p>Connect to the remote peer.</p>
//   */
//  @Override
//  public final CompletableFuture<Void> connect() {
//
////    try {
////      if (this.connected.compareAndSet(NOT_CONNECTED, CONNECTED)) {
////        return this.doConnect()
////            .whenComplete(($, error) -> {
////              if (error == null) {
////                // Emit a connected event...
////                this.pluginEventEmitter.emitEvent(PluginConnectedEvent.of(this));
////
////                logger.debug("[{}] `{}` connected to `{}`", this.getPluginSettings().getPluginType(),
////                    this.pluginSettings.getLocalNodeAddress(), this.getPluginSettings().getAccountAddress());
////              } else {
////                final String errorMessage = String.format("[%s] `%s` error while trying to connect to `%s`",
////                    this.pluginSettings.getPluginType(),
////                    this.pluginSettings.getLocalNodeAddress(), this.getPluginSettings().getAccountAddress()
////                );
////                logger.error(errorMessage, error);
////              }
////            });
////      } else {
////        logger.debug("[{}] `{}` already connected to `{}`...", this.pluginSettings.getPluginType(),
////            this.pluginSettings.getLocalNodeAddress(), this.getPluginSettings().getAccountAddress());
////        // No-op: We're already expectedCurrentState...
////        return CompletableFuture.completedFuture(null);
////      }
////    } catch (RuntimeException e) {
////      // If we can't connect, then disconnect this account in order to trigger any listeners.
////      this.disconnect().join();
////      throw e;
////    } catch (Exception e) {
////      // If we can't connect, then disconnect this account in order to trigger any listeners.
////      this.disconnect().join();
////      throw new RuntimeException(e.getMessage(), e);
////    }
//
//    // Try to connect, but no-op if already connected.
//    if (this.connected.compareAndSet(NOT_CONNECTED, CONNECTED)) {
//      return this.doConnectMux().thenApply($ -> {
//        logger.info("MUX Connected!");
//        return null;
//      });
//    } else {
//      return CompletableFuture.completedFuture(null);
//    }
//  }
//
//  /**
//   * Perform the logic of actually connecting to the remote peer.
//   */
//  public abstract CompletableFuture<Void> doConnectMux();
//
//  /**
//   * Disconnect from the remote peer.
//   */
//  @Override
//  public final CompletableFuture<Void> disconnect() {
//    // Try to disconnect, but no-op if already disconnected.
//    if (this.connected.compareAndSet(CONNECTED, NOT_CONNECTED)) {
//      return this.doDisconnectMux().thenApply($ -> {
//        logger.info("MUX Disconnected!");
//        return null;
//      });
//    } else {
//      return CompletableFuture.completedFuture(null);
//    }
//  }
//
//  /**
//   * Perform the logic of disconnecting from the remote peer.
//   */
//  public abstract CompletableFuture<Void> doDisconnectMux();

}
