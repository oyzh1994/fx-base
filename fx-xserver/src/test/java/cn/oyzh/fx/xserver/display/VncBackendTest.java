package cn.oyzh.fx.xserver.display;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.clipboard.HostClipboard;
import cn.oyzh.fx.xserver.test.RawXClient;
import cn.oyzh.fx.xserver.test.TinyWebSocket;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The VNC backend: a real RFB client watching a real X client (backlog D2).
 *
 * <p>The point of this backend is that somebody else's software can look at this server, so the test
 * is somebody else's software - a hand-written RFB client that does the handshake, asks for frames
 * and reads them - rather than a call into the backend. What it can then assert is the whole chain:
 * a pixel an X client drew arrives in a VNC update, the client's own pixel format is honoured, an
 * incremental request waits for damage instead of spinning, and a pointer or key event from the
 * viewer reaches the X client as the event it should be.
 *
 * <p>A real client (macOS Screen Sharing) was used for the live check; this is what makes the
 * behaviour a property of the suite rather than of one afternoon.
 */
class VncBackendTest {

    /**
     * A display number the rest of the suite does not use - and it has to be checked, not assumed:
     * the first version of this test picked 92, which {@code AuthenticationTest} already had, and
     * the two collided only when the whole suite ran (a client of one reached the other's server and
     * the handshake it got back was a refusal).
     */
    private static final int DISPLAY = 94;

    private static final int RED = 0xFF0000;
    private static final int GREEN = 0x00FF00;

    @Test
    void aViewerSeesWhatTheClientDrew() throws Exception {
        try (Server server = new Server()) {
            try (RawXClient client = RawXClient.connect(DISPLAY);
                    VncClient viewer = server.connect()) {
                int window = client.allocateId();
                client.createWindow(window, client.root(), 0, 0, 200, 100, 0xFFFFFF);
                client.mapWindow(window);
                int gc = client.allocateId();
                client.createGC(gc, client.root(), RED);
                client.fillRectangle(window, gc, 20, 30, 40, 10);
                client.flush();
                Thread.sleep(150);                   // the display tick presents the damage

                viewer.requestFullFrame();
                Frame frame = viewer.readFrame();
                assertEquals(server.screen.width(), frame.width, "the whole screen comes back");
                assertEquals(server.screen.height(), frame.height);
                assertEquals(RED, frame.pixel(25, 35), "the rectangle the client drew");
                assertEquals(0xFFFFFF, frame.pixel(5, 5), "and the window's white background");
            }
        }
    }

    @Test
    void anIncrementalRequestWaitsForDamageInsteadOfSpinning() throws Exception {
        try (Server server = new Server()) {
            try (RawXClient client = RawXClient.connect(DISPLAY);
                    VncClient viewer = server.connect()) {
                int window = client.allocateId();
                client.createWindow(window, client.root(), 0, 0, 200, 100, 0xFFFFFF);
                client.mapWindow(window);
                client.flush();
                Thread.sleep(150);
                viewer.requestFullFrame();
                viewer.readFrame();                  // everything so far

                // Nothing has changed, so an incremental request is answered with nothing at all -
                // which is the protocol's own idle: the client waits, and so does the server.
                viewer.requestIncrementalFrame();
                viewer.readTimeout(400);
                assertEquals(null, viewer.readFrameOrNull(), "no damage means no update");

                // And a draw wakes it up, because the tick adds the damage to the viewer's pending
                // rectangle whatever that viewer happens to be waiting for.
                viewer.readTimeout(4000);
                int gc = client.allocateId();
                client.createGC(gc, client.root(), GREEN);
                client.fillRectangle(window, gc, 50, 50, 20, 20);
                client.flush();
                Frame frame = viewer.readFrame();
                assertNotNull(frame, "the draw should have woken the waiting request");
                assertEquals(GREEN, frame.pixel(55, 55), "and the frame carries it");
                assertTrue(frame.width <= 200 && frame.height <= 100,
                        "the update is the damage, not the whole screen: " + frame.width + "x"
                                + frame.height);
            }
        }
    }

    @Test
    void aPhotographicRectangleGoesAsTightJpegAndAFlatOneDoesNot() throws Exception {
        // ZRLE is a palette-and-run encoding: for content where almost every pixel differs from its
        // neighbour it falls through to raw - 3.1 MB and 57 ms of one core for a 1300x800 frame
        // (measured, A30). JPEG is the encoding for that content, and Tight is what carries it. The
        // other half of the test matters as much: a flat rectangle must stay on ZRLE, which is
        // lossless, so text and panels are never blurred by an encoder they did not need.
        try (Server server = new Server()) {
            try (RawXClient client = RawXClient.connect(DISPLAY);
                    VncClient viewer = server.connect()) {
                int window = client.allocateId();
                client.createWindow(window, client.root(), 0, 0, 200, 150, 0xFFFFFF);
                client.mapWindow(window);
                int gc = client.allocateId();
                client.createGC(gc, client.root(), 0);
                byte[] photo = new byte[200 * 150 * 4];
                java.util.Random random = new java.util.Random(7);
                for (int i = 0; i < 200 * 150; i++) {
                    photo[i * 4] = (byte) random.nextInt(256);          // blue
                    photo[i * 4 + 1] = (byte) random.nextInt(256);      // green
                    photo[i * 4 + 2] = (byte) random.nextInt(256);      // red
                    photo[i * 4 + 3] = (byte) 0xFF;
                }
                client.putImage(window, gc, 0, 0, 200, 150, photo);
                client.flush();
                Thread.sleep(150);

                // Tight and the JPEG quality pseudo-encodings: -23 is the highest the client accepts.
                viewer.setEncodings(7, 16, 0, -23, -32);
                viewer.requestFullFrame();
                Frame frame = viewer.readFrame();
                assertTrue(viewer.lastWasJpeg(),
                        "a photographic rectangle has to go as Tight's JPEG method");
                // Lossy, so close rather than equal: the mean channel difference of a quality-8 JPEG
                // of noise is large per pixel but the picture is the same one.
                assertTrue(jpegIsCloseTo(photo, frame, 200, 150),
                        "the JPEG has to look like the pixels that were drawn");

                // And a flat rectangle stays lossless even when the client would accept JPEG.
                int flat = client.allocateId();
                client.createGC(flat, client.root(), 0x3366CC);
                client.fillRectangle(window, flat, 0, 0, 200, 150);
                client.flush();
                Thread.sleep(200);
                viewer.requestIncrementalFrame();
                Frame flatFrame = viewer.readFrame();
                assertFalse(viewer.lastWasJpeg(), "a flat rectangle stays on the lossless encoding");
                assertEquals(0x3366CC, flatFrame.pixel(100, 100), "and it arrives exactly");
            }
        }
    }

    /** Whether a JPEG rectangle is recognisably the picture that was drawn: mean channel error. */
    private static boolean jpegIsCloseTo(byte[] photo, Frame frame, int width, int height) {
        long error = 0;
        long counted = 0;
        for (int y = 0; y < height; y += 3) {
            for (int x = 0; x < width; x += 3) {
                int at = (y * width + x) * 4;
                int drawn = ((photo[at + 2] & 0xFF) << 16) | ((photo[at + 1] & 0xFF) << 8)
                        | (photo[at] & 0xFF);
                int received = frame.pixel(x, y) & 0xFFFFFF;
                error += Math.abs(((drawn >> 16) & 0xFF) - ((received >> 16) & 0xFF));
                error += Math.abs(((drawn >> 8) & 0xFF) - ((received >> 8) & 0xFF));
                error += Math.abs((drawn & 0xFF) - (received & 0xFF));
                counted += 3;
            }
        }
        double mean = (double) error / counted;
        return mean < 60;                            // noise at quality 8 measures about 20-30
    }

