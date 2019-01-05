package org.interledger.plugin;

import org.interledger.core.InterledgerAddress;
import org.interledger.core.InterledgerCondition;
import org.interledger.core.InterledgerPreparePacket;
import org.interledger.core.InterledgerResponsePacket;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Defines how a DataSender can ping remote Interledger nodes.
 */
public interface Ping extends DataSender {

  InterledgerCondition PING_PROTOCOL_CONDITION =
      InterledgerCondition.of(Base64.getDecoder().decode("Zmh6rfhivXdsj8GLjp+OIAiXFIVu4jOzkCpZHQ1fKSU="));

  /**
   * Send a 0-value payment to the destination and expect an ILP fulfillment, which demonstrates this sender has
   * send-data connectivity to the indicated destination address.
   *
   * @param destinationAddress
   */
  default CompletableFuture<Optional<InterledgerResponsePacket>> ping(final InterledgerAddress destinationAddress) {
    Objects.requireNonNull(destinationAddress);

    final InterledgerPreparePacket pingPacket = InterledgerPreparePacket.builder()
        .executionCondition(PING_PROTOCOL_CONDITION)
        // TODO: Make this timeout configurable!
        .expiresAt(Instant.now().plusSeconds(30))
        .amount(BigInteger.ZERO)
        .destination(destinationAddress)
        .build();

    return this.sendData(pingPacket);
  }
}
