package cn.oyzh.fx.xserver.tools;

import cn.oyzh.fx.xserver.test.RawXClient;

/**
 * The SYNC extension as a client uses it - the harness for the second of B5's three, and the one a
 * toolkit reaches for when it wants to know that a frame has been *seen* rather than merely sent.
 *
 * <p>What it does, in the order a client does it: asks for the version, lists the system counters,
 * makes a counter of its own and reads it back after setting and changing it, reads and sets a
 * client priority, then makes a fence and walks it through its four states - created untriggered,
 * triggered, reset, destroyed - reading {@code QueryFence} after each. Every field is printed whether
 * or not the extension answered, because "the extension is missing" and "the fence never triggers"
 * are different findings.
 *
 * <p>Usage: {@code SyncProbe &lt;host&gt; &lt;port&gt;}.
 */
public final class SyncProbe {

    private static final int INITIALIZE = 0;
    private static final int LIST_SYSTEM_COUNTERS = 1;
    private static final int CREATE_COUNTER = 2;
    private static final int SET_COUNTER = 3;
    private static final int CHANGE_COUNTER = 4;
    private static final int QUERY_COUNTER = 5;
    private static final int DESTROY_COUNTER = 6;
    private static final int SET_PRIORITY = 12;
    private static final int GET_PRIORITY = 13;
    private static final int CREATE_FENCE = 14;
    private static final int TRIGGER_FENCE = 15;
    private static final int RESET_FENCE = 16;
    private static final int DESTROY_FENCE = 17;
    private static final int QUERY_FENCE = 18;
    private static final int CREATE_ALARM = 8;
    private static final int QUERY_ALARM = 10;
    private static final int DESTROY_ALARM = 11;

