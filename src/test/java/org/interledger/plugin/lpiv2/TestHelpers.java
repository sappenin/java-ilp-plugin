package org.interledger.plugin.lpiv2;

import org.interledger.core.InterledgerAddress;

import org.immutables.value.Value.Immutable;

/**
 * An abstract class that provides a common test functionality for any plugins defined in this project.
 */
public class TestHelpers {

  protected static final byte[] PREIMAGE = "quinquagintaquadringentilliardth".getBytes();
  protected static final byte[] ALTERNATE_PREIMAGE = "11inquagintaquadringentilliard11".getBytes();

  protected static final InterledgerAddress LOCAL_NODE_ADDRESS = InterledgerAddress.of("test1.foo");
  protected static final InterledgerAddress PEER_ACCOUNT = InterledgerAddress.of("test1.b");

  public static ExtendedPluginSettings newPluginSettings() {
    return new ExtendedPluginSettings() {

      @Override
      public PluginType pluginTypeId() {
        return PluginType.of("ilp-plugin-mock");
      }

      /**
       * The ILP Address for remote peer account this Plugin is connecting to...
       */
      @Override
      public InterledgerAddress peerAccount() {
        return PEER_ACCOUNT;
      }

      /**
       * The ILP address of the ILP Node operating this plugin.
       */
      @Override
      public InterledgerAddress localNodeAddress() {
        return LOCAL_NODE_ADDRESS;
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