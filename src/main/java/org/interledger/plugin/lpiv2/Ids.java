package org.interledger.plugin.lpiv2;

import org.immutables.value.Value;

/**
 * Wrapped immutable classes for providing type-safe identifiers.
 */
public class Ids {

  /**
   * A wrapper type that defines a "type" of ledger plugin based upon a unique String. For example,
   * "ilp-mock-plugin" or "btp-plugin".
   */
  @Value.Immutable
  @Wrapped
  static abstract class _PluginType extends Wrapper<String> {

  }

}
