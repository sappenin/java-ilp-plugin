package org.interledger.plugin.lpiv2.bpp.grpc.connection.mux;

import org.interledger.plugin.connections.mux.MuxSettings;

import org.immutables.value.Value.Default;
import org.immutables.value.Value.Immutable;

public interface GrpcBppClientMuxSettings extends MuxSettings {

  static ImmutableGrpcBppClientMuxSettings.Builder builder() {
    return ImmutableGrpcBppClientMuxSettings.builder();
  }

  /**
   * The HTTP host to connect to in order to speak gRPC.
   *
   * @return
   */
  String host();

  /**
   * The HTTP port to connect to in order to speak gRPC.
   *
   * @return
   */
  int port();

  /**
   * The time to wait for a gRPC call to execute before timing out.
   *
   * @return
   */
  //@Option(name="--deadline_ms", usage="Deadline in milliseconds.")
  long grpcDeadlineMillis();

  /**
   * <p>The credential identifier used to connect authenticate to a remote gRPC server.</p>
   *
   * @return
   */
  //String getUsername();

  /**
   * <p>The credential used to connect authenticate to a remote gRPC server.</p>
   */
  //String getPassword();

  @Immutable
  abstract class AbstractGrpcBppClientMuxSettings implements GrpcBppClientMuxSettings {

    @Default
    @Override
    public long grpcDeadlineMillis() {
      return 20 * 1000;
    }

  }
}
