package org.interledger.plugin.lpiv2.btp2.spring.connection;


import org.interledger.core.InterledgerAddress;
import org.interledger.core.InterledgerFulfillPacket;
import org.interledger.core.InterledgerFulfillment;
import org.interledger.core.InterledgerPreparePacket;

import java.math.BigInteger;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * An abstract class that provides a common test functionality for any plugins defined in this project.
 */
public class TestHelpers {

  public static final byte[] PREIMAGE = "Roads? Where we're going we dont".getBytes();
  public static final InterledgerFulfillment FULFILLMENT = InterledgerFulfillment.of(PREIMAGE);
//  public static final byte[] ALTERNATE_PREIMAGE = "11inquagintaquadringentilliard11".getBytes();
//  public static final InterledgerFulfillment ALTERNATE_FULFILLMENT = InterledgerFulfillment.of(ALTERNATE_PREIMAGE);

  protected static final InterledgerAddress LOCAL_NODE_ADDRESS = InterledgerAddress.of("test1.foo");
  protected static final InterledgerAddress PEER_ACCOUNT = InterledgerAddress.of("test1.b");

  /*public static ExtendedPluginSettings newPluginSettings() {
    return new ExtendedPluginSettings() {

      @Override
      public PluginType getPluginType() {
        return PluginType.of("ilp-plugin-mock");
      }

      *//**
   * The ILP Address for remote peer account this Plugin is connecting to...
   *//*
      @Override
      public InterledgerAddress getAccountAddress() {
        return PEER_ACCOUNT;
      }

      *//**
   * The ILP address of the ILP Node operating this plugin.
   *//*
      @Override
      public InterledgerAddress getOperatorAddress() {
        return LOCAL_NODE_ADDRESS;
      }

      */

  /**
   * Additional, custom settings that any plugin can define.
   *//*
      @Override
      public Map<String, Object> getCustomSettings() {
        return Maps.newConcurrentMap();
      }

      @Override
      public String getPassword() {
        return "password";
      }
    };
  }
*/
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
//
//  public static final InterledgerRejectPacket getSendDataRejectPacket() {
//    return InterledgerRejectPacket.builder()
//        .triggeredBy(PEER_ACCOUNT)
//        .code(InterledgerErrorCode.F00_BAD_REQUEST)
//        .message("Handle SendData failed!")
//        .data(new byte[32])
//        .build();
//  }

//  /**
//   * An example of how to configure custom, though typed, configuration for a plugin.
//   */
//  @Immutable
//  public interface ExtendedPluginSettings extends PluginSettings {

//    /**
//     * The password for the connector account on the ledger.
//     */
//    String getPassword();
//  }
}