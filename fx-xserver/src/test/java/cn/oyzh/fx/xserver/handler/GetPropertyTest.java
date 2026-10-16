package cn.oyzh.fx.xserver.handler;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * How much of a property a read returns.
 *
 * <p>{@code GetProperty}'s {@code long_length} is a CARD32 counting four byte units, and the value a
 * client sends to mean "all of it" is {@code 0xFFFFFFFF}. Read as a signed int that is {@code -1},
 * which is not "everything" but a negative length - and a negative length reached the array copy as
 * an error the client was told was {@code BadImplementation} instead of being answered. Every client
 * that reads a clipboard property sends exactly that value, which is how it was found.
 */
class GetPropertyTest {

    /** A display number the rest of the suite does not use. */
    private static final int DISPLAY = 86;

    private static XServer server;

    @BeforeAll
    static void startServer() throws IOException {
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--backend", "headless", "--no-unix"}));
        server.start();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void aClientThatAsksForEverythingGetsEverything() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = window(client);
            int property = client.internAtom("XSERVER_TEST_ALL");
            String value = "x".repeat(100);
            client.setStringProperty(window, property, client.internAtom("UTF8_STRING"), value);
            client.flush();

            byte[] reply = client.getProperty(window, property, 0, true, 0, 0xFFFFFFFF);
            assertEquals(0, RawXClient.readInt(reply, 12), "nothing left after it");
            assertEquals(100, RawXClient.readInt(reply, 16),
                    "0xFFFFFFFF means the whole property, not -1 of it");
            assertEquals(value, new String(reply, 32, 100, StandardCharsets.UTF_8));
        }
    }

    @Test
    void aShortReadSaysHowMuchWasLeft() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = window(client);
            int property = client.internAtom("XSERVER_TEST_SHORT");
            client.setStringProperty(window, property, client.internAtom("UTF8_STRING"),
                    "x".repeat(100));
            client.flush();

            // Four units is sixteen bytes, which is where a client that reads in pieces starts.
            byte[] reply = client.getProperty(window, property, 0, false, 0, 4);
            assertEquals(16, RawXClient.readInt(reply, 16));
            assertEquals(84, RawXClient.readInt(reply, 12),
                    "bytesAfter is what tells the client to come back for the rest");
        }
    }

    @Test
    void askingForNothingReturnsNothing() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = window(client);
            int property = client.internAtom("XSERVER_TEST_NONE");
            client.setStringProperty(window, property, client.internAtom("UTF8_STRING"), "text");
            client.flush();

            byte[] reply = client.getProperty(window, property, 0, false, 0, 0);
            assertEquals(0, RawXClient.readInt(reply, 16), "zero units is a question, not a read");
            assertEquals(4, RawXClient.readInt(reply, 12));
        }
    }

    @Test
    void readingWithDeleteTakesThePropertyAway() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = window(client);
            int property = client.internAtom("XSERVER_TEST_DELETE");
            client.setStringProperty(window, property, client.internAtom("UTF8_STRING"), "gone");
            client.flush();

            assertEquals(4, RawXClient.readInt(
                    client.getProperty(window, property, 0, true, 0, 0xFFFFFFFF), 16));
            byte[] again = client.getProperty(window, property, 0, false, 0, 0xFFFFFFFF);
            assertEquals(0, RawXClient.readInt(again, 8),
                    "a property that was read with delete is not there any more");
        }
    }

    private static int window(RawXClient client) throws IOException {
        int window = client.allocateId();
        client.createWindow(window, client.root(), 0, 0, 100, 100);
        return window;
    }
}
