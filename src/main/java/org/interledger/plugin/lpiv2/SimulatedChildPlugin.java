package org.interledger.plugin.lpiv2;

import org.interledger.core.InterledgerErrorCode;
import org.interledger.core.InterledgerFulfillPacket;
import org.interledger.core.InterledgerFulfillment;
import org.interledger.core.InterledgerPreparePacket;
import org.interledger.core.InterledgerProtocolException;
import org.interledger.core.InterledgerRejectPacket;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

/**
 * An implementation of {@link Plugin} that simulates a relationship with a parent node where this connector is the
 * child.
 */
public class SimulatedChildPlugin extends AbstractPlugin<PluginSettings> implements Plugin<PluginSettings> {

  public static final byte[] ILP_DATA = "MARTY!".getBytes();
  public static final byte[] PREIMAGE = "Roads? Where we're going we dont".getBytes();
  public static final byte[] ALTERNATE_PREIMAGE = "11inquagintaquadringentilliard11".getBytes();

  private boolean completeSuccessfully = true;

  /**
   * Required-args Constructor.
   */
  public SimulatedChildPlugin(final PluginSettings pluginSettings) {
    super(pluginSettings);
  }

  /**
   * This Mock plugin completes successfully or throws an error, depending on the setting of {@link
   * #completeSuccessfully}.
   */
  @Override
  public InterledgerFulfillPacket doSendPacket(InterledgerPreparePacket preparePacket)
      throws InterledgerProtocolException {
    if (completeSuccessfully) {
      return InterledgerFulfillPacket.builder()
          .data(ILP_DATA)
          .fulfillment(InterledgerFulfillment.of(PREIMAGE))
          .build();
    } else {
      throw new InterledgerProtocolException(
          InterledgerRejectPacket.builder()
              .data(ILP_DATA)
              .triggeredBy(getPluginSettings().peerAccount())
              .code(InterledgerErrorCode.F00_BAD_REQUEST)
              .message("Don't do this!")
              .build()
      );
    }
  }

  @Override
  protected void doSettle(BigInteger amount) {
    // No-Op.
  }

  /**
   * Handle an incoming Interledger data packets. If an error occurs, this method MAY throw an exception. In general,
   * the callback should behave as sendData does.
   *
   * @param preparePacket The ILP packet sent from a remote peer.
   *
   * @throws InterledgerProtocolException if the request is rejected by the peer.
   */
  @Override
  public InterledgerFulfillPacket doHandleIncomingPacket(InterledgerPreparePacket preparePacket)
      throws InterledgerProtocolException {

    if (completeSuccessfully) {
      return InterledgerFulfillPacket.builder()
          .data(ILP_DATA)
          .fulfillment(InterledgerFulfillment.of(PREIMAGE))
          .build();
    } else {
      throw new InterledgerProtocolException(
          InterledgerRejectPacket.builder()
              .data(ILP_DATA)
              .triggeredBy(getPluginSettings().peerAccount())
              .code(InterledgerErrorCode.F00_BAD_REQUEST)
              .message("Don't do this!")
              .build()
      );
    }
  }

  @Override
  public CompletableFuture<Void> handleIncomingSettle(BigInteger amount) {
    throw new RuntimeException("Not yet implemented!");
  }

  @Override
  public void doConnect() {
    // No-op
  }

  @Override
  public void doDisconnect() {
    // No-op
  }

  public void setCompleteSuccessfully(final boolean completeSuccessfully) {
    this.completeSuccessfully = completeSuccessfully;
  }
}