package org.interledger.plugin.connections;

import org.interledger.plugin.support.Wrapped;
import org.interledger.plugin.support.Wrapper;

import org.immutables.value.Value;

/**
 * Wrapped immutable classes for providing type-safe identifiers.
 */
public class Ids {

  /**
   * A wrapper that defines a "type" of org.interledger.bilateral based upon a unique String. For example, "gRPC" or "WebSockets".
   */
  @Value.Immutable
  @Wrapped
  static abstract class _BilateralConnectionType extends Wrapper<String> {

  }

}
