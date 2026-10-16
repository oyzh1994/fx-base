package cn.oyzh.fx.xserver.net;

import cn.oyzh.fx.xserver.net.WebSocketListener;
import cn.oyzh.fx.xserver.test.TinyWebSocket;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The WebSocket a browser connects to (backlog D2's browser path).
 *
 * <p>The parts worth pinning are the ones a client would notice and a server cannot fake: the
 * handshake's {@code Sec-WebSocket-Accept} (a browser closes the page if it is wrong), masked frames
 * of every length encoding, a fragmented message arriving whole, a ping answered, and - the part that
 * makes the two protocols fit together at all - a *byte stream* going in and coming out with the
 * message boundaries forgotten.
 */
class WebSocketTest {

    private static WebSocketListener listener;
    private static final BlockingQueue<String> received = new LinkedBlockingQueue<>();

    /** Everything the server has read so far, once at least {@code size} bytes of it have arrived. */
    private static String awaitText(int size) throws InterruptedException {
        StringBuilder text = new StringBuilder();
        long deadline = System.currentTimeMillis() + 5000;
        while (text.length() < size && System.currentTimeMillis() < deadline) {
            String piece = received.poll(1, TimeUnit.SECONDS);
            if (piece != null) {
                text.append(piece);
            }
        }
        return text.toString();
    }

    /** Just the byte count, for the sizes whose text is not worth comparing twice. */
    private static int awaitBytes(int size) throws InterruptedException {
        return awaitText(size).length();
    }

    @BeforeAll
    static void startListener() throws IOException {
        // Echo, plus a line in a queue per message: the test asserts on what the server *read*, which
        // is the half a client cannot see.
        listener = new WebSocketListener(0, true, connection -> {
            try (connection) {
                InputStream in = connection.in();
                OutputStream out = connection.out();
                byte[] buffer = new byte[1024];
                int count;
                while ((count = in.read(buffer)) > 0) {
                    received.add(new String(buffer, 0, count, StandardCharsets.UTF_8));
                    out.write(buffer, 0, count);
                    out.flush();
                }
            } catch (IOException e) {
                received.add("error: " + e.getMessage());
            }
        });
        listener.start();
    }

    @AfterAll
    static void stopListener() {
        if (listener != null) {
            listener.stop();
        }
    }

    @Test
    void theHandshakeAnswersTheKeyTheBrowserSent() throws Exception {
        // The accept value is checked inside TinyWebSocket.connect, against this server's own
        // calculation; what this adds is the vector everyone's implementation is checked against -
        // RFC 6455's own example key and answer.
        assertEquals("s3pPLMBiTxaQ9kYGzzhZRbK+xOo=",
                WebSocketListener.accept("dGhlIHNhbXBsZSBub25jZQ=="),
                "the RFC's example: a server that gets this wrong cannot be reached from a browser");
        try (TinyWebSocket client = TinyWebSocket.connect("127.0.0.1", listener.boundPort())) {
            assertNotNull(client);
        }
    }

    @Test
    void aMessageOfAnyLengthArrivesAndEchoesBack() throws Exception {
        try (TinyWebSocket client = TinyWebSocket.connect("127.0.0.1", listener.boundPort())) {
            // 5 bytes and 200 use the 7 bit length, 70000 the 16 bit encoding, and 70000 again is the
            // one that has to come back in pieces - all the lengths a client may use.
            for (int size : new int[] {5, 200, 70000}) {
                byte[] payload = new byte[size];
                for (int i = 0; i < size; i++) {
                    payload[i] = (byte) ('a' + (i % 26));
                }
                client.sendBinary(payload);
                assertEquals(size, awaitBytes(size), "the server read " + size + " byte(s)");
                // The echo is accumulated too: the server reads and writes in its own chunks, so a
                // large message comes back as several frames and one of them is not the message.
                java.io.ByteArrayOutputStream echo = new java.io.ByteArrayOutputStream();
                while (echo.size() < size) {
                    TinyWebSocket.Frame frame = client.readFrame();
                    assertEquals(0x2, frame.opcode(), "an echo comes back as a binary frame");
                    echo.write(frame.payload());
                }
                assertArrayEquals(payload, echo.toByteArray(), "and it is the same bytes");
            }
        }
    }

    /**
     * A fragmented message reaches the reader as one run of bytes.
     *
     * <p>Asserted on the accumulation and not on one read: the layer above is a byte stream, so how
     * many reads it takes is its business - what must not show through is the *framing*, and a message
     * that arrived with a pause or an extra byte at the seam would show as a short accumulation.
     */
    @Test
    void aFragmentedMessageIsDeliveredWhole() throws Exception {
        try (TinyWebSocket client = TinyWebSocket.connect("127.0.0.1", listener.boundPort())) {
            client.sendFirstFragment("first half, ".getBytes(StandardCharsets.UTF_8));
            client.sendFragment("second half".getBytes(StandardCharsets.UTF_8), true);
            assertEquals("first half, second half", awaitText(24),
                    "the pieces are one run of bytes to the reader above");
        }
    }

    @Test
    void aPingIsAnsweredWithAPong() throws Exception {
        try (TinyWebSocket client = TinyWebSocket.connect("127.0.0.1", listener.boundPort())) {
            client.sendPing("are you there".getBytes(StandardCharsets.UTF_8));
            TinyWebSocket.Frame answer = client.readFrame();
            assertEquals(0xA, answer.opcode(), "a ping is answered with a pong");
            assertEquals("are you there", answer.text(), "carrying what the ping carried");
        }
    }

    @Test
    void aFrameTheClientForgotToMaskIsRefused() throws Exception {
        try (TinyWebSocket client = TinyWebSocket.connect("127.0.0.1", listener.boundPort())) {
            // A frame with the mask bit off, inside a connection that did its handshake: the
            // protocol requires masking from a client and this server refuses it rather than reading
            // it.
            client.sendUnmaskedFrame("abc".getBytes(StandardCharsets.UTF_8));
            String seen = received.poll(5, TimeUnit.SECONDS);
            assertTrue(seen != null && seen.startsWith("error:"),
                    "an unmasked frame must end the connection, saw " + seen);
        }
    }
}
