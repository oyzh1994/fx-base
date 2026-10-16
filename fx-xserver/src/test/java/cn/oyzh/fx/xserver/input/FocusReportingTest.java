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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What a client is told when the focus moves to or from the two states that are not a window.
 *
 * <p>A focus can be a window, {@code None}, or {@code PointerRoot} - and, less obviously, the
 * <b>root window</b>, which is a window like any other and is <em>not</em> PointerRoot. A client that
 * focuses the root window is handing the keyboard back to the desktop; a client that asks for
 * PointerRoot is asking the focus to follow the mouse. This server used to fold the two together, so
 * {@code GetInputFocus} answered {@code PointerRoot} where a reference server answers the root's own
 * id, and the keys went to the pointer rather than staying on the root.
 *
 * <p><b>Every expectation here was measured against a reference server</b>, with
 * {@code tools/FocusProbe}, one sequence at a time - the twelve sequences are the ones that pin the
 * rule, and the default sequence of that probe now prints the same lines against both servers.
 * Several of the rules are not derivable from the specification and two of them are openly odd:
 * a change <em>to</em> PointerRoot reports the window the pointer is in even when that window is the
 * one it is leaving, and a change to the <em>root window</em> does not. Both are measured, and both
 * are in the comments where they are implemented rather than here.
 */
class FocusReportingTest {

    /** A display number nothing else in the suite uses; the server listens on TCP 6000 + it. */
    private static final int DISPLAY = 82;

    private static final int FOCUS_IN = 9;
    private static final int FOCUS_OUT = 10;

    private static final int FOCUS_CHANGE_MASK = 1 << 21;

    /** {@code SetInputFocus}'s two special values, as the protocol numbers them. */
    private static final int NONE = 0;
    private static final int POINTER_ROOT = 1;

    private static final int REVERT_TO_PARENT = 2;

    /** Details, numbered as the protocol numbers them. */
    private static final int ANCESTOR = 0;
    private static final int INFERIOR = 2;
    private static final int NONLINEAR = 3;
    private static final int POINTER = 5;

    /** How long to wait before deciding that nothing more is coming. */
    private static final int QUIET_MILLIS = 200;

    private XServer server;
    private RawXClient client;
    private int a;
    private int b;
    private int c;
    private int xtest;

