package org.interledger.plugin.lpiv2.bpp.grpc.connection.mux;

import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.interledger.TestArtifacts.CODEC_CONTEXT;
import static org.interledger.TestArtifacts.FULFILLMENT;
import static org.interledger.TestArtifacts.FULFILL_PACKET;
import static org.interledger.TestArtifacts.PREPARE_PACKET;
import static org.interledger.TestArtifacts.REJECT_PACKET;
import static org.interledger.plugin.lpiv2.bpp.grpc.connection.mux.GrpcIOStreamUtils.toByteString;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.interledger.bpp.grpc.BppGrpc;
import org.interledger.bpp.grpc.BppProto.BppPrepare;
import org.interledger.bpp.grpc.BppProto.BppResponse;
import org.interledger.core.InterledgerFulfillPacket;
import org.interledger.core.InterledgerRejectPacket;
import org.interledger.core.InterledgerResponsePacket;
import org.interledger.core.InterledgerResponsePacketHandler;

import com.google.protobuf.ByteString;
import io.grpc.Context;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

/**
 * A unit test that validates the {@link GrpcBppClientMux}.
 */
@RunWith(JUnit4.class)
public class GrpcBppClientMuxTest {

  /**
   * This rule manages automatic graceful shutdown for the registered servers and channels at the end of test.
   */
  @Rule
  public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

  private ExpectedResult expectedResult;

  private BppGrpc.BppImplBase serviceImpl;
  private GrpcBppClientMux client;

  @Before
  public void setUp() throws Exception {
    this.serviceImpl =
        mock(BppGrpc.BppImplBase.class,
            delegatesTo(new BppGrpc.BppImplBase() {
              @Override
              public void send(BppPrepare request,
                  StreamObserver<BppResponse> responseObserver) {

                if (Context.current().isCancelled()) {
                  responseObserver
                      .onError(Status.CANCELLED.withDescription("Cancelled by client").asRuntimeException());
                  return;
                }

                try {

                  switch (expectedResult) {
                    case FULFILL: {
                      final ByteString packetAsByteString = toByteString(CODEC_CONTEXT, FULFILL_PACKET);
                      final BppResponse sendDataResponse = BppResponse.newBuilder()
                          .setResponsePacketBytes(packetAsByteString)
                          .build();
                      // Send the response...
                      responseObserver.onNext(sendDataResponse);
                      responseObserver.onCompleted();
                      break;
                    }
                    case REJECT: {
                      final ByteString packetAsByteString = toByteString(CODEC_CONTEXT, REJECT_PACKET);
                      final BppResponse sendDataResponse = BppResponse.newBuilder()
                          .setResponsePacketBytes(packetAsByteString)
                          .build();
                      // Send the response...
                      responseObserver.onNext(sendDataResponse);
                      responseObserver.onCompleted();
                      break;
                    }
                    case EXPIRE:
                    default: {
                      responseObserver.onCompleted();
                      break;
                    }
                  }

                } catch (Exception e) {
                  responseObserver.onError(e);
                }
              }
            }));

    // Generate a unique in-process server name.
    final String serverName = InProcessServerBuilder.generateName();

    // Create a server, add service, start, and register for automatic graceful shutdown.
    grpcCleanup.register(InProcessServerBuilder
        .forName(serverName).directExecutor().addService(serviceImpl).build().start());

    // Create a client channel and register for automatic graceful shutdown.
    final ManagedChannel channel = grpcCleanup
        .register(InProcessChannelBuilder.forName(serverName).directExecutor().build());

    // Create a IlpPluginClient using the in-process channel;
    final GrpcBppClientMuxSettings settings = GrpcBppClientMuxSettings.builder()
        .port(5000)
        .host("localhost")
        .build();
    client = new GrpcBppClientMux(settings, CODEC_CONTEXT, channel);
  }

