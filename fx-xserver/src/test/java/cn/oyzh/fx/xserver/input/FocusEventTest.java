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
 * FocusIn and FocusOut, the events that tell a client it has the keyboard.
 *
 * <p>A toolkit asks for the focus and then waits to be told it got it. Without the telling, the
 * window is drawn and inert at the same time: keystrokes go to a window that never claimed them, a
 * dialog that waits for its focus never enables its fields, and a menu opened over a control cannot
 * be driven. This server changed the focus and sent nothing at all, which a JavaFX application feels
 * as "the context menu cannot get the focus".
 *
 * <p>Every expectation here was measured against a reference server (Xvfb) for the same requests,
 * with `/tmp/FocusProbe.java`; the details are the reference's, not this server's idea of them.
 */
class FocusEventTest {

    /** A display number nothing else in the suite uses; the server listens on TCP 6000 + it. */
    private static final int DISPLAY = 98;

    private static final int FOCUS_IN = 9;
    private static final int FOCUS_OUT = 10;

    private static final int FOCUS_CHANGE_MASK = 1 << 21;

    private static final int NONE = 0;
    private static final int POINTER_ROOT = 1;
    private static final int REVERT_TO_POINTER_ROOT = 1;
    private static final int REVERT_TO_PARENT = 2;

    /** Details, numbered as the protocol numbers them. */
    private static final int ANCESTOR = 0;
    private static final int INFERIOR = 2;
    private static final int NONLINEAR = 3;
    private static final int NONLINEAR_VIRTUAL = 4;
    /** The focus is PointerRoot, so this is about the pointer and not about another window. */
    private static final int POINTER = 5;

    private static final int NORMAL_MODE = 0;

    /** How long to wait before deciding that nothing more is coming. */
    private static final int QUIET_MILLIS = 200;

    private XServer server;
    private RawXClient client;
    private int xtest;
    private int outer;
    private int inner;
    private int other;

    @BeforeEach
    void startServerAndThreeWindows() throws Exception {
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--backend", "headless", "--no-unix"}));
        server.start();
        client = RawXClient.connect(DISPLAY);
        xtest = client.queryExtension("XTEST");
        outer = client.allocateId();
        inner = client.allocateId();
        other = client.allocateId();
        client.createWindow(outer, client.root(), 10, 10, 300, 300);
        client.createWindow(inner, outer, 5, 5, 100, 100);
        client.createWindow(other, client.root(), 400, 10, 300, 300);
        client.selectInput(outer, FOCUS_CHANGE_MASK);
        client.selectInput(inner, FOCUS_CHANGE_MASK);
        client.selectInput(other, FOCUS_CHANGE_MASK);
        client.mapWindow(outer);
        client.mapWindow(inner);
        client.mapWindow(other);
        // **The pointer is parked outside every window first.** It outlives the server that moved it
        // and the class runs its tests in one JVM, so a test that leaves it in a window leaves it
        // there for the next one - and a case whose answer depends on what the pointer is inside then
        // answers differently depending on the order it ran in. Measured the hard way: with the
        // pointer inside a window, a focus change between two windows of one client reports the
        // pointer's own pair as well (`FocusOut <pointer's window> detail=Pointer` first, which a
        // reference sends too), and the assertions below - written for a pointer outside - stopped
        // matching. Parking it is what the probes do, and for the same reason.
        client.fakeMotion(xtest, 550, 380);
        client.flush();
        quiet();
    }

    /**
     * Reads and drops whatever is left on the connection, so a test only reads the events its own
     * request produced. A setup change sends a chain of events, not one, and reading a fixed number
     * of them is what made these tests read a stale one.
     */
    private void quiet() throws Exception {
        client.readTimeoutMillis(QUIET_MILLIS);
        while (client.readEvent() != null) {
            // Quiet is the end of it.
        }
        client.readTimeoutMillis(5000);
    }

    @AfterEach
    void stopServer() throws Exception {
        if (client != null) {
            client.close();
        }
        server.stop();
    }

    /**
     * The case a menu lives on: the focus is following the pointer, the pointer is already inside the
     * window that is asking for the focus, and the window has to be told it got it.
     *
     * <p>Both halves are sent even though the window is on both sides of the change, which is what a
     * reference server was measured doing - the window leaves the pointer and arrives under the
     * focus. Reporting it as a no-op leaves the client that asked never told, which is the state this
     * server was in.
     */
    @Test
    void aWindowThatAsksForTheFocusUnderThePointerIsToldItHasIt() throws Exception {
        client.fakeMotion(xtest, 250, 250);
        client.flush();

        // PointerRoot has to be asked for, and this used to work without it because the server folded
        // the root window - the state a reference server starts in, `xdpyinfo`'s "focus: window
        // 0x1ff" - into PointerRoot. They are different states and report a different detail on the
        // way out: measured with tools/FocusProbe, and pinned in FocusReportingTest.
        client.setInputFocus(POINTER_ROOT, REVERT_TO_POINTER_ROOT);
        client.flush();
        quiet();

        client.setInputFocus(outer, REVERT_TO_POINTER_ROOT);
        client.flush();

        byte[] out = client.readEvent(FOCUS_OUT);
        assertNotNull(out, "the window was never told the pointer no longer had the keyboard");
        assertEquals(outer, RawXClient.readInt(out, 4));
        assertEquals(POINTER, out[1] & 0xFF, "the report is about the pointer, not another window");
        assertEquals(NORMAL_MODE, out[8] & 0xFF, "the change was not the doing of a grab");

        byte[] in = client.readEvent(FOCUS_IN);
        assertNotNull(in, "the window asking for the focus was never told it got it");
        assertEquals(outer, RawXClient.readInt(in, 4));
        assertEquals(NONLINEAR, in[1] & 0xFF,
                "the focus came from the pointer, which is not a window to relate it to");
    }

