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
 * Immutable implementation of {@link TestHelpers.ExtendedPluginSettings}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableExtendedPluginSettings.builder()}.
 */
@SuppressWarnings({"all"})
@Generated("org.immutables.processor.ProxyProcessor")
@org.immutables.value.Generated(from = "TestHelpers.ExtendedPluginSettings", generator = "Immutables")
public final class ImmutableExtendedPluginSettings
    implements TestHelpers.ExtendedPluginSettings {
  private final String password;
  private final PluginType pluginType;
  private final InterledgerAddress peerAccountAddress;
  private final InterledgerAddress localNodeAddress;
  private final ImmutableMap<String, Object> customSettings;

  private ImmutableExtendedPluginSettings(
      String password,
      PluginType pluginType,
      InterledgerAddress peerAccountAddress,
      InterledgerAddress localNodeAddress,
      ImmutableMap<String, Object> customSettings) {
    this.password = password;
    this.pluginType = pluginType;
    this.peerAccountAddress = peerAccountAddress;
    this.localNodeAddress = localNodeAddress;
    this.customSettings = customSettings;
  }

  /**
   * The password for the connector account on the ledger.
   */
  @Override
  public String getPassword() {
    return password;
  }

  /**
   * @return The value of the {@code pluginType} attribute
   */
  @Override
  public PluginType getPluginType() {
    return pluginType;
  }

  /**
   * @return The value of the {@code peerAccountAddress} attribute
   */
  @Override
  public InterledgerAddress getPeerAccountAddress() {
    return peerAccountAddress;
  }

  /**
   * @return The value of the {@code localNodeAddress} attribute
   */
  @Override
  public InterledgerAddress getLocalNodeAddress() {
    return localNodeAddress;
  }

  /**
   * @return The value of the {@code customSettings} attribute
   */
  @Override
  public ImmutableMap<String, Object> getCustomSettings() {
    return customSettings;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link TestHelpers.ExtendedPluginSettings#getPassword() password} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for password
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableExtendedPluginSettings withPassword(String value) {
    if (this.password.equals(value)) return this;
    String newValue = Objects.requireNonNull(value, "password");
    return new ImmutableExtendedPluginSettings(newValue, this.pluginType, this.peerAccountAddress, this.localNodeAddress, this.customSettings);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link TestHelpers.ExtendedPluginSettings#getPluginType() pluginType} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for pluginType
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableExtendedPluginSettings withPluginType(PluginType value) {
    if (this.pluginType == value) return this;
    PluginType newValue = Objects.requireNonNull(value, "pluginType");
    return new ImmutableExtendedPluginSettings(this.password, newValue, this.peerAccountAddress, this.localNodeAddress, this.customSettings);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link TestHelpers.ExtendedPluginSettings#getPeerAccountAddress() peerAccountAddress} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for peerAccountAddress
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableExtendedPluginSettings withPeerAccountAddress(InterledgerAddress value) {
    if (this.peerAccountAddress == value) return this;
    InterledgerAddress newValue = Objects.requireNonNull(value, "peerAccountAddress");
    return new ImmutableExtendedPluginSettings(this.password, this.pluginType, newValue, this.localNodeAddress, this.customSettings);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link TestHelpers.ExtendedPluginSettings#getLocalNodeAddress() localNodeAddress} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for localNodeAddress
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableExtendedPluginSettings withLocalNodeAddress(InterledgerAddress value) {
    if (this.localNodeAddress == value) return this;
    InterledgerAddress newValue = Objects.requireNonNull(value, "localNodeAddress");
    return new ImmutableExtendedPluginSettings(this.password, this.pluginType, this.peerAccountAddress, newValue, this.customSettings);
  }

  /**
   * Copy the current immutable object by replacing the {@link TestHelpers.ExtendedPluginSettings#getCustomSettings() customSettings} map with the specified map.
   * Nulls are not permitted as keys or values.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param entries The entries to be added to the customSettings map
   * @return A modified copy of {@code this} object
   */
  public final ImmutableExtendedPluginSettings withCustomSettings(Map<String, ? extends Object> entries) {
    if (this.customSettings == entries) return this;
    ImmutableMap<String, Object> newValue = ImmutableMap.copyOf(entries);
    return new ImmutableExtendedPluginSettings(this.password, this.pluginType, this.peerAccountAddress, this.localNodeAddress, newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableExtendedPluginSettings} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(Object another) {
    if (this == another) return true;
    return another instanceof ImmutableExtendedPluginSettings
        && equalTo((ImmutableExtendedPluginSettings) another);
  }

  private boolean equalTo(ImmutableExtendedPluginSettings another) {
    return password.equals(another.password)
        && pluginType.equals(another.pluginType)
        && peerAccountAddress.equals(another.peerAccountAddress)
        && localNodeAddress.equals(another.localNodeAddress)
        && customSettings.equals(another.customSettings);
  }

  /**
   * Computes a hash code from attributes: {@code password}, {@code pluginType}, {@code peerAccountAddress}, {@code localNodeAddress}, {@code customSettings}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    int h = 5381;
    h += (h << 5) + password.hashCode();
    h += (h << 5) + pluginType.hashCode();
    h += (h << 5) + peerAccountAddress.hashCode();
    h += (h << 5) + localNodeAddress.hashCode();
    h += (h << 5) + customSettings.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code ExtendedPluginSettings} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("ExtendedPluginSettings")
        .omitNullValues()
        .add("password", password)
        .add("pluginType", pluginType)
        .add("peerAccountAddress", peerAccountAddress)
        .add("localNodeAddress", localNodeAddress)
        .add("customSettings", customSettings)
        .toString();
  }

  /**
   * Creates an immutable copy of a {@link TestHelpers.ExtendedPluginSettings} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable ExtendedPluginSettings instance
   */
  public static ImmutableExtendedPluginSettings copyOf(TestHelpers.ExtendedPluginSettings instance) {
    if (instance instanceof ImmutableExtendedPluginSettings) {
      return (ImmutableExtendedPluginSettings) instance;
    }
    return ImmutableExtendedPluginSettings.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableExtendedPluginSettings ImmutableExtendedPluginSettings}.
   * @return A new ImmutableExtendedPluginSettings builder
   */
  public static ImmutableExtendedPluginSettings.Builder builder() {
    return new ImmutableExtendedPluginSettings.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableExtendedPluginSettings ImmutableExtendedPluginSettings}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  public static final class Builder {
    private static final long INIT_BIT_PASSWORD = 0x1L;
    private static final long INIT_BIT_PLUGIN_TYPE = 0x2L;
    private static final long INIT_BIT_PEER_ACCOUNT_ADDRESS = 0x4L;
    private static final long INIT_BIT_LOCAL_NODE_ADDRESS = 0x8L;
    private long initBits = 0xfL;

    private String password;
    private PluginType pluginType;
    private InterledgerAddress peerAccountAddress;
    private InterledgerAddress localNodeAddress;
    private ImmutableMap.Builder<String, Object> customSettings = ImmutableMap.builder();

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code org.interledger.plugin.lpiv2.TestHelpers.ExtendedPluginSettings} instance.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder from(TestHelpers.ExtendedPluginSettings instance) {
      Objects.requireNonNull(instance, "instance");
      from((Object) instance);
      return this;
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

    private void from(Object object) {
      if (object instanceof TestHelpers.ExtendedPluginSettings) {
        TestHelpers.ExtendedPluginSettings instance = (TestHelpers.ExtendedPluginSettings) object;
        password(instance.getPassword());
      }
      if (object instanceof PluginSettings) {
        PluginSettings instance = (PluginSettings) object;
        putAllCustomSettings(instance.getCustomSettings());
        pluginType(instance.getPluginType());
        peerAccountAddress(instance.getPeerAccountAddress());
        localNodeAddress(instance.getLocalNodeAddress());
      }
    }

    /**
     * Initializes the value for the {@link TestHelpers.ExtendedPluginSettings#getPassword() password} attribute.
     * @param password The value for password 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder password(String password) {
      this.password = Objects.requireNonNull(password, "password");
      initBits &= ~INIT_BIT_PASSWORD;
      return this;
    }

    /**
     * Initializes the value for the {@link TestHelpers.ExtendedPluginSettings#getPluginType() pluginType} attribute.
     * @param pluginType The value for pluginType 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder pluginType(PluginType pluginType) {
      this.pluginType = Objects.requireNonNull(pluginType, "pluginType");
      initBits &= ~INIT_BIT_PLUGIN_TYPE;
      return this;
    }

    /**
     * Initializes the value for the {@link TestHelpers.ExtendedPluginSettings#getPeerAccountAddress() peerAccountAddress} attribute.
     * @param peerAccountAddress The value for peerAccountAddress 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder peerAccountAddress(InterledgerAddress peerAccountAddress) {
      this.peerAccountAddress = Objects.requireNonNull(peerAccountAddress, "peerAccountAddress");
      initBits &= ~INIT_BIT_PEER_ACCOUNT_ADDRESS;
      return this;
    }

    /**
     * Initializes the value for the {@link TestHelpers.ExtendedPluginSettings#getLocalNodeAddress() localNodeAddress} attribute.
     * @param localNodeAddress The value for localNodeAddress 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder localNodeAddress(InterledgerAddress localNodeAddress) {
      this.localNodeAddress = Objects.requireNonNull(localNodeAddress, "localNodeAddress");
      initBits &= ~INIT_BIT_LOCAL_NODE_ADDRESS;
      return this;
    }

    /**
     * Put one entry to the {@link TestHelpers.ExtendedPluginSettings#getCustomSettings() customSettings} map.
     * @param key The key in the customSettings map
     * @param value The associated value in the customSettings map
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder putCustomSettings(String key, Object value) {
      this.customSettings.put(key, value);
      return this;
    }

    /**
     * Put one entry to the {@link TestHelpers.ExtendedPluginSettings#getCustomSettings() customSettings} map. Nulls are not permitted
     * @param entry The key and value entry
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder putCustomSettings(Map.Entry<String, ? extends Object> entry) {
      this.customSettings.put(entry);
      return this;
    }

    /**
     * Sets or replaces all mappings from the specified map as entries for the {@link TestHelpers.ExtendedPluginSettings#getCustomSettings() customSettings} map. Nulls are not permitted
     * @param entries The entries that will be added to the customSettings map
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder customSettings(Map<String, ? extends Object> entries) {
      this.customSettings = ImmutableMap.builder();
      return putAllCustomSettings(entries);
    }

    /**
     * Put all mappings from the specified map as entries to {@link TestHelpers.ExtendedPluginSettings#getCustomSettings() customSettings} map. Nulls are not permitted
     * @param entries The entries that will be added to the customSettings map
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder putAllCustomSettings(Map<String, ? extends Object> entries) {
      this.customSettings.putAll(entries);
      return this;
    }

    /**
     * Builds a new {@link ImmutableExtendedPluginSettings ImmutableExtendedPluginSettings}.
     * @return An immutable instance of ExtendedPluginSettings
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableExtendedPluginSettings build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableExtendedPluginSettings(password, pluginType, peerAccountAddress, localNodeAddress, customSettings.build());
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_PASSWORD) != 0) attributes.add("password");
      if ((initBits & INIT_BIT_PLUGIN_TYPE) != 0) attributes.add("pluginType");
      if ((initBits & INIT_BIT_PEER_ACCOUNT_ADDRESS) != 0) attributes.add("peerAccountAddress");
      if ((initBits & INIT_BIT_LOCAL_NODE_ADDRESS) != 0) attributes.add("localNodeAddress");
      return "Cannot build ExtendedPluginSettings, some of required attributes are not set " + attributes;
    }
  }
}
