package org.interledger.plugin.lpiv2.btp2.subprotocols.auth;

import org.interledger.btp.BtpErrorCode;
import org.interledger.btp.BtpRuntimeException;

public class BtpAuthenticationException extends BtpRuntimeException {

  /**
   * Constructs a new runtime exception with {@code null} as its detail message.  The cause is not initialized, and may
   * subsequently be initialized by a call to {@link #initCause}.
   */
  public BtpAuthenticationException() {
    this("Invalid BTP authentication credentials");
  }

  /**
   * Constructs a new runtime exception with the specified detail message. The cause is not initialized, and may
   * subsequently be initialized by a call to {@link #initCause}.
   *
   * @param message the detail message. The detail message is saved for later retrieval by the {@link #getMessage()}
   *                method.
   */
  public BtpAuthenticationException(String message) {
    super(BtpErrorCode.F00_NotAcceptedError, message);
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
  public BtpAuthenticationException(String message, Throwable cause) {
    super(BtpErrorCode.F00_NotAcceptedError, message, cause);
  }

}