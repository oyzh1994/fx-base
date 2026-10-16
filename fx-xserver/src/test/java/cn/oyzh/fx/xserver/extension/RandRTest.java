package cn.oyzh.fx.xserver.extension;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RandR: the screen, as a modern client asks about it.
 *
 * <p>Without this extension a browser or an Electron application cannot work out the monitor's
 * geometry or its device pixel ratio, a HiDPI-aware toolkit has nothing to read, and a screen that
 * grows has no way to say so: the core protocol's only answer is a {@code ConfigureNotify} for the
 * root, which is not what any modern client listens for.
 *
 * <p>The model is one CRTC, one output and one mode covering the whole screen, and **every number
 * here was measured against a reference server**: {@code tools/RandrProbe} prints the same fields out
 * of Xvfb and out of this server, and they agree field for field - the ids, the mode, the sync
 * geometry, the rotation bits, the counts. The two places they deliberately differ are in the numbers
 * themselves and are asserted as such below: this server reports a real physical size where Xvfb
 * reports none, and its size range does not reach down to 1x1 because this screen never shrinks.
 *
 * <p>The last test is the one that matters to a real client: a screen that grew has to <em>tell</em>
 * the clients that asked, which is what {@code ScreenChangeNotify} is for and what C1's screen growth
 * had no way to do before this extension existed.
 */
class RandRTest {

    /** A display number nothing else in the suite uses; the server listens on TCP 6000 + it. */
    private static final int DISPLAY = 75;

    private static final int QUERY_VERSION = 0;
    private static final int SELECT_INPUT = 4;
    private static final int GET_SCREEN_SIZE_RANGE = 6;
    private static final int GET_SCREEN_RESOURCES = 8;
    /** {@code GetPanning}: RandR 1.3, minor 28 - measured, and the request xrandr dies on. */
    private static final int GET_PANNING = 28;
    private static final int GET_OUTPUT_INFO = 9;
    private static final int GET_CRTC_INFO = 20;
    private static final int GET_CRTC_GAMMA_SIZE = 22;
    private static final int GET_CRTC_GAMMA = 23;
    private static final int GET_MONITORS = 42;
    private static final int LIST_OUTPUT_PROPERTIES = 10;
    private static final int GET_OUTPUT_PROPERTY = 15;
    private static final int GET_PROVIDERS = 32;

    /** {@code SelectInput}'s screen-change bit. */
    private static final int SCREEN_CHANGE = 0x1;

    /** The 16.16 fixed point one of a transform matrix; the identity is this on the diagonal. */
    private static final int ONE = 1 << 16;

    private XServer server;
    private RawXClient client;
    private int randr;

