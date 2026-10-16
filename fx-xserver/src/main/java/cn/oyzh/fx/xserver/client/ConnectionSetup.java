package cn.oyzh.fx.xserver.client;

import com.github.moaxcp.x11.protocol.XAuthority;
import com.github.moaxcp.x11.protocol.xproto.Setup;
import com.github.moaxcp.x11.protocol.xproto.SetupRequest;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.display.Screen;
import org.eclipse.collections.api.list.primitive.ByteList;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.primitive.ByteLists;
import cn.oyzh.common.log.JulLog;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * The connection setup handshake: validates the client's credentials and answers with the
 * {@code Setup} reply describing the server's screens, visuals and formats.
 */
public final class ConnectionSetup {


    public static final String AUTH_COOKIE = "MIT-MAGIC-COOKIE-1";

    /** Protocol version we speak. 11.0 is the version every client understands. */
    private static final short PROTOCOL_MAJOR = 11;
    private static final short PROTOCOL_MINOR = 0;

    /** Reported as the server's release number; arbitrary but stable. */
    private static final int RELEASE_NUMBER = 12100000;

    private static final byte MIN_KEYCODE = 8;
    private static final byte MAX_KEYCODE = (byte) 0xFF;

    private static final byte IMAGE_BYTE_ORDER_LSB = 0;
    private static final byte IMAGE_BYTE_ORDER_MSB = 1;
    private static final byte BITMAP_BIT_ORDER_LSB = 0;
    private static final byte BITMAP_SCANLINE_UNIT = 32;
    private static final byte BITMAP_SCANLINE_PAD = 32;

    private static final String VENDOR = "cn/oyzh/fx/xserver";

    private final XServer server;

    public ConnectionSetup(XServer server) {
        this.server = server;
    }

    /** Handles a fresh connection, sending either the setup reply or a failure. */
    public void accept(ClientConnection client, SetupRequest request) throws IOException {
        String authName = asAscii(request.getAuthorizationProtocolName());
        byte[] authData = toBytes(request.getAuthorizationProtocolData());

        if (!authenticate(client, authName, authData)) {
            JulLog.info("rejecting client {}: authentication failed", client.peer());
            sendFailed(client, "Authentication rejected");
            return;
        }
        if (request.getProtocolMajorVersion() != PROTOCOL_MAJOR) {
            JulLog.info("rejecting client {}: protocol version {}.{}", client.peer(),
                    request.getProtocolMajorVersion(), request.getProtocolMinorVersion());
            sendFailed(client, "Protocol version not supported");
            return;
        }
        if (server.options().perClientWindows()) {
            // Under the server lock: the display tick walks the screens and the window tree, and it
            // must never see a half-built one.
            server.lockServer();
            try {
                server.openClientScreen(client);
            } finally {
                server.unlockServer();
            }
        }
        sendSetup(client);
    }

    private boolean authenticate(ClientConnection client, String authName, byte[] authData) {
        // A token this server minted is checked first, and **before the question of whether access
        // control is on at all**: its credentials are right by construction and the trust level it
        // was created with is what the connection gets. Measured: a reference running with `-ac` -
        // which asks nobody for anything - still treats a connection that offers such a token as
        // untrusted, because `-ac` is "never refuse a client for a missing cookie", not "ignore the
        // credentials a client does offer" (F7).
        cn.oyzh.fx.xserver.extension.SecurityExtension.Authorization authorization =
                server.security() == null ? null
                        : server.security().authorizationFor(authName, authData);
        if (authorization != null) {
            client.authenticatedBy(authorization.id(), authorization.trusted());
            JulLog.info("client {} authenticated as authorization {} (trusted={})", client.peer(),
                    authorization.id(), authorization.trusted());
            return true;
        }
        if (!server.options().authRequired()) {
            // An -ac server: no cookie is asked for, and anything else offered is ignored.
            return true;
        }
        if (authName.isEmpty()) {
            // Access control is on and the client offered nothing. A real server refuses this: the
            // setup request always has room for credentials, so "none offered" is a client asking to
            // skip them, and accepting it makes --auth decorative - any client that leaves the block
            // empty would be let in. (An empty *cookie* is a different thing and is checked below.)
            JulLog.info("rejecting client {}: access control is on and no credentials were offered",
                    client.peer());
            return false;
        }
        if (AUTH_COOKIE.equals(authName)) {
            return checkCookie(authData);
        }
        JulLog.info("unknown authorization protocol '{}'", authName);
        return false;
    }

    private boolean checkCookie(byte[] authData) {
        File file = server.options().authFile();
        if (file != null && file.exists() && checkCookieIn(file, authData)) {
            return true;
        }
        File defaultFile = XAuthority.getXAuthorityFile();
        if (defaultFile != null && defaultFile.exists() && checkCookieIn(defaultFile, authData)) {
            return true;
        }
        JulLog.warn("no matching MIT-MAGIC-COOKIE-1 entry found; rejecting client");
        return false;
    }

