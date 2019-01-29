package org.interledger.plugin.spi;

import org.interledger.plugin.PluginFactory;

/**
 * A Java SPI for instances of {@link PluginFactory}.
 */
public interface PluginFactoryProvider {

  /**
   * Create a new instance of a {@link PluginFactory}.
   *
   * @return
   */
  PluginFactory construct();

}