    /**
     * The focus moving between two of a client's own windows is reported to both, each with the
     * detail that says what the other one is to it - the same lattice a crossing uses, so a toolkit
     * that tracks both reads one rule.
     */
    @Test
    void theFocusMovingInsideOneClientIsReportedToBothWindows() throws Exception {
        client.setInputFocus(outer, REVERT_TO_POINTER_ROOT);
        client.flush();
        assertNotNull(client.readEvent(FOCUS_IN), "the first focus never arrived");
        quiet();

        // Into the child: the parent loses it and the child gains it.
        client.setInputFocus(inner, REVERT_TO_POINTER_ROOT);
        client.flush();

        byte[] out = client.readEvent(FOCUS_OUT);
        assertNotNull(out, "the window losing the focus was not told");
        assertEquals(outer, RawXClient.readInt(out, 4));
        assertEquals(INFERIOR, out[1] & 0xFF,
                "the focus went to a window inside this one, which is what it must be told");

        byte[] in = client.readEvent(FOCUS_IN);
        assertNotNull(in, "the window gaining the focus was not told");
        assertEquals(inner, RawXClient.readInt(in, 4));
        assertEquals(ANCESTOR, in[1] & 0xFF, "the focus came from the window holding this one");

        // And back out again, which is the same pair with the details the other way round.
        client.setInputFocus(outer, REVERT_TO_POINTER_ROOT);
        client.flush();

        out = client.readEvent(FOCUS_OUT);
        assertNotNull(out, "the child was not told it lost the focus");
        assertEquals(inner, RawXClient.readInt(out, 4));
        assertEquals(ANCESTOR, out[1] & 0xFF, "the focus went to the window holding this one");
        in = client.readEvent(FOCUS_IN);
        assertNotNull(in, "the parent was not told it regained the focus");
        assertEquals(outer, RawXClient.readInt(in, 4));
        assertEquals(INFERIOR, in[1] & 0xFF, "the focus came from a window inside this one");
    }

    /**
     * The window holding a focused window is told as well, so a toolkit that only watches its top
     * level - which is most of them - hears about every focus change inside itself.
     */
    @Test
    void theWindowsAroundTheFocusedOneAreToldToo() throws Exception {
        client.setInputFocus(other, REVERT_TO_POINTER_ROOT);
        client.flush();
        assertNotNull(client.readEvent(FOCUS_IN), "the first focus never arrived");
        quiet();

        client.setInputFocus(inner, REVERT_TO_POINTER_ROOT);
        client.flush();

        // The window that had it, and then the windows holding the one that gains it: outermost
        // first, so a toolkit can walk down into the window that got the focus.
        assertEquals(NONLINEAR, client.readEvent(FOCUS_OUT)[1] & 0xFF,
                "the focus went to an unrelated window");
        byte[] toOuter = client.readEvent(FOCUS_IN);
        assertNotNull(toOuter, "the window holding the focused one was not told");
        assertEquals(outer, RawXClient.readInt(toOuter, 4));
        assertEquals(NONLINEAR_VIRTUAL, toOuter[1] & 0xFF,
                "the focus arrived from outside this window's own tree");
        byte[] toInner = client.readEvent(FOCUS_IN);
        assertEquals(inner, RawXClient.readInt(toInner, 4));
        assertEquals(NONLINEAR, toInner[1] & 0xFF,
                "the focus arrived from a window with nothing to do with this one");
    }

    @Test
    void settingTheSameFocusAgainSaysNothing() throws Exception {
        client.setInputFocus(other, REVERT_TO_POINTER_ROOT);
        client.flush();
        assertNotNull(client.readEvent(FOCUS_IN), "the first focus never arrived");
        quiet();

        client.setInputFocus(other, REVERT_TO_POINTER_ROOT);
        client.flush();

        client.readTimeoutMillis(QUIET_MILLIS);
        assertNull(client.readEvent(FOCUS_IN), "a focus that did not change was reported anyway");
    }

