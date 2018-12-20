package org.interledger.plugin.mux.grpc.bpp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.interledger.TestArtifacts.CODEC_CONTEXT;
import static org.interledger.TestArtifacts.FULFILLMENT;
import static org.interledger.TestArtifacts.PREPARE_PACKET;
import static org.interledger.TestArtifacts.REJECT_PACKET;
import static org.interledger.plugin.mux.grpc.GrpcIOStreamUtils.toByteString;
import static org.mockito.Mockito.when;

import org.interledger.core.InterledgerFulfillPacket;
import org.interledger.core.InterledgerRejectPacket;
import org.interledger.grpc.bpp.GrinterledgerBppGrpc;
import org.interledger.grpc.bpp.GrinterledgerBppProto.GrinterledgerBppPrepare;
import org.interledger.grpc.bpp.GrinterledgerBppProto.GrinterledgerBppResponse;
import org.interledger.plugin.lpiv2.Plugin;
//import org.interledger.plugin.mux.grpc.bpp.GrinterledgerBppServer.GrinterledgerBppImpl;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * A unit test that validates the {@link GrinterledgerBppServer}.
 */
@RunWith(JUnit4.class)
public class GrinterledgerBppServerTest {

//  /**
//   * This rule manages automatic graceful shutdown for the registered servers and channels at the end of test.
//   */
//  @Rule
//  public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();
//
//  @Mock
//  Plugin<?> pluginMock;
//
//  private GrinterledgerBppGrpc.GrinterledgerBppBlockingStub blockingStub;
//
//  @Before
//  public void setup() throws IOException {
//    MockitoAnnotations.initMocks(this);
//
//    // Generate a unique in-process server name.
//    final String serverName = InProcessServerBuilder.generateName();
//
//    when(pluginMock.safeGetDataSender()).thenReturn((packet) -> CompletableFuture.completedFuture(Optional.empty()));
//    when(pluginMock.safeGetDataHandler()).thenReturn((packet) -> CompletableFuture.completedFuture(Optional.empty()));
//    when(pluginMock.safeGetMoneySender()).thenReturn((packet) -> CompletableFuture.completedFuture(null));
//    when(pluginMock.safeGetMoneyHandler()).thenReturn((packet) -> CompletableFuture.completedFuture(null));
//    when(pluginMock.connect()).thenReturn(CompletableFuture.completedFuture(null));
//    when(pluginMock.disconnect()).thenReturn(CompletableFuture.completedFuture(null));
//
//    // Create a server, add service, start, and register for automatic graceful shutdown.
//    grpcCleanup.register(InProcessServerBuilder
//        .forName(serverName).directExecutor().addService(new GrinterledgerBppImpl(CODEC_CONTEXT, pluginMock)).build()
//        .start());
//
//    blockingStub = GrinterledgerBppGrpc.newBlockingStub(
//        // Create a client channel and register for automatic graceful shutdown.
//        grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build()));
//  }
//
//  /**
//   * To test the server, make calls with a real stub using the in-process channel, and verify an ILP Fulfill response.
//   */
//  @Test
//  public void pinterledgerImpl_sendWithFulfill() throws Exception {
//    // This test always fulfills...
//    final InterledgerFulfillPacket expectedFulfillPacket = InterledgerFulfillPacket.builder()
//        .fulfillment(FULFILLMENT)
//        .build();
//    when(pluginMock.safeGetDataSender()).thenReturn((preparePacket) ->
//        CompletableFuture.completedFuture(Optional.of(expectedFulfillPacket))
//    );
//
//    final GrinterledgerBppResponse reply = blockingStub.send(
//        GrinterledgerBppPrepare.newBuilder()
//            .setPreparePacketBytes(toByteString(CODEC_CONTEXT, PREPARE_PACKET))
//            .build()
//    );
//    final ByteArrayOutputStream actualOutputStream = new ByteArrayOutputStream();
//    actualOutputStream.write(reply.getResponsePacketBytes().toByteArray());
//    final InterledgerFulfillPacket packet = CODEC_CONTEXT
//        .read(InterledgerFulfillPacket.class, new ByteArrayInputStream(actualOutputStream.toByteArray()));
//
//    assertThat(packet.getFulfillment(), is(FULFILLMENT));
//  }
//
//  /**
//   * To test the server, make calls with a real stub using the in-process channel, and verify an ILP Fulfill response.
//   */
//  @Test
//  public void pinterledgerImpl_sendWithReject() throws Exception {
//
//    // This test always rejects...
//    when(pluginMock.safeGetDataSender()).thenReturn((packet) ->
//        CompletableFuture.completedFuture(Optional.of(REJECT_PACKET))
//    );
//
//    final GrinterledgerBppResponse reply = blockingStub.send(
//        GrinterledgerBppPrepare.newBuilder()
//            .setPreparePacketBytes(toByteString(CODEC_CONTEXT, PREPARE_PACKET))
//            .build()
//    );
//    final ByteArrayOutputStream actualOutputStream = new ByteArrayOutputStream();
//    actualOutputStream.write(reply.getResponsePacketBytes().toByteArray());
//    final InterledgerRejectPacket packet = CODEC_CONTEXT
//        .read(InterledgerRejectPacket.class, new ByteArrayInputStream(actualOutputStream.toByteArray()));
//
//    assertThat(packet, is(REJECT_PACKET));
//  }
//
//  /**
//   * To test the server, make calls with a real stub using the in-process channel, and verify an ILP Fulfill response.
//   */
//  @Test(expected = StatusRuntimeException.class)
//  public void pinterledgerImpl_sendWithTimeout() {
//
//    // This test always times out...
//    when(pluginMock.safeGetDataSender()).thenReturn((packet) ->
//        CompletableFuture.completedFuture(Optional.empty())
//    );
//
//    try {
//      blockingStub.send(GrinterledgerBppPrepare.newBuilder()
//          .setPreparePacketBytes(toByteString(CODEC_CONTEXT, PREPARE_PACKET))
//          .build()
//      );
//    } catch (StatusRuntimeException e) {
//      assertThat(e.getStatus().getCode(), is(Status.Code.CANCELLED));
//      throw e;
//    }
//  }


}