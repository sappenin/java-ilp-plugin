package org.interledger.plugin.lpiv2.support;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * @see "https://geek.co.il/2018/03/26/javas-completablefuture-and-typed-exception-handling"
 * @see "https://gist.github.com/guss77/b8623ab7586f154895aa33248872ae21"
 * @deprecated Remove if not used.
 */
@Deprecated
public class Futures {

  public static <T, E extends Throwable> Function<Throwable, ? extends T> on(Class<E> errType,
      Function<E, ? extends T> fn) {
    return t -> {
      if (!errType.isInstance(t)) {
        Thrower.throwIt(t);
      }
      @SuppressWarnings("unchecked") E e = (E) t;
      return fn.apply(e);
    };
  }

  public static <T> CompletableFuture<T> failedFuture(Throwable thr) {
    CompletableFuture<T> f = new CompletableFuture<T>();
    f.completeExceptionally(thr);
    return f;
  }

  public static <T> CompletableFuture<T> successfulFuture(T value) {
    CompletableFuture<T> f = new CompletableFuture<T>();
    f.complete(value);
    return f;
  }

  static class Thrower {

    static Throwable except;

    Thrower() throws Throwable {
      throw except;
    }

    public static void throwIt(Throwable t) {
      except = t;
      try {
        Thrower.class.newInstance();
      } catch (InstantiationException | IllegalAccessException e) {
      }
    }
  }

}