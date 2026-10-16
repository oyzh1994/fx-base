package cn.oyzh.fx.xserver.extension;

import com.github.moaxcp.x11.protocol.X11Input;
import com.github.moaxcp.x11.protocol.X11Output;
import com.github.moaxcp.x11.protocol.XError;
import com.github.moaxcp.x11.protocol.XEvent;
import com.github.moaxcp.x11.protocol.XGenericEvent;
import com.github.moaxcp.x11.protocol.XProtocolPlugin;
import com.github.moaxcp.x11.protocol.XReply;
import com.github.moaxcp.x11.protocol.XRequest;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.extension.ExtensionRegistry;

import java.io.IOException;
import java.util.Optional;

/**
 * Generic Event Extension (XGE): the handshake for extension-defined events.
 *
 * <p>It is a version number and nothing else - one request, {@code QueryVersion}, and the reply
 * tells a client that generic events are a thing this server understands. **This server's generic
 * events already work**: XI2's device events and Present's completion notifications are
 * {@code GenericEvent}s at core event 35, and both are delivered. What was missing is not the
 * mechanism but the way a client *checks* for it - Xlib's cookie API and the toolkits that gate
 * their XI2 path behind it ask this extension first and fall back when it is absent, which is the
 * fallback F4 exists to remove.
 *
 * <p><b>The version is the server's own, measured</b> ({@code tools/XgeProbe} against Xvfb): a
 * reference answers 1.0 to a client asking for 1.0 and 1.0 to one asking for 0.0. The same rule as
 * Composite's and SYNC's {@code QueryVersion}, and the opposite of Present's, which echoes the
 * client's - which is why it is measured per extension rather than carried over.
 *
 * <p><b>There is no generated protocol module for XGE in the x11 library</b> - the extensions it
 * generates are the ones with a client-visible surface, and XGE is a handshake - so the request and
 * the reply are written out here. Both are fixed and tiny: the request is
 * {@code opcode, minor, length=2} followed by two CARD8 version fields and two bytes of padding, and
 * the reply is the standard 32 byte shape with two CARD16 version fields at offsets 8 and 10. This
 * is the one extension whose wire format is not backed by a generated class, so the offsets are
 * stated here and pinned by {@code XgeTest} against this server's own replies.
 *
 * <p>The extension defines no events and no errors, which is why it is registered with zero of
 * each - and why {@code QueryExtension} reports 0 for both, which is what a reference answers
 * (measured; see {@link ExtensionRegistry}).
 */
public final class XgeExtension implements Extension {

    /** {@code QueryVersion} is the extension's only request and its minor opcode is 0. */
    private static final int QUERY_VERSION = 0;

    /** Measured: a reference answers 1.0 to every client, whatever version it asked with. */
    private static final int MAJOR_VERSION = 1;
    private static final int MINOR_VERSION = 0;

    private final XgePlugin plugin = new XgePlugin();

    @Override
    public String name() {
        return "Generic Event Extension";
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

    /**
     * Decodes {@code QueryVersion} by hand, because there is no generated reader for XGE to delegate
     * to. The four header bytes are consumed the way every generated reader consumes them (the minor
     * opcode and the length, both already known to the dispatcher), so the stream is left exactly
     * where the next request begins.
     */
    @Override
    public XRequest decodeRequest(int minorOpcode, X11Input in) throws IOException {
        if ((minorOpcode & 0xFF) != QUERY_VERSION) {
            return null;
        }
        in.readPad(1);                              // the minor opcode, already known
        in.readCard16();                            // the request length, an eight byte request
        int clientMajor = in.readCard8();
        int clientMinor = in.readCard8();
        in.readPadAlign(8);
        return new QueryVersion(clientMajor, clientMinor);
    }

    @Override
    public void handleRequest(XServer server, ClientConnection client, XRequest request) {
        if (request instanceof QueryVersion) {
            client.sendReply(new VersionReply(client.sequenceNumber(), MAJOR_VERSION, MINOR_VERSION));
        } else {
            throw new IllegalArgumentException("unhandled XGE request " + request.getClass());
        }
    }

    /** {@code QueryVersion}: two version bytes, sent by a client that is checking for XGE. */
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
            return "ge";
        }

        @Override
        public void write(byte majorOpcode, X11Output out) throws IOException {
            out.writeCard8(majorOpcode);
            out.writeCard8((byte) QUERY_VERSION);
            out.writeCard16((short) 2);             // eight bytes, in four byte units
            out.writeCard8((byte) clientMajor);
            out.writeCard8((byte) clientMinor);
            out.writePadAlign(8);
        }
    }

    /** The reply: {@code major}, {@code minor} as CARD16s at offsets 8 and 10, and no extra data. */
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
            return "ge";
        }

        @Override
        public void write(X11Output out) throws IOException {
            out.writeCard8(getResponseCode());
            out.writePad(1);
            out.writeCard16(sequenceNumber);
            out.writeCard32(getLength());           // zero: every field fits in the fixed part
            out.writeCard16((short) major);
            out.writeCard16((short) minor);
            out.writePad(20);
        }
    }

    /**
     * The plugin the registry needs for its opcode, event and error ranges.
     *
     * <p>It is never asked to decode anything - {@link #decodeRequest} answers every request this
     * extension has - so its read methods refuse rather than pretend. The names and versions are the
     * protocol's own, which is what {@code QueryExtension} and a client's cache are about.
     */
    private static final class XgePlugin implements XProtocolPlugin {

        private byte majorOpcode;
        private byte firstEvent;
        private byte firstError;

        @Override
        public String getPluginName() {
            return "ge";
        }

        @Override
        public Optional<String> getExtensionXName() {
            return Optional.of("Generic Event Extension");
        }

        @Override
        public Optional<String> getExtensionName() {
            return Optional.of("GenericEventExtension");
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
            return request instanceof QueryVersion;
        }

        @Override
        public boolean supportedRequest(byte majorOpcode, byte minorOpcode) {
            return (minorOpcode & 0xFF) == QUERY_VERSION;
        }

        @Override
        public boolean supportedEvent(byte number) {
            return false;
        }

        @Override
        public boolean supportedError(byte code) {
            return false;
        }

        @Override
        public <T extends XRequest> T readRequest(byte majorOpcode, byte minorOpcode, X11Input in) {
            throw new IllegalArgumentException("XGE requests are decoded by the extension");
        }

        @Override
        public <T extends XEvent> T readEvent(byte number, boolean sentEvent, X11Input in) {
            throw new IllegalArgumentException("XGE defines no events");
        }

        @Override
        public <T extends XError> T readError(byte code, X11Input in) {
            throw new IllegalArgumentException("XGE defines no errors");
        }

        @Override
        public <T extends XGenericEvent> T readGenericEvent(byte firstEventOffset, boolean sentEvent,
                byte extension, short sequenceNumber, int length, short eventType, X11Input in) {
            throw new IllegalArgumentException("XGE defines no events");
        }
    }
}
