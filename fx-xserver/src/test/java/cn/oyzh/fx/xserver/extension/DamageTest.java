package cn.oyzh.fx.xserver.extension;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DAMAGE: a client hears what changed in a drawable, instead of being asked to draw again.
 *
 * <p>The levels are what these tests pin, because they were measured against a reference server
 * (`tools/DamageProbe`) rather than taken from the specification: {@code RawRectangles} reports every
 * drawing request as it happens, with that request's own area, and the two bounding levels report
 * nothing until a {@code Subtract} asks for it.
 */
class DamageTest {

    /** A display number the rest of the suite does not use. */
    private static final int DISPLAY = 62;

    private static final int QUERY_VERSION = 0;
    private static final int CREATE = 1;
    private static final int DESTROY = 2;
    private static final int SUBTRACT = 3;

    private static XServer server;

    @BeforeAll
    static void startServer() throws IOException {
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--backend", "headless", "--single-window", "--no-unix"}));
        server.start();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    /**
     * {@code RawRectangles}: one notify per request, with the request's own area, and the damage
     * cleared as it is reported - so a second draw reports again rather than reporting a box that
     * grows.
     */
    @Test
    void rawRectanglesReportsEveryChangeAsItHappens() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int[] damage = client.queryExtensionInfo("DAMAGE");
            assertTrue(damage[0] != 0, "the extension has to be advertised");
            int opcode = damage[1];
            int firstEvent = damage[2];
            assertEquals(1, RawXClient.readInt(queryVersion(client, opcode), 8),
                    "the version a client asks about");

            int window = client.allocateId();
            int gc = client.allocateId();
            client.createWindow(window, client.root(), 20, 20, 100, 80, 0x202020);
            client.mapWindow(window);
            client.createGC(gc, client.root(), 0x00FF00);
            client.createDamage(opcode, client.allocateId(), window, 0);
            client.flush();

            client.fillRectangle(window, gc, 10, 10, 40, 30);
            client.flush();
            byte[] event = client.readEvent(firstEvent);
            assertNotNull(event, "a drawing request is reported as it happens");
            assertEquals(0, event[1] & 0xFF, "the level it was created with");
            assertEquals(window, RawXClient.readInt(event, 4), "the drawable it watched");
            assertEquals(10, RawXClient.readShort(event, 16), "the area's x, in the drawable");
            assertEquals(10, RawXClient.readShort(event, 18), "and y");
            assertEquals(40, RawXClient.readShort(event, 20), "and its size");
            assertEquals(30, RawXClient.readShort(event, 22));
            assertEquals(20, RawXClient.readShort(event, 24), "with the drawable's own geometry");
            assertEquals(100, RawXClient.readShort(event, 28));

            client.fillRectangle(window, gc, 60, 40, 20, 20);
            client.flush();
            byte[] second = client.readEvent(firstEvent);
            assertNotNull(second, "the next request reports again, and reports itself");
            assertEquals(60, RawXClient.readShort(second, 16), "the new area, not a growing box");
        }
    }

    /**
     * The bounding levels hold everything back until a {@code Subtract} asks: the change that follows
     * one is reported once, and nothing is reported before it.
     */
    @Test
    void aBoundingLevelWaitsForSubtract() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = client.queryExtensionInfo("DAMAGE")[1];
            int firstEvent = client.queryExtensionInfo("DAMAGE")[2];
            int window = client.allocateId();
            int gc = client.allocateId();
            client.createWindow(window, client.root(), 20, 20, 100, 80, 0x202020);
            client.mapWindow(window);
            client.createGC(gc, client.root(), 0x00FF00);
            int damage = client.allocateId();
            client.createDamage(opcode, damage, window, 2);            // BoundingBox
            client.flush();

            client.fillRectangle(window, gc, 10, 10, 40, 30);
            client.flush();
            client.readTimeoutMillis(400);
            assertNull(client.readEvent(firstEvent),
                    "a bounding level says nothing until a subtract asks");
            client.readTimeoutMillis(5000);

            client.subtractDamage(opcode, damage);
            client.flush();
            client.fillRectangle(window, gc, 60, 40, 20, 20);
            client.flush();
            byte[] event = client.readEvent(firstEvent);
            assertNotNull(event, "the change after a subtract is reported");
            assertEquals(2, event[1] & 0xFF, "at the level it was created with");
            assertEquals(60, RawXClient.readShort(event, 16), "with the area that changed");

            // Nothing more until another subtract: the same rule again.
            client.readTimeoutMillis(400);
            assertNull(client.readEvent(firstEvent), "and then it waits again");
            client.readTimeoutMillis(5000);
        }
    }

    /** A damage object that watches a window stops when the window goes, and destroy frees the id. */
    @Test
    void damageDoesNotOutliveWhatItWatched() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = client.queryExtensionInfo("DAMAGE")[1];
            int window = client.allocateId();
            client.createWindow(window, client.root(), 20, 20, 100, 80, 0x202020);
            client.mapWindow(window);
            int damage = client.allocateId();
            client.createDamage(opcode, damage, window, 0);
            client.destroyWindow(window);
            // The id is free again: a client that creates and destroys these in a loop depends on it.
            client.createDamage(opcode, damage, client.root(), 0);
            client.destroyDamage(opcode, damage);
            client.flush();
            client.queryTree(client.root());           // a round trip: nothing was refused
        }
    }

    private static byte[] queryVersion(RawXClient client, int opcode) throws IOException {
        byte[] request = new byte[12];
        request[0] = (byte) opcode;
        request[1] = (byte) QUERY_VERSION;
        RawXClient.writeShortAt(request, 2, 3);
        RawXClient.writeIntAt(request, 4, 1);
        RawXClient.writeIntAt(request, 8, 1);
        client.sendRequest(request);
        return client.readReply();
    }
}
