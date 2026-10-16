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
 * Synchronous grabs and {@code AllowEvents} - backlog A13.
 *
 * <p>A grab taken with {@code GrabModeSync} is a client asking the server to <b>stop the device</b>
 * after the event that triggered it, so it can decide what happens to what comes next: a game that
 * re-centres the cursor after every motion, a remote-desktop client that re-delivers a click from
 * somewhere else. Everything the stopped device produces waits, and {@code AllowEvents} is what
 * starts it again - or reprocesses the event it stopped on.
 *
 * <p>The rules pinned here were read off Xorg ({@code include/inputstr.h}'s grab states,
 * {@code dix/events.c}'s {@code AllowSome}/{@code FreezeThisEventIfNeededForSyncGrab}/
 * {@code ComputeFreezes}) and then measured against a reference server with
 * {@code tools/AllowEventsProbe}, whose five arrangements now agree line for line. Two of the rules
 * cannot be guessed from the protocol specification and are the reason the item was open:
 *
 * <ul>
 *   <li><b>{@code Sync} is not "one event".</b> It means "start again, and stop on the next key or
 *       button" - motions are <em>not</em> what stops it, so a release lets everything through up to
 *       and including the first button or key.
 *   <li><b>A replay is not a second grab.</b> The event is reprocessed as if the grab had not fired,
 *       which means the search for a passive grab starts <em>below</em> the window the replayed grab
 *       was held on - so the same arm cannot take it again.
 * </ul>
 *
 * <p>The arrangement is the one the probe uses, and it needs two clients for the replay cases: the
 * client that armed the grab and the client whose window is under the pointer. One client alone
 * cannot tell "the grab delivered it" from "the application delivered it".
 */
class AllowEventsTest {

    /** A display number nothing else in the suite uses; the server listens on TCP 6000 + it. */
    private static final int DISPLAY = 71;

    /** Selected-event masks, and the event *types* they are not (see {@code PassiveGrabTest}). */
    private static final int BUTTON_PRESS_EVENT = 4;
    private static final int BUTTON_RELEASE_EVENT = 5;
    private static final int MOTION_EVENT = 6;
    private static final int LEAVE_EVENT = 8;
    private static final int KEY_PRESS_EVENT = 2;
    private static final int KEY_RELEASE_EVENT = 3;

    private static final int BUTTON = 0x4 | 0x8;
    private static final int MOTION = 0x40;
    private static final int CROSSINGS = 0x10 | 0x20;
    private static final int KEYS = 0x1 | 0x2;

    /** {@code AllowEvents} modes, as the protocol numbers them. */
    private static final int ASYNC_POINTER = 0;
    private static final int SYNC_POINTER = 1;
    private static final int REPLAY_POINTER = 2;
    private static final int REPLAY_KEYBOARD = 5;

    private static final int ANY = 0;
    private static final int ANY_MODIFIER = 0x8000;
    private static final int REVERT_TO_PARENT = 2;

    /** A keycode with a keysym on it; which one does not matter, only that it is typed. */
    private static final int SOME_KEY = 38;

    private XServer server;
    private RawXClient actor;
    private RawXClient application;
    private int xtest;
    private int window;

