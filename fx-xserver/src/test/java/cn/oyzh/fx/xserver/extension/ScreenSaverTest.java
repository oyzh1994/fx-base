package cn.oyzh.fx.xserver.extension;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MIT-SCREEN-SAVER and DPMS: the two extensions a client asks before it decides whether it may blank
 * the screen or must leave it alone.
 *
 * <p>Neither was advertised, so a video player, a presentation tool or a remote-desktop client was
 * told "extension not present" - and the answer a client gets is one it acts on: the screen saver's
 * side is what says "do not blank while this plays", and DPMS is where a client reads the energy
 * settings.
 *
 * <p>The screen saver's replies were measured against a reference server (Xvfb, through
 * {@code tools/ScreenSaverProbe}): the version is 1.1, and {@code QueryInfo}'s two countdowns are real
 * numbers rather than constants. The one field that deliberately differs is the saver window, which is
 * {@code None} here: there is no saver window to name, because what would blank is the host window.
 * DPMS has **no** reference - Xvfb does not have the extension at all, which that probe also
 * measured - so its replies follow the protocol rather than a diff, and `Capable` says no because this
 * server cannot power the display down.
 */
class ScreenSaverTest {

    /** A display number nothing else in the suite uses; the server listens on TCP 6000 + it. */
    private static final int DISPLAY = 78;

    /** The screensaver extension's minor opcodes, and DPMS's, as the protocol numbers them. */
    private static final int SAVER_QUERY_VERSION = 0;
    private static final int SAVER_QUERY_INFO = 1;
    private static final int SAVER_SELECT_INPUT = 2;
    private static final int SAVER_SUSPEND = 5;
    private static final int DPMS_GET_VERSION = 0;
    private static final int DPMS_CAPABLE = 1;
    private static final int DPMS_GET_TIMEOUTS = 2;
    private static final int DPMS_SET_TIMEOUTS = 3;
    private static final int DPMS_INFO = 7;

    /** The state-change notification's mask bit, and the event kind it carries. */
    private static final int NOTIFY_MASK = 0x1;

    // The core opcodes. They were not guessed: the first version of this test used 116/117/124 from
    // memory, and a `ForceScreenSaver` at 124 is `unsupported opcode` here - the numbers in the
    // protocol library are 107/108/115, and the live check that settles it is the real `xset` against
    // the server, which is where they came from (`xset q` prints the settings and raises no error).
    private static final int SET_SCREEN_SAVER = 107;
    private static final int GET_SCREEN_SAVER = 108;
    private static final int FORCE_SCREEN_SAVER = 115;

    private XServer server;
    private RawXClient client;
    private int saver;
    private int dpms;

    @BeforeEach
    void startServerWithTheExtensions() throws Exception {
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--backend", "headless", "--no-unix"}));
        server.start();
        client = RawXClient.connect(DISPLAY);
        saver = client.queryExtension("MIT-SCREEN-SAVER");
        dpms = client.queryExtension("DPMS");
    }

    @AfterEach
    void stopServer() throws Exception {
        if (client != null) {
            client.close();
        }
        server.stop();
    }

    /** A screensaver request: the minor opcode, and up to three sixteen bit arguments. */
    private byte[] saverRequest(int minor, int... words) {
        byte[] request = new byte[Math.max(4, 4 + words.length * 4)];
        request[0] = (byte) saver;
        request[1] = (byte) minor;
        client.writeShortAt(request, 2, request.length / 4);
        for (int i = 0; i < words.length; i++) {
            client.writeIntAt(request, 4 + i * 4, words[i]);
        }
        return request;
    }

    private byte[] dpmsRequest(int minor, int... words) {
        byte[] request = new byte[Math.max(4, 4 + words.length * 4)];
        request[0] = (byte) dpms;
        request[1] = (byte) minor;
        client.writeShortAt(request, 2, request.length / 4);
        for (int i = 0; i < words.length; i++) {
            client.writeIntAt(request, 4 + i * 4, words[i]);
        }
        return request;
    }

    /** The core {@code SetScreenSaver}, which is what a toolkit's "10 minutes, no blanking" sends. */
    private void setScreenSaver(int timeout, int interval, int preferBlanking, int allowExposures)
            throws Exception {
        byte[] request = new byte[16];
        request[0] = SET_SCREEN_SAVER;
        request[2] = 4;
        client.writeIntAt(request, 4, (timeout & 0xFFFF) | ((interval & 0xFFFF) << 16));
        request[8] = (byte) preferBlanking;
        request[9] = (byte) allowExposures;
        client.sendRequest(request);
        client.flush();
    }

