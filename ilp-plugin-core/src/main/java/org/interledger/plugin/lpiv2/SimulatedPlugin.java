package org.interledger.plugin.lpiv2;

import org.interledger.core.InterledgerCondition;
import org.interledger.core.InterledgerErrorCode;
import org.interledger.core.InterledgerFulfillPacket;
import org.interledger.core.InterledgerFulfillment;
import org.interledger.core.InterledgerPreparePacket;
import org.interledger.core.InterledgerRejectPacket;
import org.interledger.core.InterledgerResponsePacket;
import org.interledger.plugin.lpiv2.settings.PluginSettings;

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

enum ExpectedSendDataCompletionState {
  FULFILL,
  REJECT,
  EXPIRE
}

enum ExpectedSendMoneyCompletionState {
  SUCCESS,
  FAILURE
}

/**
 * An implementation of {@link Plugin} that simulates a relationship with a peer node.
 */
public class SimulatedPlugin extends AbstractPlugin<PluginSettings> implements Plugin<PluginSettings> {

  public static final String PLUGIN_TYPE_STRING = "SimulatedPlugin";
  public static final PluginType PLUGIN_TYPE = PluginType.of(PLUGIN_TYPE_STRING);

  public static final byte[] ILP_DATA = "MARTY!".getBytes();
  public static final byte[] PREIMAGE = "Roads? Where we're going we dont".getBytes();

  public static final InterledgerFulfillment FULFILLMENT = InterledgerFulfillment.of(PREIMAGE);
  public static final InterledgerCondition CONDITION = FULFILLMENT.getCondition();

  private static final Logger LOGGER = LoggerFactory.getLogger(SimulatedPlugin.class);

  // For simulation purposes, allows a test-harness to flip this flag in order to simulate failed operations.
  private ExpectedSendDataCompletionState expectedSendDataCompletionState = ExpectedSendDataCompletionState.FULFILL;

  // For simulation purposes, allows a test-harness to flip this flag in order to simulate failed operations.
  private ExpectedSendMoneyCompletionState expectedSendMoneyCompletionState = ExpectedSendMoneyCompletionState.SUCCESS;

  /**
   * Required-args Constructor.
   */
  public SimulatedPlugin(final PluginSettings pluginSettings) {
    super(pluginSettings);

    this.registerDataHandler((
        //sourceAccountAddress,
        preparePacket) -> {
      switch (expectedSendDataCompletionState) {
        case FULFILL: {
          return CompletableFuture.supplyAsync(() -> Optional.of(getSendDataFulfillPacket()));
        }
        case REJECT: {
          return CompletableFuture.supplyAsync(() -> Optional.of(getSendDataRejectPacket()));
        }
        case EXPIRE:
        default: {
          return CompletableFuture.supplyAsync(() -> Optional.empty());
        }
      }
    });

    this.registerMoneyHandler((amount) -> {
          switch (expectedSendMoneyCompletionState) {
            case SUCCESS: {
              return CompletableFuture.supplyAsync(() -> null);
            }
            case FAILURE:
            default: {
              return CompletableFuture.supplyAsync(() -> {
                throw new RuntimeException("sendMoney failed!");
              });
            }
          }
        }
    );
  }

  /**
   * This Mock plugin completes successfully or throws an error, depending on the setting of {@link
   * #expectedSendDataCompletionState}.
   */
  @Override
  public CompletableFuture<Optional<InterledgerResponsePacket>> doSendData(
      final InterledgerPreparePacket preparePacket
  ) {
    Objects.requireNonNull(preparePacket);

    switch (expectedSendDataCompletionState) {
      case FULFILL: {
        return CompletableFuture.supplyAsync(() -> Optional.of(
            InterledgerFulfillPacket.builder()
                .fulfillment(InterledgerFulfillment.of(PREIMAGE))
                .data(ILP_DATA)
                .build()
            )
        );
      }
      case REJECT: {
        return CompletableFuture.supplyAsync(() -> Optional.of(
            InterledgerRejectPacket.builder()
                .triggeredBy(getPluginSettings().getPeerAccountAddress())
                .code(InterledgerErrorCode.F00_BAD_REQUEST)
                .message("SendData failed!")
                .data(ILP_DATA)
                .build()
            )
        );
      }
      case EXPIRE:
      default: {
        return CompletableFuture.supplyAsync(() -> Optional.empty());
      }
    }
  }

  @Override
  protected CompletableFuture<Void> doSendMoney(BigInteger amount) {

    switch (expectedSendMoneyCompletionState) {
      case SUCCESS: {
        return CompletableFuture.completedFuture(null);
      }
      case FAILURE:
      default: {
        return CompletableFuture.supplyAsync(() -> {
          throw new RuntimeException("sendMoney Failed!");
        });
      }
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

  public void setExpectedSendDataCompletionState(
      final ExpectedSendDataCompletionState expectedSendDataCompletionState
  ) {
    this.expectedSendDataCompletionState = Objects.requireNonNull(expectedSendDataCompletionState);
  }

  public void setExpectedSendMoneyCompletionState(
      final ExpectedSendMoneyCompletionState expectedSendMoneyCompletionState
  ) {
    this.expectedSendMoneyCompletionState = Objects.requireNonNull(expectedSendMoneyCompletionState);
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