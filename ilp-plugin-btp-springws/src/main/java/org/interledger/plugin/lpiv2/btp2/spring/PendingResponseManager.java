package org.interledger.plugin.lpiv2.btp2.spring;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.immutables.value.Value.Derived;
import org.immutables.value.Value.Modifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * <p>Constructs and manages PendingResponses, which are a mechanism to bridge between the asynchronous nature of a
 * WebSocket connection and the {@link CompletableFuture} API.</p>
 *
 * <p>Using this mechanism, a {@link CompletableFuture} can be returned to a caller while the system operating this
 * manager waits for an incoming  WebSocket response message (this incoming response message will always be a result of
 * an outgoing request message sent over the same WebSocket session). If no WebSocket response message is received
 * withing a given time-bound, then the original CompleteableFuture returned to the caller will Timeout, allowing the
 * caller to not have to wait more than a desired amount of time.</p>
 *
 * @param <T> The type of response this manager will hold.
 */
@SuppressWarnings("unused")
public class PendingResponseManager<T> {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  //private final Map<Long, PendingResponse<T>> pendingResponses;
  private final Cache<Long, PendingResponse<T>> pendingResponses;

  // Used for running all expiry threads to cancel pendingResponses.
  private final ScheduledExecutorService expiryExecutor;

  // The type of response expected to be returned by this Manager.
  private Class<T> typeClass;

  /**
   * Required-args Constructor.
   *
   * @param typeClass The {@link Class} that this manager will manage. Required for type-introspection, and should
   *                  always match {@link T}.
   */
  public PendingResponseManager(final Class<T> typeClass) {
    this(typeClass, 10);
  }

  /**
   * @param typeClass    The {@link Class} that this manager will manage. Required for type-introspection, and should
   *                     always match {@link T}.
   * @param corePoolSize the number of threads to keep in the expiry thread pool, even if they are idle.
   */
  public PendingResponseManager(final Class<T> typeClass, final int corePoolSize) {
    this(typeClass, corePoolSize, 5000);
  }

  /**
   * @param typeClass               The {@link Class} that this manager will manage. Required for type-introspection,
   *                                and should always match {@link T}.
   * @param corePoolSize            the number of threads to keep in the expiry thread pool, even if they are idle.
   * @param expireAfterAccessMillis The number of milliseconds that a PendingResponse should hang around before being
   *                                evicted from the underlying collection. Generally, this should be set to `5000`
   *                                because only internal mechanisms call `get` on the cache, which triggers an
   *                                eviction. However, for testing purposes can be twiddled here.
   */
  PendingResponseManager(final Class<T> typeClass, final int corePoolSize, final long expireAfterAccessMillis) {

    // No Weak-references because we don't want underlying pendingRequests to be GC'd until they're removed from this
    // cache. `expireAfterAccess` ensures
    this.pendingResponses = CacheBuilder.newBuilder()
        .maximumSize(64000)
        // We want pendingRequest to hang around for a few seconds after their timeout so we can detect which operation
        // won. But we want to remove them automatically (or at least make the eligible) soon after they are read
        // which is typically only after a join).
        .expireAfterAccess(expireAfterAccessMillis, TimeUnit.MILLISECONDS)
        // No pendingResponse should live longer than 5 minutes.
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .concurrencyLevel(8)
        .initialCapacity(256)
        //.recordStats()
        .removalListener(notification -> logger.debug("PendingRequest {} removed from Cache: {}", notification))
        .build();
    this.typeClass = Objects.requireNonNull(typeClass);

    // Used to timeout blocking futures...
    this.expiryExecutor = new ScheduledThreadPoolExecutor(corePoolSize);
    ((ScheduledThreadPoolExecutor) this.expiryExecutor).setRemoveOnCancelPolicy(true);
  }

  /**
   * Helper method to call {@link #registerPendingResponse(long, String, Duration)} without a description.
   *
   * @param requestId
   * @param timeoutAfter
   *
   * @return
   */
  protected final CompletableFuture<T> registerPendingResponse(
      final long requestId, final Duration timeoutAfter
  ) {
    return this.registerPendingResponse(requestId, "n/a", timeoutAfter);
  }