    @Test
    void aChangeInTwoCornersIsSentAsSmallRectanglesAndNotOneBoxSpanningBoth() throws Exception {
        // A25 made an update the box the difference is in; A31 cuts that box into the 64x64 tiles
        // that really changed and merges the changed ones beside each other. Without it, two small
        // changes in opposite corners are one rectangle the size of the screen - and a full-screen
        // ZRLE frame measured 10 ms of one core in flat colours and 57 ms photographic, so a box is
        // not a cheap way to say "two corners".
        try (Server server = new Server()) {
            try (RawXClient client = RawXClient.connect(DISPLAY);
                    VncClient viewer = server.connect()) {
                int window = client.allocateId();
                client.createWindow(window, client.root(), 0, 0, 300, 200, 0xFFFFFF);
                client.mapWindow(window);
                client.flush();
                Thread.sleep(150);
                viewer.requestFullFrame();
                viewer.readFrame();

                int gc = client.allocateId();
                client.createGC(gc, client.root(), RED);
                // One request, so the damage really is one rectangle: the tick cannot have taken
                // half of it, which is what makes this test tell the old behaviour from the new.
                client.fillRectangles(window, gc, new int[] {5, 5, 10, 10},
                        new int[] {280, 180, 10, 10});
                client.flush();
                Thread.sleep(200);

                java.util.List<int[]> seen = new java.util.ArrayList<>();
                viewer.readTimeout(300);
                viewer.requestIncrementalFrame();
                for (Frame frame = viewer.readFrameOrNull(); frame != null;
                        frame = viewer.readFrameOrNull()) {
                    seen.addAll(viewer.lastRectangles);
                }
                assertFalse(seen.isEmpty(), "the two changes were never sent");
                for (int[] rectangle : seen) {
                    assertTrue(rectangle[2] <= 64 && rectangle[3] <= 64,
                            "each update is its own tile, not a box spanning the screen: "
                                    + java.util.Arrays.toString(rectangle));
                }

                // And both corners really arrived: a full frame carries the window as one rectangle.
                viewer.readTimeout(2000);
                viewer.requestFullFrame();
                Frame whole = viewer.readFrame();
                assertEquals(RED, whole.pixel(10, 10), "the first corner");
                assertEquals(RED, whole.pixel(285, 185), "and the far one");
            }
        }
    }

    @Test
    void aRedrawThatChangesNothingIsNotSentButAChangeAfterItIs() throws Exception {
        try (Server server = new Server()) {
            try (RawXClient client = RawXClient.connect(DISPLAY);
                    VncClient viewer = server.connect()) {
                int window = client.allocateId();
                client.createWindow(window, client.root(), 0, 0, 200, 100, 0xFFFFFF);
                client.mapWindow(window);
                int gc = client.allocateId();
                client.createGC(gc, client.root(), RED);
                client.fillRectangle(window, gc, 20, 30, 40, 10);
                client.flush();
                Thread.sleep(150);
                viewer.requestFullFrame();
                Frame frame = viewer.readFrame();
                assertEquals(RED, frame.pixel(25, 35), "the viewer has the rectangle");

                // The application draws the same rectangle again - EasyShell's aliyun tab redraws
                // its whole panel ~300 times a second without changing a pixel, and this is what
                // that used to cost: the viewer was sent the whole screen 61 times a second. The
                // damage still exists, so the request is woken, and the answer is still nothing.
                client.fillRectangle(window, gc, 20, 30, 40, 10);
                client.flush();
                Thread.sleep(150);
                viewer.requestIncrementalFrame();
                viewer.readTimeout(400);
                assertEquals(null, viewer.readFrameOrNull(),
                        "a redraw that changes no pixel is not sent");

                // And the request is still outstanding, so a real change does not have to wait for
                // the viewer to ask again - which is the whole risk of skipping: a skipped update
                // must not become a lost one.
                viewer.readTimeout(4000);
                int other = client.allocateId();
                client.createGC(other, client.root(), GREEN);
                client.fillRectangle(window, other, 100, 60, 20, 20);
                client.flush();
                Frame changed = viewer.readFrame();
                assertNotNull(changed, "the change after an identical redraw is sent");
                assertEquals(GREEN, changed.pixel(105, 65), "and it carries the new pixels");
                assertTrue(changed.width <= 24 && changed.height <= 24,
                        "and it is the box the difference is in, not the damage rectangle: "
                                + changed.width + "x" + changed.height);
            }
        }
    }

    @Test
    void theCustomersOwnPixelFormatIsHonoured() throws Exception {
        try (Server server = new Server()) {
            try (RawXClient client = RawXClient.connect(DISPLAY);
                    VncClient viewer = server.connect()) {
                int window = client.allocateId();
                client.createWindow(window, client.root(), 0, 0, 100, 100, 0x0000FF);
                client.mapWindow(window);
                client.flush();
                Thread.sleep(150);

                // 16 bits, 565, red in the high five: what a client with a narrow link asks for.
                viewer.setPixelFormat(16, 24, false, true, 31, 63, 31, 11, 5, 0);
                viewer.requestFullFrame();
                Frame frame = viewer.readFrame();
                assertEquals(2, frame.bytesPerPixel);
                assertEquals(0x001F, frame.pixel(50, 50), "pure blue in 565");
            }
        }
    }

    @Test
    void aServerWithAPasswordRefusesTheWrongOneAndServesTheRightOne() throws Exception {
        try (Server server = new Server("s3cret")) {
            // The wrong password is refused, and the client is told so rather than left hanging.
            try (VncClient refused = new VncClient(server.backend.boundPort(), "wrong")) {
                assertTrue(refused.wasRefused, "a wrong password must be refused");
            }
            try (RawXClient client = RawXClient.connect(DISPLAY);
                    VncClient viewer = server.connect()) {
                int window = client.allocateId();
                client.createWindow(window, client.root(), 0, 0, 100, 100, 0x0000FF);
                client.mapWindow(window);
                client.flush();
                Thread.sleep(150);
                viewer.requestFullFrame();
                assertEquals(0x0000FF, viewer.readFrame().pixel(50, 50),
                        "the right password gets the screen");
            }
        }
    }

    /**
     * A 3.3 client that waits for a SecurityResult gets one, and the session works - which is what
     * Finder's Screen Sharing needed (A52).
     *
     * <p>Measured against the real client 2026-10-04: it announces {@code RFB 003.003}, answers the
     * challenge correctly, and then sits there showing "connecting" for ever, because it waits for the
     * four byte result a 3.7 server sends. The specification does not send one in 3.3, so this server
     * waits a moment for the ClientInit a compliant client sends instead and sends the result only if
     * that byte does not come.
     */
    @Test
    void aThirtyThreeClientThatWaitsForTheResultGetsOne() throws Exception {
        try (Server server = new Server("s3cret")) {
            try (RawXClient client = RawXClient.connect(DISPLAY);
                    VncClient viewer = new VncClient(server.backend.boundPort(), "s3cret", true,
                            true)) {
                int window = client.allocateId();
                client.createWindow(window, client.root(), 0, 0, 100, 100, 0x0000FF);
                client.mapWindow(window);
                client.flush();
                Thread.sleep(150);
                viewer.requestFullFrame();
                assertEquals(0x0000FF, viewer.readFrame().pixel(50, 50),
                        "an Apple-shaped 3.3 client authenticates and gets the screen");
            }
        }
    }

