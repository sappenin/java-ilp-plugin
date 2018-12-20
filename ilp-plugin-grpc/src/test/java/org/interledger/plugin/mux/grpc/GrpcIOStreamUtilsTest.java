package org.interledger.plugin.mux.grpc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.interledger.TestArtifacts.CODEC_CONTEXT;
import static org.interledger.TestArtifacts.FULFILL_PACKET;
import static org.interledger.TestArtifacts.PREPARE_PACKET;
import static org.interledger.TestArtifacts.REJECT_PACKET;

import org.interledger.plugin.mux.grpc.GrpcIOStreamUtils;
import org.interledger.core.InterledgerPacket;

import com.google.protobuf.ByteString;
import org.junit.Test;

import java.io.IOException;

/**
 * Unit tests for {@link GrpcIOStreamUtils}.
 *
 * This class also includes some timing tests that
 */
public class GrpcIOStreamUtilsTest {

  @Test
  public void preparePacketToByteString() throws IOException {

    // PREPARE PACKET
    {
      final ByteString result = GrpcIOStreamUtils.toByteString(CODEC_CONTEXT, PREPARE_PACKET);
      final InterledgerPacket actualPacket = GrpcIOStreamUtils.fromByteString(CODEC_CONTEXT, result);
      assertThat(actualPacket, is(PREPARE_PACKET));
    }
    {
      // FULFILL PACKET
      final ByteString result = GrpcIOStreamUtils.toByteString(CODEC_CONTEXT, FULFILL_PACKET);
      final InterledgerPacket actualPacket = GrpcIOStreamUtils.fromByteString(CODEC_CONTEXT, result);
      assertThat(actualPacket, is(FULFILL_PACKET));
    }
    {
      // REJECT PACKET
      final ByteString result = GrpcIOStreamUtils.toByteString(CODEC_CONTEXT, REJECT_PACKET);
      final InterledgerPacket actualPacket = GrpcIOStreamUtils.fromByteString(CODEC_CONTEXT, result);
      assertThat(actualPacket, is(REJECT_PACKET));
    }
  }
}