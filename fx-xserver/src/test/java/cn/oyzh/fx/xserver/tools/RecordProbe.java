package cn.oyzh.fx.xserver.tools;

import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.test.RawXClient;

/**
 * The RECORD extension as a client uses it - the harness for F2.
 *
 * <p>RECORD is the extension a client subscribes to in order to see <em>other</em> clients' traffic:
 * a context is created over a set of clients and a set of ranges (core and extension requests and
 * replies, delivered events, device events, errors, a client coming and going), it is enabled, and
 * from then on the server streams one reply per recorded datum to the enabling client until the
 * context is disabled. {@code x11vnc -record} is the client this project cares about.
 *
 * <p>The probe has two halves, and the second is why it exists. The first half is the request
 * surface - {@code QueryVersion}, a context that is created, read back, added to and removed from,
 * and the error a bad context id is refused with - which is the half a client cannot see a
 * difference in. The second half drives a gesture from a <em>second</em> connection while the first
 * is recording it, and prints every datum that arrives (its category, its element header and what
 * the recorded data actually was), which is the half that pins what the stream has to contain.
 *
 * <p>A note on the wire, because there is no generated protocol module for RECORD in the x11 library
 * and this probe is the measurement that fixes it: {@code EnableContext} answers not with one reply
 * but with a stream of them, one datum each, and the datum's {@code length} field counts the data
 * *after* the 32 byte header - measured, after a first reading of it as the whole reply made every
 * datum after the first start at the wrong offset. The first datum of a recording is always
 * {@code StartOfData}, with no data at all, and <b>no datum is ever {@code EndOfData}</b>: the
 * reference sent none for a disable, for a free or for a second enable while it was already
 * streaming, so a recorder learns the stream is over from its own request rather than from the
 * stream.
 *
 * <p>The datum's data is a *batch* of recorded items, each preceded by the stamps the element header
 * asked for, and the items are framed only by their own protocol headers - so reading the stream back
 * is protocol parsing, and this probe does it rather than trusting a fixed stride.
 *
 * <p>Two categories carry a payload of their own and the probe decodes both, because both are
 * guessable and both were guessed wrong first: a {@code ClientStarted} carries the <em>whole
 * connection setup reply</em> of the client that arrived - status, protocol versions, size, release -
 * and the setup reply is <em>not</em> also recorded as a server message, so a recorder that watches
 * ordinary traffic has no way to learn the client's resource base; and a {@code ClientDied} carries
 * the departing client's <em>last sequence number</em> in four bytes, in that client's own byte
 * order. The second was measured with a range covering <em>one</em> request of the nine a test client
 * sent: the ninth still arrived, so it is the sequence and not a count of what the range let through.
 *
 * <p>Usage: {@code RecordProbe &lt;host&gt; &lt;port&gt;}.
 */
public final class RecordProbe {

    /** The extension's own requests, in protocol order. */
    private static final int QUERY_VERSION = 0;
    private static final int CREATE_CONTEXT = 1;
    private static final int REGISTER_CLIENTS = 2;
    private static final int UNREGISTER_CLIENTS = 3;
    private static final int GET_CONTEXT = 4;
    private static final int ENABLE_CONTEXT = 5;
    private static final int DISABLE_CONTEXT = 6;
    private static final int FREE_CONTEXT = 7;

    /** The three client specs that are not a client's id: the clients in the ranges below. */
    private static final int CURRENT_CLIENTS = 1;
    private static final int FUTURE_CLIENTS = 2;
    private static final int ALL_CLIENTS = 3;

    /** The element header bits: stamp each datum with the server's time, the client's, its sequence. */
    private static final int FROM_SERVER_TIME = 0x01;
    private static final int FROM_CLIENT_TIME = 0x02;
    private static final int FROM_CLIENT_SEQUENCE = 0x04;

    /** What a datum is: the six categories of the protocol, in order. */
    private static final String[] CATEGORIES = {
            "FromServer", "FromClient", "ClientStarted", "ClientDied", "StartOfData", "EndOfData"};

    /**
     * True when the stream's length field counts the whole reply rather than the data after it.
     *
     * <p>False, and measured rather than assumed: a {@code ClientStarted} datum's data is the new
     * client's whole setup reply, and the reply's own length field says how long that is - which
     * makes the datum's field 913 units for a 3652 byte blob, with the eight units of header on top.
     * Read the other way, that same field is 913 * 4 = 3652 bytes *after* the header and every datum
     * after the first is read from the wrong offset.
     */
    private static final boolean LENGTH_COUNTS_WHOLE_REPLY = false;

    private static final int REPLY_HEADER = 32;

    /** Which reading an item gets: the server's own data, a client's request, or a device event. */
    private static final int SERVER = 0;
    private static final int REQUEST = 1;
    private static final int EVENT = 2;

    /** The element header a recorder actually uses: both times and the client's sequence. */
    private static final int ALL_STAMPS = FROM_SERVER_TIME | FROM_CLIENT_TIME | FROM_CLIENT_SEQUENCE;

    /** How many datums to print before the stream is left to the timeout. */
    private static final int MAX_DATUMS = 40;

