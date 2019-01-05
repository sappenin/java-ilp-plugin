package org.interledger.plugin.lpiv2.bpp.grpc.connection.mux;


import static org.interledger.TestArtifacts.CODEC_CONTEXT;
import static org.interledger.TestArtifacts.FULFILL_PACKET_RND;
import static org.interledger.TestArtifacts.PREPARE_PACKET_RND;
import static org.interledger.TestArtifacts.REJECT_PACKET_RND;

import org.interledger.core.InterledgerPacket;
import org.interledger.plugin.lpiv2.bpp.grpc.connection.mux.GrpcIOStreamUtils;

import com.google.protobuf.ByteString;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Unit tests for {@link GrpcIOStreamUtils} that test the timing to go from a Byte-representation that can be read by
 * gRPC vs a representation that can be read by BTP (via CodecContext).
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GrpcIOStreamUtilsTimingTest {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final int NUM_REPS = 10000;

  /**
   * Warmup the JVM and CPU caches....
   */
  @Test
  public void aWarmup() throws IOException {
    {
      final long startMillis = System.currentTimeMillis();
      for (int i = 0; i < NUM_REPS; i++) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        CODEC_CONTEXT.write(PREPARE_PACKET_RND(), out);
      }
      final long endMillis = System.currentTimeMillis();
      logger.info("{} Reps of WARMUP1: {}ms", NUM_REPS, (endMillis - startMillis));
    }
    {
      final long startMillis = System.currentTimeMillis();
      for (int i = 0; i < NUM_REPS; i++) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        CODEC_CONTEXT.write(PREPARE_PACKET_RND(), out);
      }
      final long endMillis = System.currentTimeMillis();
      logger.info("{} Reps of WARMUP2: {}ms", NUM_REPS, (endMillis - startMillis));
    }
  }

  @Test
  public void timingTest_2toByteString() {
    {
      final long startMillis = System.currentTimeMillis();
      for (int i = 0; i < NUM_REPS; i++) {
        GrpcIOStreamUtils.toByteString(CODEC_CONTEXT, PREPARE_PACKET_RND());
      }
      final long endMillis = System.currentTimeMillis();
      logger.info("{} Reps of toByteString (PREPARE_PACKET): {}ms", NUM_REPS, (endMillis - startMillis));
    }
    {
      final long startMillis = System.currentTimeMillis();
      for (int i = 0; i < NUM_REPS; i++) {
        GrpcIOStreamUtils.toByteString(CODEC_CONTEXT, REJECT_PACKET_RND());
      }
      final long endMillis = System.currentTimeMillis();
      logger.info("{} Reps of toByteString (REJECT_PACKET): {}ms", NUM_REPS, (endMillis - startMillis));
    }
    {
      final long startMillis = System.currentTimeMillis();
      for (int i = 0; i < NUM_REPS; i++) {
        GrpcIOStreamUtils.toByteString(CODEC_CONTEXT, FULFILL_PACKET_RND());
      }
      final long endMillis = System.currentTimeMillis();
      logger.info("{} Reps of toByteString (FULFILL_PACKET): {}ms", NUM_REPS, (endMillis - startMillis));
    }
  }

  @Test
  public void timingTest_4fromByteString() {
    {
      final ByteString byteString = GrpcIOStreamUtils.toByteString(CODEC_CONTEXT, PREPARE_PACKET_RND());
      final long startMillis = System.currentTimeMillis();
      for (int i = 0; i < NUM_REPS; i++) {
        GrpcIOStreamUtils.fromByteString(CODEC_CONTEXT, byteString);
      }
      final long endMillis = System.currentTimeMillis();
      logger.info("{} Reps of fromByteString (PREPARE_PACKET): {}ms", NUM_REPS, (endMillis - startMillis));
    }
    {
      final ByteString byteString = GrpcIOStreamUtils.toByteString(CODEC_CONTEXT, REJECT_PACKET_RND());
      final long startMillis = System.currentTimeMillis();
      for (int i = 0; i < NUM_REPS; i++) {
        GrpcIOStreamUtils.fromByteString(CODEC_CONTEXT, byteString);
      }
      final long endMillis = System.currentTimeMillis();
      logger.info("{} Reps of fromByteString (REJECT_PACKET): {}ms", NUM_REPS, (endMillis - startMillis));
    }
    {
      final ByteString byteString = GrpcIOStreamUtils.toByteString(CODEC_CONTEXT, FULFILL_PACKET_RND());
      final long startMillis = System.currentTimeMillis();
      for (int i = 0; i < NUM_REPS; i++) {
        GrpcIOStreamUtils.fromByteString(CODEC_CONTEXT, byteString);
      }
      final long endMillis = System.currentTimeMillis();
      logger.info("{} Reps of fromByteString (FULFILL_PACKET): {}ms", NUM_REPS, (endMillis - startMillis));
    }
  }

  @Test
  public void timingTest_3codecRead() throws IOException {
    {
      final ByteArrayOutputStream out = new ByteArrayOutputStream();
      CODEC_CONTEXT.write(PREPARE_PACKET_RND(), out);
      final long startMillis = System.currentTimeMillis();
      for (int i = 0; i < NUM_REPS; i++) {
        CODEC_CONTEXT.read(InterledgerPacket.class, new ByteArrayInputStream(out.toByteArray()));
      }
      final long endMillis = System.currentTimeMillis();
      logger.info("{} Reps of CODEC_CONTEXT.read(PREPARE_PACKET): {}ms", NUM_REPS, (endMillis - startMillis));
    }
    {
      final ByteArrayOutputStream out = new ByteArrayOutputStream();
      CODEC_CONTEXT.write(REJECT_PACKET_RND(), out);
      final long startMillis = System.currentTimeMillis();
      for (int i = 0; i < NUM_REPS; i++) {
        CODEC_CONTEXT.read(InterledgerPacket.class, new ByteArrayInputStream(out.toByteArray()));
      }
      final long endMillis = System.currentTimeMillis();
      logger.info("{} Reps of CODEC_CONTEXT.read(REJECT_PACKET): {}ms", NUM_REPS, (endMillis - startMillis));
    }
    {
      final ByteArrayOutputStream out = new ByteArrayOutputStream();
      CODEC_CONTEXT.write(FULFILL_PACKET_RND(), out);
      final long startMillis = System.currentTimeMillis();
      for (int i = 0; i < NUM_REPS; i++) {
        CODEC_CONTEXT.read(InterledgerPacket.class, new ByteArrayInputStream(out.toByteArray()));
      }
      final long endMillis = System.currentTimeMillis();
      logger.info("{} Reps of CODEC_CONTEXT.read(FULFILL_PACKET): {}ms", NUM_REPS, (endMillis - startMillis));
    }
  }

  @Test
  public void timingTest_1codecWrite() throws IOException {
    {
      final long startMillis = System.currentTimeMillis();
      for (int i = 0; i < NUM_REPS; i++) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        CODEC_CONTEXT.write(PREPARE_PACKET_RND(), out);
      }
      final long endMillis = System.currentTimeMillis();
      logger.info("{} Reps of CODEC_CONTEXT.write(PREPARE_PACKET): {}ms", NUM_REPS, (endMillis - startMillis));
    }
    {
      final long startMillis = System.currentTimeMillis();
      for (int i = 0; i < NUM_REPS; i++) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        CODEC_CONTEXT.write(REJECT_PACKET_RND(), out);
      }
      final long endMillis = System.currentTimeMillis();
      logger.info("{} Reps of CODEC_CONTEXT.write(REJECT_PACKET): {}ms", NUM_REPS, (endMillis - startMillis));
    }
    {
      final long startMillis = System.currentTimeMillis();
      for (int i = 0; i < NUM_REPS; i++) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        CODEC_CONTEXT.write(FULFILL_PACKET_RND(), out);
      }
      final long endMillis = System.currentTimeMillis();
      logger.info("{} Reps of CODEC_CONTEXT.write(FULFILL_PACKET): {}ms", NUM_REPS, (endMillis - startMillis));
    }
  }
}