    /**
     * The same password given as a **value**: the form an embedder uses, since it has the password in
     * hand and no file to write it into. It has to reach RFB authentication exactly as the file form
     * does - a wrong password refused, the right one served the screen - which is the whole of what
     * "the two forms are the same password" means.
     */
    @Test
    void thePasswordWorksJustAsWellGivenAsAValue() throws Exception {
        try (Server server = new Server("s3cret", true)) {
            try (VncClient refused = new VncClient(server.backend.boundPort(), "wrong")) {
                assertTrue(refused.wasRefused, "a wrong password must be refused, whatever form the"
                        + " right one was given in");
            }
            try (RawXClient client = RawXClient.connect(DISPLAY);
                    VncClient viewer = server.connect()) {
                int window = client.allocateId();
                client.createWindow(window, client.root(), 0, 0, 100, 100, 0x0000FF);
                client.mapWindow(window);
                client.flush();
                Thread.sleep(150);
                viewer.requestFullFrame();
                assertEquals(0x0000FF, viewer.readFrame().pixel(50, 50),
                        "the value form authenticates and gets the screen");
            }
        }
    }

    /**
     * {@code --title}, which every backend was handed {@code "xserver"} instead of (measured
     * 2026-10-04: the option was parsed, documented, and read by nobody). The VNC desktop name is
     * where it is visible without a window: a viewer's own title bar says what it is watching.
     */
    @Test
    void theTitleOptionNamesTheDesktopAViewerShows() throws Exception {
        try (Server server = new Server(null, false, "my-desktop")) {
            try (VncClient viewer = server.connect()) {
                assertEquals("my-desktop :" + DISPLAY, viewer.desktopName,
                        "the name a viewer puts in its window is the option's");
            }
        }
    }

    /**
     * A viewer's copy lands in the X clipboard, and X's copy lands in the viewer's.
     *
     * <p>Both directions go through the server's clipboard bridge - the same one the Mac's pasteboard
     * uses (C4) - so what this pins is the wiring: a viewer's {@code ClientCutText} must reach the
     * bridge, and the bridge telling the server that the clipboard changed must reach every viewer as
     * {@code ServerCutText}. The host pasteboard is a fake here for the same reason the C4 tests use
     * one: a test that rearranges whatever the person running it had copied is a test that fails for
     * reasons of its own.
     */
    @Test
    void theClipboardGoesBothWaysBetweenAViewerAndTheXSession() throws Exception {
        try (Server server = new Server()) {
            FakeClipboard host = new FakeClipboard();
            host.setText("the host's own text");
            server.xserver.startClipboardBridge(HostClipboard.of(host));

            try (RawXClient client = RawXClient.connect(DISPLAY);
                    VncClient viewer = server.connect()) {
                int window = client.allocateId();
                client.createWindow(window, client.root(), 0, 0, 200, 100, 0xFFFFFF);
                client.mapWindow(window);
                client.flush();
                Thread.sleep(150);

                // The viewer copies: the X clipboard serves it, so a client pasting gets it.
                viewer.cutText("from the viewer");
                assertEquals("from the viewer", awaitPaste(client, window, "from the viewer"),
                        "what the viewer copied should be pasted in the X session");
                assertEquals("from the viewer", host.text(),
                        "and the Mac's clipboard should have it too - that is the path it takes");

                // And X's copy goes the other way, as ServerCutText.
                copy(client, window, "from the X session");
                assertEquals("from the X session", viewer.readCutText(),
                        "what a client copied should reach the viewer");
            }
        }
    }

    /**
     * A browser's whole path: RFB carried inside WebSocket frames.
     *
     * <p>This is what noVNC does, and until now it needed a bridge process in front of the RFB port.
     * The handshake and the frame are asserted here rather than the browser being asked to show a
     * picture, because everything a browser would do with them is the same code the socket tests
     * already exercise - what is new is the transport underneath, and that is what this checks: the
     * banner comes out of a frame, the handshake completes, and an update arrives carrying the pixel
     * an X client drew.
     */
    @Test
    void aBrowserSpeaksRfbInsideWebSocketFrames() throws Exception {
        try (Server server = new Server()) {
            assertTrue(server.backend.boundWebSocketPort() > 0,
                    "the browser path should be listening");
            try (RawXClient client = RawXClient.connect(DISPLAY);
                    TinyWebSocket ws = TinyWebSocket.connect("127.0.0.1",
                            server.backend.boundWebSocketPort())) {
                TinyWebSocket.Payloads rfb = ws.payloads();
                assertEquals("RFB 003.008\n", rfb.readAscii(12),
                        "the RFB banner arrives in the first frame");
                ws.sendBinary("RFB 003.008\n".getBytes(StandardCharsets.US_ASCII));
                byte[] security = rfb.read(2);
                assertEquals(1, security[0], "one security type");
                assertEquals(1, security[1], "and it is None");
                ws.sendBinary(new byte[] {1});
                assertEquals(0, RawXClient.readInt(rfb.read(4), 0), "SecurityResult: OK");
                ws.sendBinary(new byte[] {1});                   // ClientInit
                byte[] init = rfb.read(24);
                int width = ((init[0] & 0xFF) << 8) | (init[1] & 0xFF);
                int height = ((init[2] & 0xFF) << 8) | (init[3] & 0xFF);
                assertEquals(server.screen.width(), width, "the screen the ServerInit names");
                assertEquals(server.screen.height(), height);
                // Big endian, which is the protocol's order for this reply - and NOT what
                // RawXClient.readInt reads, which is little endian: the first version of this test
                // read the name length that way, got 0x0B000000, and waited for 184 million bytes of
                // name that never came.
                int nameLength = ((init[20] & 0xFF) << 24) | ((init[21] & 0xFF) << 16)
                        | ((init[22] & 0xFF) << 8) | (init[23] & 0xFF);
                assertTrue(rfb.readAscii(nameLength).startsWith("cn/oyzh/fx/xserver"),
                        "and the name a browser shows in its title");

                // Something to look at, then a full frame asked for the way a browser asks.
                int window = client.allocateId();
                client.createWindow(window, client.root(), 0, 0, 100, 100, 0x00FF00);
                client.mapWindow(window);
                client.flush();
                Thread.sleep(150);
                // FramebufferUpdateRequest: incremental, then x, y, width, height - each two bytes,
                // in that order. The first version had the width in y and the height in width, which
                // asked for a zero-height area and was correctly answered with nothing at all.
                ws.sendBinary(new byte[] {3, 0, 0, 0, 0, 0, (byte) (width >> 8), (byte) width,
                        (byte) (height >> 8), (byte) height});

                // The update: FramebufferUpdate, one raw rectangle, then its pixels in the
                // client's format - which is the default 32 bit, so BGRA.
                assertEquals(0, rfb.read(1)[0] & 0xFF, "FramebufferUpdate");
                rfb.read(1);                                     // padding
                byte[] count = rfb.read(2);
                assertEquals(1, ((count[0] & 0xFF) << 8) | (count[1] & 0xFF), "one rectangle");
                byte[] rect = rfb.read(12);
                int rx = ((rect[0] & 0xFF) << 8) | (rect[1] & 0xFF);
                int ry = ((rect[2] & 0xFF) << 8) | (rect[3] & 0xFF);
                int rw = ((rect[4] & 0xFF) << 8) | (rect[5] & 0xFF);
                int rh = ((rect[6] & 0xFF) << 8) | (rect[7] & 0xFF);
                assertEquals(0, RawXClient.readInt(rect, 8), "raw encoding");
                byte[] pixels = rfb.read(rw * rh * 4);
                int at = ((50 - ry) * rw + (50 - rx)) * 4;
                int pixel = ((pixels[at + 2] & 0xFF) << 16) | ((pixels[at + 1] & 0xFF) << 8)
                        | (pixels[at] & 0xFF);
                assertEquals(0x00FF00, pixel, "the pixel the X client drew, out of a WebSocket frame");
            }
        }
    }

