package cn.oyzh.fx.xserver.net;

import com.github.moaxcp.x11.protocol.X11BigEndianOutputStream;
import com.github.moaxcp.x11.protocol.X11LittleEndianOutputStream;
import com.github.moaxcp.x11.protocol.X11Output;
import com.github.moaxcp.x11.protocol.X11OutputStream;
import com.github.moaxcp.x11.protocol.XError;
import com.github.moaxcp.x11.protocol.XEvent;
import com.github.moaxcp.x11.protocol.XReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * Writes replies, events and errors to a client in that client's byte order.
 *
 * <p>Two quirks of the generated protocol classes are corrected here, centrally:
 *
 * <ul>
 *   <li>{@code XReply.getLength()} computes the reply length from the wrong base, so the 32-bit
 *       length field would be wrong in every reply. We rewrite it from the bytes actually
 *       produced.</li>
 *   <li>Replies shorter than the 32 byte wire header are serialized without their trailing
 *       padding. A real client reads those 32 bytes regardless, so we pad here.</li>
 * </ul>
 *
 * <p>Because both fixes need the finished byte image, everything is serialized into a small
 * scratch buffer first. Replies and events are at most a few kilobytes in practice, and the
 * copy is far cheaper than a desynchronized protocol stream.
 *
 * <p><b>Nothing here writes to the socket directly.</b> Every message goes onto a bounded queue that
 * a writer thread of its own drains, because the callers run while the server lock is held: a client
 * that stops reading fills its socket buffer, and a write into it blocks the thread that is holding
 * the lock - which is every other client's screen as well. With the queue, that client's own writer
 * thread is the only thing that waits, and when the queue is full the client is dropped instead of
 * the server being stopped. A real server does the same thing, and the bound is the whole of the
 * policy: a client that cannot keep up with what is being sent to it is a client that is gone.
 *
 * <p>Order is preserved because there is one queue per connection and one thread taking from it: what
 * is enqueued first is written first, whether it is a reply, an event or an error, and the writer
 * flushes after each message so a client waiting for one is not left waiting for the next.
 */
public final class ClientOutput {

    private static final Logger log = LoggerFactory.getLogger(ClientOutput.class);


    /** Fixed size of an event or error on the wire. */
    private static final int EVENT_SIZE = 32;
    /** Fixed size of a reply header; reply payload follows. */
    private static final int REPLY_HEADER_SIZE = 32;

    /**
     * How many messages may wait for a client that is not reading before it is dropped.
     *
     * <p>A thousand events is far more than a healthy client ever has outstanding - the socket buffer
     * holds several times that in bytes - so a queue this deep means the client has stopped reading
     * altogether, not that it is busy.
     */
    private static final int MAX_QUEUED_MESSAGES = 1024;

    /** How long {@link #close()} lets the writer finish the queue before the socket goes. */
    private static final int DRAIN_MILLIS = 100;

    private final OutputStream out;
    private final boolean bigEndian;
    private final ByteArrayOutputStream scratch = new ByteArrayOutputStream(256);

    /**
     * Messages waiting for the socket, and the thread that drains them.
     *
     * <p>The queue is what keeps a blocked write off the server's own thread: the callers enqueue and
     * return, and only this thread ever waits for the client.
     */
    private final java.util.concurrent.BlockingQueue<byte[]> queue =
            new java.util.concurrent.ArrayBlockingQueue<>(MAX_QUEUED_MESSAGES);

    private final Thread writer;

    private volatile boolean running = true;

    /**
     * True while the writer is inside a socket write.
     *
     * <p>{@link #close()} waits for the queue to empty, and a message the writer has already taken is
     * no longer in the queue - so without this the close could go ahead while that message was still
     * on its way out. It is a race, and it is not theoretical: the connection-setup refusal was lost
     * about half the time, which reads to the client as "the server hung up" instead of "no".
     */
    private volatile boolean writing;

    private volatile boolean broken;

