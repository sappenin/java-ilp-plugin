package org.interledger.plugin.lpiv2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.interledger.plugin.lpiv2.SimulatedPlugin.ILP_DATA;
import static org.junit.Assert.fail;

import org.interledger.core.InterledgerAddress;
import org.interledger.core.InterledgerErrorCode;
import org.interledger.core.InterledgerFulfillPacket;
import org.interledger.core.InterledgerFulfillment;
import org.interledger.core.InterledgerPreparePacket;
import org.interledger.core.InterledgerRejectPacket;
import org.interledger.core.InterledgerResponsePacket;
import org.interledger.core.InterledgerResponsePacketHandler;

import ch.qos.logback.classic.Level;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

/**
 * A unit test for {@link SimulatedPlugin} to ensure that it is functioning properly.
 */
public class SimulatedPluginTest {

  private static final InterledgerPreparePacket PREPARE_PACKET = InterledgerPreparePacket.builder()
      .executionCondition(SimulatedPlugin.CONDITION)
      .expiresAt(Instant.now().plus(30, ChronoUnit.SECONDS))
      .destination(InterledgerAddress.of("test.foo"))
      .amount(BigInteger.TEN)
      .build();

  private SimulatedPlugin simulatedPlugin;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    // Enable debug mode...
    ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME))
        .setLevel(Level.DEBUG);

    this.simulatedPlugin = new SimulatedPlugin(TestHelpers.newPluginSettings());
  }

  @Test
  public void testSendDataThenFulfill() {
    simulatedPlugin.setExpectedSendDataCompletionState(ExpectedSendDataCompletionState.FULFILL);

    final Optional<InterledgerResponsePacket> responsePacket = this.simulatedPlugin.sendData(PREPARE_PACKET).join();

    new InterledgerResponsePacketHandler() {
      @Override
      protected void handleFulfillPacket(InterledgerFulfillPacket fulfillPacket) {
        assertThat(fulfillPacket.getFulfillment().getPreimage(), is(SimulatedPlugin.PREIMAGE));
        assertThat(fulfillPacket.getFulfillment().getCondition(),
            is(InterledgerFulfillment.of(SimulatedPlugin.PREIMAGE).getCondition()));
        assertThat(fulfillPacket.getData(), is(ILP_DATA));
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
  public void testSendDataThenReject() {
    simulatedPlugin.setExpectedSendDataCompletionState(ExpectedSendDataCompletionState.REJECT);

    final Optional<InterledgerResponsePacket> responsePacket = this.simulatedPlugin.sendData(PREPARE_PACKET).join();

    new InterledgerResponsePacketHandler() {
      @Override
      protected void handleFulfillPacket(InterledgerFulfillPacket interledgerFulfillPacket) {
        throw new RuntimeException("Should not Fulfill!");
      }

      @Override
      protected void handleRejectPacket(InterledgerRejectPacket interledgerRejectPacket) {
        assertThat(interledgerRejectPacket.getCode(), is(InterledgerErrorCode.F00_BAD_REQUEST));
        assertThat(interledgerRejectPacket.getTriggeredBy(),
            is(simulatedPlugin.getPluginSettings().getPeerAccountAddress()));
        assertThat(interledgerRejectPacket.getMessage(), is("SendData failed!"));
      }

      @Override
      protected void handleExpiredPacket() {
        throw new RuntimeException("Should not expire!");
      }
    }.handle(responsePacket);
  }

  @Test
  public void testSendMoney() throws ExecutionException, InterruptedException {
    simulatedPlugin.setExpectedSendMoneyCompletionState(ExpectedSendMoneyCompletionState.SUCCESS);
    this.simulatedPlugin.sendMoney(BigInteger.ZERO).get();
  }

  @Test(expected = RuntimeException.class)
  public void testSendMoneyFailure() {
    simulatedPlugin.setExpectedSendMoneyCompletionState(ExpectedSendMoneyCompletionState.FAILURE);

    try {
      this.simulatedPlugin.sendMoney(BigInteger.ZERO).join();
      fail();
    } catch (CompletionException e) {
      final RuntimeException expectedException = (RuntimeException) e.getCause();
      assertThat(expectedException.getMessage(), is("sendMoney Failed!"));
      throw expectedException;
    }
  }

  ////////////////////
  // Incoming Messages
  ////////////////////

  /**
   * Simulate an incoming Prepare packet, and assert that the handler is properly called.
   */
  @Test
  public void testIncomingData() {
    final Optional<InterledgerResponsePacket> result = simulatedPlugin
        .safeGetDataHandler().handleIncomingData(PREPARE_PACKET).join();

    assertThat(result.isPresent(), is(true));
    assertThat(result.get(), is(simulatedPlugin.getSendDataFulfillPacket()));
  }

  /**
   * Simulate an incoming Prepare packet, and assert that the handler is properly called.
   */
  @Test
  public void testIncomingDataRejection() {
    this.simulatedPlugin.setExpectedSendDataCompletionState(ExpectedSendDataCompletionState.REJECT);

    final Optional<InterledgerResponsePacket> responsePacket = simulatedPlugin
        .safeGetDataHandler().handleIncomingData(PREPARE_PACKET).join();

    new InterledgerResponsePacketHandler() {
      @Override
      protected void handleFulfillPacket(InterledgerFulfillPacket interledgerFulfillPacket) {
        throw new RuntimeException("Should not Fulfill!");
      }

      @Override
      protected void handleRejectPacket(InterledgerRejectPacket interledgerRejectPacket) {
        assertThat(interledgerRejectPacket.getCode(), is(InterledgerErrorCode.F00_BAD_REQUEST));
        assertThat(interledgerRejectPacket.getTriggeredBy(),
            is(simulatedPlugin.getPluginSettings().getPeerAccountAddress()));
        assertThat(interledgerRejectPacket.getMessage(), is("Handle SendData failed!"));
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
  public void testIncomingDataExpiration() {
    this.simulatedPlugin.setExpectedSendDataCompletionState(ExpectedSendDataCompletionState.EXPIRE);

    final Optional<InterledgerResponsePacket> responsePacket = simulatedPlugin.safeGetDataHandler()
        .handleIncomingData(PREPARE_PACKET).join();

    new InterledgerResponsePacketHandler() {
      @Override
      protected void handleFulfillPacket(InterledgerFulfillPacket interledgerFulfillPacket) {
        throw new RuntimeException("Should not Fulfill!");
      }

      @Override
      protected void handleRejectPacket(InterledgerRejectPacket interledgerRejectPacket) {
        throw new RuntimeException("Should not Reject!");
      }

      @Override
      protected void handleExpiredPacket() {
        // Nothing to check here...
      }
    }.handle(responsePacket);
  }

  /**
   * Simulate an incoming Prepare packet, and assert that the handler is properly called.
   */
  @Test
  public void testIncomingMoney() throws ExecutionException, InterruptedException {
    // TODO: Add more tests here...
    simulatedPlugin.safeGetMoneyHandler().handleIncomingMoney(BigInteger.ZERO).get();
  }

  /**
   * Simulate an incoming Prepare packet, and assert that the handler is properly called.
   */
  @Test(expected = RuntimeException.class)
  public void testIncomingMoneyRejection() {
    this.simulatedPlugin.setExpectedSendMoneyCompletionState(ExpectedSendMoneyCompletionState.FAILURE);

    try {
      simulatedPlugin.safeGetMoneyHandler().handleIncomingMoney(BigInteger.ZERO).join();
      fail();
    } catch (CompletionException e) {
      final RuntimeException expectedException = (RuntimeException) e.getCause();
      assertThat(expectedException.getMessage(), is("sendMoney failed!"));
      throw expectedException;
    }
  }
}