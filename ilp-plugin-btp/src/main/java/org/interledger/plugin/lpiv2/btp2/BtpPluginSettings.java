package org.interledger.plugin.lpiv2.btp2;

import org.interledger.plugin.lpiv2.PluginSettings;

import org.immutables.value.Value;

public interface BtpPluginSettings extends PluginSettings {

  /**
   * A typed key for the BTP Auth shared secret that both the client and server will use to authenticate a BTP session.
   */
  String KEY_SECRET = "secret";

  /**
   * The shared auth token, expected by a server or presented by a client, that both will use to authenticate a BTP
   * session.
   *
   * @return
   */
  String getSecret();

  @Value.Immutable
  abstract class AbstractBtpPluginSettings implements BtpPluginSettings {

  }
}
