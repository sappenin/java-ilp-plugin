package org.interledger.plugin.lpiv2.events;

import com.google.common.base.MoreObjects;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Generated;
import org.interledger.core.InterledgerAddress;

/**
 * Immutable implementation of {@link PluginErrorEvent.AbstractPluginErrorEvent}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutablePluginErrorEvent.builder()}.
 */
@SuppressWarnings({"all"})
@Generated("org.immutables.processor.ProxyProcessor")
@org.immutables.value.Generated(from = "PluginErrorEvent.AbstractPluginErrorEvent", generator = "Immutables")
public final class ImmutablePluginErrorEvent
    extends PluginErrorEvent.AbstractPluginErrorEvent {
  private final Exception error;
  private final InterledgerAddress peerAccountAddress;

  private ImmutablePluginErrorEvent(Exception error, InterledgerAddress peerAccountAddress) {
    this.error = error;
    this.peerAccountAddress = peerAccountAddress;
  }

  /**
   * @return An error that the plugin emitted.
   */
  @Override
  public Exception getError() {
    return error;
  }

  /**
   * The ILP Address-prefix of the LPIv2 plugin that emitted this event.
   */
  @Override
  public InterledgerAddress getPeerAccountAddress() {
    return peerAccountAddress;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link PluginErrorEvent.AbstractPluginErrorEvent#getError() error} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for error
   * @return A modified copy of the {@code this} object
   */
  public final ImmutablePluginErrorEvent withError(Exception value) {
    if (this.error == value) return this;
    Exception newValue = Objects.requireNonNull(value, "error");
    return new ImmutablePluginErrorEvent(newValue, this.peerAccountAddress);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link PluginErrorEvent.AbstractPluginErrorEvent#getPeerAccountAddress() peerAccountAddress} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for peerAccountAddress
   * @return A modified copy of the {@code this} object
   */
  public final ImmutablePluginErrorEvent withPeerAccountAddress(InterledgerAddress value) {
    if (this.peerAccountAddress == value) return this;
    InterledgerAddress newValue = Objects.requireNonNull(value, "peerAccountAddress");
    return new ImmutablePluginErrorEvent(this.error, newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutablePluginErrorEvent} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(Object another) {
    if (this == another) return true;
    return another instanceof ImmutablePluginErrorEvent
        && equalTo((ImmutablePluginErrorEvent) another);
  }

  private boolean equalTo(ImmutablePluginErrorEvent another) {
    return error.equals(another.error)
        && peerAccountAddress.equals(another.peerAccountAddress);
  }

  /**
   * Computes a hash code from attributes: {@code error}, {@code peerAccountAddress}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    int h = 5381;
    h += (h << 5) + error.hashCode();
    h += (h << 5) + peerAccountAddress.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code PluginErrorEvent} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("PluginErrorEvent")
        .omitNullValues()
        .add("error", error)
        .add("peerAccountAddress", peerAccountAddress)
        .toString();
  }

  /**
   * Creates an immutable copy of a {@link PluginErrorEvent.AbstractPluginErrorEvent} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable PluginErrorEvent instance
   */
  public static ImmutablePluginErrorEvent copyOf(PluginErrorEvent.AbstractPluginErrorEvent instance) {
    if (instance instanceof ImmutablePluginErrorEvent) {
      return (ImmutablePluginErrorEvent) instance;
    }
    return ImmutablePluginErrorEvent.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutablePluginErrorEvent ImmutablePluginErrorEvent}.
   * @return A new ImmutablePluginErrorEvent builder
   */
  public static ImmutablePluginErrorEvent.Builder builder() {
    return new ImmutablePluginErrorEvent.Builder();
  }

  /**
   * Builds instances of type {@link ImmutablePluginErrorEvent ImmutablePluginErrorEvent}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  public static final class Builder {
    private static final long INIT_BIT_ERROR = 0x1L;
    private static final long INIT_BIT_PEER_ACCOUNT_ADDRESS = 0x2L;
    private long initBits = 0x3L;

    private Exception error;
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
     * Fill a builder with attribute values from the provided {@code org.interledger.plugin.lpiv2.events.PluginErrorEvent} instance.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder from(PluginErrorEvent instance) {
      Objects.requireNonNull(instance, "instance");
      from((Object) instance);
      return this;
    }

    /**
     * Fill a builder with attribute values from the provided {@code org.interledger.plugin.lpiv2.events.PluginErrorEvent.AbstractPluginErrorEvent} instance.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder from(PluginErrorEvent.AbstractPluginErrorEvent instance) {
      Objects.requireNonNull(instance, "instance");
      from((Object) instance);
      return this;
    }

    private void from(Object object) {
      if (object instanceof PluginEvent) {
        PluginEvent instance = (PluginEvent) object;
        peerAccountAddress(instance.getPeerAccountAddress());
      }
      if (object instanceof PluginErrorEvent) {
        PluginErrorEvent instance = (PluginErrorEvent) object;
        error(instance.getError());
      }
    }

    /**
     * Initializes the value for the {@link PluginErrorEvent.AbstractPluginErrorEvent#getError() error} attribute.
     * @param error The value for error 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder error(Exception error) {
      this.error = Objects.requireNonNull(error, "error");
      initBits &= ~INIT_BIT_ERROR;
      return this;
    }

    /**
     * Initializes the value for the {@link PluginErrorEvent.AbstractPluginErrorEvent#getPeerAccountAddress() peerAccountAddress} attribute.
     * @param peerAccountAddress The value for peerAccountAddress 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder peerAccountAddress(InterledgerAddress peerAccountAddress) {
      this.peerAccountAddress = Objects.requireNonNull(peerAccountAddress, "peerAccountAddress");
      initBits &= ~INIT_BIT_PEER_ACCOUNT_ADDRESS;
      return this;
    }

    /**
     * Builds a new {@link ImmutablePluginErrorEvent ImmutablePluginErrorEvent}.
     * @return An immutable instance of PluginErrorEvent
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutablePluginErrorEvent build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutablePluginErrorEvent(error, peerAccountAddress);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_ERROR) != 0) attributes.add("error");
      if ((initBits & INIT_BIT_PEER_ACCOUNT_ADDRESS) != 0) attributes.add("peerAccountAddress");
      return "Cannot build PluginErrorEvent, some of required attributes are not set " + attributes;
    }
  }
}
