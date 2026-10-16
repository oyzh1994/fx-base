package cn.oyzh.fx.xserver.extension;

import com.github.moaxcp.x11.protocol.X11Input;
import com.github.moaxcp.x11.protocol.XError;
import com.github.moaxcp.x11.protocol.XEvent;
import com.github.moaxcp.x11.protocol.XGenericEvent;
import com.github.moaxcp.x11.protocol.XProtocolPlugin;
import com.github.moaxcp.x11.protocol.XReply;
import com.github.moaxcp.x11.protocol.XRequest;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.client.ProtocolException;
import cn.oyzh.fx.xserver.extension.ProtocolRecorder;
import cn.oyzh.common.log.JulLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RECORD: a client asks to be told what another client did.
 *
 * <p>The extension is a context - which clients it covers, which kinds of message inside them - and
 * then a stream: {@code EnableContext} makes the server answer that request with one "reply" per
 * recorded element, forever, until the recorder stops it. A recorder is how a session is replayed,
 * how a test is checked against a recording, and how a debugging tool sees inside a client it does
 * not own.
 *
 * <p><b>Everything here was measured against Xvfb</b> with {@code tools/RecordProbe}, because the
 * shapes a recorder consumes are not written down anywhere a reader can rely on: what a request
 * stores, what a range means, which stamps appear in front of an element and when the stream is
 * flushed are all decisions of the server, and the ones below are the reference's.
 *
 * <ul>
 *   <li><b>The version is the server's own, 1.13</b>, whatever the client asked for - the same rule
 *       as SYNC and the Generic Event Extension, and the opposite of Present's echo.</li>
 *   <li><b>A context's client specs are resolved when the request arrives</b>:
 *       {@code CurrentClients} becomes the clients connected now, each named by its resource base,
 *       {@code FutureClients} becomes the literal 2 - a client that has not connected yet - and
 *       {@code AllClients} becomes both. A spec for one client is that client's base. A spec that is
 *       given twice is stored once, and re-registering a client replaces its ranges.</li>
 *   <li><b>A datum's header is 32 bytes</b>: type 1, category, the sequence number of the
 *       {@code EnableContext} that started the stream, the length in four byte units <em>after</em>
 *       that header, the element header byte, the {@code clientSwapped} flag, the recorded client's
 *       resource base, the server time, the recorded sequence number, and eight bytes of padding.
 *       The measured offsets are {@code elementHeader} 8, {@code clientSwapped} 9,
 *       {@code clientResource} 12, {@code serverTime} 16, {@code recordedSequenceNumber} 20 and the
 *       data from 32.</li>
 *   <li><b>The categories</b> are FromServer 0, FromClient 1, ClientStarted 2, ClientDied 3,
 *       StartOfData 4 and EndOfData 5. A recording opens with an empty StartOfData; a client's
 *       arrival is a ClientStarted whose data is that client's whole setup reply (measured: 3652
 *       bytes, the length field saying 913 units); and a departure is a ClientDied whose data is the
 *       client's last sequence number, four bytes in the recorded client's byte order - measured with
 *       a range that covered one request of the nine the client sent, and the ninth arrived.
 *       <b>A reference never sends EndOfData</b>: not on {@code DisableContext}, not on
 *       {@code FreeContext}, not on a second {@code EnableContext} - measured three ways, so this
 *       server does not send one either.</li>
 *   <li><b>The stamps are per item, and the element header says which</b>: a FromServer item carries
 *       a four byte time iff {@code FromServerTime} is set, and a FromClient item carries a four byte
 *       time iff {@code FromClientTime} is set plus a four byte sequence iff {@code FromClientSequence}
 *       is set. ClientStarted and ClientDied carry none - their client and sequence are in the datum's
 *       own header.</li>
 *   <li><b>The data is a batch</b>: consecutive items from the same side are concatenated into one
 *       datum, which is what makes a burst of requests arrive as one reply. What ends the batch is an
 *       item from the other side, and the measured rule is that a FromServer item is flushed on its
 *       own - the reference wrote each delivered event and each reply as a single element datum while
 *       three consecutive requests shared one.</li>
 *   <li><b>A datum is attributed to the request being executed when it is written</b>: a reply
 *       carries the asking client's base and sequence, an event with no request behind it carries
 *       zero. That is what the header's {@code clientResource} and {@code recordedSequenceNumber}
 *       are, and the probe compares them.</li>
 *   <li><b>The connection setup reply is recorded once</b>, as the ClientStarted element and not as
 *       a FromServer message: measured, a reference's first datum after StartOfData is the new
 *       client's arrival, so this server attaches its recorder hook after the setup reply has been
 *       written.</li>
 * </ul>
 *
 * <p><b>This server has no device event stage to record</b>, which is the one measured difference in
 * the stream: a reference records each input event as a FromClient "device" element attributed to
 * the client that caused it (Xvfb wrote three for a pointer gesture: a motion, a press and a
 * release), because its input is processed on its own loop. This server derives its XI2 events from
 * the delivered core events, so there is nothing to record that is not already a delivered message -
 * and the input-driven events that do appear are attributed to the request that produced them, which
 * is this server's honest answer for its own execution model. See {@code docs/backlog.md} §9 F2.
 *
 * <p><b>There is no generated protocol module for RECORD in the x11 library</b>, so the eight
 * requests and the {@code GetContext} reply are decoded and encoded here, the way XGE and X-Resource
 * are. The ranges are 24 bytes of first/last pairs; the one this server's own probe registers is
 * {@code 0..127 / 0..127 / 128..255:0..65535 twice / events 2..127 / device 2..6 / errors 0..255 /
 * started 1 / died 1}.
 */
