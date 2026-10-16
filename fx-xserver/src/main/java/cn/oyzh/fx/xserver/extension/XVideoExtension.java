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
import cn.oyzh.fx.xserver.client.ProtocolException;

import java.io.IOException;
import java.util.Optional;

/**
 * XVideo (Xv): the extension a video player hands frames to, and the one this server has the least
 * business implementing.
 *
 * <p>A player asks which <b>adaptors</b> the server has - pieces of hardware that can scale and colour
 * convert a video frame - and then which ports and encodings each one offers, before it puts a frame
 * into a window. There is no video hardware behind this server's host window and there never can be:
 * the picture arrives as pixels through the same window as everything else. So the honest
 * implementation is the one a reference server with no such hardware gives, and that is exactly what
 * this is:
 *
 * <ul>
 *   <li>{@code QueryAdaptors} answers <b>zero</b> adaptors. Measured: Xvfb, which advertises XVideo,
 *       answers zero for the same request - a player that gets that falls back to its non-Xv path,
 *       which every player has, and it is the answer that costs it nothing.</li>
 *   <li>{@code QueryExtension} answers the extension's own version, <b>2.2</b>, which is the
 *       reference's answer rather than the X protocol's 11.0.</li>
 *   <li>Every request that names a port is refused with the extension's own {@code BadPort}. There is
 *       no port, so no port id can be valid, and a client that guesses one is told why rather than
 *       answered with nothing. The order of the checks is the reference's: {@code PutVideo},
 *       {@code PutStill}, {@code GetVideo}, {@code GetStill}, {@code PutImage} and
 *       {@code ShmPutImage} resolve their drawable and GC <em>first</em>, so a GC of {@code None} is
 *       {@code BadGContext} there and here, and only a well-formed request reaches {@code BadPort}.</li>
 *   <li>{@code SelectVideoNotify} names a <b>drawable</b> rather than a port, and a reference accepts
 *       it even with no adaptors - the mask is recorded, and nothing ever fires. That is what this
 *       does too, which is what a client that arms it and waits sees on both servers: silence.</li>
 * </ul>
 *
 * <p><b>What is deliberately not built</b>, and why the entry in {@code docs/backlog.md} calls this
 * the weakest case of the seven: a software scaler behind {@code PutImage}. It would be a *better*
 * answer than the reference's for a client that reaches a port - but no client can reach one here,
 * because this server would be the only one advertising an adaptor it does not have. A player would
 * then take a video path that ends in a scaler nobody asked for, and the fallback it would have taken
 * (drawing the frames itself) is what it does today. The extension exists here so that "is XVideo
 * there?" is answered truthfully rather than not at all; the frames themselves are the player's.
 *
 * <p>The wire format comes from {@code X11/extensions/Xvproto.h} - there is no generated protocol
 * module for Xv in the x11 library - and every answer above was measured with {@code tools/XvProbe}
 * against a reference.
 */
public final class XVideoExtension implements Extension {

    /** Xv's requests, in the protocol's order. */
    private static final int QUERY_EXTENSION = 0;
    private static final int QUERY_ADAPTORS = 1;
    private static final int QUERY_ENCODINGS = 2;
    private static final int GRAB_PORT = 3;
    private static final int UNGRAB_PORT = 4;
    private static final int PUT_VIDEO = 5;
    private static final int PUT_STILL = 6;
    private static final int GET_VIDEO = 7;
    private static final int GET_STILL = 8;
    private static final int STOP_VIDEO = 9;
    private static final int SELECT_VIDEO_NOTIFY = 10;
    private static final int SELECT_PORT_NOTIFY = 11;
    private static final int QUERY_BEST_SIZE = 12;
    private static final int SET_PORT_ATTRIBUTE = 13;
    private static final int GET_PORT_ATTRIBUTE = 14;
    private static final int QUERY_PORT_ATTRIBUTES = 15;
    private static final int LIST_IMAGE_FORMATS = 16;
    private static final int QUERY_IMAGE_ATTRIBUTES = 17;
    private static final int PUT_IMAGE = 18;
    private static final int SHM_PUT_IMAGE = 19;

    /** The extension's own version: the reference answers this, not the X protocol's 11.0. */
    private static final int MAJOR_VERSION = 2;
    private static final int MINOR_VERSION = 2;

