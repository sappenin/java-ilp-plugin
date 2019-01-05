package org.interledger.plugin.lpiv2.exceptions;

import org.interledger.core.InterledgerAddress;
import org.interledger.plugin.MoneyHandler;
import org.interledger.plugin.lpiv2.Plugin;

/**
 * Thrown if a caller attempts to register a {@link MoneyHandler } in a {@link Plugin} where one is already
 * registered.
 */
public class MoneyHandlerAlreadyRegisteredException extends PluginException {

  /**
   * Constructs a new runtime exception with {@code null} as its detail message.  The cause is not initialized, and may
   * subsequently be initialized by a call to {@link #initCause}.
   */
  public MoneyHandlerAlreadyRegisteredException(InterledgerAddress pluginAccount) {
    super(pluginAccount);
  }

  /**
   * Constructs a new runtime exception with the specified detail message. The cause is not initialized, and may
   * subsequently be initialized by a call to {@link #initCause}.
   *
   * @param message the detail message. The detail message is saved for later retrieval by the {@link #getMessage()}
   *                method.
   */
  public MoneyHandlerAlreadyRegisteredException(String message, InterledgerAddress pluginAccount) {
    super(message, pluginAccount);
  }

  /**
   * Constructs a new runtime exception with the specified detail message and cause.  <p>Note that the detail message
   * associated with {@code cause} is <i>not</i> automatically incorporated in this runtime exception's detail message.
   *
   * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method).
   * @param cause   the cause (which is saved for later retrieval by the {@link #getCause()} method).  (A <tt>null</tt>
   *                value is permitted, and indicates that the cause is nonexistent or unknown.)
   *
   * @since 1.4
   */
  public MoneyHandlerAlreadyRegisteredException(String message, Throwable cause,
      InterledgerAddress pluginAccount) {
    super(message, cause, pluginAccount);
  }

  /**
   * Constructs a new runtime exception with the specified cause and a detail message of
   * <tt>(cause==null ? null : cause.toString())</tt> (which typically contains the class and detail
   * message of <tt>cause</tt>).  This constructor is useful for runtime exceptions that are little more than wrappers
   * for other throwables.
   *
   * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method). (A <tt>null</tt>
   *              value is permitted, and indicates that the cause is nonexistent or unknown.)
   *
   * @since 1.4
   */
  public MoneyHandlerAlreadyRegisteredException(Throwable cause,
      InterledgerAddress pluginAccount) {
    super(cause, pluginAccount);
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
  public MoneyHandlerAlreadyRegisteredException(String message, Throwable cause,
      boolean enableSuppression,
      boolean writableStackTrace,
      InterledgerAddress pluginAccount) {
    super(message, cause, enableSuppression, writableStackTrace, pluginAccount);
  }
}
