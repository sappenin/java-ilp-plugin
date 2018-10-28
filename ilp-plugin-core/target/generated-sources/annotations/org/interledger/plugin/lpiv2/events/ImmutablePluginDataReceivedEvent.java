package org.interledger.plugin.lpiv2.events;

import com.google.common.base.MoreObjects;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.annotation.Generated;
import org.interledger.core.InterledgerAddress;

/**
 * Immutable implementation of {@link PluginDataReceivedEvent.AbstractPluginDataReceivedEvent}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutablePluginDataReceivedEvent.builder()}.
 */
@SuppressWarnings({"all"})
@Generated("org.immutables.processor.ProxyProcessor")
@org.immutables.value.Generated(from = "PluginDataReceivedEvent.AbstractPluginDataReceivedEvent", generator = "Immutables")
public final class ImmutablePluginDataReceivedEvent
    extends PluginDataReceivedEvent.AbstractPluginDataReceivedEvent {
  private final byte[] data;
  private final InterledgerAddress peerAccountAddress;

  private ImmutablePluginDataReceivedEvent(byte[] data, InterledgerAddress peerAccountAddress) {
    this.data = data;
    this.peerAccountAddress = peerAccountAddress;
  }

  /**
   * The data payload that was received via an incoming {@link InterledgerPreparePacket}.
   * @return A byte-array containing the raw data bits received from an incoming ILP prepare operation.
   */
  @Override
  public byte[] getData() {
    return data.clone();
  }

  /**
   * The ILP Address-prefix of the LPIv2 plugin that emitted this event.
   */
  @Override
  public InterledgerAddress getPeerAccountAddress() {
    return peerAccountAddress;
  }

  /**
   * Copy the current immutable object with elements that replace the content of {@link PluginDataReceivedEvent.AbstractPluginDataReceivedEvent#getData() data}.
   * The array is cloned before being saved as attribute values.
   * @param elements The non-null elements for data
   * @return A modified copy of {@code this} object
   */
  public final ImmutablePluginDataReceivedEvent withData(byte... elements) {
    byte[] newValue = elements.clone();
    return new ImmutablePluginDataReceivedEvent(newValue, this.peerAccountAddress);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link PluginDataReceivedEvent.AbstractPluginDataReceivedEvent#getPeerAccountAddress() peerAccountAddress} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for peerAccountAddress
   * @return A modified copy of the {@code this} object
   */
  public final ImmutablePluginDataReceivedEvent withPeerAccountAddress(InterledgerAddress value) {
    if (this.peerAccountAddress == value) return this;
    InterledgerAddress newValue = Objects.requireNonNull(value, "peerAccountAddress");
    return new ImmutablePluginDataReceivedEvent(this.data, newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutablePluginDataReceivedEvent} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(Object another) {
    if (this == another) return true;
    return another instanceof ImmutablePluginDataReceivedEvent
        && equalTo((ImmutablePluginDataReceivedEvent) another);
  }

  private boolean equalTo(ImmutablePluginDataReceivedEvent another) {
    return Arrays.equals(data, another.data)
        && peerAccountAddress.equals(another.peerAccountAddress);
  }

  /**
   * Computes a hash code from attributes: {@code data}, {@code peerAccountAddress}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    int h = 5381;
    h += (h << 5) + Arrays.hashCode(data);
    h += (h << 5) + peerAccountAddress.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code PluginDataReceivedEvent} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("PluginDataReceivedEvent")
        .omitNullValues()
        .add("data", Arrays.toString(data))
        .add("peerAccountAddress", peerAccountAddress)
        .toString();
  }

  /**
   * Creates an immutable copy of a {@link PluginDataReceivedEvent.AbstractPluginDataReceivedEvent} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable PluginDataReceivedEvent instance
   */
  public static ImmutablePluginDataReceivedEvent copyOf(PluginDataReceivedEvent.AbstractPluginDataReceivedEvent instance) {
    if (instance instanceof ImmutablePluginDataReceivedEvent) {
      return (ImmutablePluginDataReceivedEvent) instance;
    }
    return ImmutablePluginDataReceivedEvent.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutablePluginDataReceivedEvent ImmutablePluginDataReceivedEvent}.
   * @return A new ImmutablePluginDataReceivedEvent builder
   */
  public static ImmutablePluginDataReceivedEvent.Builder builder() {
    return new ImmutablePluginDataReceivedEvent.Builder();
  }

  /**
   * Builds instances of type {@link ImmutablePluginDataReceivedEvent ImmutablePluginDataReceivedEvent}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  public static final class Builder {
    private static final long INIT_BIT_DATA = 0x1L;
    private static final long INIT_BIT_PEER_ACCOUNT_ADDRESS = 0x2L;
    private long initBits = 0x3L;

    private byte[] data;
    private InterledgerAddress peerAccountAddress;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code org.interledger.plugin.lpiv2.events.PluginDataReceivedEvent} instance.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder from(PluginDataReceivedEvent instance) {
      Objects.requireNonNull(instance, "instance");
      from((Object) instance);
      return this;
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
     * Fill a builder with attribute values from the provided {@code org.interledger.plugin.lpiv2.events.PluginDataReceivedEvent.AbstractPluginDataReceivedEvent} instance.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder from(PluginDataReceivedEvent.AbstractPluginDataReceivedEvent instance) {
      Objects.requireNonNull(instance, "instance");
      from((Object) instance);
      return this;
    }

    private void from(Object object) {
      if (object instanceof PluginDataReceivedEvent) {
        PluginDataReceivedEvent instance = (PluginDataReceivedEvent) object;
        data(instance.getData());
      }
      if (object instanceof PluginEvent) {
        PluginEvent instance = (PluginEvent) object;
        peerAccountAddress(instance.getPeerAccountAddress());
      }
    }

    /**
     * Initializes the value for the {@link PluginDataReceivedEvent.AbstractPluginDataReceivedEvent#getData() data} attribute.
     * @param data The elements for data
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder data(byte... data) {
      this.data = data.clone();
      initBits &= ~INIT_BIT_DATA;
      return this;
    }

    /**
     * Initializes the value for the {@link PluginDataReceivedEvent.AbstractPluginDataReceivedEvent#getPeerAccountAddress() peerAccountAddress} attribute.
     * @param peerAccountAddress The value for peerAccountAddress 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder peerAccountAddress(InterledgerAddress peerAccountAddress) {
      this.peerAccountAddress = Objects.requireNonNull(peerAccountAddress, "peerAccountAddress");
      initBits &= ~INIT_BIT_PEER_ACCOUNT_ADDRESS;
      return this;
    }

    /**
     * Builds a new {@link ImmutablePluginDataReceivedEvent ImmutablePluginDataReceivedEvent}.
     * @return An immutable instance of PluginDataReceivedEvent
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutablePluginDataReceivedEvent build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutablePluginDataReceivedEvent(data, peerAccountAddress);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_DATA) != 0) attributes.add("data");
      if ((initBits & INIT_BIT_PEER_ACCOUNT_ADDRESS) != 0) attributes.add("peerAccountAddress");
      return "Cannot build PluginDataReceivedEvent, some of required attributes are not set " + attributes;
    }
  }
}
