package cn.oyzh.fx.xserver.input;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * What a keyboard grab does to the focus, and what the clients are told about it.
 *
 * <p>A grab is not only a change of where keys are delivered: the client that took it is told the
 * focus is on the window it grabbed, and told again when the grab ends. The event's {@code mode}
 * says which - {@code NotifyGrab} and {@code NotifyUngrab} for the two ends, and
 * {@code NotifyWhileGrabbed} for a change asked for while the grab is held. A toolkit reads the mode
 * to tell "my window lost the focus because somebody grabbed the keyboard" from "because the user
 * clicked elsewhere", which decides whether a menu or a dialog keeps running its own focus handling.
 *
 * <p>This server reported {@code NotifyNormal} for all of it and sent nothing at the ends of a grab,
 * which is invisible until a client is watched closely.
 *
 * <p>Every expectation here was measured against a reference server for the same requests, with
 * {@code tools/FocusProbe} (backlog item A6). Two of them prose does not mention, and both were
 * surprises worth pinning: a grab taken on the window that already has the focus still reports the
 * pair, and a grab taken while the focus is on nothing at all reports nothing - not even to the
 * window it grabbed.
 */
class KeyboardGrabFocusTest {

    /** A display number nothing else in the suite uses; the server listens on TCP 6000 + it. */
    private static final int DISPLAY = 83;

    private static final int FOCUS_IN = 9;
    private static final int FOCUS_OUT = 10;

    private static final int FOCUS_CHANGE_MASK = 1 << 21;

    private static final int NONE = 0;
    private static final int REVERT_TO_POINTER_ROOT = 1;

    /** Details, numbered as the protocol numbers them. */
    private static final int NONLINEAR = 3;

    /** Modes, numbered as the protocol numbers them. */
    private static final int NOTIFY_NORMAL = 0;
    private static final int NOTIFY_GRAB = 1;
    private static final int NOTIFY_UNGRAB = 2;
    private static final int NOTIFY_WHILE_GRABBED = 3;

    /** How long to wait before deciding that nothing more is coming. */
    private static final int QUIET_MILLIS = 200;

    private XServer server;
    private RawXClient client;
    private int first;
    private int second;

    @BeforeEach
    void startServerAndTwoWindows() throws Exception {
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--backend", "headless", "--no-unix"}));
        server.start();
        client = RawXClient.connect(DISPLAY);
        first = client.allocateId();
        second = client.allocateId();
        client.createWindow(first, client.root(), 10, 10, 300, 300);
        client.createWindow(second, client.root(), 400, 10, 300, 300);
        client.selectInput(first, FOCUS_CHANGE_MASK);
        client.selectInput(second, FOCUS_CHANGE_MASK);
        client.mapWindow(first);
        client.mapWindow(second);
        client.setInputFocus(first, REVERT_TO_POINTER_ROOT);
        client.flush();
        quiet();
    }

    @AfterEach
    void stopServer() throws Exception {
        if (client != null) {
            client.close();
        }
        server.stop();
    }

    /**
     * Reads and drops whatever is left on the connection, so a test only reads what its own request
     * produced. A focus change sends a chain of events rather than one, and reading a fixed number
     * of them is what makes a test read a stale one.
     */
    private void quiet() throws Exception {
        client.readTimeoutMillis(QUIET_MILLIS);
        while (client.readEvent() != null) {
            // Quiet is the end of it. A reply left in the stream is dropped here too, which is how
            // the two grab requests' replies are consumed.
        }
        client.readTimeoutMillis(5000);
    }

    /**
     * One event, with the window it names and the fields this class is about: a focus event's
     * detail is at byte 1 and its mode at byte 8.
     */
    private record Focus(int type, int window, int detail, int mode) {
    }

    private Focus readFocus(int type) throws Exception {
        byte[] event = client.readEvent();
        assertNotNull(event, type == FOCUS_IN ? "no FocusIn arrived" : "no FocusOut arrived");
        assertEquals(type, event[0] & 0xFF, "the event that arrived was not a focus event");
        return new Focus(type, RawXClient.readInt(event, 4), event[1] & 0xFF, event[8] & 0xFF);
    }

    /**
     * The grab's own reply, which is written **before** the focus events it causes - on both this
     * server and the reference, measured with {@code tools/FocusProbe}. Reading the events first
     * finds the reply, so the order here is the order they arrive in.
     */
    private void assertGrabTaken() throws Exception {
        byte[] reply = client.readEvent();
        assertNotNull(reply, "no answer to GrabKeyboard");
        assertEquals(1, reply[0] & 0xFF, "the answer to GrabKeyboard was not a reply");
        assertEquals(0, reply[1] & 0xFF, "the grab was refused");
    }

