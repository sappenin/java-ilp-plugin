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
 * Immutable implementation of {@link BtpClientPluginSettings}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableBtpClientPluginSettings.builder()}.
 */
@SuppressWarnings({"all"})
@Generated("org.immutables.processor.ProxyProcessor")
@org.immutables.value.Generated(from = "BtpClientPluginSettings", generator = "Immutables")
public final class ImmutableBtpClientPluginSettings
    implements BtpClientPluginSettings {
  private final String remotePeerScheme;
  private final String remotePeerHostname;
  private final String remotePeerPort;
  private final String secret;
  private final PluginType pluginType;
  private final InterledgerAddress peerAccountAddress;
  private final InterledgerAddress localNodeAddress;
  private final ImmutableMap<String, Object> customSettings;

  private ImmutableBtpClientPluginSettings(ImmutableBtpClientPluginSettings.Builder builder) {
    this.secret = builder.secret;
    this.pluginType = builder.pluginType;
    this.peerAccountAddress = builder.peerAccountAddress;
    this.localNodeAddress = builder.localNodeAddress;
    this.customSettings = builder.customSettings.build();
    if (builder.remotePeerScheme != null) {
      initShim.remotePeerScheme(builder.remotePeerScheme);
    }
    if (builder.remotePeerHostname != null) {
      initShim.remotePeerHostname(builder.remotePeerHostname);
    }
    if (builder.remotePeerPort != null) {
      initShim.remotePeerPort(builder.remotePeerPort);
    }
    this.remotePeerScheme = initShim.getRemotePeerScheme();
    this.remotePeerHostname = initShim.getRemotePeerHostname();
    this.remotePeerPort = initShim.getRemotePeerPort();
    this.initShim = null;
  }

  private ImmutableBtpClientPluginSettings(
      String remotePeerScheme,
      String remotePeerHostname,
      String remotePeerPort,
      String secret,
      PluginType pluginType,
      InterledgerAddress peerAccountAddress,
      InterledgerAddress localNodeAddress,
      ImmutableMap<String, Object> customSettings) {
    this.remotePeerScheme = remotePeerScheme;
    this.remotePeerHostname = remotePeerHostname;
    this.remotePeerPort = remotePeerPort;
    this.secret = secret;
    this.pluginType = pluginType;
    this.peerAccountAddress = peerAccountAddress;
    this.localNodeAddress = localNodeAddress;
    this.customSettings = customSettings;
    this.initShim = null;
  }

  private static final byte STAGE_INITIALIZING = -1;
  private static final byte STAGE_UNINITIALIZED = 0;
  private static final byte STAGE_INITIALIZED = 1;
  private transient volatile InitShim initShim = new InitShim();

  private final class InitShim {
    private byte remotePeerSchemeBuildStage = STAGE_UNINITIALIZED;
    private String remotePeerScheme;

    String getRemotePeerScheme() {
      if (remotePeerSchemeBuildStage == STAGE_INITIALIZING) throw new IllegalStateException(formatInitCycleMessage());
      if (remotePeerSchemeBuildStage == STAGE_UNINITIALIZED) {
        remotePeerSchemeBuildStage = STAGE_INITIALIZING;
        this.remotePeerScheme = Objects.requireNonNull(getRemotePeerSchemeInitialize(), "remotePeerScheme");
        remotePeerSchemeBuildStage = STAGE_INITIALIZED;
      }
      return this.remotePeerScheme;
    }

    void remotePeerScheme(String remotePeerScheme) {
      this.remotePeerScheme = remotePeerScheme;
      remotePeerSchemeBuildStage = STAGE_INITIALIZED;
    }

    private byte remotePeerHostnameBuildStage = STAGE_UNINITIALIZED;
    private String remotePeerHostname;

    String getRemotePeerHostname() {
      if (remotePeerHostnameBuildStage == STAGE_INITIALIZING) throw new IllegalStateException(formatInitCycleMessage());
      if (remotePeerHostnameBuildStage == STAGE_UNINITIALIZED) {
        remotePeerHostnameBuildStage = STAGE_INITIALIZING;
        this.remotePeerHostname = Objects.requireNonNull(getRemotePeerHostnameInitialize(), "remotePeerHostname");
        remotePeerHostnameBuildStage = STAGE_INITIALIZED;
      }
      return this.remotePeerHostname;
    }

    void remotePeerHostname(String remotePeerHostname) {
      this.remotePeerHostname = remotePeerHostname;
      remotePeerHostnameBuildStage = STAGE_INITIALIZED;
    }

    private byte remotePeerPortBuildStage = STAGE_UNINITIALIZED;
    private String remotePeerPort;

    String getRemotePeerPort() {
      if (remotePeerPortBuildStage == STAGE_INITIALIZING) throw new IllegalStateException(formatInitCycleMessage());
      if (remotePeerPortBuildStage == STAGE_UNINITIALIZED) {
        remotePeerPortBuildStage = STAGE_INITIALIZING;
        this.remotePeerPort = Objects.requireNonNull(getRemotePeerPortInitialize(), "remotePeerPort");
        remotePeerPortBuildStage = STAGE_INITIALIZED;
      }
      return this.remotePeerPort;
    }

    void remotePeerPort(String remotePeerPort) {
      this.remotePeerPort = remotePeerPort;
      remotePeerPortBuildStage = STAGE_INITIALIZED;
    }

    private String formatInitCycleMessage() {
      List<String> attributes = new ArrayList<>();
      if (remotePeerSchemeBuildStage == STAGE_INITIALIZING) attributes.add("remotePeerScheme");
      if (remotePeerHostnameBuildStage == STAGE_INITIALIZING) attributes.add("remotePeerHostname");
      if (remotePeerPortBuildStage == STAGE_INITIALIZING) attributes.add("remotePeerPort");
      return "Cannot build BtpClientPluginSettings, attribute initializers form cycle " + attributes;
    }
  }

  private String getRemotePeerSchemeInitialize() {
    return BtpClientPluginSettings.super.getRemotePeerScheme();
  }

  private String getRemotePeerHostnameInitialize() {
    return BtpClientPluginSettings.super.getRemotePeerHostname();
  }

  private String getRemotePeerPortInitialize() {
    return BtpClientPluginSettings.super.getRemotePeerPort();
  }

  /**
   * The scheme for the remote peer connection. Currently only "ws" and "wss" are supported.
   * @return
   */
  @Override
  public String getRemotePeerScheme() {
    InitShim shim = this.initShim;
    return shim != null
        ? shim.getRemotePeerScheme()
        : this.remotePeerScheme;
  }

  /**
   * The hostname for the remote BTP peer.
   */
  @Override
  public String getRemotePeerHostname() {
    InitShim shim = this.initShim;
    return shim != null
        ? shim.getRemotePeerHostname()
        : this.remotePeerHostname;
  }

  /**
   * The port for the remote BTP peer.
   */
  @Override
  public String getRemotePeerPort() {
    InitShim shim = this.initShim;
    return shim != null
        ? shim.getRemotePeerPort()
        : this.remotePeerPort;
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
   * Copy the current immutable object by setting a value for the {@link BtpClientPluginSettings#getRemotePeerScheme() remotePeerScheme} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for remotePeerScheme
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableBtpClientPluginSettings withRemotePeerScheme(String value) {
    if (this.remotePeerScheme.equals(value)) return this;
    String newValue = Objects.requireNonNull(value, "remotePeerScheme");
    return validate(new ImmutableBtpClientPluginSettings(
        newValue,
        this.remotePeerHostname,
        this.remotePeerPort,
        this.secret,
        this.pluginType,
        this.peerAccountAddress,
        this.localNodeAddress,
        this.customSettings));
  }

  /**
   * Copy the current immutable object by setting a value for the {@link BtpClientPluginSettings#getRemotePeerHostname() remotePeerHostname} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for remotePeerHostname
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableBtpClientPluginSettings withRemotePeerHostname(String value) {
    if (this.remotePeerHostname.equals(value)) return this;
    String newValue = Objects.requireNonNull(value, "remotePeerHostname");
    return validate(new ImmutableBtpClientPluginSettings(
        this.remotePeerScheme,
        newValue,
        this.remotePeerPort,
        this.secret,
        this.pluginType,
        this.peerAccountAddress,
        this.localNodeAddress,
        this.customSettings));
  }

  /**
   * Copy the current immutable object by setting a value for the {@link BtpClientPluginSettings#getRemotePeerPort() remotePeerPort} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for remotePeerPort
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableBtpClientPluginSettings withRemotePeerPort(String value) {
    if (this.remotePeerPort.equals(value)) return this;
    String newValue = Objects.requireNonNull(value, "remotePeerPort");
    return validate(new ImmutableBtpClientPluginSettings(
        this.remotePeerScheme,
        this.remotePeerHostname,
        newValue,
        this.secret,
        this.pluginType,
        this.peerAccountAddress,
        this.localNodeAddress,
        this.customSettings));
  }

  /**
   * Copy the current immutable object by setting a value for the {@link BtpClientPluginSettings#getSecret() secret} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for secret
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableBtpClientPluginSettings withSecret(String value) {
    if (this.secret.equals(value)) return this;
    String newValue = Objects.requireNonNull(value, "secret");
    return validate(new ImmutableBtpClientPluginSettings(
        this.remotePeerScheme,
        this.remotePeerHostname,
        this.remotePeerPort,
        newValue,
        this.pluginType,
        this.peerAccountAddress,
        this.localNodeAddress,
        this.customSettings));
  }

  /**
   * Copy the current immutable object by setting a value for the {@link BtpClientPluginSettings#getPluginType() pluginType} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for pluginType
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableBtpClientPluginSettings withPluginType(PluginType value) {
    if (this.pluginType == value) return this;
    PluginType newValue = Objects.requireNonNull(value, "pluginType");
    return validate(new ImmutableBtpClientPluginSettings(
        this.remotePeerScheme,
        this.remotePeerHostname,
        this.remotePeerPort,
        this.secret,
        newValue,
        this.peerAccountAddress,
        this.localNodeAddress,
        this.customSettings));
  }

  /**
   * Copy the current immutable object by setting a value for the {@link BtpClientPluginSettings#getPeerAccountAddress() peerAccountAddress} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for peerAccountAddress
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableBtpClientPluginSettings withPeerAccountAddress(InterledgerAddress value) {
    if (this.peerAccountAddress == value) return this;
    InterledgerAddress newValue = Objects.requireNonNull(value, "peerAccountAddress");
    return validate(new ImmutableBtpClientPluginSettings(
        this.remotePeerScheme,
        this.remotePeerHostname,
        this.remotePeerPort,
        this.secret,
        this.pluginType,
        newValue,
        this.localNodeAddress,
        this.customSettings));
  }

  /**
   * Copy the current immutable object by setting a value for the {@link BtpClientPluginSettings#getLocalNodeAddress() localNodeAddress} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for localNodeAddress
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableBtpClientPluginSettings withLocalNodeAddress(InterledgerAddress value) {
    if (this.localNodeAddress == value) return this;
    InterledgerAddress newValue = Objects.requireNonNull(value, "localNodeAddress");
    return validate(new ImmutableBtpClientPluginSettings(
        this.remotePeerScheme,
        this.remotePeerHostname,
        this.remotePeerPort,
        this.secret,
        this.pluginType,
        this.peerAccountAddress,
        newValue,
        this.customSettings));
  }

  /**
   * Copy the current immutable object by replacing the {@link BtpClientPluginSettings#getCustomSettings() customSettings} map with the specified map.
   * Nulls are not permitted as keys or values.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param entries The entries to be added to the customSettings map
   * @return A modified copy of {@code this} object
   */
  public final ImmutableBtpClientPluginSettings withCustomSettings(Map<String, ? extends Object> entries) {
    if (this.customSettings == entries) return this;
    ImmutableMap<String, Object> newValue = ImmutableMap.copyOf(entries);
    return validate(new ImmutableBtpClientPluginSettings(
        this.remotePeerScheme,
        this.remotePeerHostname,
        this.remotePeerPort,
        this.secret,
        this.pluginType,
        this.peerAccountAddress,
        this.localNodeAddress,
        newValue));
  }

  /**
   * This instance is equal to all instances of {@code ImmutableBtpClientPluginSettings} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(Object another) {
    if (this == another) return true;
    return another instanceof ImmutableBtpClientPluginSettings
        && equalTo((ImmutableBtpClientPluginSettings) another);
  }

  private boolean equalTo(ImmutableBtpClientPluginSettings another) {
    return remotePeerScheme.equals(another.remotePeerScheme)
        && remotePeerHostname.equals(another.remotePeerHostname)
        && remotePeerPort.equals(another.remotePeerPort)
        && secret.equals(another.secret)
        && pluginType.equals(another.pluginType)
        && peerAccountAddress.equals(another.peerAccountAddress)
        && localNodeAddress.equals(another.localNodeAddress)
        && customSettings.equals(another.customSettings);
  }

  /**
   * Computes a hash code from attributes: {@code remotePeerScheme}, {@code remotePeerHostname}, {@code remotePeerPort}, {@code secret}, {@code pluginType}, {@code peerAccountAddress}, {@code localNodeAddress}, {@code customSettings}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    int h = 5381;
    h += (h << 5) + remotePeerScheme.hashCode();
    h += (h << 5) + remotePeerHostname.hashCode();
    h += (h << 5) + remotePeerPort.hashCode();
    h += (h << 5) + secret.hashCode();
    h += (h << 5) + pluginType.hashCode();
    h += (h << 5) + peerAccountAddress.hashCode();
    h += (h << 5) + localNodeAddress.hashCode();
    h += (h << 5) + customSettings.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code BtpClientPluginSettings} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("BtpClientPluginSettings")
        .omitNullValues()
        .add("remotePeerScheme", remotePeerScheme)
        .add("remotePeerHostname", remotePeerHostname)
        .add("remotePeerPort", remotePeerPort)
        .add("secret", secret)
        .add("pluginType", pluginType)
        .add("peerAccountAddress", peerAccountAddress)
        .add("localNodeAddress", localNodeAddress)
        .add("customSettings", customSettings)
        .toString();
  }


  private static ImmutableBtpClientPluginSettings validate(ImmutableBtpClientPluginSettings instance) {
    instance.check();
    return instance;
  }

  /**
   * Creates an immutable copy of a {@link BtpClientPluginSettings} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable BtpClientPluginSettings instance
   */
  public static ImmutableBtpClientPluginSettings copyOf(BtpClientPluginSettings instance) {
    if (instance instanceof ImmutableBtpClientPluginSettings) {
      return (ImmutableBtpClientPluginSettings) instance;
    }
    return ImmutableBtpClientPluginSettings.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableBtpClientPluginSettings ImmutableBtpClientPluginSettings}.
   * @return A new ImmutableBtpClientPluginSettings builder
   */
  public static ImmutableBtpClientPluginSettings.Builder builder() {
    return new ImmutableBtpClientPluginSettings.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableBtpClientPluginSettings ImmutableBtpClientPluginSettings}.
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

    private String remotePeerScheme;
    private String remotePeerHostname;
    private String remotePeerPort;
    private String secret;
    private PluginType pluginType;
    private InterledgerAddress peerAccountAddress;
    private InterledgerAddress localNodeAddress;
    private ImmutableMap.Builder<String, Object> customSettings = ImmutableMap.builder();

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code org.interledger.plugin.lpiv2.btp2.BtpClientPluginSettings} instance.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder from(BtpClientPluginSettings instance) {
      Objects.requireNonNull(instance, "instance");
      from((Object) instance);
      return this;
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

    private void from(Object object) {
      if (object instanceof BtpClientPluginSettings) {
        BtpClientPluginSettings instance = (BtpClientPluginSettings) object;
        remotePeerHostname(instance.getRemotePeerHostname());
        remotePeerScheme(instance.getRemotePeerScheme());
        remotePeerPort(instance.getRemotePeerPort());
      }
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
     * Initializes the value for the {@link BtpClientPluginSettings#getRemotePeerScheme() remotePeerScheme} attribute.
     * <p><em>If not set, this attribute will have a default value as returned by the initializer of {@link BtpClientPluginSettings#getRemotePeerScheme() remotePeerScheme}.</em>
     * @param remotePeerScheme The value for remotePeerScheme 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder remotePeerScheme(String remotePeerScheme) {
      this.remotePeerScheme = Objects.requireNonNull(remotePeerScheme, "remotePeerScheme");
      return this;
    }

    /**
     * Initializes the value for the {@link BtpClientPluginSettings#getRemotePeerHostname() remotePeerHostname} attribute.
     * <p><em>If not set, this attribute will have a default value as returned by the initializer of {@link BtpClientPluginSettings#getRemotePeerHostname() remotePeerHostname}.</em>
     * @param remotePeerHostname The value for remotePeerHostname 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder remotePeerHostname(String remotePeerHostname) {
      this.remotePeerHostname = Objects.requireNonNull(remotePeerHostname, "remotePeerHostname");
      return this;
    }

    /**
     * Initializes the value for the {@link BtpClientPluginSettings#getRemotePeerPort() remotePeerPort} attribute.
     * <p><em>If not set, this attribute will have a default value as returned by the initializer of {@link BtpClientPluginSettings#getRemotePeerPort() remotePeerPort}.</em>
     * @param remotePeerPort The value for remotePeerPort 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder remotePeerPort(String remotePeerPort) {
      this.remotePeerPort = Objects.requireNonNull(remotePeerPort, "remotePeerPort");
      return this;
    }

    /**
     * Initializes the value for the {@link BtpClientPluginSettings#getSecret() secret} attribute.
     * @param secret The value for secret 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder secret(String secret) {
      this.secret = Objects.requireNonNull(secret, "secret");
      initBits &= ~INIT_BIT_SECRET;
      return this;
    }

    /**
     * Initializes the value for the {@link BtpClientPluginSettings#getPluginType() pluginType} attribute.
     * @param pluginType The value for pluginType 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder pluginType(PluginType pluginType) {
      this.pluginType = Objects.requireNonNull(pluginType, "pluginType");
      initBits &= ~INIT_BIT_PLUGIN_TYPE;
      return this;
    }

    /**
     * Initializes the value for the {@link BtpClientPluginSettings#getPeerAccountAddress() peerAccountAddress} attribute.
     * @param peerAccountAddress The value for peerAccountAddress 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder peerAccountAddress(InterledgerAddress peerAccountAddress) {
      this.peerAccountAddress = Objects.requireNonNull(peerAccountAddress, "peerAccountAddress");
      initBits &= ~INIT_BIT_PEER_ACCOUNT_ADDRESS;
      return this;
    }

    /**
     * Initializes the value for the {@link BtpClientPluginSettings#getLocalNodeAddress() localNodeAddress} attribute.
     * @param localNodeAddress The value for localNodeAddress 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder localNodeAddress(InterledgerAddress localNodeAddress) {
      this.localNodeAddress = Objects.requireNonNull(localNodeAddress, "localNodeAddress");
      initBits &= ~INIT_BIT_LOCAL_NODE_ADDRESS;
      return this;
    }

    /**
     * Put one entry to the {@link BtpClientPluginSettings#getCustomSettings() customSettings} map.
     * @param key The key in the customSettings map
     * @param value The associated value in the customSettings map
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder putCustomSettings(String key, Object value) {
      this.customSettings.put(key, value);
      return this;
    }

    /**
     * Put one entry to the {@link BtpClientPluginSettings#getCustomSettings() customSettings} map. Nulls are not permitted
     * @param entry The key and value entry
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder putCustomSettings(Map.Entry<String, ? extends Object> entry) {
      this.customSettings.put(entry);
      return this;
    }

    /**
     * Sets or replaces all mappings from the specified map as entries for the {@link BtpClientPluginSettings#getCustomSettings() customSettings} map. Nulls are not permitted
     * @param entries The entries that will be added to the customSettings map
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder customSettings(Map<String, ? extends Object> entries) {
      this.customSettings = ImmutableMap.builder();
      return putAllCustomSettings(entries);
    }

    /**
     * Put all mappings from the specified map as entries to {@link BtpClientPluginSettings#getCustomSettings() customSettings} map. Nulls are not permitted
     * @param entries The entries that will be added to the customSettings map
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder putAllCustomSettings(Map<String, ? extends Object> entries) {
      this.customSettings.putAll(entries);
      return this;
    }

    /**
     * Builds a new {@link ImmutableBtpClientPluginSettings ImmutableBtpClientPluginSettings}.
     * @return An immutable instance of BtpClientPluginSettings
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableBtpClientPluginSettings build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return ImmutableBtpClientPluginSettings.validate(new ImmutableBtpClientPluginSettings(this));
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_SECRET) != 0) attributes.add("secret");
      if ((initBits & INIT_BIT_PLUGIN_TYPE) != 0) attributes.add("pluginType");
      if ((initBits & INIT_BIT_PEER_ACCOUNT_ADDRESS) != 0) attributes.add("peerAccountAddress");
      if ((initBits & INIT_BIT_LOCAL_NODE_ADDRESS) != 0) attributes.add("localNodeAddress");
      return "Cannot build BtpClientPluginSettings, some of required attributes are not set " + attributes;
    }
  }
}
