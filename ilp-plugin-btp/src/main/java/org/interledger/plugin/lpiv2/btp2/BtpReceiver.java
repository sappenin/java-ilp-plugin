package org.interledger.plugin.lpiv2.btp2;

import org.interledger.btp.BtpPacket;
import org.interledger.btp.BtpResponsePacket;
import org.interledger.btp.BtpSession;

import java.util.Optional;

/**
 * Defines the contract that any sender of BTP packets must implement.
 */
public interface BtpReceiver {

  /**
   * Handle an incoming BTP Packet. The payload may be a BTP Request type, or it may be a BTP response type, so
   * implementations must handle all BTP packet types approporiately.
   *
   * @param btpSession        The {@link BtpSession} associated with the incoming packet.
   * @param incomingBtpPacket A {@link BtpPacket} sent from the bilateral BTP peer.
   *
   * @return An optionally-present response of type {@link BtpResponsePacket}.
   */
  Optional<BtpResponsePacket> handleBtpPacket(BtpSession btpSession, BtpPacket incomingBtpPacket);

}