    /**
     * Handing the focus back to the pointer takes it out of the window that had it, and the window
     * the pointer is in is told it has the keyboard now - the whole chain of windows holding it, and
     * with the detail that says this is the pointer rather than another window.
     */
    @Test
    void givingTheFocusBackToThePointerMovesItToThePointer() throws Exception {
        client.setInputFocus(inner, REVERT_TO_POINTER_ROOT);
        client.flush();
        assertNotNull(client.readEvent(FOCUS_IN), "the first focus never arrived");
        quiet();
        client.fakeMotion(xtest, 60, 60);
        client.flush();

        client.setInputFocus(POINTER_ROOT, REVERT_TO_POINTER_ROOT);
        client.flush();

        byte[] out = client.readEvent(FOCUS_OUT);
        assertNotNull(out, "the window was not told it lost the focus");
        assertEquals(inner, RawXClient.readInt(out, 4));
        assertEquals(NONLINEAR, out[1] & 0xFF, "the focus went to the pointer, not to a window");

        // The parent of the window the pointer is in is told first: the chain arrives outermost
        // first, like every other way in - measured against a reference server, and the opposite of
        // what this test asserted when the server sent it the other way round.
        byte[] toOuter = client.readEvent(FOCUS_IN);
        assertNotNull(toOuter, "the window holding the one the pointer is in was not told");
        assertEquals(outer, RawXClient.readInt(toOuter, 4));
        assertEquals(POINTER, toOuter[1] & 0xFF);

        byte[] in = client.readEvent(FOCUS_IN);
        assertNotNull(in, "the window the pointer is in was not told");
        assertEquals(inner, RawXClient.readInt(in, 4));
        assertEquals(POINTER, in[1] & 0xFF, "the keyboard went back to the pointer");
    }

    /**
     * Unmapping the focused window moves the focus, to wherever the last {@code SetInputFocus} said
     * it should revert to. A toolkit that unmaps a dialog instead of putting the focus back itself -
     * which is the usual way a dialog is dismissed - depends on this, and keyboard events aimed at a
     * window nobody can see are keyboard events nobody handles.
     */
    @Test
    void unmappingTheFocusedWindowRevertsTheFocus() throws Exception {
        client.setInputFocus(inner, REVERT_TO_PARENT);
        client.flush();
        assertNotNull(client.readEvent(FOCUS_IN), "the first focus never arrived");
        quiet();

        client.unmapWindow(inner);
        client.flush();

        byte[] out = client.readEvent(FOCUS_OUT);
        assertNotNull(out, "the unmapped window was not told it lost the focus");
        assertEquals(inner, RawXClient.readInt(out, 4));
        assertEquals(ANCESTOR, out[1] & 0xFF, "the focus went to the window holding this one");

        byte[] in = client.readEvent(FOCUS_IN);
        assertNotNull(in, "the window the focus reverted to was not told");
        assertEquals(outer, RawXClient.readInt(in, 4));
        assertEquals(INFERIOR, in[1] & 0xFF, "the focus came from a window inside this one");
    }

    /** The same, for a window that goes away entirely rather than being unmapped. */
    @Test
    void destroyingTheFocusedWindowRevertsTheFocus() throws Exception {
        client.setInputFocus(inner, REVERT_TO_PARENT);
        client.flush();
        assertNotNull(client.readEvent(FOCUS_IN), "the first focus never arrived");
        quiet();

        client.destroyWindow(inner);
        client.flush();

        byte[] in = client.readEvent(FOCUS_IN);
        assertNotNull(in, "the window the focus reverted to was not told");
        assertEquals(outer, RawXClient.readInt(in, 4));
        assertEquals(INFERIOR, in[1] & 0xFF);
    }

    /**
     * A window holding the focused window takes the focus with it: the focused window stops being
     * viewable, so the focus has to move even though it is not the window being unmapped.
     */
    @Test
    void unmappingAWindowHoldingTheFocusedOneRevertsTheFocus() throws Exception {
        client.setInputFocus(inner, REVERT_TO_PARENT);
        client.flush();
        assertNotNull(client.readEvent(FOCUS_IN), "the first focus never arrived");
        quiet();

        client.unmapWindow(outer);
        client.flush();

        byte[] out = client.readEvent(FOCUS_OUT);
        assertNotNull(out, "the focused window was not told it lost the focus");
        assertEquals(inner, RawXClient.readInt(out, 4));
    }

    /**
     * The focus can be nowhere at all, and a client can tell that from the focus following the
     * pointer: nothing is told it gained the keyboard, and asking which window has it answers None.
     */
    @Test
    void theFocusCanBeNowhere() throws Exception {
        client.setInputFocus(other, REVERT_TO_POINTER_ROOT);
        client.flush();
        assertNotNull(client.readEvent(FOCUS_IN), "the first focus never arrived");
        quiet();

        client.setInputFocus(NONE, REVERT_TO_POINTER_ROOT);
        client.flush();

        byte[] out = client.readEvent(FOCUS_OUT);
        assertNotNull(out, "the window was not told it lost the focus");
        assertEquals(other, RawXClient.readInt(out, 4));
        client.readTimeoutMillis(QUIET_MILLIS);
        assertNull(client.readEvent(FOCUS_IN),
                "something was told it gained a keyboard that belongs to no window");
        assertEquals(NONE, client.queryInputFocus(), "the focus is on no window");
    }
}
