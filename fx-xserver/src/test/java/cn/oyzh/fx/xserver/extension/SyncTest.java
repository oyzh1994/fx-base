package cn.oyzh.fx.xserver.extension;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ProtocolException;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static cn.oyzh.fx.xserver.extension.SyncExtension.ALARM_ACTIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SYNC: counters a client sets and reads back, and fences it triggers and waits on.
 *
 * <p>Two things here were measured against a reference rather than read out of the specification
 * ({@code tools/SyncProbe}): {@code Initialize} answers **the server's** version - 3.1 to a client
 * asking for 3.1 and to one asking for 3.0, where echoing the client's own version is the obvious
 * implementation and tells a 3.0 client that a 3.1 request is available to it - and a fence's
 * untriggered-to-triggered-to-reset walk behaves as the client sees it, which is the whole of what a
 * fence is for.
 */
class SyncTest {

    /** A display number the rest of the suite does not use. */
    private static final int DISPLAY = 85;

    private static final int INITIALIZE = 0;
    private static final int CREATE_COUNTER = 2;
    private static final int SET_COUNTER = 3;
    private static final int CHANGE_COUNTER = 4;
    private static final int QUERY_COUNTER = 5;
    private static final int CREATE_FENCE = 14;
    private static final int TRIGGER_FENCE = 15;
    private static final int RESET_FENCE = 16;
    private static final int QUERY_FENCE = 18;
    private static final int CREATE_ALARM = 8;
    private static final int QUERY_ALARM = 10;
    private static final int DESTROY_ALARM = 11;

    /** Every {@code XSyncCA*} bit: a create that carries all of an alarm's fields. */
    private static final int CA_ALL = 0x3F;
    private static final int ALARM_NOTIFY = 1;

