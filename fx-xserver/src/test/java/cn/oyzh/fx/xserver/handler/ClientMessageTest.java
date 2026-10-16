package cn.oyzh.fx.xserver.handler;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * A {@code ClientMessage} from one client to another's window.
 *
 * <p>Sent with no event mask, which is not the same as "nobody selected for it": X sends such an
 * event to the client that <em>created</em> the window it names. It has to work that way, because
 * every one of these messages is about something the target did not ask for in advance - a drag
 * entering its window, a window manager being asked for something - so a server that required a mask
 * would drop the lot. This is the shape XDND is built out of, which is where it was found.
 */
class ClientMessageTest {

    /** A display number the rest of the suite does not use. */
    private static final int DISPLAY = 69;

    private static final int CLIENT_MESSAGE = 33;

    private XServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void aMessageWithNoMaskReachesTheWindowItself() throws IOException {
        startServer();
        try (RawXClient target = RawXClient.connect(DISPLAY);
             RawXClient sender = RawXClient.connect(DISPLAY)) {
            // The target selects nothing at all: a drag does not announce itself before it arrives.
            int window = target.allocateId();
            target.createWindow(window, target.root(), 0, 0, 200, 200);
            target.mapWindow(window);
            target.flush();

            int type = sender.internAtom("XDND_ENTER");
            sender.sendClientMessage(window, type, 42, 7);
            sender.flush();

            byte[] message = target.readEvent(CLIENT_MESSAGE);
            assertNotNull(message, "a message sent to a window has to reach the client that owns it");
            assertEquals(type, RawXClient.readInt(message, 8), "with the type it was sent with");
            assertEquals(42, RawXClient.readInt(message, 12), "and its data intact");
            assertEquals(7, RawXClient.readInt(message, 16));
        }
    }

    @Test
    void aMaskedMessageStillNeedsTheMask() throws IOException {
        startServer();
        try (RawXClient target = RawXClient.connect(DISPLAY);
             RawXClient sender = RawXClient.connect(DISPLAY)) {
            int window = target.allocateId();
            target.createWindow(window, target.root(), 0, 0, 200, 200);
            target.mapWindow(window);
            target.flush();

            // A mask was asked for and the target selected nothing, so it is not told. The rule above
            // is about the no-mask case only.
            byte[] event = new byte[32];
            event[0] = 33;
            event[1] = 32;
            sender.sendEventWithMask(window, 0x10000, event);
            sender.flush();

            assertNull(readQuiet(target), "an unselected mask is still an unselected mask");
        }
    }

    /** The next event, or null when nothing arrives: a client that is sent nothing stays quiet. */
    private static byte[] readQuiet(RawXClient client) throws IOException {
        client.readTimeoutMillis(500);
        try {
            return client.readEvent();
        } finally {
            client.readTimeoutMillis(5000);
        }
    }

    private void startServer() throws IOException {
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--geometry", "400x300", "--backend", "headless", "--single-window", "--no-unix"}));
        server.start();
        server.startEventLoop();
    }
}
