package org.interledger.plugin.mux.grpc.bpp;

import org.interledger.plugin.connections.BilateralConnectionSettings;

import org.immutables.value.Value;

public interface GrpcBppPushConnectionSettings extends BilateralConnectionSettings {

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
   * <p>The `auth_username` for a BTP client. Enables multiple accounts over a single BTP WebSocket connection.</p>
   *
   * @return
   */
  //Optional<String> getAuthUsername();

  /**
   * The `auth_token` for a BTP client, as specified in IL-RFC-23.
   *
   * @return
   * @see "https://github.com/interledger/rfcs/blob/master/0023-bilateral-transfer-protocol/0023-bilateral-transfer-protocol.md#authentication"
   */
  //String getSecret();

  @Value.Immutable
  abstract class AbstractGrpcBppPushConnectionSettings implements GrpcBppPushConnectionSettings {

  }
}