    /**
     * A viewer's pointer and buttons reach the X screen, which is the other half of "VNC works".
     *
     * <p>Measured 2026-10-04, while chasing a report that clicks in a viewer did nothing: the
     * position the X pointer ends at is the viewer's own, and a press and release arrive as the X
     * events a client selects.
     */
    @Test
    void aViewersPointerAndButtonsReachTheXScreen() throws Exception {
        try (Server server = new Server()) {
            try (RawXClient client = RawXClient.connect(DISPLAY);
                    VncClient viewer = server.connect()) {
                int window = client.allocateId();
                client.createWindow(window, client.root(), 0, 0, 300, 200, 0xFFFFFF);
                client.mapWindow(window);
                client.selectInput(window, 0x04);                // ButtonPressMask
                client.flush();
                Thread.sleep(150);

                // **With an incremental request outstanding and a screen that is not repainting**,
                // which is the state a viewer spends its life in: the request has nothing to send,
                // and a server that waits on damage inside it never reads the events that follow.
                // Measured 2026-10-04 against Finder's Screen Sharing: a picture that was fine and
                // clicks that did nothing at all.
                viewer.setEncodings(0);
                viewer.requestFullFrame();
                viewer.readFrame();
                viewer.requestIncrementalFrame();

                viewer.pointer(0, 50, 60);
                viewer.pointer(1, 50, 60);                       // left button down
                viewer.pointer(0, 50, 60);                       // and up
                client.flush();

                byte[] press = null;
                for (byte[] event = client.readEvent(); event != null; event = client.readEvent()) {
                    if ((event[0] & 0x7F) == 4) {
                        press = event;
                        break;
                    }
                }
                assertNotNull(press, "a click in the viewer has to reach the X client");
                int[] location = client.queryPointer(client.root());   // {x, y}
                assertEquals(50, location[0], "the X pointer is where the viewer put it");
                assertEquals(60, location[1], "in both axes");
            }
        }
    }

    /**
     * ZRLE to a client whose pixel format is **Apple's**: 32 bits per pixel with a <b>depth of 32</b>,
     * which is what Finder's Screen Sharing asks for (measured 2026-10-04). The CPIXEL is four bytes
     * there where the common 32bpp/depth-24 client's is three - the difference the garbled picture
     * turned out to hang on.
     */
    @Test
    void aZrleClientInApplesFormatGetsTheSamePixels() throws Exception {
        try (Server server = new Server()) {
            try (RawXClient client = RawXClient.connect(DISPLAY);
                    VncClient viewer = server.connect()) {
                viewer.setPixelFormat(32, 32, false, true, 255, 255, 255, 16, 8, 0);
                int window = client.allocateId();
                client.createWindow(window, client.root(), 0, 0, 200, 200, 0xFFFFFF);
                client.mapWindow(window);
                int gc = client.allocateId();
                client.createGC(gc, client.root(), 0x0000FF);
                client.fillRectangle(window, gc, 0, 0, 200, 200);
                client.flush();
                Thread.sleep(150);

                viewer.setEncodings(16, 0);
                viewer.requestFullFrame();
                Frame frame = viewer.readFrame();

                assertEquals(0x0000FF, frame.pixel(50, 50), "the flat area");
                assertEquals(0xE8E8E8, frame.pixel(250, 230), "and the root outside it");
            }
        }
    }

    /**
     * A client that asks for ZRLE gets compressed frames, and they decode to the same pixels.
     *
     * <p>The bandwidth claim in one test: a screen of flat colours is where ZRLE wins, and the
     * assertion is on the *decoded* picture first and the size second - a compressed frame that is
     * smaller and wrong is worse than a raw one. The whole X-side setup is a window of one colour
     * with a stripe of another, which drives the encoder through its solid, palette and RLE paths.
     */
    @Test
    void aClientThatAsksForZrleGetsCompressedFrames() throws Exception {
        try (Server server = new Server()) {
            try (RawXClient client = RawXClient.connect(DISPLAY);
                    VncClient viewer = server.connect()) {
                int window = client.allocateId();
                client.createWindow(window, client.root(), 0, 0, 200, 200, 0xFFFFFF);
                client.mapWindow(window);
                int gc = client.allocateId();
                client.createGC(gc, client.root(), 0x0000FF);
                client.fillRectangle(window, gc, 0, 0, 200, 200);      // one flat colour
                int gc2 = client.allocateId();
                client.createGC(gc2, client.root(), 0x00FF00);
                client.fillRectangle(window, gc2, 0, 100, 200, 4);     // a stripe: runs and changes
                client.flush();
                Thread.sleep(150);

                viewer.setEncodings(16, 0);
                viewer.requestFullFrame();
                Frame frame = viewer.readFrame();

                assertEquals(0x0000FF, frame.pixel(50, 50), "the flat area");
                assertEquals(0x00FF00, frame.pixel(50, 101), "the stripe");
                assertEquals(0xE8E8E8, frame.pixel(250, 230), "and somewhere with no window at all");
                int rawBytes = frame.width * frame.height * 4;
                assertTrue(frame.encodedBytes() < rawBytes / 8,
                        "a screen of flat colours should compress far below raw: " + frame.encodedBytes()
                                + " of " + rawBytes);
            }
        }
    }

    /**
     * A client that speaks **3.3**, which is what macOS Screen Sharing announces.
     *
     * <p>Found by the live check, and it is the branch no test covered: 3.3 has no security-type
     * list (a single four byte type instead) and **no SecurityResult** after it, so a server that
     * answers a 3.3 client the 3.8 way leaves it waiting for a byte that never comes. Measured on
     * the wire before this test existed: {@code RFB 003.003\n} from Screen Sharing, then silence.
     */
    @Test
    void aClientThatSpeaksThirtyThreeIsAnsweredInItsOwnTerms() throws Exception {
        try (Server server = new Server()) {
            try (RawXClient client = RawXClient.connect(DISPLAY);
                    VncClient viewer = new VncClient(server.backend.boundPort(), null, true)) {
                int window = client.allocateId();
                client.createWindow(window, client.root(), 0, 0, 100, 100, 0x0000FF);
                client.mapWindow(window);
                client.flush();
                Thread.sleep(150);
                viewer.requestFullFrame();
                assertEquals(0x0000FF, viewer.readFrame().pixel(50, 50),
                        "a 3.3 client gets the screen too, with no SecurityResult in between");
            }
        }
    }

    /**
     * A viewer that asked for the Cursor pseudo-encoding is told what the pointer looks like.
     *
     * <p>Without it a viewer draws a pointer of its own, so an I-beam over a text field looks like an
     * arrow and a resize handle looks like an arrow. The message is a rectangle whose <b>x and y are
     * the hotspot</b> - not a position, which is the one thing about it that is unlike every other
     * update - followed by the image and a one bit mask.
     */
    @Test
    void aViewerThatAsksForTheCursorIsToldWhatThePointerLooksLike() throws Exception {
        try (Server server = new Server()) {
            try (RawXClient client = RawXClient.connect(DISPLAY);
                    VncClient viewer = server.connect()) {
                int window = client.allocateId();
                client.createWindow(window, client.root(), 0, 0, 200, 200, 0xFFFFFF);
                client.mapWindow(window);
                // The X cursor font's left pointer (68), which is the arrow every client asks for.
                int font = client.allocateId();
                client.openFont(font, "cursor");
                int cursor = client.allocateId();
                client.createGlyphCursor(cursor, font, font, 68, 69, 0, 0);
                client.setWindowCursor(window, cursor);
                client.warpPointer(window, 100, 100);
                client.flush();

                viewer.setEncodings(-239, 16, 0);
                // The pointer's shape is the display tick's news, not the framebuffer's: it arrives
                // with the next frame the tick presents, so this waits for it rather than reading one
                // frame and looking.
                CursorRect shape = null;
                long deadline = System.currentTimeMillis() + 5000;
                while (shape == null && System.currentTimeMillis() < deadline) {
                    viewer.requestFullFrame();
                    // A message that carries only the cursor has no data rectangle in it, so this
                    // reads whatever arrives rather than insisting on a frame: the cursor is its own
                    // update.
                    viewer.readFrameOrNull();
                    shape = viewer.lastCursor();
                }
                assertNotNull(shape, "a viewer that asked for the cursor should be sent one");
                assertTrue(shape.width() > 0 && shape.height() > 0, "with an image: " + shape);
                assertTrue(shape.hotX() >= 0 && shape.hotX() < shape.width()
                                && shape.hotY() >= 0 && shape.hotY() < shape.height(),
                        "and a hotspot inside it: " + shape);
                assertTrue(shape.maskBits() > 0,
                        "the mask says which pixels are the pointer: " + shape);
            }
        }
    }

