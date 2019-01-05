package org.interledger.plugin.lpiv2.btp2.spring;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;

import org.interledger.plugin.lpiv2.btp2.spring.PendingResponseManager.PendingResponse;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for {@link PendingResponseManager}.
 */
public class PendingResponseManagerTest {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private PendingResponseManager<String> pendingResponseManager;

  @Before
  public void setup() {
    this.pendingResponseManager = new PendingResponseManager<>(String.class);
  }

  @Test
  public void testRemoveOnCancelPolicy() {
    assertThat(pendingResponseManager.getExpiryExecutor().getRemoveOnCancelPolicy(), is(true));
  }

  @Test(expected = RuntimeException.class)
  public void testScheduleSamePendingResponseTwice() {
    pendingResponseManager.registerPendingResponse(1, 2000, TimeUnit.MILLISECONDS);
    try {
      pendingResponseManager.registerPendingResponse(1, 2000, TimeUnit.MILLISECONDS);
    } catch (RuntimeException e) {
      assertThat(e.getMessage(), is("Attempted to schedule PendingResponse `1` twice!"));
      throw e;
    }
  }

  ////////////////////////////
  // Successful Response Tests
  ////////////////////////////

  /**
   * Tests a single call of the Manager for the success scenario.
   */
  @Test
  public void testSingleSuccessfulResponse() {
    long requestId = 1L;

    final CompletableFuture<String> response = pendingResponseManager
        .registerPendingResponse(requestId, 2000, TimeUnit.MILLISECONDS);
    assertThat(response.isDone(), is(false));
    assertThat(response.isCompletedExceptionally(), is(false));
    assertThat(response.isCancelled(), is(false));

    final PendingResponse<String> pendingResponse =
        pendingResponseManager.joinPendingResponse(requestId, "foo");

    assertThat(response.join(), is("foo"));
    this.validateCompletableFutureState(response, true, false, false, 0);

    // Expect the caller to NOT see an exception!
    response.handle((actualValue, error) -> {
      if (error != null) {
        fail("Unexpected Error: " + error);
      } else {
        assertThat(actualValue.equals("foo"), is(true));
      }
      return null;
    }).join();

    // PendingResponse Verification
    assertThat(pendingResponse.getRequestId(), is(requestId));
    this.validateCompletableFutureState(pendingResponse.getTimeoutFuture(), true, true, true, 0);
    this.validateCompletableFutureState(pendingResponse.getJoinableResponseFuture(), true, false, false, 0);

    // Internal State Validations
    assertThat(pendingResponseManager.getPendingResponses().size(), is(0));
    this.assertEmptyExpiryExecutor();
  }

  /**
   * Tests a single call of the Manager for the success scenario.
   */
  @Test
  public void testMultipleSuccessfulResponses() {
    final int numThreads = 227;

    // Make 227 PendingResponses, all waiting...
    final List<CompletableFuture<String>> responses = Lists.newArrayList();
    for (int requestId = 0; requestId < numThreads; requestId++) {
      responses.add(pendingResponseManager.registerPendingResponse(requestId, 1000, TimeUnit.MILLISECONDS));
    }

    // Queue up 227 CompletableFutures that will join randomly, but with the correct request id.
    final List<CompletableFuture<PendingResponse<String>>> pendingResponses = Lists.newArrayList();
    for (int requestId = 0; requestId < numThreads; requestId++) {
      final int fRequestId = requestId;
      pendingResponses.add(
          CompletableFuture.supplyAsync(() -> pendingResponseManager.joinPendingResponse(fRequestId, fRequestId + ""))
      );
    }

    final AtomicInteger counter = new AtomicInteger();
    pendingResponses.parallelStream()
        .map(CompletableFuture::join)
        .forEach(pendingResponse -> {
          counter.incrementAndGet();
          validateCompletableFutureState(pendingResponse.getTimeoutFuture(), true, true, true, 0);
          validateCompletableFutureState(pendingResponse.getJoinableResponseFuture(), true, false, false, 0);

          // Expect the caller to NOT see an exception!
          pendingResponse.getJoinableResponseFuture().handle((actualValue, error) -> {
            if (error != null) {
              fail("Unexpected Error: " + error);
            } else {
              //assertThat(actualValue.equals("foo"), is(true));
            }
            return null;
          }).join();

        });
    assertThat(counter.get(), is(numThreads));

    // Internal State Validations
    assertThat(pendingResponseManager.getPendingResponses().size(), is(0));
    this.assertEmptyExpiryExecutor();
    // Must be called after assertEmptyExpiryExecutor
    assertThat(pendingResponseManager.getExpiryExecutor().getCompletedTaskCount(), is(Long.valueOf(numThreads)));
  }

