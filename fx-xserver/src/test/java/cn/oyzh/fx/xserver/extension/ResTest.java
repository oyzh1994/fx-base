package cn.oyzh.fx.xserver.extension;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.client.ProtocolException;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * X-Resource: what a client is holding, which is the question E4's lazy allocation left with no way
 * to answer.
 *
 * <p>The shapes here are the ones {@code tools/ResProbe} measured on a reference, and two of them
 * are not guessable: {@code QueryVersion} answers **the server's** 1.2 to every client, and
 * {@code QueryClients} lists the **server's own resources as a client with base zero**, before the
 * connected ones. The rest are pinned because a count that is right by accident is not pinned at
 * all: the type atoms are resolved to their names, the pixmap byte count is checked against the
 * store's own size, and an id no client owns is checked to be refused rather than answered zero.
 */
class ResTest {

    /** A display number the rest of the suite does not use. */
    private static final int DISPLAY = 88;

    private static final int QUERY_VERSION = 0;
    private static final int QUERY_CLIENTS = 1;
    private static final int QUERY_CLIENT_RESOURCES = 2;
    private static final int QUERY_CLIENT_PIXMAP_BYTES = 3;
    private static final int QUERY_CLIENT_IDS = 4;
    private static final int QUERY_RESOURCE_BYTES = 5;

    /** The mask a client's resource ids carry, as the setup reply reports it. */
    private static final int RESOURCE_MASK = ClientConnection.RESOURCE_MASK;

    /** An id outside every client's range, which is what makes a request name no client at all. */
    private static final int NOBODYS_ID = 0xDEADBEEF;

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

