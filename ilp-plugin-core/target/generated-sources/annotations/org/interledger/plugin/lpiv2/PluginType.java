package org.interledger.plugin.lpiv2;

import java.util.Objects;
import javax.annotation.Generated;

/**
 * A wrapper type that defines a "type" of ledger plugin based upon a unique String. For example,
 * "ilp-mock-plugin" or "btp2-plugin".
 */
@SuppressWarnings({"all"})
@Generated("org.immutables.processor.ProxyProcessor")
@org.immutables.value.Generated(from = "Ids._PluginType", generator = "Immutables")
public final class PluginType extends Ids._PluginType {
  private final String value;

  private PluginType(String value) {
    this.value = Objects.requireNonNull(value, "value");
  }

  /**
   * @return The value of the {@code value} attribute
   */
  @Override
  public String value() {
    return value;
  }

  /**
   * This instance is equal to all instances of {@code PluginType} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(Object another) {
    if (this == another) return true;
    return another instanceof PluginType
        && equalTo((PluginType) another);
  }

  private boolean equalTo(PluginType another) {
    return value.equals(another.value);
  }

  /**
   * Computes a hash code from attributes: {@code value}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    int h = 5381;
    h += (h << 5) + value.hashCode();
    return h;
  }

  /**
   * Construct a new immutable {@code PluginType} instance.
   * @param value The value for the {@code value} attribute
   * @return An immutable PluginType instance
   */
  public static PluginType of(String value) {
    return new PluginType(value);
  }
}
