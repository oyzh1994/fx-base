package cn.oyzh.fx.xserver.extension;

import cn.oyzh.fx.xserver.extension.XFixesExtension;
import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * The XFixes selection and cursor notifications.
 *
 * <p>{@code SelectSelectionInput} and {@code SelectCursorInput} used to be recorded and never
 * answered, so a clipboard manager or a cursor-theme tool - the clients that subscribe instead of
 * polling - saw nothing ever change. The events are raised from the code that changes a selection's
 * owner and from the display tick, which is outside this extension, so what these tests pin is
 * mostly the contract between the two: which event, with which window, and with which of the two
 * timestamps.
 *
 * <p>Every expectation here was measured against a reference server first - the probes that did it
 * are {@code tools/SelectionProbe} and {@code tools/CursorProbe} in the verification kit, and their
 * output against Xvfb is what these assertions encode.
 *
 * <p>Single-window mode, because the cursor test drives the pointer through XTEST and XTEST is
 * scoped to the sender's own screen: in the default mode a probe client cannot reach another
 * connection's windows.
 */
class XFixesNotifyTest {

    /** A display number the rest of the suite does not use. */
    private static final int DISPLAY = 89;

    /** The three subscription bits, so the mask in each test says what it is asking for. */
    private static final int SET_OWNER = 0b1;
    private static final int WINDOW_DESTROY = 0b10;
    private static final int CLIENT_CLOSE = 0b100;
    private static final int ALL = SET_OWNER | WINDOW_DESTROY | CLIENT_CLOSE;

    private static XServer server;

