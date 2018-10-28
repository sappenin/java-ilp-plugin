package org.interledger.plugin.lpiv2.btp2;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Generated;
import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.lpiv2.PluginSettings;
import org.interledger.plugin.lpiv2.PluginType;

/**
 * Immutable implementation of {@link BtpServerPluginSettings.AbstractBtpServerPluginSettings}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableBtpServerPluginSettings.builder()}.
 */
@SuppressWarnings({"all"})
@Generated("org.immutables.processor.ProxyProcessor")
@org.immutables.value.Generated(from = "BtpServerPluginSettings.AbstractBtpServerPluginSettings", generator = "Immutables")
public final class ImmutableBtpServerPluginSettings
    extends BtpServerPluginSettings.AbstractBtpServerPluginSettings {
  private final String secret;
  private final PluginType pluginType;
  private final InterledgerAddress peerAccountAddress;
  private final InterledgerAddress localNodeAddress;
  private final ImmutableMap<String, Object> customSettings;

  private ImmutableBtpServerPluginSettings(
      String secret,
      PluginType pluginType,
      InterledgerAddress peerAccountAddress,
      InterledgerAddress localNodeAddress,
      ImmutableMap<String, Object> customSettings) {
    this.secret = secret;
    this.pluginType = pluginType;
    this.peerAccountAddress = peerAccountAddress;
    this.localNodeAddress = localNodeAddress;
    this.customSettings = customSettings;
  }

  /**
   * The shared auth token, expected by a server or presented by a client, that both will use to authenticate a BTP
   * session.
   * @return
   */
  @Override
  public String getSecret() {
    return secret;
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
   * Copy the current immutable object by setting a value for the {@link BtpServerPluginSettings.AbstractBtpServerPluginSettings#getSecret() secret} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for secret
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableBtpServerPluginSettings withSecret(String value) {
    if (this.secret.equals(value)) return this;
    String newValue = Objects.requireNonNull(value, "secret");
    return new ImmutableBtpServerPluginSettings(newValue, this.pluginType, this.peerAccountAddress, this.localNodeAddress, this.customSettings);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link BtpServerPluginSettings.AbstractBtpServerPluginSettings#getPluginType() pluginType} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for pluginType
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableBtpServerPluginSettings withPluginType(PluginType value) {
    if (this.pluginType == value) return this;
    PluginType newValue = Objects.requireNonNull(value, "pluginType");
    return new ImmutableBtpServerPluginSettings(this.secret, newValue, this.peerAccountAddress, this.localNodeAddress, this.customSettings);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link BtpServerPluginSettings.AbstractBtpServerPluginSettings#getPeerAccountAddress() peerAccountAddress} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for peerAccountAddress
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableBtpServerPluginSettings withPeerAccountAddress(InterledgerAddress value) {
    if (this.peerAccountAddress == value) return this;
    InterledgerAddress newValue = Objects.requireNonNull(value, "peerAccountAddress");
    return new ImmutableBtpServerPluginSettings(this.secret, this.pluginType, newValue, this.localNodeAddress, this.customSettings);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link BtpServerPluginSettings.AbstractBtpServerPluginSettings#getLocalNodeAddress() localNodeAddress} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for localNodeAddress
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableBtpServerPluginSettings withLocalNodeAddress(InterledgerAddress value) {
    if (this.localNodeAddress == value) return this;
    InterledgerAddress newValue = Objects.requireNonNull(value, "localNodeAddress");
    return new ImmutableBtpServerPluginSettings(this.secret, this.pluginType, this.peerAccountAddress, newValue, this.customSettings);
  }

  /**
   * Copy the current immutable object by replacing the {@link BtpServerPluginSettings.AbstractBtpServerPluginSettings#getCustomSettings() customSettings} map with the specified map.
   * Nulls are not permitted as keys or values.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param entries The entries to be added to the customSettings map
   * @return A modified copy of {@code this} object
   */
  public final ImmutableBtpServerPluginSettings withCustomSettings(Map<String, ? extends Object> entries) {
    if (this.customSettings == entries) return this;
    ImmutableMap<String, Object> newValue = ImmutableMap.copyOf(entries);
    return new ImmutableBtpServerPluginSettings(this.secret, this.pluginType, this.peerAccountAddress, this.localNodeAddress, newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableBtpServerPluginSettings} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(Object another) {
    if (this == another) return true;
    return another instanceof ImmutableBtpServerPluginSettings
        && equalTo((ImmutableBtpServerPluginSettings) another);
  }

  private boolean equalTo(ImmutableBtpServerPluginSettings another) {
    return secret.equals(another.secret)
        && pluginType.equals(another.pluginType)
        && peerAccountAddress.equals(another.peerAccountAddress)
        && localNodeAddress.equals(another.localNodeAddress)
        && customSettings.equals(another.customSettings);
  }

  /**
   * Computes a hash code from attributes: {@code secret}, {@code pluginType}, {@code peerAccountAddress}, {@code localNodeAddress}, {@code customSettings}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    int h = 5381;
    h += (h << 5) + secret.hashCode();
    h += (h << 5) + pluginType.hashCode();
    h += (h << 5) + peerAccountAddress.hashCode();
    h += (h << 5) + localNodeAddress.hashCode();
    h += (h << 5) + customSettings.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code BtpServerPluginSettings} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("BtpServerPluginSettings")
        .omitNullValues()
        .add("secret", secret)
        .add("pluginType", pluginType)
        .add("peerAccountAddress", peerAccountAddress)
        .add("localNodeAddress", localNodeAddress)
        .add("customSettings", customSettings)
        .toString();
  }

  /**
   * Creates an immutable copy of a {@link BtpServerPluginSettings.AbstractBtpServerPluginSettings} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable BtpServerPluginSettings instance
   */
  public static ImmutableBtpServerPluginSettings copyOf(BtpServerPluginSettings.AbstractBtpServerPluginSettings instance) {
    if (instance instanceof ImmutableBtpServerPluginSettings) {
      return (ImmutableBtpServerPluginSettings) instance;
    }
    return ImmutableBtpServerPluginSettings.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableBtpServerPluginSettings ImmutableBtpServerPluginSettings}.
   * @return A new ImmutableBtpServerPluginSettings builder
   */
  public static ImmutableBtpServerPluginSettings.Builder builder() {
    return new ImmutableBtpServerPluginSettings.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableBtpServerPluginSettings ImmutableBtpServerPluginSettings}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  public static final class Builder {
    private static final long INIT_BIT_SECRET = 0x1L;
    private static final long INIT_BIT_PLUGIN_TYPE = 0x2L;
    private static final long INIT_BIT_PEER_ACCOUNT_ADDRESS = 0x4L;
    private static final long INIT_BIT_LOCAL_NODE_ADDRESS = 0x8L;
    private long initBits = 0xfL;

    private String secret;
    private PluginType pluginType;
    private InterledgerAddress peerAccountAddress;
    private InterledgerAddress localNodeAddress;
    private ImmutableMap.Builder<String, Object> customSettings = ImmutableMap.builder();

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code org.interledger.plugin.lpiv2.btp2.BtpPluginSettings} instance.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder from(BtpPluginSettings instance) {
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

    /**
     * Fill a builder with attribute values from the provided {@code org.interledger.plugin.lpiv2.btp2.BtpServerPluginSettings.AbstractBtpServerPluginSettings} instance.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder from(BtpServerPluginSettings.AbstractBtpServerPluginSettings instance) {
      Objects.requireNonNull(instance, "instance");
      from((Object) instance);
      return this;
    }

    private void from(Object object) {
      if (object instanceof BtpPluginSettings) {
        BtpPluginSettings instance = (BtpPluginSettings) object;
        secret(instance.getSecret());
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
     * Initializes the value for the {@link BtpServerPluginSettings.AbstractBtpServerPluginSettings#getSecret() secret} attribute.
     * @param secret The value for secret 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder secret(String secret) {
      this.secret = Objects.requireNonNull(secret, "secret");
      initBits &= ~INIT_BIT_SECRET;
      return this;
    }

    /**
     * Initializes the value for the {@link BtpServerPluginSettings.AbstractBtpServerPluginSettings#getPluginType() pluginType} attribute.
     * @param pluginType The value for pluginType 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder pluginType(PluginType pluginType) {
      this.pluginType = Objects.requireNonNull(pluginType, "pluginType");
      initBits &= ~INIT_BIT_PLUGIN_TYPE;
      return this;
    }

    /**
     * Initializes the value for the {@link BtpServerPluginSettings.AbstractBtpServerPluginSettings#getPeerAccountAddress() peerAccountAddress} attribute.
     * @param peerAccountAddress The value for peerAccountAddress 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder peerAccountAddress(InterledgerAddress peerAccountAddress) {
      this.peerAccountAddress = Objects.requireNonNull(peerAccountAddress, "peerAccountAddress");
      initBits &= ~INIT_BIT_PEER_ACCOUNT_ADDRESS;
      return this;
    }

    /**
     * Initializes the value for the {@link BtpServerPluginSettings.AbstractBtpServerPluginSettings#getLocalNodeAddress() localNodeAddress} attribute.
     * @param localNodeAddress The value for localNodeAddress 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder localNodeAddress(InterledgerAddress localNodeAddress) {
      this.localNodeAddress = Objects.requireNonNull(localNodeAddress, "localNodeAddress");
      initBits &= ~INIT_BIT_LOCAL_NODE_ADDRESS;
      return this;
    }

    /**
     * Put one entry to the {@link BtpServerPluginSettings.AbstractBtpServerPluginSettings#getCustomSettings() customSettings} map.
     * @param key The key in the customSettings map
     * @param value The associated value in the customSettings map
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder putCustomSettings(String key, Object value) {
      this.customSettings.put(key, value);
      return this;
    }

    /**
     * Put one entry to the {@link BtpServerPluginSettings.AbstractBtpServerPluginSettings#getCustomSettings() customSettings} map. Nulls are not permitted
     * @param entry The key and value entry
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder putCustomSettings(Map.Entry<String, ? extends Object> entry) {
      this.customSettings.put(entry);
      return this;
    }

    /**
     * Sets or replaces all mappings from the specified map as entries for the {@link BtpServerPluginSettings.AbstractBtpServerPluginSettings#getCustomSettings() customSettings} map. Nulls are not permitted
     * @param entries The entries that will be added to the customSettings map
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder customSettings(Map<String, ? extends Object> entries) {
      this.customSettings = ImmutableMap.builder();
      return putAllCustomSettings(entries);
    }

    /**
     * Put all mappings from the specified map as entries to {@link BtpServerPluginSettings.AbstractBtpServerPluginSettings#getCustomSettings() customSettings} map. Nulls are not permitted
     * @param entries The entries that will be added to the customSettings map
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder putAllCustomSettings(Map<String, ? extends Object> entries) {
      this.customSettings.putAll(entries);
      return this;
    }

    /**
     * Builds a new {@link ImmutableBtpServerPluginSettings ImmutableBtpServerPluginSettings}.
     * @return An immutable instance of BtpServerPluginSettings
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableBtpServerPluginSettings build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableBtpServerPluginSettings(secret, pluginType, peerAccountAddress, localNodeAddress, customSettings.build());
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_SECRET) != 0) attributes.add("secret");
      if ((initBits & INIT_BIT_PLUGIN_TYPE) != 0) attributes.add("pluginType");
      if ((initBits & INIT_BIT_PEER_ACCOUNT_ADDRESS) != 0) attributes.add("peerAccountAddress");
      if ((initBits & INIT_BIT_LOCAL_NODE_ADDRESS) != 0) attributes.add("localNodeAddress");
      return "Cannot build BtpServerPluginSettings, some of required attributes are not set " + attributes;
    }
  }
}
