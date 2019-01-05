package org.interledger.plugin.lpiv2.btp2.spring;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
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

  // TODO: Use WeakHashMap? Guava Cache so that these are removed after a certain amount of time?
  private final Map<Long, PendingResponse<T>> pendingResponses;

  // Used for running all expiry threads to cancel pendingResponses.
  private final ScheduledExecutorService expiryExecutor;

  // The type of response expected to be returned by this Manager.
  private Class<T> typeClass;

  /**
   * No-args Constructor.
   */
  public PendingResponseManager(final Class<T> typeClass) {
    this(typeClass, 10);
  }

  /**
   * @param corePoolSize the number of threads to keep in the expiry thread pool, even if they are idle.
   */
  public PendingResponseManager(final Class<T> typeClass, final int corePoolSize) {
    this.pendingResponses = Maps.newConcurrentMap();
    this.typeClass = Objects.requireNonNull(typeClass);

    // Used to timeout blocking futures...
    this.expiryExecutor = new ScheduledThreadPoolExecutor(corePoolSize);
    ((ScheduledThreadPoolExecutor) this.expiryExecutor).setRemoveOnCancelPolicy(true);
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
   * @param delay      the time from now to delay execution
   * @param delayUnits The {@link TimeUnit} to denominate {@code delay}.
   * @param requestId  The unique identifier of the request that should receive a response, but only once that response
   *                   can be returned.
   *
   * @return A {@link CompletableFuture} that will either complete with a valid instance of {@link T}, or will complete
   *     exceptionally due to a timeout; or will complete exceptionally due to an operational error (e.g., registering
   *     the same
   */
  protected final CompletableFuture<T> registerPendingResponse(
      final long requestId, final long delay, final TimeUnit delayUnits
  ) {

    // Not a perfect check if operating under heavy load, but acts as a fail-fast mechanism if something tries to
    // register the same requestId twice, before CompleteableFuture's are engaged. Fail-safe check is below.
    if (pendingResponses.containsKey(requestId)) {
      throw new RuntimeException(String.format("Attempted to schedule PendingResponse `%s` twice!", requestId));
    }

    // joinableResponseFuture: A do-nothing future that never fulfills on its own, allowing another caller to complete
    // it either exceptionally or happily.

    // timeoutFuture: A CF that will be scheduled to expire at a certain point in the future. Never completes on its
    // own, but is completed excepitonally below at the appointed time.

    // Wrap both CF's and return to the caller...
    final PendingResponse<T> pendingResponse = PendingResponse.of(
        requestId, new CompletableFuture<>(), new CompletableFuture<>()
    );

    // Wrap both `joinableResponseFuture` and `timeoutFuture` and return this to the caller.
    final CompletableFuture<T> returnableFuture = CompletableFuture
        .anyOf(pendingResponse.getJoinableResponseFuture(), pendingResponse.getTimeoutFuture())
        .thenApplyAsync((obj) -> {
          if (typeClass.isInstance(obj)) {
            return (T) obj;
          } else if (obj instanceof Throwable) {
            throw new RuntimeException((Throwable) obj);
          } else {
            throw new RuntimeException("Invalid response type returned from anyOf: " + obj);
          }
        });

    if (this.pendingResponses.putIfAbsent(requestId, pendingResponse) == null) {
      expiryExecutor.schedule(() -> timeoutPendingResponse(requestId), delay, delayUnits);

      // This is the wrapper CF of `joinableResponseFuture` and `timeoutFuture` that the caller actually gets.
      return returnableFuture;
    } else {
      throw new RuntimeException(String.format("Attempted to schedule PendingResponse `%s` twice!", requestId));
    }
  }

  /**
   * Helper to timeout a {@link PendingResponse} and properly cleanup after such an event.
   *
   * @param requestId
   */
  private void timeoutPendingResponse(final long requestId) {
    // Remove the PendingResponse from this Manager...
    final PendingResponse<T> removedPendingResponse = pendingResponses.remove(requestId);
    // Trigger the Timeout CF...
    removedPendingResponse.getTimeoutFuture().completeExceptionally(new TimeoutException());
    removedPendingResponse.getJoinableResponseFuture().cancel(true);
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
   * @throws RuntimeException If there is no pending-response to be joined.
   */
  protected PendingResponse<T> joinPendingResponse(final long requestId, final T responseToReturn) {
    Objects.requireNonNull(responseToReturn,
        "responseToReturn must not be null in order to correlate to a pending response identifier!");

    return Optional.ofNullable(pendingResponses.remove(requestId))
        .map(pendingResponse -> {
          // Always connect the `responseToReturn` to a pendingResponse, which has been previously returned to a caller
          // (the caller is waiting for the CF to either be completed or to timeout).
          final boolean successfullyJoined = pendingResponse.getJoinableResponseFuture().complete(responseToReturn);
          if (successfullyJoined) {
            logger
                .debug("PendingResponse joined and completed Successfully! PendingResponse: {}; ResponseToReturn: {}",
                    pendingResponse, responseToReturn);
          } else {
            logger.error("PendingResponse Not Completed! PendingResponse: {}; ResponseToReturn: {}", pendingResponse,
                responseToReturn);
          }

          // In either case, cancel the TimeoutFuture since it's not needed anymore...
          pendingResponse.getTimeoutFuture().cancel(true);

          return pendingResponse;
        })
        .orElseThrow(() -> new NoPendingResponseException(
            String.format("No PendingResponse available to connect to responseToReturn: %s", responseToReturn),
            requestId
        ));
  }

  @VisibleForTesting
  Map<Long, PendingResponse<T>> getPendingResponses() {
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

  /**
   * Holds a {@link CompletableFuture} and additional meta-data about a pending response, such as the request
   * correlation identifier for connecting a response to an original request.
   *
   * @param <T>
   */
  public static class PendingResponse<T> implements Respondable {

    private final CompletableFuture<T> joinableResponseFuture;
    private final CompletableFuture<?> timeoutFuture;

    private long requestId;

    /**
     * Required-args Constructor.
     *
     * @param requestId              A request identifier to correlate a response back to an original request.
     * @param joinableResponseFuture A {@link CompletableFuture} that will eventually be completed with a valid
     *                               response, so long as the {@code timeoutFuture} doesn't complete first.
     * @param timeoutFuture          A {@link CompletableFuture} that will eventually be completed exceptionally with a
     *                               {@link TimeoutException}, so long as the {@code joinableResponseFuture} doesn't
     *                               complete first.
     */
    public PendingResponse(
        final long requestId, final CompletableFuture<T> joinableResponseFuture,
        final CompletableFuture<?> timeoutFuture
    ) {
      this.requestId = requestId;
      this.joinableResponseFuture = Objects.requireNonNull(joinableResponseFuture);
      this.timeoutFuture = Objects.requireNonNull(timeoutFuture);
    }

    public static <T> PendingResponse of(
        final long requestId, final CompletableFuture<T> joinableResponseFuture,
        final CompletableFuture<?> timeoutFuture
    ) {
      return new PendingResponse<>(requestId, joinableResponseFuture, timeoutFuture);
    }

    @Override
    public long getRequestId() {
      return requestId;
    }

    public CompletableFuture<T> getJoinableResponseFuture() {
      return joinableResponseFuture;
    }

    public CompletableFuture<?> getTimeoutFuture() {
      return timeoutFuture;
    }

    public boolean isTimedOut() {
      return this.timeoutFuture.isCompletedExceptionally();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      PendingResponse<?> that = (PendingResponse<?>) o;

      if (requestId != that.requestId) {
        return false;
      }
      if (!joinableResponseFuture.equals(that.joinableResponseFuture)) {
        return false;
      }
      return timeoutFuture.equals(that.timeoutFuture);
    }

    @Override
    public int hashCode() {
      int result = joinableResponseFuture.hashCode();
      result = 31 * result + timeoutFuture.hashCode();
      result = 31 * result + (int) (requestId ^ (requestId >>> 32));
      return result;
    }

    @Override
    public String toString() {
      return new StringJoiner(", ", PendingResponse.class.getSimpleName() + "[", "]")
          .add("joinableResponseFuture=" + joinableResponseFuture)
          .add("timeoutFuture=" + timeoutFuture)
          .add("requestId=" + requestId)
          .toString();
    }
  }

  /**
   * Thrown when no {@link PendingResponse} is avaialable to be joined.
   */
  public static class NoPendingResponseException extends RuntimeException {

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
