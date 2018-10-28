package org.interledger.plugin.lpiv2.events;

import com.google.common.base.MoreObjects;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Generated;
import org.interledger.core.InterledgerAddress;

/**
 * Immutable implementation of {@link PluginConnectedEvent.AbstractPluginConnectedEvent}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutablePluginConnectedEvent.builder()}.
 */
@SuppressWarnings({"all"})
@Generated("org.immutables.processor.ProxyProcessor")
@org.immutables.value.Generated(from = "PluginConnectedEvent.AbstractPluginConnectedEvent", generator = "Immutables")
public final class ImmutablePluginConnectedEvent
    extends PluginConnectedEvent.AbstractPluginConnectedEvent {
  private final InterledgerAddress peerAccountAddress;

  private ImmutablePluginConnectedEvent(InterledgerAddress peerAccountAddress) {
    this.peerAccountAddress = peerAccountAddress;
  }

  /**
   * The ILP Address-prefix of the LPIv2 plugin that emitted this event.
   */
  @Override
  public InterledgerAddress getPeerAccountAddress() {
    return peerAccountAddress;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link PluginConnectedEvent.AbstractPluginConnectedEvent#getPeerAccountAddress() peerAccountAddress} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for peerAccountAddress
   * @return A modified copy of the {@code this} object
   */
  public final ImmutablePluginConnectedEvent withPeerAccountAddress(InterledgerAddress value) {
    if (this.peerAccountAddress == value) return this;
    InterledgerAddress newValue = Objects.requireNonNull(value, "peerAccountAddress");
    return new ImmutablePluginConnectedEvent(newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutablePluginConnectedEvent} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(Object another) {
    if (this == another) return true;
    return another instanceof ImmutablePluginConnectedEvent
        && equalTo((ImmutablePluginConnectedEvent) another);
  }

  private boolean equalTo(ImmutablePluginConnectedEvent another) {
    return peerAccountAddress.equals(another.peerAccountAddress);
  }

  /**
   * Computes a hash code from attributes: {@code peerAccountAddress}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    int h = 5381;
    h += (h << 5) + peerAccountAddress.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code PluginConnectedEvent} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("PluginConnectedEvent")
        .omitNullValues()
        .add("peerAccountAddress", peerAccountAddress)
        .toString();
  }

  /**
   * Creates an immutable copy of a {@link PluginConnectedEvent.AbstractPluginConnectedEvent} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable PluginConnectedEvent instance
   */
  public static ImmutablePluginConnectedEvent copyOf(PluginConnectedEvent.AbstractPluginConnectedEvent instance) {
    if (instance instanceof ImmutablePluginConnectedEvent) {
      return (ImmutablePluginConnectedEvent) instance;
    }
    return ImmutablePluginConnectedEvent.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutablePluginConnectedEvent ImmutablePluginConnectedEvent}.
   * @return A new ImmutablePluginConnectedEvent builder
   */
  public static ImmutablePluginConnectedEvent.Builder builder() {
    return new ImmutablePluginConnectedEvent.Builder();
  }

  /**
   * Builds instances of type {@link ImmutablePluginConnectedEvent ImmutablePluginConnectedEvent}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  public static final class Builder {
    private static final long INIT_BIT_PEER_ACCOUNT_ADDRESS = 0x1L;
    private long initBits = 0x1L;

    private InterledgerAddress peerAccountAddress;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code org.interledger.plugin.lpiv2.events.PluginEvent} instance.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder from(PluginEvent instance) {
      Objects.requireNonNull(instance, "instance");
      from((Object) instance);
      return this;
    }

    /**
     * Fill a builder with attribute values from the provided {@code org.interledger.plugin.lpiv2.events.PluginConnectedEvent.AbstractPluginConnectedEvent} instance.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder from(PluginConnectedEvent.AbstractPluginConnectedEvent instance) {
      Objects.requireNonNull(instance, "instance");
      from((Object) instance);
      return this;
    }

    private void from(Object object) {
      if (object instanceof PluginEvent) {
        PluginEvent instance = (PluginEvent) object;
        peerAccountAddress(instance.getPeerAccountAddress());
      }
    }

    /**
     * Initializes the value for the {@link PluginConnectedEvent.AbstractPluginConnectedEvent#getPeerAccountAddress() peerAccountAddress} attribute.
     * @param peerAccountAddress The value for peerAccountAddress 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder peerAccountAddress(InterledgerAddress peerAccountAddress) {
      this.peerAccountAddress = Objects.requireNonNull(peerAccountAddress, "peerAccountAddress");
      initBits &= ~INIT_BIT_PEER_ACCOUNT_ADDRESS;
      return this;
    }

    /**
     * Builds a new {@link ImmutablePluginConnectedEvent ImmutablePluginConnectedEvent}.
     * @return An immutable instance of PluginConnectedEvent
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutablePluginConnectedEvent build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutablePluginConnectedEvent(peerAccountAddress);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_PEER_ACCOUNT_ADDRESS) != 0) attributes.add("peerAccountAddress");
      return "Cannot build PluginConnectedEvent, some of required attributes are not set " + attributes;
    }
  }
}