    /**
     * Who is told about every message this connection sends, or null for nobody.
     *
     * <p>RECORD needs each message as a whole, with its bytes, and this is the only place all of them
     * pass through: replies, events, errors, structs and the raw images extensions format themselves.
     * The listener runs on the caller's thread, in the order the messages were queued, which is the
     * order a recorder has to see them in - and it is called <em>before</em> the queue, so what a
     * recorder is told is what will be sent even if the client has stopped reading.
     */
    private volatile Listener listener;

    /**
     * True when bytes have been buffered but not yet handed to the socket. A caller holding a
     * reply in here holds a client waiting for it, which is why the request loop asks.
     */
    private boolean pending;

    public ClientOutput(OutputStream out, boolean bigEndian) {
        this.out = out;
        this.bigEndian = bigEndian;
        this.writer = new Thread(this::drainToSocket, "xserver-client-writer");
        // A daemon, so a server that is shutting down is not held up by a client that is not reading.
        this.writer.setDaemon(true);
        this.writer.start();
    }

    public boolean isBigEndian() {
        return bigEndian;
    }

    /** Sets the listener told about every message. Null is nobody, which is the default. */
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    /** Told one finished message, in the order it was queued. */
    @FunctionalInterface
    public interface Listener {

        void message(byte[] bytes);
    }

    /** True once a write has failed; the connection is torn down by the owner. */
    public boolean isBroken() {
        return broken;
    }

    public synchronized void writeReply(XReply reply) throws IOException {
        byte[] bytes = serialize(out -> reply.write(out));
        if (bytes.length < REPLY_HEADER_SIZE) {
            // Clients always consume 32 bytes of reply header, so pad the short ones.
            byte[] padded = new byte[REPLY_HEADER_SIZE];
            System.arraycopy(bytes, 0, padded, 0, bytes.length);
            bytes = padded;
        }
        // length is "everything after the 32 byte header, in 4 byte units"
        int length = (bytes.length - REPLY_HEADER_SIZE) / 4;
        putCard32(bytes, 4, length);
        if (log.isTraceEnabled()) {
            log.trace("reply {}: {} bytes, length field {}", reply.getClass().getSimpleName(),
                    bytes.length, length);
        }
        write(bytes);
    }

    /**
     * Writes a reply whose length field is part of its own layout rather than the reply length.
     * Only the connection setup reply works this way: its bytes 4..8 hold the protocol version.
     */
    public synchronized void writeReplyRaw(XReply reply) throws IOException {
        write(serialize(out -> reply.write(out)));
    }

    /**
     * Writes a protocol struct verbatim. The connection setup reply is a struct rather than a
     * reply, and its length field counts only the variable part, so nothing is patched here.
     */
    public synchronized void writeStruct(com.github.moaxcp.x11.protocol.XStruct struct)
            throws IOException {
        write(serialize(struct::write));
    }

    /**
     * The bytes a struct would be sent as, without sending them.
     *
     * <p>RECORD wants the connection setup reply as the client-started element (F2), which is the one
     * message the server has to hand over rather than merely observe: the listener sees it go past,
     * but by then it is already queued for the socket.
     */
    public synchronized byte[] encode(com.github.moaxcp.x11.protocol.XStruct struct)
            throws IOException {
        return serialize(struct::write);
    }

    /**
     * Writes an event and pushes it out.
     *
     * <p>An event is often produced with no request in flight - a focus change arrives from the
     * toolkit's own thread - and a client waiting for that event has nothing else to send. Left in
     * the buffer it would sit there until some unrelated request happened to arrive, which for a
     * client blocked on the event is never.
     */
    public synchronized void writeEvent(XEvent event) throws IOException {
        byte[] bytes = serialize(out -> event.write(out));
        correctEventType(bytes, event);
        correctGenericEventLength(bytes, event);
        write(padTo(bytes, EVENT_SIZE));
        flush();
    }

