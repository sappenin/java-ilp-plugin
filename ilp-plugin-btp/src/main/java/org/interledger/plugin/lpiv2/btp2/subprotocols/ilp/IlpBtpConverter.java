package org.interledger.plugin.lpiv2.btp2.subprotocols.ilp;

import org.interledger.btp.BtpResponse;
import org.interledger.btp.BtpSubProtocol;
import org.interledger.btp.BtpSubProtocols;
import org.interledger.core.InterledgerPacket;
import org.interledger.core.InterledgerResponsePacket;
import org.interledger.encoding.asn.framework.CodecContext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Objects;

/**
 * A central place to convert between ILP and BTP packets.
 */
public class IlpBtpConverter {

  /**
   * Construct an {@link InterledgerPacket} using data in the supplied BTP packet.
   *
   * @param btpResponse     A {@link BtpResponse} containing fulfill or reject packet.
   * @param ilpCodecContext A {@link CodecContext} that can read ILP Packets.
   *
   * @return An newly constructed {@link InterledgerPacket}.
   */
  public static InterledgerResponsePacket toIlpPacket(
      BtpResponse btpResponse, final CodecContext ilpCodecContext
  ) {
    Objects.requireNonNull(btpResponse);
    Objects.requireNonNull(ilpCodecContext);

    try {
      final ByteArrayInputStream inputStream =
          new ByteArrayInputStream(btpResponse.getPrimarySubProtocol().getData());
      return ilpCodecContext.read(InterledgerResponsePacket.class, inputStream);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Construct a {@link BtpSubProtocol} instance for using the <tt>ILP</tt> packet data.
   *
   * @param ilpPacket       An {@link InterledgerPacket} that can be a prepare, fulfill, or error packet.
   * @param ilpCodecContext A {@link CodecContext} that can operate on ILP primitives.
   *
   * @return A {@link BtpSubProtocol}
   */
  public static BtpSubProtocol toBtpSubprotocol(
      final InterledgerPacket ilpPacket, final CodecContext ilpCodecContext
  ) {
    Objects.requireNonNull(ilpPacket);
    Objects.requireNonNull(ilpCodecContext);
    try {
      final ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ilpCodecContext.write(ilpPacket, baos);

      return BtpSubProtocol.builder()
          .protocolName(BtpSubProtocols.INTERLEDGER)
          .contentType(BtpSubProtocol.ContentType.MIME_APPLICATION_OCTET_STREAM)
          .data(baos.toByteArray())
          .build();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


}
