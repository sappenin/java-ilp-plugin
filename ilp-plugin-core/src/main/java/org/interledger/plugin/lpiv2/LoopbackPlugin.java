package org.interledger.plugin.lpiv2;

import org.interledger.core.InterledgerAddress;
import org.interledger.core.InterledgerFulfillPacket;
import org.interledger.core.InterledgerFulfillment;
import org.interledger.core.InterledgerPreparePacket;
import org.interledger.core.InterledgerResponsePacket;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * An implementation of {@link Plugin} that provides loopback functionality by always fulfilling an incoming prepare
 * packet using the first 32-bytes of the data-payload found inside the incoming prepare packet.
 */
public class LoopbackPlugin extends AbstractPlugin<PluginSettings> implements Plugin<PluginSettings> {

  public static final String PLUGIN_TYPE_STRING = "LoopbackPlugin";
  public static final PluginType PLUGIN_TYPE = PluginType.of(PLUGIN_TYPE_STRING);

  /**
   * Required-args Constructor.
   */
  public LoopbackPlugin(final InterledgerAddress operatorAddress) {
    super(pluginSettings(operatorAddress));

    // This is called when the other side of the account relationship has called sendData, and a packet has been
    // forward (usually through a Connector) to this plugin, which will handle the incoming prepare packet.
    this.registerDataHandler((incomingPreparePacket) -> CompletableFuture.supplyAsync(() -> {
          final byte[] preimage = Arrays.copyOfRange(incomingPreparePacket.getData(), 0, 32);
          return Optional.of(
              InterledgerFulfillPacket.builder()
                  .fulfillment(
                      InterledgerFulfillment.of(preimage)
                  )
                  .build()
          );
        })
    );

    this.registerMoneyHandler((amount) -> CompletableFuture.completedFuture(null));
  }

  private static final PluginSettings pluginSettings(final InterledgerAddress localNodeAddress) {
    return PluginSettings.builder()
        .pluginType(LoopbackPlugin.PLUGIN_TYPE)
        .operatorAddress(localNodeAddress)
        .build();
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

  /**
   * Any caller that calls this method will receive a Fulfill packet using the PREIMAGE that was encoded into the
   * supplied {@code preparePacket}.
   *
   * @param preparePacket An {@link InterledgerPreparePacket} with information that can be used to
   *
   * @return
   */
  @Override
  public CompletableFuture<Optional<InterledgerResponsePacket>> sendData(InterledgerPreparePacket preparePacket) {
    return CompletableFuture.supplyAsync(() -> {
      if (preparePacket.getData().length != 32) {
        throw new RuntimeException("Loopback Plugin must contain 32 bytes of data to use as a Preimage!");
      }

      final byte[] preimage = Arrays.copyOfRange(preparePacket.getData(), 0, 32);
      return Optional.of(InterledgerFulfillPacket.builder().fulfillment(InterledgerFulfillment.of(preimage)).build());
    });
  }


  @Override
  public CompletableFuture<Void> sendMoney(BigInteger amount) {
    // No-op. Loopback Plugins don't track balances.
    return CompletableFuture.completedFuture(null);
  }
}