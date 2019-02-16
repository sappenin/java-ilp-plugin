package org.interledger.lpiv2.blast;

import org.interledger.core.InterledgerPreparePacket;
import org.interledger.core.InterledgerResponsePacket;
import org.interledger.plugin.lpiv2.AbstractPlugin;
import org.interledger.plugin.lpiv2.PluginType;
import org.interledger.plugin.lpiv2.events.PluginEventEmitter;

import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * An {@link AbstractPlugin} that handles BLAST (aka, ILP over HTTP) connections.
 *
 * @see "https://github.com/interledger/rfcs/TODO"
 */
public class BlastPlugin extends AbstractPlugin<BlastPluginSettings> {

  public static final String PLUGIN_TYPE_STRING = "BlastPlugin";
  public static final PluginType PLUGIN_TYPE = PluginType.of(PLUGIN_TYPE_STRING);

  // This RestTemplate is shared between all plugins...
  private BlastHttpSender blastHttpSender;

  /**
   * Required-args Constructor. Utilizes a default {@link PluginEventEmitter} that synchronously connects to any event
   * handlers.
   *
   * @param pluginSettings A {@link BlastPluginSettings} that specified ledger plugin options.
   * @param restTemplate   A {@link RestTemplate} used to communicate with the remote BLAST peer.
   */
  public BlastPlugin(final BlastPluginSettings pluginSettings, final RestTemplate restTemplate) {
    super(pluginSettings);
    this.blastHttpSender = new BlastHttpSender(
        pluginSettings.getOperatorAddress(),
        pluginSettings.getOutgoingUrl().uri(),
        restTemplate,
        () -> pluginSettings.getOutgoingSecret().getBytes()
    );
  }

  /**
   * Required-args Constructor.
   *
   * @param pluginSettings     A {@link BlastPluginSettings} that specified ledger plugin options.
   * @param pluginEventEmitter A {@link PluginEventEmitter} that is used to emit events from this plugin.
   */
  public BlastPlugin(
      final BlastPluginSettings pluginSettings,
      final RestTemplate restTemplate,
      final PluginEventEmitter pluginEventEmitter
  ) {
    super(pluginSettings, pluginEventEmitter);
    this.blastHttpSender = new BlastHttpSender(
        pluginSettings.getOperatorAddress(),
        pluginSettings.getOutgoingUrl().uri(),
        restTemplate,
        () -> pluginSettings.getOutgoingSecret().getBytes());
  }

  /**
   * Reconfigure this plugin with a new {@link BlastPluginSettings}.
   *
   * @param blastPluginSettings
   */
  public void reconfigure(final BlastPluginSettings blastPluginSettings) {
    this.blastHttpSender = new BlastHttpSender(
        blastPluginSettings.getOperatorAddress(),
        blastPluginSettings.getOutgoingUrl().uri(),
        blastHttpSender.getRestTemplate(),
        () -> blastPluginSettings.getOutgoingSecret().getBytes()
    );
  }

  /**
   * Perform the logic of actually connecting to the remote peer.
   */
  @Override
  public CompletableFuture<Void> doConnect() {
    // No-op.
    return CompletableFuture.supplyAsync(() -> {
      // If the peer is not up, the server operating this plugin will warn, but will not fail. One side needs to
      // startup first, so it's likely that this test will fail for the first side to startup, but can be useful for
      // connection debugging.
      blastHttpSender.testConnection();
      return null;
    });
  }

  /**
   * Perform the logic of disconnecting from the remote peer.
   */
  @Override
  public CompletableFuture<Void> doDisconnect() {
    // No-op.
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> sendMoney(BigInteger amount) {
    // No-op.
    return CompletableFuture.completedFuture(null);
  }

  @Override
  @Async
  public CompletableFuture<Optional<InterledgerResponsePacket>> sendData(final InterledgerPreparePacket preparePacket) {
    Objects.requireNonNull(preparePacket);

    // While this call to blastHttpSender appears to block, the @Async annotation in this method actually instructs
    // Spring to wrap the entire method in a Proxy that runs in a separate thread. Thus, this return is simply to
    // conform to the Java API.
    return CompletableFuture.completedFuture(Optional.ofNullable(blastHttpSender.sendData(preparePacket)));
  }
}