    @BeforeEach
    void startServerAndTwoClients() throws Exception {
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--geometry", "600x400", "--backend", "headless",
                // One shared screen: XTEST is scoped to the screen of the client that sends it, and
                // the two clients here have to be looking at the same pointer and keyboard.
                "--single-window", "--no-unix"}));
        server.start();
        actor = RawXClient.connect(DISPLAY);
        application = RawXClient.connect(DISPLAY);
        xtest = application.queryExtension("XTEST");
        window = application.allocateId();
        application.createWindow(window, application.root(), 100, 100, 200, 200, 0x00FF00);
        application.selectInput(window, BUTTON | KEYS | CROSSINGS | MOTION);
        application.mapWindow(window);
        application.flush();
        // The pointer over the application's window, which is where a grab's events land.
        application.fakeMotion(xtest, 200, 200);
        application.flush();
        quiet(actor);
        quiet(application);
    }

    @AfterEach
    void stopServer() {
        close(actor);
        close(application);
        server.stop();
    }

    private void close(RawXClient client) {
        try {
            client.close();
        } catch (Exception ignored) {
            // Best effort: a test that already failed is the one that notices.
        }
    }

    private void assertNothing(RawXClient client, int type, String why) throws Exception {
        client.readTimeoutMillis(200);
        assertNull(client.readEvent(type), why);
        client.readTimeoutMillis(5000);
    }

    private void quiet(RawXClient client) throws Exception {
        client.readTimeoutMillis(200);
        while (client.readEvent() != null) {
            // Quiet is the end of it.
        }
        client.readTimeoutMillis(5000);
    }

    /** Arms a button grab on the root - a global shortcut - with the modes spelled out. */
    private void armButtonGrab(RawXClient client, int pointerMode, int keyboardMode)
            throws Exception {
        client.grabButton(client.root(), false, BUTTON, ANY, ANY_MODIFIER, pointerMode,
                keyboardMode);
        client.flush();
        client.queryInputFocus();                   // a round trip: the arm is in place afterwards
    }

    private void pressMoveRelease() throws Exception {
        actor.fakeButton(xtest, 1, true);
        actor.fakeMotion(xtest, 220, 220);
        actor.fakeMotion(xtest, 240, 240);
        actor.fakeButton(xtest, 1, false);
        actor.flush();
    }

    /**
     * The core of it: a {@code Sync} grab stops the device, and nothing arrives until the client
     * says so.
     *
     * <p>The first release is {@code SyncPointer}, and what it lets through is the event the device
     * froze <em>on</em> - the press. It then stops the device again, so the motions behind it wait
     * for the next release. That asymmetry between the two releases is the item's own puzzle, and it
     * is Xorg's rule: only a key or a button stops a device that was told to freeze on the next one.
     */
    @Test
    void aSyncPointerGrabHoldsTheEventsUntilTheClientAllowsThem() throws Exception {
        assertEquals(0, actor.grabPointer(actor.root(), false, BUTTON | MOTION,
                RawXClient.GRAB_SYNC, RawXClient.GRAB_SYNC), "the grab was refused");

        pressMoveRelease();

        assertNothing(actor, BUTTON_PRESS_EVENT, "a frozen device delivered a press");
        assertNothing(actor, MOTION_EVENT, "a frozen device delivered a motion");
        assertNothing(actor, BUTTON_RELEASE_EVENT, "a frozen device delivered a release");

        actor.allowEvents(SYNC_POINTER);
        actor.flush();
        assertNotNull(actor.readEvent(BUTTON_PRESS_EVENT),
                "SyncPointer did not release the event the device froze on");
        assertNothing(actor, MOTION_EVENT,
                "SyncPointer released more than the one event it stops on again");

        actor.allowEvents(ASYNC_POINTER);
        actor.flush();
        assertNotNull(actor.readEvent(MOTION_EVENT), "AsyncPointer did not release the motions");
        assertNotNull(actor.readEvent(MOTION_EVENT), "and not all of them");
        assertNotNull(actor.readEvent(BUTTON_RELEASE_EVENT), "and not the release");
    }

    /**
     * The two positions a frozen device has, and why a client can tell them apart.
     *
     * <p>{@code QueryPointer} answers where the pointer the <em>clients</em> see is, and while the
     * device is frozen that is where it was when it stopped: the user's motion is not shown. The
     * events that come out when the client lets them through carry the position the device reached -
     * which is the whole point for a game that froze the pointer to re-centre it, and for a
     * remote-desktop client that has to redeliver a click where the pointer now is.
     */
    @Test
    void aFrozenPointerStaysWhereTheClientsSeeItAndTheEventsCarryTheNewPosition() throws Exception {
        assertEquals(0, actor.grabPointer(actor.root(), false, BUTTON | MOTION,
                RawXClient.GRAB_SYNC, RawXClient.GRAB_SYNC));
        actor.fakeButton(xtest, 1, true);
        actor.fakeMotion(xtest, 300, 300);
        actor.flush();

        int[] seen = actor.queryPointer(actor.root());
        assertEquals(200, seen[0], "a frozen pointer moved as far as the clients are concerned");
        assertEquals(200, seen[1], "a frozen pointer moved as far as the clients are concerned");

        // SyncPointer releases the press - and the device is told to freeze on the next key or
        // button, which is what keeps the held motion waiting.
        actor.allowEvents(SYNC_POINTER);
        actor.flush();
        byte[] press = actor.readEvent(BUTTON_PRESS_EVENT);
        assertNotNull(press, "the press was not released");
        assertEquals(300, RawXClient.readShort(press, 24),
                "the released press was stamped with the position the pointer had when it stopped");

        actor.allowEvents(ASYNC_POINTER);
        actor.flush();
        byte[] motion = actor.readEvent(MOTION_EVENT);
        assertNotNull(motion, "the held motion was not released");
        assertEquals(300, RawXClient.readShort(motion, 24),
                "the held motion was delivered at a position the device had already left");
    }

    /**
     * A replay is "reprocess this as if my grab had not fired", and the measurement that makes it
     * mean something is that the event reaches the window <em>underneath</em>.
     *
     * <p>The client that armed the grab is told nothing more - no second {@code NotifyGrab}, no
     * second press - and the application gets the press it would have got had the grab never been
     * armed. A server that simply fired the grab again would look identical from the grabber's side
     * and be wrong from the application's, which is why this needs two clients.
     */
    @Test
    void aReplayReprocessesTheEventAsIfTheGrabHadNotFired() throws Exception {
        armButtonGrab(actor, RawXClient.GRAB_SYNC, RawXClient.GRAB_SYNC);

        application.fakeButton(xtest, 1, true);
        application.flush();
        assertNotNull(actor.readEvent(BUTTON_PRESS_EVENT),
                "the press did not go to the client that armed the grab");
        byte[] left = application.readEvent(LEAVE_EVENT);
        assertNotNull(left, "the application was not told the grab took the pointer");
        assertEquals(1, left[30] & 0xFF, "the crossing does not say a grab did it: NotifyGrab");

        actor.allowEvents(REPLAY_POINTER);
        actor.flush();

        byte[] replayed = application.readEvent(BUTTON_PRESS_EVENT);
        assertNotNull(replayed, "the replayed press never reached the window underneath");
        assertEquals(window, RawXClient.readInt(replayed, 12),
                "the replayed press is not reported against the window under the pointer");
        assertNothing(actor, BUTTON_PRESS_EVENT,
                "the grab the replay removed fired a second time on the replayed press");

        application.fakeButton(xtest, 1, false);
        application.flush();
        assertNotNull(application.readEvent(BUTTON_RELEASE_EVENT),
                "the release after a replay did not reach the application either");
    }

    /**
     * The keyboard's replay, which is the arrangement where it is unmistakable: the key goes to the
     * window that has the focus, and that is another client's.
     */
    @Test
    void aReplayedKeyReachesTheWindowThatHasTheFocus() throws Exception {
        application.setInputFocus(window, REVERT_TO_PARENT);
        application.flush();
        actor.grabKey(actor.root(), false, ANY_MODIFIER, ANY, RawXClient.GRAB_SYNC,
                RawXClient.GRAB_SYNC);
        actor.flush();
        actor.queryInputFocus();
        quiet(application);

        application.fakeKey(xtest, SOME_KEY, true);
        application.fakeKey(xtest, SOME_KEY, false);
        application.flush();

        assertNotNull(actor.readEvent(KEY_PRESS_EVENT), "the key press did not go to the grab");
        assertNothing(application, KEY_PRESS_EVENT, "the focused window heard a key the grab took");
        assertNothing(application, KEY_RELEASE_EVENT,
                "the focused window heard the release of a key the grab took");

        actor.allowEvents(REPLAY_KEYBOARD);
        actor.flush();

        assertNotNull(application.readEvent(KEY_PRESS_EVENT),
                "the replayed press did not reach the focused window");
        assertNotNull(application.readEvent(KEY_RELEASE_EVENT),
                "the release the grab was holding did not reach it either");
    }

    /**
     * X's time check, and it is a silent one: a request that names a time outside the grab is
     * <em>ignored</em> rather than refused, so the device stays frozen and the client has to ask again
     * with a time it can use. A time before the grab and one in the future are both refused; the
     * {@code CurrentTime} every real client sends is not.
     */
    @Test
    void aTimeOutsideTheGrabReleasesNothing() throws Exception {
        assertEquals(0, actor.grabPointer(actor.root(), false, BUTTON | MOTION,
                RawXClient.GRAB_SYNC, RawXClient.GRAB_SYNC));
        pressMoveRelease();

        actor.allowEvents(SYNC_POINTER, 1);
        actor.flush();
        assertNothing(actor, BUTTON_PRESS_EVENT, "a time before the grab released the device");

        actor.allowEvents(SYNC_POINTER, (int) (System.currentTimeMillis() + 60000));
        actor.flush();
        assertNothing(actor, BUTTON_PRESS_EVENT, "a time after now released the device");

        actor.allowEvents(SYNC_POINTER);
        actor.flush();
        assertNotNull(actor.readEvent(BUTTON_PRESS_EVENT),
                "CurrentTime did not release the event the device froze on");
    }

    /**
     * The guard: an <em>asynchronous</em> grab never stops anything, which is what every live client
     * here uses (measured for EasyShell and termora) and therefore the case that must not change.
     */
    @Test
    void anAsyncGrabNeverHoldsAnythingBack() throws Exception {
        assertEquals(0, actor.grabPointer(actor.root(), false, BUTTON | MOTION,
                RawXClient.GRAB_ASYNC, RawXClient.GRAB_ASYNC));

        pressMoveRelease();

        assertNotNull(actor.readEvent(BUTTON_PRESS_EVENT), "an async grab held the press back");
        assertNotNull(actor.readEvent(MOTION_EVENT), "an async grab held a motion back");
        assertNotNull(actor.readEvent(BUTTON_RELEASE_EVENT), "an async grab held the release back");
    }
}
