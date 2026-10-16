package cn.oyzh.fx.xserver.input;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * What a crossing event says beyond the window it names: the flags byte in the corner, the detail
 * for the windows in the middle of a chain, and the order the chain arrives in.
 *
 * <p>All three were measured against a reference server with {@code tools/FocusProbe}, and all three
 * were wrong here in a way no client-side feature test would have caught:
 *
 * <ul>
 *   <li>**The last byte packs the focus flag in bit 0 and {@code same_screen} in bit 1** - the
 *       opposite of the order the field's name suggests. This server had them the other way round, so
 *       every crossing it sent claimed the pointer was on another screen, and reported the focus bit
 *       inverted whenever the crossing's window was not focused. Xlib is the decoder every client
 *       uses, and it is what settled it: the same event read as "same_screen NO, focus YES" out of
 *       this server and "same_screen YES, focus NO" out of a reference server.
 *   <li>**The focus bit is set when the crossing's window is inside the focus window** - the focus
 *       window itself or a descendant of it. The relation is easy to take backwards, and the
 *       measurement that pins the direction is a window of each kind: a child of the focus (set), the
 *       focus's own parent (clear), and an unrelated window (clear).
 *   <li>**The chain arrives outermost first and the window itself last**, so a toolkit can walk down
 *       into the window the pointer entered. Leaving is the other way round - innermost first - and
 *       that asymmetry is the reference's, not a slip.
 *   <li>**A window in the middle of the chain reports {@code Virtual} when the window on the other
 *       end is above it**, and {@code NonlinearVirtual} when the other end is unrelated. Reporting
 *       {@code Virtual} for every window in the chain - which this did - tells a toolkit the pointer
 *       moved inside its subtree when it left it.
 * </ul>
 */
class CrossingReportTest {

    /** A display number nothing else in the suite uses; the server listens on TCP 6000 + it. */
    private static final int DISPLAY = 82;

    private static final int ENTER_NOTIFY = 7;
    private static final int LEAVE_NOTIFY = 8;

    private static final int ENTER_WINDOW_MASK = 0x10;
    private static final int LEAVE_WINDOW_MASK = 0x20;

    private static final int REVERT_TO_POINTER_ROOT = 1;

    /** Details, numbered as the protocol numbers them. */
    private static final int ANCESTOR = 0;
    private static final int VIRTUAL = 1;
    private static final int NONLINEAR_VIRTUAL = 4;

    /** How long to wait before deciding that nothing more is coming. */
    private static final int QUIET_MILLIS = 200;

    /** A point no window of this test reaches, for parking the pointer between crossings. */
    private static final int PARK_X = 1000;
    private static final int PARK_Y = 700;

    private XServer server;
    private RawXClient client;
    private int xtest;
    private int outer;
    private int middle;
    private int inner;
    private int other;

    @BeforeEach
    void startServerAndFourWindows() throws Exception {
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--backend", "headless", "--no-unix"}));
        server.start();
        client = RawXClient.connect(DISPLAY);
        xtest = client.queryExtension("XTEST");
        outer = client.allocateId();
        middle = client.allocateId();
        inner = client.allocateId();
        other = client.allocateId();
        // Three deep, so that a window can be a *virtual* ancestor of the pointer's window rather
        // than its parent: outer 10,10 300x300 > middle 5,5 200x200 > inner 5,5 100x100, and an
        // unrelated window beside them.
        client.createWindow(outer, client.root(), 10, 10, 300, 300);
        client.createWindow(middle, outer, 5, 5, 200, 200);
        client.createWindow(inner, middle, 5, 5, 100, 100);
        client.createWindow(other, client.root(), 400, 10, 300, 300);
        for (int window : new int[] {outer, middle, inner, other}) {
            client.selectInput(window, ENTER_WINDOW_MASK | LEAVE_WINDOW_MASK);
            client.mapWindow(window);
        }
        client.flush();
        park();
    }

    @AfterEach
    void stopServer() throws Exception {
        if (client != null) {
            client.close();
        }
        server.stop();
    }

    /** Moves the pointer somewhere no window reaches and drops what that produced. */
    private void park() throws Exception {
        move(PARK_X, PARK_Y);
    }

    /** Moves the pointer and drops the events the move produced, so a test reads only its own. */
    private void move(int x, int y) throws Exception {
        client.fakeMotion(xtest, x, y);
        client.flush();
        quiet();
    }

    private void quiet() throws Exception {
        client.readTimeoutMillis(QUIET_MILLIS);
        while (client.readEvent() != null) {
            // Quiet is the end of it.
        }
        client.readTimeoutMillis(5000);
    }

    /** One crossing: the window it names (from byte 4), its detail (byte 1) and its flags (byte 31). */
    private record Crossing(int window, int detail, int flags) {
    }

    private Crossing readCrossing(int type) throws Exception {
        byte[] event = client.readEvent(type);
        assertNotNull(event, type == ENTER_NOTIFY ? "no EnterNotify arrived" : "no LeaveNotify arrived");
        return new Crossing(RawXClient.readInt(event, 12), event[1] & 0xFF, event[31] & 0xFF);
    }

