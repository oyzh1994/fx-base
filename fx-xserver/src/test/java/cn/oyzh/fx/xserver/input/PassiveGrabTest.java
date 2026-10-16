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
 * Passive grabs: {@code GrabButton} and {@code GrabKey}, the "when this happens, the input is mine"
 * that a global shortcut, a toolkit's popup binding and a window manager's frame buttons are made of.
 *
 * <p>They were recorded and never matched, so a client that armed one never heard about the button or
 * the key it asked for - the last hole in the input model. The rules implemented here were measured
 * against a reference server with {@code tools/GrabProbe}, and the arrangement that makes the
 * measurement possible is worth stating: **the grab is armed on the root, and the window the pointer is
 * over belongs to another client**. A press inside the grabbing client's own window belongs to it
 * whether or not a grab fired, and a covering window from another client cannot be made a child of the
 * grab window - so nothing else tells the two cases apart.
 *
 * <p>The grab is armed on the root because that is also the real case: {@code GrabButton} on the root
 * is how a client takes a global shortcut.
 */
class PassiveGrabTest {

    /** A display number nothing else in the suite uses; the server listens on TCP 6000 + it. */
    private static final int DISPLAY = 70;

    /** The masks a client selects with. */
    private static final int BUTTON_PRESS_MASK = 0x4;
    private static final int BUTTON_RELEASE_MASK = 0x8;
    private static final int KEY_PRESS_MASK = 0x1;
    private static final int KEY_RELEASE_MASK = 0x2;
    private static final int ENTER_WINDOW = 0x10;
    private static final int LEAVE_WINDOW = 0x20;

    private static final int BUTTON = BUTTON_PRESS_MASK | BUTTON_RELEASE_MASK;
    private static final int KEYS = KEY_PRESS_MASK | KEY_RELEASE_MASK;

    /**
     * And the event <em>types</em>, which are not the same numbers.
     *
     * <p>{@code ButtonPress} happens to be 4 both as a mask and as an event type, which is what makes
     * this worth writing down: {@code ButtonRelease} is mask 8 and type 5, {@code KeyPress} is mask 1
     * and type 2, {@code KeyRelease} is mask 2 and type 3. A test that reads with the mask waits for
     * an event that will never come - and it looks exactly like a server that delivers nothing, which
     * is how the first version of this test failed while the client was being sent everything.
     */
    private static final int BUTTON_RELEASE_EVENT = 5;
    private static final int KEY_PRESS_EVENT = 2;
    private static final int KEY_RELEASE_EVENT = 3;

    /** {@code AnyButton}/{@code AnyKey}, and {@code AnyModifier}: the protocol's own spelling. */
    private static final int ANY = 0;
    private static final int ANY_MODIFIER = 0x8000;

    private static final int SHIFT_MASK = 0x1;
    private static final int SHIFT_L = 50;

    private static final int ASYNC = 1;
    private static final int REVERT_TO_PARENT = 2;

    private XServer server;
    private RawXClient shortcut;
    private RawXClient application;
    private int xtest;
    private int window;