    private static final int POSITIVE_TRANSITION = 0;
    private static final int POSITIVE_COMPARISON = 2;
    private static final int NEGATIVE_TRANSITION = 1;
    private static final int ABSOLUTE = 0;

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
     * The version is the server's, not the client's. A client that asks for 3.0 is told 3.1, and a
     * client that asks for 3.1 is told 3.1 - measured on a reference, and the reply is what tells a
     * client which requests it may use.
     */
    @Test
    void initializeAnswersTheServersVersion() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            assertEquals(3, initialize(client, 3, 0)[0], "major");
            assertEquals(1, initialize(client, 3, 0)[1], "a 3.0 client is told 3.1");
            assertEquals(1, initialize(client, 3, 1)[1], "and so is a 3.1 client");
        }
    }

    /**
     * A counter holds what it was set to, and {@code ChangeCounter} adds a delta to it - including
     * above 32 bits, which is the part the wire's two-word {@code INT64} gets wrong if the words are
     * taken in the other order (a value of 2^32 + 7 reads back as 7).
     */
    @Test
    void countersHoldTheirValueAcrossTheFullSixtyFourBits() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = opcode(client);
            int counter = client.allocateId();
            int64Request(client, opcode, CREATE_COUNTER, counter, 5L);
            assertEquals(5L, queryCounter(client, opcode, counter), "the initial value");

            int64Request(client, opcode, SET_COUNTER, counter, 42L);
            assertEquals(42L, queryCounter(client, opcode, counter), "and what it is set to");

            int64Request(client, opcode, CHANGE_COUNTER, counter, 8L);
            assertEquals(50L, queryCounter(client, opcode, counter), "and the delta added to it");

            long big = (1L << 32) + 7;
            int64Request(client, opcode, SET_COUNTER, counter, big);
            assertEquals(big, queryCounter(client, opcode, counter),
                    "a value above 32 bits, which the two words of an INT64 have to carry in order");
        }
    }

    /**
     * A fence's four states, as a client sees them. It is created untriggered, triggered, reset, and
     * read back after each - and the reads are the test, because a fence is exactly the bit a client
     * cannot observe any other way.
     */
    @Test
    void aFenceTriggersAndResets() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = opcode(client);
            int fence = client.allocateId();

            byte[] create = new byte[16];
            create[0] = (byte) opcode;
            create[1] = (byte) CREATE_FENCE;
            RawXClient.writeShortAt(create, 2, 4);
            RawXClient.writeIntAt(create, 4, client.root());    // drawable first, fence second
            RawXClient.writeIntAt(create, 8, fence);
            create[12] = 0;
            client.sendRequest(create);
            assertTrue(!queryFence(client, opcode, fence), "created untriggered");

            trigger(client, opcode, TRIGGER_FENCE, fence);
            assertTrue(queryFence(client, opcode, fence), "triggered");

            trigger(client, opcode, RESET_FENCE, fence);
            assertTrue(!queryFence(client, opcode, fence), "and reset again");
        }
    }

    /**
     * An alarm is a trigger the server watches for the client: the counter crosses the threshold and
     * the notification arrives, carrying the value that crossed it and the threshold it crossed.
     *
     * <p>The order is the measurement, as it is everywhere a notification is involved: the
     * {@code AlarmNotify} is read first, and the state afterwards, because a server that fires and a
     * server that only accepts the requests look identical until the counter moves.
     */
    @Test
    void anAlarmFiresWhenItsCounterCrossesTheThreshold() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = opcode(client);
            int firstEvent = eventBase(client);
            int counter = client.allocateId();
            int64Request(client, opcode, CREATE_COUNTER, counter, 0L);

            int alarm = client.allocateId();
            createAlarm(client, opcode, alarm, counter, 10L, POSITIVE_TRANSITION, 0L);
            assertEquals(ALARM_ACTIVE, alarmState(client, opcode, alarm, counter), "armed");

            int64Request(client, opcode, CHANGE_COUNTER, counter, 20L);
            byte[] event = client.readEvent();
            assertEquals(firstEvent + 1, event[0] & 0x7F,
                    "the notification is a real extension event at firstEvent + 1, not a generic one");
            assertEquals(alarm, RawXClient.readInt(event, 4), "for the alarm that fired");
            assertEquals(20L, int64(event, 8), "carrying the value that crossed the threshold");
            assertEquals(10L, int64(event, 16), "and the threshold it crossed");
            assertEquals(ALARM_ACTIVE, event[28] & 0xFF, "a transition stays armed after firing");

            // 20 is already above the threshold, so moving further up is not a crossing.
            int64Request(client, opcode, CHANGE_COUNTER, counter, 5L);
            client.readTimeoutMillis(400);
            assertNull(client.readEvent(), "a move on the same side of the threshold is not a cross");
        }
    }

    /**
     * A comparison is the other kind of test, and the difference is only visible after it has fired
     * once: the alarm goes **inactive**, and moving the counter further the same way - or back across
     * the threshold - produces nothing more.
     */
    @Test
    void aComparisonFiresOnceAndGoesInactive() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = opcode(client);
            int counter = client.allocateId();
            int64Request(client, opcode, CREATE_COUNTER, counter, 0L);
            int alarm = client.allocateId();
            createAlarm(client, opcode, alarm, counter, 10L, POSITIVE_COMPARISON, 0L);

            int64Request(client, opcode, CHANGE_COUNTER, counter, 20L);
            byte[] event = client.readEvent();
            assertEquals(alarm, RawXClient.readInt(event, 4), "the comparison fires");
            assertEquals(1, event[28] & 0xFF, "and the notification says it is inactive now");

            int64Request(client, opcode, CHANGE_COUNTER, counter, 5L);
            client.readTimeoutMillis(400);
            assertNull(client.readEvent(), "once inactive it does not fire again");
            int64Request(client, opcode, CHANGE_COUNTER, counter, -30L);
            client.readTimeoutMillis(400);
            assertNull(client.readEvent(), "not even going back across the threshold");

            assertEquals(1, alarmState(client, opcode, alarm, counter), "and the alarm says so too");
        }
    }

    /**
     * The combination a reference refuses, and the reason it can: a transition moves its own
     * threshold by its delta, so a negative transition with a positive delta would move the
     * threshold up - where the counter has just come down from - and could never fire again.
     */
    @Test
    void aTransitionWhoseDeltaMovesTheWrongWayIsRefused() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = opcode(client);
            int counter = client.allocateId();
            int64Request(client, opcode, CREATE_COUNTER, counter, 0L);

            int wrongWay = client.allocateId();
            sendAlarm(client, opcode, wrongWay, counter, 10L, NEGATIVE_TRANSITION, 100L);
            client.flush();
            // A one-way request reports nothing, so the refusal is read off the stream directly.
            byte[] refusal = client.readEvent();
            assertEquals(0, refusal[0] & 0x7F, "an error, not a reply");
            assertEquals(ProtocolException.MATCH, refusal[1],
                    "BadMatch, which is what a reference answers");
            assertEquals(wrongWay, RawXClient.readInt(refusal, 4), "on the alarm's own id");
        }
    }

    /** An alarm is a resource: destroying it makes it unknown, with SYNC's own BadAlarm. */
    @Test
    void aDestroyedAlarmIsUnknown() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = opcode(client);
            int firstError = errorBase(client);
            int counter = client.allocateId();
            int64Request(client, opcode, CREATE_COUNTER, counter, 0L);
            int alarm = client.allocateId();
            createAlarm(client, opcode, alarm, counter, 10L, POSITIVE_TRANSITION, 0L);

            byte[] destroy = new byte[8];
            destroy[0] = (byte) opcode;
            destroy[1] = (byte) DESTROY_ALARM;
            RawXClient.writeShortAt(destroy, 2, 2);
            RawXClient.writeIntAt(destroy, 4, alarm);
            client.sendRequest(destroy);
            client.flush();

            byte[] request = new byte[8];
            request[0] = (byte) opcode;
            request[1] = (byte) QUERY_ALARM;
            RawXClient.writeShortAt(request, 2, 2);
            RawXClient.writeIntAt(request, 4, alarm);
            client.sendRequest(request);
            client.flush();
            byte[] refusal = client.readEvent();
            assertEquals(0, refusal[0] & 0x7F, "an error, not a reply");
            assertEquals(firstError + 1, refusal[1],
                    "BadAlarm, which is the extension's error base plus one");
            assertEquals(alarm, RawXClient.readInt(refusal, 4), "naming the alarm");
        }
    }

    // ------------------------------------------------------------------

    private static void createAlarm(RawXClient client, int opcode, int alarm, int counter,
                                    long value, int testType, long delta) throws IOException {
        sendAlarm(client, opcode, alarm, counter, value, testType, delta);
        client.flush();
    }

    /** CreateAlarm with every field present, as {@code tools/SyncProbe} writes it. */
    private static void sendAlarm(RawXClient client, int opcode, int alarm, int counter,
                                  long value, int testType, long delta) throws IOException {
        byte[] request = new byte[44];
        request[0] = (byte) opcode;
        request[1] = (byte) CREATE_ALARM;
        RawXClient.writeShortAt(request, 2, 11);
        RawXClient.writeIntAt(request, 4, alarm);
        RawXClient.writeIntAt(request, 8, CA_ALL);
        RawXClient.writeIntAt(request, 12, counter);
        RawXClient.writeIntAt(request, 16, ABSOLUTE);
        RawXClient.writeIntAt(request, 20, (int) value);
        RawXClient.writeIntAt(request, 24, (int) (value >> 32));
        RawXClient.writeIntAt(request, 28, testType);
        RawXClient.writeIntAt(request, 32, (int) delta);
        RawXClient.writeIntAt(request, 36, (int) (delta >> 32));
        RawXClient.writeIntAt(request, 40, ALARM_NOTIFY);
        client.sendRequest(request);
    }

    /** {@code QueryAlarm}'s reply: the trigger, the delta, and the state at offset 37. */
    private static int alarmState(RawXClient client, int opcode, int alarm, int counter)
            throws IOException {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = (byte) QUERY_ALARM;
        RawXClient.writeShortAt(request, 2, 2);
        RawXClient.writeIntAt(request, 4, alarm);
        client.sendRequest(request);
        byte[] reply = client.readReply();
        assertEquals(counter, RawXClient.readInt(reply, 8),
                "the trigger the alarm reports, which is the counter it watches");
        return reply[37] & 0xFF;
    }

    private static long int64(byte[] bytes, int offset) {
        return (RawXClient.readInt(bytes, offset) & 0xFFFFFFFFL)
                | ((long) RawXClient.readInt(bytes, offset + 4) << 32);
    }

    private static int eventBase(RawXClient client) throws IOException {
        int[] info = client.queryExtensionInfo("SYNC");
        assertTrue(info[0] != 0, "the extension has to be advertised");
        return info[2];
    }

    private static int errorBase(RawXClient client) throws IOException {
        int[] info = client.queryExtensionInfo("SYNC");
        assertTrue(info[0] != 0, "the extension has to be advertised");
        return info[3];
    }

    private static int[] initialize(RawXClient client, int major, int minor) throws IOException {
        byte[] request = new byte[8];
        request[0] = (byte) opcode(client);
        request[1] = (byte) INITIALIZE;
        RawXClient.writeShortAt(request, 2, 2);
        request[4] = (byte) major;
        request[5] = (byte) minor;
        client.sendRequest(request);
        byte[] reply = client.readReply();
        return new int[]{reply[8] & 0xFF, reply[9] & 0xFF};
    }

    /** CreateCounter, SetCounter and ChangeCounter differ only in which request carries the value. */
    private static void int64Request(RawXClient client, int opcode, int minor, int counter, long value)
            throws IOException {
        byte[] request = new byte[16];
        request[0] = (byte) opcode;
        request[1] = (byte) minor;
        RawXClient.writeShortAt(request, 2, 4);
        RawXClient.writeIntAt(request, 4, counter);
        RawXClient.writeIntAt(request, 8, (int) value);
        RawXClient.writeIntAt(request, 12, (int) (value >> 32));
        client.sendRequest(request);
    }

    private static long queryCounter(RawXClient client, int opcode, int counter) throws IOException {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = (byte) QUERY_COUNTER;
        RawXClient.writeShortAt(request, 2, 2);
        RawXClient.writeIntAt(request, 4, counter);
        client.sendRequest(request);
        byte[] reply = client.readReply();
        return (RawXClient.readInt(reply, 8) & 0xFFFFFFFFL)
                | ((long) RawXClient.readInt(reply, 12) << 32);
    }

    private static void trigger(RawXClient client, int opcode, int minor, int fence)
            throws IOException {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = (byte) minor;
        RawXClient.writeShortAt(request, 2, 2);
        RawXClient.writeIntAt(request, 4, fence);
        client.sendRequest(request);
    }

    private static boolean queryFence(RawXClient client, int opcode, int fence) throws IOException {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = (byte) QUERY_FENCE;
        RawXClient.writeShortAt(request, 2, 2);
        RawXClient.writeIntAt(request, 4, fence);
        client.sendRequest(request);
        return client.readReply()[8] != 0;
    }

    private static int opcode(RawXClient client) throws IOException {
        int[] info = client.queryExtensionInfo("SYNC");
        assertTrue(info[0] != 0, "the extension has to be advertised");
        return info[1];
    }
}
