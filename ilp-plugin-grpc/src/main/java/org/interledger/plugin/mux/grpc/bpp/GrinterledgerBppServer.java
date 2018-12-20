package org.interledger.plugin.mux.grpc.bpp;

import org.interledger.core.InterledgerFulfillPacket;
import org.interledger.core.InterledgerPreparePacket;
import org.interledger.core.InterledgerRejectPacket;
import org.interledger.core.InterledgerResponsePacket;
import org.interledger.core.InterledgerResponsePacketHandler;
import org.interledger.encoding.asn.framework.CodecContext;
import org.interledger.grpc.bpp.GrinterledgerBppGrpc;
import org.interledger.grpc.bpp.GrinterledgerBppProto.GrinterledgerBppPrepare;
import org.interledger.grpc.bpp.GrinterledgerBppProto.GrinterledgerBppResponse;
import org.interledger.plugin.lpiv2.Plugin;
import org.interledger.plugin.connections.mux.AbstractBilateralReceiverMux;
import org.interledger.plugin.connections.mux.BilateralReceiverMux;

import com.google.protobuf.ByteString;
import io.grpc.Context;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Server that manages startup/shutdown of a gRPC server supporting the gRPC Interledger Push Protocol.
 */
public class GrinterledgerBppServer {//extends AbstractBilateralReceiverMux implements BilateralReceiverMux {

//  private final Logger logger = LoggerFactory.getLogger(this.getClass());
//
//  private final CodecContext ilpCodecContext;
//  private final Plugin<?> plugin;
//  private Server server;
//
//  public GrinterledgerBppServer(final CodecContext ilpCodecContext, Plugin<?> plugin) {
//    this.ilpCodecContext = Objects.requireNonNull(ilpCodecContext);
//    this.plugin = Objects.requireNonNull(plugin);
//  }
//
//  private void start() throws IOException {
//    /* The port on which the server should run */
//    int port = 50051;
//    server = ServerBuilder.forPort(port)
//        .addService(new GrinterledgerBppImpl(ilpCodecContext, plugin))
//        .build()
//        .start();
//    logger.info("Server started, listening on " + port);
//    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//      // Use stderr here since the logger may have been reset by its JVM shutdown hook.
//      System.err.println("*** shutting down gRPC server since JVM is shutting down");
//      GrinterledgerBppServer.this.stop();
//      System.err.println("*** server shut down");
//    }));
//  }
//
//  private void stop() {
//    if (server != null) {
//      server.shutdown();
//    }
//  }
//
//  @Override
//  public CompletableFuture<Void> doConnect() {
//    return CompletableFuture.completedFuture(null);
//
//  }
//
//  @Override
//  public CompletableFuture<Void> doDisconnect() {
//    return CompletableFuture.completedFuture(null);
//  }
//
//  static class GrinterledgerBppImpl extends GrinterledgerBppGrpc.GrinterledgerBppImplBase {
//
//    private final CodecContext ilpCodecContext;
//    // TODO: Narrow this type?
//    private final Plugin<?> plugin;
//
//    public GrinterledgerBppImpl(final CodecContext ilpCodecContext, Plugin<?> plugin) {
//      this.ilpCodecContext = Objects.requireNonNull(ilpCodecContext);
//      this.plugin = plugin;
//    }
//
//    /**
//     * <pre>
//     * Sends a PreparePacket to the Bilateral Peer
//     * </pre>
//     *
//     * @param request
//     * @param responseObserver
//     */
//    @Override
//    public void send(
//        final GrinterledgerBppPrepare request, final StreamObserver<GrinterledgerBppResponse> responseObserver
//    ) {
//
//      if (Context.current().isCancelled()) {
//        responseObserver.onError(Status.CANCELLED.withDescription("Cancelled by client").asRuntimeException());
//        return;
//      }
//
//      try {
//        // Convert the request bytes into a Prepare Packet.
//        final ByteArrayOutputStream out = new ByteArrayOutputStream();
//        request.getPreparePacketBytes().writeTo(out);
//        final InterledgerPreparePacket preparePacket = ilpCodecContext
//            .read(InterledgerPreparePacket.class, new ByteArrayInputStream(out.toByteArray()));
//
//        plugin.safeGetDataSender().sendData(preparePacket)
//            .whenComplete((ilpResponse, error) -> {
//              if (error != null) {
//                responseObserver.onError(error);
//              } else {
//
//                // Handle a valid response...
//                new InterledgerResponsePacketHandler() {
//                  @Override
//                  protected void handleFulfillPacket(InterledgerFulfillPacket interledgerFulfillPacket) {
//                    // Convert to gRPC Response
//                    try {
//                      responseObserver.onNext(toSendDataResponse(interledgerFulfillPacket));
//                      responseObserver.onCompleted();
//                    } catch (IOException e) {
//                      responseObserver.onError(e);
//                    }
//                  }
//
//                  @Override
//                  protected void handleRejectPacket(InterledgerRejectPacket interledgerRejectPacket) {
//                    try {
//                      responseObserver.onNext(toSendDataResponse(interledgerRejectPacket));
//                      responseObserver.onCompleted();
//                    } catch (IOException e) {
//                      responseObserver.onError(e);
//                    }
//                  }
//
//                  @Override
//                  protected void handleExpiredPacket() {
//                    // TODO: Return an error in gRPC?
//                    responseObserver.onCompleted();
//                  }
//                }.handle(ilpResponse);
//              }
//
//            });
//      } catch (Exception e) {
//        responseObserver.onError(e);
//      }
//    }
//
//    private GrinterledgerBppResponse toSendDataResponse(final InterledgerResponsePacket responsePacket)
//        throws IOException {
//      Objects.requireNonNull(responsePacket);
//
//      final ByteArrayOutputStream out = new ByteArrayOutputStream();
//      ilpCodecContext.write(responsePacket, out);
//      return GrinterledgerBppResponse.newBuilder()
//          .setResponsePacketBytes(ByteString.readFrom(new ByteArrayInputStream(out.toByteArray())))
//          .build();
//    }
//  }
}