  /////////////////////////////////////
  // Timeout Response Before Join Tests
  /////////////////////////////////////

  /**
   * Tests a single call of the Manager for the timeout scenario where the Timeout occurs with no join having been
   * called.
   */
  @Test
  public void testSingleTimeoutResponseWithNoJoin() throws InterruptedException {
    long requestId = 1L;

    // Virtually no delay, so should always expire.
    final CompletableFuture<String> response = pendingResponseManager
        .registerPendingResponse(requestId, 0, TimeUnit.NANOSECONDS);
    // Give the ScheduledThreadExecutor a moment to cleanup...
    Thread.sleep(200);
    this.validateCompletableFutureState(response, true, false, true, 0);

    // Expect the caller to see an exception!
    response.handle((actualValue, error) -> {
      if (error != null) {
        assertThat(error instanceof CompletionException, is(true));
        assertThat(error.getMessage(), is("java.util.concurrent.TimeoutException"));
      } else {
        fail("Unexpected Completion: " + actualValue);
      }
      return null;
    }).join();

    this.validateCompletableFutureState(response, true, false, true, 0);

    // Internal State Validations
    assertThat(pendingResponseManager.getPendingResponses().size(), is(0));
    this.assertEmptyExpiryExecutor();
  }

  /**
   * Tests a single call of the Manager for the timeout scenario where the Timeout occurs and then the pendingResponse
   * is joined (an Exception is expected in this case because there will be no CF to join due to timeout).
   */
  @Test
  public void testSingleTimeoutResponseWithLaterJoin() throws InterruptedException {
    long requestId = 1L;

    // Virtually no delay, so should always expire.
    final CompletableFuture<String> response = pendingResponseManager
        .registerPendingResponse(requestId, 1, TimeUnit.NANOSECONDS);
    // Give the ScheduledThreadExecutor a moment to cleanup...
    Thread.sleep(200);
    this.validateCompletableFutureState(response, true, false, true, 0);

    // Expect the caller to see an exception!
    response.handle((actualValue, error) -> {
      if (error != null) {
        assertThat(error instanceof CompletionException, is(true));
        assertThat(error.getMessage(), is("java.util.concurrent.TimeoutException"));
      } else {
        fail("Unexpected Completion: " + actualValue);
      }
      return null;
    }).join();

    this.validateCompletableFutureState(response, true, false, true, 0);

    // PendingResponse Verification
    try {
      pendingResponseManager.joinPendingResponse(requestId, "foo");
    } catch (RuntimeException e) {
      assertThat(e.getMessage(), is("No PendingResponse available to connect to responseToReturn: foo"));
    }

    // Internal State Validations
    assertThat(pendingResponseManager.getPendingResponses().size(), is(0));
    this.assertEmptyExpiryExecutor();
  }

  /**
   * Tests a single call of the Manager for the success scenario.
   */
  @Test
  public void testMultipleTimeoutResponses() {
    final int numThreads = 137;

    // Make 138 PendingResponses, all which expire immediately...
    final List<CompletableFuture<String>> responses = Lists.newArrayList();
    for (int requestId = 0; requestId < numThreads; requestId++) {
      responses.add(pendingResponseManager.registerPendingResponse(requestId, 1, TimeUnit.NANOSECONDS));
    }

    // Validate each response to the caller...
    responses.stream().forEach(cfResponse -> {
      // Expect the caller to see an exception!
      cfResponse.handle((actualValue, error) -> {
        if (error != null) {
          assertThat(error instanceof CompletionException, is(true));
          assertThat(error.getMessage(), is("java.util.concurrent.TimeoutException"));
        } else {
          fail("Unexpected Completion: " + actualValue);
        }
        return null;
      }).join();
    });

    // Internal State Validations
    assertThat(pendingResponseManager.getPendingResponses().size(), is(0));
    this.assertEmptyExpiryExecutor();
    // Must be called after assertEmptyExpiryExecutor
    assertThat(pendingResponseManager.getExpiryExecutor().getCompletedTaskCount(), is(Long.valueOf(numThreads)));

    // Queue up 138 CompletableFutures that will join randomly, but with the correct request id.
    final List<CompletableFuture<PendingResponse<String>>> pendingResponses = Lists.newArrayList();
    for (int requestId = 0; requestId < numThreads; requestId++) {
      final int fRequestId = requestId;
      pendingResponses.add(
          CompletableFuture.supplyAsync(() -> pendingResponseManager.joinPendingResponse(fRequestId, fRequestId + ""))
      );
    }

    final AtomicInteger counter = new AtomicInteger();
    pendingResponses.parallelStream().forEach(
        pendingResponse -> pendingResponse.handle((actualValue, error) -> {
          if (error != null) {
            assertThat(error.getClass().getName(), is(CompletionException.class.getName()));
            assertThat(error.getMessage().startsWith(
                "org.interledger.plugin.lpiv2.btp2.spring.PendingResponseManager$NoPendingResponseException: No "
                    + "PendingResponse available to connect to responseToReturn"),
                is(true));
            counter.incrementAndGet();
          } else {
            fail("Should have thrown a `NoPendingResponseException`!");
          }
          return null;
        }).join()
    );
    assertThat(counter.get(), is(numThreads));

    // Internal State Validations
    assertThat(pendingResponseManager.getPendingResponses().size(), is(0));
    this.assertEmptyExpiryExecutor();
    // Must be called after assertEmptyExpiryExecutor
    assertThat(pendingResponseManager.getExpiryExecutor().getCompletedTaskCount(), is(Long.valueOf(numThreads)));
  }

