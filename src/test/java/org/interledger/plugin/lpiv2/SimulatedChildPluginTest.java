package org.interledger.plugin.lpiv2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.interledger.plugin.lpiv2.TestHelpers.PREIMAGE;

import org.interledger.core.InterledgerAddress;
import org.interledger.core.InterledgerCondition;
import org.interledger.core.InterledgerErrorCode;
import org.interledger.core.InterledgerFulfillPacket;
import org.interledger.core.InterledgerFulfillment;
import org.interledger.core.InterledgerPreparePacket;
import org.interledger.core.InterledgerProtocolException;

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
  public void testSendPacketThenFulfill() throws ExecutionException, InterruptedException {
    simulatedChildPlugin.setCompleteSuccessfully(true);
    final InterledgerFulfillPacket actual = this.simulatedChildPlugin.sendPacket(
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
    assertThat(actual.getData(), is(SimulatedChildPlugin.ILP_DATA));
  }

  @Test(expected = InterledgerProtocolException.class)
  public void testSendPacketThenReject() throws InterruptedException {
    simulatedChildPlugin.setCompleteSuccessfully(false);

    try {
      this.simulatedChildPlugin.sendPacket(
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
          is(simulatedChildPlugin.getPluginSettings().peerAccount()));
      assertThat(ilpe.getInterledgerRejectPacket().getMessage(), is("Don't do this!"));
      assertThat(ilpe.getMessage(), is("Interledger Rejection: Don't do this!"));
      throw ilpe;
    }
  }
}