    private boolean checkCookieIn(File file, byte[] authData) {
        List<XAuthority> authorities = XAuthority.getAuthorities(file);
        for (XAuthority authority : authorities) {
            if (!AUTH_COOKIE.equals(asAscii(authority.getProtocolName()))) {
                continue;
            }
            if (authority.getDisplayNumber() != server.options().displayNumber()
                    && authority.getDisplayNumber() != -1) {
                continue;
            }
            if (java.util.Arrays.equals(toBytes(authority.getProtocolData()), authData)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sends a setup failure. The reply is encoded here rather than with the generated class
     * because its length field counts the padded reason, which the generated writer does not
     * produce.
     */
    private void sendFailed(ClientConnection client, String reason) throws IOException {
        byte[] text = reason.getBytes(StandardCharsets.ISO_8859_1);
        int padded = (text.length + 3) & ~3;
        byte[] bytes = new byte[8 + padded];
        bytes[0] = 0;                                   // 0 means "failed"
        bytes[1] = (byte) text.length;
        putCard16(client, bytes, 2, PROTOCOL_MAJOR);
        putCard16(client, bytes, 4, PROTOCOL_MINOR);
        putCard16(client, bytes, 6, padded / 4);        // reason length in 4 byte units
        System.arraycopy(text, 0, bytes, 8, text.length);
        client.output().writeRaw(bytes);
        client.flush();
        client.close();
    }

    /**
     * The screen as the protocol describes it, which means its size - and a screen's size can change
     * while clients are connected, because the display tick grows one when a client's window is
     * dragged past it.
     *
     * <p>Read under the server lock, so a client cannot be told a size the framebuffer does not have:
     * a client that believes a root is larger than the pixels behind it computes a stride and an
     * origin that do not exist, and the damage it then draws lands outside the screen.
     */
    private com.github.moaxcp.x11.protocol.xproto.Screen protocolScreen(Screen screen) {
        server.lockServer();
        try {
            return screen.toProtocolScreen(screen.root().id(), screen.defaultColormap().id());
        } finally {
            server.unlockServer();
        }
    }

    private void sendSetup(ClientConnection client) throws IOException {
        // A connection with a screen of its own is told about that one only: it is the only screen
        // that client can see, and its root is the one it builds its windows in.
        Screen screen = server.screenOf(client);
        int vendorPadded = (VENDOR.length() + 3) & ~3;

        var formats = screen.pixmapFormats();
        var root = protocolScreen(screen);

        // The length field counts everything after the 8 byte prefix (status, unused, and the two
        // version words): the rest of the 40 byte header plus all the variable data. Verified
        // against Xvfb, whose length times four plus eight is exactly its reply size.
        int headerRemainder = 32;
        int additionalBytes = headerRemainder + vendorPadded
                + formats.stream().mapToInt(f -> f.getSize()).sum()
                + root.getSize();
        short length = (short) (additionalBytes / 4);

        Setup setup = Setup.builder()
                .status((byte) 1)
                .protocolMajorVersion(PROTOCOL_MAJOR)
                .protocolMinorVersion(PROTOCOL_MINOR)
                .length(length)
                .releaseNumber(RELEASE_NUMBER)
                .resourceIdBase(client.resourceIdBase())
                .resourceIdMask(client.resourceIdMask())
                .motionBufferSize(0)
                .maximumRequestLength((short) client.maximumRequestUnits())
                .imageByteOrder(client.isBigEndian() ? IMAGE_BYTE_ORDER_MSB : IMAGE_BYTE_ORDER_LSB)
                .bitmapFormatBitOrder(client.isBigEndian() ? IMAGE_BYTE_ORDER_MSB
                        : BITMAP_BIT_ORDER_LSB)
                .bitmapFormatScanlineUnit(BITMAP_SCANLINE_UNIT)
                .bitmapFormatScanlinePad(BITMAP_SCANLINE_PAD)
                .minKeycode(MIN_KEYCODE)
                .maxKeycode(MAX_KEYCODE)
                .vendor(ByteLists.immutable.of(VENDOR.getBytes(StandardCharsets.ISO_8859_1)))
                .pixmapFormats(Lists.immutable.withAll(formats))
                .roots(Lists.immutable.of(root))
                .build();

        // The setup reply's own length field is at bytes 4..8, so it must not be rewritten the
        // way ordinary replies are.
        byte[] reply = client.output().encode(setup);
        client.output().writeRaw(reply);
        client.flush();
        // The setup reply is what a recorder is given as the client-started element: the bytes it
        // needs to rebuild the client that produced the rest of the stream. It is handed over rather
        // than merely observed because the listener below is attached after it - a reference does not
        // record the setup reply as an ordinary server message, and attaching the listener first put
        // a second, redundant datum in front of every recording (F2, measured).
        server.recordClientStarted(client, reply);
        // Everything this connection sends from here on passes through the output, so that is where
        // RECORD sees it (F2): one hook covers replies, events, errors and every hand-encoded image
        // an extension writes.
        client.output().setListener(bytes -> server.recordServerMessage(client, bytes));
        JulLog.debug("setup sent to {}", client.peer());
    }

    private void putCard16(ClientConnection client, byte[] bytes, int offset, int value) {
        if (client.isBigEndian()) {
            bytes[offset] = (byte) (value >> 8);
            bytes[offset + 1] = (byte) value;
        } else {
            bytes[offset] = (byte) value;
            bytes[offset + 1] = (byte) (value >> 8);
        }
    }

    private static String asAscii(ByteList bytes) {
        return new String(toBytes(bytes), StandardCharsets.ISO_8859_1);
    }

    private static byte[] toBytes(ByteList bytes) {
        return bytes == null ? new byte[0] : bytes.toArray();
    }

    /** The display name clients should use, e.g. {@code :1}. */
    public String displayName() {
        return ":" + server.options().displayNumber();
    }
}
