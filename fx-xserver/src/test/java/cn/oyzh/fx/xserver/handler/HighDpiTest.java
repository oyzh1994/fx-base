package cn.oyzh.fx.xserver.handler;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * What a scaled screen tells the clients that ask about it.
 *
 * <p>{@code --scale 2} makes the screen twice as many pixels across for the same physical size, which
 * is what makes a forwarded application sharp on a Retina panel - but only if the application draws
 * at that resolution, and the server cannot set a client's own toolkit flags. The one thing it *can*
 * do is answer the question a toolkit asks the display: {@code Xft.dpi} in the resource manager, the
 * same property `xrdb -merge` sets. A client that reads it follows by itself.
 */
class HighDpiTest {

    private static final int SCALED_DISPLAY = 77;
    private static final int PLAIN_DISPLAY = 76;

    @Test
    void aScaledScreenPublishesItsResolution() throws IOException {
        XServer server = server(SCALED_DISPLAY, "--scale", "2");
        try (RawXClient client = RawXClient.connect(SCALED_DISPLAY)) {
            assertEquals("Xft.dpi:\t192\n", resourceManager(client),
                    "a toolkit that reads this draws its interface at the resolution the screen has");
        } finally {
            server.stop();
        }
    }

    @Test
    void aScreenAtOneToOnePublishesNothing() throws IOException {
        XServer server = server(PLAIN_DISPLAY, "--scale", "1");
        try (RawXClient client = RawXClient.connect(PLAIN_DISPLAY)) {
            assertNull(resourceManager(client),
                    "a 1x display has nothing to say to a client that would not have assumed it");
        } finally {
            server.stop();
        }
    }

    // ------------------------------------------------------------------

    private static XServer server(int display, String... extra) throws IOException {
        String[] args = new String[8 + extra.length];
        args[0] = "--display";
        args[1] = ":" + display;
        args[2] = "--geometry";
        args[3] = "400x300";
        args[4] = "--single-window";
        args[5] = "--no-unix";
        // Headless, like every other test that starts a server: without it this one took the default
        // backend, which opens a real window - and on a machine with no display the toolkit's startup
        // never returns, so the whole suite hung there (E2). What this test is about is the screen's
        // own size and the property it publishes, and neither needs a window.
        args[6] = "--backend";
        args[7] = "headless";
        System.arraycopy(extra, 0, args, 8, extra.length);
        XServer server = new XServer(ServerOptions.parse(args));
        server.start();
        return server;
    }

    /** The resource manager the root window carries, or null when there is none. */
    private static String resourceManager(RawXClient client) throws IOException {
        int atom = client.internAtom("RESOURCE_MANAGER");
        byte[] reply = client.getProperty(client.root(), atom, 0, false);
        int type = RawXClient.readInt(reply, 8);
        if (type == 0) {
            return null;
        }
        int length = RawXClient.readInt(reply, 16);
        return new String(reply, 32, length, StandardCharsets.ISO_8859_1);
    }
}
