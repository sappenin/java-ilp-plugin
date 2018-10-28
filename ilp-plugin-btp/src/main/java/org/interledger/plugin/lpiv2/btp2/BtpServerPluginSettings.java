package org.interledger.plugin.lpiv2.btp2;

import org.immutables.value.Value;

public interface BtpServerPluginSettings extends BtpPluginSettings {

  static ImmutableBtpServerPluginSettings.Builder builder() {
    return ImmutableBtpServerPluginSettings.builder();
  }

  @Value.Immutable
  abstract class AbstractBtpServerPluginSettings implements BtpServerPluginSettings {

  }

}
