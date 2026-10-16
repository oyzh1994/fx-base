package cn.oyzh.fx.xserver.extension;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.extension.RecordTest;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The event base an extension answers in {@code QueryExtension} is where a client library installs
 * its wire-to-event converter, and that makes it a correctness question rather than bookkeeping.
 *
 * <p>This is A18, and the bug is worth restating because nothing in this server sends an event in
 * the range that was wrong. {@code XESetWireToEvent(dpy, first_event + i, handler)} writes straight
 * into Xlib's conversion table, one slot per event code - the same table Xlib uses for the core
 * events - and libXi calls it for its whole range the moment a client first touches XInput, which
 * AWT, GDK and every other toolkit do at startup through {@code XListInputDevices}. With
 * {@code first_event = 0}, those device converters landed on {@code KeyPress}, {@code ButtonPress},
 * {@code MotionNotify} and the crossings: Xlib then unpacked every click with
 * {@code XDeviceButtonEvent}'s layout, whose fields sit one word further along than
 * {@code XButtonEvent}'s, and AWT read the click's root <b>y</b> as the button number - a click at
 * y = 20 became "Nonexistent button 18", and the click was dropped because the button was neither 1
 * nor 2 nor 3. Measured 2026-10-03 by dumping the ninety six raw bytes AWT was handed next to the
 * same click's wire bytes.
 *
 * <p>The rule the two tests below pin: an extension whose client library hooks the range must not
 * advertise a base inside the core event numbers, whether or not this server ever sends an event
 * there; and the extensions that answer zero because they have no events at all (BIG-REQUESTS,
 * XC-MISC, Composite, Present, XGE - all measured against a reference, and {@link RecordTest} pins
 * RECORD's) stay at zero, because no client library hooks a range it is told is empty.
 */
class ExtensionEventBaseTest {

    /** A display number the rest of the suite does not use. */
    private static final int DISPLAY = 76;

    /**
     * Core events end at 35 and extensions start at 64 (see {@code ExtensionRegistry}). 64 is the
     * floor, not a choice: anything below it is a core event's own number.
     */
    private static final int FIRST_EXTENSION_EVENT = 64;

    private static XServer server;

    @BeforeAll
    static void startServer() throws IOException {
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--geometry", "600x400", "--backend", "headless", "--no-unix"}));
        server.start();
        server.startEventLoop();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void theHandshakeExtensionsReportAnEventBaseOutsideTheCoreEvents() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            for (String name : List.of("XInputExtension", "XKEYBOARD")) {
                int[] info = client.queryExtensionInfo(name);
                assertEquals(1, info[0], name + " is present");
                assertNotEquals(0, info[2], name + ": zero is where the core events live, and"
                        + " libXi and libX11's XKB support hook every code above the base they"
                        + " are given - A18");
                assertTrue(info[2] >= FIRST_EXTENSION_EVENT, name + " answers base event " + info[2]
                        + ", inside the core event numbers: the client library's converters would"
                        + " land on KeyPress, ButtonPress and MotionNotify");
            }
        }
    }

    @Test
    void everyExtensionThatSendsEventsReportsAUsableBase() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            // Every extension this server puts an event on the wire for; each one's base has to be
            // outside the core numbers, or the event arrives labelled as a core event that the
            // client is not listening for - and a hooked core event, as A18 showed, is worse.
            for (String name : List.of("SHAPE", "DAMAGE", "XFIXES", "RANDR", "SYNC",
                    "MIT-SCREEN-SAVER", "SECURITY", "XVideo")) {
                int[] info = client.queryExtensionInfo(name);
                assertEquals(1, info[0], name + " is present");
                assertTrue(info[2] >= FIRST_EXTENSION_EVENT,
                        name + " answers base event " + info[2]);
            }
        }
    }
}