public final class RecordExtension implements Extension, ProtocolRecorder {


    // Minor opcodes, in the protocol's own order.
    private static final int QUERY_VERSION = 0;
    private static final int CREATE_CONTEXT = 1;
    private static final int REGISTER_CLIENTS = 2;
    private static final int UNREGISTER_CLIENTS = 3;
    private static final int GET_CONTEXT = 4;
    private static final int ENABLE_CONTEXT = 5;
    private static final int DISABLE_CONTEXT = 6;
    private static final int FREE_CONTEXT = 7;

    /** The client spec values, and the literal a context stores for a client that has not arrived. */
    private static final int CURRENT_CLIENTS = 1;
    private static final int FUTURE_CLIENTS = 2;
    private static final int ALL_CLIENTS = 3;

    /** Element header bits: which stamps a recorded item carries. */
    private static final int FROM_SERVER_TIME = 0x01;
    private static final int FROM_CLIENT_TIME = 0x02;
    private static final int FROM_CLIENT_SEQUENCE = 0x04;

    /** Datum categories. */
    private static final int FROM_SERVER = 0;
    private static final int FROM_CLIENT = 1;
    private static final int CLIENT_STARTED = 2;
    private static final int CLIENT_DIED = 3;
    private static final int START_OF_DATA = 4;

    /** The extension's one error, {@code BadContext}. */
    private static final int BAD_CONTEXT = 0;

    /** Measured: a reference answers its own version to every client, whatever it asked for. */
    private static final int MAJOR_VERSION = 1;
    private static final int MINOR_VERSION = 13;

    /** A datum's fixed header, before the data. */
    private static final int DATUM_HEADER = 32;

    /** One range: ten first/last pairs and the two client flags, twenty four bytes on the wire. */
    private record Range(int coreRequestFirst, int coreRequestLast, int coreReplyFirst,
                         int coreReplyLast, int extRequestMajor, int extRequestMajorLast,
                         int extRequestFirst, int extRequestLast, int extReplyMajor,
                         int extReplyMajorLast, int extReplyFirst, int extReplyLast,
                         int eventFirst, int eventLast, int deviceFirst, int deviceLast,
                         int errorFirst, int errorLast, boolean clientStarted, boolean clientDied) {

        /**
         * One 24 byte range, as a client writes it.
         *
         * <p><b>The extension slots are six bytes, not five</b>: a pair of major codes <em>and</em> a
         * pair of minor ones, so an extension range covers a rectangle of major and minor opcodes. The
         * core slots are the same shape with one byte each, and the last six bytes are the delivered
         * events, the device events, the errors and the two flags for a client arriving and leaving.
         */
        static Range read(X11Input in) throws IOException {
            int coreRequestFirst = in.readCard8();
            int coreRequestLast = in.readCard8();
            int coreReplyFirst = in.readCard8();
            int coreReplyLast = in.readCard8();
            int extRequestMajor = in.readCard8();
            int extRequestMajorLast = in.readCard8();
            int extRequestFirst = in.readCard16();
            int extRequestLast = in.readCard16();
            int extReplyMajor = in.readCard8();
            int extReplyMajorLast = in.readCard8();
            int extReplyFirst = in.readCard16();
            int extReplyLast = in.readCard16();
            int eventFirst = in.readCard8();
            int eventLast = in.readCard8();
            int deviceFirst = in.readCard8();
            int deviceLast = in.readCard8();
            int errorFirst = in.readCard8();
            int errorLast = in.readCard8();
            boolean clientStarted = in.readBool();
            boolean clientDied = in.readBool();
            return new Range(coreRequestFirst, coreRequestLast, coreReplyFirst, coreReplyLast,
                    extRequestMajor, extRequestMajorLast, extRequestFirst, extRequestLast,
                    extReplyMajor, extReplyMajorLast, extReplyFirst, extReplyLast, eventFirst,
                    eventLast, deviceFirst, deviceLast, errorFirst, errorLast, clientStarted,
                    clientDied);
        }

        void write(byte[] bytes, int at, boolean bigEndian) {
            bytes[at] = (byte) coreRequestFirst;
            bytes[at + 1] = (byte) coreRequestLast;
            bytes[at + 2] = (byte) coreReplyFirst;
            bytes[at + 3] = (byte) coreReplyLast;
            bytes[at + 4] = (byte) extRequestMajor;
            bytes[at + 5] = (byte) extRequestMajorLast;
            putCard16(bytes, at + 6, extRequestFirst, bigEndian);
            putCard16(bytes, at + 8, extRequestLast, bigEndian);
            bytes[at + 10] = (byte) extReplyMajor;
            bytes[at + 11] = (byte) extReplyMajorLast;
            putCard16(bytes, at + 12, extReplyFirst, bigEndian);
            putCard16(bytes, at + 14, extReplyLast, bigEndian);
            bytes[at + 16] = (byte) eventFirst;
            bytes[at + 17] = (byte) eventLast;
            bytes[at + 18] = (byte) deviceFirst;
            bytes[at + 19] = (byte) deviceLast;
            bytes[at + 20] = (byte) errorFirst;
            bytes[at + 21] = (byte) errorLast;
            bytes[at + 22] = (byte) (clientStarted ? 1 : 0);
            bytes[at + 23] = (byte) (clientDied ? 1 : 0);
        }

        /** Whether a request/reply/event/error of this shape is inside the range. */
        boolean allows(int category, int opcode, int minorOpcode, int code) {
            return switch (category) {
                case FROM_CLIENT -> opcode < 128
                        ? between(opcode, coreRequestFirst, coreRequestLast)
                        : between(opcode, extRequestMajor, extRequestMajorLast)
                                && between(minorOpcode, extRequestFirst, extRequestLast);
                case FROM_SERVER -> switch (code) {
                    // A reply, an event and an error are told apart by the first byte of the message,
                    // which is the only thing the recorder has to go on: 1, 0 and anything else.
                    case MESSAGE_REPLY -> opcode < 128
                            ? between(opcode, coreReplyFirst, coreReplyLast)
                            : between(opcode, extReplyMajor, extReplyMajorLast)
                                    && between(minorOpcode, extReplyFirst, extReplyLast);
                    case MESSAGE_ERROR -> between(opcode, errorFirst, errorLast);
                    default -> between(opcode, eventFirst, eventLast);
                };
                default -> true;
            };
        }

        private static boolean between(int value, int first, int last) {
            return value >= first && value <= last;
        }
    }

