package cn.oyzh.fx.xserver.event;

import com.github.moaxcp.x11.protocol.X11Output;
import com.github.moaxcp.x11.protocol.XEvent;
import cn.oyzh.fx.xserver.client.ClientConnection;

import java.io.IOException;

/**
 * An event assembled from raw wire fields.
 *
 * <p>Almost everything is built from the generated event classes, which carry the field layout.
 * This exists for the two cases they cannot express: events the server forwards on a client's
 * behalf, which arrive as opaque bytes in the sending client's byte order, and
 * {@code ConfigureRequestEvent}, whose {@code value_mask} field the generator only exposes through
 * private builder methods.
 *
 * <p>Bytes are written in the recipient's byte order at build time, so a built event can be handed
 * straight to {@link net} output without further translation.
 */
public final class RawEvent implements XEvent {

    public static final int SIZE = 32;

    private final byte[] bytes;
    private final short sequenceNumber;

    private RawEvent(byte[] bytes, short sequenceNumber) {
        this.bytes = bytes;
        this.sequenceNumber = sequenceNumber;
    }

    /** Starts building a core event with the given response code. */
    public static Builder builder(ClientConnection recipient, int eventCode) {
        return new Builder(recipient, eventCode);
    }

    /** Starts building from bytes that are already in {@code recipient}'s byte order. */
    public static RawEvent of(byte[] bytes, short sequenceNumber) {
        if (bytes.length != SIZE) {
            throw new IllegalArgumentException("an event is exactly " + SIZE + " bytes");
        }
        return new RawEvent(bytes, sequenceNumber);
    }

    @Override
    public byte getNumber() {
        return (byte) (bytes[0] & 0x7F);
    }

    @Override
    public byte getResponseCode() {
        return getNumber();
    }

    @Override
    public short getSequenceNumber() {
        return sequenceNumber;
    }

    @Override
    public byte getFirstEventOffset() {
        return 0;
    }

    @Override
    public void write(X11Output out) throws IOException {
        out.writeVoid(org.eclipse.collections.impl.factory.primitive.ByteLists.immutable
                .of(bytes));
    }

    @Override
    public int getSize() {
        return SIZE;
    }

    @Override
    public String getPluginName() {
        return "xproto";
    }

    /** Field by field builder that lays out a 32 byte event. */
    public static final class Builder {
        private final byte[] bytes = new byte[SIZE];
        private final boolean bigEndian;
        private final short sequenceNumber;

        private Builder(ClientConnection recipient, int eventCode) {
            this.bigEndian = recipient.isBigEndian();
            this.sequenceNumber = recipient.sequenceNumber();
            bytes[0] = (byte) eventCode;
            putCard16(2, sequenceNumber);
        }

        public Builder byteAt(int offset, int value) {
            bytes[offset] = (byte) value;
            return this;
        }

        public Builder card16(int offset, int value) {
            putCard16(offset, value);
            return this;
        }

        public Builder card32(int offset, int value) {
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
            return this;
        }

        /** Marks the event as synthetic, which is what {@code SendEvent} requires. */
        public Builder sentEvent() {
            bytes[0] = (byte) (bytes[0] | 0x80);
            return this;
        }

        private void putCard16(int offset, int value) {
            if (bigEndian) {
                bytes[offset] = (byte) (value >> 8);
                bytes[offset + 1] = (byte) value;
            } else {
                bytes[offset] = (byte) value;
                bytes[offset + 1] = (byte) (value >> 8);
            }
        }

        public RawEvent build() {
            return new RawEvent(bytes, sequenceNumber);
        }
    }
}