    @BeforeEach
    void startServerAndThreeWindows() throws Exception {
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--backend", "headless", "--no-unix"}));
        server.start();
        client = RawXClient.connect(DISPLAY);
        xtest = client.queryExtension("XTEST");
        a = client.allocateId();
        b = client.allocateId();
        c = client.allocateId();
        // B is inside A, which is what makes "the window the pointer is in" different from "the
        // window the focus is on" - and C is unrelated to both.
        client.createWindow(a, client.root(), 0, 0, 300, 300);
        client.createWindow(b, a, 10, 10, 100, 100);
        client.createWindow(c, client.root(), 400, 0, 300, 300);
        for (int window : new int[] {a, b, c}) {
            client.selectInput(window, FOCUS_CHANGE_MASK);
            client.mapWindow(window);
        }
        client.setInputFocus(NONE, REVERT_TO_PARENT);
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

    @Test
    void theRootWindowFocusedIsTheRootWindowAndNotPointerRoot() throws Exception {
        client.setInputFocus(client.root(), REVERT_TO_PARENT);
        client.flush();
        quiet();
        assertEquals(client.root(), focusOf(),
                "the root window is a window, and GetInputFocus names it; answering PointerRoot here"
                        + " folds two states together that report different events");

        client.setInputFocus(POINTER_ROOT, REVERT_TO_PARENT);
        client.flush();
        quiet();
        assertEquals(POINTER_ROOT, focusOf(), "and this is the state that says so in the reply");
    }

    @Test
    void aFocusArrivingInsideThePointerIsReportedWithPointerDetail() throws Exception {
        pointerInto(b);
        client.setInputFocus(a, REVERT_TO_PARENT);
        client.flush();

        assertEquals(new Focus(FOCUS_IN, a, NONLINEAR, 0), readFocus(),
                "the focus arriving on A, related to nothing");
        assertEquals(new Focus(FOCUS_IN, b, POINTER, 0), readFocus(),
                "and the window the pointer is in, told that the keyboard is where the mouse is -"
                        + " which is the only thing a client can act on there");
        assertQuiet();
    }

    @Test
    void aFocusLeavingThePointerIsReportedBeforeTheWindowLosesIt() throws Exception {
        pointerInto(b);
        client.setInputFocus(a, REVERT_TO_PARENT);
        client.flush();
        quiet();

        client.setInputFocus(b, REVERT_TO_PARENT);
        client.flush();

        assertEquals(new Focus(FOCUS_OUT, b, POINTER, 0), readFocus(),
                "the window the pointer is in gives up the implicit focus first");
        assertEquals(new Focus(FOCUS_OUT, a, INFERIOR, 0), readFocus(),
                "then the ordinary pair: A loses the focus to a window inside it");
        assertEquals(new Focus(FOCUS_IN, b, ANCESTOR, 0), readFocus(), "and B gains it");
        assertQuiet();
    }

    @Test
    void aChangeToPointerRootReportsTheWholeChainOutermostFirst() throws Exception {
        pointerInto(b);
        client.setInputFocus(c, REVERT_TO_PARENT);
        client.flush();
        quiet();

        client.setInputFocus(POINTER_ROOT, REVERT_TO_PARENT);
        client.flush();

        assertEquals(new Focus(FOCUS_OUT, c, NONLINEAR, 0), readFocus(),
                "PointerRoot is not a window, so the window losing the focus is related to nothing");
        assertEquals(new Focus(FOCUS_IN, a, POINTER, 0), readFocus(),
                "then the chain the pointer implies, outermost first");
        assertEquals(new Focus(FOCUS_IN, b, POINTER, 0), readFocus(), "and innermost last");
        assertQuiet();
    }

    @Test
    void aChangeToTheRootWindowLeavesTheChainInsideTheFocusAlone() throws Exception {
        pointerInto(b);
        client.setInputFocus(a, REVERT_TO_PARENT);
        client.flush();
        quiet();

        client.setInputFocus(client.root(), REVERT_TO_PARENT);
        client.flush();

        assertEquals(new Focus(FOCUS_OUT, a, ANCESTOR, 0), readFocus(),
                "the root window is A's ancestor, so that is the detail - where folding it into"
                        + " PointerRoot reported Nonlinear instead");
        assertQuiet();
    }

    @Test
    void aChangeToTheRootWindowTellsThePointerChainThatIsNotInsideTheFocus() throws Exception {
        pointerInto(b);
        client.setInputFocus(c, REVERT_TO_PARENT);
        client.flush();
        quiet();

        client.setInputFocus(client.root(), REVERT_TO_PARENT);
        client.flush();

        assertEquals(new Focus(FOCUS_OUT, c, ANCESTOR, 0), readFocus(), "C leaves the focus");
        assertEquals(new Focus(FOCUS_IN, a, POINTER, 0), readFocus(),
                "and the pointer's chain arrives, because none of it was inside C");
        assertEquals(new Focus(FOCUS_IN, b, POINTER, 0), readFocus(), "innermost last");
        assertQuiet();
    }

    // ------------------------------------------------------------------

    /** Moves the pointer into a window, so it is that window's subtree the focus implies. */
    private void pointerInto(int window) throws Exception {
        int x = window == b ? 50 : 500;
        int y = window == b ? 50 : 150;
        // Park it outside first, so the crossing this causes is not mistaken for a focus event: the
        // pointer outlives the client, and where the last run left it is not this test's business.
        client.fakeMotion(xtest, 1000, 600);
        client.fakeMotion(xtest, x, y);
        client.flush();
        quiet();
    }

    /** {@code GetInputFocus}: the reply names the focus at byte 8. */
    private int focusOf() throws Exception {
        byte[] request = new byte[4];
        request[0] = 43;                            // GetInputFocus
        RawXClient.writeShortAt(request, 2, 1);
        client.sendRequest(request);
        return RawXClient.readInt(client.readReply(), 8);
    }

    /** One focus event: the window it names, its detail at byte 1 and its mode at byte 8. */
    private record Focus(int type, int window, int detail, int mode) {
    }

    private Focus readFocus() throws Exception {
        byte[] event = client.readEvent();
        assertNotNull(event, "no focus event arrived");
        int type = event[0] & 0xFF;
        assertTrue(type == FOCUS_IN || type == FOCUS_OUT,
                "the event that arrived was not a focus event: type " + type);
        return new Focus(type, RawXClient.readInt(event, 4), event[1] & 0xFF, event[8] & 0xFF);
    }

    /** Asserts that nothing else is coming, which is how "and no more" is checked. */
    private void assertQuiet() throws Exception {
        client.readTimeoutMillis(QUIET_MILLIS);
        byte[] extra = client.readEvent();
        client.readTimeoutMillis(5000);
        assertNull(extra, "an event arrived that the reference server does not send");
    }

    /** Reads and drops whatever is left, so a test only reads what its own request produced. */
    private void quiet() throws Exception {
        client.readTimeoutMillis(QUIET_MILLIS);
        while (client.readEvent() != null) {
            // Quiet is the end of it.
        }
        client.readTimeoutMillis(5000);
    }
}
