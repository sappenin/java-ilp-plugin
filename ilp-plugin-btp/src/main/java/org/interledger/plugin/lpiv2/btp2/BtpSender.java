package org.interledger.plugin.lpiv2.btp2;

import org.interledger.btp.BtpMessage;
import org.interledger.btp.BtpResponsePacket;
import org.interledger.btp.BtpTransfer;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

/**
 * Defines the contract that any sender of BTP packets must implement.
 */
public interface BtpSender {

  // Can either be a BtpResponse or a BtpError...
  CompletableFuture<BtpResponsePacket> sendBtpMessage(BtpMessage btpMessage, final Duration waitTime);

  // Can either be a BtpResponse or a BtpError...
  CompletableFuture<BtpResponsePacket> sendBtpTransfer(BtpTransfer btpTransfer, final Duration waitTime);
}
