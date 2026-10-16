package cn.oyzh.fx.xserver.client;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The type byte of the events the protocol declares as <em>copies</em> of another, read off the wire.
 *
 * <p>{@code xproto.xml} says a release is a press with another number, a leave is an enter, and so on,
 * and the generator in the protocol fork ignored the copy's own number - so the generated classes
 * carried the number of the event they copy, and a {@code ButtonRelease} went out as a second
 * {@code ButtonPress}. A client that saw that never completed a click, got every keystroke twice, and
 * tracked hover and focus backwards. {@code ClientOutput.EVENT_NUMBERS} rewrites the five bytes, and
 * <b>nothing tested it</b>: the failure is silent, it is in a dependency, and the rewrite is the only
 * thing standing between the generated classes and a client.
 *
 * <p><b>These tests drive the events the server can actually send and read the byte the client
 * receives</b>, which is the requirement rather than a re-implementation of it - so they pass with the
 * rewrite in place, and they pass with a generator that has been fixed (the rewrite is then a no-op).
 * They were shown to fail only when neither is there: with the published {@code 0.22.0} artifact put
 * back and the rewrite disabled, a {@code KeyRelease} arrives as type 2. The fifth event,
 * {@code CirculateRequest}, has no driver here - a server with no window manager never sends one - so
 * it is pinned where it lives, in the table the encoder uses.
 */
class EventTypeByteTest {

    /** A display number the rest of the suite does not use. */
    private static final int DISPLAY = 73;

    private static final int KEY_PRESS = 2;
    private static final int KEY_RELEASE = 3;
    private static final int BUTTON_PRESS = 4;
    private static final int BUTTON_RELEASE = 5;
    private static final int ENTER_NOTIFY = 7;
    private static final int LEAVE_NOTIFY = 8;
    private static final int FOCUS_IN = 9;
    private static final int FOCUS_OUT = 10;

    private static final int KEY_PRESS_MASK = 0x1;
    private static final int KEY_RELEASE_MASK = 0x2;
    private static final int BUTTON_PRESS_MASK = 0x4;
    private static final int BUTTON_RELEASE_MASK = 0x8;
    private static final int ENTER_WINDOW_MASK = 0x10;
    private static final int LEAVE_WINDOW_MASK = 0x20;
    private static final int FOCUS_CHANGE_MASK = 0x200000;

    private XServer server;
    private RawXClient client;
    private int xtest;

    @BeforeEach
    void startServer() throws Exception {
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--geometry", "800x600", "--backend", "headless", "--no-unix"}));
        server.start();
        client = RawXClient.connect(DISPLAY);
        xtest = client.queryExtension("XTEST");
    }

