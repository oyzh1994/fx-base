package cn.oyzh.fx.xserver.extension;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DOUBLE-BUFFER (Xdbe): the buffer a client draws into before showing it, which every toolkit's
 * no-flicker path wants (F5).
 *
 * <p>The shapes pinned here are the ones {@code tools/DbeProbe} measured on a reference and none of
 * them can be read off the specification's tables: the allocate's field order (window, buffer,
 * swapAction) and its lack of a reply, {@code SwapBuffers} naming a <em>window</em> with a count of
 * (window, action) pairs, {@code GetBackBufferAttributes} being minor <b>7</b>, and the swap actions'
 * own rule - the front takes the first buffer's contents and then every buffer is left holding what
 * the action names. The action table in particular is only visible with <em>two</em> buffers on one
 * window: with one, "keeps its contents" and "is given the front's contents" are the same picture.
 */
class DbeTest {

    /** A display number the rest of the suite does not use. */
    private static final int DISPLAY = 87;

    private static final int GET_VERSION = 0;
    private static final int ALLOCATE_BACK_BUFFER = 1;
    private static final int DEALLOCATE_BACK_BUFFER = 2;
    private static final int SWAP_BUFFERS = 3;
    private static final int BEGIN_IDIOM = 4;
    private static final int END_IDIOM = 5;
    private static final int GET_VISUAL_INFO = 6;
    private static final int GET_BACK_BUFFER_ATTRIBUTES = 7;

    private static final int UNDEFINED = 0;
    private static final int BACKGROUND = 1;
    private static final int UNTOUCHED = 2;
    private static final int COPIED = 3;

    /** Core errors this test asserts on, by their own numbers. */
    private static final int BAD_REQUEST = 1;
    private static final int BAD_VALUE = 2;
    private static final int BAD_WINDOW = 3;
    private static final int BAD_MATCH = 8;
    private static final int BAD_DRAWABLE = 9;
    private static final int BAD_ID_CHOICE = 14;

    /** Colours that cannot be confused, so "which surface" is one assertion. */
    private static final int FRONT = 0x00FF00;
    private static final int BACK = 0x0000FF;
    private static final int PLANES = 0xFFFFFF;

    private static XServer server;

