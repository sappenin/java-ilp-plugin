package org.interledger.plugin.lpiv2;

import org.interledger.core.InterledgerCondition;
import org.interledger.core.InterledgerErrorCode;
import org.interledger.core.InterledgerFulfillPacket;
import org.interledger.core.InterledgerFulfillment;
import org.interledger.core.InterledgerPreparePacket;
import org.interledger.core.InterledgerProtocolException;
import org.interledger.core.InterledgerRejectPacket;
import org.interledger.core.InterledgerResponsePacket;

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

/**
 * An implementation of {@link Plugin} that simulates a relationship with a parent node where this connector is the
 * child.
 */
public class SimulatedChildPlugin extends AbstractPlugin<PluginSettings> implements Plugin<PluginSettings> {

  public static final String PLUGIN_TYPE_STRING = "SimulatedChildPlugin";
  public static final PluginType PLUGIN_TYPE = PluginType.of(PLUGIN_TYPE_STRING);

  public static final byte[] ILP_DATA = "MARTY!".getBytes();
  public static final byte[] PREIMAGE = "Roads? Where we're going we dont".getBytes();

  public static final InterledgerFulfillment FULFILLMENT = InterledgerFulfillment.of(PREIMAGE);
  public static final InterledgerCondition CONDITION = FULFILLMENT.getCondition();

  private static final Logger LOGGER = LoggerFactory.getLogger(SimulatedChildPlugin.class);

  // For simulation purposes, allows a test-harness to flip this flag in order to simulate failed operations.
  private boolean completeSuccessfully = true;

  /**
   * Required-args Constructor.
   */
  public SimulatedChildPlugin(final PluginSettings pluginSettings) {
    super(pluginSettings);

    this.registerDataHandler((sourceAccountAddress, preparePacket) -> {
      if (completeSuccessfully) {
        return CompletableFuture.supplyAsync(() -> getSendDataFulfillPacket());
      } else {
        return CompletableFuture.supplyAsync(() -> getSendDataRejectPacket());
      }
    });

    this.registerMoneyHandler((amount -> {
      if (completeSuccessfully) {
        // No-Op.
        return CompletableFuture.supplyAsync(() -> null);
      } else {
        throw new RuntimeException("sendMoney failed!");
      }
    }));

  }

  /**
   * This Mock plugin completes successfully or throws an error, depending on the setting of {@link
   * #completeSuccessfully}.
   */
  @Override
  public CompletableFuture<InterledgerResponsePacket> doSendData(InterledgerPreparePacket preparePacket) {
    if (completeSuccessfully) {
      return CompletableFuture.supplyAsync(() -> InterledgerFulfillPacket.builder()
          .fulfillment(InterledgerFulfillment.of(PREIMAGE))
          .data(ILP_DATA)
          .build()
      );
    } else {
      return CompletableFuture.supplyAsync(() -> InterledgerRejectPacket.builder()
          .triggeredBy(getPluginSettings().getPeerAccountAddress())
          .code(InterledgerErrorCode.F00_BAD_REQUEST)
          .message("SendData failed!")
          .data(ILP_DATA)
          .build()
      );
    }
  }

  @Override
  protected CompletableFuture<Void> doSendMoney(BigInteger amount) {
    if (completeSuccessfully) {
      return CompletableFuture.completedFuture(null);
    } else {
      throw new InterledgerProtocolException(
          InterledgerRejectPacket.builder()
              .triggeredBy(getPluginSettings().getPeerAccountAddress())
              .code(InterledgerErrorCode.F00_BAD_REQUEST)
              .message("SendMoney failed!")
              .data(ILP_DATA)
              .build()
      );
    }
  }

  @Override
  public CompletableFuture<Void> doConnect() {
    // No-op
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> doDisconnect() {
    // No-op
    return CompletableFuture.completedFuture(null);
  }

  public void simulatedCompleteSuccessfully(final boolean completeSuccessfully) {
    this.completeSuccessfully = completeSuccessfully;
  }

  /**
   * Helper method to return the fulfill packet that is used by this simulated plugin.
   *
   * @return
   */
  @VisibleForTesting
  public final InterledgerFulfillPacket getSendDataFulfillPacket() {
    return InterledgerFulfillPacket.builder()
        .fulfillment(FULFILLMENT)
        .data(ILP_DATA)
        .build();
  }

  /**
   * Helper method to return the rejection packet that is used by this simulated plugin.
   *
   * @return
   */
  @VisibleForTesting
  public final InterledgerRejectPacket getSendDataRejectPacket() {
    return InterledgerRejectPacket.builder()
        .triggeredBy(getPluginSettings().getPeerAccountAddress())
        .code(InterledgerErrorCode.F00_BAD_REQUEST)
        .message("Handle SendData failed!")
        .data(ILP_DATA)
        .build();
  }
}