package org.interledger.plugin.lpiv2;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.interledger.plugin.lpiv2.SimulatedChildPlugin.ILP_DATA;
import static org.interledger.plugin.lpiv2.TestHelpers.PREIMAGE;

import org.interledger.core.InterledgerAddress;
import org.interledger.core.InterledgerCondition;
import org.interledger.core.InterledgerErrorCode;
import org.interledger.core.InterledgerFulfillPacket;
import org.interledger.core.InterledgerFulfillment;
import org.interledger.core.InterledgerPreparePacket;
import org.interledger.core.InterledgerProtocolException;
import org.interledger.core.InterledgerRuntimeException;

import ch.qos.logback.classic.Level;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * A unit test for {@link SimulatedChildPlugin} to ensure that it is functioning properly.
 */
public class SimulatedChildPluginTest {

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
    final InterledgerFulfillPacket actual = this.simulatedChildPlugin.sendData(
        InterledgerPreparePacket.builder()
            .executionCondition(InterledgerCondition.of(PREIMAGE))
            .expiresAt(Instant.now().plus(5, ChronoUnit.SECONDS))
            .destination(InterledgerAddress.of("test1.foo"))
            .amount(BigInteger.ZERO)
            .build()
    ).get();

    assertThat(actual.getFulfillment().getPreimage(), is(SimulatedChildPlugin.PREIMAGE));
    assertThat(actual.getFulfillment().getCondition(),
        is(InterledgerFulfillment.of(SimulatedChildPlugin.PREIMAGE).getCondition()));
    assertThat(actual.getData(), is(ILP_DATA));
  }

  @Test(expected = InterledgerProtocolException.class)
  public void testSendDataThenReject() throws InterruptedException {
    simulatedChildPlugin.simulatedCompleteSuccessfully(false);

    try {
      this.simulatedChildPlugin.sendData(
          InterledgerPreparePacket.builder()
              .executionCondition(InterledgerCondition.of(PREIMAGE))
              .expiresAt(Instant.now().plus(5, ChronoUnit.SECONDS))
              .destination(InterledgerAddress.of("test1.foo"))
              .amount(BigInteger.ZERO)
              .build()
      ).get();
    } catch (ExecutionException e) {
      final InterledgerProtocolException ilpe = (InterledgerProtocolException) e.getCause();
      assertThat(ilpe.getInterledgerRejectPacket().getCode(), is(InterledgerErrorCode.F00_BAD_REQUEST));
      assertThat(ilpe.getInterledgerRejectPacket().getTriggeredBy(),
          is(simulatedChildPlugin.getPluginSettings().getPeerAccountAddress()));
      assertThat(ilpe.getInterledgerRejectPacket().getMessage(), is("SendData failed!"));
      assertThat(ilpe.getMessage(), is("Interledger Rejection: SendData failed!"));
      throw ilpe;
    }
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
  public void testIncomingData() throws ExecutionException, InterruptedException {
    final InterledgerPreparePacket packet = InterledgerPreparePacket.builder()
        .executionCondition(InterledgerCondition.of(PREIMAGE))
        .expiresAt(Instant.now().plus(5, ChronoUnit.SECONDS))
        .destination(InterledgerAddress.of("test1.foo"))
        .amount(BigInteger.ZERO)
        .build();

    final CompletableFuture<InterledgerFulfillPacket> result = simulatedChildPlugin
        .onIncomingData(InterledgerAddress.of("test.alice"), packet);

    assertThat(result.isDone(), is(false));
    assertThat(result.get(), is(simulatedChildPlugin.getFulfillPacket()));
    assertThat(result.isDone(), is(true));
  }

  /**
   * Simulate an incoming Prepare packet, and assert that the handler is properly called.
   */
  @Test(expected = InterledgerRuntimeException.class)
  public void testIncomingDataRejection() throws InterruptedException {
    this.simulatedChildPlugin.simulatedCompleteSuccessfully(false);

    final InterledgerPreparePacket packet = InterledgerPreparePacket.builder()
        .executionCondition(InterledgerCondition.of(PREIMAGE))
        .expiresAt(Instant.now().plus(5, ChronoUnit.SECONDS))
        .destination(InterledgerAddress.of("test1.foo"))
        .amount(BigInteger.ZERO)
        .build();

    try {
      simulatedChildPlugin.onIncomingData(InterledgerAddress.of("test.alice"), packet).get();
    } catch (ExecutionException e) {
      assertThat(e.getCause() instanceof InterledgerRuntimeException, is(true));
      InterledgerRuntimeException ire = (InterledgerRuntimeException) e.getCause();
      assertThat(ire.getMessage(), is("Interledger Rejection: Handle SendData failed!"));
      throw ire;
    }
  }

  /**
   * Simulate an incoming Prepare packet, and assert that the handler is properly called.
   */
  @Test
  public void testIncomingMoney() throws ExecutionException, InterruptedException {
    final CompletableFuture<Void> result = simulatedChildPlugin.onIncomingMoney(BigInteger.ZERO);

    assertThat(result.isDone(), is(false));
    assertThat(result.get(), is(nullValue()));
    assertThat(result.isDone(), is(true));
  }

  /**
   * Simulate an incoming Prepare packet, and assert that the handler is properly called.
   */
  @Test(expected = InterledgerRuntimeException.class)
  public void testIncomingMoneyRejection() throws InterruptedException {
    this.simulatedChildPlugin.simulatedCompleteSuccessfully(false);

    final InterledgerPreparePacket packet = InterledgerPreparePacket.builder()
        .executionCondition(InterledgerCondition.of(PREIMAGE))
        .expiresAt(Instant.now().plus(5, ChronoUnit.SECONDS))
        .destination(InterledgerAddress.of("test1.foo"))
        .amount(BigInteger.ZERO)
        .build();

    try {
      simulatedChildPlugin.onIncomingMoney(BigInteger.ZERO).get();
    } catch (ExecutionException e) {
      assertThat(e.getCause() instanceof InterledgerRuntimeException, is(true));
      InterledgerRuntimeException ire = (InterledgerRuntimeException) e.getCause();
      assertThat(ire.getMessage(), is("Interledger Rejection: Handle SendMoney failed!"));
      throw ire;
    }
  }


}