    /** What a FromServer message is, for the range check: its first byte, read as a shape. */
    private static final int MESSAGE_REPLY = 0;
    private static final int MESSAGE_ERROR = 1;
    private static final int MESSAGE_EVENT = 2;

    /** One recorded item, waiting for the datum that will carry it. */
    /**
     * One recorded element waiting to go out.
     *
     * <p>{@code client} is the client the element is attributed to - the one whose request produced it,
     * or null when nothing did - and {@code swapped} is whether the element's bytes are in the other
     * byte order from the recorder's, which for a server message is the order of the client it is
     * <em>going to</em> and not the one that asked for it.
     */
    private record Item(int category, byte[] bytes, int time, ClientConnection client,
                        short sequence, int swapped) {
    }

    /** A client the context records, with the ranges that decide what inside it is recorded. */
    private record ClientSpec(int resource, List<Range> ranges) {
    }

    /**
     * One recording: everything a client asked for, and the batch it is currently filling.
     *
     * <p>Guarded by its own monitor rather than the server's, because the writers are the request
     * thread and whichever thread produces an event and neither of them expects to wait - and because
     * a context is small and its critical sections are a list append.
     */
    private static final class Context {

        private final ClientConnection recorder;
        private final int elementHeader;
        private final Map<Integer, ClientSpec> specs = new LinkedHashMap<>();
        private final List<Item> pending = new ArrayList<>();
        private volatile boolean enabled;
        private short enabledSequence;

        Context(ClientConnection recorder, int elementHeader) {
            this.recorder = recorder;
            this.elementHeader = elementHeader;
        }

        /** The recorded client's spec: its own if it has one, otherwise the one for future clients. */
        synchronized ClientSpec specFor(ClientConnection client) {
            ClientSpec future = null;
            for (ClientSpec spec : specs.values()) {
                if (spec.resource == client.resourceIdBase()) {
                    return spec;
                }
                if (spec.resource == FUTURE_CLIENTS) {
                    future = spec;
                }
            }
            return future;
        }

        synchronized boolean isEnabled() {
            return enabled;
        }

        synchronized void add(Item item) {
            pending.add(item);
        }

        /** Takes the batch to be sent, or null when there is nothing waiting. */
        synchronized List<Item> takePending() {
            if (pending.isEmpty()) {
                return null;
            }
            List<Item> taken = List.copyOf(pending);
            pending.clear();
            return taken;
        }

        synchronized void clearPending() {
            pending.clear();
        }
    }

    private final RecordPlugin plugin = new RecordPlugin();
    private final Map<Integer, Context> contexts = new ConcurrentHashMap<>();
    /** The clients whose start has been announced, so a departure is only reported for one. */
    private final Map<ClientConnection, Boolean> started = new ConcurrentHashMap<>();

    private XServer server;

    @Override
    public void init(XServer server) {
        this.server = server;
        server.recorder(this);
    }

    @Override
    public String name() {
        return "RECORD";
    }

    @Override
    public byte majorOpcode() {
        return plugin.getMajorOpcode();
    }

    @Override
    public byte firstEvent() {
        return plugin.getFirstEvent();
    }

    @Override
    public byte firstError() {
        return plugin.getFirstError();
    }

    @Override
    public XProtocolPlugin plugin() {
        return plugin;
    }

    // ------------------------------------------------------------------
    // Requests
    // ------------------------------------------------------------------

