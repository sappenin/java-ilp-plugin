package org.interledger.plugin.lpiv2.btp2;

import org.interledger.btp.BtpError;
import org.interledger.btp.BtpPacket;
import org.interledger.btp.BtpResponse;
import org.interledger.core.InterledgerResponsePacket;

import java.util.Objects;

/**
 * A helper class for mapping polymorphic BTP responses to their proper type.
 */
public abstract class BtpResponsePacketMapper<T> {

  /**
   * Handle the supplied {@code btpResponse} in a type-safe manner.
   *
   * @param btpResponse The generic {@link InterledgerResponsePacket} to be mapped in a type-safe manner.
   */
  public final T map(final BtpPacket btpResponse) {
    Objects.requireNonNull(btpResponse);

    if (BtpError.class.isAssignableFrom(btpResponse.getClass())) {
      return handleBtpError((BtpError) btpResponse);
    } else if (BtpResponse.class.isAssignableFrom(btpResponse.getClass())) {
      return handleBtpResponse((BtpResponse) btpResponse);
    } else {
      throw new RuntimeException(String.format("Unsupported BtpResponse Type: %s", btpResponse.getClass()));
    }

  }

  /**
   * Handle the packet as an {@link BtpPacket}.
   *
   * @param btpError A {@link BtpError}.
   */
  protected abstract T handleBtpError(final BtpError btpError);

  /**
   * Handle the packet as an {@link BtpPacket}.
   *
   * @param btpResponse A {@link BtpResponse}.
   */
  protected abstract T handleBtpResponse(final BtpResponse btpResponse);

}
