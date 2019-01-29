package org.interledger.plugin.lpiv2.btp2.spring.factories;

import org.interledger.plugin.PluginFactory;
import org.interledger.plugin.lpiv2.PluginType;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A provider of plugin factories scoped by type.
 */
public class PluginFactoryProvider {

  // At runtime, there will potentially be _many_ factories depending on the plugintype (e.g., a BTP Factory, a
  // LoopbackFactory, etc).
  private final Map<PluginType, PluginFactory> pluginFactories;

  public PluginFactoryProvider() {
    this(Maps.newConcurrentMap());
  }

  public PluginFactoryProvider(final Map<PluginType, PluginFactory> pluginFactories) {
    this.pluginFactories = Objects.requireNonNull(pluginFactories);
  }

  public Optional<PluginFactory> getPluginFactory(final PluginType pluginType) {
    return Optional.ofNullable(this.pluginFactories.get(pluginType));
  }

  public PluginFactory registerPluginFactory(final PluginType pluginType, final PluginFactory pluginFactory) {
    return this.pluginFactories.put(pluginType, pluginFactory);
  }
}