    /**
     * Every RECORD request is decoded here: the library generates no module for this extension, and
     * the eight requests are small enough that the offsets are clearer than a generated reader would
     * be. Each reader consumes the four header bytes the dispatcher has already looked at, so the
     * stream is left where the next request begins.
     */
    @Override
    public XRequest decodeRequest(int minorOpcode, X11Input in) throws IOException {
        int minor = minorOpcode & 0xFF;
        if (minor == QUERY_VERSION) {
            in.readPad(1);
            in.readCard16();
            return new QueryVersion(in.readCard16(), in.readCard16());
        }
        if (minor == CREATE_CONTEXT || minor == REGISTER_CLIENTS) {
            in.readPad(1);
            in.readCard16();
            int context = in.readCard32();
            int elementHeader = in.readCard8();
            in.readPad(3);
            int numClientSpecs = in.readCard32();
            int numRanges = in.readCard32();
            return new RegisterClients(minor == CREATE_CONTEXT, context, elementHeader,
                    readSpecs(in, numClientSpecs, numRanges));
        }
        if (minor == UNREGISTER_CLIENTS) {
            in.readPad(1);
            in.readCard16();
            int context = in.readCard32();
            int numClientSpecs = in.readCard32();
            return new UnregisterClients(context, readSpecs(in, numClientSpecs, 0));
        }
        if (minor == GET_CONTEXT || minor == ENABLE_CONTEXT || minor == DISABLE_CONTEXT
                || minor == FREE_CONTEXT) {
            in.readPad(1);
            in.readCard16();
            int context = in.readCard32();
            return new ContextRequest(minor, context);
        }
        throw ProtocolException.unimplemented("RECORD request " + minor);
    }

    /**
     * The client specs of a request, each with the request's whole range list.
     *
     * <p><b>The specs come first on the wire and the ranges follow them</b>, which is the one thing
     * about this layout that cannot be guessed: the reply carries the two in the other order (each
     * spec immediately followed by its own ranges), and reading the request that way turns the
     * client's spec into a range. Measured with {@code tools/RecordProbe} against Xvfb: a context
     * created over {@code CurrentClients} came back with seven specs, each echoing the range the
     * probe sent - which it could only do if the specs were read from where the probe wrote them.
     */
    private static List<ClientSpec> readSpecs(X11Input in, int numClientSpecs, int numRanges)
            throws IOException {
        List<Integer> resources = new ArrayList<>(numClientSpecs);
        for (int i = 0; i < numClientSpecs; i++) {
            resources.add(in.readCard32());
        }
        List<Range> ranges = new ArrayList<>(numRanges);
        for (int i = 0; i < numRanges; i++) {
            ranges.add(Range.read(in));
        }
        List<ClientSpec> specs = new ArrayList<>(numClientSpecs);
        for (int resource : resources) {
            specs.add(new ClientSpec(resource, List.copyOf(ranges)));
        }
        return specs;
    }

    @Override
    public void handleRequest(XServer server, ClientConnection client, XRequest request) {
        if (request instanceof QueryVersion) {
            client.sendReply(new VersionReply(client.sequenceNumber(), MAJOR_VERSION, MINOR_VERSION));
            return;
        }
        if (request instanceof RegisterClients register) {
            if (register.create()) {
                createContext(server, client, register);
            } else {
                registerClients(server, client, register);
            }
            return;
        }
        if (request instanceof UnregisterClients unregister) {
            Context context = context(client, unregister.context());
            synchronized (context) {
                for (ClientSpec spec : resolve(client, unregister.specs())) {
                    context.specs.remove(spec.resource());
                }
            }
            return;
        }
        if (request instanceof ContextRequest contextRequest) {
            switch (contextRequest.minor()) {
                case GET_CONTEXT -> {
                    Context context = context(client, contextRequest.context());
                    // Encoded here rather than with a generated reply class: the reply carries a
                    // variable list the generated writers do not produce for this extension.
                    try {
                        client.output().writeRaw(getContextReply(client, context));
                    } catch (IOException gone) {
                        client.close();
                    }
                }
                case ENABLE_CONTEXT -> enable(server, client, contextRequest.context());
                case DISABLE_CONTEXT -> {
                    Context context = context(client, contextRequest.context());
                    disable(context);
                }
                case FREE_CONTEXT -> {
                    Context context = context(client, contextRequest.context());
                    disable(context);
                    contexts.remove(contextRequest.context());
                }
                default -> throw ProtocolException.unimplemented("RECORD request "
                        + contextRequest.minor());
            }
            return;
        }
        throw new IllegalArgumentException("unhandled RECORD request " + request.getClass());
    }

    private void createContext(XServer server, ClientConnection client, RegisterClients request) {
        int id = request.context();
        if (contexts.containsKey(id)) {
            // A context is a resource, so an id that is already in use is the core error for that and
            // not this extension's own - BadContext is for a context that is not there.
            throw ProtocolException.badIdChoice(id);
        }
        Context context = new Context(client, request.elementHeader());
        for (ClientSpec spec : resolve(client, request.specs())) {
            context.specs.put(spec.resource(), spec);
        }
        contexts.put(id, context);
    }

