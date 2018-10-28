package org.interledger.plugin.lpiv2;

import org.interledger.plugin.lpiv2.support.Wrapped;
import org.interledger.plugin.lpiv2.support.Wrapper;

import org.immutables.value.Value;

/**
 * Wrapped immutable classes for providing type-safe identifiers.
 */
public class Ids {

  /**
   * A wrapper type that defines a "type" of ledger plugin based upon a unique String. For example,
   * "ilp-mock-plugin" or "btp2-plugin".
   */
  @Value.Immutable
  @Wrapped
  static abstract class _PluginType extends Wrapper<String> {

  }

}