    @AfterEach
    void stopServer() throws IOException {
        if (client != null) {
            client.close();
        }
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void aKeyReleaseDoesNotArriveAsASecondPress() throws Exception {
        int window = keyWindow();
        // Keys go to the window with the focus, and buttons to the window under the pointer - so this
        // one needs the focus and the motion test does not.
        client.setInputFocus(window, 0);
        client.flush();
        client.fakeKey(xtest, 38, true);                    // 'a', the hardware's own keycode
        client.flush();
        assertEquals(KEY_PRESS, readType(window, KEY_PRESS), "the press is a press");

        client.fakeKey(xtest, 38, false);
        client.flush();
        byte[] release = client.readEvent(KEY_RELEASE);
        assertNotNull(release, "a key that comes up is a release: without the correction the generated"
                + " KeyReleaseEvent carries the press's number and a client never sees one");
        assertEquals(KEY_RELEASE, release[0] & 0x7F);
        // And it names the key that came up, not some other one: a release that reports the wrong
        // keycode is a client stuck with a key held down for ever.
        assertEquals(38, release[1] & 0xFF, "the keycode that came up");
    }

    @Test
    void aButtonReleaseDoesNotArriveAsASecondPress() throws IOException {
        int window = keyWindow();
        client.fakeButton(xtest, 1, true);
        client.flush();
        assertEquals(BUTTON_PRESS, readType(window, BUTTON_PRESS), "the press is a press");

        client.fakeButton(xtest, 1, false);
        client.flush();
        byte[] release = client.readEvent(BUTTON_RELEASE);
        assertNotNull(release, "a click that completes is the release arriving as itself - a button"
                + " that never releases is a drag a client keeps starting");
        assertEquals(BUTTON_RELEASE, release[0] & 0x7F);
    }

    @Test
    void leavingAWindowDoesNotArriveAsAnotherEnter() throws IOException {
        int window = keyWindow();
        client.fakeMotion(xtest, 100, 100);
        client.flush();
        assertEquals(ENTER_NOTIFY, readType(window, ENTER_NOTIFY), "the pointer arrives");

        client.fakeMotion(xtest, 600, 500);                 // outside the 400x300 window
        client.flush();
        byte[] leave = client.readEvent(LEAVE_NOTIFY);
        assertNotNull(leave, "and it leaves: a leave that arrives as an enter is a client that thinks"
                + " the pointer is in a window it left, which is how a hover sticks");
        assertEquals(LEAVE_NOTIFY, leave[0] & 0x7F);
    }

    @Test
    void losingTheFocusDoesNotArriveAsAnotherFocusIn() throws IOException {
        int first = client.allocateId();
        int second = client.allocateId();
        client.createWindow(first, client.root(), 0, 0, 200, 200);
        client.createWindow(second, client.root(), 300, 0, 200, 200);
        client.selectInput(first, FOCUS_CHANGE_MASK);
        client.selectInput(second, FOCUS_CHANGE_MASK);
        client.mapWindow(first);
        client.mapWindow(second);
        client.setInputFocus(first, 0);
        client.flush();
        assertEquals(FOCUS_IN, readType(first, FOCUS_IN), "the first window is given the focus");

        client.setInputFocus(second, 0);
        client.flush();
        byte[] out = client.readEvent(FOCUS_OUT);
        assertNotNull(out, "and the one that had it hears that it lost it: a focus-out arriving as a"
                + " focus-in is the state a menu reads to decide whether to stay open");
        assertEquals(FOCUS_OUT, out[0] & 0x7F);
    }

    /**
     * The fifth event has no driver in this server - {@code CirculateRequest} goes to a window manager
     * when a client circulates a window, and there is no window manager here - so what is pinned is
     * the table the encoder reads, which is what a client's byte would come from if it ever did.
     */
    @Test
    void theCirculateRequestTheServerCannotSendIsStillCorrected() throws Exception {
        Field table = Class.forName("cn.oyzh.fx.xserver.net.ClientOutput")
                .getDeclaredField("EVENT_NUMBERS");
        table.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Class<?>, Byte> numbers = (Map<Class<?>, Byte>) table.get(null);
        assertEquals(5, numbers.size(), "the five copies xproto declares");
        assertTrue(numbers.containsKey(com.github.moaxcp.x11.protocol.xproto
                .CirculateRequestEvent.class) && numbers.get(com.github.moaxcp.x11.protocol.xproto
                .CirculateRequestEvent.class) == (byte) 27,
                "a circulate request is 27, not the 26 of the CirculateNotify it copies");
    }

    /** A mapped window that reports what happens to it. */
    private int keyWindow() throws IOException {
        int window = client.allocateId();
        client.createWindow(window, client.root(), 0, 0, 400, 300);
        client.selectInput(window, KEY_PRESS_MASK | KEY_RELEASE_MASK | BUTTON_PRESS_MASK
                | BUTTON_RELEASE_MASK | ENTER_WINDOW_MASK | LEAVE_WINDOW_MASK);
        client.mapWindow(window);
        client.flush();
        return window;
    }

    /** The type byte of the next event of that kind, as the client receives it. */
    private int readType(int window, int wanted) throws IOException {
        byte[] event = client.readEvent(wanted);
        assertNotNull(event, "the window " + window + " was told about the event");
        return event[0] & 0x7F;
    }
}
