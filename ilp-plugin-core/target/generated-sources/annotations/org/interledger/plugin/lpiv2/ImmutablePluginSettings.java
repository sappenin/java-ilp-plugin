package org.interledger.plugin.lpiv2;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Generated;
import org.interledger.core.InterledgerAddress;

/**
 * Immutable implementation of {@link PluginSettings.AbstractPluginSettings}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutablePluginSettings.builder()}.
 */
@SuppressWarnings({"all"})
@Generated("org.immutables.processor.ProxyProcessor")
@org.immutables.value.Generated(from = "PluginSettings.AbstractPluginSettings", generator = "Immutables")
public final class ImmutablePluginSettings
    extends PluginSettings.AbstractPluginSettings {
  private final PluginType pluginType;
  private final InterledgerAddress peerAccountAddress;
  private final InterledgerAddress localNodeAddress;
  private final ImmutableMap<String, Object> customSettings;

  private ImmutablePluginSettings(
      PluginType pluginType,
      InterledgerAddress peerAccountAddress,
      InterledgerAddress localNodeAddress,
      ImmutableMap<String, Object> customSettings) {
    this.pluginType = pluginType;
    this.peerAccountAddress = peerAccountAddress;
    this.localNodeAddress = localNodeAddress;
    this.customSettings = customSettings;
  }

  /**
   * The type of this ledger plugin.
   */
  @Override
  public PluginType getPluginType() {
    return pluginType;
  }

  /**
   * The ILP Address for remote peer account this Plugin is connecting to...
   */
  @Override
  public InterledgerAddress getPeerAccountAddress() {
    return peerAccountAddress;
  }

  /**
   * The ILP address of the ILP Node operating this plugin.
   */
  @Override
  public InterledgerAddress getLocalNodeAddress() {
    return localNodeAddress;
  }

  /**
   * Additional, custom settings that any plugin can define.
   */
  @Override
  public ImmutableMap<String, Object> getCustomSettings() {
    return customSettings;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link PluginSettings.AbstractPluginSettings#getPluginType() pluginType} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for pluginType
   * @return A modified copy of the {@code this} object
   */
  public final ImmutablePluginSettings withPluginType(PluginType value) {
    if (this.pluginType == value) return this;
    PluginType newValue = Objects.requireNonNull(value, "pluginType");
    return new ImmutablePluginSettings(newValue, this.peerAccountAddress, this.localNodeAddress, this.customSettings);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link PluginSettings.AbstractPluginSettings#getPeerAccountAddress() peerAccountAddress} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for peerAccountAddress
   * @return A modified copy of the {@code this} object
   */
  public final ImmutablePluginSettings withPeerAccountAddress(InterledgerAddress value) {
    if (this.peerAccountAddress == value) return this;
    InterledgerAddress newValue = Objects.requireNonNull(value, "peerAccountAddress");
    return new ImmutablePluginSettings(this.pluginType, newValue, this.localNodeAddress, this.customSettings);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link PluginSettings.AbstractPluginSettings#getLocalNodeAddress() localNodeAddress} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for localNodeAddress
   * @return A modified copy of the {@code this} object
   */
  public final ImmutablePluginSettings withLocalNodeAddress(InterledgerAddress value) {
    if (this.localNodeAddress == value) return this;
    InterledgerAddress newValue = Objects.requireNonNull(value, "localNodeAddress");
    return new ImmutablePluginSettings(this.pluginType, this.peerAccountAddress, newValue, this.customSettings);
  }

  /**
   * Copy the current immutable object by replacing the {@link PluginSettings.AbstractPluginSettings#getCustomSettings() customSettings} map with the specified map.
   * Nulls are not permitted as keys or values.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param entries The entries to be added to the customSettings map
   * @return A modified copy of {@code this} object
   */
  public final ImmutablePluginSettings withCustomSettings(Map<String, ? extends Object> entries) {
    if (this.customSettings == entries) return this;
    ImmutableMap<String, Object> newValue = ImmutableMap.copyOf(entries);
    return new ImmutablePluginSettings(this.pluginType, this.peerAccountAddress, this.localNodeAddress, newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutablePluginSettings} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(Object another) {
    if (this == another) return true;
    return another instanceof ImmutablePluginSettings
        && equalTo((ImmutablePluginSettings) another);
  }

  private boolean equalTo(ImmutablePluginSettings another) {
    return pluginType.equals(another.pluginType)
        && peerAccountAddress.equals(another.peerAccountAddress)
        && localNodeAddress.equals(another.localNodeAddress)
        && customSettings.equals(another.customSettings);
  }

  /**
   * Computes a hash code from attributes: {@code pluginType}, {@code peerAccountAddress}, {@code localNodeAddress}, {@code customSettings}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    int h = 5381;
    h += (h << 5) + pluginType.hashCode();
    h += (h << 5) + peerAccountAddress.hashCode();
    h += (h << 5) + localNodeAddress.hashCode();
    h += (h << 5) + customSettings.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code PluginSettings} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("PluginSettings")
        .omitNullValues()
        .add("pluginType", pluginType)
        .add("peerAccountAddress", peerAccountAddress)
        .add("localNodeAddress", localNodeAddress)
        .add("customSettings", customSettings)
        .toString();
  }

  /**
   * Creates an immutable copy of a {@link PluginSettings.AbstractPluginSettings} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable PluginSettings instance
   */
  public static ImmutablePluginSettings copyOf(PluginSettings.AbstractPluginSettings instance) {
    if (instance instanceof ImmutablePluginSettings) {
      return (ImmutablePluginSettings) instance;
    }
    return ImmutablePluginSettings.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutablePluginSettings ImmutablePluginSettings}.
   * @return A new ImmutablePluginSettings builder
   */
  public static ImmutablePluginSettings.Builder builder() {
    return new ImmutablePluginSettings.Builder();
  }

  /**
   * Builds instances of type {@link ImmutablePluginSettings ImmutablePluginSettings}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  public static final class Builder {
    private static final long INIT_BIT_PLUGIN_TYPE = 0x1L;
    private static final long INIT_BIT_PEER_ACCOUNT_ADDRESS = 0x2L;
    private static final long INIT_BIT_LOCAL_NODE_ADDRESS = 0x4L;
    private long initBits = 0x7L;

    private PluginType pluginType;
    private InterledgerAddress peerAccountAddress;
    private InterledgerAddress localNodeAddress;
    private ImmutableMap.Builder<String, Object> customSettings = ImmutableMap.builder();

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code org.interledger.plugin.lpiv2.PluginSettings} instance.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder from(PluginSettings instance) {
      Objects.requireNonNull(instance, "instance");
      from((Object) instance);
      return this;
    }

    /**
     * Fill a builder with attribute values from the provided {@code org.interledger.plugin.lpiv2.PluginSettings.AbstractPluginSettings} instance.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder from(PluginSettings.AbstractPluginSettings instance) {
      Objects.requireNonNull(instance, "instance");
      from((Object) instance);
      return this;
    }

    private void from(Object object) {
      if (object instanceof PluginSettings) {
        PluginSettings instance = (PluginSettings) object;
        putAllCustomSettings(instance.getCustomSettings());
        pluginType(instance.getPluginType());
        peerAccountAddress(instance.getPeerAccountAddress());
        localNodeAddress(instance.getLocalNodeAddress());
      }
    }

    /**
     * Initializes the value for the {@link PluginSettings.AbstractPluginSettings#getPluginType() pluginType} attribute.
     * @param pluginType The value for pluginType 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder pluginType(PluginType pluginType) {
      this.pluginType = Objects.requireNonNull(pluginType, "pluginType");
      initBits &= ~INIT_BIT_PLUGIN_TYPE;
      return this;
    }

    /**
     * Initializes the value for the {@link PluginSettings.AbstractPluginSettings#getPeerAccountAddress() peerAccountAddress} attribute.
     * @param peerAccountAddress The value for peerAccountAddress 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder peerAccountAddress(InterledgerAddress peerAccountAddress) {
      this.peerAccountAddress = Objects.requireNonNull(peerAccountAddress, "peerAccountAddress");
      initBits &= ~INIT_BIT_PEER_ACCOUNT_ADDRESS;
      return this;
    }

    /**
     * Initializes the value for the {@link PluginSettings.AbstractPluginSettings#getLocalNodeAddress() localNodeAddress} attribute.
     * @param localNodeAddress The value for localNodeAddress 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder localNodeAddress(InterledgerAddress localNodeAddress) {
      this.localNodeAddress = Objects.requireNonNull(localNodeAddress, "localNodeAddress");
      initBits &= ~INIT_BIT_LOCAL_NODE_ADDRESS;
      return this;
    }

    /**
     * Put one entry to the {@link PluginSettings.AbstractPluginSettings#getCustomSettings() customSettings} map.
     * @param key The key in the customSettings map
     * @param value The associated value in the customSettings map
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder putCustomSettings(String key, Object value) {
      this.customSettings.put(key, value);
      return this;
    }

    /**
     * Put one entry to the {@link PluginSettings.AbstractPluginSettings#getCustomSettings() customSettings} map. Nulls are not permitted
     * @param entry The key and value entry
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder putCustomSettings(Map.Entry<String, ? extends Object> entry) {
      this.customSettings.put(entry);
      return this;
    }

    /**
     * Sets or replaces all mappings from the specified map as entries for the {@link PluginSettings.AbstractPluginSettings#getCustomSettings() customSettings} map. Nulls are not permitted
     * @param entries The entries that will be added to the customSettings map
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder customSettings(Map<String, ? extends Object> entries) {
      this.customSettings = ImmutableMap.builder();
      return putAllCustomSettings(entries);
    }

    /**
     * Put all mappings from the specified map as entries to {@link PluginSettings.AbstractPluginSettings#getCustomSettings() customSettings} map. Nulls are not permitted
     * @param entries The entries that will be added to the customSettings map
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder putAllCustomSettings(Map<String, ? extends Object> entries) {
      this.customSettings.putAll(entries);
      return this;
    }

    /**
     * Builds a new {@link ImmutablePluginSettings ImmutablePluginSettings}.
     * @return An immutable instance of PluginSettings
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutablePluginSettings build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutablePluginSettings(pluginType, peerAccountAddress, localNodeAddress, customSettings.build());
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_PLUGIN_TYPE) != 0) attributes.add("pluginType");
      if ((initBits & INIT_BIT_PEER_ACCOUNT_ADDRESS) != 0) attributes.add("peerAccountAddress");
      if ((initBits & INIT_BIT_LOCAL_NODE_ADDRESS) != 0) attributes.add("localNodeAddress");
      return "Cannot build PluginSettings, some of required attributes are not set " + attributes;
    }
  }
}