    @BeforeAll
    static void startServer() throws IOException {
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--backend", "headless", "--single-window", "--no-unix"}));
        server.start();
        // The cursor notification is the display tick's work - the tick is what knows which window
        // the pointer is over - so this test needs the loop. Without it the server only answers
        // requests and announces nothing on its own. The selection notifications do not wait for it,
        // which is why those tests would pass either way.
        server.startEventLoop();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void subscribersHearTheOwnerChange() throws IOException {
        try (RawXClient observer = RawXClient.connect(DISPLAY)) {
            int firstEvent = subscribe(observer, "A8_ONE", ALL);

            try (RawXClient owner = RawXClient.connect(DISPLAY)) {
                int selection = owner.internAtom("A8_ONE");
                int holder = owner.allocateId();
                owner.createWindow(holder, owner.root(), 0, 0, 100, 100);

                owner.setSelectionOwner(holder, selection, 0);
                owner.flush();

                byte[] event = readXfixesEvent(observer, firstEvent);
                assertNotNull(event, "taking a selection must be reported to its subscribers");
                assertEquals(0, event[1] & 0xFF, "the subtype of a new owner");
                assertEquals(holder, RawXClient.readInt(event, 8), "the new owner");
                assertEquals(selection, RawXClient.readInt(event, 12), "the selection");
                // For a change of hands the two timestamps are the same instant; they only come
                // apart when the owner goes away rather than the selection changing. An
                // implementation that reads the clock twice lands on the same millisecond most runs
                // and this passes by luck - `tools/probe-diff.py` on the two probe runs is what
                // actually pins it, and it is how the one that did exactly that was found.
                assertEquals(RawXClient.readInt(event, 16), RawXClient.readInt(event, 20),
                        "the selection was taken now, and the event happened now");
            }
        }
    }

    @Test
    void theSubscribedWindowIsTheOneTheEventNames() throws IOException {
        try (RawXClient observer = RawXClient.connect(DISPLAY)) {
            // Two windows to subscribe on, so "which window" cannot be guessed from there being one.
            int first = observer.allocateId();
            int second = observer.allocateId();
            observer.createWindow(first, observer.root(), 0, 0, 50, 50);
            observer.createWindow(second, observer.root(), 0, 0, 50, 50);
            int[] xfixes = observer.queryExtensionInfo("XFIXES");
            observer.queryXfixesVersion(xfixes[1], 4, 0);
            int selection = observer.internAtom("A8_TWO");
            observer.selectSelectionInput(xfixes[1], second, selection, SET_OWNER);
            observer.flush();

            try (RawXClient owner = RawXClient.connect(DISPLAY)) {
                owner.internAtom("A8_TWO");
                int holder = owner.allocateId();
                owner.createWindow(holder, owner.root(), 0, 0, 100, 100);
                owner.setSelectionOwner(holder, selection, 0);
                owner.flush();

                byte[] event = readXfixesEvent(observer, xfixes[2]);
                assertNotNull(event);
                assertEquals(second, RawXClient.readInt(event, 4),
                        "the event names the window the subscription was made on, not the owner and "
                                + "not the window under the pointer");
            }
        }
    }

    @Test
    void destroyingTheOwnersWindowIsReportedAsTheWindowGoingAway() throws IOException {
        try (RawXClient observer = RawXClient.connect(DISPLAY)) {
            int firstEvent = subscribe(observer, "A8_THREE", ALL);

            RawXClient owner = RawXClient.connect(DISPLAY);
            int selection;
            try {
                selection = owner.internAtom("A8_THREE");
                int holder = owner.allocateId();
                owner.createWindow(holder, owner.root(), 0, 0, 100, 100);
                owner.setSelectionOwner(holder, selection, 0);
                owner.flush();

                byte[] taken = readXfixesEvent(observer, firstEvent);
                assertNotNull(taken);
                int ownershipTime = RawXClient.readInt(taken, 20);

                owner.destroyWindow(holder);
                owner.flush();

                byte[] event = readXfixesEvent(observer, firstEvent);
                assertNotNull(event, "the selection lost its window and the subscribers must be told");
                assertEquals(XFixesExtension.SELECTION_WINDOW_DESTROY, event[1] & 0xFF,
                        "a destroyed owner window is its own subtype, not a client closing");
                assertEquals(0, RawXClient.readInt(event, 8), "the selection belongs to nobody now");
                assertEquals(selection, RawXClient.readInt(event, 12));
                assertEquals(ownershipTime, RawXClient.readInt(event, 20),
                        "nothing took the selection away - it lost the window that held it - so the "
                                + "selection's own time does not move");
            } finally {
                owner.close();
            }

            assertNull(readXfixesEvent(observer, firstEvent),
                    "and the client closing afterwards is not reported a second time");
        }
    }

    @Test
    void aDisconnectingOwnerIsReportedAsTheClientGoingAway() throws IOException {
        try (RawXClient observer = RawXClient.connect(DISPLAY)) {
            int firstEvent = subscribe(observer, "A8_FOUR", ALL);

            RawXClient owner = RawXClient.connect(DISPLAY);
            int selection = owner.internAtom("A8_FOUR");
            int holder = owner.allocateId();
            owner.createWindow(holder, owner.root(), 0, 0, 100, 100);
            owner.setSelectionOwner(holder, selection, 0);
            owner.flush();
            assertNotNull(readXfixesEvent(observer, firstEvent));

            owner.close();

            byte[] event = readXfixesEvent(observer, firstEvent);
            assertNotNull(event, "a client that owns a selection cannot leave unannounced");
            assertEquals(XFixesExtension.SELECTION_CLIENT_CLOSE, event[1] & 0xFF,
                    "a window still exists - what went away is the client");
            assertEquals(0, RawXClient.readInt(event, 8));
        }
    }

    @Test
    void theSubscriptionGoesWithTheWindowItWasMadeOn() throws IOException {
        try (RawXClient observer = RawXClient.connect(DISPLAY)) {
            int[] xfixes = observer.queryExtensionInfo("XFIXES");
            observer.queryXfixesVersion(xfixes[1], 4, 0);
            int selection = observer.internAtom("A8_FIVE");
            int watched = observer.allocateId();
            observer.createWindow(watched, observer.root(), 0, 0, 50, 50);
            observer.selectSelectionInput(xfixes[1], watched, selection, ALL);
            observer.flush();

            observer.destroyWindow(watched);
            observer.flush();

            try (RawXClient owner = RawXClient.connect(DISPLAY)) {
                owner.internAtom("A8_FIVE");
                int holder = owner.allocateId();
                owner.createWindow(holder, owner.root(), 0, 0, 100, 100);
                owner.setSelectionOwner(holder, selection, 0);
                owner.flush();
                assertNull(readXfixesEvent(observer, xfixes[2]),
                        "the subscription was a resource of a window that no longer exists: a "
                                + "reference server stops notifying and so does this one");
            }
        }
    }

    @Test
    void aClientThatDidNotSubscribeHearsNothing() throws IOException {
        try (RawXClient observer = RawXClient.connect(DISPLAY)) {
            int firstEvent = subscribe(observer, "A8_SIX", ALL);

            // The other selection, not the one subscribed to: subscriptions are per selection.
            try (RawXClient owner = RawXClient.connect(DISPLAY)) {
                int other = owner.internAtom("A8_SIX_OTHER");
                int holder = owner.allocateId();
                owner.createWindow(holder, owner.root(), 0, 0, 100, 100);
                owner.setSelectionOwner(holder, other, 0);
                owner.flush();
                assertNull(readXfixesEvent(observer, firstEvent),
                        "a subscription names one selection, not every selection");
            }

            // And a selection nobody subscribed to produces nothing either.
            int watched = observer.allocateId();
            observer.createWindow(watched, observer.root(), 0, 0, 50, 50);
            observer.selectSelectionInput(observer.queryExtensionInfo("XFIXES")[1], watched,
                    observer.internAtom("A8_SIX_WATCHED"), SET_OWNER);
            observer.flush();
            try (RawXClient owner = RawXClient.connect(DISPLAY)) {
                int unwatched = owner.internAtom("A8_SIX_UNWATCHED");
                int holder = owner.allocateId();
                owner.createWindow(holder, owner.root(), 0, 0, 100, 100);
                owner.setSelectionOwner(holder, unwatched, 0);
                owner.flush();
                assertNull(readXfixesEvent(observer, firstEvent));
            }
        }
    }

    /**
     * The cursor notification, and the thing about it that is easy to get wrong: the serial names
     * the cursor rather than the change, so going back to a cursor already displayed reports its
     * original number again. Measured on a reference server, whose serials do exactly that.
     */
    @Test
    void theCursorNotificationNamesTheCursorRatherThanTheChange() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int[] xfixes = client.queryExtensionInfo("XFIXES");
            client.queryXfixesVersion(xfixes[1], 4, 0);
            int firstEvent = xfixes[2];
            int xtest = client.queryExtension("XTEST");

            int watched = client.allocateId();
            client.createWindow(watched, client.root(), 0, 0, 50, 50);
            client.mapWindow(watched);
            client.selectCursorInput(xfixes[1], watched, 0b1);
            client.flush();

            // Two cursors out of the font every X client has: the arrow and the pointing hand.
            int font = client.allocateId();
            client.openFont(font, "cursor");
            client.flush();
            int arrow = client.allocateId();
            int hand = client.allocateId();
            client.createGlyphCursor(arrow, font, font, 68, 69, 0, 0xFFFFFF);
            client.createGlyphCursor(hand, font, font, 60, 61, 0, 0xFFFFFF);
            client.flush();

            int first = client.allocateId();
            int second = client.allocateId();
            client.createWindow(first, client.root(), 100, 100, 200, 200, 0x00FF00);
            client.createWindow(second, client.root(), 500, 100, 200, 200, 0x0000FF);
            client.setWindowCursor(first, arrow);
            client.setWindowCursor(second, hand);
            client.mapWindow(first);
            client.mapWindow(second);
            client.flush();

            client.fakeMotion(xtest, 200, 200);
            client.flush();
            byte[] onArrow = readCursorEvent(client, firstEvent);
            assertNotNull(onArrow, "moving onto a window with its own cursor is a cursor change");
            assertEquals(watched, RawXClient.readInt(onArrow, 4),
                    "the event names the subscribed window");

            client.fakeMotion(xtest, 600, 200);
            client.flush();
            byte[] onHand = readCursorEvent(client, firstEvent);
            assertNotNull(onHand, "and so is moving onto a window with a different one");
            assertNotEquals(RawXClient.readInt(onArrow, 8), RawXClient.readInt(onHand, 8),
                    "two cursors have two serials");

            client.fakeMotion(xtest, 200, 200);
            client.flush();
            byte[] backOnArrow = readCursorEvent(client, firstEvent);
            assertNotNull(backOnArrow);
            assertEquals(RawXClient.readInt(onArrow, 8), RawXClient.readInt(backOnArrow, 8),
                    "the serial is the cursor's, so the cursor already seen reports its own number");
        }
    }

    // ------------------------------------------------------------------
    // Harness
    // ------------------------------------------------------------------

    /** Subscribes on a window of its own to one selection, and returns the extension's first event. */
    private static int subscribe(RawXClient client, String selectionName, int mask)
            throws IOException {
        int[] xfixes = client.queryExtensionInfo("XFIXES");
        client.queryXfixesVersion(xfixes[1], 4, 0);
        int selection = client.internAtom(selectionName);
        int watched = client.allocateId();
        client.createWindow(watched, client.root(), 0, 0, 50, 50);
        client.selectSelectionInput(xfixes[1], watched, selection, mask);
        client.flush();
        return xfixes[2];
    }

    /** The next selection notification, or null when nothing arrives within the read timeout. */
    private static byte[] readXfixesEvent(RawXClient client, int firstEvent) throws IOException {
        return readType(client, firstEvent);
    }

    /** The next cursor notification, or null when nothing arrives within the read timeout. */
    private static byte[] readCursorEvent(RawXClient client, int firstEvent) throws IOException {
        return readType(client, firstEvent + 1);
    }

    /**
     * The next event of one type, or null when none arrives.
     *
     * <p>An error is raised rather than skipped. The events here are one-way requests, so the only
     * way a client learns its request was refused is this error arriving later - and a harness that
     * drops it reports "the server sent no notification", which is exactly what a broken
     * implementation looks like.
     */
    private static byte[] readType(RawXClient client, int type) throws IOException {
        client.readTimeoutMillis(1500);
        try {
            for (byte[] event = client.readEvent(); event != null; event = client.readEvent()) {
                int got = event[0] & 0x7F;
                if (got == type) {
                    return event;
                }
                if (got == 0) {
                    throw new IOException("the server refused a request: error "
                            + (event[1] & 0xFF) + " value 0x"
                            + Integer.toHexString(RawXClient.readInt(event, 4)) + " minor "
                            + RawXClient.readShort(event, 8) + " major " + (event[10] & 0xFF));
                }
            }
            return null;
        } finally {
            client.readTimeoutMillis(5000);
        }
    }
}
