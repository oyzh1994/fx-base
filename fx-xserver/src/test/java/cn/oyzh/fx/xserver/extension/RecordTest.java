package cn.oyzh.fx.xserver.extension;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RECORD: a client asking to be told what another client did, which is the extension a replay tool,
 * a test harness and a debugger all stand on (F2).
 *
 * <p>The shapes pinned here are the ones {@code tools/RecordProbe} measured on a reference and are
 * the ones a recorder cannot guess: the version is the *server's* (1.13, whatever was asked for),
 * a context stores the client specs it was given with the future marker as the literal 2, a datum's
 * header is 32 bytes with the data's own length in the length field, a batch holds consecutive client
 * messages while a server message is a datum of its own, and <b>no stream ever ends with
 * EndOfData</b> - the reference sent none for a disable, a free or a second enable.
 */
class RecordTest {

    /** A display number the rest of the suite does not use. */
    private static final int DISPLAY = 89;

    private static final int QUERY_VERSION = 0;
    private static final int CREATE_CONTEXT = 1;
    private static final int REGISTER_CLIENTS = 2;
    private static final int UNREGISTER_CLIENTS = 3;
    private static final int GET_CONTEXT = 4;
    private static final int ENABLE_CONTEXT = 5;
    private static final int DISABLE_CONTEXT = 6;
    private static final int FREE_CONTEXT = 7;

    private static final int CURRENT_CLIENTS = 1;
    private static final int FUTURE_CLIENTS = 2;
    private static final int ALL_CLIENTS = 3;

    private static final int FROM_SERVER = 0;
    private static final int FROM_CLIENT = 1;
    private static final int CLIENT_STARTED = 2;
    private static final int CLIENT_DIED = 3;
    private static final int START_OF_DATA = 4;
    private static final int END_OF_DATA = 5;

    /** Every stamp a recording can ask for, which is what the probe's default stream uses. */
    private static final int ALL_STAMPS = 0x07;

    /** The mask a client's resource ids carry, so a base can be recovered from any of its ids. */
    private static final int RESOURCE_MASK = ClientConnection.RESOURCE_MASK;

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