    /**
     * Writes the length of a {@code GenericEvent}, which the generated writer leaves at zero.
     *
     * <p>A generic event - Present's completion notification is the one this server sends - carries
     * more than the thirty two bytes a reader assumes, and the only thing that says so is this field:
     * "the number of extra four byte units". Left at zero the client reads the fixed part and leaves
     * the rest in the stream, so **every event and reply after it is read from the wrong offset**.
     * Measured with {@code tools/PresentProbe}: the reference's completion arrived as forty bytes and
     * this server's as thirty two, with the same kind, mode and serial inside them - so the fields
     * looked right and the client then hung on the next request.
     *
     * <p>Patched here for the same reason {@link #correctEventType} is: the fault is in a dependency,
     * every event already passes through this method, and the real fix belongs in the generator.
     */
    private void correctGenericEventLength(byte[] bytes, XEvent event) {
        if (!(event instanceof com.github.moaxcp.x11.protocol.XGenericEvent) || bytes.length < 8) {
            return;
        }
        putCard32(bytes, 4, (bytes.length - EVENT_SIZE) / 4);
    }

    /**
     * The event numbers the generated classes get wrong, and what they should be.
     *
     * <p>{@code xproto.xml} declares these five with {@code <eventcopy ...>} - a release is a press
     * with another number, a leave is an enter, and so on - and the generator ignores the number on
     * a copy, so each generated class carries the number of the event it copies. On the wire a
     * release then looks like a second press and a leave looks like an enter, with the effect that a
     * client never sees a click completed (buttons never fire), gets every keystroke twice, and
     * tracks hover and focus backwards.
     */
    private static final Map<Class<?>, Byte> EVENT_NUMBERS = Map.of(
            com.github.moaxcp.x11.protocol.xproto.ButtonReleaseEvent.class, (byte) 5,
            com.github.moaxcp.x11.protocol.xproto.KeyReleaseEvent.class, (byte) 3,
            com.github.moaxcp.x11.protocol.xproto.LeaveNotifyEvent.class, (byte) 8,
            com.github.moaxcp.x11.protocol.xproto.FocusOutEvent.class, (byte) 10,
            com.github.moaxcp.x11.protocol.xproto.CirculateRequestEvent.class, (byte) 27);

    /**
     * Rewrites the type byte of an event the generated protocol numbers wrongly.
     *
     * <p>Done here because every generated class's output already passes through this method, and
     * because the fault is in a dependency: the real fix is for the generator to honour the number
     * on an {@code eventcopy}.
     */
    private static void correctEventType(byte[] bytes, XEvent event) {
        Byte number = EVENT_NUMBERS.get(event.getClass());
        if (number != null) {
            // Bit 7 is the synthetic flag, which the type byte also carries.
            bytes[0] = (byte) ((bytes[0] & 0x80) | number);
        }
    }

    /**
     * Writes an error. The wire format is fixed, so it is produced directly rather than going
     * through the generated per-code error classes.
     */
    public synchronized void writeError(byte errorCode, int badValue, short sequenceNumber,
                                        byte majorOpcode, short minorOpcode) throws IOException {
        byte[] bytes = new byte[EVENT_SIZE];
        bytes[0] = 0;                       // error
        bytes[1] = errorCode;
        putCard16(bytes, 2, sequenceNumber);
        putCard32(bytes, 4, badValue);
        putCard16(bytes, 8, minorOpcode);
        bytes[10] = majorOpcode;
        // bytes 11..31 stay zero
        write(bytes);
    }

    /** Writes an already encoded reply/event image, used by extensions that format their own. */
    public synchronized void writeRaw(byte[] bytes) throws IOException {
        write(bytes);
    }

    /** True when a reply, event or error is still queued for the writer. */
    public boolean hasPending() {
        return pending && !queue.isEmpty();
    }

    /**
     * Nothing to do: the writer thread flushes after every message it writes, so a client waiting
     * for a reply has it as soon as the queue reaches the socket.
     *
     * <p>This used to be where the bytes actually left, and it was the request loop's job to call it
     * after each request - a client waiting for a reply while the server waited for the rest of a
     * request is a deadlock neither side can break. The flush is now the writer's own, which is
     * better for the same reason the queue exists: it happens off the server's thread.
     */
    public void flush() {
    }

    private interface Writer {
        void write(X11Output out) throws IOException;
    }

