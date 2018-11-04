package org.interledger.plugin.lpiv2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.interledger.plugin.lpiv2.SimulatedChildPlugin.ILP_DATA;
import static org.junit.Assert.fail;

import org.interledger.core.InterledgerAddress;
import org.interledger.core.InterledgerErrorCode;
import org.interledger.core.InterledgerFulfillPacket;
import org.interledger.core.InterledgerFulfillment;
import org.interledger.core.InterledgerPreparePacket;
import org.interledger.core.InterledgerProtocolException;
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
import java.util.concurrent.ExecutionException;

/**
 * A unit test for {@link SimulatedChildPlugin} to ensure that it is functioning properly.
 */
public class SimulatedChildPluginTest {

  private static final InterledgerPreparePacket PREPARE_PACKET = InterledgerPreparePacket.builder()
      .executionCondition(SimulatedChildPlugin.CONDITION)
      .expiresAt(Instant.now().plus(30, ChronoUnit.SECONDS))
      .destination(InterledgerAddress.of("test.foo"))
      .amount(BigInteger.TEN)
      .build();

  private SimulatedChildPlugin simulatedChildPlugin;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    // Enable debug mode...
    ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME))
        .setLevel(Level.DEBUG);

    this.simulatedChildPlugin = new SimulatedChildPlugin(TestHelpers.newPluginSettings());
  }

  @Test
  public void testSendDataThenFulfill() throws ExecutionException, InterruptedException {
    simulatedChildPlugin.simulatedCompleteSuccessfully(true);

    final InterledgerResponsePacket responsePacket = this.simulatedChildPlugin.sendData(PREPARE_PACKET).get();

    new InterledgerResponsePacketHandler() {
      @Override
      protected void mapFulfillPacket(InterledgerFulfillPacket interledgerFulfillPacket) {
        assertThat(responsePacket instanceof InterledgerFulfillPacket, is(true));
        InterledgerFulfillPacket actualFulfillPacket = (InterledgerFulfillPacket) responsePacket;
        assertThat(actualFulfillPacket.getFulfillment().getPreimage(), is(SimulatedChildPlugin.PREIMAGE));
        assertThat(actualFulfillPacket.getFulfillment().getCondition(),
            is(InterledgerFulfillment.of(SimulatedChildPlugin.PREIMAGE).getCondition()));
        assertThat(responsePacket.getData(), is(ILP_DATA));
      }

      @Override
      protected void mapRejectPacket(InterledgerRejectPacket interledgerRejectPacket) {
        throw new RuntimeException("Should not reject!");
      }
    }.map(responsePacket);

  }

  @Test
  public void testSendDataThenReject() {
    simulatedChildPlugin.simulatedCompleteSuccessfully(false);

    final InterledgerResponsePacket responsePacket = this.simulatedChildPlugin.sendData(PREPARE_PACKET).join();

    new InterledgerResponsePacketHandler() {
      @Override
      protected void mapFulfillPacket(InterledgerFulfillPacket interledgerFulfillPacket) {
        throw new RuntimeException("Should not Fulfill!");
      }

      @Override
      protected void mapRejectPacket(InterledgerRejectPacket interledgerRejectPacket) {
        assertThat(interledgerRejectPacket.getCode(), is(InterledgerErrorCode.F00_BAD_REQUEST));
        assertThat(interledgerRejectPacket.getTriggeredBy(),
            is(simulatedChildPlugin.getPluginSettings().getPeerAccountAddress()));
        assertThat(interledgerRejectPacket.getMessage(), is("SendData failed!"));
      }
    }.map(responsePacket);
  }

  @Test
  public void testSendMoney() throws ExecutionException, InterruptedException {
    simulatedChildPlugin.simulatedCompleteSuccessfully(true);
    this.simulatedChildPlugin.sendMoney(BigInteger.ZERO).get();
  }

  @Test(expected = InterledgerProtocolException.class)
  public void testSendMoneyWithReject() throws InterruptedException {
    simulatedChildPlugin.simulatedCompleteSuccessfully(false);

    try {
      this.simulatedChildPlugin.sendMoney(BigInteger.ZERO).get();
      fail();
    } catch (ExecutionException e) {
      final InterledgerProtocolException ilpe = (InterledgerProtocolException) e.getCause();
      assertThat(ilpe.getInterledgerRejectPacket().getCode(), is(InterledgerErrorCode.F00_BAD_REQUEST));
      assertThat(ilpe.getInterledgerRejectPacket().getTriggeredBy(),
          is(simulatedChildPlugin.getPluginSettings().getPeerAccountAddress()));
      assertThat(ilpe.getInterledgerRejectPacket().getMessage(), is("SendMoney failed!"));
      assertThat(ilpe.getMessage(), is("Interledger Rejection: SendMoney failed!"));
      throw ilpe;
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
    final InterledgerResponsePacket result = simulatedChildPlugin.getDataHandler()
        .handleIncomingData(InterledgerAddress.of("test.alice"), PREPARE_PACKET).join();

    assertThat(result, is(simulatedChildPlugin.getSendDataFulfillPacket()));
  }

  /**
   * Simulate an incoming Prepare packet, and assert that the handler is properly called.
   */
  @Test
  public void testIncomingDataRejection() {
    this.simulatedChildPlugin.simulatedCompleteSuccessfully(false);

    final InterledgerResponsePacket responsePacket = simulatedChildPlugin.getDataHandler()
        .handleIncomingData(InterledgerAddress.of("test.alice"), PREPARE_PACKET)
        .join();

    new InterledgerResponsePacketHandler() {
      @Override
      protected void mapFulfillPacket(InterledgerFulfillPacket interledgerFulfillPacket) {
        throw new RuntimeException("Should not Fulfill!");
      }

      @Override
      protected void mapRejectPacket(InterledgerRejectPacket interledgerRejectPacket) {
        assertThat(interledgerRejectPacket.getCode(), is(InterledgerErrorCode.F00_BAD_REQUEST));
        assertThat(interledgerRejectPacket.getTriggeredBy(),
            is(simulatedChildPlugin.getPluginSettings().getPeerAccountAddress()));
        assertThat(interledgerRejectPacket.getMessage(), is("Handle SendData failed!"));
      }
    }.map(responsePacket);

  }

  /**
   * Simulate an incoming Prepare packet, and assert that the handler is properly called.
   */
  @Test
  public void testIncomingMoney() throws ExecutionException, InterruptedException {
    // TODO: Add more tests here...
    simulatedChildPlugin.getMoneyHandler().handleIncomingMoney(BigInteger.ZERO).get();
  }

  /**
   * Simulate an incoming Prepare packet, and assert that the handler is properly called.
   */
  @Test(expected = RuntimeException.class)
  public void testIncomingMoneyRejection() {
    this.simulatedChildPlugin.simulatedCompleteSuccessfully(false);

    try {
      simulatedChildPlugin.getMoneyHandler().handleIncomingMoney(BigInteger.ZERO).join();
      fail();
    } catch (Exception e) {
      assertThat(e.getMessage(), is("sendMoney failed!"));
      throw e;
    }
  }


}