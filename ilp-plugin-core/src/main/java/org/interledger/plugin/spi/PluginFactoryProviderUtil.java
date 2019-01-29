package org.interledger.plugin.spi;

import org.interledger.plugin.PluginFactory;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * A Java SPI for instances of {@link PluginFactory}.
 */
public class PluginFactoryProviderUtil {

//  private final ServiceLoader<PluginFactoryProvider> loader = ServiceLoader.load(PluginFactoryProvider.class);
//
//  /**
//   * Accessor for all PluginFactoryProvider registered via SPI.
//   *
//   * @param refresh A boolean to indicate whether or not to reload the providers cached. The search result is cached so
//   *                we can invoke the ServiceLoader.reload(true) method in order to discover newly installed
//   *                implementations.
//   *
//   * @return An {@link Iterator} of all installed {@link PluginFactory} instances.
//   */
//  public Iterator<PluginFactoryProvider> providers(boolean refresh) {
//    if (refresh) {
//      loader.reload();
//    }
//    return loader.iterator();
//  }

}