    private void registerClients(XServer server, ClientConnection client, RegisterClients request) {
        Context context = context(client, request.context());
        synchronized (context) {
            for (ClientSpec spec : resolve(client, request.specs())) {
                // Re-registering a client replaces the ranges it was recorded with, which is how a
                // recorder narrows or widens what it hears from someone already in the context.
                context.specs.put(spec.resource(), spec);
            }
        }
    }

    /**
     * Turns the client specs of a request into the specs a context stores.
     *
     * <p>The three selectors are resolved here rather than at the time a message arrives: a context
     * made over {@code CurrentClients} holds the clients that were connected when it was made, and
     * one made over {@code FutureClients} holds the literal 2, which matches every client that
     * connects later. That is the measured shape of a reference's {@code GetContext} reply.
     */
    private List<ClientSpec> resolve(ClientConnection recorder, List<ClientSpec> specs) {
        List<ClientSpec> resolved = new ArrayList<>();
        for (ClientSpec spec : specs) {
            switch (spec.resource()) {
                case CURRENT_CLIENTS -> {
                    for (ClientConnection each : server.clients()) {
                        resolved.add(new ClientSpec(each.resourceIdBase(), spec.ranges()));
                    }
                }
                case FUTURE_CLIENTS -> resolved.add(new ClientSpec(FUTURE_CLIENTS, spec.ranges()));
                case ALL_CLIENTS -> {
                    for (ClientConnection each : server.clients()) {
                        resolved.add(new ClientSpec(each.resourceIdBase(), spec.ranges()));
                    }
                    resolved.add(new ClientSpec(FUTURE_CLIENTS, spec.ranges()));
                }
                default -> resolved.add(spec);
            }
        }
        return resolved;
    }

    /** A context this client owns, or the refusal the protocol defines for one it does not. */
    private Context context(ClientConnection client, int id) {
        Context context = contexts.get(id);
        if (context == null || context.recorder != client) {
            throw new ProtocolException((byte) (firstError() + BAD_CONTEXT), id,
                    "no context 0x" + Integer.toHexString(id) + " for this client");
        }
        return context;
    }

    // ------------------------------------------------------------------
    // Enabling and disabling
    // ------------------------------------------------------------------

    /**
     * Starts a stream: the request is answered by an empty StartOfData and then by one datum per
     * recorded element until the recorder stops it.
     *
     * <p>A second {@code EnableContext} on a context that is already enabled does nothing - measured:
     * the reference kept streaming and sent no second StartOfData, and no EndOfData for the enable it
     * did not perform.
     */
    private void enable(XServer server, ClientConnection client, int id) {
        Context context = context(client, id);
        synchronized (context) {
            if (context.enabled) {
                return;
            }
            context.enabled = true;
            context.enabledSequence = client.sequenceNumber();
        }
        sendDatum(context, START_OF_DATA, 0, server.events().time(), 0, 0, new byte[0]);
    }

    /**
     * Ends a stream. The batch still waiting goes out first, and then nothing: a reference sends no
     * EndOfData for either way of ending one, so neither does this server.
     */
    private void disable(Context context) {
        synchronized (context) {
            if (!context.enabled) {
                context.clearPending();
                return;
            }
            context.enabled = false;
        }
        flush(context);
        synchronized (context) {
            context.clearPending();
        }
    }

    // ------------------------------------------------------------------
    // ProtocolRecorder: what the core hands over
    // ------------------------------------------------------------------

    @Override
    public void clientRequest(ClientConnection client, byte[] request, short sequenceNumber) {
        if (contexts.isEmpty()) {
            return;
        }
        int time = server.events().time();
        for (Context context : contexts.values()) {
            if (!records(context, client)) {
                continue;
            }
            int opcode = request[0] & 0xFF;
            int minor = request.length > 1 ? request[1] & 0xFF : 0;
            if (!allows(context, client, FROM_CLIENT, opcode, minor, 0)) {
                continue;
            }
            context.add(new Item(FROM_CLIENT, request, time, client, sequenceNumber,
                    swapped(context, client)));
        }
    }

    @Override
    public void serverMessage(ClientConnection recipient, byte[] message,
                              ClientConnection answeringClient, short answeringSequence) {
        if (contexts.isEmpty()) {
            return;
        }
        int time = server.events().time();
        int first = message[0] & 0xFF;
        int shape = first == 0 ? MESSAGE_ERROR : first == 1 ? MESSAGE_REPLY : MESSAGE_EVENT;
        // A reply's range is the range of the request that asked for it, so the answering client's
        // opcodes are the key; an event is keyed on its own type byte, which is the message's first.
        boolean fromResponse = shape != MESSAGE_EVENT && answeringClient != null;
        int opcode = shape == MESSAGE_EVENT || !fromResponse ? first
                : answeringClient.currentOpcode() & 0xFF;
        int minor = shape == MESSAGE_EVENT || !fromResponse ? 0
                : answeringClient.currentMinorOpcode() & 0xFF;
        // A record of the recorder's own connection would carry the datums this extension is sending
        // it, which is the one message a recorder cannot be told about without a loop.
        for (Context context : contexts.values()) {
            if (!records(context, recipient)) {
                continue;
            }
            if (!allows(context, recipient, FROM_SERVER, opcode, minor, shape)) {
                continue;
            }
            // A server message closes the batch in front of it, and is then a datum of its own: a
            // reference writes every reply, event and error singly, and only consecutive client
            // messages share one datum. It goes through the same queue as a request so that the
            // element header's stamp is written in front of it - measured: an event's datum carries
            // four bytes of the server's time with FromServerTime set, and the first version wrote the
            // message bare, which the probe read back as an unparsed payload.
            flush(context);
            context.add(new Item(FROM_SERVER, message, time, answeringClient, answeringSequence,
                    swapped(context, recipient)));
            flush(context);
        }
    }