    @BeforeAll
    static void startServer() throws IOException {
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--backend", "headless", "--single-window", "--no-unix"}));
        server.start();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    /** The version is the server's own, and DBE defines one error and no events. */
    @Test
    void theVersionIsTheServersOwn() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int[] info = client.queryExtensionInfo("DOUBLE-BUFFER");
            assertEquals(1, info[0], "the extension is there");
            assertEquals(0, info[2], "no events, so firstEvent is zero, as a reference reports");
            assertTrue(info[3] > 0, "and one error, BadBuffer, has a base of its own");
            int opcode = info[1];
            assertArrayEquals(new int[]{1, 0}, version(client, opcode, 1, 0), "asking for 1.0");
            assertArrayEquals(new int[]{1, 0}, version(client, opcode, 0, 0), "asking for 0.0");
            assertArrayEquals(new int[]{1, 0}, version(client, opcode, 2, 0), "asking for 2.0");
        }
    }

    /**
     * The whole point of the extension: a client draws into a buffer nobody is looking at, and a
     * swap makes it the window's contents - in one step, which is what the frame presents.
     */
    @Test
    void aSwapShowsWhatWasDrawnIntoTheBuffer() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = client.queryExtensionInfo("DOUBLE-BUFFER")[1];
            int window = client.allocateId();
            client.createWindow(window, client.root(), 10, 10, 60, 40, FRONT);
            client.mapWindow(window);
            int gc = client.allocateId();
            client.createGC(gc, window, BACK);
            client.flush();
            Thread.sleep(150);
            assertEquals(FRONT, pixel(client, window, 5, 5), "the window starts as its background");

            int buffer = client.allocateId();
            // The allocate has no reply, so the evidence that it worked is the attributes below.
            client.sendRequest(allocate(opcode, window, buffer, COPIED));
            client.sendRequest(attributes(opcode, buffer));
            byte[] reply = client.readReply();
            assertEquals(window, RawXClient.readInt(reply, 8), "the buffer names its window");
            assertNotEquals(0, buffer, "and the client chose its id");

            // A back buffer is a drawable: the ordinary requests address it by its own id, and what
            // is drawn into it does not reach the window until a swap.
            client.fillRectangle(buffer, gc, 0, 0, 60, 40);
            client.flush();
            Thread.sleep(50);
            assertEquals(FRONT, pixel(client, window, 5, 5), "drawing into the buffer is not showing");
            assertEquals(BACK, pixel(client, buffer, 5, 5), "but the buffer holds it");

            client.sendRequest(swap(opcode, window, COPIED));
            client.sendRequest(attributes(opcode, buffer));      // a fence: the swap is done
            client.readReply();
            assertEquals(BACK, pixel(client, window, 5, 5), "and the swap makes it the window's");
            assertEquals(BACK, pixel(client, buffer, 5, 5), "Copied leaves the buffer as it was");
        }
    }

    /**
     * What each swap action leaves behind, which takes <em>two</em> buffers on one window to see: the
     * front takes the first allocated buffer's contents, and then every buffer of that window holds
     * what the action names.
     */
    @Test
    void eachActionLeavesTheBufferAsItNames() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = client.queryExtensionInfo("DOUBLE-BUFFER")[1];
            int window = client.allocateId();
            client.createWindow(window, client.root(), 10, 10, 60, 40, FRONT);
            client.mapWindow(window);
            int gc = client.allocateId();
            client.createGC(gc, window, 0xFFFFFF);
            int first = client.allocateId();
            int second = client.allocateId();
            client.sendRequest(allocate(opcode, window, first, COPIED));
            client.sendRequest(allocate(opcode, window, second, COPIED));
            client.flush();
            Thread.sleep(150);

            for (int action : new int[]{COPIED, UNDEFINED, BACKGROUND, UNTOUCHED}) {
                paint(client, gc, second, 0x500000);
                paint(client, gc, first, 0x600000);
                paint(client, gc, window, 0x700000);
                client.flush();
                Thread.sleep(30);
                client.sendRequest(swap(opcode, window, action));
                client.sendRequest(attributes(opcode, first));
                client.readReply();

                assertEquals(0x600000, pixel(client, window, 5, 5),
                        "the front takes the first allocated buffer's contents, whatever the action");
                int expected = action == UNTOUCHED ? 0x700000
                        : action == BACKGROUND ? FRONT : 0x600000;
                assertEquals(expected, pixel(client, first, 5, 5),
                        "the first buffer holds what " + action + " names");
                assertEquals(expected, pixel(client, second, 5, 5),
                        "and so does every other buffer of that window");
            }
        }
    }

    /** A buffer follows its window: a resize reallocates it and fills it with the background. */
    @Test
    void aBufferFollowsItsWindow() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = client.queryExtensionInfo("DOUBLE-BUFFER")[1];
            int window = client.allocateId();
            client.createWindow(window, client.root(), 10, 10, 60, 40, FRONT);
            client.mapWindow(window);
            int gc = client.allocateId();
            client.createGC(gc, window, 0xFFFFFF);
            int buffer = client.allocateId();
            client.sendRequest(allocate(opcode, window, buffer, COPIED));
            paint(client, gc, buffer, BACK);
            client.flush();
            Thread.sleep(150);
            assertEquals(BACK, pixel(client, buffer, 5, 5), "the buffer holds what was drawn");

            client.configureWindow(window, 10, 10, 80, 50);
            client.flush();
            Thread.sleep(150);

            // Outside the old 60x40: a pixmap that had not followed would refuse the read.
            assertEquals(FRONT, pixel(client, buffer, 70, 45),
                    "the buffer grew with its window, and the new part is the window's background");
            assertEquals(FRONT, pixel(client, buffer, 5, 5),
                    "and the resize threw the buffer's contents away, as a reference does");
            client.sendRequest(attributes(opcode, buffer));
            assertEquals(window, RawXClient.readInt(client.readReply(), 8),
                    "and it is still the same buffer, under the same id");
        }
    }

    /** Every refusal the extension has, each named by the error a client can act on. */
    @Test
    void theRefusalsAreTheProtocols() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int[] info = client.queryExtensionInfo("DOUBLE-BUFFER");
            int opcode = info[1];
            int window = client.allocateId();
            client.createWindow(window, client.root(), 10, 10, 40, 30, FRONT);
            client.mapWindow(window);
            int bare = client.allocateId();
            client.createWindow(bare, client.root(), 100, 100, 20, 20, FRONT);
            client.mapWindow(bare);
            client.flush();

            // A window that never had a buffer allocated on it.
            client.sendRequest(swap(opcode, bare, COPIED));
            assertEquals(BAD_MATCH, refusal(client), "a swap for a window with no buffers");

            // An action that is not one of the four, on the allocate and on the swap.
            client.sendRequest(allocate(opcode, window, client.allocateId(), 99));
            assertEquals(BAD_VALUE, refusal(client), "an undefined action at the allocate");
            client.sendRequest(swap(opcode, window, 99));
            assertEquals(BAD_VALUE, refusal(client), "and at the swap");

            // An id that is not a window, and one that is already in use.
            client.sendRequest(allocate(opcode, client.allocateId(), client.allocateId(), COPIED));
            assertEquals(BAD_WINDOW, refusal(client), "a buffer allocated on an id that is not a window");
            int buffer = client.allocateId();
            client.sendRequest(allocate(opcode, window, buffer, COPIED));
            client.sendRequest(attributes(opcode, buffer));
            client.readReply();
            client.sendRequest(allocate(opcode, window, buffer, COPIED));
            assertEquals(BAD_ID_CHOICE, refusal(client),
                    "an id that is already a resource is the client's to get right");

            // The extension's own error: an id that is not a buffer of this client.
            client.sendRequest(deallocate(opcode, client.allocateId()));
            assertEquals(info[3], refusal(client), "freeing something that is not a buffer");
            client.sendRequest(deallocate(opcode, buffer));
            client.sendRequest(deallocate(opcode, buffer));
            assertEquals(info[3], refusal(client), "and freeing one twice");

            // But *asking* about an id is a question, and a reference answers it with zero rather
            // than raising - which is what the probe measured, and what a client checks first.
            client.sendRequest(attributes(opcode, client.allocateId()));
            assertEquals(0, RawXClient.readInt(client.readReply(), 8),
                    "an id that is not a buffer has no window");
        }
    }

    /**
     * A second client: a window is not private in X, so it can swap one - but a back buffer belongs
     * to the client that allocated it, which cannot see or free somebody else's.
     */
    @Test
    void anotherClientCanSwapTheWindowButNotTouchTheBuffer() throws Exception {
        try (RawXClient owner = RawXClient.connect(DISPLAY);
             RawXClient other = RawXClient.connect(DISPLAY)) {
            int[] info = owner.queryExtensionInfo("DOUBLE-BUFFER");
            int opcode = info[1];
            int window = owner.allocateId();
            owner.createWindow(window, owner.root(), 10, 10, 60, 40, FRONT);
            owner.mapWindow(window);
            int gc = owner.allocateId();
            owner.createGC(gc, window, BACK);
            int buffer = owner.allocateId();
            owner.sendRequest(allocate(opcode, window, buffer, COPIED));
            owner.fillRectangle(buffer, gc, 0, 0, 60, 40);
            owner.flush();
            Thread.sleep(150);

            other.sendRequest(attributes(opcode, buffer));
            assertEquals(0, RawXClient.readInt(other.readReply(), 8),
                    "another client's buffer has no window as far as this one is concerned");
            other.sendRequest(deallocate(opcode, buffer));
            assertEquals(info[3], refusal(other), "and it cannot free it either");

            other.sendRequest(swap(opcode, window, COPIED));
            other.sendRequest(attributes(opcode, buffer));          // a fence
            owner.sendRequest(attributes(opcode, buffer));
            owner.readReply();
            assertEquals(BACK, pixel(owner, window, 5, 5), "but it can swap the window");
        }
    }

    /** The visuals a client can double buffer on, in the reply shape a client actually parses. */
    @Test
    void visualInfoDescribesTheScreen() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int[] info = client.queryExtensionInfo("DOUBLE-BUFFER");
            int opcode = info[1];
            client.sendRequest(visualInfo(opcode, new int[]{client.root(), client.root()}));
            byte[] reply = client.readReply();
            int screens = RawXClient.readInt(reply, 8);
            assertEquals(2, screens, "a count of the drawables answered");
            int at = 32;
            for (int i = 0; i < screens; i++) {
                int visuals = RawXClient.readInt(reply, at);
                at += 4;
                assertTrue(visuals > 0, "a screen with no visuals cannot be double buffered at all");
                // The records are eight bytes each: visualID, depth, perfLevel and two of padding.
                assertEquals(24, reply[at + 4] & 0xFF, "the first visual is the 24 bit one");
                assertEquals(0, reply[at + 5] & 0xFF, "and a software server's swap costs nothing");
                at += visuals * 8;
            }
            assertEquals(at, 32 + RawXClient.readInt(reply, 4) * 4,
                    "the length field counts exactly the sections, which is what a client walks");

            client.sendRequest(visualInfo(opcode, new int[]{client.allocateId()}));
            assertEquals(BAD_DRAWABLE, refusal(client), "an id that is not a drawable");
        }
    }

    /** A minor opcode the extension does not have is refused, not ignored. */
    @Test
    void anUnknownRequestIsRefused() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = client.queryExtensionInfo("DOUBLE-BUFFER")[1];
            client.sendRequest(new byte[]{(byte) opcode, 8, 1, 0});
            assertEquals(BAD_REQUEST, refusal(client), "a request this extension does not define");
        }
    }

    /** The idioms are hints that cost nothing: a reference accepts them and does not act on them. */
    @Test
    void theIdiomsAreAccepted() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = client.queryExtensionInfo("DOUBLE-BUFFER")[1];
            client.sendRequest(idiom(opcode, BEGIN_IDIOM));
            client.sendRequest(idiom(opcode, END_IDIOM));
            client.sendRequest(idiom(opcode, END_IDIOM));
            // A fence: if any of the three had been refused, this reply would be preceded by its
            // error and the read below would take the error for the answer.
            client.sendRequest(versionRequest(opcode, 1, 0));
            byte[] reply = client.readReply();
            assertArrayEquals(new int[]{1, 0}, new int[]{reply[8] & 0xFF, reply[9] & 0xFF},
                    "nothing was refused");
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static int[] version(RawXClient client, int opcode, int major, int minor)
            throws IOException {
        client.sendRequest(versionRequest(opcode, major, minor));
        byte[] reply = client.readReply();
        return new int[]{reply[8] & 0xFF, reply[9] & 0xFF};
    }

    private static byte[] versionRequest(int opcode, int major, int minor) {
        return new byte[]{(byte) opcode, GET_VERSION, 2, 0, (byte) major, (byte) minor, 0, 0};
    }

    private static byte[] allocate(int opcode, int window, int buffer, int action) {
        byte[] request = new byte[16];
        request[0] = (byte) opcode;
        request[1] = ALLOCATE_BACK_BUFFER;
        RawXClient.writeShortAt(request, 2, 4);
        RawXClient.writeIntAt(request, 4, window);
        RawXClient.writeIntAt(request, 8, buffer);
        request[12] = (byte) action;
        return request;
    }

    private static byte[] deallocate(int opcode, int buffer) {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = DEALLOCATE_BACK_BUFFER;
        RawXClient.writeShortAt(request, 2, 2);
        RawXClient.writeIntAt(request, 4, buffer);
        return request;
    }

    /** A swap of one window: a count, then the window and its action. */
    private static byte[] swap(int opcode, int window, int action) {
        byte[] request = new byte[16];
        request[0] = (byte) opcode;
        request[1] = SWAP_BUFFERS;
        RawXClient.writeShortAt(request, 2, 4);
        RawXClient.writeIntAt(request, 4, 1);
        RawXClient.writeIntAt(request, 8, window);
        request[12] = (byte) action;
        return request;
    }

    private static byte[] attributes(int opcode, int buffer) {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = GET_BACK_BUFFER_ATTRIBUTES;
        RawXClient.writeShortAt(request, 2, 2);
        RawXClient.writeIntAt(request, 4, buffer);
        return request;
    }

    private static byte[] visualInfo(int opcode, int[] drawables) {
        byte[] request = new byte[8 + drawables.length * 4];
        request[0] = (byte) opcode;
        request[1] = GET_VISUAL_INFO;
        RawXClient.writeShortAt(request, 2, 2 + drawables.length);
        RawXClient.writeIntAt(request, 4, drawables.length);
        for (int i = 0; i < drawables.length; i++) {
            RawXClient.writeIntAt(request, 8 + i * 4, drawables[i]);
        }
        return request;
    }

    private static byte[] idiom(int opcode, int which) {
        return new byte[]{(byte) opcode, (byte) which, 1, 0};
    }

    /** The error code of the refusal that is waiting, which is the answer to a one-way request. */
    private static int refusal(RawXClient client) throws IOException {
        byte[] error = client.readBytes(32);
        assertEquals(0, error[0] & 0xFF, "the next message is an error, not a reply or an event");
        return error[1] & 0xFF;
    }

    private static void paint(RawXClient client, int gc, int drawable, int colour)
            throws IOException {
        client.changeGC(gc, RawXClient.GCFOREGROUND, colour);
        client.fillRectangle(drawable, gc, 0, 0, 60, 40);
    }

    private static int pixel(RawXClient client, int drawable, int x, int y) throws IOException {
        byte[] image = client.getImage(drawable, x, y, 1, 1, PLANES);
        return ((image[34] & 0xFF) << 16) | ((image[33] & 0xFF) << 8) | (image[32] & 0xFF);
    }
}