    private byte[] getScreenSaver() throws Exception {
        byte[] request = new byte[4];
        request[0] = GET_SCREEN_SAVER;
        request[2] = 1;
        client.sendRequest(request);
        client.flush();
        return client.readReply();
    }

    /** The core {@code ForceScreenSaver}: 1 activates, 0 resets, and both are reported to subscribers. */
    private void forceScreenSaver(int mode) throws Exception {
        // Four bytes, not eight: `ForceScreenSaver` is a mode byte and a length, which is what Xlib
        // sends and what a reference server *requires* - an eight byte one is `BadLength` there and is
        // accepted here, because this server hands an over-long request on rather than refusing it.
        // (The design notes record the same trap for `SetInputFocus`; this one was found by sending
        // the request through `xset` and through this client and comparing what the two servers said.)
        byte[] request = new byte[4];
        request[0] = FORCE_SCREEN_SAVER;
        request[1] = (byte) mode;
        request[2] = 1;
        client.sendRequest(request);
        client.flush();
    }

    /**
     * Both extensions are advertised, which is the whole of what most clients check before deciding
     * whether to ask anything else.
     */
    @Test
    void bothExtensionsAreAdvertised() {
        assertTrue(saver != 0, "MIT-SCREEN-SAVER was not advertised");
        assertTrue(dpms != 0, "DPMS was not advertised");
        assertTrue(saver != dpms, "the two extensions were given the same opcode");
    }

    /** The version is the reference's answer, 1.1, and DPMS's is the last one it ever had, 1.2. */
    @Test
    void theVersionsAreTheOnesAClientExpects() throws Exception {
        client.sendRequest(saverRequest(SAVER_QUERY_VERSION, 1 << 16));
        client.flush();
        byte[] reply = client.readReply();
        assertEquals(1, RawXClient.readShort(reply, 8), "the screen saver's major version");
        assertEquals(1, RawXClient.readShort(reply, 10), "the screen saver's minor version");

        client.sendRequest(dpmsRequest(DPMS_GET_VERSION));
        client.flush();
        reply = client.readReply();
        assertEquals(1, RawXClient.readShort(reply, 8));
        assertEquals(2, RawXClient.readShort(reply, 10));
    }

    /**
     * DPMS answers the truth about this display: it cannot be powered down, so {@code Capable} is no,
     * and the timeouts and the level a client sets are the ones it reads back.
     */
    @Test
    void dpmsIsHonestAboutWhatThisDisplayCanDo() throws Exception {
        client.sendRequest(dpmsRequest(DPMS_CAPABLE));
        client.flush();
        assertEquals(0, client.readReply()[8] & 0xFF,
                "this server cannot power the display down, and saying so is what lets a client "
                        + "skip its DPMS logic");

        byte[] setTimeouts = new byte[12];
        setTimeouts[0] = (byte) dpms;
        setTimeouts[1] = (byte) DPMS_SET_TIMEOUTS;
        RawXClient.writeShortAt(setTimeouts, 2, 3);
        RawXClient.writeShortAt(setTimeouts, 4, 60);    // standby, then suspend, then off:
        RawXClient.writeShortAt(setTimeouts, 6, 30);    // three CARD16s, not three words
        RawXClient.writeShortAt(setTimeouts, 8, 10);
        client.sendRequest(setTimeouts);
        client.flush();
        client.sendRequest(dpmsRequest(DPMS_GET_TIMEOUTS));
        client.flush();
        byte[] reply = client.readReply();
        assertEquals(60, RawXClient.readShort(reply, 8), "the standby timeout that was set");
        assertEquals(30, RawXClient.readShort(reply, 10), "the suspend timeout that was set");
        assertEquals(10, RawXClient.readShort(reply, 12), "the off timeout that was set");

        client.sendRequest(dpmsRequest(DPMS_INFO));
        client.flush();
        reply = client.readReply();
        assertEquals(0, RawXClient.readShort(reply, 8), "nothing has asked for a power level yet");
    }