  /**
   * <p>Register and return a "pending response", mapping it to the supplied {@code requestId}. This mechanism works by
   * returning a completed future to a caller, who then waits for the future to be completed. The receiver processes the
   * request, and eventually returns a response by completing the appropriate <tt>PendingResponse</tt>.</p>
   *
   * <p>The following diagram illustrates this flow:</p>
   *
   * <pre>
   * ┌──────────┐                                              ┌──────────┐
   * │          │────────────Request (Object)─────────────────▷│          │
   * │          │                                              │          │
   * │          │             Response (Uncompleted            │          │
   * │          │◁─────────────CompletableFuture)───△──────────┤          │
   * │          │                                   │          │          │
   * │          │                                   │          │          │
   * │          │                                   │          │          │
   * │          │                                   │          │          │
   * │  Sender  │                                   │ Complete │ Receiver │
   * │          │                                   └or Timeout┤          │
   * │          │                                              │          │
   * │          │                                              │          │
   * │          │                                              │          │
   * │          │                                              │          │
   * │          │                                              │          │
   * │          │                                              │          │
   * │          │                                              │          │
   * └──────────┘                                              └──────────┘
   * </pre>
   *
   * @param timeoutAfter The {@link Duration} to wait before timing out a pendingResponse (for reporting purposes
   *                     only).
   * @param requestId    The unique identifier of the request that should receive a response, but only once that
   *                     response can be returned.
   *
   * @return A {@link CompletableFuture} that will either complete with a valid instance of {@link T}, or will complete
   *     exceptionally due to a timeout; or will complete exceptionally due to an operational error (e.g., registering
   *     the same
   */
  protected final CompletableFuture<T> registerPendingResponse(
      final long requestId, final String description, final Duration timeoutAfter
  ) {
    logger.debug("RegisterING PendingResponse: `{}` ({})", requestId, description);
    // Not a perfect check if operating under heavy load, but acts as a fail-fast mechanism if something tries to
    // register the same requestId twice, before CompleteableFuture's are engaged. Fail-safe check is below.
    if (pendingResponses.getIfPresent(requestId) != null) {
      throw new RuntimeException(String.format("Attempted to schedule PendingResponse `%s` twice!", requestId));
    }

    // A do-nothing future that never fulfills on its own, allowing another caller to complete
    // it either exceptionally or happily.
    final CompletableFuture<T> joinableResponseFuture = new CompletableFuture<>();

    // A CF that will be scheduled to expire at a certain point in the future. Never completes on its
    // own, but is completed excepitonally below at the appointed time.
    final CompletableFuture<?> timeoutFuture = new CompletableFuture<>();

    // Wrap both `joinableResponseFuture` and `timeoutFuture` and return this to the caller.
    final CompletableFuture<T> returnableFuture = CompletableFuture
        .anyOf(joinableResponseFuture, timeoutFuture)
        .thenApplyAsync((obj) -> {
          if (typeClass.isInstance(obj)) {
            return (T) obj;
          } else if (obj instanceof Throwable) {
            throw new RuntimeException((Throwable) obj);
          } else {
            throw new RuntimeException("Invalid response type returned from anyOf: " + obj);
          }
        });

    if (this.getPendingResponsesMap().get(requestId) != null) {
      throw new RuntimeException(String.format("Attempted to schedule PendingResponse `%s` twice!", requestId));
    } else {

      final PendingResponse<T> pendingResponse = ModifiablePendingResponse.<T>create()
          .setRequestId(requestId)
          .setDescription(description)
          .setJoinableResponseFuture(joinableResponseFuture)
          .setTimeoutFuture(timeoutFuture);

      if (this.getPendingResponsesMap().putIfAbsent(requestId, pendingResponse) == null) {
        final ScheduledFuture<?> scheduledTimeoutFuture = expiryExecutor.schedule(
            () -> timeoutPendingResponse(requestId, timeoutAfter), timeoutAfter.getSeconds(), TimeUnit.SECONDS
        );

        // Update the pendingResponse with a new variant that has the scheduled future...
        ((ModifiablePendingResponse<T>) pendingResponse).setScheduledTimeoutFuture(scheduledTimeoutFuture);

        logger.debug("RegisterED PendingResponse: `{}` ({})", requestId, description);
        // This is the wrapper CF of `joinableResponseFuture` and `timeoutFuture` that the caller actually gets.
        return returnableFuture;
      } else {
        throw new RuntimeException(String.format("Attempted to schedule PendingResponse `%s` twice!", requestId));
      }
    }
  }

