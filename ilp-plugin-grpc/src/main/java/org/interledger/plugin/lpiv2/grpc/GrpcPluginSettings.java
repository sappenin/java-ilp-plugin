package org.interledger.plugin.lpiv2.grpc;

import org.interledger.plugin.lpiv2.PluginSettings;

import org.immutables.value.Value;

public interface GrpcPluginSettings extends PluginSettings {

  @Value.Immutable
  abstract class AbstractGrpcPluginSettings implements GrpcPluginSettings {

  }
}