    /** This extension's errors, at {@code firstError + n}: the reference's own three. */
    private static final int BAD_PORT = 0;
    private static final int BAD_ENCODING = 1;
    private static final int BAD_CONTROL = 2;

    private final XvPlugin plugin = new XvPlugin();

    private XServer server;

    @Override
    public void init(XServer server) {
        this.server = server;
    }

    @Override
    public String name() {
        return "XVideo";
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
    // Decoding
    // ------------------------------------------------------------------

    /**
     * Every Xv request, decoded here, in the layouts {@code Xvproto.h} gives: the library generates no
     * module for this extension.
     */
    @Override
    public XRequest decodeRequest(int minorOpcode, X11Input in) throws IOException {
        int minor = minorOpcode & 0xFF;
        if (minor == QUERY_EXTENSION) {
            in.readPad(1);
            in.readCard16();
            return new Simple(minor);
        }
        if (minor == QUERY_ADAPTORS) {
            in.readPad(1);
            in.readCard16();
            return new DrawableRequest(minor, in.readCard32());
        }
        if (minor == QUERY_ENCODINGS || minor == QUERY_PORT_ATTRIBUTES
                || minor == LIST_IMAGE_FORMATS) {
            in.readPad(1);
            in.readCard16();
            return new PortRequest(minor, in.readCard32());
        }
        if (minor == GRAB_PORT || minor == UNGRAB_PORT) {
            in.readPad(1);
            in.readCard16();
            int port = in.readCard32();
            in.readCard32();                            // the time
            return new PortRequest(minor, port);
        }
        if (minor == PUT_VIDEO || minor == PUT_STILL || minor == GET_VIDEO || minor == GET_STILL) {
            in.readPad(1);
            in.readCard16();
            int port = in.readCard32();
            int drawable = in.readCard32();
            int gc = in.readCard32();
            in.readPadAlign(32);
            return new VideoRequest(minor, port, drawable, gc);
        }
        if (minor == STOP_VIDEO) {
            in.readPad(1);
            in.readCard16();
            int port = in.readCard32();
            int drawable = in.readCard32();
            return new VideoRequest(minor, port, drawable, 0);
        }
        if (minor == SELECT_VIDEO_NOTIFY) {
            in.readPad(1);
            in.readCard16();
            int drawable = in.readCard32();
            int onoff = in.readCard8();
            in.readPadAlign(12);
            return new SelectVideoNotify(drawable, onoff != 0);
        }
        if (minor == SELECT_PORT_NOTIFY) {
            in.readPad(1);
            in.readCard16();
            int port = in.readCard32();
            in.readCard8();                             // onoff, which a bad port never reaches
            in.readPadAlign(12);
            return new PortRequest(minor, port);
        }
        if (minor == QUERY_BEST_SIZE) {
            in.readPad(1);
            in.readCard16();
            int port = in.readCard32();
            in.readPadAlign(20);
            return new PortRequest(minor, port);
        }
        if (minor == SET_PORT_ATTRIBUTE) {
            in.readPad(1);
            in.readCard16();
            int port = in.readCard32();
            in.readPadAlign(16);
            return new PortRequest(minor, port);
        }
        if (minor == GET_PORT_ATTRIBUTE) {
            in.readPad(1);
            in.readCard16();
            int port = in.readCard32();
            in.readPadAlign(12);
            return new PortRequest(minor, port);
        }
        if (minor == QUERY_IMAGE_ATTRIBUTES) {
            in.readPad(1);
            in.readCard16();
            int port = in.readCard32();
            in.readPadAlign(16);
            return new PortRequest(minor, port);
        }
        if (minor == PUT_IMAGE || minor == SHM_PUT_IMAGE) {
            in.readPad(1);
            in.readCard16();
            int port = in.readCard32();
            int drawable = in.readCard32();
            int gc = in.readCard32();
            in.readPadAlign(minor == PUT_IMAGE ? 40 : 52);
            return new VideoRequest(minor, port, drawable, gc);
        }
        throw new ProtocolException(ProtocolException.REQUEST, minor,
                "unknown XVideo request " + minor);
    }

    // ------------------------------------------------------------------
    // Behaviour
    // ------------------------------------------------------------------

    @Override
    public void handleRequest(XServer server, ClientConnection client, XRequest request) {
        if (request instanceof Simple) {
            client.sendReply(new ExtensionReply(client.sequenceNumber(), MAJOR_VERSION,
                    MINOR_VERSION));
            return;
        }
        if (request instanceof DrawableRequest adaptors) {
            // A drawable the client cannot name is the core error, which is what a reference answers
            // - and what it does *not* answer is anything about adaptors, because there are none.
            server.resources().requireDrawable(client, adaptors.drawable());
            client.sendReply(new AdaptorsReply(client.sequenceNumber()));
            return;
        }
        if (request instanceof SelectVideoNotify notify) {
            // A drawable rather than a port: a reference accepts this with no adaptors at all, and
            // nothing ever fires. Nothing fires here either, and the mask is not kept, because there
            // is no video to report on.
            server.resources().requireDrawable(client, notify.drawable());
            return;
        }
        if (request instanceof VideoRequest video) {
            // The reference's order: the drawable and the GC are resolved before the port, so a
            // client that sends a GC of None is told that rather than BadPort. StopVideo carries no
            // GC at all - it is a port and a drawable - and checking one there answered BadGContext
            // where the reference answers BadPort (measured: the probe caught exactly this).
            server.resources().requireDrawable(client, video.drawable());
            if (video.minorOpcode() != STOP_VIDEO) {
                server.resources().require(client, video.gc(),
                        cn.oyzh.fx.xserver.resource.XResource.CLASS_GC, false);
            }
            throw badPort(video.port());
        }
        if (request instanceof PortRequest port) {
            throw badPort(port.port());
        }
        throw new IllegalArgumentException("unhandled XVideo request " + request.getClass());
    }

    /**
     * {@code BadPort}, this extension's own error: no adaptor means no port id can be valid, and a
     * client that names one is told which error is which - a core {@code BadValue} would send it
     * looking for a decoding mistake instead of a missing adaptor.
     */
    private ProtocolException badPort(int port) {
        return new ProtocolException((byte) (firstError() + BAD_PORT), port,
                "no XVideo port 0x" + Integer.toHexString(port)
                        + ": this server has no video adaptor");
    }

    // ------------------------------------------------------------------
    // The requests, as objects
    // ------------------------------------------------------------------

    /** {@code QueryExtension}: nothing to carry but the version it is asked for, which is ignored. */
    private record Simple(int minorOpcode) implements XRequest {

        @Override
        public byte getOpCode() {
            return (byte) minorOpcode;
        }

        @Override
        public int getSize() {
            return 4;
        }

        @Override
        public String getPluginName() {
            return "xv";
        }

        @Override
        public void write(byte majorOpcode, X11Output out) throws IOException {
            out.writeCard8(majorOpcode);
            out.writeCard8((byte) minorOpcode);
            out.writeCard16((short) 1);
        }
    }

    /** A request whose only field is the drawable to ask about or to arm. */
    private record DrawableRequest(int minorOpcode, int drawable) implements XRequest {

        @Override
        public byte getOpCode() {
            return (byte) minorOpcode;
        }

        @Override
        public int getSize() {
            return 8;
        }

        @Override
        public String getPluginName() {
            return "xv";
        }

        @Override
        public void write(byte majorOpcode, X11Output out) throws IOException {
            out.writeCard8(majorOpcode);
            out.writeCard8((byte) minorOpcode);
            out.writeCard16((short) 2);
            out.writeCard32(drawable);
        }
    }

    /** A request whose only field is the port it names - and there are no ports. */
    private record PortRequest(int minorOpcode, int port) implements XRequest {

        @Override
        public byte getOpCode() {
            return (byte) minorOpcode;
        }

        @Override
        public int getSize() {
            return 8;
        }

        @Override
        public String getPluginName() {
            return "xv";
        }

        @Override
        public void write(byte majorOpcode, X11Output out) throws IOException {
            out.writeCard8(majorOpcode);
            out.writeCard8((byte) minorOpcode);
            out.writeCard16((short) 2);
            out.writeCard32(port);
        }
    }

    /** {@code SelectVideoNotify}: a drawable and whether to arm or disarm it. */
    private record SelectVideoNotify(int drawable, boolean on) implements XRequest {

        @Override
        public byte getOpCode() {
            return (byte) SELECT_VIDEO_NOTIFY;
        }

        @Override
        public int getSize() {
            return 12;
        }

        @Override
        public String getPluginName() {
            return "xv";
        }

        @Override
        public void write(byte majorOpcode, X11Output out) throws IOException {
            out.writeCard8(majorOpcode);
            out.writeCard8((byte) SELECT_VIDEO_NOTIFY);
            out.writeCard16((short) 3);
            out.writeCard32(drawable);
            out.writeCard8((byte) (on ? 1 : 0));
            out.writePadAlign(12);
        }
    }

    /** One of the requests that carries a port, a drawable and a GC. */
    private record VideoRequest(int minorOpcode, int port, int drawable, int gc)
            implements XRequest {

        @Override
        public byte getOpCode() {
            return (byte) minorOpcode;
        }

        @Override
        public int getSize() {
            return 32;
        }

        @Override
        public String getPluginName() {
            return "xv";
        }

        @Override
        public void write(byte majorOpcode, X11Output out) throws IOException {
            out.writeCard8(majorOpcode);
            out.writeCard8((byte) minorOpcode);
            out.writeCard16((short) 8);
            out.writeCard32(port);
            out.writeCard32(drawable);
            out.writeCard32(gc);
            out.writePadAlign(32);
        }
    }

    // ------------------------------------------------------------------
    // The replies
    // ------------------------------------------------------------------

    /** {@code QueryExtension}'s reply: the extension's own version, at offsets 8 and 10. */
    private record ExtensionReply(short sequenceNumber, int major, int minor) implements XReply {

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
            return "xv";
        }

        @Override
        public void write(X11Output out) throws IOException {
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
     * {@code QueryAdaptors}' reply, which is empty: zero adaptors, and therefore no names, no ports
     * and no formats behind it.
     */
    private record AdaptorsReply(short sequenceNumber) implements XReply {

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
            return "xv";
        }

        @Override
        public void write(X11Output out) throws IOException {
            out.writeCard8(getResponseCode());
            out.writePad(1);
            out.writeCard16(sequenceNumber);
            out.writeCard32(getLength());
            out.writeCard32(0);                          // the number of adaptors
            out.writePad(20);
        }
    }

    /**
     * The plugin the registry needs for this extension's opcode, event and error ranges.
     *
     * <p>Xv defines two events ({@code VideoNotify} and {@code PortNotify}) and three errors
     * ({@code BadPort}, {@code BadEncoding}, {@code BadControl}) - the two and the three of a real
     * implementation, which is what a client's cache is told about. Neither event is ever sent here:
     * there is no video to notify about.
     */
    private static final class XvPlugin implements XProtocolPlugin {

        private byte majorOpcode;
        private byte firstEvent;
        private byte firstError;

        @Override
        public String getPluginName() {
            return "xv";
        }

        @Override
        public Optional<String> getExtensionXName() {
            return Optional.of("XVideo");
        }

        @Override
        public Optional<String> getExtensionName() {
            return Optional.of("XVideo");
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
            int minor = minorOpcode & 0xFF;
            return minor >= QUERY_EXTENSION && minor <= SHM_PUT_IMAGE;
        }

        @Override
        public boolean supportedEvent(byte number) {
            return (number & 0xFF) >= firstEvent && (number & 0xFF) < firstEvent + 2;
        }

        @Override
        public boolean supportedError(byte code) {
            return (code & 0xFF) >= firstError + BAD_PORT
                    && (code & 0xFF) <= firstError + BAD_CONTROL;
        }

        @Override
        public <T extends XRequest> T readRequest(byte majorOpcode, byte minorOpcode, X11Input in) {
            throw new IllegalArgumentException("XVideo requests are decoded by the extension");
        }

        @Override
        public <T extends XEvent> T readEvent(byte number, boolean sentEvent, X11Input in) {
            throw new IllegalArgumentException("XVideo's events are written by the server");
        }

        @Override
        public <T extends XError> T readError(byte code, X11Input in) {
            throw new IllegalArgumentException("XVideo's errors are written by the server");
        }

        @Override
        public <T extends XGenericEvent> T readGenericEvent(byte firstEventOffset, boolean sentEvent,
                byte extension, short sequenceNumber, int length, short eventType, X11Input in) {
            throw new IllegalArgumentException("XVideo defines no generic events");
        }
    }
}