  /**
   * Helper to timeout a {@link PendingResponse} and properly cleanup after such an event.
   *
   * @param requestId The unique identifier of a request.
   * @param delayTime The {@link Duration} to wait before timing out a pendingResponse (for reporting purposes only).
   */
  private void timeoutPendingResponse(final long requestId, final Duration delayTime) {

    try {
      // Remove the PendingResponse from this Manager...
      final PendingResponse<T> removedPendingResponse = getPendingResponsesMap().get(requestId);
      if (removedPendingResponse == null) {
        logger.warn("Unable to TIMEOUT response `{}`. This response was likely completed successfully.", requestId);
      } else {
        logger.debug(
            "PendingResponse TIMED_OUT[{}] (after {}s): `{}` [{}]",
            removedPendingResponse.getJoinableResponseFuture().isDone() ? "DONE" : "NOT_DONE",
            delayTime.get(ChronoUnit.SECONDS),
            requestId,
            removedPendingResponse.getDescription()
        );

        // Trigger the Timeout CF...
        try {
          removedPendingResponse.getTimeoutFuture().completeExceptionally(new TimeoutException());
        } catch (Exception e) {
          logger.error("Error while exceptionally completing TimeoutFuture: " + e.getMessage(), e);
        }

        // Preclude the joinableResponse from completing with anything.
        try {
          removedPendingResponse.getJoinableResponseFuture().cancel(true);
        } catch (Exception e) {
          logger.error("Error while cancelling JoinableResponseFuture: " + e.getMessage(), e);
        }
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  private void cancelTimeoutThreads(final PendingResponse<T> pendingResponse) {
    Objects.requireNonNull(pendingResponse);

    // TODO: Only cancel if it's not already cancelled...?
    //if(!pendingResponse.getTimeoutFuture().isCancelled() && !pendingResponse.getTimeoutFuture().isDone()){}
    // In either case, cancel the TimeoutFuture since it's not needed anymore...

    // Prevent the ScheduledExecutor from triggering the actual timeout (because we're preemptively timing-out here.
//    try {
//      pendingResponse.getScheduledTimeoutFuture().cancel(true);
//    } catch (Exception e) {
//      logger.error("Error while attempting to cancel Timeout ScheduledFuture: " + e.getMessage(), e);
//    }

    // Cancel the timeout thread (it's not needed anymore)
    try {
      pendingResponse.getTimeoutFuture().cancel(true);
    } catch (Exception e) {
      logger.error("Error while attempting to cancel Timeout CompleteableFuture: " + e.getMessage(), e);
    }
  }

  /**
   * Join a response from a remote server to a pending response that has been previously returned to a caller who is
   * waiting for it to be completed or to timeout.
   *
   * @param responseToReturn A {@link T} from a remote server that should be used to complete a pending response future.
   *                         Note that this value is never <tt>Optional</tt> because the system either gets a response
   *                         from a remote BTP connection, or else the pending future times-out.
   *
   * @return {@code true} if this invocation caused a pending response to transition to a completed state, else {@code
   *     false}.
   *
   * @throws NoPendingResponseException If there is no pending-response to be joined.
   */
  protected PendingResponse<T> joinPendingResponse(final long requestId, final T responseToReturn)
      throws NoPendingResponseException {
    Objects.requireNonNull(responseToReturn,
        "responseToReturn must not be null in order to correlate to a pending response identifier!");

    // Don't remove here -- instead, just `get` and allow the eviction policy of the underlying cache to govern
    // removal.
    return Optional.ofNullable(getPendingResponsesMap().get(requestId))
        .map(pendingResponse -> {
          // Always connect the `responseToReturn` to a pendingResponse, which has been previously returned to a caller
          // (the caller is waiting for the CF to either be completed or to timeout).
          final boolean successfullyJoined = pendingResponse.getJoinableResponseFuture().complete(responseToReturn);
          if (successfullyJoined) {
            // Only cancel Timeout threads if we can successfully join...
            this.cancelTimeoutThreads(pendingResponse);
            logger
                .debug("PendingResponse joined and completed Successfully! PendingResponse: {}; ResponseToReturn: {}",
                    pendingResponse, responseToReturn);
          } else {
            if (!pendingResponse.getTimeoutFuture().isCompletedExceptionally()) {
              logger
                  .debug("PendingResponse Not Completed AND not Timed Out)! PendingResponse: {}; ResponseToReturn: {}",
                      pendingResponse, responseToReturn);
            }
          }

          return pendingResponse;
        })
        .orElseThrow(() -> new NoPendingResponseException(
            String.format("No PendingResponse available to connect to responseToReturn: %s", responseToReturn),
            requestId
        ));
  }

  // Only used for testing, so fine to return a Map equivalent.
  @VisibleForTesting
  Map<Long, PendingResponse<T>> getPendingResponsesMap() {
    return pendingResponses.asMap();
  }

  // Only used for testing, so fine to return a Map equivalent.
  @VisibleForTesting
  Cache<Long, PendingResponse<T>> getPendingResponses() {
    return pendingResponses;
  }

  @VisibleForTesting
  ScheduledThreadPoolExecutor getExpiryExecutor() {
    return (ScheduledThreadPoolExecutor) expiryExecutor;
  }

  /**
   * Defines how a PendingResponse can correlate to an original request.
   */
  public interface Respondable {

    long getRequestId();
  }

  @Modifiable
  interface PendingResponse<T> extends Respondable {

    @Override
    long getRequestId();

    String getDescription();

    /**
     * The {@link CompletableFuture} that is wrapped via `anyOf`and potentially completed in order to percolate the
     * underlying result to the caller.
     */
    CompletableFuture<T> getJoinableResponseFuture();

    /**
     * The {@link CompletableFuture} that is wrapped via `anyOf` and potentially timed-out in order to percolate this to
     * the caller.
     */
    CompletableFuture<?> getTimeoutFuture();

    /**
     * A future task that will execute {@link #getTimeoutFuture()}.
     */
    Optional<ScheduledFuture<?>> getScheduledTimeoutFuture();

    @Derived
    default boolean isTimedOut() {
      return this.getTimeoutFuture().isCompletedExceptionally();
    }
  }

  /**
   * Thrown when no {@link PendingResponse} is avaialable to be joined.
   */
  public static class NoPendingResponseException extends Exception {

    private final long requestId;

    /**
     * Constructs a new runtime exception with {@code null} as its detail message.  The cause is not initialized, and
     * may subsequently be initialized by a call to {@link #initCause}.
     */
    public NoPendingResponseException(long requestId) {
      this.requestId = requestId;
    }

    /**
     * Constructs a new runtime exception with the specified detail message. The cause is not initialized, and may
     * subsequently be initialized by a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for later retrieval by the {@link #getMessage()}
     *                method.
     */
    public NoPendingResponseException(String message, long requestId) {
      super(message);
      this.requestId = requestId;
    }

    /**
     * Constructs a new runtime exception with the specified detail message and cause.  <p>Note that the detail message
     * associated with {@code cause} is <i>not</i> automatically incorporated in this runtime exception's detail
     * message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method).
     * @param cause   the cause (which is saved for later retrieval by the {@link #getCause()} method).  (A
     *                <tt>null</tt> value is permitted, and indicates that the cause is nonexistent or unknown.)
     *
     * @since 1.4
     */
    public NoPendingResponseException(String message, Throwable cause, long requestId) {
      super(message, cause);
      this.requestId = requestId;
    }

    /**
     * Constructs a new runtime exception with the specified cause and a detail message of <tt>(cause==null ? null :
     * cause.toString())</tt> (which typically contains the class and detail message of
     * <tt>cause</tt>).  This constructor is useful for runtime exceptions
     * that are little more than wrappers for other throwables.
     *
     * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).  (A <tt>null</tt>
     *              value is permitted, and indicates that the cause is nonexistent or unknown.)
     *
     * @since 1.4
     */
    public NoPendingResponseException(Throwable cause, long requestId) {
      super(cause);
      this.requestId = requestId;
    }

    /**
     * Constructs a new runtime exception with the specified detail message, cause, suppression enabled or disabled, and
     * writable stack trace enabled or disabled.
     *
     * @param message            the detail message.
     * @param cause              the cause.  (A {@code null} value is permitted, and indicates that the cause is
     *                           nonexistent or unknown.)
     * @param enableSuppression  whether or not suppression is enabled or disabled
     * @param writableStackTrace whether or not the stack trace should be writable
     *
     * @since 1.7
     */
    public NoPendingResponseException(
        String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, long requestId
    ) {
      super(message, cause, enableSuppression, writableStackTrace);
      this.requestId = requestId;
    }
  }
}


