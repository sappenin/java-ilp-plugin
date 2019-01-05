package org.interledger.plugin.lpiv2.bpp.grpc;

import org.interledger.encoding.asn.framework.CodecContext;
import org.interledger.plugin.connections.mux.BilateralReceiverMux;
import org.interledger.plugin.connections.mux.BilateralSenderMux;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * An extension of {@link AbstractGrpcPlugin} that implements gRPC Bilateral Push Protocol using a gRPC client and gRPC
 * server as the actual transport layers.
 */
// TODO: Capture unit tests from {@link PinterledgerClientTest}.
public class GrpcBppPushPlugin extends AbstractGrpcPlugin<GrpcPluginSettings> {

  private final BilateralSenderMux senderMux;
  private final BilateralReceiverMux receiverMux;

  /**
   * Required-args Constructor.
   */
  public GrpcBppPushPlugin(
      final GrpcPluginSettings pluginSettings, final CodecContext ilpCodecContext,
      final BilateralSenderMux senderMux, final BilateralReceiverMux receiverMux
  ) {
    super(pluginSettings, ilpCodecContext);
    this.senderMux = Objects.requireNonNull(senderMux);
    this.receiverMux = Objects.requireNonNull(receiverMux);

    this.senderMux.registerBilateralSender(pluginSettings.getAccountAddress(), this);
    this.receiverMux.registerBilateralReceiver(pluginSettings.getAccountAddress(), this);
  }

  /**
   * Perform the logic of actually connecting to the remote peer.
   */
  @Override
  public CompletableFuture<Void> doConnect() {
    // This is a no-op. All connectivity is performed by the MUXes...
    return CompletableFuture.completedFuture(null);
  }

  /**
   * Perform the logic of disconnecting from the remote peer.
   */
  @Override
  public CompletableFuture<Void> doDisconnect() {
    // This is a no-op. All connectivity is performed by the MUXes...
    return CompletableFuture.completedFuture(null);
  }
}
