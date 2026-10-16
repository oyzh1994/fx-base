package cn.oyzh.fx.xserver.event;

import com.github.moaxcp.x11.protocol.X11Output;
import com.github.moaxcp.x11.protocol.XEvent;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.event.XInputMasks;

import java.io.IOException;

/**
 * One XI2 <em>raw</em> event (XI 2.1), which is a third shape - not a device event and not a
 * crossing.
 *
 * <p>A raw event is what the device did, before the server did anything with it: no window, no grab,
 * no acceleration. That is why it has no root, event or child window at all, where every other XI2
 * event has three - and its layout was measured off Xvfb with {@code tools/XiProbe raw} on
 * 2026-10-04 rather than assembled from the field list:
 *
 * <pre>
 *   0  type 35 (GenericEvent)      16 detail          (a keycode or a button; 0 for a motion)
 *   1  the extension's opcode      20 source id       (the XTEST slave on a reference)
 *   2  sequence                    22 valuators_len   = 2 words, measured, for a key as well
 *   4  length = size/4 - 8         24 flags
 *   8  evtype                      28 a pad word
 *   10 device id                   32 the valuator mask, two words
 *   12 time                        [40 the raw values, one FP3232 per axis set in the mask]
 *                                  [56 the processed values, the same shape]
 * </pre>
 *
 * <p><b>A key or a button is 40 bytes and a motion 72</b> - measured, and the difference is the two
 * axes twice: the mask says 0 and 1, and two FP3232 pairs follow (raw, then processed). A motion
 * <em>without</em> them is what this server's ordinary device events do (see {@code Xi2Event.MOTION}
 * for why), but a raw event has no other content at all, so the values are the event.
 *
 * <p>The two length fields are the ones worth copying exactly: {@code valuators_len} is <b>two</b>
 * words on a reference even for the keyboard, whose device has no axes, and even for a button
 * press, where the mask is empty - so the words are a fixed size here rather than the axis count.
 * A client reads the mask with that length and then walks whatever the mask has set, so a client
 * that is given a shorter event reads the next message's bytes as its own.
 */
public final class Xi2RawEvent implements XEvent {

    /** The fixed part of a raw event, before the valuator mask. Measured. */
    private static final int FIXED = 32;

    /** Words of valuator mask a reference sends on every raw event: measured, and not the axis count. */
    private static final int VALUATOR_WORDS = 2;

    /** Bytes of the two axes: raw and processed, one {@code FP3232} each per set valuator. */
    private static final int MOTION_VALUES = 4 * 8;

    private static final int BUTTON_OR_KEY_SIZE = FIXED + VALUATOR_WORDS * 4;
    private static final int MOTION_SIZE = BUTTON_OR_KEY_SIZE + MOTION_VALUES;

    private final byte[] bytes;
    private final short sequenceNumber;
    private final boolean bigEndian;

    private Xi2RawEvent(byte[] bytes, short sequenceNumber, boolean bigEndian) {
        this.bytes = bytes;
        this.sequenceNumber = sequenceNumber;
        this.bigEndian = bigEndian;
    }

    /**
     * A {@code XI_RawMotion}: the pointer moved, with both axes in the mask and their values twice -
     * raw and processed.
     *
     * <p>This server's pointer does not accelerate, so the two pairs carry the same numbers: the raw
     * value is the position the host reported and the processed one is the same. A reference is no
     * help in telling the two apart either - Xvfb's pointer has no acceleration to apply (the
     * measurement is in the backlog's A47) - so the order here is the one Xlib reads them in, and
     * nothing else in this server can make them differ.
     */
    public static Xi2RawEvent motion(ClientConnection recipient, int extensionOpcode, int device,
                                     int source, int time, int x, int y) {
        Xi2RawEvent event = new Xi2RawEvent(new byte[MOTION_SIZE], recipient.sequenceNumber(),
                recipient.isBigEndian());
        event.header(extensionOpcode, XInputMasks.RAW_MOTION, device, source, time, 0);
        event.card32(32, 0b11);                     // both axes are in this event
        event.card32(36, 0);
        event.fixed(40, x);
        event.fixed(48, y);
        event.fixed(56, x);
        event.fixed(64, y);
        return event;
    }

