package org.interledger.plugin.lpiv2.bpp.grpc.connection;

import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.connections.AbstractBilateralConnection;
import org.interledger.plugin.lpiv2.bpp.grpc.connection.mux.GrpcBppClientMux;
import org.interledger.plugin.lpiv2.bpp.grpc.connection.mux.GrpcBppServerMux;

/**
 * <p>An extension of {@link AbstractBilateralConnection} that support the Bilateral Push Protocol (BPP), which uses
 * two unary gRPC muxes to support incoming and outgoing messages without having to re-implement request/response
 * correlation (i.e., Unary gRPC handles which response corresponds to which request).</p>
 * <p/>
 * <p>This protocol is meant to be used between two ILSP nodes, such as two Connectors, or wherever it is possible to
 * establish bidirectional connectivity between two nodes. As an example, this setup would likely not work if one of the
 * participants is a Browser, due to firewall constraints.</p>
 *
 * <p>The following diagram illustrates how this bilateral connection would look like between two nodes:</p>
 *
 * <pre>
 *                 ┌───────────┬───────────┐                 ┌───────────┬───────────┐      ┌─────────┐
 * ┌─────────┐     │           │gRPC Client│◁───TLS+HTTP/2──▷│gRPC Server│           │◁─ ─ ▷│ Plugin1 │
 * │ Plugin1 │◁ ─ ▷│           ├───────────┘                 └───────────┤           │      └─────────┘
 * └─────────┘     │ Connector │                                         │ Connector │
 *                 │Connection │                                         │Connection1│      ┌─────────┐
 * ┌─────────┐     │           ├───────────┐                 ┌───────────┤           │◁─ ─ ▷│ Plugin2 │
 * │ Plugin2 │◁ ─ ▷│           │gRPC Server│◁───TLS+HTTP/2──▷│gRPC Client│           │      └─────────┘
 * └─────────┘     ├───────────┼───────────┘                 └───────────┼───────────┤
 *                 │           │                                         │           │
 *                 │           │                                         │           │
 *                 │ Connector │                                         │ Connector │
 *                 │Connection2│                                         │Connection2│
 *                 │           │                                         │           │
 *                 │           │                                         │           │
 *                 └───────────┘                                         └───────────┘
 * </pre>
 */
public class BppConnection extends AbstractBilateralConnection<GrpcBppClientMux, GrpcBppServerMux> {

  /**
   * Required-args Constructor.
   *
   * @param operatorAddress        The {@link InterledgerAddress} of the node operating this MUX.
   * @param grinterledgerBppClient The client used to send requests to the gRPC server on the other side of this
   *                               org.interledger.bilateral connection.
   * @param grinterledgerBppServer The sever used to receive requests from the gRPC server on the other side of this
   *                               org.interledger.bilateral connection.
   */
  public BppConnection(
      final InterledgerAddress operatorAddress,
      final GrpcBppClientMux grinterledgerBppClient,
      final GrpcBppServerMux grinterledgerBppServer
  ) {
    super(operatorAddress, grinterledgerBppClient, grinterledgerBppServer);
  }
}
