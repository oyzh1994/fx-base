package cn.oyzh.fx.xserver.extension;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Xinerama, which answers "no, I am one screen" - and has to answer.
 *
 * <p>It is not here because a client uses it: this server has one screen and always will. It is here
 * because a client *asks*, and Xlib's own {@code XineramaIsActive} does not survive being told the
 * extension is absent - the question is what a toolkit asks at startup, so the answer has to exist.
 */
class XineramaTest {

    /** A display number the rest of the suite does not use. */
    private static final int DISPLAY = 66;

    /** The extension's requests, in the order the specification numbers them. */
    private static final int QUERY_VERSION = 0;
    private static final int IS_ACTIVE = 4;
    private static final int QUERY_SCREENS = 5;

    /** The three Xinerama 1.1 requests this server deliberately leaves to the dispatcher. */
    private static final int GET_STATE = 1;
    private static final int GET_SCREEN_COUNT = 2;
    private static final int GET_SCREEN_SIZE = 3;

    private static XServer server;

    @BeforeAll
    static void startServer() throws IOException {
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--geometry", "600x400", "--backend", "headless", "--no-unix"}));
        server.start();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void itIsOfferedAndReportshisVersion() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = client.queryExtension("XINERAMA");
            assertNotEquals(0, opcode, "a client that asks has to be told the extension is there");

            byte[] reply = client.extensionRequest(opcode, QUERY_VERSION, 2);
            assertEquals(1, RawXClient.readShort(reply, 8), "the major version the client asked about");
        }
    }

    @Test
    void itSaysItIsNotSeveralScreens() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = client.queryExtension("XINERAMA");
            byte[] reply = client.extensionRequest(opcode, IS_ACTIVE);
            assertEquals(0, RawXClient.readInt(reply, 8),
                    "one screen is not a Xinerama display, and saying so is the answer that matters");
        }
    }

    @Test
    void andItCanSayWhichScreenThatIs() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = client.queryExtension("XINERAMA");
            byte[] reply = client.extensionRequest(opcode, QUERY_SCREENS);
            assertEquals(1, RawXClient.readInt(reply, 8), "the list of screens has one screen in it");
            // ScreenInfo: x, y, width, height, each sixteen bit, from offset 32.
            assertEquals(0, RawXClient.readShort(reply, 32));
            assertEquals(0, RawXClient.readShort(reply, 34));
            assertEquals(600, RawXClient.readShort(reply, 36), "the screen the server has");
            assertEquals(400, RawXClient.readShort(reply, 38));
        }
    }

    /**
     * The three Xinerama 1.1 requests are deliberately not answered, and this is the test that says so
     * out loud.
     *
     * <p>They are {@code GetState}, {@code GetScreenCount} and {@code GetScreenSize} - for a client
     * that already knows it is on a Xinerama display, which none of this server's clients are (a
     * client has to ask {@code IsActive} first, and this one says no). A refusal is a decision, and a
     * decision that nothing records is one that gets re-litigated; so it is recorded here, and the
     * measurement behind it is in {@code docs/backlog.md} B3: a reference server's own answers are
     * **255, 255 and {@code BadLength}** - the first two are not a state or a count that means
     * anything, and the third refuses the request this probe builds even with the window argument the
     * other two take. Implementing them from that would be guessing, for a client that does not exist.
     */
    @Test
    void theOneOneRequestsAreDeliberatelyLeftUnanswered() throws IOException {
        int[] extras = {GET_STATE, GET_SCREEN_COUNT, GET_SCREEN_SIZE};
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = client.queryExtension("XINERAMA");
            for (int minor : extras) {
                // The three take a window (the screen they are asking about), so a refusal from the
                // dispatcher is what comes back - and RawXClient reports an error as an exception.
                byte[] request = new byte[8];
                request[0] = (byte) opcode;
                request[1] = (byte) minor;
                RawXClient.writeShortAt(request, 2, 2);
                RawXClient.writeIntAt(request, 4, client.root());
                client.sendRequest(request);
                client.flush();
                assertThrows(IOException.class, client::readReply,
                        "minor " + minor + " is answered now: if that is deliberate, the decision in "
                                + "the backlog item and in this test's javadoc goes with it");
            }
        }
    }
}
