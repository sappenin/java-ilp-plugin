package org.interledger.plugin.lpiv2;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.interledger.core.InterledgerAddress;
import org.interledger.core.InterledgerCondition;
import org.interledger.core.InterledgerErrorCode;
import org.interledger.core.InterledgerPreparePacket;
import org.interledger.core.InterledgerProtocolException;
import org.interledger.core.InterledgerRejectPacket;
import org.interledger.core.InterledgerResponsePacket;
import org.interledger.plugin.lpiv2.TestHelpers.ExtendedPluginSettings;
import org.interledger.plugin.lpiv2.support.Futures;

import ch.qos.logback.classic.Level;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Unit test to validate that exceptions are emitted properly from {@link AbstractPlugin}.
 */
public class AbstractPluginExceptionTests {

//  private static final InterledgerAddress DESTINATION = InterledgerAddress.of("test.foo");
//
//  protected AbstractPlugin<ExtendedPluginSettings> plugin;
//
//  private ExtendedPluginSettings extendedPluginSettings;
//
//  private InterledgerPreparePacket preparePacket;
//
//  @Before
//  public void setup() {
//    MockitoAnnotations.initMocks(this);
//
//    // Enable debug mode...
//    ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.DEBUG);
//
//    this.extendedPluginSettings = TestHelpers.newPluginSettings();
//    this.preparePacket = InterledgerPreparePacket.builder()
//        .destination(DESTINATION)
//        .amount(BigInteger.TEN)
//        .expiresAt(Instant.now().plusSeconds(500))
//        .executionCondition(InterledgerCondition.of(new byte[32]))
//        .build();
//  }
//
//  ////////
//  // ILP Plugin Tests
//  ////////
//
//  /**
//   * Tests what happens when {@link Plugin#sendData(InterledgerPreparePacket)} throw an {@link
//   * InterledgerProtocolException} inside of the method, but not in a CompletableFuture.
//   */
//  @Test
//  public void testPluginWithException() {
//    plugin = new ExceptionalPlugin(extendedPluginSettings) {
//      @Override
//      public CompletableFuture<InterledgerResponsePacket> doSendData(InterledgerPreparePacket preparePacket)
//          throws InterledgerProtocolException {
//        throw new RuntimeException();
//      }
//    };
//
//    try {
//      plugin.sendData(preparePacket).handle((responsePacket, t)).get();
//    } catch (Exception e) {
//      assertThat(e.getMessage(), is(nullValue()));
//    }
//  }
//
//  /**
//   * Tests what happens when {@link Plugin#sendData(InterledgerPreparePacket)} throws an {@link
//   * InterledgerProtocolException} inside of a CompletableFuture running inside the method.
//   */
//  @Test
//  public void testPluginIlpExceptionInsideCompletableFuture() {
//    plugin = new ExceptionalPlugin(extendedPluginSettings) {
//      @Override
//      public CompletableFuture<InterledgerResponsePacket> doSendData(InterledgerPreparePacket preparePacket) {
//
//        return CompletableFuture.supplyAsync(() -> InterledgerRejectPacket.builder()
//            .code(InterledgerErrorCode.F00_BAD_REQUEST)
//            .triggeredBy(InterledgerAddress.of("test.foo"))
//            .message("foo-message")
//            .build());
//      }
//    };
//
//    InterledgerResponsePacket actual = plugin.sendData(preparePacket).join();
//
//    assertThat(actual instanceof InterledgerRejectPacket, is(true));
//    InterledgerRejectPacket actualRejectPacket = (InterledgerRejectPacket) actual;
//    assertThat(actualRejectPacket.getMessage(), is("foo-message"));
//    assertThat(actualRejectPacket.getCode(), is(InterledgerErrorCode.F00_BAD_REQUEST));
//
//  }
//
//  ////////
//  // Timeout Tests
//  ////////
//
//  /**
//   * Tests what happens when {@link Plugin#sendData(InterledgerPreparePacket)} throw an {@link
//   * InterledgerProtocolException} inside of the method.
//   */
//  @Test
//  public void testPluginTimeoutException() {
//    plugin = new ExceptionalPlugin(extendedPluginSettings) {
//      @Override
//      public CompletableFuture<InterledgerResponsePacket> doSendData(InterledgerPreparePacket preparePacket)
//          throws InterledgerProtocolException {
//
//        return CompletableFuture.supplyAsync(() -> {
//              try {
//                return CompletableFuture.supplyAsync(() -> {
//                  try {
//                    Thread.sleep(500);
//                  } catch (InterruptedException e) {
//                    throw new RuntimeException(e.getMessage(), e);
//                  }
//                  return (InterledgerResponsePacket) null;
//                }).get(1, TimeUnit.MICROSECONDS); // Timeout immeditately.
//              } catch (TimeoutException | InterruptedException | ExecutionException e) {
//                throw new CompletionException(e.getMessage(), e);
//              }
//            }
//        );
//      }
//    };
//
//    try {
//      plugin.sendData(preparePacket).join();
//    } catch (CompletionException e) {
//      assertThat(e.getCause() instanceof TimeoutException, is(true));
//    }
//  }
//
//  ////////
//  // NPE Tests
//  ////////
//
//  /**
//   * Tests what happens when {@link Plugin#sendData(InterledgerPreparePacket)} throws an {@link
//   * InterledgerProtocolException} inside of a CompletableFuture running inside the method.
//   */
//  @Test
//  public void testPluginNpeInsideCompletableFuture() {
//    plugin = new ExceptionalPlugin(extendedPluginSettings) {
//      @Override
//      public CompletableFuture<InterledgerResponsePacket> doSendData(InterledgerPreparePacket preparePacket)
//          throws InterledgerProtocolException {
//
//        return CompletableFuture.supplyAsync(() -> {
//          throw new NullPointerException("Oops!");
//        });
//      }
//    };
//
//    try {
//      plugin.sendData(preparePacket).get();
//    } catch (Exception e) {
//    }
//  }
//
//  @Test
//  public void testExceptionInCFUsingFuturesUtilsWithNPE() throws ExecutionException {
//
//    // This implementation throws an NPE inside of the CF.
//    plugin = new ExceptionalPlugin(extendedPluginSettings) {
//      @Override
//      public CompletableFuture<InterledgerResponsePacket> doSendData(InterledgerPreparePacket preparePacket)
//          throws InterledgerProtocolException {
//
//        return CompletableFuture.supplyAsync(() -> {
//          throw new NullPointerException("Oops!");
//
//        });
//      }
//    };
//
//    plugin.sendData(preparePacket)
//        .exceptionally(Futures.on(InterledgerProtocolException.class, e -> {
//
//          // let downstream "know" that unexpected bad things happened
//          throw new RuntimeException(e);
//        }))
//        .exceptionally(Futures.on(ExecutionException.class, e -> {
//          throw (RuntimeException) e.getCause();
//        }))
//        .exceptionally(Futures.on(CompletionException.class, e -> {
//          throw (RuntimeException) e.getCause();
//        }))
//        .exceptionally((err) -> {
//          throw new RuntimeException(err.getMessage(), err);
//        });
//
//  }
//
//  //////////////////
//  // Helper Methods.
//  //////////////////
//
//  private InterledgerProtocolException newInterledgerProtocolException() {
//    return new InterledgerProtocolException(
//        InterledgerRejectPacket.builder()
//            .code(InterledgerErrorCode.F00_BAD_REQUEST)
//            .triggeredBy(InterledgerAddress.of("test.foo"))
//            .message("foo-message")
//            .build()
//    );
//  }
//
//  /**
//   * An abstract extension of {@link AbstractPlugin} that allow {@link Plugin#sendData(InterledgerPreparePacket)} to be
//   * overidden to simulate various exception scenarios.
//   */
//  private abstract static class ExceptionalPlugin extends AbstractPlugin<ExtendedPluginSettings> {
//
//    protected ExceptionalPlugin(ExtendedPluginSettings pluginSettings) {
//      super(pluginSettings);
//    }
//
//    @Override
//    public CompletableFuture<Void> doConnect() {
//      return CompletableFuture.completedFuture(null);
//    }
//
//    @Override
//    public CompletableFuture<Void> doDisconnect() {
//      return CompletableFuture.completedFuture(null);
//    }
//
//
//    @Override
//    protected CompletableFuture<Void> doSendMoney(BigInteger amount) {
//      return CompletableFuture.completedFuture(null);
//    }
//  }
}
