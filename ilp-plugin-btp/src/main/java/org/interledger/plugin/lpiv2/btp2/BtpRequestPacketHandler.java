package org.interledger.plugin.lpiv2.btp2;

import org.interledger.btp.BtpError;
import org.interledger.btp.BtpMessage;
import org.interledger.btp.BtpPacket;
import org.interledger.btp.BtpTransfer;
import org.interledger.core.InterledgerResponsePacket;

import java.util.Objects;

/**
 * A helper class for mapping polymorphic BTP request packets to their proper type.
 */
// TODO: Remove if unused.
public abstract class BtpRequestPacketHandler {

  /**
   * Handle the supplied {@code btpRequest} in a type-safe manner.
   *
   * @param btpRequest The generic {@link InterledgerResponsePacket} to be mapped in a type-safe manner.
   */
  public final void handle(final BtpPacket btpRequest) {
    Objects.requireNonNull(btpRequest);

    if (BtpMessage.class.isAssignableFrom(btpRequest.getClass())) {
      handleBtpMessage((BtpMessage) btpRequest);
    } else if (BtpTransfer.class.isAssignableFrom(btpRequest.getClass())) {
      handleBtpTransfer((BtpTransfer) btpRequest);
    } else {
      throw new RuntimeException(String.format("Unsupported BtpRequest Type: %s", btpRequest.getClass()));
    }

  }

  /**
   * Handle the packet as an {@link BtpPacket}.
   *
   * @param btpMessage A {@link BtpError}.
   */
  protected abstract void handleBtpMessage(final BtpMessage btpMessage);

  /**
   * Handle the packet as an {@link BtpPacket}.
   *
   * @param btpTransfer A {@link BtpTransfer}.
   */
  protected abstract void handleBtpTransfer(final BtpTransfer btpTransfer);

}