    /**
     * The last byte: {@code same_screen} in bit 1, the focus flag in bit 0.
     *
     * <p>The two values the measurement gives: {@code 0x02} when the pointer is on the same screen
     * and the crossing's window is not focused, and {@code 0x03} when it is. A byte of {@code 0x01}
     * - what this server used to send in the first case - reads as "same screen no, focus yes", which
     * is wrong twice over.
     */
    @Test
    void theFlagsByteCarriesSameScreenAboveTheFocus() throws Exception {
        // The focus on an unrelated window, so the bit must be clear.
        client.setInputFocus(other, REVERT_TO_POINTER_ROOT);
        client.flush();
        quiet();

        client.fakeMotion(xtest, 300, 300);             // inside outer, outside middle
        client.flush();

        Crossing enter = readCrossing(ENTER_NOTIFY);
        assertEquals(outer, enter.window(), "the pointer entered the outermost window");
        assertEquals(0x02, enter.flags(),
                "same screen is bit 1 and the focus bit is bit 0; the focus is elsewhere, so only "
                        + "the screen bit is set");

        // And the same crossing with the focus on the window itself: the low bit comes on too.
        client.setInputFocus(outer, REVERT_TO_POINTER_ROOT);
        client.flush();
        move(PARK_X, PARK_Y);
        client.fakeMotion(xtest, 300, 300);
        client.flush();

        enter = readCrossing(ENTER_NOTIFY);
        assertEquals(outer, enter.window());
        assertEquals(0x03, enter.flags(),
                "the focus is this window, so both bits are set - and same_screen is still the high "
                        + "one, which is what xev reads it as");
    }

    /**
     * The focus bit is set for a window inside the focus window. The direction is the trap: this
     * server used to ask whether the crossing's window was an *ancestor* of the focus, which is the
     * same relation backwards and gives the right answer for none of the three cases.
     */
    @Test
    void theFocusBitIsSetForAWindowInsideTheFocus() throws Exception {
        client.setInputFocus(outer, REVERT_TO_POINTER_ROOT);
        client.flush();
        quiet();
        park();

        client.fakeMotion(xtest, 60, 60);               // into outer, into middle, into inner
        client.flush();

        // Every window on the way in is told, outermost first: outer (which *is* the focus), then
        // the two inside it - and the bit is set for all three, because each one is the focus window
        // or inside it.
        for (int expected : new int[] {outer, middle, inner}) {
            Crossing crossing = readCrossing(ENTER_NOTIFY);
            assertEquals(expected, crossing.window());
            assertEquals(0x03, crossing.flags(),
                    "this window is the focus window or inside it, so the focus bit is set");
        }

        // The focus window's own parent is *not* inside it, and the reference clears the bit there.
        client.setInputFocus(inner, REVERT_TO_POINTER_ROOT);
        client.flush();
        quiet();
        park();

        client.fakeMotion(xtest, 300, 300);             // into outer, which holds the focus
        client.flush();

        Crossing toOuter = readCrossing(ENTER_NOTIFY);
        assertEquals(outer, toOuter.window());
        assertEquals(0x02, toOuter.flags(),
                "the focus window is inside this one, which is the other direction and clears the bit");
    }

    /**
     * The chain arrives outermost first and the window itself last.
     *
     * <p>A toolkit walks down into the window that gained the pointer, which it can only do if the
     * window holding it is named first. Leaving is innermost first, and the asymmetry is the
     * reference's.
     */
    @Test
    void theEnterChainArrivesOutermostFirst() throws Exception {
        park();

        client.fakeMotion(xtest, 60, 60);               // into outer, middle and inner
        client.flush();

        assertEquals(outer, readCrossing(ENTER_NOTIFY).window(), "the outermost window is named first");
        assertEquals(middle, readCrossing(ENTER_NOTIFY).window(),
                "then the window holding the one the pointer is in");
        assertEquals(inner, readCrossing(ENTER_NOTIFY).window(),
                "and the window the pointer is in is named last");
    }

    /**
     * A window in the middle of the chain reports what the window on the other end is to it:
     * {@code Virtual} when the other end is above it - the pointer moved into its subtree from
     * outside - and {@code NonlinearVirtual} when the other end is unrelated.
     */
    @Test
    void aChainWindowSaysVirtualOnlyWhenTheOtherEndIsAboveIt() throws Exception {
        // From outer's own area into inner: middle's other end is outer, which holds it.
        client.fakeMotion(xtest, 300, 300);
        client.flush();
        quiet();

        client.fakeMotion(xtest, 60, 60);
        client.flush();

        Crossing toMiddle = readCrossing(ENTER_NOTIFY);
        assertEquals(middle, toMiddle.window());
        assertEquals(VIRTUAL, toMiddle.detail(),
                "the pointer came from outside this window and stopped inside it");
        assertEquals(ANCESTOR, readCrossing(ENTER_NOTIFY).detail(),
                "and from the window holding it, for the window the pointer is in");

        // From an unrelated window into middle: outer's other end is that window.
        park();
        client.fakeMotion(xtest, 500, 100);             // inside other
        client.flush();
        quiet();

        client.fakeMotion(xtest, 60, 60);
        client.flush();

        Crossing toOuter = readCrossing(ENTER_NOTIFY);
        assertEquals(outer, toOuter.window());
        assertEquals(NONLINEAR_VIRTUAL, toOuter.detail(),
                "the pointer left an unrelated window, so nothing about this subtree is what moved");
    }
}