    /** The {@code XSyncCA*} bits: which of an alarm's fields a create or change request carries. */
    private static final int CA_ALL = 0x3F;
    /** {@code XSyncAlarmNotifyMask}: ask the alarm to send its notification to its creator. */
    private static final int ALARM_NOTIFY = 1;

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("usage: SyncProbe <host> <port>");
            return;
        }
        try (RawXClient client = RawXClient.connect(args[0], Integer.parseInt(args[1]))) {
            int[] info = client.queryExtensionInfo("SYNC");
            System.out.println("SYNC present=" + info[0] + " major=" + info[1]
                    + " firstEvent=" + info[2] + " firstError=" + info[3]);
            if (info[0] == 0) {
                return;
            }
            int opcode = info[1];

            for (int[] asked : new int[][]{{3, 1}, {3, 0}}) {
                byte[] init = new byte[8];
                init[0] = (byte) opcode;
                init[1] = (byte) INITIALIZE;
                RawXClient.writeShortAt(init, 2, 2);
                init[4] = (byte) asked[0];
                init[5] = (byte) asked[1];
                client.sendRequest(init);
                byte[] initReply = client.readReply();
                System.out.println("  Initialize(" + asked[0] + "." + asked[1] + ") -> "
                        + (initReply[8] & 0xFF) + "." + (initReply[9] & 0xFF));
            }

            // The counters a server maintains itself. A reference names one (SERVERTIME); a server
            // with no system counter may answer none, which is a measurement and not a failure - so
            // the count and the names are printed rather than required.
            byte[] list = new byte[4];
            list[0] = (byte) opcode;
            list[1] = (byte) LIST_SYSTEM_COUNTERS;
            RawXClient.writeShortAt(list, 2, 1);
            client.sendRequest(list);
            byte[] listReply = client.readReply();
            int count = RawXClient.readInt(listReply, 8);
            System.out.println("  ListSystemCounters -> " + count + " system counter(s), reply "
                    + listReply.length + " bytes");
            // The entries' own bytes, for the record: the name is a length-prefixed padded list and
            // its stride is the one field a probe cannot guess without reading the reply.
            StringBuilder hex = new StringBuilder();
            for (int i = 32; i < Math.min(listReply.length, 96); i++) {
                hex.append(String.format("%02x", listReply[i]));
            }
            System.out.println("    payload[32..]: " + hex);
            // The entries themselves are **not** decoded, and that is deliberate. The name is a
            // length-prefixed padded list and the stride a first version assumed read one counter's
            // name out of the next one's fields ("counter=0x45434956 name= 7" is really two halves of
            // one entry). Nothing here needs them - this server keeps no system counters at all - and
            // a decoded field that is wrong is worse than an undecoded one, which is the whole of
            // B6's lesson about layouts. The count and the raw bytes are what the comparison uses.
            if (count > 0) {
                System.out.println("    (the entries are left packed; see tools/README.md)");
            }

            int counter = client.allocateId();
            byte[] create = new byte[16];
            create[0] = (byte) opcode;
            create[1] = (byte) CREATE_COUNTER;
            RawXClient.writeShortAt(create, 2, 4);
            RawXClient.writeIntAt(create, 4, counter);
            RawXClient.writeIntAt(create, 8, 0);            // initial value, low word
            RawXClient.writeIntAt(create, 12, 0);           // high word
            client.sendRequest(create);
            client.flush();
            System.out.println("  CreateCounter 0 -> " + queryCounter(client, opcode, counter));

            byte[] set = new byte[16];
            set[0] = (byte) opcode;
            set[1] = (byte) SET_COUNTER;
            RawXClient.writeShortAt(set, 2, 4);
            RawXClient.writeIntAt(set, 4, counter);
            RawXClient.writeIntAt(set, 8, 42);
            RawXClient.writeIntAt(set, 12, 0);
            client.sendRequest(set);
            System.out.println("  SetCounter 42 -> " + queryCounter(client, opcode, counter));

            byte[] change = new byte[16];
            change[0] = (byte) opcode;
            change[1] = (byte) CHANGE_COUNTER;
            RawXClient.writeShortAt(change, 2, 4);
            RawXClient.writeIntAt(change, 4, counter);
            RawXClient.writeIntAt(change, 8, 8);
            RawXClient.writeIntAt(change, 12, 0);
            client.sendRequest(change);
            System.out.println("  ChangeCounter +8 -> " + queryCounter(client, opcode, counter));

            byte[] destroyCounter = new byte[8];
            destroyCounter[0] = (byte) opcode;
            destroyCounter[1] = (byte) DESTROY_COUNTER;
            RawXClient.writeShortAt(destroyCounter, 2, 2);
            RawXClient.writeIntAt(destroyCounter, 4, counter);
            client.sendRequest(destroyCounter);

            System.out.println("  GetPriority(0) -> " + getPriority(client, opcode));
            byte[] setPriority = new byte[12];
            setPriority[0] = (byte) opcode;
            setPriority[1] = (byte) SET_PRIORITY;
            RawXClient.writeShortAt(setPriority, 2, 3);
            RawXClient.writeIntAt(setPriority, 4, 0);
            RawXClient.writeIntAt(setPriority, 8, -7);
            client.sendRequest(setPriority);
            System.out.println("  SetPriority(-7) then GetPriority(0) -> "
                    + getPriority(client, opcode));

            // The fence: created untriggered, triggered, reset, and read back after each. This is the
            // sequence the extension exists for, and the one a client cannot fake.
            int fence = client.allocateId();
            byte[] createFence = new byte[16];
            createFence[0] = (byte) opcode;
            createFence[1] = (byte) CREATE_FENCE;
            RawXClient.writeShortAt(createFence, 2, 4);
            // Drawable first, fence second - which is the opposite of the order the generated
            // class's getters list them in, and the reference says so with BadDrawable on the fence
            // id when a probe believes the getters.
            RawXClient.writeIntAt(createFence, 4, client.root());
            RawXClient.writeIntAt(createFence, 8, fence);
            createFence[12] = 0;                            // initially triggered: no
            client.sendRequest(createFence);
            System.out.println("  CreateFence(untriggered) -> " + queryFence(client, opcode, fence));

            byte[] trigger = new byte[8];
            trigger[0] = (byte) opcode;
            trigger[1] = (byte) TRIGGER_FENCE;
            RawXClient.writeShortAt(trigger, 2, 2);
            RawXClient.writeIntAt(trigger, 4, fence);
            client.sendRequest(trigger);
            System.out.println("  TriggerFence -> " + queryFence(client, opcode, fence));

            byte[] reset = new byte[8];
            reset[0] = (byte) opcode;
            reset[1] = (byte) RESET_FENCE;
            RawXClient.writeShortAt(reset, 2, 2);
            RawXClient.writeIntAt(reset, 4, fence);
            client.sendRequest(reset);
            System.out.println("  ResetFence -> " + queryFence(client, opcode, fence));

            byte[] destroyFence = new byte[8];
            destroyFence[0] = (byte) opcode;
            destroyFence[1] = (byte) DESTROY_FENCE;
            RawXClient.writeShortAt(destroyFence, 2, 2);
            RawXClient.writeIntAt(destroyFence, 4, fence);
            client.sendRequest(destroyFence);
            client.flush();
            System.out.println("  DestroyFence sent");

            alarms(client, opcode, info[2], info[3]);
        }
    }

    /**
     * The alarm half: a server-side trigger on a counter, which is one of the two holes F1 is about.
     *
     * <p>What it does is the only sequence that can tell "the alarm fired" from "the alarm was
     * accepted": make a counter, arm an alarm on it at an absolute threshold with a positive
     * transition, read the alarm back, change the counter across the threshold and read what
     * arrives, then change it again inside the threshold and check that nothing does. A server that
     * accepts the requests and never fires fails at the third step and looks exactly like one that
     * implements them until then - which is why the events and not the replies are the measurement.
     *
     * <p>The notification is a real extension event - 32 bytes at {@code firstEvent + 1}, not a
     * generic event - so it is read with the plain event reader and its type byte is printed.
     */
    private static void alarms(RawXClient client, int opcode, int firstEvent, int firstError)
            throws Exception {
        int counter = client.allocateId();
        byte[] create = new byte[16];
        create[0] = (byte) opcode;
        create[1] = (byte) CREATE_COUNTER;
        RawXClient.writeShortAt(create, 2, 4);
        RawXClient.writeIntAt(create, 4, counter);
        client.sendRequest(create);

        int alarm = client.allocateId();
        client.sendRequest(alarm(CREATE_ALARM, opcode, alarm, counter, 0, 10, 0, 0, ALARM_NOTIFY));
        client.flush();
        System.out.println("  CreateAlarm(counter=" + counter + ", absolute 10, positive transition) -> "
                + queryAlarm(client, opcode, alarm, firstError));

        byte[] change = new byte[16];
        change[0] = (byte) opcode;
        change[1] = (byte) CHANGE_COUNTER;
        RawXClient.writeShortAt(change, 2, 4);
        RawXClient.writeIntAt(change, 4, counter);
        RawXClient.writeIntAt(change, 8, 20);               // 0 -> 20, across the threshold
        client.sendRequest(change);
        client.flush();
        System.out.println("  ChangeCounter +20 -> " + readAlarmNotify(client, firstEvent, firstError));
        System.out.println("    " + queryAlarm(client, opcode, alarm, firstError));

        change = change.clone();
        RawXClient.writeIntAt(change, 8, 5);                // 20 -> 25, same side of it
        client.sendRequest(change);
        client.flush();
        System.out.println("  ChangeCounter +5 (no transition) -> "
                + readAlarmNotify(client, firstEvent, firstError));

        byte[] destroy = new byte[8];
        destroy[0] = (byte) opcode;
        destroy[1] = (byte) DESTROY_ALARM;
        RawXClient.writeShortAt(destroy, 2, 2);
        RawXClient.writeIntAt(destroy, 4, alarm);
        client.sendRequest(destroy);
        client.flush();
        System.out.println("  DestroyAlarm then QueryAlarm -> " + queryAlarm(client, opcode, alarm, firstError));

        // The other direction: a negative transition on a counter that starts above the threshold.
        // Whether it fires going *down* through it is the mirror of the first case and is measured
        // rather than assumed from it.
        int down = client.allocateId();
        client.sendRequest(counterRequest(opcode, down, 20));
        int downAlarm = client.allocateId();
        // testType 1 is NegativeTransition; it is passed as an int because the probe writes bytes.
        client.sendRequest(alarm(CREATE_ALARM, opcode, downAlarm, down, 0, 10, 1, 0, ALARM_NOTIFY));
        client.flush();
        System.out.println("  CreateAlarm(absolute 10, negative transition) on a counter at 20 -> "
                + queryAlarm(client, opcode, downAlarm, firstError));
        sendChange(client, opcode, down, -25);              // 20 -> -5
        System.out.println("  ChangeCounter -25 (down through it) -> "
                + readAlarmNotify(client, firstEvent, firstError));
        System.out.println("    " + queryAlarm(client, opcode, downAlarm, firstError));

        // Delta: what an alarm does after it has fired, which is the one part of the trigger a
        // single crossing cannot show. The counter climbs past the threshold, the alarm's own
        // threshold is read back, and the counter then climbs by exactly the value that would cross
        // it again if the alarm re-armed at `waitValue + delta`.
        int step = client.allocateId();
        client.sendRequest(counterRequest(opcode, step, 0));
        int stepAlarm = client.allocateId();
        client.sendRequest(alarm(CREATE_ALARM, opcode, stepAlarm, step, 0, 10, 0, 100, ALARM_NOTIFY));
        client.flush();
        sendChange(client, opcode, step, 20);
        System.out.println("  ChangeCounter +20 (delta 100 alarm) -> "
                + readAlarmNotify(client, firstEvent, firstError));
        System.out.println("    " + queryAlarm(client, opcode, stepAlarm, firstError));
        sendChange(client, opcode, step, 95);               // 20 -> 115
        System.out.println("  ChangeCounter +95 (to 115) -> " + readAlarmNotify(client, firstEvent, firstError));
        System.out.println("    " + queryAlarm(client, opcode, stepAlarm, firstError));

        // Delta in the other direction, so the mirror rule is measured too and not inferred from
        // the first one: a negative-transition alarm whose delta moves its threshold the way the
        // test needs (down, so a negative delta). The positive-delta form of the same alarm is
        // measured first, because a server can *refuse* a combination whose trigger it could never
        // re-arm - and one does, which is why the refusal is part of the probe.
        int down2 = client.allocateId();
        client.sendRequest(counterRequest(opcode, down2, 20));
        int wrongWay = client.allocateId();
        client.sendRequest(alarm(CREATE_ALARM, opcode, wrongWay, down2, 0, 10, 1, 100, ALARM_NOTIFY));
        client.flush();
        // A one-way request cannot report its own refusal, so the error is read off the stream
        // directly - and it has to be, because an unread error shifts every read after it (which is
        // how this probe first reported a counter value of -4294967420).
        System.out.println("  CreateAlarm(negative transition, delta +100) -> " + readRefusal(client, firstError));

        int downAlarm2 = client.allocateId();
        client.sendRequest(alarm(CREATE_ALARM, opcode, downAlarm2, down2, 0, 10, 1, -100, ALARM_NOTIFY));
        client.flush();
        sendChange(client, opcode, down2, -25);             // 20 -> -5, through 10
        System.out.println("  ChangeCounter -25 (negative transition, delta -100) -> "
                + readAlarmNotify(client, firstEvent, firstError));
        System.out.println("    " + queryAlarm(client, opcode, downAlarm2, firstError));
        // The mirror of the refusal above, so the rule is a rule and not one measured half: a
        // positive-transition alarm whose delta would move its trigger the wrong way.
        int wrongWayUp = client.allocateId();
        client.sendRequest(alarm(CREATE_ALARM, opcode, wrongWayUp, step, 0, 10, 0, -100, ALARM_NOTIFY));
        client.flush();
        System.out.println("  CreateAlarm(positive transition, delta -100) -> " + readRefusal(client, firstError));

        // A comparison rather than a transition: what a server does when the condition is *already*
        // true at the moment of the change, which is the difference between the two test kinds.
        int cmp = client.allocateId();
        client.sendRequest(counterRequest(opcode, cmp, 0));
        int cmpAlarm = client.allocateId();
        client.sendRequest(alarm(CREATE_ALARM, opcode, cmpAlarm, cmp, 0, 10, 2, 0, ALARM_NOTIFY));
        client.flush();
        sendChange(client, opcode, cmp, 20);                // 0 -> 20, first time above
        System.out.println("  ChangeCounter +20 (positive comparison) -> "
                + readAlarmNotify(client, firstEvent, firstError));
        sendChange(client, opcode, cmp, 5);                 // still above it
        System.out.println("  ChangeCounter +5 (still above) -> " + readAlarmNotify(client, firstEvent, firstError));
        sendChange(client, opcode, cmp, -30);               // 25 -> -5, below it
        System.out.println("  ChangeCounter -30 (below it) -> " + readAlarmNotify(client, firstEvent, firstError));
    }

    /** A counter created at a value, which is how an alarm's starting side is set up. */
    private static byte[] counterRequest(int opcode, int counter, long value) {
        byte[] request = new byte[16];
        request[0] = (byte) opcode;
        request[1] = (byte) CREATE_COUNTER;
        RawXClient.writeShortAt(request, 2, 4);
        RawXClient.writeIntAt(request, 4, counter);
        RawXClient.writeIntAt(request, 8, (int) value);
        RawXClient.writeIntAt(request, 12, (int) (value >> 32));
        return request;
    }

    /** ChangeCounter by a signed amount, which is how a transition is driven. */
    private static void sendChange(RawXClient client, int opcode, int counter, long amount)
            throws Exception {
        byte[] request = new byte[16];
        request[0] = (byte) opcode;
        request[1] = (byte) CHANGE_COUNTER;
        RawXClient.writeShortAt(request, 2, 4);
        RawXClient.writeIntAt(request, 4, counter);
        RawXClient.writeIntAt(request, 8, (int) amount);
        RawXClient.writeIntAt(request, 12, (int) (amount >> 32));
        client.sendRequest(request);
        client.flush();
    }

    /** A create (or change) request for an alarm, with every field present. */
    private static byte[] alarm(int minor, int opcode, int id, int counter, int valueType,
                                long value, int testType, long delta, int events) {
        byte[] request = new byte[44];
        request[0] = (byte) opcode;
        request[1] = (byte) minor;                          // CreateAlarm owns one shape only
        RawXClient.writeShortAt(request, 2, 11);
        RawXClient.writeIntAt(request, 4, id);
        RawXClient.writeIntAt(request, 8, CA_ALL);
        RawXClient.writeIntAt(request, 12, counter);
        RawXClient.writeIntAt(request, 16, valueType);
        RawXClient.writeIntAt(request, 20, (int) value);
        RawXClient.writeIntAt(request, 24, (int) (value >> 32));
        RawXClient.writeIntAt(request, 28, testType);
        RawXClient.writeIntAt(request, 32, (int) delta);
        RawXClient.writeIntAt(request, 36, (int) (delta >> 32));
        RawXClient.writeIntAt(request, 40, events);
        return request;
    }

    /** {@code QueryAlarm}: the trigger's own fields, then the alarm's state. */
    private static String queryAlarm(RawXClient client, int opcode, int alarm, int firstError) {
        try {
            byte[] request = new byte[8];
            request[0] = (byte) opcode;
            request[1] = (byte) QUERY_ALARM;
            RawXClient.writeShortAt(request, 2, 2);
            RawXClient.writeIntAt(request, 4, alarm);
            client.sendRequest(request);
            byte[] reply = client.readReply();
            return "counter=" + RawXClient.readInt(reply, 8)
                    + " waitType=" + RawXClient.readInt(reply, 12)
                    + " waitValue=" + int64(reply, 16)
                    + " testType=" + RawXClient.readInt(reply, 24)
                    + " delta=" + int64(reply, 28)
                    + " events=" + (reply[36] & 0xFF)
                    + " state=" + (reply[37] & 0xFF);
        } catch (java.io.IOException refused) {
            // The raw message carries the server's own error base and sequence numbers, so the
            // readable form is reconstructed from it and two servers' refusal of the same request
            // then compares equal.
            String message = refused.getMessage();
            int code = Integer.parseInt(message.substring(message.indexOf("error ") + 6,
                    message.indexOf(" value ")));
            return "refused: " + errorText(code, firstError);
        }
    }

    /**
     * The next message if it is a refusal, which is how a one-way request's answer is read.
     *
     * <p>Reading it is not optional: an error left on the stream is read as the *next* reply and
     * shifts every value after it, which is a wrong measurement that looks like a server bug.
     */
    private static String readRefusal(RawXClient client, int firstError) throws Exception {
        client.readTimeoutMillis(600);
        byte[] message = client.readEvent();
        if (message == null) {
            return "accepted (nothing came back)";
        }
        if ((message[0] & 0x7F) == 0) {
            return "refused: " + errorText(message[1] & 0xFF, firstError) + " value 0x"
                    + Integer.toHexString(RawXClient.readInt(message, 4));
        }
        return "accepted (a message of type " + (message[0] & 0x7F) + " arrived)";
    }

    /**
     * An error named the way a client reads it: a core error by its own code, and an extension error
     * by its offset from the extension's base - because the base is what the server assigns, and
     * printing "135" against "35" for the same BadAlarm is a difference that is not one.
     */
    private static String errorText(int code, int firstError) {
        return code >= firstError ? "extension error +" + (code - firstError) : "core error " + code;
    }

    /** The next event if it is an alarm notification, and what it carried; "none" when it is not. */
    private static String readAlarmNotify(RawXClient client, int firstEvent, int firstError)
            throws Exception {
        client.readTimeoutMillis(600);
        byte[] event = client.readEvent();
        if (event == null) {
            return "no event";
        }
        if ((event[0] & 0x7F) == 0) {
            return "refused: " + errorText(event[1] & 0xFF, firstError) + " value 0x"
                    + Integer.toHexString(RawXClient.readInt(event, 4));
        }
        if ((event[0] & 0x7F) != firstEvent + 1) {
            return "a message of type " + (event[0] & 0x7F) + " instead of the alarm's "
                    + (firstEvent + 1);
        }
        // The event's fields sit right after its two byte head, not after a request's four: alarm at
        // 4, the two 64 bit values at 8 and 16, and the state at 28 (read off the generated writer).
        return "AlarmNotify kind=" + (event[1] & 0xFF) + " alarm=" + RawXClient.readInt(event, 4)
                + " counterValue=" + int64(event, 8)
                + " alarmValue=" + int64(event, 16)
                + " state=" + (event[28] & 0xFF);
    }

    private static long int64(byte[] bytes, int offset) {
        return (RawXClient.readInt(bytes, offset) & 0xFFFFFFFFL)
                | ((long) RawXClient.readInt(bytes, offset + 4) << 32);
    }

    private static String getPriority(RawXClient client, int opcode) throws Exception {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = (byte) GET_PRIORITY;
        RawXClient.writeShortAt(request, 2, 2);
        RawXClient.writeIntAt(request, 4, 0);
        client.sendRequest(request);
        return String.valueOf(RawXClient.readInt(client.readReply(), 8));
    }

    private static String queryFence(RawXClient client, int opcode, int fence) throws Exception {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = (byte) QUERY_FENCE;
        RawXClient.writeShortAt(request, 2, 2);
        RawXClient.writeIntAt(request, 4, fence);
        client.sendRequest(request);
        return client.readReply()[8] == 0 ? "triggered=no" : "triggered=yes";
    }

    private static String queryCounter(RawXClient client, int opcode, int counter) throws Exception {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = (byte) QUERY_COUNTER;
        RawXClient.writeShortAt(request, 2, 2);
        RawXClient.writeIntAt(request, 4, counter);
        client.sendRequest(request);
        byte[] reply = client.readReply();
        long low = RawXClient.readInt(reply, 8) & 0xFFFFFFFFL;
        long high = RawXClient.readInt(reply, 12);
        return String.valueOf(low | (high << 32));
    }

    private SyncProbe() {
    }
}