    /** The version is the server's, and it is 1.2 - so the 1.2 requests below are available. */
    @Test
    void theVersionIsTheServers() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = opcode(client);
            assertArrayEquals(new int[]{1, 2}, queryVersion(client, opcode, 1, 2), "a 1.2 client");
            assertArrayEquals(new int[]{1, 2}, queryVersion(client, opcode, 1, 0), "a 1.0 client");
            assertArrayEquals(new int[]{1, 2}, queryVersion(client, opcode, 2, 0), "and a 2.0 client");
        }
    }

    /**
     * The client list: the server's own resources first, with resource base zero, then every
     * connected client with the range its ids come from.
     */
    @Test
    void clientsAreListedWithTheServersOwnFirst() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = opcode(client);
            List<int[]> clients = queryClients(client, opcode);
            assertTrue(clients.size() >= 2, "the server's own resources are a client too");
            assertEquals(0, clients.get(0)[0], "the server's own resource base is zero");
            assertEquals(RESOURCE_MASK, clients.get(0)[1], "with the same mask every client gets");

            int own = client.allocateId() & ~RESOURCE_MASK;
            assertTrue(clients.stream().anyMatch(c -> c[0] == own),
                    "and the connection asking is in the list, at 0x" + Integer.toHexString(own));
        }
    }

    /** The counts are by resource type, and the types are named by atoms a client can resolve. */
    @Test
    void resourcesAreCountedByType() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = opcode(client);
            int window = client.allocateId();
            client.createWindow(window, client.root(), 10, 10, 40, 30);
            int pixmap = client.allocateId();
            client.createPixmap(pixmap, client.root(), 16, 16, 24);
            int gc = client.allocateId();
            client.createGC(gc, client.root(), 0xFFFFFF);
            int font = client.allocateId();
            client.openFont(font, "fixed");
            int cursor = client.allocateId();
            client.createGlyphCursor(cursor, font, font, 'x', 'x', 0, 0xFFFFFF);
            client.flush();

            List<Object[]> types = queryClientResources(client, opcode, window);
            assertEquals(5, types.size(), "one row per type this client holds");
            for (Object[] type : types) {
                int count = (int) type[1];
                assertEquals(1, count, "one of each: " + type[0]);
            }
            List<String> names = new ArrayList<>();
            for (Object[] type : types) {
                names.add((String) type[0]);
            }
            assertTrue(names.contains("WINDOW"), "a window is a WINDOW: " + names);
            assertTrue(names.contains("PIXMAP"), "a pixmap is a PIXMAP: " + names);
            assertTrue(names.contains("GC"), "and a graphics context is a GC: " + names);
            assertTrue(names.contains("FONT"), "a font is a FONT: " + names);
            assertTrue(names.contains("CURSOR"), "a cursor is a CURSOR: " + names);
        }
    }

    /** The bytes a client's pixmaps hold, which is the store's own size and nothing else. */
    @Test
    void pixmapBytesAreTheStoresSize() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = opcode(client);
            int window = client.allocateId();
            client.createWindow(window, client.root(), 10, 10, 40, 30);
            client.flush();
            assertEquals(0, pixmapBytes(client, opcode, window)[0],
                    "a window is not a pixmap, and a reference answers zero here too");

            int pixmap = client.allocateId();
            client.createPixmap(pixmap, client.root(), 16, 16, 24);
            client.flush();
            assertEquals(16 * 16 * 4, pixmapBytes(client, opcode, window)[0],
                    "16 by 16 at four bytes a pixel, which is what the store is");
        }
    }

    /**
     * An id no client owns is refused with {@code BadValue} carrying it - by every request that
     * names a client. Answering an empty list instead would be a silent lie about a typo.
     */
    @Test
    void anIdNobodyOwnsIsRefused() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = opcode(client);
            byte[] request = new byte[8];
            request[0] = (byte) opcode;
            request[1] = (byte) QUERY_CLIENT_RESOURCES;
            RawXClient.writeShortAt(request, 2, 2);
            RawXClient.writeIntAt(request, 4, NOBODYS_ID);
            client.sendRequest(request);
            client.flush();
            byte[] refusal = client.readEvent();
            assertEquals(0, refusal[0] & 0x7F, "an error, not a reply");
            assertEquals(ProtocolException.VALUE, refusal[1], "BadValue");
            assertEquals(NOBODYS_ID, RawXClient.readInt(refusal, 4), "naming the id");
        }
    }

    /**
     * The id query answers one row per client and per bit asked, and the row names the client and
     * the **bit** - a request for both bits produces two rows, not one row with both.
     */
    @Test
    void clientIdsAnswerOneRowPerClientAndBit() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = opcode(client);
            List<Object[]> rows = queryClientIds(client, opcode, 0, 0b1);
            assertTrue(rows.size() >= 2, "the server's own resources and this connection at least");
            for (Object[] row : rows) {
                assertEquals(0b1, row[1], "the row names the bit it answers");
                assertEquals(0, ((int[]) row[2]).length,
                        "and a reference answers the XID bit with no value at all");
            }
        }
    }

    /** A named resource's size: the store it holds, with the type it was reported under. */
    @Test
    void aNamedResourcesSizeIsItsStore() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = opcode(client);
            int window = client.allocateId();
            client.createWindow(window, client.root(), 10, 10, 40, 30);
            int pixmap = client.allocateId();
            client.createPixmap(pixmap, client.root(), 16, 16, 24);
            client.flush();

            Object[] windowSize = queryResourceBytes(client, opcode, 0, window, 0)[0];
            assertEquals(40 * 30 * 4, windowSize[2],
                    "a window keeps its pixels here, so it holds bytes where a reference holds none");
            assertEquals("WINDOW", windowSize[3], "and its type is the atom a count would use");
            assertEquals(1, windowSize[4], "refCount");

            // The type is matched against the resource's own atom, which is the specification's
            // rule and a deliberate difference from a reference - it answers nothing for a named
            // type, even the resource's own.
            assertEquals(1, queryResourceBytes(client, opcode, 0, window,
                    atomOf(client, "WINDOW")).length, "asked for by its own type");
            assertEquals(0, queryResourceBytes(client, opcode, 0, window,
                    atomOf(client, "PIXMAP")).length, "and a type it is not is no match");
        }
    }

    // ------------------------------------------------------------------

    private static int[] queryVersion(RawXClient client, int opcode, int major, int minor)
            throws IOException {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = (byte) QUERY_VERSION;
        RawXClient.writeShortAt(request, 2, 2);
        request[4] = (byte) major;
        request[5] = (byte) minor;
        client.sendRequest(request);
        byte[] reply = client.readReply();
        return new int[]{RawXClient.readShort(reply, 8), RawXClient.readShort(reply, 10)};
    }

    private static List<int[]> queryClients(RawXClient client, int opcode) throws IOException {
        byte[] request = new byte[4];
        request[0] = (byte) opcode;
        request[1] = (byte) QUERY_CLIENTS;
        RawXClient.writeShortAt(request, 2, 1);
        client.sendRequest(request);
        byte[] reply = client.readReply();
        int count = RawXClient.readInt(reply, 8);
        List<int[]> clients = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int at = 32 + i * 8;
            clients.add(new int[]{RawXClient.readInt(reply, at), RawXClient.readInt(reply, at + 4)});
        }
        return clients;
    }

    /** The resource counts, with each type's atom resolved to its name for the assertion. */
    private static List<Object[]> queryClientResources(RawXClient client, int opcode, int xid)
            throws IOException {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = (byte) QUERY_CLIENT_RESOURCES;
        RawXClient.writeShortAt(request, 2, 2);
        RawXClient.writeIntAt(request, 4, xid);
        client.sendRequest(request);
        byte[] reply = client.readReply();
        int count = RawXClient.readInt(reply, 8);
        List<Object[]> types = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int at = 32 + i * 8;
            types.add(new Object[]{client.atomName(RawXClient.readInt(reply, at)),
                    RawXClient.readInt(reply, at + 4)});
        }
        return types;
    }

    private static int[] pixmapBytes(RawXClient client, int opcode, int xid) throws IOException {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = (byte) QUERY_CLIENT_PIXMAP_BYTES;
        RawXClient.writeShortAt(request, 2, 2);
        RawXClient.writeIntAt(request, 4, xid);
        client.sendRequest(request);
        byte[] reply = client.readReply();
        return new int[]{RawXClient.readInt(reply, 8), RawXClient.readInt(reply, 12)};
    }

    /** Each row as {client, mask, values}. */
    private static List<Object[]> queryClientIds(RawXClient client, int opcode, int named, int mask)
            throws IOException {
        byte[] request = new byte[16];
        request[0] = (byte) opcode;
        request[1] = (byte) QUERY_CLIENT_IDS;
        RawXClient.writeShortAt(request, 2, 4);
        RawXClient.writeIntAt(request, 4, 1);                   // one spec
        RawXClient.writeIntAt(request, 8, named);               // the client it asks about
        RawXClient.writeIntAt(request, 12, mask);
        client.sendRequest(request);
        byte[] reply = client.readReply();
        int count = RawXClient.readInt(reply, 8);
        List<Object[]> rows = new ArrayList<>(count);
        int at = 32;
        for (int i = 0; i < count; i++) {
            int specClient = RawXClient.readInt(reply, at);
            int specMask = RawXClient.readInt(reply, at + 4);
            int valueBytes = RawXClient.readInt(reply, at + 8);
            int[] values = new int[valueBytes / 4];
            for (int v = 0; v < values.length; v++) {
                values[v] = RawXClient.readInt(reply, at + 12 + v * 4);
            }
            rows.add(new Object[]{specClient, specMask, values});
            at += 12 + valueBytes;
        }
        return rows;
    }

    /** Each row as {resource, type, bytes, typeName, refCount, useCount}. */
    private static Object[][] queryResourceBytes(RawXClient client, int opcode, int named,
                                                 int resource, int type) throws IOException {
        byte[] request = new byte[20];
        request[0] = (byte) opcode;
        request[1] = (byte) QUERY_RESOURCE_BYTES;
        RawXClient.writeShortAt(request, 2, 5);
        RawXClient.writeIntAt(request, 4, named);
        RawXClient.writeIntAt(request, 8, 1);                   // one spec
        RawXClient.writeIntAt(request, 12, resource);
        RawXClient.writeIntAt(request, 16, type);
        client.sendRequest(request);
        byte[] reply = client.readReply();
        int count = RawXClient.readInt(reply, 8);
        Object[][] sizes = new Object[count][];
        int at = 32;
        for (int i = 0; i < count; i++) {
            sizes[i] = new Object[]{RawXClient.readInt(reply, at), RawXClient.readInt(reply, at + 4),
                    RawXClient.readInt(reply, at + 8),
                    client.atomName(RawXClient.readInt(reply, at + 4)),
                    RawXClient.readInt(reply, at + 12), RawXClient.readInt(reply, at + 16)};
            at += 24 + RawXClient.readInt(reply, at + 20) * 20;
        }
        return sizes;
    }

    /** The atom a type name is interned under - the same atom the count reply reported. */
    private static int atomOf(RawXClient client, String name) throws IOException {
        return client.internAtom(name);
    }

    private static int opcode(RawXClient client) throws IOException {
        int[] info = client.queryExtensionInfo("X-Resource");
        assertTrue(info[0] != 0, "the extension has to be advertised");
        return info[1];
    }
}
