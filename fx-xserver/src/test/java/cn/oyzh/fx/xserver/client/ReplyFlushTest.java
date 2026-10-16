package cn.oyzh.fx.xserver.client;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A reply must reach the client even when the request that follows it has only partly arrived.
 *
 * <p>This is the shape a real client's traffic takes. Xlib flushes its sixteen kilobyte buffer
 * wherever the buffer fills, so a small round trip followed by a large {@code PutImage} arrives as
 * one reply-worthy request and the head of an image. A server that waits for the whole image before
 * flushing holds a reply the client is waiting for, while the client holds the rest of the image
 * the server is waiting for: neither side ever moves again. The window of a client that does this
 * stops halfway through its first frame and the display stays black, with nothing logged to say
 * why - the server thread is simply parked in a read.
 *
 * <p>Before the flush was unconditional the server only pushed its buffer out when nothing at all
 * was readable, which is exactly the case this test does not present.
 */
class ReplyFlushTest {

    /** A display number nothing else in the suite uses; the server listens on TCP 6000 + it. */
    private static final int DISPLAY = 99;

    private static XServer server;

    @BeforeAll
    static void startServer() throws IOException {
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--backend", "headless", "--no-unix"}));
        server.start();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void replyArrivesWhileTheNextRequestIsStillIncomplete() throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", 6000 + DISPLAY), 5000);
            socket.setSoTimeout(5000);
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            handshake(in, out);

            // A GetInputFocus - which the server answers - followed by the first bytes of a
            // PutImage whose length field promises far more than has been sent. This is the split
            // Xlib's buffer produces, and the reply to the first request has to come out anyway.
            out.write(new byte[]{43, 0, 1, 0});
            out.write(putImageHead());
            out.flush();

            int reply = readReplyType(in);
            assertEquals(1, reply, "expected a GetInputFocus reply, not an event or an error");
        }
    }

    /** The first sixteen bytes of a PutImage: header, and no image data at all. */
    private static byte[] putImageHead() {
        byte[] head = new byte[16];
        head[0] = 72;                                   // opcode: PutImage
        head[1] = 2;                                    // format: ZPixmap
        head[2] = (byte) 0xFE;                          // length, low byte   } 65006 units, the
        head[3] = (byte) 0xFD;                          // length, high byte  } size a 1300x50 band is
        head[7] = 0x40;                                 // drawable
        head[11] = 0x20;                                // gc
        head[12] = (byte) 0x14;                         // width, low byte: 1300
        head[13] = (byte) 0x05;                         // width, high byte
        head[14] = 50;                                  // height
        return head;
    }

    /**
     * Reads one reply and returns its type byte, which is zero for every reply.
     *
     * <p>Everything here is read a byte at a time in the connection's own order. A
     * {@link DataInputStream} would be the obvious tool and is the wrong one: it reads multi byte
     * fields most significant byte first, which for this little endian connection turns a reply
     * length of 82 into 20992.
     */
    private static int readReplyType(InputStream in) throws IOException {
        byte[] header = readFully(in, 8);
        int length = card32(header, 4);
        readFully(in, length * 4L);
        return header[0] & 0xFF;
    }

    /**
     * Sends a minimal setup request and consumes the setup reply. Only its status matters here, but
     * the whole of it has to come off the socket before the connection is usable.
     */
    private static void handshake(InputStream in, OutputStream out) throws IOException {
        byte[] setup = new byte[12];
        setup[0] = 'l';                                 // little endian client
        setup[2] = 11;                                  // protocol major
        out.write(setup);
        out.flush();

        byte[] header = readFully(in, 8);
        assertEquals(1, header[0], "the connection setup was refused");
        readFully(in, card16(header, 6) * 4L);
        assertTrue(true);
    }

    private static byte[] readFully(InputStream in, long count) throws IOException {
        byte[] bytes = new byte[(int) count];
        int read = 0;
        while (read < bytes.length) {
            int n = in.read(bytes, read, bytes.length - read);
            if (n < 0) {
                throw new IOException("the server closed the connection after " + read + " bytes");
            }
            read += n;
        }
        return bytes;
    }

    private static int card16(byte[] bytes, int offset) {
        return (bytes[offset] & 0xFF) | ((bytes[offset + 1] & 0xFF) << 8);
    }

    private static int card32(byte[] bytes, int offset) {
        return card16(bytes, offset) | (card16(bytes, offset + 2) << 16);
    }
}
