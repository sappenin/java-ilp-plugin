package org.interledger.plugin.mux.grpc.bpp;

import org.interledger.core.InterledgerPreparePacket;
import org.interledger.core.InterledgerResponsePacket;
import org.interledger.encoding.asn.framework.CodecContext;
import org.interledger.grpc.bpp.GrinterledgerBppGrpc;
import org.interledger.grpc.bpp.GrinterledgerBppProto.GrinterledgerBppPrepare;
import org.interledger.grpc.bpp.GrinterledgerBppProto.GrinterledgerBppResponse;
import org.interledger.plugin.connections.mux.AbstractBilateralSenderMux;
import org.interledger.plugin.connections.mux.BilateralSenderMux;
import org.interledger.plugin.mux.grpc.GrpcIOStreamUtils;

import com.spotify.futures.CompletableFuturesExtra;
import io.grpc.Channel;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

/**
 * A simple client that sends ILPv4 Prepare-packets to a remote peer, and expects either a fulfill or a reject as a
 * response.
 */
public class GrinterledgerBppClient{ // {extends AbstractBilateralSenderMux implements BilateralSenderMux {

//  private final Logger logger = LoggerFactory.getLogger(this.getClass());
//  private final CodecContext ilpCodecContext;
//  private final ManagedChannel channel;
//  private final GrinterledgerBppGrpc.GrinterledgerBppFutureStub nonblockingStub;
//
//  // TODO: Make Configurable.
//  //@Option(name="--deadline_ms", usage="Deadline in milliseconds.")
//  private int deadlineMs = 20 * 1000;
//
//  /**
//   * Construct client connecting to HelloWorld server at {@code host:port}.
//   */
//  public GrinterledgerBppClient(final String host, final int port, final CodecContext ilpCodecContext) {
//    this(ilpCodecContext, ManagedChannelBuilder.forAddress(host, port)
//        // TODO: Enable/Disable via configuration.
//        // Channels are secure by default (via SSL/TLS), but in dev-mode, we disable TLS to avoid needing certificates.
//        //.usePlaintext()
//        .build());
//  }
//
//  /**
//   * Construct client for accessing HelloWorld server using the existing channel.
//   */
//  GrinterledgerBppClient(final CodecContext ilpCodecContext, final ManagedChannel channel) {
//    this.ilpCodecContext = Objects.requireNonNull(ilpCodecContext);
//    this.channel = Objects.requireNonNull(channel);
//    //this.blockingStub = PinterledgerGrpc.newBlockingStub(channel);
//    this.nonblockingStub = GrinterledgerBppGrpc.newFutureStub(channel);
//  }
//
//  @Override
//  public CompletableFuture<Void> doConnectSenderMux() {
//
//    return CompletableFuture.runAsync(() -> {
//
//      // Connect the sender to the remote server....
//      // TODO: How do we reconnect the Channel?
//
//      // If the channel shuts down, then disconnect this client.
//      this.channel.notifyWhenStateChanged(ConnectivityState.SHUTDOWN, () -> shutdown());
//    });
//  }
//
//  /**
//   * Perform the logic of disconnecting from the remote peer.
//   */
//  @Override
//  public CompletableFuture<Void> doDisconnect() {
//    return CompletableFuture.runAsync(this::shutdown);
//  }
//
//  public void shutdown() {
//    try {
//      channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
//    } catch (InterruptedException e) {
//      throw new RuntimeException(e.getMessage(), e);
//    }
//  }
//
//  /**
//   * Send a Prepare packet to the gRPC server this client is connected to.
//   */
//  public CompletableFuture<Optional<InterledgerResponsePacket>> send(final InterledgerPreparePacket preparePacket) {
//    logger.debug("Sending preparePacket: {}", preparePacket);
//
//    final GrinterledgerBppPrepare request = GrinterledgerBppPrepare.newBuilder()
//        .setPreparePacketBytes(GrpcIOStreamUtils.toByteString(ilpCodecContext, preparePacket))
//        .build();
//
//    try {
//      return CompletableFuturesExtra.toCompletableFuture(
//          nonblockingStub.withDeadlineAfter(deadlineMs, TimeUnit.MILLISECONDS).send(request)
//      )
//          .thenApply(GrinterledgerBppResponse::getResponsePacketBytes)
//          .thenApply(responsePacketBytes -> {
//            final InterledgerResponsePacket packet = (InterledgerResponsePacket) GrpcIOStreamUtils
//                .fromByteString(ilpCodecContext, responsePacketBytes);
//            return Optional.ofNullable(packet);
//          }).exceptionally(err -> {
//            // If there's an error, then return empty.
//            if (CompletionException.class.isAssignableFrom(err.getClass())) {
//              logger.warn("gRPC call timed out: {}", err.getMessage(), err);
//            } else {
//              logger.error("Unexepcted Error: {}", err.getMessage(), err);
//            }
//            return Optional.empty();
//          });
//    } catch (StatusRuntimeException e) {
//      // TODO: Look at status codes for better error messaging.
//      logger.warn("Pinterledger Prepare Request Timed-out!", e.getMessage(), e);
//      return CompletableFuture.completedFuture(Optional.empty());
//    }
//  }
//
//  public Channel getChannel() {
//    return this.channel;
//  }

}
