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
 * What a grab does to the crossing events a menu is built on.
 *
 * <p>Entering and leaving are the only way a client learns which of its own windows the pointer is
 * in. A menu's submenu is a window of its own that appears beside the menu the pointer is hovering,
 * and a toolkit highlights the submenu and routes clicks inside it from the EnterNotify that says
 * the pointer arrived. A server that stops sending those, or sends them against the wrong window
 * while a grab is held, leaves the submenu drawn but dead: nothing in it highlights, and the press
 * that should open its item is read as a click outside the menu, which closes everything.
 *
 * <p>The expectations here are the ones a real X server was measured producing, request for request,
 * for the same gestures: a grab that reports events to itself confines crossings to the window that
 * holds it, an owner-events grab does not, and an explicit grab decides which window the release of
 * a press that opened a popup belongs to. The crossing pairs a grab itself produces are deliberately
 * not among them - see the design notes.
 */
class GrabCrossingTest {

    /** A display number nothing else in the suite uses; the server listens on TCP 6000 + it. */
    private static final int DISPLAY = 95;

    private static final int ENTER_NOTIFY = 7;
    private static final int BUTTON_RELEASE = 5;

    private static final int BUTTON_PRESS_MASK = 0x4;
    private static final int BUTTON_RELEASE_MASK = 0x8;
    private static final int ENTER_WINDOW_MASK = 0x10;
    private static final int LEAVE_WINDOW_MASK = 0x20;
    private static final int POINTER_MOTION_MASK = 0x40;

    private static final int CROSSING_MASK = ENTER_WINDOW_MASK | LEAVE_WINDOW_MASK;
    private static final int MENU_MASK = CROSSING_MASK | BUTTON_PRESS_MASK | BUTTON_RELEASE_MASK
            | POINTER_MOTION_MASK;

    private static final int NORMAL = 0;

    private XServer server;
    private RawXClient client;
    private int xtest;
    private int menu;
    private int submenu;

    @BeforeEach
    void startServerAndTwoWindows() throws Exception {
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--backend", "headless", "--no-unix"}));
        server.start();
        client = RawXClient.connect(DISPLAY);
        xtest = client.queryExtension("XTEST");
        menu = client.allocateId();
        submenu = client.allocateId();
        client.createWindow(menu, client.root(), 100, 100, 200, 200);
        client.createWindow(submenu, client.root(), 400, 100, 200, 200);
        client.selectInput(menu, MENU_MASK);
        client.selectInput(submenu, MENU_MASK);
        client.mapWindow(menu);
        client.mapWindow(submenu);
        client.flush();
    }

    @AfterEach
    void stopServer() throws Exception {
        if (client != null) {
            client.close();
        }
        server.stop();
    }

    /**
     * Moving into a window that holds the pointer is still reported to it: without that the submenu
     * never highlights, because the toolkit is still watching the menu it left.
     */
    @Test
    void theWindowHoldingThePointerHearsThePointerArrive() throws Exception {
        client.fakeMotion(xtest, 150, 150);
        client.flush();
        client.grabPointer(submenu, false, MENU_MASK);
        client.flush();

        client.fakeMotion(xtest, 500, 200);
        client.flush();

        byte[] entered = client.readEvent(ENTER_NOTIFY);
        assertNotNull(entered, "the submenu never heard the pointer arrive");
        assertEquals(submenu, RawXClient.readInt(entered, 12));
        assertEquals(NORMAL, entered[30] & 0xFF, "the pointer's own move, not the grab's");
    }

    /**
     * A menu that appears under the pointer while a button is held is the case that goes wrong when
     * the implicit grab wins: the release belongs to the window under the pointer, as it does on a
     * real server, and reporting it against the window the press landed on is a release the menu
     * reads as a click outside itself.
     */
    @Test
    void aReleaseBelongsToTheWindowUnderAnOwnerEventsGrab() throws Exception {
        int popup = client.allocateId();
        client.fakeMotion(xtest, 150, 150);
        client.fakeButton(xtest, 1, true);
        client.flush();
        client.readEvent(4);                        // the press, against the menu

        // The popup appears under the pointer while the button is still down, and the client grabs
        // the pointer on its main window for it.
        client.createWindow(popup, client.root(), 120, 120, 100, 100);
        client.selectInput(popup, MENU_MASK);
        client.mapWindow(popup);
        client.grabPointer(menu, true, MENU_MASK);
        client.flush();

        client.fakeButton(xtest, 1, false);
        client.flush();

        byte[] release = client.readEvent(BUTTON_RELEASE);
        assertNotNull(release, "the release was never delivered");
        assertEquals(popup, RawXClient.readInt(release, 12),
                "the release belongs to the window the pointer is in, not the one the press hit");
    }
}
