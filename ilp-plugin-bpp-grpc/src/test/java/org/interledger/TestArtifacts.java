package org.interledger;

import org.interledger.core.InterledgerAddress;
import org.interledger.core.InterledgerErrorCode;
import org.interledger.core.InterledgerFulfillPacket;
import org.interledger.core.InterledgerFulfillment;
import org.interledger.core.InterledgerPreparePacket;
import org.interledger.core.InterledgerRejectPacket;
import org.interledger.core.asn.framework.InterledgerCodecContextFactory;
import org.interledger.encoding.asn.framework.CodecContext;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class TestArtifacts {

  public static final InterledgerFulfillment FULFILLMENT = InterledgerFulfillment.of(new byte[32]);
  public static final CodecContext CODEC_CONTEXT = InterledgerCodecContextFactory.oer();

  public static final InterledgerPreparePacket PREPARE_PACKET = InterledgerPreparePacket.builder()
      .destination(InterledgerAddress.of("test.goo"))
      .amount(BigInteger.TEN)
      .executionCondition(FULFILLMENT.getCondition())
      .expiresAt(Instant.now().plus(5, ChronoUnit.MINUTES))
      .build();

  public static final InterledgerFulfillPacket FULFILL_PACKET = InterledgerFulfillPacket.builder()
      .fulfillment(FULFILLMENT).build();

  public static final InterledgerRejectPacket REJECT_PACKET = InterledgerRejectPacket.builder()
      .code(InterledgerErrorCode.F00_BAD_REQUEST)
      .triggeredBy(InterledgerAddress.of("test.foo"))
      .message("none")
      .build();

  private static final SecureRandom RND = new SecureRandom();

  public static final InterledgerFulfillment FULFILLMENT_RND() {
    final byte[] bytes = RND.generateSeed(32);
    return InterledgerFulfillment.of(bytes);
  }

  public static final InterledgerPreparePacket PREPARE_PACKET_RND() {
    return InterledgerPreparePacket.builder()
        .destination(InterledgerAddress.of("test.goo").with(UUID.randomUUID().toString()))
        .amount(BigInteger.TEN)
        .executionCondition(FULFILLMENT_RND().getCondition())
        .expiresAt(Instant.now().plus(5, ChronoUnit.MINUTES))
        .build();
  }

  public static final InterledgerRejectPacket REJECT_PACKET_RND() {
    return InterledgerRejectPacket.builder()
        .code(InterledgerErrorCode.F00_BAD_REQUEST)
        .triggeredBy(InterledgerAddress.of("test." + UUID.randomUUID()))
        .message("none")
        .build();
  }

  public static final InterledgerFulfillPacket FULFILL_PACKET_RND() {
    return InterledgerFulfillPacket.builder().fulfillment(FULFILLMENT_RND()).build();
  }

}