    /** The recorder's own resource base, and the recorded client's once its arrival says so. */
    private static int recorderBase;
    private static int actorBase;

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("usage: RecordProbe <host> <port> [--element-headers] [--free-enabled]"
                    + " [--narrow] [--input]");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        java.util.Set<String> flags = new java.util.HashSet<>();
        for (int i = 2; i < args.length; i++) {
            flags.add(args[i]);
        }

        int[] info;
        try (RawXClient rec = RawXClient.connect(host, port)) {
            recorderBase = rec.resourceIdBase();
            info = rec.queryExtensionInfo("RECORD");
            System.out.println("RECORD present=" + info[0] + " firstEvent=" + info[2]
                    + " firstError=" + info[3]);
            if (info[0] == 0) {
                return;
            }
            int opcode = info[1];
            int firstError = info[3];

            queryVersions(rec, opcode);
            clientSpecs(host, port);
            int context = rec.allocateId();
            refusal(rec, opcode, context, firstError);
            context(rec, host, port, opcode, context, firstError);
        }

        boolean freeWhileEnabled = flags.contains("--free-enabled");
        boolean input = flags.contains("--input");
        if (flags.contains("--narrow")) {
            // One range that covers a single core request, so that what a datum carries can be told
            // apart from what the recording happened to contain: the actor sends six requests and only
            // one of them is inside this range, which is the experiment that says whether a client-died
            // element's four bytes are its *last sequence number* or a count of the elements recorded.
            streamOnItsOwn(host, port, info[1], info[3], ALL_STAMPS, freeWhileEnabled, narrowRange(),
                    input);
        } else if (flags.contains("--element-headers")) {
            // The one-off table that fixes what each stamp bit adds, which is a measurement rather than
            // part of the sweep: a stream is compared for its *content*, and the width of a stamp is
            // something the analysis of that table settled once.
            for (int header : new int[]{ALL_STAMPS, FROM_SERVER_TIME, FROM_CLIENT_TIME,
                    FROM_CLIENT_SEQUENCE, FROM_SERVER_TIME | FROM_CLIENT_TIME,
                    FROM_SERVER_TIME | FROM_CLIENT_SEQUENCE, FROM_CLIENT_TIME | FROM_CLIENT_SEQUENCE, 0}) {
                System.out.println("  elementHeader=0x0" + Integer.toHexString(header)
                        + " ---------------------------------------------");
                streamOnItsOwn(host, port, info[1], info[3], header, freeWhileEnabled,
                        broadRange(), input);
            }
        } else {
            streamOnItsOwn(host, port, info[1], info[3], ALL_STAMPS, freeWhileEnabled, broadRange(),
                    input);
        }
    }

    /**
     * One recording of the gesture, on a connection of its own.
     *
     * <p>The recordable clients are the ones that arrive *after* the context is made, and that is
     * deliberate twice over: a recording of every client also carries whatever else is on the display
     * (x11vnc's own polling, on the reference), and a client this probe did not start is a client whose
     * traffic is not the same twice.
     */
    private static void streamOnItsOwn(String host, int port, int opcode, int firstError, int header,
                                       boolean freeWhileEnabled, byte[] range, boolean input)
            throws Exception {
        try (RawXClient rec = RawXClient.connect(host, port)) {
            int context = rec.allocateId();
            rec.sendRequest(createContext(opcode, context, header,
                    new int[]{FUTURE_CLIENTS}, new byte[][]{range}));
            rec.sendRequest(getContext(opcode, context));
            rec.readReply();
            stream(rec, host, port, opcode, context, firstError, freeWhileEnabled, input);
        }
    }

    // ------------------------------------------------------------------
    // The request surface
    // ------------------------------------------------------------------

    /** The version pairs a client asks for: the reply's rule is per-extension, so ask three. */
    private static void queryVersions(RawXClient client, int opcode) throws Exception {
        for (int[] asked : new int[][]{{1, 13}, {1, 0}, {2, 0}}) {
            byte[] request = header(opcode, QUERY_VERSION, 2);
            RawXClient.writeShortAt(request, 4, asked[0]);
            RawXClient.writeShortAt(request, 6, asked[1]);
            client.sendRequest(request);
            byte[] reply = client.readReply();
            System.out.println("  QueryVersion(" + asked[0] + "." + asked[1] + ") -> "
                    + RawXClient.readShort(reply, 8) + "." + RawXClient.readShort(reply, 10));
        }
    }

    /**
     * A context id that was never created, which is the extension's own error and not a core one:
     * the code is printed as an offset from the extension's error base, because the base is the
     * opcode the server assigned and a raw code would differ between two servers for that reason
     * alone.
     */
    private static void refusal(RawXClient client, int opcode, int context, int firstError)
            throws Exception {
        client.sendRequest(getContext(opcode, context));
        try {
            byte[] reply = client.readReply();
            System.out.println("  GetContext(unknown) -> accepted, type=" + (reply[0] & 0xFF));
        } catch (Exception refused) {
            System.out.println("  GetContext(unknown) -> " + readRefusalText(refused, firstError));
        }
    }

    /**
     * A context created, read back, added to and removed from - the request surface, on one
     * connection, with a keeper connection open so that the client a spec resolves to can be looked
     * for by name rather than counted.
     */
    private static void context(RawXClient client, String host, int port, int opcode, int context,
                                int firstError) throws Exception {
        // A context over the clients that will arrive, with the element header asking for all three
        // stamps: that is what makes the recorded time and sequence fields arrive. The *future* clients
        // rather than every client, so that the stream this context enables is only the actor's.
        int header = FROM_SERVER_TIME | FROM_CLIENT_TIME | FROM_CLIENT_SEQUENCE;
        try (RawXClient keeper = RawXClient.connect(host, port)) {
            int keeperBase = keeper.allocateId() & ~ClientConnection.RESOURCE_MASK;
            client.sendRequest(createContext(opcode, context, header,
                    new int[]{FUTURE_CLIENTS}, new byte[][]{broadRange()}));
            System.out.println("  CreateContext(future clients, elementHeader=0x0" + header
                    + ") -> ok");

            System.out.println("  GetContext -> "
                    + describe(client, opcode, context, firstError, keeperBase));

            // Adding a *second* spec to the same context, over the clients that exist now, which is how
            // a client widens what it hears without making a new context.
            client.sendRequest(registerClients(opcode, context, header,
                    new int[]{CURRENT_CLIENTS}, new byte[][]{eventsOnly()}));
            System.out.println("  RegisterClients(current clients) -> "
                    + describe(client, opcode, context, firstError, keeperBase));

            client.sendRequest(unregisterClients(opcode, context, new int[]{CURRENT_CLIENTS}));
            System.out.println("  UnregisterClients -> "
                    + describe(client, opcode, context, firstError, keeperBase));
        }
    }

    /** Reads a context back: what it holds is the client's own request echoed, so it is comparable. */
    private static String describe(RawXClient client, int opcode, int context, int firstError,
                                   int keeperBase) throws Exception {
        client.sendRequest(getContext(opcode, context));
        try {
            return describe(client.readReply(), keeperBase);
        } catch (Exception refused) {
            return readRefusalText(refused, firstError);
        }
    }

    /** One 24 byte range, printed as the client sent it - this is the encoding being pinned. */
    private static String describeRange(byte[] bytes, int at) {
        return "coreReq=" + u(bytes[at]) + ".." + u(bytes[at + 1])
                + " coreRep=" + u(bytes[at + 2]) + ".." + u(bytes[at + 3])
                + " extReq=" + u(bytes[at + 4]) + ".." + u(bytes[at + 5])
                + ":" + RawXClient.readShort(bytes, at + 6) + ".."
                + RawXClient.readShort(bytes, at + 8)
                + " extRep=" + u(bytes[at + 10]) + ".." + u(bytes[at + 11])
                + ":" + RawXClient.readShort(bytes, at + 12) + ".."
                + RawXClient.readShort(bytes, at + 14)
                + " events=" + u(bytes[at + 16]) + ".." + u(bytes[at + 17])
                + " device=" + u(bytes[at + 18]) + ".." + u(bytes[at + 19])
                + " errors=" + u(bytes[at + 20]) + ".." + u(bytes[at + 21])
                + " started=" + u(bytes[at + 22]) + " died=" + u(bytes[at + 23]);
    }

    // ------------------------------------------------------------------
    // The stream
    // ------------------------------------------------------------------

    /**
     * What each client spec stores, one connection per variant.
     *
     * <p>A fresh connection each time, because a request that is refused leaves an error on the stream
     * and the *next* read consumes it - so on one connection a refusal is attributed to whichever
     * request happens to be read next. The error's own sequence number says which request it belongs
     * to, and {@link #tryCreate} prints it.
     *
     * <p>A fourth variant was measured and is deliberately not compared: {@code [current,future]}
     * resolves to seven specs on a reference while {@code [all]} - the same set - resolves to eight,
     * and no rule makes those two agree. What is compared is the four cases a reader can reason about.
     */
    private static void clientSpecs(String host, int port) throws Exception {
        // A keeper connection, so that "the clients that are here now" is a set this probe controls:
        // the keeper is present on both servers, and its base is the one spec that can be looked for
        // by name. The *count* of the specs is not compared - it is whatever else is on the display,
        // which for a reference includes a window manager and a VNC server and for this server does
        // not.
        try (RawXClient keeper = RawXClient.connect(host, port)) {
            int keeperBase = keeper.allocateId() & ~ClientConnection.RESOURCE_MASK;
            for (int[] spec : new int[][]{{}, {CURRENT_CLIENTS}, {FUTURE_CLIENTS}, {ALL_CLIENTS}}) {
                StringBuilder names = new StringBuilder();
                for (int client : spec) {
                    names.append(names.length() == 0 ? "" : ",").append(name(client));
                }
                tryCreate(host, port, "clients=[" + names + "]", spec,
                        new byte[][]{eventsOnly()}, 0x07, keeperBase);
            }
            tryCreate(host, port, "everything", new int[]{ALL_CLIENTS},
                    new byte[][]{broadRange()}, 0x07, keeperBase);
        }
    }

    private static String name(int spec) {
        return switch (spec) {
            case CURRENT_CLIENTS -> "current";
            case FUTURE_CLIENTS -> "future";
            case ALL_CLIENTS -> "all";
            default -> Integer.toString(spec);
        };
    }

    private static void tryCreate(String host, int port, String what, int[] clients,
                                  byte[][] ranges, int header, int keeperBase) throws Exception {
        try (RawXClient client = RawXClient.connect(host, port)) {
            int[] info = client.queryExtensionInfo("RECORD");
            int context = client.allocateId();
            client.sendRequest(createContext(info[1], context, header, clients, ranges));
            client.sendRequest(getContext(info[1], context));
            try {
                System.out.println("  " + what + " -> "
                        + describe(client.readReply(), keeperBase));
            } catch (Exception refused) {
                System.out.println("  " + what + " -> " + readRefusalText(refused, info[3]));
            }
        }
    }

    /**
     * A context read back, summarised for the two things that are the same on any two servers.
     *
     * <p>Which clients a context resolved to is compared by <em>name</em> - is the keeper that is here
     * now in it, is the literal 2 that stands for a client that has not arrived - because the ids
     * themselves and the number of specs are the display's, not the extension's. The ranges are the
     * client's own bytes echoed back, so those are printed in full for the specs that are looked for.
     */
    private static String describe(byte[] reply, int keeperBase) {
        int enabled = reply[1] & 0xFF;
        int element = reply[8] & 0xFF;
        int clients = RawXClient.readInt(reply, 12);
        boolean hasKeeper = false;
        boolean hasFuture = false;
        String keeper = null;
        String future = null;
        for (int i = 0, at = 32; i < clients; i++) {
            int resource = RawXClient.readInt(reply, at);
            int ranges = RawXClient.readInt(reply, at + 4);
            if (resource == keeperBase) {
                hasKeeper = true;
                keeper = specs(resource, reply, at, ranges);
            } else if (resource == FUTURE_CLIENTS) {
                hasFuture = true;
                future = specs(resource, reply, at, ranges);
            }
            at += 8 + ranges * 24;
        }
        StringBuilder text = new StringBuilder("enabled=" + enabled + " elementHeader=0x0" + element
                + " hasKeeper=" + hasKeeper + " hasFuture=" + hasFuture);
        if (keeper != null) {
            text.append("\n    ").append(keeper);
        }
        if (future != null) {
            text.append("\n    ").append(future);
        }
        return text.toString();
    }

    /**
     * One spec, named by what it is rather than by the base it carries.
     *
     * <p>A base is the *server's* to choose, and a server that reuses a freed one - this one does, a
     * reference does not here - puts the same literal in two places, which is a difference the diff's
     * role mapping turns into a run of unrelated differences (measured 2026-09-28: the same run
     * reported identical and then different, with the datums themselves byte for byte alike). What
     * the probe compares is which client a spec names, so that is what it prints.
     */
    private static String specs(int resource, byte[] reply, int at, int ranges) {
        String which = resource == FUTURE_CLIENTS ? "future" : "keeper";
        StringBuilder text = new StringBuilder("spec=").append(which)
                .append(" ranges=").append(ranges);
        for (int r = 0; r < ranges; r++) {
            text.append("\n      ").append(describeRange(reply, at + 8 + r * 24));
        }
        return text.toString();
    }

    /**
     * Enables the context and reads the datums a gesture produces.
     *
     * <p>The gesture is made from a second connection, because that is the whole point of the
     * extension: a client recording *another* client. It connects after the context is enabled, so
     * its arrival is a datum of its own, and it closes at the end so its departure is one too.
     */
    private static void stream(RawXClient rec, String host, int port, int opcode, int context,
                               int firstError, boolean freeWhileEnabled, boolean input)
            throws Exception {
        rec.socketTimeout(1500);
        rec.sendRequest(header(opcode, ENABLE_CONTEXT, 2, context));

        Actor actor = new Actor(host, port, input);
        Thread thread = new Thread(actor, "record-probe-actor");
        thread.start();

        int datums = 0;
        while (datums < MAX_DATUMS && readDatum(rec, datums)) {
            datums++;
        }
        thread.join(3000);
        // Sent while the context is *still enabled*: this is where the protocol's EndOfData belongs, if
        // a server sends one at all - and a second gesture after it is what shows the context went on
        // streaming rather than being left disabled.
        rec.sendRequest(header(opcode, ENABLE_CONTEXT, 2, context));
        while (datums < MAX_DATUMS && readDatum(rec, datums)) {
            datums++;
        }
        Actor again = new Actor(host, port, input);
        Thread second = new Thread(again, "record-probe-actor-2");
        second.start();
        while (datums < MAX_DATUMS && readDatum(rec, datums)) {
            datums++;
        }
        second.join(3000);
        // The stream does not end when the gesture does: it ends when the recording client says so.
        // Waiting for the actor to go quiet first is what gives the departure datum a chance to be
        // written before the stream is closed.
        //
        // **Three seconds, not one and a half**, because a departure is asynchronous on both sides:
        // a reference notices a client's closed socket on its next select wakeup, which can be a
        // second later, and with the shorter deadline the diff was sometimes 16 datums against this
        // server's 17 - the client-died element of the *reference* arriving just after the probe had
        // given up (measured 2026-09-28, the sweep catching a race nothing else had).
        Thread.sleep(300);
        rec.socketTimeout(3000);
        if (freeWhileEnabled) {
            // Freeing a context that is *still enabled* is the other way a stream can end, and the
            // protocol's EndOfData has to be somewhere: this is where a recorder looks for it.
            rec.sendRequest(header(opcode, FREE_CONTEXT, 2, context));
            while (datums < MAX_DATUMS && readDatum(rec, datums)) {
                datums++;
            }
        } else {
            rec.sendRequest(header(opcode, DISABLE_CONTEXT, 2, context));
            while (datums < MAX_DATUMS && readDatum(rec, datums)) {
                datums++;
            }
            // And the context itself is freed after that: a recorder that only disables keeps the
            // context (and its ranges) to enable again later, which is the whole reason the two are
            // separate requests at all.
            rec.sendRequest(header(opcode, FREE_CONTEXT, 2, context));
            while (datums < MAX_DATUMS && readDatum(rec, datums)) {
                datums++;
            }
        }
        System.out.println("  actor: " + actor.outcome + ", second: " + again.outcome);
        System.out.println("  datums=" + datums);
        if (actor.failure != null) {
            System.out.println("  actor failed: " + actor.failure);
        }
        if (again.failure != null) {
            System.out.println("  second actor failed: " + again.failure);
        }
    }

    /**
     * Reads one datum and prints it, {@code false} when the stream has gone quiet or ended.
     *
     * <p>{@code EndOfData} is the server's own full stop, so it is the one datum that ends the read
     * rather than the timeout - and a {@code DisableContext} has nothing else to read, which is why a
     * probe that waits for the clock instead ends up with a reply left over that poisons every read
     * after it.
     */
    private static boolean readDatum(RawXClient rec, int index) throws Exception {
        byte[] header;
        try {
            header = rec.readBytes(REPLY_HEADER);
        } catch (java.net.SocketTimeoutException quiet) {
            return false;
        }
        if ((header[0] & 0xFF) != 1) {
            System.out.println("  (not a reply) type=" + (header[0] & 0xFF) + " length="
                    + RawXClient.readInt(header, 4) + " hex=" + hex(header));
            return true;
        }
        int category = header[1] & 0xFF;
        int length = RawXClient.readInt(header, 4);
        int dataBytes = length * 4 - (LENGTH_COUNTS_WHOLE_REPLY ? REPLY_HEADER : 0);
        // A client-started datum's length is its setup reply's size, which is the server's own and
        // not the extension's, so it is left out here rather than normalised away for every datum.
        String lengthField = category == 2 ? "" : " length=" + length;
        byte[] data;
        try {
            data = dataBytes > 0 ? rec.readBytes(dataBytes) : new byte[0];
        } catch (java.net.SocketTimeoutException quiet) {
            // A datum's own length was answered but its body never arrived: the framing is wrong, and
            // saying so is more useful than a stack trace.
            System.out.println("  (truncated: " + dataBytes + " bytes of body never came)");
            return false;
        }
        System.out.println("  " + (index < 9 ? " " : "") + (index + 1) + " category=" + category
                + "(" + (category < CATEGORIES.length ? CATEGORIES[category] : "?") + ")"
                + " seq=" + RawXClient.readShort(header, 2)
                + lengthField
                + " element=0x" + Integer.toHexString(header[8] & 0xFF)
                + " swapped=" + (header[9] & 0xFF)
                + " idBase=" + whichClient(RawXClient.readInt(header, 12))
                + " time=" + (RawXClient.readInt(header, 16) & 0xFFFFFFFFL)
                + " recordedSeq=" + RawXClient.readInt(header, 20)
                + " " + describeData(category, data));
        return category != 5;
    }

    /**
     * Which client a datum belongs to, named rather than numbered.
     *
     * <p>The base is the server's to choose and differs between two servers by construction; what a
     * comparison needs is *which* of the probe's connections it names. A client-started element is
     * what tells the probe the recorded client's base, so it is recorded here as it goes past.
     */
    private static String whichClient(int base) {
        if (base == 0) {
            return "0";
        }
        if (base == recorderBase) {
            return "recorder";
        }
        if (base == actorBase) {
            return "actor";
        }
        if (actorBase == 0) {
            actorBase = base;                   // the first client-started element is the actor
            return "actor";
        }
        return "other";
    }

    /**
     * What a datum's data is, decoded.
     *
     * <p>For the two data categories the payload is a *batch*: one or more recorded items, each
     * preceded by the stamps the element header asked for. Nothing in the reply says where one item
     * ends and the next begins - that comes from the item itself, which is exactly the parsing work a
     * recorder's own protocol layer does. So the only way to name the items is to parse them, and a
     * parse that consumes the batch exactly is also the proof that the stamps are the width they were
     * assumed to be.
     */
    private static String describeData(int category, byte[] data) {
        if (category == 2) {
            // A client arriving, whose data is the whole of that client's setup reply - which is how a
            // recorder learns the client's resource id base before any of its ids appear in the datums
            // that follow. The fields are printed rather than the bytes: the release number and the
            // length are the server's own, and a recorder is told them here rather than comparing them.
            if (data.length < 12) {
                return "bytes=" + data.length + " " + hex(data);
            }
            // The size and the length field are the server's own setup reply's, so they are named
            // apart from every other size a probe prints - a reference's is 3652 bytes and this
            // server's 336, and neither is a decision RECORD makes (tools/probe-diff.py substitutes
            // the two names and the release number, and compares everything else).
            return "setupBytes=" + data.length + " status=" + u(data[0])
                    + " version=" + RawXClient.readShort(data, 2) + "." + RawXClient.readShort(data, 4)
                    + " setupUnits=" + RawXClient.readShort(data, 6)
                    + " release=" + hex(data, 8, 12);
        }
        if (category == 3) {
            // A client leaving: four bytes of its last sequence number, and the datum's own header
            // carries the same number - measured with a range that covered one request of six.
            return "bytes=" + data.length + (data.length == 4
                    ? " lastSequence=" + RawXClient.readInt(data, 0) : "");
        }
        if (category == 4 || category == 5) {
            // The two control datums: start and end of data carry one sequence number and nothing
            // else, which is why they are the only datums this short.
            return "payload=" + hex(data);
        }
        // The stamps are per element and their width depends on what is being recorded, so try the
        // widths an element header could ask for and keep the ones that parse. More than one means the
        // width is genuinely ambiguous for this datum and the print says so.
        int found = -1;
        String parsed = null;
        StringBuilder also = new StringBuilder();
        for (int[] attempt : attemptsFor(category)) {
            String candidate = parseBatch(data, attempt[0], attempt[1]);
            if (candidate == null) {
                continue;
            }
            if (found < 0) {
                found = attempt[0];
                parsed = candidate;
            } else {
                also.append(also.length() == 0 ? " (also " : ",").append(attempt[0])
                        .append(attempt[1] == REQUEST ? "r" : "e");
            }
        }
        if (found < 0) {
            return "unparsed len=" + data.length + " " + hex(data, 64);
        }
        return "stamps=" + found + (also.length() == 0 ? "" : also + ")") + " " + parsed;
    }

    /**
     * The width and reading to try for a datum's batch, in the order they are tried.
     *
     * <p>The two directions hold different things and that is what makes the reading decidable: a
     * {@code FromClient} datum holds requests and device events, never replies or errors, and a
     * {@code FromServer} datum holds replies, errors and delivered events, never requests. Without
     * that, byte {@code 0x01} is both {@code CreateWindow}'s opcode and a reply's type and nothing in
     * the bytes themselves says which.
     */
    private static int[][] attemptsFor(int category) {
        if (category == 1) {
            // A request first: a device event's type and a core request's opcode are the same numbers,
            // so the event reading is only the fallback for a batch the request reading cannot consume.
            return new int[][]{{8, REQUEST}, {12, REQUEST}, {8, EVENT}, {12, EVENT}, {4, REQUEST}};
        }
        return new int[][]{{4, SERVER}, {8, SERVER}, {12, SERVER}};
    }

    /**
     * Parses a datum's payload as a batch of stamped items, or {@code null} if {@code stamps} bytes of
     * stamp per item does not consume it exactly.
     */
    private static String parseBatch(byte[] data, int stamps, int kind) {
        StringBuilder text = new StringBuilder();
        int at = 0;
        while (at < data.length) {
            if (at + stamps > data.length) {
                return null;
            }
            long recordedSeq = stamps >= 8 ? RawXClient.readInt(data, at + 4) & 0xFFFFFFFFL : 0;
            long time = stamps >= 4 ? RawXClient.readInt(data, at) & 0xFFFFFFFFL : 0;
            at += stamps;
            int size = itemLength(data, at, kind);
            if (size < 0 || at + size > data.length) {
                return null;
            }
            if (text.length() > 0) {
                text.append("; ");
            }
            text.append(nameItem(data, at, kind)).append("/").append(size).append("b")
                    .append("@").append(Long.toHexString(time));
            if (stamps >= 8) {
                text.append("+seq").append(recordedSeq);
            }
            at += size;
        }
        return text.length() == 0 ? "empty" : text.toString();
    }

    /**
     * How long the item that starts at {@code at} is, from its own header.
     *
     * <p>A recorded item is one of: an error (32 bytes, no length field), a reply or a generic event
     * (32 bytes plus the length field's units), a core event (32 bytes) or a request (its length field's
     * units, which count the request's own four bytes).
     */
    private static int itemLength(byte[] data, int at, int kind) {
        if (at + 8 > data.length) {
            return -1;
        }
        int first = data[at] & 0xFF;
        if (kind == REQUEST) {
            // A request's length counts the request's own four bytes, and cannot be zero - a zero would
            // consume nothing and the batch would be read in a loop.
            int units = (data[at + 2] & 0xFF) | ((data[at + 3] & 0xFF) << 8);
            return units == 0 ? -1 : units * 4;
        }
        if (kind == EVENT) {
            return first == 35 ? 32 + RawXClient.readInt(data, at + 4) * 4 : 32;
        }
        if (first == 0) {
            return 32;
        }
        if (first == 1 || first == 35) {
            return 32 + RawXClient.readInt(data, at + 4) * 4;
        }
        return 32;
    }

    private static String nameItem(byte[] data, int at, int kind) {
        int first = data[at] & 0xFF;
        if (kind == REQUEST) {
            return "request " + (first >= 128 ? "ext minor=" + u(data[at + 1])
                    : coreRequestName(first));
        }
        if (kind == EVENT) {
            return first == 35 ? "device event generic" : "device " + eventName(first, data, at);
        }
        if (first == 0) {
            return "error" + (data[at + 1] & 0xFF);
        }
        if (first == 1) {
            return "reply seq=" + u(data[at + 2]);
        }
        if (first == 35) {
            return "event generic ext=" + u(data[at + 1]) + " type=" + u(data[at + 2]);
        }
        return "event " + eventName(first, data, at);
    }

    /** The core requests the actor sends, by name, so the diff compares decisions rather than numbers. */
    private static String coreRequestName(int opcode) {
        return switch (opcode) {
            case 1 -> "CreateWindow";
            case 2 -> "ChangeWindowAttributes";
            case 3 -> "GetWindowAttributes";
            case 4 -> "DestroyWindow";
            case 8 -> "MapWindow";
            case 10 -> "UnmapWindow";
            case 43 -> "GetInputFocus";
            case 53 -> "CreateGC";
            case 55 -> "CreateColormap";
            case 60 -> "AllocColor";
            case 109 -> "QueryExtension";
            default -> "opcode" + opcode;
        };
    }

    /**
     * Events by name, with the one field of each that the gesture decides.
     *
     * <p>The window ids inside them are left out: they are the actor's own ids out of the range its
     * server handed it, and they differ between two servers by construction. What is compared is
     * *which* event arrived and in what order.
     */
    private static String eventName(int type, byte[] data, int at) {
        int detail = at + 1 < data.length ? data[at + 1] & 0xFF : -1;
        return switch (type) {
            case 2 -> "KeyPress key=" + detail;
            case 3 -> "KeyRelease key=" + detail;
            case 4 -> "ButtonPress button=" + detail;
            case 5 -> "ButtonRelease button=" + detail;
            case 6 -> "MotionNotify detail=" + detail;
            case 12 -> "Expose";
            case 16 -> "CreateNotify";
            case 17 -> "DestroyNotify";
            case 18 -> "UnmapNotify";
            case 19 -> "MapNotify";
            case 21 -> "ReparentNotify";
            case 22 -> "ConfigureNotify";
            case 34 -> "MappingNotify";
            default -> "type" + type;
        };
    }

    /**
     * The gesture: a window created, mapped, unmapped and destroyed.
     *
     * <p>The pointer and the button through XTEST are behind {@code --input} and are <em>not</em> part
     * of the sweep's gesture, because that half is the one thing the two servers genuinely answer
     * differently: a reference records input as its own device elements on its own loop, and this
     * server derives its input events inside the request that caused them and has no such stage. The
     * protocol half - what a client sends and what comes back for it - is compared instead, and the
     * input half is measured separately (see {@code docs/backlog.md} §9 F2).
     */
    private static final class Actor implements Runnable {

        private final String host;
        private final int port;
        private final boolean input;

        private volatile String outcome = "did not run";
        private volatile String failure;

        Actor(String host, int port, boolean input) {
            this.host = host;
            this.port = port;
            this.input = input;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(300);
                try (RawXClient client = RawXClient.connect(host, port)) {
                    int xtest = input ? client.queryExtension("XTEST") : -1;
                    int window = client.allocateId();
                    client.createWindow(window, client.root(), 100, 100, 120, 90);
                    client.selectInput(window, 0x20000);            // StructureNotifyMask
                    client.mapWindow(window);
                    client.flush();
                    if (xtest != -1) {
                        client.fakeMotion(xtest, 300, 200);
                        client.fakeButton(xtest, 1, true);
                        client.fakeButton(xtest, 1, false);
                    }
                    Thread.sleep(200);
                    client.unmapWindow(window);
                    client.destroyWindow(window);
                    client.flush();
                    Thread.sleep(200);
                    outcome = "ok";
                }
            } catch (Exception failed) {
                failure = failed.toString();
                outcome = "failed";
            }
        }
    }

    // ------------------------------------------------------------------
    // Requests
    // ------------------------------------------------------------------

    private static byte[] header(int opcode, int minor, int units) {
        byte[] request = new byte[units * 4];
        request[0] = (byte) opcode;
        request[1] = (byte) minor;
        RawXClient.writeShortAt(request, 2, units);
        return request;
    }

    private static byte[] header(int opcode, int minor, int units, int context) {
        byte[] request = header(opcode, minor, units);
        RawXClient.writeIntAt(request, 4, context);
        return request;
    }

    private static byte[] getContext(int opcode, int context) {
        return header(opcode, GET_CONTEXT, 2, context);
    }

    private static byte[] createContext(int opcode, int context, int elementHeader, int[] clients,
                                        byte[][] ranges) {
        return withClients(opcode, CREATE_CONTEXT, context, elementHeader, clients, ranges);
    }

    private static byte[] registerClients(int opcode, int context, int elementHeader, int[] clients,
                                          byte[][] ranges) {
        return withClients(opcode, REGISTER_CLIENTS, context, elementHeader, clients, ranges);
    }

    /**
     * The two requests that name clients and ranges, which have one layout between them: the context
     * and the element header, the counts, then every client spec and then every 24 byte range.
     */
    private static byte[] withClients(int opcode, int minor, int context, int elementHeader,
                                      int[] clients, byte[][] ranges) {
        int units = 5 + clients.length + ranges.length * 6;
        byte[] request = header(opcode, minor, units, context);
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

    private static byte[] unregisterClients(int opcode, int context, int[] clients) {
        byte[] request = header(opcode, UNREGISTER_CLIENTS, 3 + clients.length, context);
        RawXClient.writeIntAt(request, 8, clients.length);
        int at = 12;
        for (int client : clients) {
            RawXClient.writeIntAt(request, at, client);
            at += 4;
        }
        return request;
    }

    /**
     * Everything a recorder can ask for, within the bounds a reference enforces.
     *
     * <p>Two of those bounds are not guessable and were measured: a <b>delivered event</b> range may
     * not include 0 or 1, because those two codes are an error and a reply rather than events, and an
     * <b>extension</b> range's major codes start at 128 - 0..127 is the core protocol, and a range
     * that spans both is refused {@code BadValue}. A reference refuses the whole {@code CreateContext}
     * for either, which is how this was found.
     */
    private static byte[] broadRange() {
        byte[] range = new byte[24];
        core(range, 0, 127, 0, 127);
        extension(range, 4, 128, 255, 0, 0xFFFF);
        extension(range, 10, 128, 255, 0, 0xFFFF);
        events(range, 2, 127, 2, 6);
        errors(range, 0, 255);
        state(range, true, true);
        return range;
    }

    /** One core request - {@code MapWindow} - and the two client flags, and nothing else. */
    private static byte[] narrowRange() {
        byte[] range = new byte[24];
        core(range, 8, 8, 0, 0);
        state(range, true, true);
        return range;
    }

    /** The same range with only delivered events in it, to be sure a spec is read and kept per spec. */
    private static byte[] eventsOnly() {
        byte[] range = new byte[24];
        events(range, 2, 127, 0, 0);
        return range;
    }

    private static void core(byte[] range, int reqFirst, int reqLast, int repFirst, int repLast) {
        range[0] = (byte) reqFirst;
        range[1] = (byte) reqLast;
        range[2] = (byte) repFirst;
        range[3] = (byte) repLast;
    }

    /** One of the two extension bounds: at {@code at} is the major pair, then the minor pair. */
    private static void extension(byte[] range, int at, int majorFirst, int majorLast,
                                  int minorFirst, int minorLast) {
        range[at] = (byte) majorFirst;
        range[at + 1] = (byte) majorLast;
        RawXClient.writeShortAt(range, at + 2, minorFirst);
        RawXClient.writeShortAt(range, at + 4, minorLast);
    }

    private static void events(byte[] range, int first, int last, int deviceFirst, int deviceLast) {
        range[16] = (byte) first;
        range[17] = (byte) last;
        range[18] = (byte) deviceFirst;
        range[19] = (byte) deviceLast;
    }

    private static void errors(byte[] range, int first, int last) {
        range[20] = (byte) first;
        range[21] = (byte) last;
    }

    /** The two one-byte flags for a client arriving and a client leaving. */
    private static void state(byte[] range, boolean started, boolean died) {
        range[22] = (byte) (started ? 1 : 0);
        range[23] = (byte) (died ? 1 : 0);
    }

    // ------------------------------------------------------------------
    // Reading refusals and printing bytes
    // ------------------------------------------------------------------

    /**
     * An extension's error named by its offset from the extension's own error base, since the base
     * is the opcode the server assigned: {@code BadContext} is error 0 everywhere, error 128 on one
     * server and 137 on the other.
     */
    private static String readRefusalText(Exception refused, int firstError) {
        String message = refused.getMessage();
        java.util.regex.Matcher matcher =
                java.util.regex.Pattern.compile("refused request (\\d+): error (\\d+)")
                        .matcher(message == null ? "" : message);
        if (!matcher.find()) {
            return message;
        }
        int code = Integer.parseInt(matcher.group(2));
        return "seq=" + matcher.group(1) + " "
                + (code >= firstError && firstError > 0 ? "extension error " + (code - firstError)
                        : "core error " + code);
    }

    private static String hex(byte[] bytes) {
        StringBuilder text = new StringBuilder();
        for (byte value : bytes) {
            text.append(String.format("%02x", value));
        }
        return text.toString();
    }

    /** The first {@code max} bytes, since one datum's payload is a whole client setup - 7 KB of hex. */
    private static String hex(byte[] bytes, int max) {
        return bytes.length <= max ? hex(bytes)
                : hex(java.util.Arrays.copyOf(bytes, max)) + "...(" + bytes.length + ")";
    }

    /** Bytes {@code from}..{@code to} as hex, for a field that is a server's own value. */
    private static String hex(byte[] bytes, int from, int to) {
        return "0x" + hex(java.util.Arrays.copyOfRange(bytes, from, to));
    }

    private static int u(byte value) {
        return value & 0xFF;
    }

    private RecordProbe() {
    }
}
