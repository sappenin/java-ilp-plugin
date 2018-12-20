package org.interledger.plugin.mux.grpc;

import org.interledger.core.InterledgerPacket;
import org.interledger.encoding.asn.framework.CodecContext;

import com.google.protobuf.ByteString;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Utilities for operating with gRPC binary payloads and I/O streams.
 */
public class GrpcIOStreamUtils {

  public static ByteString toByteString(final CodecContext ilpCodecContext, final InterledgerPacket packet) {
    try {
      return toByteStringSafe(ilpCodecContext, packet);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  public static ByteString toByteStringSafe(
      final CodecContext ilpCodecContext, final InterledgerPacket packet
  ) throws IOException {
    Objects.requireNonNull(packet);

    // One way to do this completely in-memory is to use a PipedInputStream/PipedOutputStream. However, this is meant
    // for usage by multiple threads, and using these in a single-thread is not advised per the Javadoc due to potential
    // deadlock. Plus, ILP packets shouldn't be very big, so using NIO and a buffer should still be performant.

    // Writes to a buffer newly created by ByteArrayOutputStream
    final ByteArrayOutputStream expectedBytesOutputStream = new ByteArrayOutputStream();
    ilpCodecContext.write(packet, expectedBytesOutputStream);

    // Copies the buffer when calling `toByteArray`, but passes the reference to ByteArrayInputStream.
    final ByteArrayInputStream inputStream = new ByteArrayInputStream(expectedBytesOutputStream.toByteArray());

    // Copies the buffer again
    return ByteString.readFrom(inputStream);
  }

  /**
   * TODO
   *
   * @param ilpCodecContext
   * @param byteString
   *
   * @return
   */
  public static InterledgerPacket fromByteString(
      final CodecContext ilpCodecContext, final ByteString byteString
  ) {
    Objects.requireNonNull(byteString);

    try {
      return fromByteStringSafe(ilpCodecContext, byteString);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * TODO
   *
   * @param ilpCodecContext
   * @param byteString
   *
   * @return
   */
  public static InterledgerPacket fromByteStringSafe(
      final CodecContext ilpCodecContext, final ByteString byteString
  ) throws IOException {
    Objects.requireNonNull(byteString);

    return ilpCodecContext.read(
        InterledgerPacket.class,
        // byteString.asReadOnlyByteBuffer() provides a view to the underlying byte[] without copying the bytes...
        new ByteBufferBackedInputStream(byteString.asReadOnlyByteBuffer())
    );
  }

  // This implementation is 2x slower than fromByteString due to an extra copy of the byte-buffer.
//  public static InterledgerPacket fromByteStringWithBuffer(
//      final CodecContext ilpCodecContext, final ByteString byteString
//  ) throws IOException {
//    Objects.requireNonNull(byteString);
//
//    // toByteArray performs 1 copy, but ByteArrayInputStream just stored a reference.
//    ByteArrayInputStream buf = new ByteArrayInputStream(byteString.toByteArray());
//
//    return ilpCodecContext.read(InterledgerPacket.class, buf);
//  }

  /**
   * An inputstream that wraps a {@link ByteBuffer}, which is provided by gRPC. This implementation avoids a second
   * array copy of bytes to improve packet (de)serialization performance between gRPC, Java, and OER.
   */
  public static class ByteBufferBackedInputStream extends InputStream {

    ByteBuffer buf;

    public ByteBufferBackedInputStream(final ByteBuffer buf) {
      this.buf = Objects.requireNonNull(buf);
    }

    public int read() {
      if (!buf.hasRemaining()) {
        return -1;
      }
      return buf.get() & 0xFF;
    }

    public int read(byte[] bytes, int off, int len) {
      if (!buf.hasRemaining()) {
        return -1;
      }

      len = Math.min(len, buf.remaining());
      buf.get(bytes, off, len);
      return len;
    }
  }
}
