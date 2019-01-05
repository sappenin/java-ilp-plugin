package org.interledger.plugin.lpiv2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.interledger.core.InterledgerAddress;
import org.interledger.core.InterledgerCondition;
import org.interledger.core.InterledgerFulfillPacket;
import org.interledger.core.InterledgerFulfillment;
import org.interledger.core.InterledgerPreparePacket;
import org.interledger.core.InterledgerRejectPacket;
import org.interledger.core.InterledgerResponsePacket;
import org.interledger.core.InterledgerResponsePacketHandler;

import ch.qos.logback.classic.Level;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * A unit test for {@link LoopbackPlugin} to ensure that it is functioning properly.
 */
@RunWith(Parameterized.class)
public class LoopbackPluginTest {

  public static final byte[] PREIMAGE = "Roads? Where we're going we dont".getBytes();

  private InterledgerAddress nodeAddress;
  private InterledgerAddress accountAddress;

  private LoopbackPlugin loopbackPlugin;

  public LoopbackPluginTest(final InterledgerAddress nodeAddress, final InterledgerAddress accountAddress) {
    this.nodeAddress = Objects.requireNonNull(nodeAddress);
    this.accountAddress = Objects.requireNonNull(accountAddress);
  }

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        {InterledgerAddress.of("test.node1"), InterledgerAddress.of("test.usd.destination")},
        {InterledgerAddress.of("test.node1"), InterledgerAddress.of("test.eur.destination")},
        {InterledgerAddress.of("test.node1"), InterledgerAddress.of("test.mxn.destination")},
        {InterledgerAddress.of("test.node1"), InterledgerAddress.of("test.btc.destination")},
    });
  }

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    // Enable debug mode...
    ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME))
        .setLevel(Level.DEBUG);

    this.loopbackPlugin = new LoopbackPlugin(nodeAddress, accountAddress);
  }

  @Test
  public void testSendData() {
    final InterledgerPreparePacket preparePacket = InterledgerPreparePacket.builder()
        .executionCondition(InterledgerCondition.of(new byte[32])) // unused in loopback.
        .expiresAt(Instant.now().plus(30, ChronoUnit.SECONDS))
        .destination(InterledgerAddress.of("test.foo"))
        .amount(BigInteger.TEN)
        .data(PREIMAGE)
        .build();

    final Optional<InterledgerResponsePacket> responsePacket = this.loopbackPlugin
        .sendData(preparePacket)
        .join();

    new InterledgerResponsePacketHandler() {
      @Override
      protected void handleFulfillPacket(final InterledgerFulfillPacket fulfillPacket) {
        assertThat(fulfillPacket.getFulfillment().getPreimage(), is(PREIMAGE));
        assertThat(fulfillPacket.getFulfillment().getCondition(),
            is(InterledgerFulfillment.of(PREIMAGE).getCondition()));
        assertThat(fulfillPacket.getData().length, is(0));
      }

      @Override
      protected void handleRejectPacket(InterledgerRejectPacket interledgerRejectPacket) {
        throw new RuntimeException("Should not reject!");
      }

      @Override
      protected void handleExpiredPacket() {
        throw new RuntimeException("Should not expire!");
      }
    }.handle(responsePacket);
  }

  @Test
  public void testSendMoney() throws ExecutionException, InterruptedException {
    this.loopbackPlugin.sendMoney(BigInteger.ZERO).get();
  }

  ////////////////////
  // Incoming Messages
  ////////////////////

  /**
   * Simulate an incoming Prepare packet, and assert that the handler is properly called.
   */
  @Test
  public void testIncomingData() {
    final InterledgerPreparePacket preparePacket = InterledgerPreparePacket.builder()
        .executionCondition(InterledgerCondition.of(new byte[32])) // unused in loopback.
        .expiresAt(Instant.now().plus(30, ChronoUnit.SECONDS))
        .destination(InterledgerAddress.of("test.foo"))
        .amount(BigInteger.TEN)
        .data(PREIMAGE)
        .build();

    final Optional<InterledgerResponsePacket> responsePacket = this.loopbackPlugin
        .safeGetDataHandler()
        .handleIncomingData(preparePacket)
        .join();

    new InterledgerResponsePacketHandler() {
      @Override
      protected void handleFulfillPacket(final InterledgerFulfillPacket fulfillPacket) {
        assertThat(fulfillPacket.getFulfillment().getPreimage(), is(PREIMAGE));
        assertThat(fulfillPacket.getFulfillment().getCondition(),
            is(InterledgerFulfillment.of(PREIMAGE).getCondition()));
        assertThat(fulfillPacket.getData().length, is(0));
      }

      @Override
      protected void handleRejectPacket(InterledgerRejectPacket interledgerRejectPacket) {
        throw new RuntimeException("Should not reject!");
      }

      @Override
      protected void handleExpiredPacket() {
        throw new RuntimeException("Should not expire!");
      }
    }.handle(responsePacket);

  }

  /**
   * Simulate an incoming Prepare packet, and assert that the handler is properly called.
   */
  @Test
  public void testIncomingMoney() throws ExecutionException, InterruptedException {
    loopbackPlugin.safeGetMoneyHandler().handleIncomingMoney(BigInteger.ZERO).get();
  }

}