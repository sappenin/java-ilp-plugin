package org.interledger.plugin.connections;

import org.interledger.core.InterledgerAddress;
import org.interledger.core.InterledgerFulfillment;
import org.interledger.core.InterledgerPreparePacket;
import org.interledger.plugin.lpiv2.LoopbackPlugin;

import java.math.BigInteger;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public abstract class AbstractConnectionTestHelper {

//  public static final byte[] USD_PREIMAGE = "Why don't you make like a tree a".getBytes();
//  public static final InterledgerFulfillment USD_FULFILLMENT = InterledgerFulfillment.of(USD_PREIMAGE);
//
//  public static final byte[] EUR_PREIMAGE = "It's make like a tree and leaf y".getBytes();
//  public static final InterledgerFulfillment EUR_FULFILLMENT = InterledgerFulfillment.of(EUR_PREIMAGE);
//
//  public static final InterledgerPreparePacket USD_PREPARE_PACKET = InterledgerPreparePacket.builder()
//      .executionCondition(LoopbackPlugin.FULFILLMENT.getCondition())
//      .expiresAt(Instant.now().plus(30, ChronoUnit.SECONDS))
//      .destination(InterledgerAddress.of("test.foo"))
//      .amount(BigInteger.TEN)
//      .build();
//  public static final InterledgerPreparePacket EUR_PREPARE_PACKET = InterledgerPreparePacket.builder()
//      .executionCondition(LoopbackPlugin.FULFILLMENT.getCondition())
//      .expiresAt(Instant.now().plus(30, ChronoUnit.SECONDS))
//      .destination(InterledgerAddress.of("test.foo"))
//      .amount(BigInteger.TEN)
//      .build();

  // Simulates an account with a peer called `mypeer`
  public static final InterledgerAddress OPERATOR_ADDRESS = InterledgerAddress.of("test1.node.operator");

  // Simulates a USD account with the peer
  public static final InterledgerAddress USD_ACCOUNT_ADDRESS = OPERATOR_ADDRESS.with("usd");

  // Simulates a EUR account with the peer
  public static final InterledgerAddress EUR_ACCOUNT_ADDRESS = OPERATOR_ADDRESS.with("eur");
}