  ////////////////////////////
  // Exceptional Response Tests
  ////////////////////////////

  /**
   * Tests a single call of the Manager for the success scenario.
   */
  @Test
  public void testSingleExceptionalResponse() {
    long requestId = 1L;

    final CompletableFuture<String> response = pendingResponseManager
        .registerPendingResponse(requestId, 2000, TimeUnit.MILLISECONDS);
    assertThat(response.isDone(), is(false));
    assertThat(response.isCompletedExceptionally(), is(false));
    assertThat(response.isCancelled(), is(false));

    final PendingResponse<String> pendingResponse =
        pendingResponseManager.joinPendingResponse(requestId, "foo");

    assertThat(response.join(), is("foo"));
    this.validateCompletableFutureState(response, true, false, false, 0);

    // Expect the caller to NOT see an exception!
    response.handle((actualValue, error) -> {
      if (error != null) {
        fail("Unexpected Error: " + error);
      } else {
        assertThat(actualValue.equals("foo"), is(true));
      }
      return null;
    }).join();

    // PendingResponse Verification
    assertThat(pendingResponse.getRequestId(), is(requestId));
    this.validateCompletableFutureState(pendingResponse.getTimeoutFuture(), true, true, true, 0);
    this.validateCompletableFutureState(pendingResponse.getJoinableResponseFuture(), true, false, false, 0);

    // Internal State Validations
    assertThat(pendingResponseManager.getPendingResponses().size(), is(0));
    this.assertEmptyExpiryExecutor();
  }

  //////////////////
  // Private Helpers
  //////////////////

  /**
   * Helper method to keep checking the Scheduled Executor's queue to make sure it is properly emptied.
   */
  private void assertEmptyExpiryExecutor() {
    final ScheduledThreadPoolExecutor expiryExecutor = pendingResponseManager.getExpiryExecutor();

    // Don't wait more than 20 seconds.
    for (int i = 0; i < 40; i++) {
      // Wait for the task to be removed from the Manager.
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }

      if (expiryExecutor.getQueue().size() > 0) {
        logger.info("Skipping check number {}", i + 1);
        continue;
      } else {
        break;
      }
    }

    System.out.println(expiryExecutor.toString());
    assertThat(expiryExecutor.getQueue().size(), is(0));
  }

  /**
   * Helper to verify the state of a CompletableFuture.
   *
   * @param completableFuture        The {@link CompletableFuture} to validate.
   * @param isDone                   The expected value for {@link CompletableFuture#isDone()}.
   * @param isCancelled              The expected value for {@link CompletableFuture#isCancelled()}.
   * @param isCompletedExceptionally The expected value for {@link CompletableFuture#isCompletedExceptionally()}.
   * @param numberOfDependents       The expected value for {@link CompletableFuture#getNumberOfDependents()}.
   */
  private void validateCompletableFutureState(
      final CompletableFuture completableFuture, final boolean isDone, final boolean isCancelled,
      final boolean isCompletedExceptionally, final int numberOfDependents
  ) {
    assertThat("done", completableFuture.isDone(), is(isDone));
    assertThat("isCancelled", completableFuture.isCancelled(), is(isCancelled));
    assertThat("isCompletedExceptionally", completableFuture.isCompletedExceptionally(), is(isCompletedExceptionally));
    assertThat("numberOfDependents", completableFuture.getNumberOfDependents(), is(numberOfDependents));
  }
}