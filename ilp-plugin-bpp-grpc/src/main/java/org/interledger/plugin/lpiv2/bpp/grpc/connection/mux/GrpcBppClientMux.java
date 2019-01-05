package org.interledger.plugin.lpiv2.bpp.grpc.connection.mux;

import org.interledger.bpp.grpc.BppGrpc;
import org.interledger.bpp.grpc.BppProto.BppPrepare;
import org.interledger.bpp.grpc.BppProto.BppResponse;
import org.interledger.core.InterledgerPreparePacket;
import org.interledger.core.InterledgerResponsePacket;
import org.interledger.encoding.asn.framework.CodecContext;
import org.interledger.plugin.connections.mux.AbstractBilateralSenderMux;
import org.interledger.plugin.connections.mux.BilateralSenderMux;

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
 * response. This provides the <tt>client-side</tt> of a Bilateral Push Protocol (BPP) connection.
 */
public class GrpcBppClientMux extends AbstractBilateralSenderMux implements BilateralSenderMux {

  final GrpcBppClientMuxSettings settings;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final CodecContext ilpCodecContext;
  private final ManagedChannel channel;
  private final BppGrpc.BppFutureStub nonblockingStub;

  /**
   * Construct a client connecting to server specified in {@code settings}.
   */
  public GrpcBppClientMux(final GrpcBppClientMuxSettings settings, final CodecContext ilpCodecContext) {
    this(settings, ilpCodecContext, ManagedChannelBuilder.forAddress(settings.host(), settings.port())
        // Channels are secure by default (via SSL/TLS), but in dev-mode, we disable TLS to avoid needing certificates.
        //.usePlaintext()
        .build());
  }

  /**
   * Construct a client connecting to server using an existing channel.
   */
  GrpcBppClientMux(
      final GrpcBppClientMuxSettings settings, final CodecContext ilpCodecContext, final ManagedChannel channel
  ) {
    this.settings = Objects.requireNonNull(settings);
    this.ilpCodecContext = Objects.requireNonNull(ilpCodecContext);
    this.channel = Objects.requireNonNull(channel);
    this.nonblockingStub = BppGrpc.newFutureStub(channel);
  }

  /**
   * Perform the logic of connecting the actual transport underneath this MUX.
   */
  @Override
  public CompletableFuture<Void> doConnectTransport() {
    return CompletableFuture.runAsync(() -> {
      // TODO: How do we reconnect the Channel?

      // If the channel shuts down, then disconnect this client.
      this.channel.notifyWhenStateChanged(ConnectivityState.SHUTDOWN, () -> doDisconnectTransport());
    });
  }

  /**
   * Perform the logic of disconnecting the actual transport underneath this MUX.
   */
  @Override
  public CompletableFuture<Void> doDisconnectTransport() {
    return CompletableFuture.runAsync(this::shutdown);
  }

  public void shutdown() {
    try {
      channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Send a Prepare packet to the gRPC server this client is connected to.
   */
  public CompletableFuture<Optional<InterledgerResponsePacket>> send(final InterledgerPreparePacket preparePacket) {
    logger.debug("Sending preparePacket: {}", preparePacket);

    final BppPrepare request = BppPrepare.newBuilder()
        .setPreparePacketBytes(GrpcIOStreamUtils.toByteString(ilpCodecContext, preparePacket))
        .build();

    try {
      return CompletableFuturesExtra.toCompletableFuture(
          nonblockingStub.withDeadlineAfter(settings.grpcDeadlineMillis(), TimeUnit.MILLISECONDS).send(request)
      )
          .thenApply(BppResponse::getResponsePacketBytes)
          .thenApply(responsePacketBytes -> {
            final InterledgerResponsePacket packet = (InterledgerResponsePacket) GrpcIOStreamUtils
                .fromByteString(ilpCodecContext, responsePacketBytes);
            return Optional.ofNullable(packet);
          }).exceptionally(err -> {
            // If there's an error, then return empty.
            if (CompletionException.class.isAssignableFrom(err.getClass())) {
              logger.warn("gRPC call timed out: {}", err.getMessage(), err);
            } else {
              logger.error("Unexepcted Error: {}", err.getMessage(), err);
            }
            return Optional.empty();
          });
    } catch (StatusRuntimeException e) {
      // TODO: Look at status codes for better error messaging.
      logger.warn("Pinterledger Prepare Request Timed-out!", e.getMessage(), e);
      return CompletableFuture.completedFuture(Optional.empty());
    }
  }

  public Channel getChannel() {
    return this.channel;
  }
}
