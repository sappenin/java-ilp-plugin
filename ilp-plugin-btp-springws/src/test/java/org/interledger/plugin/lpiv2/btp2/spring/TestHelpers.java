package org.interledger.plugin.lpiv2.btp2.spring;


import org.interledger.core.InterledgerAddress;
import org.interledger.core.InterledgerErrorCode;
import org.interledger.core.InterledgerFulfillPacket;
import org.interledger.core.InterledgerFulfillment;
import org.interledger.core.InterledgerPreparePacket;
import org.interledger.core.InterledgerRejectPacket;

import java.math.BigInteger;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * An abstract class that provides a common test functionality for any plugins defined in this project.
 */
public class TestHelpers {

  public static final byte[] PREIMAGE = "Roads? Where we're going we dont".getBytes();
  public static final InterledgerFulfillment FULFILLMENT = InterledgerFulfillment.of(PREIMAGE);

  public static final InterledgerPreparePacket constructSendDataPreparePacket(
      final InterledgerAddress destinationAddress
  ) {
    return InterledgerPreparePacket.builder()
        .amount(BigInteger.TEN)
        .executionCondition(FULFILLMENT.getCondition())
        .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
        .destination(destinationAddress)
        // Used by Loopback Plugin...
        .data(PREIMAGE)
        .build();
  }

  public static final InterledgerFulfillPacket constructSendDataFulfillPacket() {
    return InterledgerFulfillPacket.builder()
        .fulfillment(FULFILLMENT)
        .data(new byte[32])
        .build();
  }

  public static final InterledgerRejectPacket getSendDataRejectPacket(final InterledgerAddress triggeredBy) {
    return InterledgerRejectPacket.builder()
        .triggeredBy(triggeredBy)
        .code(InterledgerErrorCode.F00_BAD_REQUEST)
        .message("Handle SendData failed!")
        .data(new byte[32])
        .build();
  }
}