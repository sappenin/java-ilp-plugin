package org.interledger.plugin.lpiv2.bpp.grpc.connection.mux;

import org.interledger.bpp.grpc.BppGrpc;
import org.interledger.bpp.grpc.BppProto.BppPrepare;
import org.interledger.bpp.grpc.BppProto.BppResponse;
import org.interledger.core.InterledgerAddress;
import org.interledger.core.InterledgerFulfillPacket;
import org.interledger.core.InterledgerPreparePacket;
import org.interledger.core.InterledgerRejectPacket;
import org.interledger.core.InterledgerResponsePacket;
import org.interledger.core.InterledgerResponsePacketHandler;
import org.interledger.encoding.asn.framework.CodecContext;
import org.interledger.plugin.BilateralReceiver;
import org.interledger.plugin.BilateralReceiver.DataHandler;
import org.interledger.plugin.connections.mux.AbstractBilateralReceiverMux;
import org.interledger.plugin.connections.mux.BilateralReceiverMux;

import com.google.protobuf.ByteString;
import io.grpc.Context;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * A Bilateral Receiver MUX that listens to a gRPC server that supports BPP. This implementation will inspect the BPP
 * packet and connect the incoming request to a register {@link BilateralReceiver}.
 */
public class GrpcBppServerMux extends AbstractBilateralReceiverMux implements BilateralReceiverMux {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  //private final int grpcServerPort;
  private final CodecContext ilpCodecContext;
  private Server server;

//  public GrpcBppServerMux(final CodecContext ilpCodecContext, final Plugin<?> plugin, final int grpcServerPort) {
//    this.ilpCodecContext = Objects.requireNonNull(ilpCodecContext);
//    this.plugin = Objects.requireNonNull(plugin);
//    this.grpcServerPort = grpcServerPort;
//  }

  public GrpcBppServerMux(final CodecContext ilpCodecContext, final Server server) {
    this.ilpCodecContext = Objects.requireNonNull(ilpCodecContext);
    this.server = Objects.requireNonNull(server);

    // Connect gRPC to this Mux...
    final BppImpl bppImpl = new BppImpl(ilpCodecContext, this);
    server.getMutableServices().add(bppImpl.bindService());

    // TODO: Add this to the server definition in the Connector, and then remove from here...
    //    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//      // Use stderr here since the logger may have been reset by its JVM shutdown hook.
//      System.err.println("*** shutting down gRPC server since JVM is shutting down");
//      disconnect();
//      System.err.println("*** server shut down");
//    }));
  }

  //  private void start() throws IOException {
//    server = ServerBuilder.forPort(grpcServerPort)
//        .addService(new BppImpl(ilpCodecContext, plugin))
//        .build()
//        .start();
//    logger.info("Server started, listening on " + grpcServerPort);
//    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//      // Use stderr here since the logger may have been reset by its JVM shutdown hook.
//      System.err.println("*** shutting down gRPC server since JVM is shutting down");
//      GrpcBppServerMux.this.stop();
//      System.err.println("*** server shut down");
//    }));
//  }
//
//  private void stop() {
//    if (server != null) {
//      server.shutdown();
//    }
//  }

  /**
   * Perform the logic of connecting the actual transport underneath this MUX.
   */
  @Override
  public CompletableFuture<Void> doConnectTransport() {
    // No-op because the grpc server listens for connections at startup, but doesn't connect on-demand.
    return CompletableFuture.completedFuture(null);
  }

  /**
   * Perform the logic of disconnecting the actual transport underneath this MUX.
   */
  @Override
  public CompletableFuture<Void> doDisconnectTransport() {
    // No-op because the grpc server listens for connections at startup, but doesn't connect on-demand.
    return CompletableFuture.completedFuture(null);
  }

  /**
   * An implementation of {@link BppImpl} that connects gRPC Server to a {@link BilateralReceiver}. Note that BPP will
   * utilize a completely different gRPC transport channel for bilateral `send` operations, so despite this
   * implementation's definition of `send`, this implementation is actually is implementing the receiving side of the
   * RPC `send` operation.
   */
  static class BppImpl extends BppGrpc.BppImplBase {

    private final CodecContext ilpCodecContext;
    private final BilateralReceiverMux bilateralReceiverMux;

    public BppImpl(final CodecContext ilpCodecContext, final BilateralReceiverMux bilateralReceiverMux) {
      this.ilpCodecContext = Objects.requireNonNull(ilpCodecContext);
      this.bilateralReceiverMux = Objects.requireNonNull(bilateralReceiverMux);
    }

    /**
     * <p>Accepts an incoming Prepare bppPrepare, processes it, and then returns a response to the caller via the
     * supplied {@code responseObserver}.</p>
     *
     * @param bppPrepare
     * @param responseObserver
     */
    @Override
    public void send(final BppPrepare bppPrepare, final StreamObserver<BppResponse> responseObserver) {
      if (Context.current().isCancelled()) {
        responseObserver.onError(Status.CANCELLED.withDescription("Cancelled by client").asRuntimeException());
        return;
      }

      final InterledgerAddress accountAddress = InterledgerAddress.of(bppPrepare.getAccountAddress());

      try {
        // Convert the bppPrepare bytes into a Prepare Packet.
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        bppPrepare.getPreparePacketBytes().writeTo(out);

        final InterledgerPreparePacket preparePacket = ilpCodecContext
            .read(InterledgerPreparePacket.class, new ByteArrayInputStream(out.toByteArray()));

        final DataHandler dataHandler = bilateralReceiverMux
            .getBilateralReceiver(accountAddress)
            .orElseThrow(() -> new RuntimeException(String.format(
                "No BilateralReceiver registered with BilateralReceiverMux for Account: %s", accountAddress
            )))
            .getDataHandler()
            .orElseThrow(() -> new RuntimeException("No DataHandler registered with BppImpl!"));

        dataHandler.handleIncomingData(preparePacket)
            .whenComplete((ilpResponse, error) -> {
              // Push the response back to the grpc responseObserver so it will go back to the requestor...
              if (error != null) {
                responseObserver.onError(error);
              } else {
                // Handle a valid response...
                new InterledgerResponsePacketHandler() {
                  @Override
                  protected void handleFulfillPacket(final InterledgerFulfillPacket interledgerFulfillPacket) {
                    Objects.requireNonNull(interledgerFulfillPacket);
                    responseObserver
                        .onNext(toSendDataResponse(accountAddress, interledgerFulfillPacket));
                    responseObserver.onCompleted();
                  }

                  @Override
                  protected void handleRejectPacket(final InterledgerRejectPacket interledgerRejectPacket) {
                    Objects.requireNonNull(interledgerRejectPacket);
                    responseObserver
                        .onNext(toSendDataResponse(accountAddress, interledgerRejectPacket));
                    responseObserver.onCompleted();
                  }

                  @Override
                  protected void handleExpiredPacket() {
                    // TODO: Return an error in gRPC?
                    responseObserver.onCompleted();
                  }
                }.handle(ilpResponse);
              }
            });
      } catch (Exception e) {
        responseObserver.onError(e);
        responseObserver.onCompleted();
      }
    }

    private BppResponse toSendDataResponse(
        final InterledgerAddress accountAddress, final InterledgerResponsePacket responsePacket
    ) {
      Objects.requireNonNull(responsePacket);

      final ByteString byteString = GrpcIOStreamUtils.toByteString(ilpCodecContext, responsePacket);
      return BppResponse.newBuilder()
          .setAccountAddress(accountAddress.getValue())
          .setResponsePacketBytes(byteString)
          .build();
    }
  }
}
