package org.interledger.plugin.link;

import org.interledger.core.InterledgerProtocolException;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

/**
 * Handles an incoming message to settle an account with a remote peer, managed by a plugin.
 */
@FunctionalInterface
public interface BilateralMoneyHandler {

  /**
   * Handles an incoming ILP fulfill packet.
   *
   * @param amount
   *
   * @return A {@link CompletableFuture} that resolves to {@link Void}.
   */
  CompletableFuture<Void> handleIncomingMoney(BigInteger amount) throws InterledgerProtocolException;
}