    /**
     * The settings are kept and read back, rather than answered with constants: a client that sets a
     * timeout and reads it back has to see its own value, or it cannot tell its own change took.
     */
    @Test
    void theSettingsAClientSetsAreTheSettingsItReadsBack() throws Exception {
        setScreenSaver(120, 60, 0, 1);
        byte[] reply = getScreenSaver();
        assertEquals(120, RawXClient.readShort(reply, 8), "the timeout that was set");
        assertEquals(60, RawXClient.readShort(reply, 10), "the interval that was set");
        assertEquals(0, reply[12] & 0xFF, "prefer blanking was turned off and must read back off");
        assertEquals(1, reply[13] & 0xFF, "allow exposures was left on");
    }

    /**
     * {@code QueryInfo}'s countdown is real: with a one-minute timeout it reports less than a minute
     * remaining and a few seconds since the last input, which is what a client uses to decide whether
     * it has to say "do not blank" yet.
     */
    @Test
    void queryInfoCountsDownAgainstTheTimeoutThatWasSet() throws Exception {
        setScreenSaver(60, 60, 1, 1);
        client.sendRequest(saverRequest(SAVER_QUERY_INFO, client.root()));
        client.flush();
        byte[] reply = client.readReply();

        assertEquals(0, reply[1] & 0xFF, "the saver has not been forced on, so its state is Off");
        int untilServer = RawXClient.readInt(reply, 12);
        int sinceInput = RawXClient.readInt(reply, 16);
        assertTrue(untilServer > 0 && untilServer <= 60_000,
                "a one-minute timeout must leave less than a minute on the clock, not " + untilServer);
        assertTrue(sinceInput >= 0 && sinceInput < 60_000,
                "the last input was moments ago, not " + sinceInput + " ms ago");
        assertEquals(0, RawXClient.readInt(reply, 8),
                "there is no saver window here - what blanks is the host window");
    }

    /**
     * A client that subscribes is told when the state changes, and the change it can cause is
     * {@code ForceScreenSaver} - the one the server has.
     */
    @Test
    void forcingTheSaverTellsASubscriber() throws Exception {
        client.sendRequest(saverRequest(SAVER_SELECT_INPUT, client.root(), NOTIFY_MASK));
        client.flush();

        // The notification is an extension event: its type is the extension's first event code plus
        // the event's own number, which the registry assigned when the extension was registered.
        int firstEvent = client.queryExtensionInfo("MIT-SCREEN-SAVER")[2];
        forceScreenSaver(1);

        byte[] event = client.readEvent(firstEvent);
        assertNotNull(event, "the subscriber was never told the saver's state changed");
        assertEquals(1, event[1] & 0xFF, "the state reported is On");
        // The event's own layout: state at 1, the time at 4, the root at 8, the window it is
        // reported against at 12, the kind at 16 and the forced flag at 17.
        assertEquals(client.root(), RawXClient.readInt(event, 12), "the drawable it subscribed with");
        assertEquals(1, event[17] & 0xFF, "the change was forced, not the server's own doing");

        // And back off again, which is the other half of what a client watches for.
        forceScreenSaver(0);
        event = client.readEvent(firstEvent);
        assertNotNull(event, "the subscriber was not told the saver stopped");
        assertEquals(0, event[1] & 0xFF, "the state reported is Off");
    }

    /**
     * A client that did not subscribe hears nothing: the notification is not something every
     * connection is sent, and a test that skipped this would pass with the subscription ignored.
     */
    @Test
    void aClientThatDidNotSubscribeHearsNothing() throws Exception {
        int firstEvent = client.queryExtensionInfo("MIT-SCREEN-SAVER")[2];
        forceScreenSaver(1);

        client.readTimeoutMillis(200);
        assertNull(client.readEvent(firstEvent), "an event arrived for a client that never asked");
        client.readTimeoutMillis(5000);
    }

    /**
     * A client that suspends the saver is not refused, and the state it asked for is what the server
     * keeps - {@code xset s activate} and a video player's "do not blank" both go through here.
     */
    @Test
    void suspendingTheSaverIsAccepted() throws Exception {
        // A round trip between the request and the assertion: `Suspend` has no reply, so without one
        // the assertion races the server's own thread and reads the state before it was set. (It did:
        // the first version of this test failed about half the time for a reason that had nothing to
        // do with the request being handled wrongly.)
        client.sendRequest(saverRequest(SAVER_SUSPEND, 1));
        client.flush();
        getScreenSaver();
        assertTrue(server.screenSaver().suspended(), "the server did not keep the suspension");

        client.sendRequest(saverRequest(SAVER_SUSPEND, 0));
        client.flush();
        getScreenSaver();
        assertTrue(!server.screenSaver().suspended(), "the server did not release the suspension");
    }
}