    @BeforeEach
    void startServer() throws Exception {
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                // Single window, so the client's window is on the master screen - which is the
                // screen the drag grows, and therefore the one a claim about the screen is about.
                "--geometry", "600x400", "--max-screen", "900x600", "--backend", "headless",
                "--single-window", "--no-unix"}));
        server.start();
        // The display tick, which is what applies a drag (and therefore what grows the screen): a
        // test that only needs request handling leaves it off, and one that measures a change over
        // time cannot.
        server.startEventLoop();
        client = RawXClient.connect(DISPLAY);
        randr = client.queryExtension("RANDR");
    }

    @AfterEach
    void stopServer() {
        if (client != null) {
            try {
                client.close();
            } catch (IOException ignored) {
                // A test that already failed is the one that notices.
            }
        }
        server.stop();
    }

    /** {@code SelectInput}, which has no reply: the subscription is registered when it is read. */
    private void selectInput(int window, int mask) throws IOException {
        byte[] request = new byte[12];
        request[0] = (byte) randr;
        request[1] = (byte) SELECT_INPUT;
        RawXClient.writeShortAt(request, 2, 3);
        RawXClient.writeIntAt(request, 4, window);
        RawXClient.writeShortAt(request, 8, mask);
        client.sendRequest(request);
        client.flush();
        // A reply-carrying request after it, so the subscription is certainly in place first.
        client.queryExtension("RANDR");
    }

    /** A RandR request: the minor opcode, then however many four byte words the caller has. */
    private byte[] request(int minor, int units, int... arguments) throws IOException {
        byte[] request = new byte[units * 4];
        request[0] = (byte) randr;
        request[1] = (byte) minor;
        RawXClient.writeShortAt(request, 2, units);
        for (int i = 0; i < arguments.length; i++) {
            RawXClient.writeIntAt(request, 4 + i * 4, arguments[i]);
        }
        client.sendRequest(request);
        client.flush();
        return client.readReply();
    }

    /** The CRTC and output ids the server hands out, which a client passes back to it. */
    /**
     * Panning is <em>answered</em>, and with zeros.
     *
     * <p>Both halves are measured. An error for this request is where {@code xrandr} dies in its own
     * library rather than reporting the display as un-pannable; and naming the whole screen as the
     * panning area - which is what this answered until 2026-09-28 - makes every {@code xrandr
     * --verbose} print `Panning: 1300x800+0+0` and `Tracking: ...` lines that a reference's output
     * does not have. A reference answers zeros.
     */
    @Test
    void panningIsAnsweredWithNothingToPan() throws IOException {
        int crtc = ids()[0];
        byte[] reply = request(GET_PANNING, 2, crtc);
        assertEquals(0, reply[1] & 0xFF, "the CRTC this server named is answered about");
        for (int at = 12; at <= 34; at += 2) {
            assertEquals(0, RawXClient.readShort(reply, at),
                    "no panning, no track area and no border (field at offset " + at + ")");
        }
    }

    private int[] ids() throws IOException {
        byte[] reply = request(GET_SCREEN_RESOURCES, 2, client.root());
        assertEquals(1, RawXClient.readShort(reply, 16), "one CRTC");
        assertEquals(1, RawXClient.readShort(reply, 18), "one output");
        assertEquals(1, RawXClient.readShort(reply, 20), "one mode");
        // The lists start at 32: the CRTC ids first, then the output ids, then the modes.
        return new int[] {RawXClient.readInt(reply, 32), RawXClient.readInt(reply, 36),
                RawXClient.readInt(reply, 40)};
    }

    /** The extension is there, and the version it answers is the one the client asked about. */
    @Test
    void itIsOfferedAndAnswersTheClientsOwnVersion() throws IOException {
        assertTrue(randr != 0, "a client that asks has to be told the extension is there");

        // 3 units: the client's major and minor, four bytes each. A client that asks for less than
        // this server has is answered in kind, which is what the reply's own version means.
        byte[] reply = request(QUERY_VERSION, 3, 1, 6);
        assertEquals(1, RawXClient.readInt(reply, 8), "the major version");
        assertEquals(6, RawXClient.readInt(reply, 12), "the minor version this server has");

        reply = request(QUERY_VERSION, 3, 1, 3);
        assertEquals(1, RawXClient.readInt(reply, 8));
        assertEquals(3, RawXClient.readInt(reply, 12),
                "a client that asks for 1.3 is told 1.3, not 1.6: the fields it then reads are the "
                        + "ones its version has");
    }

    /**
     * The output covers the whole screen, and every id a client is given answers a question.
     */
    @Test
    void oneOutputOnOneCrtcCoversTheWholeScreen() throws IOException {
        int[] ids = ids();
        int crtc = ids[0];
        int output = ids[1];

        // The output: connected, on the CRTC the list named, with the screen's own physical size,
        // offering one mode.
        byte[] info = request(GET_OUTPUT_INFO, 3, output, 0);
        assertEquals(0, info[1] & 0xFF, "the output the server names has to be answered about");
        assertEquals(crtc, RawXClient.readInt(info, 12), "the output is on that CRTC");
        assertEquals(server.screen().widthMillimeters(), RawXClient.readInt(info, 16),
                "the physical size is the one the core setup reply reports - a client computing a "
                        + "DPI reads both, and two answers that disagree are worse than none");
        assertEquals(server.screen().heightMillimeters(), RawXClient.readInt(info, 20),
                "the physical height, by the same rule");
        assertEquals(0, info[24] & 0xFF, "the output is connected");
        // The counts, at the offsets the protocol puts them at: **numCrtcs, numModes, numClones,
        // numPreferred, nameLen** - in that order, which is the order a reference's own bytes have
        // and *not* the order the generated reply class writes them in (it puts numPreferred before
        // numClones, which shifts every count after it and told a client the output had no modes at
        // all: xrandr warned "has no modes" and printed the mode without its `*current` marker).
        assertEquals(1, RawXClient.readShort(info, 26), "the output is on one CRTC");
        assertEquals(1, RawXClient.readShort(info, 28), "and it offers the one mode the CRTC shows");
        assertEquals(0, RawXClient.readShort(info, 30), "no clones");
        assertEquals(0, RawXClient.readShort(info, 32), "no preferred mode, as a reference answers");
        assertEquals("screen".length(), RawXClient.readShort(info, 34), "and its name is its name");
        assertEquals(crtc, RawXClient.readInt(info, 36), "the crtc array holds that CRTC");
        assertEquals(ids[2], RawXClient.readInt(info, 40), "the mode array holds that mode");

        // The CRTC: the whole screen, with that mode.
        info = request(GET_CRTC_INFO, 3, crtc, 0);
        assertEquals(0, info[1] & 0xFF, "the CRTC the server named has to be answered about");
        assertEquals(0, RawXClient.readShort(info, 12), "x");
        assertEquals(0, RawXClient.readShort(info, 14), "y");
        assertEquals(600, RawXClient.readShort(info, 16), "the width is the screen's");
        assertEquals(400, RawXClient.readShort(info, 18), "the height is the screen's");
        assertEquals(1, RawXClient.readShort(info, 24), "rotation bits: the identity, which is 1");
        assertEquals(1, RawXClient.readShort(info, 28), "one output on it");
    }

    /**
     * The screen's size range. The minimum is the size it is, not one pixel: this screen grows and
     * never shrinks, so a client cannot ask for anything smaller - and a client that reads the range
     * to size itself should not be told a number it will never get.
     */
    @Test
    void theScreenSizeRangeIsWhatThisScreenCanBe() throws IOException {
        byte[] reply = request(GET_SCREEN_SIZE_RANGE, 2, client.root());
        // The screen is 600x400 here: the range says so at both ends, because this screen grows and
        // never shrinks - a client cannot ask for anything smaller than it already has.
        assertEquals(600, RawXClient.readShort(reply, 8), "minimum width");
        assertEquals(400, RawXClient.readShort(reply, 10), "minimum height");
        assertEquals(600, RawXClient.readShort(reply, 12), "maximum width: --max-screen is not reported here");
        assertEquals(400, RawXClient.readShort(reply, 14), "maximum height");
    }

    /**
     * The gamma ramp is the identity - a client reads it to see what the display does with a colour,
     * and "nothing" is the honest answer for a screen whose colour is the platform's.
     */
    @Test
    void theGammaRampIsTheIdentity() throws IOException {
        int[] ids = ids();
        byte[] reply = request(GET_CRTC_GAMMA_SIZE, 2, ids[0]);
        assertEquals(256, RawXClient.readShort(reply, 8), "a ramp a client can read");

        reply = request(GET_CRTC_GAMMA, 2, ids[0]);
        assertEquals(256, RawXClient.readShort(reply, 8), "red entries");
        // The ramps start at 32, each padded to a four byte boundary: red, then green, then blue.
        assertEquals(0, RawXClient.readShort(reply, 32), "the first entry maps black to black");
        assertEquals(255, RawXClient.readShort(reply, 32 + 255 * 2) >> 8,
                "and the last maps white to white");
    }

    /**
     * The whole screen is one monitor.
     *
     * <p>This is what {@code xrandr --listmonitors} prints, and the reply has two counts that are not
     * the same thing: how many monitors there are, and how many outputs the array carries. The second
     * one was left at zero when this was written, and a client that reads it to know how far the array
     * reaches died in its own library rather than printing anything - so it is pinned here, and so is
     * the monitor's name being a real atom rather than {@code None}.
     */
    @Test
    void theWholeScreenIsOneMonitor() throws IOException {
        // GetMonitors: the window, then get-active - a three unit request.
        byte[] reply = request(GET_MONITORS, 3, client.root(), 1);
        assertEquals(1, RawXClient.readInt(reply, 12), "one monitor");
        assertEquals(1, RawXClient.readInt(reply, 16),
                "the reply says how many outputs its array carries");
        // The array starts at 32: the reply has twelve bytes of padding before it.
        assertEquals(client.internAtom("screen"), RawXClient.readInt(reply, 32),
                "the monitor's name is an atom a client can resolve");
        assertEquals(0, reply[36] & 0xFF, "not the primary monitor: there is only one");
        assertEquals(1, reply[37] & 0xFF, "and it was made automatically, not by a client");
        assertEquals(1, RawXClient.readShort(reply, 38), "it covers the one output");
        assertEquals(600, RawXClient.readShort(reply, 44), "its width is the screen's");
        assertEquals(400, RawXClient.readShort(reply, 46), "its height is the screen's");
        assertEquals(server.screen().widthMillimeters(), RawXClient.readInt(reply, 48),
                "and its physical size is the one the core reply reports");
        assertEquals(server.screen().heightMillimeters(), RawXClient.readInt(reply, 52));
    }

    /**
     * The test that matters to a client: a screen that grew has to tell the clients that asked.
     *
     * <p>This is the half of C1 that was missing - the screen could grow, and a client that lays
     * itself out against the display had no way to hear about it.
     */
    @Test
    void aClientThatSelectedTheScreenChangeIsToldWhenTheScreenGrows() throws Exception {
        // A window for the growth to size to: the drag asks for a screen big enough for the client's
        // windows, so a session with nothing mapped has nothing to grow to (which is what the first
        // version of this test measured, and it read as "the event never came").
        mapWindow(0, 0, 500, 300);
        // SelectInput answers with nothing, so it is sent without a reply to read - and the round
        // trip that follows is what makes sure the server has processed it before the drag.
        selectInput(client.root(), SCREEN_CHANGE);
        int firstEvent = client.queryExtensionInfo("RANDR")[2];

        drag(800, 600);
        // The change has to have happened before the event about it can: the tick applies the drag,
        // and the server's own screen is where that shows. (Read through the server rather than with
        // a request, because a request's reply reader drops the events that arrived before it.)
        long deadline = System.currentTimeMillis() + 4000;
        while (server.screen().width() == 600 && System.currentTimeMillis() < deadline) {
            Thread.sleep(20);
        }
        assertEquals(800, server.screen().width(), "the screen did not grow, so there is nothing to "
                + "be told about - check the drag, not the notification");

        byte[] event = null;
        deadline = System.currentTimeMillis() + 4000;
        while (event == null && System.currentTimeMillis() < deadline) {
            event = client.readEvent(firstEvent);
        }
        assertNotNull(event, "the client asked to be told the screen changed and was not");
        assertEquals(800, RawXClient.readShort(event, 24), "the width it changed to");
        assertEquals(600, RawXClient.readShort(event, 26), "the height it changed to");
        assertEquals(client.root(), RawXClient.readInt(event, 16),
                "reported against the window the client subscribed with");
        assertEquals(1, event[1] & 0xFF, "rotation bits: the identity");
    }

    /** And a client that did not ask hears nothing, which is what makes the test above mean something. */
    @Test
    void aClientThatDidNotSelectHearsNothing() throws IOException {
        mapWindow(0, 0, 500, 300);
        int firstEvent = client.queryExtensionInfo("RANDR")[2];
        drag(800, 600);

        client.readTimeoutMillis(400);
        assertNull(client.readEvent(firstEvent), "an event arrived for a client that never asked");
        client.readTimeoutMillis(5000);
    }

    /**
     * The three questions a client asks about things this server does not have, each answered the
     * way a reference server answers it rather than refused.
     *
     * <p>All three were found by measuring a real client and a reference server (2026-09-26):
     * JavaFX asks for an output property as it starts - that is how a toolkit gets at a monitor's
     * EDID - and the answer here was "not implemented", which tells the client the feature is
     * missing rather than the monitor having no EDID. Xvfb's own output has one property
     * ({@code non-desktop}) and no EDID either, and it answers a property it does not have with
     * {@code BadName}; {@code xrandr --listproviders} against it prints {@code number : 0}.
     */
    @Test
    void theThingsThisServerDoesNotHaveAreAnsweredRatherThanRefused() throws IOException {
        int[] ids = ids();

        byte[] reply = request(LIST_OUTPUT_PROPERTIES, 2, ids[1]);
        assertEquals(0, RawXClient.readShort(reply, 8), "no properties on the output");

        // GetOutputProperty, for a property the output does not have. The request is seven units:
        // output, property, type, long offset, long length, then delete/pending/pad in one word.
        reply = request(GET_OUTPUT_PROPERTY, 7, ids[1], edid(), 0, 0, 0xFFFFFFFF, 0);
        // **An empty reply**, not an error, and that is measured against the reference: Xvfb
        // answers format 0, type None, no items and nothing after it for a property that is not on
        // the output. The first version of this refused it with BadName - read off Xorg's source,
        // and wrong - which is a protocol error where the client expects an answer.
        assertEquals(0, reply[1] & 0xFF, "format 0: no such property");
        assertEquals(0, RawXClient.readInt(reply, 8), "type None");
        assertEquals(0, RawXClient.readInt(reply, 12), "nothing after it");
        assertEquals(0, RawXClient.readInt(reply, 16), "and no items");

        // GetProviders, the other question a client asks before it looks for a provider's
        // properties. A reference server with no GPU driver answers zero providers.
        reply = request(GET_PROVIDERS, 2, client.root());
        assertEquals(0, RawXClient.readShort(reply, 12), "no providers");
    }

    /** The atom a toolkit asks an output for, which is the one this server does not have. */
    private int edid() {
        return server.atoms().intern("EDID");
    }

    /** A window bigger than the screen, which is what a drag grows the screen to fit. */
    private void mapWindow(int x, int y, int width, int height) throws IOException {
        int window = client.allocateId();
        client.createWindow(window, client.root(), x, y, width, height, 0x00FF00);
        client.mapWindow(window);
        client.flush();
    }

    /** The user dragging the host window's edge, which the display tick picks up and applies. */
    private void drag(int width, int height) {
        server.inputFor(server.screen()).windowResized(width, height);
    }
}
