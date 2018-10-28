package org.interledger.plugin.lpiv2;

import org.interledger.core.InterledgerAddress;
import org.interledger.core.InterledgerFulfillment;

import com.google.common.collect.Maps;
import org.immutables.value.Value.Immutable;

import java.util.Map;

/**
 * An abstract class that provides a common test functionality for any plugins defined in this project.
 */
public class TestHelpers {

  public static final byte[] PREIMAGE = "Roads? Where we're going we dont".getBytes();
  public static final InterledgerFulfillment FULFILLMENT = InterledgerFulfillment.of(PREIMAGE);

  public static final byte[] ALTERNATE_PREIMAGE = "11inquagintaquadringentilliard11".getBytes();
  public static final InterledgerFulfillment ALTERNATE_FULFILLMENT = InterledgerFulfillment.of(ALTERNATE_PREIMAGE);

  protected static final InterledgerAddress LOCAL_NODE_ADDRESS = InterledgerAddress.of("test1.foo");
  protected static final InterledgerAddress PEER_ACCOUNT = InterledgerAddress.of("test1.b");

  public static ExtendedPluginSettings newPluginSettings() {
    return new ExtendedPluginSettings() {

      @Override
      public PluginType getPluginType() {
        return PluginType.of("ilp-plugin-mock");
      }

      /**
       * The ILP Address for remote peer account this Plugin is connecting to...
       */
      @Override
      public InterledgerAddress getPeerAccountAddress() {
        return PEER_ACCOUNT;
      }

      /**
       * The ILP address of the ILP Node operating this plugin.
       */
      @Override
      public InterledgerAddress getLocalNodeAddress() {
        return LOCAL_NODE_ADDRESS;
      }

      /**
       * Additional, custom settings that any plugin can define.
       */
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

  /**
   * An example of how to configure custom, though typed, configuration for a plugin.
   */
  @Immutable
  public interface ExtendedPluginSettings extends PluginSettings {

    /**
     * The password for the connector account on the ledger.
     */
    String getPassword();
  }

}