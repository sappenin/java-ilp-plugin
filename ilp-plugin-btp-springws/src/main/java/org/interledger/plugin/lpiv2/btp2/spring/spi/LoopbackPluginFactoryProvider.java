package org.interledger.plugin.lpiv2.btp2.spring.spi;

import org.interledger.plugin.lpiv2.btp2.spring.factories.LoopbackPluginFactory;
import org.interledger.plugin.spi.PluginFactoryProvider;

/**
 * A {@link PluginFactoryProvider} for creating instances of {@link LoopbackPluginFactory}.
 */
public class LoopbackPluginFactoryProvider implements PluginFactoryProvider {

  public LoopbackPluginFactory construct() {
    return new LoopbackPluginFactory();
  }

}
