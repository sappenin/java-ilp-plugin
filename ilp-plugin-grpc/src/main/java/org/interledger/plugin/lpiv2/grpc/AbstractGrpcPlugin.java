package org.interledger.plugin.lpiv2.grpc;

import org.interledger.encoding.asn.framework.CodecContext;
import org.interledger.plugin.lpiv2.AbstractPlugin;
import org.interledger.plugin.lpiv2.Plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * <p>An extension of {@link Plugin} that utilizes gRPC as a transport.</p>
 *
 * <p>The default {@link #getMoneySender()} implementation is a no-op. In order to handle money, two features must be
 * defined: {@link #getMoneySender()}, which sends an amount of units to the peer for this Plugin. The second method is
 * {@link #getMoneyHandler()}.
 */
public abstract class AbstractGrpcPlugin<T extends GrpcPluginSettings> extends AbstractPlugin<T> implements Plugin<T> {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final CodecContext ilpCodecContext;

  /**
   * Required-args Constructor.
   */
  public AbstractGrpcPlugin(final T pluginSettings, final CodecContext ilpCodecContext) {
    super(pluginSettings);
    this.ilpCodecContext = Objects.requireNonNull(ilpCodecContext);
  }

//  /**
//   * Perform the logic of sending an ILP prepare-packet to a remote peer using BTP. This method is mux-agnostic, so
//   * implementations must define an implementation of the actual mux, such as Websocket or Http/2.
//   *
//   * @param preparePacket
//   */
//  @Override
//  public CompletableFuture<Optional<InterledgerResponsePacket>> doSendData(
//      final InterledgerPreparePacket preparePacket
//  ) {
//    Objects.requireNonNull(preparePacket);
//
//    // TODO: Implement re-connection logic, but only if this is a BTP Client. Servers simply have to wait to be
//    // connected...
//    // If the plugin is not connected, then throw an exception...
//    if (!this.isConnected()) {
//      throw new InterledgerProtocolException(
//          InterledgerRejectPacket.builder()
//              .message("Plugin not connected!")
//              .triggeredBy(getPluginSettings().getLocalNodeAddress())
//              .code(InterledgerErrorCode.T01_LEDGER_UNREACHABLE)
//              .build()
//      );
//    }
//
//
//
//
//
//    // Call the gRPC Stub.
//
////    // This is just a translation layer. Transmit the above `preparePacket` to a remote peer via BTP.
////    final BtpSubProtocol ilpSubProtocol = IlpBtpSubprotocolHandler
////        .toBtpSubprotocol(preparePacket, ilpCodecContext);
////    final BtpMessage btpMessage = BtpMessage.builder()
////        .requestId(nextRequestId())
////        .subProtocols(BtpSubProtocols.fromPrimarySubProtocol(ilpSubProtocol))
////        .build();
//
////
////    final CompletableFuture<Optional<InterledgerResponsePacket>> response = this.doSendDataOverBtp(btpMessage)
////        .thenApply(btpResponse -> btpResponse
////            .map($ -> IlpBtpSubprotocolHandler.toIlpPacket($, ilpCodecContext))
////            .map(Optional::of)
////            .orElse(Optional.empty())
////        )
////        .thenApply(ilpPacket -> ilpPacket
////            .map(p -> {
////              // Convert the ilpPacket into either a fulfill or an exception.
////              // TODO Use InterlederPacketHandler if this sticks around...
////              if (InterledgerFulfillPacket.class.isAssignableFrom(p.getClass())) {
////                return (InterledgerFulfillPacket) p;
////              } else {
////                return (InterledgerRejectPacket) p;
////              }
////            })
////            .map(Optional::of)
////            .orElse(Optional.empty())
////        );
////
////    // NOTE: Request/Response matching is a function of Websockets and being able to
////    return response;
//
//    return null;
//  }

  protected CodecContext getIlpCodecContext() {
    return ilpCodecContext;
  }

  /**
   * Perform the logic of settling with a remote peer.
   *
   * @param amount
   */
  //@Override
  protected CompletableFuture<Void> doSendMoney(BigInteger amount) {
    // No-op in vanilla BTP. Can be extended by an ILP Plugin.
    return CompletableFuture.completedFuture(null);
  }

//  @Override
//  public Optional<BilateralDataHandler> getDataHandler() {
//    // When this plugin receives a new DataHandler, it must be connected to teh BtpSubProtocol registered in the registry,
//    // so we always just return that handler, if present.
//    return this.getBtpSubProtocolHandlerRegistry()
//        .getHandler(BTP_SUB_PROTOCOL_ILP, MIME_APPLICATION_OCTET_STREAM)
//        .map(ilpHandler -> (IlpBtpSubprotocolHandler) ilpHandler)
//        .map(IlpBtpSubprotocolHandler::getDataHandler);
//  }
//
//  /**
//   * Removes the currently used {@link BilateralDataHandler}. This has the same effect as if {@link
//   * #registerDataHandler(BilateralDataHandler)} had never been called. If no data handler is currently set, this method
//   * does nothing.
//   */
//  @Override
//  public void unregisterDataHandler() {
//    final IlpBtpSubprotocolHandler handler =
//        this.getBtpSubProtocolHandlerRegistry()
//            .getHandler(BTP_SUB_PROTOCOL_ILP, MIME_APPLICATION_OCTET_STREAM)
//            .map(abstractHandler -> (IlpBtpSubprotocolHandler) abstractHandler)
//            .orElseThrow(() -> new RuntimeException(
//                String.format("BTP subprotocol handler with name `%s` MUST be registered!", BTP_SUB_PROTOCOL_ILP)));
//    handler.unregisterDataHandler();
//  }
//
//  @Override
//  public void registerDataHandler(final BilateralDataHandler ilpDataHandler)
//      throws DataHandlerAlreadyRegisteredException {
//    // The BilateralDataHandler for Btp Plugins is always the ILP handler registered with the BtpProtocolRegistry, so setting
//    // a handler here should overwrite the handler there.
//
//    final IlpBtpSubprotocolHandler handler =
//        this.getBtpSubProtocolHandlerRegistry()
//            .getHandler(BTP_SUB_PROTOCOL_ILP, MIME_APPLICATION_OCTET_STREAM)
//            .map(abstractHandler -> (IlpBtpSubprotocolHandler) abstractHandler)
//            .orElseThrow(() -> new RuntimeException(
//                String.format("BTP subprotocol handler with name `%s` MUST be registered!", BTP_SUB_PROTOCOL_ILP)));
//    handler.registerDataHandler(getPluginSettings().getLocalNodeAddress(), ilpDataHandler);
//  }


}