    /**
     * The DES itself, against a vector computed outside this codebase.
     *
     * <p>The scheme is the one every VNC server implements and no DES library does by default: the
     * password's bytes are the key **with each byte's bits reversed**. The vector below is
     * {@code openssl enc -des-ecb -K ceccc64ea62e0000 -nopad} over the bytes 00..0f - the key being
     * "s3cret" (73 33 63 72 65 74) reversed and zero padded - so it is an independent answer to the
     * question "is this server's VNC authentication the standard one". Getting the reversal wrong
     * does not fail loudly anywhere: the server just refuses every password, which is exactly what
     * a live client showed before this was pinned.
     */
    @Test
    void theChallengeIsEncryptedTheWayTheProtocolSays() throws Exception {
        byte[] challenge = new byte[16];
        for (int i = 0; i < 16; i++) {
            challenge[i] = (byte) i;
        }
        byte[] expected = new byte[16];
        String hex = "fc9a2bb8546a63388eb45b530d3a6337";
        for (int i = 0; i < 16; i++) {
            expected[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        assertArrayEquals(expected, RfbSession.encryptChallenge(challenge, "s3cret"),
                "the VNC challenge encrypted with the password as a bit-reversed DES key");
    }

    @Test
    void aPointerAndAKeyFromTheViewerReachTheClient() throws Exception {
        try (Server server = new Server()) {
            try (RawXClient client = RawXClient.connect(DISPLAY);
                    VncClient viewer = server.connect()) {
                int window = client.allocateId();
                client.createWindow(window, client.root(), 0, 0, 200, 200, 0xFFFFFF);
                client.selectInput(window, 0x03 | (1 << 2));    // key, button and pointer events
                client.mapWindow(window);
                client.setInputFocus(window, 1);
                client.flush();
                client.readTimeoutMillis(2000);
                Thread.sleep(150);
                viewer.requestFullFrame();
                viewer.readFrame();

                // A press at 30,40: the X client is told where the pointer is and what happened.
                viewer.pointer(1, 30, 40);
                byte[] press = client.readEvent(4);             // ButtonPress
                assertNotNull(press, "the viewer's click should arrive as a button press");
                assertEquals(1, press[1] & 0xFF, "button 1, which is the event's detail byte");
                assertEquals(30, (short) RawXClient.readShort(press, 24), "at the x the viewer sent");
                assertEquals(40, (short) RawXClient.readShort(press, 26), "and the y");
                viewer.pointer(0, 30, 40);                      // release

                // And a key: RFB sends the keysym, and the keycode is the server's own map's.
                viewer.key(true, 'a');
                byte[] key = client.readEvent(2);               // KeyPress
                assertNotNull(key, "the viewer's key should arrive as a key press");
                assertEquals(cn.oyzh.fx.xserver.input.Keymap.keycodeForLetter('a'),
                        key[1] & 0xFF, "the keycode 'a' has in this server's keymap");
                viewer.key(false, 'a');
            }
        }
    }

    // ------------------------------------------------------------------
    // The harness
    // ------------------------------------------------------------------

    /**
     * A server on the VNC backend, with the backend itself in hand.
     *
     * <p>{@code --vnc-port 0} asks for any free port and {@code VncBackend.boundPort} answers which
     * one it got: a test that hard-coded 5900 would fail on any machine already running a VNC server.
     */
    private static final class Server implements AutoCloseable {

        final XServer xserver;
        final VncBackend backend;
        final Screen screen;

        final String password;

        Server() throws Exception {
            this(null);
        }

        Server(String password) throws Exception {
            this(password, false);
        }

        /**
         * The same, with the password handed over as {@code --vnc-password} rather than through a
         * file - the form a program that starts this server as a library uses, because it has the
         * password in hand and no file to put it in.
         */
        Server(String password, boolean asValue) throws Exception {
            this(password, asValue, null);
        }

        /** The same, with {@code --title} naming the desktop a viewer shows. */
        Server(String password, boolean asValue, String title) throws Exception {
            this.password = password;
            // Through the real path: `--backend vnc` is parsed, implies single-window (which is what
            // puts a backend on the master screen before any client connects), and the factory makes
            // the backend itself. A test that built one by hand would not be testing any of that.
            java.nio.file.Path passwordFile = java.nio.file.Files.createTempFile("vnc-pass", ".txt");
            passwordFile.toFile().deleteOnExit();
            if (password != null) {
                java.nio.file.Files.writeString(passwordFile, password);
            }
            java.util.List<String> base = new java.util.ArrayList<>(java.util.List.of(
                    "--display", ":" + DISPLAY, "--backend", "vnc", "--vnc-port", "0",
                    "--vnc-ws-port", "0", "--no-unix", "--geometry", "320x240", "--no-clipboard"));
            if (title != null) {
                base.add("--title");
                base.add(title);
            }
            if (password == null) {
                // no more arguments
            } else if (asValue) {
                base.add("--vnc-password");
                base.add(password);
            } else {
                base.add("--vnc-password-file");
                base.add(passwordFile.toString());
            }
            String[] args = base.toArray(new String[0]);
            xserver = new XServer(ServerOptions.parse(args));
            xserver.start();
            xserver.startEventLoop();
            screen = xserver.screen();
            backend = (VncBackend) xserver.sessions().get(0).backend();
            assertNotNull(backend, "the VNC backend should be listening before a client connects");
        }

        VncClient connect() throws IOException {
            return new VncClient(backend.boundPort(), password);
        }

        @Override
        public void close() {
            xserver.stop();
        }
    }

    /**
     * Pastes through an X client until the clipboard serves {@code expected}.
     *
     * <p>Waiting for the *text* and not for "something": the clipboard already has the host's
     * contents when the test starts, so the first paste answers with those - and an assertion on the
     * first non-empty answer passes while testing nothing at all.
     */
    private static String awaitPaste(RawXClient client, int window, String expected)
            throws Exception {
        long deadline = System.currentTimeMillis() + 5000;
        String text = null;
        while (System.currentTimeMillis() < deadline) {
            text = paste(client, window);
            if (expected.equals(text)) {
                return text;
            }
            Thread.sleep(50);
        }
        return text;
    }

    private static String paste(RawXClient client, int window) throws IOException {
        int property = client.internAtom("XSERVER_VNC_TEST_DATA");
        client.convertSelection(window, client.internAtom("CLIPBOARD"),
                client.internAtom("UTF8_STRING"), property, 0);
        client.flush();
        byte[] notify = null;
        long deadline = System.currentTimeMillis() + 3000;
        while (notify == null && System.currentTimeMillis() < deadline) {
            byte[] event = client.readEvent();
            if (event != null && (event[0] & 0x7F) == 31) {
                notify = event;
            }
        }
        if (notify == null || RawXClient.readInt(notify, 20) == 0) {
            return "";
        }
        byte[] reply = client.getProperty(window, property, 0, true);
        int length = RawXClient.readInt(reply, 16) * (reply[1] & 0xFF) / 8;
        return new String(reply, 32, length, StandardCharsets.UTF_8);
    }

    /** Copies from inside the X session: take the selection and answer the bridge's fetch. */
    private static void copy(RawXClient client, int window, String text) throws IOException {
        int selection = client.internAtom("CLIPBOARD");
        client.setSelectionOwner(window, selection, 0);
        client.flush();
        long deadline = System.currentTimeMillis() + 3000;
        while (System.currentTimeMillis() < deadline) {
            byte[] event = client.readEvent();
            if (event == null) {
                continue;
            }
            if ((event[0] & 0x7F) == 30 && RawXClient.readInt(event, 16) == selection) {
                int requestor = RawXClient.readInt(event, 12);
                int target = RawXClient.readInt(event, 20);
                int property = RawXClient.readInt(event, 24);
                client.setStringProperty(requestor, property, target, text);
                client.flush();
                client.sendSelectionNotify(requestor, 0, requestor, selection, target, property);
                client.flush();
                return;
            }
        }
    }

    /** The host clipboard the test owns, so the suite never touches the Mac's pasteboard. */
    private static final class FakeClipboard extends java.awt.datatransfer.Clipboard {

        private String text;

        FakeClipboard() {
            super("xserver-vnc-test");
        }

        synchronized String text() {
            return text;
        }

        /** What the test puts on the "Mac's clipboard" before the bridge starts polling it. */
        synchronized void setText(String text) {
            this.text = text;
        }

        @Override
        public synchronized void setContents(java.awt.datatransfer.Transferable contents,
                                             java.awt.datatransfer.ClipboardOwner owner) {
            try {
                text = contents == null ? null
                        : (String) contents.getTransferData(
                                java.awt.datatransfer.DataFlavor.stringFlavor);
            } catch (Exception e) {
                text = null;
            }
        }

        @Override
        public synchronized java.awt.datatransfer.Transferable getContents(Object requestor) {
            return text == null ? null : new java.awt.datatransfer.StringSelection(text);
        }
    }

    /**
     * One decoded update: the rectangle and its pixels, read the way the format they were sent in
     * says to read them.
     *
     * <p>The endianness matters and is easy to forget: the handshake's default is little endian - it
     * is the framebuffer's own layout - so a test that assembled the bytes high-first read every
     * colour reversed, which looks like a server that swapped red and blue rather than a reader that
     * was one line wrong.
     */
    /** The cursor the server sent: the rectangle's x and y are its hotspot, which is the point. */
    private record CursorRect(int hotX, int hotY, int width, int height, int maskBits) {
    }

    private record Frame(int x, int y, int width, int height, int bytesPerPixel, boolean bigEndian,
                         int encodedBytes, byte[] pixels) {
        int pixel(int px, int py) {
            int at = ((py - y) * width + (px - x)) * bytesPerPixel;
            int value = 0;
            if (bigEndian) {
                for (int i = 0; i < bytesPerPixel; i++) {
                    value = (value << 8) | (pixels[at + i] & 0xFF);
                }
            } else {
                for (int i = bytesPerPixel - 1; i >= 0; i--) {
                    value = (value << 8) | (pixels[at + i] & 0xFF);
                }
            }
            return value;
        }
    }

    /** Just enough RFB to be a client: 3.8, security None, raw frames, pointer and key. */
    private static final class VncClient implements AutoCloseable {

        final Socket socket;
        private final InputStream in;
        private final OutputStream out;
        private int bytesPerPixel = 4;
        private boolean bigEndian;
        /** The bytes the last update took on the wire, for the encoding tests. */
        private int lastFrameBytes;
        /**
         * One inflater for the whole session, because the ZRLE stream is one: each update's bytes
         * continue the last one's. A fresh inflater per update decodes the first frame and then says
         * "incorrect header check" - which is exactly what the first version of this client did.
         */
        private final java.util.zip.Inflater zrleInflater = new java.util.zip.Inflater();
        /** The last cursor the server sent, for the Cursor pseudo-encoding test. */
        private CursorRect lastCursor;
        /** The geometry of every rectangle of the update just read, in the order they arrived. */
        final java.util.List<int[]> lastRectangles = new java.util.ArrayList<>();
        /** Whether the update just read carried a Tight-JPEG rectangle (A32). */
        private boolean lastWasJpeg;
        /** The desktop name from the ServerInit: what a viewer puts in its own window's title. */
        String desktopName;

        boolean lastWasJpeg() {
            return lastWasJpeg;
        }

        CursorRect lastCursor() {
            return lastCursor;
        }


        boolean wasRefused;

        VncClient(int port, String password) throws IOException {
            this(port, password, false);
        }

        /** {@code thirtyThree} announces RFB 003.003, the version Apple's client speaks. */
        VncClient(int port, String password, boolean thirtyThree) throws IOException {
            this(port, password, thirtyThree, false);
        }

        /**
         * The same, with {@code apple33} making the client behave like Finder's own Screen Sharing:
         * it speaks 003.003, sends the challenge response, and then **waits for the four byte
         * SecurityResult** a 3.7 server would send before it sends ClientInit. Measured 2026-10-04
         * against the real client: a server that sends nothing there leaves it "connecting" for ever.
         */
        VncClient(int port, String password, boolean thirtyThree, boolean apple33)
                throws IOException {
            socket = new Socket();
            socket.connect(new InetSocketAddress("127.0.0.1", port), 4000);
            socket.setSoTimeout(4000);
            in = new BufferedInputStream(socket.getInputStream());
            out = new BufferedOutputStream(socket.getOutputStream());
            runHandshake(password, thirtyThree, apple33);
        }

        private void runHandshake(String password, boolean thirtyThree, boolean apple33)
                throws IOException {
            String version = readAscii(12);
            assertEquals("RFB 003.008\n", version, "the server announces the version it speaks");
            out.write((thirtyThree ? "RFB 003.003\n" : "RFB 003.008\n")
                    .getBytes(StandardCharsets.US_ASCII));
            out.flush();
            if (thirtyThree) {
                // 3.3: the security type is four bytes and there is no result after the exchange.
                assertEquals(password == null ? 1 : 2, readInt(), "the 3.3 security type");
                if (password != null) {
                    byte[] challenge = readBytes(16);
                    out.write(vncEncrypt(challenge, password));
                    out.flush();
                    if (apple33) {
                        assertEquals(0, readInt(), "the SecurityResult Apple's 3.3 client waits for,"
                                + " and which the specification does not send in 3.3");
                    }
                }
            } else {
            assertEquals(1, in.read(), "one security type");
            int type = in.read();
            if (password == null) {
                assertEquals(1, type, "and it is None");
                out.write(1);                               // choose it
                out.flush();
            } else {
                assertEquals(2, type, "with a password configured the offer is VNC authentication");
                out.write(2);
                out.flush();
                byte[] challenge = readBytes(16);
                out.write(vncEncrypt(challenge, password));
                out.flush();
            }
            }
            if (!thirtyThree) {
            int result = readInt();
            if (result != 0) {
                // Refused: the server sends the reason after the result and then closes, so there is
                // nothing else to do here and the caller asserts on wasRefused.
                wasRefused = true;
                return;
            }
            }
            out.write(1);                                   // ClientInit: shared
            out.flush();

            readU16();                                      // width
            readU16();                                      // height
            setPixelFormatBytes(readBytes(16));             // the default format
            int nameLength = readInt();
            desktopName = readAscii(nameLength);
        }

        /** {@code SetEncodings}: what this viewer says it can decode, in its order of preference. */
        void setEncodings(int... encodings) throws IOException {
            out.write(2);
            out.write(0);
            writeU16(out, encodings.length);
            for (int encoding : encodings) {
                writeInt(out, encoding);
            }
            out.flush();
        }

        void setPixelFormat(int bpp, int depth, boolean bigEndian, boolean trueColour, int redMax,
                            int greenMax, int blueMax, int redShift, int greenShift, int blueShift)
                throws IOException {
            byte[] format = new byte[16];
            format[0] = (byte) bpp;
            format[1] = (byte) depth;
            format[2] = (byte) (bigEndian ? 1 : 0);
            format[3] = (byte) (trueColour ? 1 : 0);
            writeU16(format, 4, redMax);
            writeU16(format, 6, greenMax);
            writeU16(format, 8, blueMax);
            format[10] = (byte) redShift;
            format[11] = (byte) greenShift;
            format[12] = (byte) blueShift;
            out.write(0);                                   // SetPixelFormat
            out.write(new byte[3]);                         // padding
            out.write(format);
            out.flush();
            setPixelFormatBytes(format);
        }

        private void setPixelFormatBytes(byte[] format) {
            bytesPerPixel = Math.max(1, (format[0] & 0xFF) / 8);
            bigEndian = (format[2] & 0xFF) != 0;
        }

        void requestFullFrame() throws IOException {
            request(false);
        }

        void requestIncrementalFrame() throws IOException {
            request(true);
        }

        private void request(boolean incremental) throws IOException {
            out.write(3);                                   // FramebufferUpdateRequest
            out.write(incremental ? 1 : 0);
            writeU16(out, 0);
            writeU16(out, 0);
            writeU16(out, 0xFFFF);
            writeU16(out, 0xFFFF);
            out.flush();
        }

        /** {@code ClientCutText}: this viewer copied something. */
        void cutText(String text) throws IOException {
            byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
            out.write(6);
            out.write(new byte[3]);
            writeInt(out, bytes.length);
            out.write(bytes);
            out.flush();
        }

        /** {@code ServerCutText}, which is how the server tells this viewer the clipboard changed. */
        String readCutText() throws IOException {
            while (true) {
                int type = in.read();
                if (type < 0) {
                    throw new EOFException("the server closed the connection");
                }
                if (type == 3) {
                    readBytes(3);
                    int length = readInt();
                    return new String(readBytes(length), StandardCharsets.UTF_8);
                }
                if (type == 0) {                         // an update we are not waiting for
                    readBytes(1);
                    int rectangles = (readBytes(2)[0] << 8) | 0;
                    continue;
                }
                throw new IOException("unexpected RFB message " + type);
            }
        }


        void pointer(int buttonMask, int x, int y) throws IOException {
            out.write(5);
            out.write(buttonMask);
            writeU16(out, x);
            writeU16(out, y);
            out.flush();
        }

        void key(boolean down, int character) throws IOException {
            out.write(4);
            out.write(down ? 1 : 0);
            out.write(new byte[2]);
            writeInt(out, character);                       // an ASCII keysym is the character
            out.flush();
        }

        Frame readFrame() throws IOException {
            Frame frame = readFrameOrNull();
            assertNotNull(frame, "expected a frame, and the connection closed instead");
            return frame;
        }

        /** Null when nothing arrived before the socket's own timeout, and null at end of stream. */
        Frame readFrameOrNull() throws IOException {
            int type;
            try {
                type = in.read();
            } catch (java.net.SocketTimeoutException e) {
                return null;
            }
            if (type < 0) {
                return null;
            }
            assertEquals(0, type, "FramebufferUpdate");
            in.read();
            int rectangles = readU16();
            lastRectangles.clear();
            lastWasJpeg = false;
            Frame last = null;
            for (int i = 0; i < rectangles; i++) {
                int x = readU16();
                int y = readU16();
                int width = readU16();
                int height = readU16();
                lastRectangles.add(new int[] {x, y, width, height});
                int encoding = readInt();
                if (encoding == -223) {                     // DesktopSize: no pixels follow it
                    continue;
                }
                if (encoding == -239) {                     // Cursor: image then a one bit mask
                    int bytesPerPixel = Math.max(1, this.bytesPerPixel);
                    byte[] image = readBytes(width * height * bytesPerPixel);
                    byte[] mask = readBytes(((width + 7) / 8) * height);
                    int bits = 0;
                    for (byte b : mask) {
                        bits += Integer.bitCount(b & 0xFF);
                    }
                    lastCursor = new CursorRect(x, y, width, height, bits);
                    continue;
                }
                byte[] data;
                if (encoding == 7) {                        // Tight, which this server sends as JPEG
                    int control = in.read();
                    assertEquals(0x90, control, "Tight from this server is JPEG with nothing to reset");
                    int length = readCompactLength();
                    byte[] jpeg = readBytes(length);
                    lastFrameBytes = jpeg.length;
                    lastWasJpeg = true;
                    data = decodeJpeg(jpeg, width, height);
                } else if (encoding == 16) {                 // ZRLE: a length, then compressed tiles
                    int compressed = readInt();
                    byte[] zrle = readBytes(compressed);
                    lastFrameBytes = compressed;
                    data = decodeZrle(zrle, width, height);
                } else {
                    assertEquals(0, encoding, "raw encoding");
                    data = new byte[width * height * bytesPerPixel];
                    in.readNBytes(data, 0, data.length);
                    lastFrameBytes = data.length;
                }
                last = new Frame(x, y, width, height, bytesPerPixel, bigEndian, lastFrameBytes, data);
            }
            return last;
        }

        /**
         * The length of a Tight rectangle's JPEG data: one, two or three bytes, seven bits at a time,
         * with the top bit of each byte saying "another one follows".
         */
        private int readCompactLength() throws IOException {
            int first = in.read();
            if (first < 0) {
                throw new EOFException("the connection closed inside a Tight rectangle");
            }
            if ((first & 0x80) == 0) {
                return first;
            }
            int second = in.read();
            if (second < 0) {
                throw new EOFException("the connection closed inside a Tight length");
            }
            if ((second & 0x80) == 0) {
                return (first & 0x7F) | second << 7;
            }
            int third = in.read();
            if (third < 0) {
                throw new EOFException("the connection closed inside a Tight length");
            }
            return (first & 0x7F) | (second & 0x7F) << 7 | third << 14;
        }

        /** One JPEG rectangle, decoded into the client's own pixel format as {@link Frame} reads it. */
        private byte[] decodeJpeg(byte[] jpeg, int width, int height) throws IOException {
            java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(
                    new java.io.ByteArrayInputStream(jpeg));
            assertNotNull(image, "the JPEG rectangle is not a JPEG");
            assertEquals(width, image.getWidth(), "the JPEG's width");
            assertEquals(height, image.getHeight(), "the JPEG's height");
            byte[] data = new byte[width * height * bytesPerPixel];
            int at = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int argb = image.getRGB(x, y);
                    // Little endian, red at 16 in the 32 bit format this client asked for.
                    data[at++] = (byte) (argb & 0xFF);
                    data[at++] = (byte) ((argb >> 8) & 0xFF);
                    data[at++] = (byte) ((argb >> 16) & 0xFF);
                    for (int rest = 3; rest < bytesPerPixel; rest++) {
                        data[at++] = 0;
                    }
                }
            }
            return data;
        }

        /**
         * A ZRLE decoder, so the test reads what a real client reads.
         *
         * <p>An independent implementation on purpose: the encoder and the decoder would agree if
         * they shared a helper, and the thing worth testing is that what goes on the wire is what the
         * *protocol* says - the CPIXEL width, the tile boundaries, the run lengths and the zlib
         * stream. It handles the four subencodings this server emits and refuses anything else, so a
         * future encoder that invented one fails here rather than in somebody's viewer.
         */
        private byte[] decodeZrle(byte[] compressed, int width, int height) throws IOException {
            java.util.zip.Inflater inflater = zrleInflater;
            inflater.setInput(compressed);
            byte[] raw = new byte[width * height * 4 + 1024];
            int rawLength = 0;
            try {
                while (!inflater.needsInput()) {
                    if (rawLength == raw.length) {
                        raw = java.util.Arrays.copyOf(raw, raw.length * 2);
                    }
                    int count = inflater.inflate(raw, rawLength, raw.length - rawLength);
                    if (count <= 0) {
                        break;
                    }
                    rawLength += count;
                }
            } catch (java.util.zip.DataFormatException e) {
                throw new IOException("the ZRLE stream is not zlib", e);
            }
            int cpxel = bytesPerPixel == 4 ? 3 : bytesPerPixel;
            // Each tile goes into the region at its own place: the tiles arrive in order, but the
            // frame they make up is two dimensional and the reader above indexes it that way.
            byte[] pixels = new byte[width * height * bytesPerPixel];
            int at = 0;
            for (int tileY = 0; tileY < height; tileY += 64) {
                int tileHeight = Math.min(64, height - tileY);
                for (int tileX = 0; tileX < width; tileX += 64) {
                    int tileWidth = Math.min(64, width - tileX);
                    java.io.ByteArrayOutputStream tile = new java.io.ByteArrayOutputStream();
                    at = decodeTile(raw, at, tile, tileWidth, tileHeight, cpxel);
                    byte[] tileBytes = tile.toByteArray();
                    int rowBytes = tileWidth * bytesPerPixel;
                    for (int y = 0; y < tileHeight; y++) {
                        System.arraycopy(tileBytes, y * rowBytes, pixels,
                                ((tileY + y) * width + tileX) * bytesPerPixel, rowBytes);
                    }
                }
            }
            return pixels;
        }

        /** One tile, its 1..16 colours already unpacked into the client's pixels. */
        private int decodeTile(byte[] raw, int at, java.io.ByteArrayOutputStream out, int width,
                               int height, int cpxel) throws IOException {
            int pixels = width * height;
            int subencoding = raw[at++] & 0xFF;
            if (subencoding == 0) {
                for (int i = 0; i < pixels; i++) {
                    at = emit(raw, at, out, cpxel);
                }
                return at;
            }
            if (subencoding == 1) {
                byte[] colour = value(raw, at, cpxel);
                for (int i = 0; i < pixels; i++) {
                    write(out, colour);
                }
                return at + cpxel;
            }
            if (subencoding == 128) {
                int written = 0;
                while (written < pixels) {
                    byte[] colour = value(raw, at, cpxel);
                    at += cpxel;
                    int length = 1;
                    int b;
                    do {
                        b = raw[at++] & 0xFF;
                        length += b;
                    } while (b == 255);
                    for (int i = 0; i < length && written < pixels; i++, written++) {
                        write(out, colour);
                    }
                }
                return at;
            }
            if (subencoding >= 2 && subencoding <= 16) {
                byte[][] palette = new byte[subencoding][];
                for (int i = 0; i < subencoding; i++) {
                    palette[i] = value(raw, at, cpxel);
                    at += cpxel;
                }
                int bits = subencoding == 2 ? 1 : subencoding <= 4 ? 2 : 4;
                int perByte = 8 / bits;
                int mask = (1 << bits) - 1;
                for (int i = 0; i < pixels; i++) {
                    int index = i % perByte;
                    int value = raw[at + i / perByte] & 0xFF;
                    int shift = 8 - bits * (index + 1);
                    write(out, palette[(value >> shift) & mask]);
                }
                return at + (pixels + perByte - 1) / perByte;
            }
            throw new IOException("unsupported ZRLE subencoding " + subencoding
                    + ": the encoder should not be sending this");
        }

        /**
         * One CPIXEL as the client's full pixel: a three byte CPIXEL gets the unused high byte back,
         * which is what {@code Frame} reads. The value is the same either way - the byte is not part
         * of any channel at a depth of 24 - so this is bookkeeping, not a colour decision.
         */
        private byte[] value(byte[] raw, int at, int cpxel) {
            byte[] pixel = new byte[bytesPerPixel];
            System.arraycopy(raw, at, pixel, 0, Math.min(cpxel, bytesPerPixel));
            return pixel;
        }

        private void write(java.io.ByteArrayOutputStream out, byte[] pixel) {
            out.write(pixel, 0, pixel.length);
        }

        private int emit(byte[] raw, int at, java.io.ByteArrayOutputStream out, int cpxel) {
            write(out, value(raw, at, cpxel));
            return at + cpxel;
        }

        /**
         * The RFB challenge encrypted the way the protocol says, written out here rather than
         * called from the server: a test that used the server's own method would agree with it on
         * any mistake they shared. The bit reversal is the part that is easy to get wrong.
         */
        private static byte[] vncEncrypt(byte[] challenge, String password) throws IOException {
            byte[] key = new byte[8];
            byte[] bytes = password.getBytes(StandardCharsets.ISO_8859_1);
            for (int i = 0; i < 8; i++) {
                int value = i < bytes.length ? bytes[i] & 0xFF : 0;
                int reversed = 0;
                for (int bit = 0; bit < 8; bit++) {
                    reversed = (reversed << 1) | ((value >> bit) & 1);
                }
                key[i] = (byte) reversed;
            }
            try {
                javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("DES/ECB/NoPadding");
                cipher.init(javax.crypto.Cipher.ENCRYPT_MODE,
                        new javax.crypto.spec.SecretKeySpec(key, "DES"));
                return cipher.doFinal(challenge);
            } catch (java.security.GeneralSecurityException e) {
                throw new IOException(e);
            }
        }

        /** The read timeout for the next byte, on either transport. */
        void readTimeout(int millis) throws IOException {
            socket.setSoTimeout(millis);
        }

        @Override
        public void close() throws IOException {
            zrleInflater.end();
            socket.close();
        }

        // --- the wire, both directions -----------------------------------

        private int readU16() throws IOException {
            int high = in.read();
            int low = in.read();
            if ((high | low) < 0) {
                throw new EOFException("the server closed the connection");
            }
            return (high << 8) | low;
        }

        private int readInt() throws IOException {
            int a = in.read();
            int b = in.read();
            int c = in.read();
            int d = in.read();
            if ((a | b | c | d) < 0) {
                throw new EOFException("the server closed the connection");
            }
            return (a << 24) | (b << 16) | (c << 8) | d;
        }

        private byte[] readBytes(int count) throws IOException {
            byte[] bytes = new byte[count];
            in.readNBytes(bytes, 0, count);
            return bytes;
        }

        private String readAscii(int count) throws IOException {
            return new String(readBytes(count), StandardCharsets.US_ASCII);
        }

        private static void writeU16(OutputStream out, int value) throws IOException {
            out.write((value >> 8) & 0xFF);
            out.write(value & 0xFF);
        }

        private static void writeU16(byte[] bytes, int at, int value) {
            bytes[at] = (byte) (value >> 8);
            bytes[at + 1] = (byte) value;
        }

        private static void writeInt(OutputStream out, int value) throws IOException {
            out.write((value >> 24) & 0xFF);
            out.write((value >> 16) & 0xFF);
            out.write((value >> 8) & 0xFF);
            out.write(value & 0xFF);
        }
    }
}
