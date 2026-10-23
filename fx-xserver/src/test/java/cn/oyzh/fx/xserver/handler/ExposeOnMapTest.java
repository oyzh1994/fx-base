package cn.oyzh.fx.xserver.handler;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * A window mapped before its parent still has to be told to draw itself.
 *
 * <p>Toolkits do this the other way round from what a server finds convenient: the children are
 * created and mapped first, while the window that holds them is not on screen yet, and the toplevel
 * is mapped last. Nothing of a child shows until then - and the moment it does is the moment it is
 * owed an Expose, because that is what a toolkit draws itself from. Miss it and the window is
 * black: {@code xclock}, {@code xeyes} and {@code xcalc} all come up empty, and the parts that
 * appear while the pointer is dragged across them are the pieces some other path happened to
 * repaint.
 *
 * <p>The check runs over a real connection rather than against the window tree, because what is
 * broken is not the region a window has but the event it is sent: a child whose region is perfectly
 * correct is still left blank when the map reports it as gaining nothing, since it "already" had
 * that region by the time the parent's map reached it.
 */
class ExposeOnMapTest {

    /** A display number nothing else in the suite uses; the server listens on TCP 6000 + it. */
    private static final int DISPLAY = 96;

    private static final int EXPOSE = 12;

    /** Where an event's {@code window} field sits, in bytes. */
    private static final int WINDOW = 4;

    /** ExposureMask, the event a toolkit draws itself from. */
    private static final int EXPOSURE_MASK = 1 << 15;

    @Test
    void aChildMappedBeforeItsParentIsExposedWhenTheParentIsMapped() throws Exception {
        XServer server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--backend", "headless", "--no-unix"}));
        server.start();
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int parent = client.allocateId();
            int child = client.allocateId();
            client.createWindow(parent, client.root(), 0, 0, 200, 200);
            client.createWindow(child, parent, 0, 0, 200, 200);
            // The child is the one that draws: it asks for Expose and nothing else.
            client.selectInput(child, EXPOSURE_MASK);
            // Mapped while the parent is not, so nothing of it shows yet ...
            client.mapWindow(child);
            // ... and then the map that puts both of them on screen.
            client.mapWindow(parent);
            client.flush();

            byte[] expose = client.readEvent(EXPOSE);

            assertNotNull(expose, "the child was never told to draw itself");
            assertEquals(0, RawXClient.readShort(expose, 8), "the exposure starts at the child's origin");
            assertEquals(0, RawXClient.readShort(expose, 10));
            assertEquals(200, RawXClient.readShort(expose, 12), "the whole child is new to the screen");
            assertEquals(200, RawXClient.readShort(expose, 14));
        } finally {
            server.stop();
        }
    }

    /**
     * Unmapping a parent leaves its mapped children mapped, and remapping the parent exposes them.
     *
     * <p>X11 has three map states rather than two: a child of an unmapped parent is
     * {@code IsUnviewable}, not {@code IsUnmapped}, and stays mapped so that the parent's remap puts
     * it back on screen. xcalc is the real client that depends on it: a resize unmaps and remaps its
     * form, whose fifty-odd button windows are never mapped again by the client. Recursively unmapping
     * the subtree here left every button unmapped after a stretch, so the calculator kept its display
     * and lost the whole keypad (measured 2026-10-23).
     */
    @Test
    void aParentRemapExposesAChildThatStayedMapped() throws Exception {
        XServer server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--backend", "headless", "--no-unix"}));
        server.start();
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            client.socketTimeout(1000);
            int parent = client.allocateId();
            int child = client.allocateId();
            client.createWindow(parent, client.root(), 0, 0, 200, 200);
            client.createWindow(child, parent, 0, 0, 200, 200);
            client.selectInput(child, EXPOSURE_MASK);
            client.mapWindow(child);
            client.mapWindow(parent);
            client.flush();

            assertNotNull(client.readEvent(EXPOSE), "the child draws itself when the parent first maps");

            client.unmapWindow(parent);
            client.flush();
            client.mapWindow(parent);
            client.flush();

            byte[] expose = readExposeFor(client, child);
            assertNotNull(expose,
                    "the child stayed mapped, so remapping the parent owes it the whole of itself");
            assertEquals(0, RawXClient.readShort(expose, 8));
            assertEquals(0, RawXClient.readShort(expose, 10));
            assertEquals(200, RawXClient.readShort(expose, 12));
            assertEquals(200, RawXClient.readShort(expose, 14));
        } finally {
            server.stop();
        }
    }

    /** The next Expose for {@code window}, or null when the server sends none before the timeout. */
    private static byte[] readExposeFor(RawXClient client, int window) throws Exception {
        for (byte[] event = client.readEvent(); event != null; event = client.readEvent()) {
            if ((event[0] & 0x7F) == EXPOSE && RawXClient.readInt(event, WINDOW) == window) {
                return event;
            }
        }
        return null;
    }
}