  /**
   * To test the client, call from the client against the fake server, and verify a fulfill response.
   */
  @Test
  public void client_sendAndFulfill() throws IOException {
    this.expectedResult = ExpectedResult.FULFILL;
    final ArgumentCaptor<BppPrepare> requestCaptor = ArgumentCaptor.forClass(BppPrepare.class);

    final ByteArrayOutputStream expectedPrepareBytesOutputStream = new ByteArrayOutputStream();
    CODEC_CONTEXT.write(PREPARE_PACKET, expectedPrepareBytesOutputStream);
    final byte[] expectedBytes = expectedPrepareBytesOutputStream.toByteArray();

    final Optional<InterledgerResponsePacket> response = client.send(PREPARE_PACKET).join();

    verify(serviceImpl).send(requestCaptor.capture(), ArgumentMatchers.any());
    assertThat(requestCaptor.getValue().getPreparePacketBytes().toByteArray(), is(expectedBytes));

    new InterledgerResponsePacketHandler() {

      @Override
      protected void handleFulfillPacket(InterledgerFulfillPacket interledgerFulfillPacket) {
        assertThat(interledgerFulfillPacket.getFulfillment(), is(FULFILLMENT));
      }

      @Override
      protected void handleRejectPacket(InterledgerRejectPacket interledgerRejectPacket) {
        fail("Request should not reject!");
      }

      @Override
      protected void handleExpiredPacket() {
        fail("Request should not expired!");
      }
    }.handle(response);
  }

  /**
   * To test the client, call from the client against the fake server, and verify a rejection response.
   */
  @Test
  public void client_sendAndReject() throws IOException {
    this.expectedResult = ExpectedResult.REJECT;
    final ArgumentCaptor<BppPrepare> requestCaptor = ArgumentCaptor
        .forClass(BppPrepare.class);

    ByteArrayOutputStream expectedPrepareBytesOutputStream = new ByteArrayOutputStream();
    CODEC_CONTEXT.write(PREPARE_PACKET, expectedPrepareBytesOutputStream);
    final byte[] expectedBytes = expectedPrepareBytesOutputStream.toByteArray();

    final Optional<InterledgerResponsePacket> response = client.send(PREPARE_PACKET).join();

    verify(serviceImpl).send(requestCaptor.capture(), ArgumentMatchers.any());
    assertThat(requestCaptor.getValue().getPreparePacketBytes().toByteArray(), is(expectedBytes));

    new InterledgerResponsePacketHandler() {

      @Override
      protected void handleFulfillPacket(InterledgerFulfillPacket interledgerFulfillPacket) {
        fail("Request should not fulfill!");
      }

      @Override
      protected void handleRejectPacket(InterledgerRejectPacket interledgerRejectPacket) {
        assertThat(interledgerRejectPacket, is(REJECT_PACKET));
      }

      @Override
      protected void handleExpiredPacket() {
        fail("Request should not expired!");
      }
    }.handle(response);
  }

  /**
   * To test the client, call from the client against the fake server, and verify a rejection response.
   */
  @Test
  public void client_sendAndTimeout() throws IOException {
    this.expectedResult = ExpectedResult.EXPIRE;
    final ArgumentCaptor<BppPrepare> requestCaptor = ArgumentCaptor
        .forClass(BppPrepare.class);

    ByteArrayOutputStream expectedPrepareBytesOutputStream = new ByteArrayOutputStream();
    CODEC_CONTEXT.write(PREPARE_PACKET, expectedPrepareBytesOutputStream);
    final byte[] expectedBytes = expectedPrepareBytesOutputStream.toByteArray();

    final Optional<InterledgerResponsePacket> response = client.send(PREPARE_PACKET).join();

    verify(serviceImpl).send(requestCaptor.capture(), ArgumentMatchers.any());
    assertThat(requestCaptor.getValue().getPreparePacketBytes().toByteArray(), is(expectedBytes));

    new InterledgerResponsePacketHandler() {

      @Override
      protected void handleFulfillPacket(InterledgerFulfillPacket interledgerFulfillPacket) {
        fail("Request should not fulfill!");
      }

      @Override
      protected void handleRejectPacket(InterledgerRejectPacket interledgerRejectPacket) {
        fail("Request should not reject!");
      }

      @Override
      protected void handleExpiredPacket() {
        // No-op
      }
    }.handle(response);
  }

  private enum ExpectedResult {
    FULFILL,
    REJECT,
    EXPIRE
  }
}