    private byte[] serialize(Writer writer) throws IOException {
        scratch.reset();
        X11OutputStream encoded = bigEndian
                ? new X11BigEndianOutputStream(scratch)
                : new X11LittleEndianOutputStream(scratch);
        writer.write(encoded);
        encoded.flush();
        return scratch.toByteArray();
    }

    /**
     * Queues one message for the writer thread.
     *
     * <p>A full queue is the one failure this class reports rather than absorbs: the client has
     * stopped reading, and the caller - which is either the display tick or another client's request
     * - has to be released rather than blocked. The exception's message is what the log line says, so
     * it names the reason: a client that is gone and a client that is not reading look the same from
     * a closed socket, and they are not the same thing to whoever reads the log.
     */
    private void write(byte[] bytes) throws IOException {
        if (broken) {
            throw new IOException("connection is broken");
        }
        Listener observation = listener;
        if (observation != null) {
            observation.message(bytes);
        }
        if (!queue.offer(bytes)) {
            broken = true;
            throw new IOException("the client is not reading: " + queue.size()
                    + " messages are already queued for it");
        }
        pending = true;
    }

    /**
     * The writer: the only thread that ever waits for this client's socket.
     *
     * <p>A failed write ends the loop and closes the stream, which is what tells the connection's own
     * read loop that the client is gone - the class has no reference to the connection, and the side
     * that can report a disconnect is the side that owns it.
     */
    private void drainToSocket() {
        while (running) {
            byte[] bytes;
            try {
                // Blocking, with no poll interval: a message must leave as soon as it is queued, and
                // a poll timeout is latency on every message - the first version of this waited up to
                // two hundred milliseconds per reply, which for a client waiting on one is a pause
                // the user can see. The thread is stopped by interrupting it.
                bytes = queue.take();
            } catch (InterruptedException stopped) {
                Thread.currentThread().interrupt();
                return;
            }
            writing = true;
            try {
                out.write(bytes);
                out.flush();
            } catch (IOException gone) {
                broken = true;
                running = false;
                closeQuietly();
                return;
            } finally {
                writing = false;
            }
        }
    }

    /**
     * Lets what is already queued reach the client, then stops the writer and closes the stream.
     *
     * <p>The drain is not politeness: a refusal of the connection setup, a final error and the
     * farewell a graceful shutdown sends are all "the last thing this client will hear", and closing
     * over them turns "the server said no" into "the server hung up" - which is exactly what the
     * authentication tests caught when this closed immediately (a client that offered a wrong cookie
     * read an empty stream instead of the refusal).
     *
     * <p>Bounded, because the caller may be holding the server lock and the client may be the one that
     * stopped reading: a write that never finishes must not become a close that never returns.
     */
    public void close() {
        long deadline = System.nanoTime() + DRAIN_MILLIS * 1_000_000L;
        while ((!queue.isEmpty() || writing) && !broken && System.nanoTime() < deadline) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException stopped) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        running = false;
        writer.interrupt();
        closeQuietly();
    }

    private void closeQuietly() {
        try {
            out.close();
        } catch (IOException ignored) {
            // Closing is best effort, and the caller is on its way out either way.
        }
    }

    private static byte[] padTo(byte[] bytes, int size) {
        if (bytes.length >= size) {
            return bytes;
        }
        byte[] padded = new byte[size];
        System.arraycopy(bytes, 0, padded, 0, bytes.length);
        return padded;
    }

    private void putCard16(byte[] bytes, int offset, short value) {
        if (bigEndian) {
            bytes[offset] = (byte) (value >> 8);
            bytes[offset + 1] = (byte) value;
        } else {
            bytes[offset] = (byte) value;
            bytes[offset + 1] = (byte) (value >> 8);
        }
    }

    private void putCard32(byte[] bytes, int offset, int value) {
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

    /** Convenience for {@link XError} instances produced by extension code. */
    public synchronized void writeError(XError error) throws IOException {
        byte[] bytes = serialize(out -> error.write(out));
        write(padTo(bytes, EVENT_SIZE));
    }
}