    /** The version is the server's, and RECORD defines no events - so its event base is zero. */
    @Test
    void theVersionAndRangesAreTheServers() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int[] info = client.queryExtensionInfo("RECORD");
            assertEquals(1, info[0], "the extension is there");
            assertEquals(0, info[2], "no events, so firstEvent is zero, as a reference reports");
            assertTrue(info[3] > 0, "and one error, BadContext, has a base of its own");
            int opcode = info[1];
            assertArrayEquals(new int[]{1, 13}, queryVersion(client, opcode, 1, 13), "asking 1.13");
            assertArrayEquals(new int[]{1, 13}, queryVersion(client, opcode, 1, 0), "asking 1.0");
            assertArrayEquals(new int[]{1, 13}, queryVersion(client, opcode, 2, 0), "asking 2.0");
        }
    }

    /** A context the client did not make, or one that does not exist, is BadContext. */
    @Test
    void anUnknownContextIsRefused() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int[] info = client.queryExtensionInfo("RECORD");
            int opcode = info[1];
            client.sendRequest(contextRequest(opcode, GET_CONTEXT, client.allocateId()));
            IOException refused = assertThrows(IOException.class, client::readReply);
            assertTrue(refused.getMessage().contains("error " + info[3]),
                    "the extension's own error, not a core one: " + refused.getMessage());
        }
    }

    /** A context belongs to the client that made it, and another client is told so. */
    @Test
    void anotherClientsContextIsRefused() throws Exception {
        try (RawXClient owner = RawXClient.connect(DISPLAY);
             RawXClient stranger = RawXClient.connect(DISPLAY)) {
            int[] info = owner.queryExtensionInfo("RECORD");
            int opcode = info[1];
            int context = owner.allocateId();
            owner.sendRequest(createContext(opcode, context, ALL_STAMPS, new int[]{FUTURE_CLIENTS},
                    new byte[][]{everything()}));
            assertEquals(0, describe(owner, opcode, context).enabled,
                    "the owner's context is there, and a new one is disabled");

            stranger.sendRequest(contextRequest(opcode, GET_CONTEXT, context));
            IOException refused = assertThrows(IOException.class, stranger::readReply);
            assertTrue(refused.getMessage().contains("error " + info[3]),
                    "a stranger is refused with BadContext: " + refused.getMessage());

            // And the refusal left the owner's context alone.
            assertEquals(2, describe(owner, opcode, context).specs.get(0).resource,
                    "the future marker is the literal 2, which is what a context stores");
        }
    }

    /**
     * What a context stores is the client's own request echoed back: the specs are resolved when the
     * request arrives, and the ranges are the twenty four byte shapes a client sent.
     */
    @Test
    void aContextEchoesWhatItStores() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int[] info = client.queryExtensionInfo("RECORD");
            int opcode = info[1];
            int context = client.allocateId();
            byte[] range = new byte[24];
            range[0] = 20;                              // core requests 20..25
            range[1] = 25;
            range[16] = 2;                              // delivered events 2..127
            range[17] = 127;
            range[22] = 1;                              // clientStarted
            client.sendRequest(createContext(opcode, context, 0x05, new int[]{FUTURE_CLIENTS},
                    new byte[][]{range}));
            client.sendRequest(contextRequest(opcode, GET_CONTEXT, context));

            byte[] reply = client.readReply();
            assertEquals(0, reply[1] & 0xFF, "a new context is disabled");
            assertEquals(0x05, reply[8] & 0xFF, "with the element header it was made with");
            assertEquals(1, RawXClient.readInt(reply, 12), "one client spec");
            assertEquals(2, RawXClient.readInt(reply, 32), "the future marker, as the literal 2");
            assertEquals(1, RawXClient.readInt(reply, 36), "with one range");
            assertEquals(20, reply[40] & 0xFF, "core requests, first");
            assertEquals(25, reply[41] & 0xFF, "core requests, last");
            assertEquals(2, reply[40 + 16] & 0xFF, "delivered events, first");
            assertEquals(127, reply[40 + 17] & 0xFF, "delivered events, last");
            assertEquals(1, reply[40 + 22] & 0xFF, "and client started");
            assertEquals(0, reply[40 + 23] & 0xFF, "but not client died");
        }
    }

    /** The three selectors resolve to the clients connected now, the future marker, or both. */
    @Test
    void theSelectorsResolveToClientBases() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int[] info = client.queryExtensionInfo("RECORD");
            int base = client.allocateId() & ~RESOURCE_MASK;

            int current = client.allocateId();
            client.sendRequest(createContext(info[1], current, ALL_STAMPS, new int[]{CURRENT_CLIENTS},
                    new byte[][]{everything()}));
            List<Integer> specs = describe(client, info[1], current).specResources();
            assertTrue(specs.contains(base), "the client that is here now is named by its base");
            assertTrue(!specs.contains(FUTURE_CLIENTS), "and only the clients that are here now");

            int all = client.allocateId();
            client.sendRequest(createContext(info[1], all, ALL_STAMPS, new int[]{ALL_CLIENTS},
                    new byte[][]{everything()}));
            specs = describe(client, info[1], all).specResources();
            assertTrue(specs.contains(base), "all clients includes the ones here now");
            assertTrue(specs.contains(FUTURE_CLIENTS), "and the ones that come later");
        }
    }

    /**
     * A recording of another client's whole life: its arrival, its requests, the replies and events
     * they produced, and its departure - with the requests recorded verbatim.
     */
    @Test
    void aRecordingCarriesAClientsLifetime() throws Exception {
        try (RawXClient recorder = RawXClient.connect(DISPLAY)) {
            int[] info = recorder.queryExtensionInfo("RECORD");
            int opcode = info[1];
            int context = recorder.allocateId();
            recorder.sendRequest(createContext(opcode, context, ALL_STAMPS, new int[]{FUTURE_CLIENTS},
                    new byte[][]{everything()}));
            // The enable is the third request on this connection, and its sequence number is what
            // every datum of the stream carries.
            short enableSequence = 3;
            recorder.socketTimeout(2000);
            recorder.sendRequest(contextRequest(opcode, ENABLE_CONTEXT, context));

            Datum start = readDatum(recorder);
            assertEquals(START_OF_DATA, start.category, "a recording opens with an empty start of data");
            assertEquals(0, start.data.length, "with no data at all");
            assertEquals(enableSequence, start.sequence, "and the enabling request's sequence");
            assertEquals(0x07, start.elementHeader, "the context's element header");
            assertEquals(0, start.clientBase, "no client is named by it");

            RawXClient actor = RawXClient.connect(DISPLAY);
            int actorBase = actor.allocateId() & ~RESOURCE_MASK;
            Datum started = readDatum(recorder);
            // Nothing before it: the connection setup reply is the client-started element itself, and
            // a reference does not also record it as a server message (measured).
            assertEquals(CLIENT_STARTED, started.category, "the arrival is a client-started element");
            assertEquals(actorBase, started.clientBase, "named by the client's resource base");
            assertEquals(1, started.data[0] & 0xFF, "carrying the connection setup reply");
            assertEquals(11, RawXClient.readShort(started.data, 2), "of protocol 11.0");
            assertEquals(0, RawXClient.readShort(started.data, 4), "and its minor version");

            // A request the test built itself, so the recording can be compared byte for byte.
            int window = actor.allocateId();
            actor.createWindow(window, actor.root(), 10, 10, 60, 40);
            actor.selectInput(window, 0x20000);         // StructureNotifyMask
            byte[] map = new byte[]{8, 0, 2, 0, 0, 0, 0, 0};
            RawXClient.writeIntAt(map, 4, window);
            actor.sendRequest(map);
            actor.flush();

            boolean sawTheRequest = false;
            boolean sawTheEvent = false;
            // The requests the actor sent arrive as one batch - consecutive client messages share a
            // datum - and the map is the last of them, so the batch ends with the bytes the test
            // built. Then comes the MapNotify the map produced.
            for (int i = 0; i < 6 && !(sawTheRequest && sawTheEvent); i++) {
                Datum datum = readDatum(recorder);
                if (datum.category == FROM_CLIENT) {
                    sawTheRequest |= endsWith(datum.data, map);
                }
                int message = messageStart(datum);
                if (datum.category == FROM_SERVER && (datum.data[message] & 0xFF) == 19) {
                    sawTheEvent = true;                 // MapNotify
                    assertEquals(actorBase, datum.clientBase,
                            "an event caused by a request names the client that asked");
                    assertEquals(actor.sentRequests(), datum.recordedSequence,
                            "and the sequence number of the request that caused it, which is the "
                                    + "map the actor sent last");
                }
            }
            assertTrue(sawTheRequest, "the request is recorded verbatim, header and all");
            assertTrue(sawTheEvent, "and the MapNotify it produced is recorded as a server message");

            int lastSequence = actor.sentRequests();
            actor.close();
            Datum died = readDatum(recorder);
            for (int i = 0; i < 6 && died.category != CLIENT_DIED; i++) {
                // Anything a closing connection still sends comes first; the departure is last.
                died = readDatum(recorder);
            }
            assertEquals(CLIENT_DIED, died.category, "a departure is a client-died element");
            assertEquals(actorBase, died.clientBase, "named by the client's resource base");
            assertEquals(lastSequence, died.recordedSequence,
                    "with the client's last sequence number, which is its last request");
            assertEquals(4, died.data.length, "and that same number as its data");
            assertEquals(lastSequence, RawXClient.readInt(died.data, 0),
                    "four bytes in the recorded client's own order - measured against a range that "
                            + "covered one request of nine, which still carried the ninth");

            recorder.close();
        }
    }

    /**
     * <b>No stream ends with EndOfData.</b> A reference sent none for a disable, a free or a second
     * enable - measured three ways - so what a recorder sees after one is silence.
     */
    @Test
    void noEndOfDataWhenAStreamEnds() throws Exception {
        try (RawXClient recorder = RawXClient.connect(DISPLAY)) {
            int[] info = recorder.queryExtensionInfo("RECORD");
            int opcode = info[1];
            int context = recorder.allocateId();
            recorder.sendRequest(createContext(opcode, context, ALL_STAMPS, new int[]{FUTURE_CLIENTS},
                    new byte[][]{everything()}));
            recorder.sendRequest(contextRequest(opcode, ENABLE_CONTEXT, context));

            Datum start = readDatum(recorder);
            assertEquals(START_OF_DATA, start.category, "the stream opened");

            // A second enable while the context is already enabled does nothing: the reference kept
            // streaming and neither opened it again nor ended it.
            recorder.sendRequest(contextRequest(opcode, ENABLE_CONTEXT, context));
            recorder.sendRequest(contextRequest(opcode, DISABLE_CONTEXT, context));
            recorder.sendRequest(contextRequest(opcode, FREE_CONTEXT, context));

            recorder.socketTimeout(400);
            assertThrows(SocketTimeoutException.class, () -> recorder.readBytes(32),
                    "nothing follows: no EndOfData, and the context is gone");
            recorder.close();
        }
    }

    /** A range that covers nothing records nothing but the start of data. */
    @Test
    void aRangeDecidesWhatIsRecorded() throws Exception {
        try (RawXClient recorder = RawXClient.connect(DISPLAY)) {
            int[] info = recorder.queryExtensionInfo("RECORD");
            int opcode = info[1];
            int context = recorder.allocateId();
            recorder.sendRequest(createContext(opcode, context, ALL_STAMPS, new int[]{FUTURE_CLIENTS},
                    new byte[][]{new byte[24]}));
            recorder.sendRequest(contextRequest(opcode, ENABLE_CONTEXT, context));
            assertEquals(START_OF_DATA, readDatum(recorder).category, "the stream opened");

            try (RawXClient nobody = RawXClient.connect(DISPLAY)) {
                int window = nobody.allocateId();
                nobody.createWindow(window, nobody.root(), 0, 0, 40, 40);
                nobody.mapWindow(window);
                nobody.flush();
                // The client arrived, asked and left, and none of it is inside the range.
                recorder.socketTimeout(500);
                assertThrows(SocketTimeoutException.class, () -> recorder.readBytes(32),
                        "a range of zeros records nothing");
            }
            recorder.close();
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /** One recorded element as it arrived: the 32 byte header and the data behind it. */
    private record Datum(int category, int sequence, int length, int elementHeader,
                         int clientSwapped, int clientBase, int serverTime, int recordedSequence,
                         byte[] data) {
    }

    /** Whether one byte image ends with another, which is how a batch's last request is found. */
    private static boolean endsWith(byte[] data, byte[] tail) {
        if (data.length < tail.length) {
            return false;
        }
        return java.util.Arrays.equals(java.util.Arrays.copyOfRange(data,
                data.length - tail.length, data.length), tail);
    }

    /**
     * Where the message inside a datum begins, which is not its first byte.
     *
     * <p>The element header's stamps come in front of the element itself, and a server message's
     * datum carries four bytes of the server's time when {@code FromServerTime} is set - which is what
     * the probe measured on a reference, and what this test reads past: a datum's first byte is a
     * stamp, not the event's type.
     */
    private static int messageStart(Datum datum) {
        return (datum.elementHeader & 0x01) == 0 ? 0 : 4;
    }

    private static Datum readDatum(RawXClient client) throws IOException {
        byte[] header = client.readBytes(32);
        if ((header[0] & 0xFF) != 1) {
            throw new IOException("a recording's datum is a reply, and this was type "
                    + (header[0] & 0xFF));
        }
        int category = header[1] & 0xFF;
        if (category == END_OF_DATA) {
            throw new IOException("an EndOfData datum: a reference sends none");
        }
        int length = RawXClient.readInt(header, 4);
        byte[] data = length == 0 ? new byte[0] : client.readBytes(length * 4);
        return new Datum(category, RawXClient.readShort(header, 2), length, header[8] & 0xFF,
                header[9] & 0xFF, RawXClient.readInt(header, 12), RawXClient.readInt(header, 16),
                RawXClient.readInt(header, 20), data);
    }

    private static int[] queryVersion(RawXClient client, int opcode, int major, int minor)
            throws IOException {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = (byte) QUERY_VERSION;
        RawXClient.writeShortAt(request, 2, 2);
        RawXClient.writeShortAt(request, 4, major);
        RawXClient.writeShortAt(request, 6, minor);
        client.sendRequest(request);
        byte[] reply = client.readReply();
        return new int[]{RawXClient.readShort(reply, 8), RawXClient.readShort(reply, 10)};
    }

    /** A context read back, decoded into the pieces the tests assert on. */
    private record Context(int enabled, int elementHeader, List<Spec> specs) {

        List<Integer> specResources() {
            List<Integer> resources = new ArrayList<>();
            for (Spec spec : specs) {
                resources.add(spec.resource);
            }
            return resources;
        }
    }

    private record Spec(int resource, int ranges) {
    }

    private static Context describe(RawXClient client, int opcode, int context) throws IOException {
        client.sendRequest(contextRequest(opcode, GET_CONTEXT, context));
        byte[] reply = client.readReply();
        int clients = RawXClient.readInt(reply, 12);
        List<Spec> specs = new ArrayList<>();
        for (int i = 0, at = 32; i < clients; i++) {
            int ranges = RawXClient.readInt(reply, at + 4);
            specs.add(new Spec(RawXClient.readInt(reply, at), ranges));
            at += 8 + ranges * 24;
        }
        return new Context(reply[1] & 0xFF, reply[8] & 0xFF, specs);
    }

    private static byte[] contextRequest(int opcode, int minor, int context) {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = (byte) minor;
        RawXClient.writeShortAt(request, 2, 2);
        RawXClient.writeIntAt(request, 4, context);
        return request;
    }

    /**
     * A {@code CreateContext} over the given specs, which all share the one range - the shape the
     * protocol has: the specs come first on the wire, the ranges follow, and every spec holds the
     * whole list (measured against Xvfb - see {@code RecordExtension.readSpecs}).
     */
    private static byte[] createContext(int opcode, int context, int elementHeader, int[] clients,
                                        byte[][] ranges) {
        byte[] request = new byte[(5 + clients.length + ranges.length * 6) * 4];
        request[0] = (byte) opcode;
        request[1] = (byte) CREATE_CONTEXT;
        RawXClient.writeShortAt(request, 2, request.length / 4);
        RawXClient.writeIntAt(request, 4, context);
        request[8] = (byte) elementHeader;
        RawXClient.writeIntAt(request, 12, clients.length);
        RawXClient.writeIntAt(request, 16, ranges.length);
        int at = 20;
        for (int client : clients) {
            RawXClient.writeIntAt(request, at, client);
            at += 4;
        }
        for (byte[] range : ranges) {
            System.arraycopy(range, 0, request, at, 24);
            at += 24;
        }
        return request;
    }

    /** Everything a range can ask for: what the probe registers on both servers. */
    private static byte[] everything() {
        byte[] range = new byte[24];
        range[0] = 0;
        range[1] = 127;                                 // core requests 0..127
        range[2] = 0;
        range[3] = 127;                                 // core replies 0..127
        range[4] = (byte) 128;                          // extension requests, majors 128..255
        range[5] = (byte) 255;
        RawXClient.writeShortAt(range, 6, 0);           // and their minors, 0..65535
        RawXClient.writeShortAt(range, 8, 65535);
        range[10] = (byte) 128;                         // extension replies, the same rectangle
        range[11] = (byte) 255;
        RawXClient.writeShortAt(range, 12, 0);
        RawXClient.writeShortAt(range, 14, 65535);
        range[16] = 2;
        range[17] = 127;                                // delivered events 2..127
        range[18] = 2;
        range[19] = 6;                                  // device events 2..6
        range[20] = 0;
        range[21] = (byte) 255;                         // errors 0..255
        range[22] = 1;                                  // client started
        range[23] = 1;                                  // client died
        return range;
    }

    /** A test that pins a difference rather than a shape says so where it is. */
    @Test
    void registeringAndUnregisteringClientsIsAccepted() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int[] info = client.queryExtensionInfo("RECORD");
            int opcode = info[1];
            int context = client.allocateId();
            client.sendRequest(createContext(opcode, context, ALL_STAMPS, new int[]{FUTURE_CLIENTS},
                    new byte[][]{everything()}));

            byte[] register = createContext(opcode, context, ALL_STAMPS, new int[]{CURRENT_CLIENTS},
                    new byte[][]{everything()});
            register[1] = (byte) REGISTER_CLIENTS;
            client.sendRequest(register);
            Context after = describe(client, opcode, context);
            assertTrue(after.specs.size() >= 1, "registering a client is accepted and stored");

            byte[] unregister = new byte[(2 + 1 + 1) * 4];
            unregister[0] = (byte) opcode;
            unregister[1] = (byte) UNREGISTER_CLIENTS;
            RawXClient.writeShortAt(unregister, 2, unregister.length / 4);
            RawXClient.writeIntAt(unregister, 4, context);
            RawXClient.writeIntAt(unregister, 8, 1);    // one spec: the current clients
            RawXClient.writeIntAt(unregister, 12, CURRENT_CLIENTS);
            client.sendRequest(unregister);
            Context afterRemoval = describe(client, opcode, context);
            assertNotEquals(after.specs.size(), afterRemoval.specs.size(),
                    "and unregistering removes them again");
        }
    }
}
