package cn.oyzh.fx.xserver.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * How big is the next message in a stream - the three answers that each cost a silent bug.
 *
 * <p>{@code XProxy} walks two byte streams it did not produce, and everything it knows about either is
 * how many bytes the next message is. Three of those answers were wrong, each in a way that is
 * invisible: the client simply stopped, or the log read as a desync that was not there, and the
 * months-old note in the kit ("EasyShell hangs through {@code XProxy}, the reason was never found")
 * was one of them. The arithmetic is two small functions now, and these are their cases - the very
 * bytes that were captured off the wire when each was found.
 */
class XProxyFramingTest {

    /** A request's bytes, from its opcode and length field: {@code head[0..3]}. */
    private static byte[] head(int... bytes) {
        byte[] head = new byte[8];
        for (int i = 0; i < bytes.length; i++) {
            head[i] = (byte) bytes[i];
        }
        return head;
    }

    /**
     * A request is not always eight bytes. A length field of one unit means the four bytes already
     * read are the whole request - {@code BIG-REQUESTS}' {@code Enable}, which libxcb sends on every
     * connection, and which a proxy assuming eight framed correctly and then desynchronised one
     * message later, hanging the client with nothing logged on either side.
     */
    @Test
    void aFourByteRequestIsFourBytes() {
        assertEquals(4, XProxy.requestLength(head(0x80, 0x00, 0x01, 0x00)),
                "`Enable`: opcode 128, one unit, and that is the lot");
    }

    @Test
    void anOrdinaryRequestIsItsLengthField() {
        // The by-name keymap load a real `setxkbmap` sends: length field 21 units = 84 bytes.
        assertEquals(84, XProxy.requestLength(head(0x87, 0x17, 0x15, 0x00)));
        assertEquals(8, XProxy.requestLength(head(0x82, 0x02, 0x02, 0x00)));
        assertEquals(12, XProxy.requestLength(head(0x10, 0x01, 0x03, 0x00)));
    }

    /**
     * A length field of zero means "the length is in the body" - the extended form - and **not** "this
     * message is empty". Which opcode it is, is the wrong test: 139 is also the ordinary {@code
     * Enable}, whose length field is three.
     */
    @Test
    void aZeroLengthFieldMeansReadFourMore() {
        assertEquals(0, XProxy.requestLength(head(0x8b, 0x00, 0x00, 0x00)),
                "zero: the caller reads the next word and asks again with it");
        assertEquals(12, XProxy.requestLength(head(0x8b, 0x00, 0x03, 0x00)),
                "and an `Enable` at that opcode is an ordinary three unit request, not an extended one");
    }

    /**
     * A reply is thirty two bytes plus its length, and **so is a generic event**: XI2's events are
     * generic events, GDK's whole input path is XI2, and forwarding the fixed part of one leaves the
     * rest in the stream - the client desynchronises at its first device event and stalls before
     * drawing. Measured as `type 35 length field 11 framed to 32` on the connection carrying a real
     * client's work.
     */
    @Test
    void aReplyAndAGenericEventCarryTheirLength() {
        assertEquals(32 + 29 * 4, XProxy.serverMessageLength(head(1, 3, 0x00, 0x00, 0x1d, 0x00, 0x00, 0x00)),
                "a reply: thirty two plus the length field");
        assertEquals(76, XProxy.serverMessageLength(head(35, 0x03, 0x00, 0x00, 11, 0x00, 0x00, 0x00)),
                "a generic event of 11 units: 76 bytes, not 32");
    }

    /** Everything else a server sends - events and errors - is a fixed thirty two bytes. */
    @Test
    void everyOtherServerMessageIsThirtyTwoBytes() {
        assertEquals(32, XProxy.serverMessageLength(head(12, 0, 0x00, 0x00, 0, 0, 0, 0)),
                "an Expose");
        assertEquals(32, XProxy.serverMessageLength(head(8, 0, 0x00, 0x00, 0, 0, 0, 0)),
                "a LeaveNotify - an event whose *number* is one of the five the generator gets wrong,"
                        + " but whose size is the ordinary thirty two");
        assertEquals(32, XProxy.serverMessageLength(head(0, 0, 0x00, 0x00, 0, 0, 0, 0)),
                "an error");
    }
}
