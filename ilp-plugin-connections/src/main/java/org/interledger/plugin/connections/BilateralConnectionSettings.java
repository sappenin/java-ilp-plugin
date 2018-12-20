package org.interledger.plugin.connections;

import org.immutables.value.Value.Immutable;

import java.util.Map;

/**
 * Configuration information relating to a {@link BilateralConnection}.
 */
public interface BilateralConnectionSettings {

  /**
   * The type of this ledger plugin.
   */
  BilateralConnectionType getBilateralConnectionType();

  /**
   * Additional, custom settings that any plugin can define.
   */
  Map<String, Object> getCustomSettings();

  @Immutable
  abstract class AbstractBilateralConnectionSettings implements BilateralConnectionSettings {

  }

}