    /** A {@code XI_RawButtonPress} or {@code XI_RawButtonRelease}: 40 bytes, with an empty mask. */
    public static Xi2RawEvent button(ClientConnection recipient, int extensionOpcode, int device,
                                     int source, int time, int button, boolean pressed) {
        return keyOrButton(recipient, extensionOpcode, device, source, time, button,
                pressed ? XInputMasks.RAW_BUTTON_PRESS : XInputMasks.RAW_BUTTON_RELEASE);
    }

    /** A {@code XI_RawKeyPress} or {@code XI_RawKeyRelease}, which has the same shape. */
    public static Xi2RawEvent key(ClientConnection recipient, int extensionOpcode, int device,
                                  int source, int time, int keycode, boolean pressed) {
        return keyOrButton(recipient, extensionOpcode, device, source, time, keycode,
                pressed ? XInputMasks.RAW_KEY_PRESS : XInputMasks.RAW_KEY_RELEASE);
    }

    private static Xi2RawEvent keyOrButton(ClientConnection recipient, int extensionOpcode,
                                           int device, int source, int time, int detail,
                                           int eventNumber) {
        Xi2RawEvent event = new Xi2RawEvent(new byte[BUTTON_OR_KEY_SIZE],
                recipient.sequenceNumber(), recipient.isBigEndian());
        event.header(extensionOpcode, eventNumber, device, source, time, detail);
        // The mask is two words of nought: a key or a button carries no axis, and a client is told
        // the size rather than left to guess that there is nothing behind it.
        return event;
    }

    private void header(int extensionOpcode, int eventNumber, int device, int source, int time,
                        int detail) {
        bytes[0] = 35;                             // GenericEvent
        bytes[1] = (byte) extensionOpcode;
        card16(2, sequenceNumber);
        card32(4, bytes.length / 4 - 8);           // length is the size in units, less eight
        card16(8, eventNumber);
        card16(10, device);
        card32(12, time);
        card32(16, detail);
        card16(20, source);
        card16(22, VALUATOR_WORDS);
        card32(24, 0);                             // flags: no key repeat to report
        card32(28, 0);                             // pad
    }

    /**
     * One axis value, which a raw event writes as an {@code FP3232}: <b>two</b> words - the integer
     * part in the first and the fraction in the second - not the {@code FP1616} a device event's
     * positions are. Measured: a reference's motion carries {@code 64000000 00000000} for x = 100,
     * and writing it as an {@code FP1616} (as this did first) makes the integral part 6553600.
     */
    private void fixed(int offset, int value) {
        card32(offset, value);
        card32(offset + 4, 0);
    }

    private void card16(int offset, int value) {
        if (bigEndian) {
            bytes[offset] = (byte) (value >> 8);
            bytes[offset + 1] = (byte) value;
        } else {
            bytes[offset] = (byte) value;
            bytes[offset + 1] = (byte) (value >> 8);
        }
    }

    private void card32(int offset, int value) {
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

    /** The bytes as they go on the wire, for a test that wants to read the layout back. */
    public byte[] wire() {
        return bytes.clone();
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
        out.writeVoid(org.eclipse.collections.impl.factory.primitive.ByteLists.immutable.of(bytes));
    }

    @Override
    public int getSize() {
        return bytes.length;
    }

    @Override
    public String getPluginName() {
        return "xinput";
    }

    @Override
    public String toString() {
        return "Xi2RawEvent[evtype=" + card16(8) + " device=" + card16(10) + "]";
    }

    private int card16(int offset) {
        return bigEndian ? ((bytes[offset] & 0xFF) << 8) | (bytes[offset + 1] & 0xFF)
                : (bytes[offset] & 0xFF) | ((bytes[offset + 1] & 0xFF) << 8);
    }
}
