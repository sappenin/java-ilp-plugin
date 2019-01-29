package org.interledger.plugin.lpiv2;

import org.interledger.plugin.support.Wrapped;
import org.interledger.plugin.support.Wrapper;

import org.immutables.value.Value;

/**
 * Wrapped immutable classes for providing type-safe identifiers.
 */
public class Ids {

  /**
   * A unique identifier for a {@link Plugin}. This value is generally only set once, (e.g., in a Connector to correlate
   * a {@link Plugin} to an account identifier) so that a plugin can be referenced across requests.
   */
  @Value.Immutable
  @Wrapped
  static abstract class _PluginId extends Wrapper<String> {

  }

  /**
   * A wrapper that defines a "type" of ledger plugin based upon a unique String. For example, "simulated-plugin" or
   * "btp2-plugin".
   */
  @Value.Immutable
  @Wrapped
  static abstract class _PluginType extends Wrapper<String> {

  }

}