    @Override
    public void clientStarted(ClientConnection client, byte[] setupReply) {
        started.put(client, Boolean.TRUE);
        if (contexts.isEmpty()) {
            return;
        }
        int time = server.events().time();
        for (Context context : contexts.values()) {
            ClientSpec spec = records(context, client) ? context.specFor(client) : null;
            if (spec == null || !declared(spec, true)) {
                continue;
            }
            flush(context);
            sendDatum(context, CLIENT_STARTED, client.resourceIdBase(), time, 0,
                    swapped(context, client), setupReply);
        }
    }

    @Override
    public void clientDied(ClientConnection client) {
        if (started.remove(client) == null || contexts.isEmpty()) {
            return;
        }
        int time = server.events().time();
        for (Context context : contexts.values()) {
            ClientSpec spec = records(context, client) ? context.specFor(client) : null;
            if (spec == null || !declared(spec, false)) {
                continue;
            }
            // A client whose departure is recorded has its last requests in front of it, so the
            // batch goes out first.
            flush(context);
            // The element's data is the client's last sequence number, four bytes in the recorded
            // client's own byte order - measured: a context whose range covered one request of nine
            // still got the ninth, so it is the sequence and not the number of items recorded.
            byte[] last = new byte[4];
            putCard32(last, 0, client.sequenceNumber(), client.isBigEndian());
            sendDatum(context, CLIENT_DIED, client.resourceIdBase(), time,
                    client.sequenceNumber(), swapped(context, client), last);
        }
    }

