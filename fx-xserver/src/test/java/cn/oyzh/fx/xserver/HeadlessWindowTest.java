package cn.oyzh.fx.xserver;

import cn.oyzh.fx.xserver.display.HeadlessBackend;
import cn.oyzh.fx.xserver.display.JavaFxBackend;
import cn.oyzh.fx.xserver.display.SwingBackend;
import cn.oyzh.fx.xserver.display.VncBackend;
import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.awt.Rectangle;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What the server says about where a client's screen is being shown.
 *
 * <p>Found while closing E2's "a server with no display serves into the void": the fallback to the
 * headless backend was already there and already logged - and then, one line later, the server
 * logged <b>"opened a window for display :95 showing ..."</b> for a backend that opens no window at
 * all. Measured live on 2026-09-26 with {@code xclock} against a {@code -Djava.awt.headless=true}
 * server: the warning that nothing can be shown and the claim that a window was opened, adjacent.
 *
 * <p>That sentence is not decoration - it is the line a user follows to find where their application
 * went - so a backend that has no window has to say so, and the two backends that have none have to
 * be the ones that claim it. What is pinned here is the decision, which is a pure function because
 * the case that went wrong is the one a machine with a display cannot otherwise reach.
 */
class HeadlessWindowTest {

    private static final Rectangle SHOWING = new Rectangle(0, 0, 200, 150);

    @Test
    void aBackendWithNoWindowIsNotDescribedAsHavingOpenedOne() {
        String said = XServer.openedMessage(new HeadlessBackend(), 95, SHOWING, null);

        assertFalse(said.contains("opened a window"), () -> "a window that does not exist: " + said);
        assertTrue(said.contains("opens no window"), () -> "it has to say so: " + said);
        assertTrue(said.contains("headless"), () -> "and say by which backend: " + said);
        assertTrue(said.contains(":95"), () -> "and for which display: " + said);
        assertTrue(said.contains(SHOWING.toString()), () -> "and what it is showing: " + said);
    }

    @Test
    void andAWindowBackendIs() {
        String said = XServer.openedMessage(new SwingBackend("cn/oyzh/fx/xserver"), 95, SHOWING,
                new Point(40, 60));

        assertTrue(said.startsWith("opened a window for display :95 showing " + SHOWING),
                () -> "the ordinary line: " + said);
        assertTrue(said.endsWith("at 40,60"), () -> "and where it was put: " + said);
    }

    /**
     * The contract the line above rests on: exactly the backends with no window on this machine say
     * they are headless. A backend that draws for viewers elsewhere (VNC) counts - there is no window
     * here to move, title or bring forward, which is what the server's log is about.
     */
    @Test
    void theBackendsWithoutAWindowAreTheOnesThatSaySo() {
        assertTrue(new HeadlessBackend().isHeadless());
        assertTrue(new VncBackend(0, 0, "cn/oyzh/fx/xserver", false, null).isHeadless());
        assertFalse(new SwingBackend("cn/oyzh/fx/xserver").isHeadless());
        assertFalse(new JavaFxBackend("cn/oyzh/fx/xserver").isHeadless());
    }
}
