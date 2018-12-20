package org.interledger.plugin.mux.grpc.bpp;

import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.connections.AbstractBilateralConnection;

/**
 * <p>An extension of {@link AbstractBilateralConnection} that support gRPC Bilateral Push Protocol, which uses two
 * unary gRPC mux to support incoming and outgoing mux without having to support request/response correlation (i.e.,
 * gRPC handles which response corresponds to which request).</p>
 * <p/>
 * <p>This protocol is meant to be used between two ILSP nodes, such as two Connectors, or wherever it is possible to
 * establish bidirectional connectivity between two nodes (as an example, this setup would likely not work if one of the
 * participants is a Browser, due to firewall constraints).</p>
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
public class GrpcPushBilateralConnection { //extends
    //AbstractBilateralConnection<GrinterledgerBppClient, GrinterledgerBppServer> {

//  /**
//   * Required-args Constructor.
//   *
//   * @param operatorAddress        The {@link InterledgerAddress} of the node operating this MUX.
//   * @param grinterledgerBppClient The client used to send requests to the gRPC server on the other side of this
//   *                               org.interledger.bilateral connection.
//   * @param grinterledgerBppServer The sever used to receive requests from the gRPC server on the other side of this
//   *                               org.interledger.bilateral connection.
//   */
//  public GrpcPushBilateralConnection(
//      final InterledgerAddress operatorAddress,
//      final GrinterledgerBppClient grinterledgerBppClient,
//      final GrinterledgerBppServer grinterledgerBppServer
//  ) {
//    super(operatorAddress, grinterledgerBppClient, grinterledgerBppServer);
//  }
}