    /** Whether any of a client's ranges asks for started ({@code true}) or died elements. */
    private static boolean declared(ClientSpec spec, boolean started) {
        for (Range range : spec.ranges()) {
            if (started ? range.clientStarted() : range.clientDied()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void clientDisconnected(XServer server, ClientConnection client) {
        // The client-started marker is deliberately left alone: this runs while the departing
        // client's resources are destroyed, which is *before* the departure itself is recorded - and
        // the departure is the element the marker is for. Clearing it here silenced every ClientDied,
        // which is what the test caught (XServer.removeClient calls destroyClientResources first).
        contexts.values().removeIf(context -> context.recorder == client);
    }

    /** Whether this context records this client at all, and is streaming. */
    private boolean records(Context context, ClientConnection client) {
        if (!context.isEnabled() || context.recorder == client) {
            return false;
        }
        ClientSpec spec = context.specFor(client);
        return spec != null && !spec.ranges().isEmpty();
    }

    /** Whether any of the client's ranges covers this message. */
    private boolean allows(Context context, ClientConnection client, int category, int opcode,
                           int minorOpcode, int code) {
        ClientSpec spec = context.specFor(client);
        if (spec == null) {
            return false;
        }
        for (Range range : spec.ranges()) {
            if (range.allows(category, opcode, minorOpcode, code)) {
                return true;
            }
        }
        return false;
    }

    private int swapped(Context context, ClientConnection client) {
        return client.isBigEndian() == context.recorder.isBigEndian() ? 0 : 1;
    }

    // ------------------------------------------------------------------
    // The stream
    // ------------------------------------------------------------------

    /**
     * Puts the items waiting for this context into one datum.
     *
     * <p>Called after a FromServer item - which is a datum of its own, so the batch in front of it is
     * closed first - and when the context is disabled or freed.
     */
    private void flush(Context context) {
        List<Item> items = context.takePending();
        if (items == null) {
            return;
        }
        Item first = items.get(0);
        int size = 0;
        for (Item item : items) {
            size += stamps(context, item) + item.bytes().length;
        }
        byte[] data = new byte[size];
        int at = 0;
        for (Item item : items) {
            at = writeStamps(context, item, data, at);
            System.arraycopy(item.bytes(), 0, data, at, item.bytes().length);
            at += item.bytes().length;
        }
        sendDatum(context, first.category(),
                first.client() == null ? 0 : first.client().resourceIdBase(), first.time(),
                first.sequence(), first.swapped(), data);
    }

    /** How many stamp bytes one item carries, which is what the element header asked for. */
    private int stamps(Context context, Item item) {
        if (item.category() == FROM_SERVER) {
            return (context.elementHeader & FROM_SERVER_TIME) == 0 ? 0 : 4;
        }
        int bytes = (context.elementHeader & FROM_CLIENT_TIME) == 0 ? 0 : 4;
        return bytes + ((context.elementHeader & FROM_CLIENT_SEQUENCE) == 0 ? 0 : 4);
    }

    private int writeStamps(Context context, Item item, byte[] data, int at) {
        boolean big = context.recorder.isBigEndian();
        if (item.category() == FROM_SERVER) {
            if ((context.elementHeader & FROM_SERVER_TIME) != 0) {
                putCard32(data, at, item.time(), big);
                at += 4;
            }
            return at;
        }
        if ((context.elementHeader & FROM_CLIENT_TIME) != 0) {
            putCard32(data, at, item.time(), big);
            at += 4;
        }
        if ((context.elementHeader & FROM_CLIENT_SEQUENCE) != 0) {
            putCard32(data, at, item.sequence(), big);
            at += 4;
        }
        return at;
    }

    /** Queues one datum, which is a reply-shaped message on the recorder's own connection. */
    private void sendDatum(Context context, int category, int clientBase, int serverTime,
                           int recordedSequence, int clientSwapped, byte[] data) {
        ClientConnection recorder = context.recorder;
        if (recorder.isClosed() || recorder.isShuttingDown()) {
            return;
        }
        boolean big = recorder.isBigEndian();
        byte[] datum = new byte[DATUM_HEADER + data.length];
        datum[0] = 1;                               // a reply
        datum[1] = (byte) category;
        putCard16(datum, 2, context.enabledSequence, big);
        putCard32(datum, 4, data.length / 4, big);  // the length counts the data, not the header
        datum[8] = (byte) context.elementHeader;
        datum[9] = (byte) clientSwapped;
        putCard32(datum, 12, clientBase, big);
        putCard32(datum, 16, serverTime, big);
        putCard32(datum, 20, recordedSequence, big);
        System.arraycopy(data, 0, datum, DATUM_HEADER, data.length);
        try {
            recorder.output().writeRaw(datum);
        } catch (IOException gone) {
            // The recorder stopped reading: its own connection is being torn down, and that is not
            // the business of the client whose message this was.
            JulLog.debug("RECORD datum for {} could not be written: {}", recorder.peer(),
                    gone.getMessage());
        }
    }

    /**
     * A {@code GetContext} reply: what the context holds, which is the client's own request echoed.
     */
    private byte[] getContextReply(ClientConnection client, Context context) {
        List<ClientSpec> specs;
        synchronized (context) {
            specs = List.copyOf(context.specs.values());
        }
        int size = DATUM_HEADER;
        for (ClientSpec spec : specs) {
            size += 8 + spec.ranges().size() * 24;
        }
        byte[] bytes = new byte[size];
        boolean big = client.isBigEndian();
        bytes[0] = 1;
        bytes[1] = (byte) (context.enabled ? 1 : 0);
        putCard16(bytes, 2, client.sequenceNumber(), big);
        putCard32(bytes, 4, (size - DATUM_HEADER) / 4, big);
        bytes[8] = (byte) context.elementHeader;
        putCard32(bytes, 12, specs.size(), big);
        int at = DATUM_HEADER;
        for (ClientSpec spec : specs) {
            putCard32(bytes, at, spec.resource(), big);
            putCard32(bytes, at + 4, spec.ranges().size(), big);
            at += 8;
            for (Range range : spec.ranges()) {
                range.write(bytes, at, big);
                at += 24;
            }
        }
        return bytes;
    }

    private static void putCard16(byte[] bytes, int offset, int value, boolean bigEndian) {
        if (bigEndian) {
            bytes[offset] = (byte) (value >> 8);
            bytes[offset + 1] = (byte) value;
        } else {
            bytes[offset] = (byte) value;
            bytes[offset + 1] = (byte) (value >> 8);
        }
    }

    private static void putCard32(byte[] bytes, int offset, int value, boolean bigEndian) {
        if (bigEndian) {
            bytes[offset] = (byte) (value >> 24);
            bytes[offset + 1] = (byte) (value >> 16);
            bytes[offset + 2] = (byte) (value >> 8);
            bytes[offset + 3] = (byte) value;
        } else {
            bytes[offset] = (byte) value;
            bytes[offset + 1] = (byte) (value >> 8);
            bytes[offset + 2] = (byte) (value >> 16);
            bytes[offset + 3] = (byte) (value >> 24);
        }
    }

    // ------------------------------------------------------------------
    // Requests, as records
    // ------------------------------------------------------------------

    /** {@code QueryVersion}: the version the client offers, which the reply does not echo. */
    private record QueryVersion(int clientMajor, int clientMinor) implements XRequest {

        @Override
        public byte getOpCode() {
            return (byte) QUERY_VERSION;
        }

        @Override
        public int getSize() {
            return 8;
        }

        @Override
        public String getPluginName() {
            return "record";
        }

        @Override
        public void write(byte majorOpcode, com.github.moaxcp.x11.protocol.X11Output out)
                throws IOException {
            out.writeCard8(majorOpcode);
            out.writeCard8((byte) QUERY_VERSION);
            out.writeCard16((short) 2);
            out.writeCard16((short) clientMajor);
            out.writeCard16((short) clientMinor);
        }
    }

    /**
     * {@code CreateContext} and {@code RegisterClients}, which are the same request once decoded:
     * a context, an element header, and a list of client specs that share a list of ranges.
     */
    private record RegisterClients(boolean create, int context, int elementHeader,
                                   List<ClientSpec> specs) implements XRequest {

        @Override
        public byte getOpCode() {
            return (byte) (create ? CREATE_CONTEXT : REGISTER_CLIENTS);
        }

        @Override
        public int getSize() {
            return 0;                               // only written by a client, never by this server
        }

        @Override
        public String getPluginName() {
            return "record";
        }

        @Override
        public void write(byte majorOpcode, com.github.moaxcp.x11.protocol.X11Output out)
                throws IOException {
            throw new UnsupportedOperationException("RECORD requests are client to server");
        }
    }

    /** {@code UnregisterClients}: the specs to drop, and no ranges - the request has none. */
    private record UnregisterClients(int context, List<ClientSpec> specs) implements XRequest {

        @Override
        public byte getOpCode() {
            return (byte) UNREGISTER_CLIENTS;
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public String getPluginName() {
            return "record";
        }

        @Override
        public void write(byte majorOpcode, com.github.moaxcp.x11.protocol.X11Output out)
                throws IOException {
            throw new UnsupportedOperationException("RECORD requests are client to server");
        }
    }

    /** {@code GetContext}, {@code EnableContext}, {@code DisableContext}, {@code FreeContext}. */
    private record ContextRequest(int minor, int context) implements XRequest {

        @Override
        public byte getOpCode() {
            return (byte) minor;
        }

        @Override
        public int getSize() {
            return 8;
        }

        @Override
        public String getPluginName() {
            return "record";
        }

        @Override
        public void write(byte majorOpcode, com.github.moaxcp.x11.protocol.X11Output out)
                throws IOException {
            throw new UnsupportedOperationException("RECORD requests are client to server");
        }
    }

    /** The reply to {@code QueryVersion}: two CARD16s at offsets 8 and 10. */
    private record VersionReply(short sequenceNumber, int major, int minor) implements XReply {

        @Override
        public short getSequenceNumber() {
            return sequenceNumber;
        }

        @Override
        public int getSize() {
            return 32;
        }

        @Override
        public String getPluginName() {
            return "record";
        }

        @Override
        public void write(com.github.moaxcp.x11.protocol.X11Output out) throws IOException {
            out.writeCard8(getResponseCode());
            out.writePad(1);
            out.writeCard16(sequenceNumber);
            out.writeCard32(getLength());
            out.writeCard16((short) major);
            out.writeCard16((short) minor);
            out.writePad(20);
        }
    }

    /**
     * The plugin the registry needs for an opcode and an error range.
     *
     * <p>RECORD defines no events, so its event range is empty and {@code QueryExtension} reports
     * zero for it - measured: a reference reports {@code firstEvent=0 firstError=<base>}, the shape
     * every requests-and-one-error extension has.
     */
    private static final class RecordPlugin implements XProtocolPlugin {

        private byte majorOpcode;
        private byte firstEvent;
        private byte firstError;

        @Override
        public String getPluginName() {
            return "record";
        }

        @Override
        public Optional<String> getExtensionXName() {
            return Optional.of("RECORD");
        }

        @Override
        public Optional<String> getExtensionName() {
            return Optional.of("Record");
        }

        @Override
        public Optional<Boolean> getExtensionMultiword() {
            return Optional.of(true);
        }

        @Override
        public Optional<Byte> getMajorVersion() {
            return Optional.of((byte) MAJOR_VERSION);
        }

        @Override
        public Optional<Byte> getMinorVersion() {
            return Optional.of((byte) MINOR_VERSION);
        }

        @Override
        public byte getMajorOpcode() {
            return majorOpcode;
        }

        @Override
        public void setMajorOpcode(byte majorOpcode) {
            this.majorOpcode = majorOpcode;
        }

        @Override
        public byte getFirstEvent() {
            return firstEvent;
        }

        @Override
        public void setFirstEvent(byte firstEvent) {
            this.firstEvent = firstEvent;
        }

        @Override
        public byte getFirstError() {
            return firstError;
        }

        @Override
        public void setFirstError(byte firstError) {
            this.firstError = firstError;
        }

        @Override
        public boolean supportedRequest(XRequest request) {
            return true;
        }

        @Override
        public boolean supportedRequest(byte majorOpcode, byte minorOpcode) {
            return (minorOpcode & 0xFF) <= FREE_CONTEXT;
        }

        @Override
        public boolean supportedEvent(byte number) {
            return false;
        }

        @Override
        public boolean supportedError(byte code) {
            return (code & 0xFF) == BAD_CONTEXT;
        }

        @Override
        public <T extends XRequest> T readRequest(byte majorOpcode, byte minorOpcode, X11Input in) {
            throw new IllegalArgumentException("RECORD requests are decoded by the extension");
        }

        @Override
        public <T extends XEvent> T readEvent(byte number, boolean sentEvent, X11Input in) {
            throw new IllegalArgumentException("RECORD defines no events");
        }

        @Override
        public <T extends XError> T readError(byte code, X11Input in) {
            throw new IllegalArgumentException("RECORD errors are read by the extension");
        }

        @Override
        public <T extends XGenericEvent> T readGenericEvent(byte firstEventOffset, boolean sentEvent,
                byte extension, short sequenceNumber, int length, short eventType, X11Input in) {
            throw new IllegalArgumentException("RECORD defines no events");
        }
    }
}
