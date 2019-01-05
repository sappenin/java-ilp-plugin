package org.interledger.plugin.lpiv2.bpp.grpc.connection.mux;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.interledger.TestArtifacts.CODEC_CONTEXT;
import static org.interledger.TestArtifacts.FULFILLMENT;
import static org.interledger.TestArtifacts.PREPARE_PACKET;
import static org.interledger.TestArtifacts.REJECT_PACKET;
import static org.interledger.plugin.lpiv2.bpp.grpc.connection.mux.GrpcIOStreamUtils.toByteString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.interledger.bpp.grpc.BppGrpc;
import org.interledger.bpp.grpc.BppProto.BppPrepare;
import org.interledger.bpp.grpc.BppProto.BppResponse;
import org.interledger.core.InterledgerAddress;
import org.interledger.core.InterledgerFulfillPacket;
import org.interledger.core.InterledgerRejectPacket;
import org.interledger.plugin.connections.mux.BilateralReceiverMux;
import org.interledger.plugin.lpiv2.Plugin;
import org.interledger.plugin.lpiv2.bpp.grpc.connection.mux.GrpcBppServerMux.BppImpl;

import io.grpc.Server;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * A unit test that validates the {@link GrpcBppServerMux}.
 */
@RunWith(JUnit4.class)
public class GrpcBppServerMuxTest {

  private static final InterledgerAddress ACCOUNT_ADDRESS = InterledgerAddress.of("test.foo-account");

  /**
   * This rule manages automatic graceful shutdown for the registered servers and channels at the end of test.
   */
  @Rule
  public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

  @Mock
  Plugin<?> pluginMock;

  @Mock
  BilateralReceiverMux receiverMux;

  private BppGrpc.BppBlockingStub blockingStub;
  private GrpcBppServerMux serverMux;

  @Before
  public void setup() throws IOException {
    MockitoAnnotations.initMocks(this);

    // Generate a unique in-process server name.
    final String serverName = InProcessServerBuilder.generateName();

    when(receiverMux.getBilateralReceiver(any())).thenReturn(Optional.of(pluginMock));

    when(pluginMock.safeGetDataSender()).thenReturn((packet) -> CompletableFuture.completedFuture(Optional.empty()));
    when(pluginMock.safeGetDataHandler()).thenReturn((packet) -> CompletableFuture.completedFuture(Optional.empty()));
    when(pluginMock.safeGetMoneySender()).thenReturn((packet) -> CompletableFuture.completedFuture(null));
    when(pluginMock.safeGetMoneyHandler()).thenReturn((packet) -> CompletableFuture.completedFuture(null));
    when(pluginMock.connect()).thenReturn(CompletableFuture.completedFuture(null));
    when(pluginMock.disconnect()).thenReturn(CompletableFuture.completedFuture(null));

    // Create a server, add service, start, and register for automatic graceful shutdown.
    final Server server = InProcessServerBuilder.forName(serverName).directExecutor()
        .addService(new BppImpl(CODEC_CONTEXT, receiverMux)).build();
    grpcCleanup.register(server.start());

    blockingStub = BppGrpc.newBlockingStub(
        // Create a client channel and register for automatic graceful shutdown.
        grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build()));

    serverMux = new GrpcBppServerMux(CODEC_CONTEXT, server);
    serverMux.registerBilateralReceiver(ACCOUNT_ADDRESS, pluginMock);
    serverMux.connect();
  }

  /**
   * To test the server, make calls with a real stub using the in-process channel, and verify an ILP Fulfill response.
   */
  @Test
  public void serverMux_sendWithFulfill() {
    // This test always fulfills...
    final InterledgerFulfillPacket expectedFulfillPacket = InterledgerFulfillPacket.builder()
        .fulfillment(FULFILLMENT)
        .build();

    // Make the plugin fulfill correctly when it encounters a prepare packet...
    when(pluginMock.safeGetDataSender()).thenReturn((preparePacket) ->
        CompletableFuture.completedFuture(Optional.of(expectedFulfillPacket))
    );

    // Simulates an incoming message on the Server....
    final BppResponse reply = blockingStub.send(
        BppPrepare.newBuilder()
            .setPreparePacketBytes(toByteString(CODEC_CONTEXT, PREPARE_PACKET))
            .build()
    );
    final InterledgerFulfillPacket packet = (InterledgerFulfillPacket) GrpcIOStreamUtils
        .fromByteString(CODEC_CONTEXT, reply.getResponsePacketBytes());
    assertThat(packet.getFulfillment(), is(FULFILLMENT));
  }

  /**
   * To test the server, make calls with a real stub using the in-process channel, and verify an ILP Fulfill response.
   */
  @Test
  public void serverMux_sendWithReject() {

    // This test always rejects...
    when(pluginMock.safeGetDataSender()).thenReturn((packet) ->
        CompletableFuture.completedFuture(Optional.of(REJECT_PACKET))
    );

    final BppResponse reply = blockingStub.send(
        BppPrepare.newBuilder()
            .setPreparePacketBytes(toByteString(CODEC_CONTEXT, PREPARE_PACKET))
            .build()
    );

    final InterledgerRejectPacket packet = (InterledgerRejectPacket) GrpcIOStreamUtils
        .fromByteString(CODEC_CONTEXT, reply.getResponsePacketBytes());
    assertThat(packet, is(REJECT_PACKET));
  }

  /**
   * To test the server, make calls with a real stub using the in-process channel, and verify an ILP Fulfill response.
   */
  @Test(expected = StatusRuntimeException.class)
  public void serverMux_sendWithTimeout() {

    // This test always times out...
    when(pluginMock.safeGetDataSender()).thenReturn((packet) ->
        CompletableFuture.completedFuture(Optional.empty())
    );

    try {
      blockingStub.send(BppPrepare.newBuilder()
          .setPreparePacketBytes(toByteString(CODEC_CONTEXT, PREPARE_PACKET))
          .build()
      );
    } catch (StatusRuntimeException e) {
      assertThat(e.getStatus().getCode(), is(Status.Code.CANCELLED));
      throw e;
    }
  }


}