    @BeforeEach
    void startServerAndTwoClients() throws Exception {
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--geometry", "600x400", "--backend", "headless",
                // One shared screen, so one client's XTEST moves the pointer and the keyboard the
                // others see: XTEST is scoped to the screen of the client that sends it.
                "--single-window", "--no-unix"}));
        server.start();
        shortcut = RawXClient.connect(DISPLAY);
        application = RawXClient.connect(DISPLAY);
        xtest = application.queryExtension("XTEST");
        window = application.allocateId();
        application.createWindow(window, application.root(), 100, 100, 200, 200, 0x00FF00);
        application.selectInput(window, BUTTON | KEYS | ENTER_WINDOW | LEAVE_WINDOW);
        application.mapWindow(window);
        application.flush();
        // The pointer starts over the application's window: the arrangement the measurement needs.
        application.fakeMotion(xtest, 200, 200);
        application.flush();
        quiet(application);
    }

    @AfterEach
    void stopServer() {
        close(shortcut);
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

    /**
     * Nothing of that type arrives, checked with a short read timeout: waiting out the client's own
     * five seconds for every "and it heard nothing" assertion is most of a test suite's run time.
     */
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

    /** Arms a button grab on the root - a global shortcut - and waits for the server to have it. */
    private void armButtonGrab(int modifiers) throws Exception {
        shortcut.grabButton(shortcut.root(), false, BUTTON, ANY, modifiers, ASYNC, ASYNC);
        shortcut.flush();
        // A round trip after it, so the arm is in place before anything is pressed.
        shortcut.queryInputFocus();
    }

    /** One press and release of button 1, with the pointer where it already is. */
    private void click() throws Exception {
        application.fakeButton(xtest, 1, true);
        application.fakeButton(xtest, 1, false);
        application.flush();
    }

    /**
     * The whole point: the click belongs to the client that armed the grab, even though the pointer is
     * over another client's window - and that client is told nothing at all.
     */
    @Test
    void theClickGoesToTheClientThatArmedTheGrab() throws Exception {
        armButtonGrab(ANY_MODIFIER);
        click();

        byte[] press = shortcut.readEvent(BUTTON_PRESS_MASK);
        assertNotNull(press, "the client that armed the grab was not told the button went down");
        assertEquals(shortcut.root(), RawXClient.readInt(press, 12),
                "the event is reported against the grab window - the root it was armed on");
        byte[] release = shortcut.readEvent(BUTTON_RELEASE_EVENT);
        assertNotNull(release, "and it was not told the button came up");

        assertNothing(application, BUTTON_PRESS_MASK,
                "the client whose window was under the pointer heard a click it does not own");
    }

    /**
     * The active grab lasts from the press to the release; the *arm* lasts until the client disarms it.
     *
     * <p>Those are two different lifetimes and it is easy to write a test that confuses them - the
     * first version of this one did, and read as "the grab outlived the button". An armed shortcut
     * fires on every press; what ends with the button is the <em>grab</em>, and the application is
     * told so by the crossing pair that comes back the moment the button is released. Disarming it
     * with {@code UngrabButton} is what hands the clicks back.
     */
    @Test
    void theGrabEndsWithTheButtonAndTheArmWithUngrab() throws Exception {
        armButtonGrab(ANY_MODIFIER);
        click();
        assertNotNull(shortcut.readEvent(BUTTON_PRESS_MASK));
        assertNotNull(shortcut.readEvent(BUTTON_RELEASE_EVENT));

        // The pair that says the grab is over: the pointer is the application's again, and this is
        // how it is told - the same EnterNotify(NotifyUngrab) a reference server sends.
        byte[] back = application.readEvent(7);                 // EnterNotify
        assertNotNull(back, "the application was not told the pointer came back to it");
        // NotifyUngrab is 2 (NotifyNormal 0, NotifyGrab 1): the number is worth naming, because the
        // one before it means the opposite.
        assertEquals(2, back[30] & 0xFF, "and the mode says the grab let it go: NotifyUngrab");
        quiet(shortcut);
        quiet(application);

        // Armed still: the next click is the shortcut's too, which is the whole point of a shortcut.
        click();
        assertNotNull(shortcut.readEvent(BUTTON_PRESS_MASK),
                "an armed grab fired only once: a global shortcut has to keep firing");
        assertNothing(application, BUTTON_PRESS_MASK, "the application heard a click its shortcut owns");
        quiet(shortcut);
        quiet(application);

        // Disarmed: the click is the application's again.
        shortcut.ungrabButton(shortcut.root(), ANY, ANY_MODIFIER);
        shortcut.flush();
        shortcut.queryInputFocus();
        click();

        byte[] press = application.readEvent(BUTTON_PRESS_MASK);
        assertNotNull(press, "the click is still not the application's after UngrabButton");
        assertEquals(window, RawXClient.readInt(press, 12), "and it is reported against its own window");
        assertNothing(shortcut, BUTTON_PRESS_MASK, "the disarmed client heard a click");
    }

    /**
     * The modifier is part of the match: a grab armed with Shift fires when Shift is held and not
     * otherwise. Measured against a reference server, which fires it exactly the same way.
     */
    @Test
    void aGrabArmedWithAModifierOnlyFiresWithItHeld() throws Exception {
        armButtonGrab(SHIFT_MASK);
        click();

        byte[] press = application.readEvent(BUTTON_PRESS_MASK);
        assertNotNull(press, "the grab fired without the modifier it was armed with");
        assertEquals(window, RawXClient.readInt(press, 12));
        assertNothing(shortcut, BUTTON_PRESS_MASK, "the grab fired without its modifier");
        quiet(application);
        quiet(shortcut);

        application.fakeKey(xtest, SHIFT_L, true);
        click();
        application.fakeKey(xtest, SHIFT_L, false);
        application.flush();

        press = shortcut.readEvent(BUTTON_PRESS_MASK);
        assertNotNull(press, "the grab did not fire with its modifier held");
        assertEquals(1, RawXClient.readShort(press, 28) & SHIFT_MASK,
                "the event reports the modifier state the press happened in");
    }

    /**
     * A key grab consumes the press: the client that armed it gets the key, and the focused window
     * does not - which is what a global shortcut is for.
     */
    @Test
    void aKeyGrabTakesTheKeyFromTheFocusedWindow() throws Exception {
        shortcut.grabKey(shortcut.root(), false, ANY_MODIFIER, ANY, ASYNC, ASYNC);
        shortcut.flush();
        shortcut.queryInputFocus();

        // The application has the focus, so the key would be its without the grab.
        application.setInputFocus(window, REVERT_TO_PARENT);
        application.flush();
        quiet(application);

        application.fakeKey(xtest, 38, true);
        application.fakeKey(xtest, 38, false);
        application.flush();

        byte[] press = shortcut.readEvent(KEY_PRESS_EVENT);
        assertNotNull(press, "the client that armed the key grab was not told the key went down");
        assertEquals(38, press[1] & 0xFF, "and it is told which key");
        assertNotNull(shortcut.readEvent(KEY_RELEASE_EVENT), "nor that it came up");
        assertNothing(application, KEY_PRESS_EVENT,
                "the focused window was told about a key the grab had taken");
    }

    /**
     * And a client that armed nothing is not affected: the same click is its own. The guard that makes
     * the first test mean something.
     */
    @Test
    void withoutAGrabTheClickIsTheApplications() throws Exception {
        click();

        byte[] press = application.readEvent(BUTTON_PRESS_MASK);
        assertNotNull(press, "a click with no grab armed never arrived");
        assertEquals(window, RawXClient.readInt(press, 12));
        assertNothing(shortcut, BUTTON_PRESS_MASK, "an unarmed client was told about a click");
    }
}