    /**
     * The grab takes the focus, and both ends are told with the mode that says a grab did it.
     */
    @Test
    void aKeyboardGrabMovesTheFocusAndSaysSo() throws Exception {
        client.sendGrabKeyboard(second, false);
        client.flush();
        assertGrabTaken();

        Focus out = readFocus(FOCUS_OUT);
        assertEquals(first, out.window(), "the window that had the keyboard was not the one told");
        assertEquals(NONLINEAR, out.detail(), "the two windows are unrelated");
        assertEquals(NOTIFY_GRAB, out.mode(), "the grab took the focus and the client was not told");

        Focus in = readFocus(FOCUS_IN);
        assertEquals(second, in.window(), "the window the keyboard was grabbed on was not told");
        assertEquals(NONLINEAR, in.detail(), "the two windows are unrelated");
        assertEquals(NOTIFY_GRAB, in.mode(), "the grab took the focus and the client was not told");

        client.sendUngrabKeyboard();
        client.flush();

        out = readFocus(FOCUS_OUT);
        assertEquals(second, out.window(), "the grab window was not told it lost the keyboard");
        assertEquals(NOTIFY_UNGRAB, out.mode(), "the grab gave the focus back and did not say so");

        in = readFocus(FOCUS_IN);
        assertEquals(first, in.window(), "the window that had the keyboard did not get it back");
        assertEquals(NOTIFY_UNGRAB, in.mode(), "the grab gave the focus back and did not say so");

        quiet();
    }

    /**
     * A grab taken on the window that already has the focus still reports the pair.
     *
     * <p>It is not a no-op, and treating it as one is the trap: a client grabs the keyboard precisely
     * to be told it holds it, and a server that sees "the focus is already there" and says nothing
     * leaves that client waiting for an event that never comes. Measured on a reference server,
     * which sends {@code FocusOut} and {@code FocusIn} against the same window.
     */
    @Test
    void aGrabOnTheFocusedWindowStillReportsThePair() throws Exception {
        client.sendGrabKeyboard(first, false);
        client.flush();
        assertGrabTaken();

        Focus out = readFocus(FOCUS_OUT);
        assertEquals(first, out.window(), "the window that already had the focus was not told");
        assertEquals(NONLINEAR, out.detail(), "a window related to itself is reported Nonlinear");
        assertEquals(NOTIFY_GRAB, out.mode());

        Focus in = readFocus(FOCUS_IN);
        assertEquals(first, in.window());
        assertEquals(NOTIFY_GRAB, in.mode());

        client.sendUngrabKeyboard();
        client.flush();

        assertEquals(NOTIFY_UNGRAB, readFocus(FOCUS_OUT).mode());
        assertEquals(NOTIFY_UNGRAB, readFocus(FOCUS_IN).mode());
        quiet();
    }

    /**
     * A focus change asked for while the grab is held is reported as made by the grab.
     */
    @Test
    void aFocusChangeInsideAGrabIsReportedWhileGrabbed() throws Exception {
        client.sendGrabKeyboard(first, false);
        client.flush();
        assertGrabTaken();
        quiet();

        client.setInputFocus(second, REVERT_TO_POINTER_ROOT);
        client.flush();

        Focus out = readFocus(FOCUS_OUT);
        assertEquals(first, out.window(), "the grab window was not told the focus left it");
        assertEquals(NOTIFY_WHILE_GRABBED, out.mode(),
                "the change was made inside a grab and the client was not told which");

        Focus in = readFocus(FOCUS_IN);
        assertEquals(second, in.window());
        assertEquals(NOTIFY_WHILE_GRABBED, in.mode());

        // And the way back is reported from the grab window to where the focus now is, not to the
        // window the grab took the focus from.
        client.sendUngrabKeyboard();
        client.flush();

        out = readFocus(FOCUS_OUT);
        assertEquals(first, out.window(), "the report is made from the grab window");
        assertEquals(NOTIFY_UNGRAB, out.mode());
        in = readFocus(FOCUS_IN);
        assertEquals(second, in.window(), "the focus stayed where the client put it");
        assertEquals(NOTIFY_UNGRAB, in.mode());
        quiet();
    }

    /**
     * A grab taken while the focus is on nothing at all reports nothing.
     *
     * <p>The one case of the four that is asymmetric, and the one no prose describes: the reference
     * does not tell the window it grabbed that it now has the focus, but it does tell it that the
     * focus left when the grab ends - which is the half a client can act on. Measured rather than
     * reasoned, and pinned here so a later change cannot quietly "fix" it into a pair.
     */
    @Test
    void aGrabTakenWithTheFocusOnNothingReportsNothing() throws Exception {
        client.setInputFocus(NONE, REVERT_TO_POINTER_ROOT);
        client.flush();
        quiet();

        client.sendGrabKeyboard(second, false);
        client.flush();
        assertGrabTaken();

        client.readTimeoutMillis(QUIET_MILLIS);
        assertNull(client.readEvent(), "a grab with the focus nowhere was reported as a change");
        client.readTimeoutMillis(5000);

        client.sendUngrabKeyboard();
        client.flush();

        Focus out = readFocus(FOCUS_OUT);
        assertEquals(second, out.window(), "the grab window was not told it lost the keyboard");
        assertEquals(NOTIFY_UNGRAB, out.mode());
        client.readTimeoutMillis(QUIET_MILLIS);
        assertNull(client.readEvent(), "nothing gained the focus, so nothing may be told it did");
        client.readTimeoutMillis(5000);
    }

    /**
     * A focus change with no grab in sight is still reported as an ordinary one, which is what the
     * mode is for: a client that saw {@code NotifyGrab} everywhere would stop being able to tell the
     * two situations apart.
     */
    @Test
    void aFocusChangeOutsideAGrabIsStillNormal() throws Exception {
        client.setInputFocus(second, REVERT_TO_POINTER_ROOT);
        client.flush();

        assertEquals(NOTIFY_NORMAL, readFocus(FOCUS_OUT).mode());
        assertEquals(NOTIFY_NORMAL, readFocus(FOCUS_IN).mode());
        quiet();
    }
}
