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
import cn.oyzh.fx.xserver.extension.Extension;
import cn.oyzh.fx.xserver.resource.XResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SECURITY (XSecurity): the extension that lets a client run another client it does not trust.
 *
 * <p>Its subject is not a resource but a <em>connection</em>. A trusted client asks for an
 * authorization - a token - and hands it to the client it does not trust, which connects with it; the
 * connection it gets is an <b>untrusted</b> one. It draws its own windows and asks the questions every
 * client asks, and it may not read another client's pixels or properties, move another client's
 * windows, change the focus or the selections, or touch the SECURITY extension at all. That is the
 * answer to "how do I run a client I do not trust", which without this extension is "you do not".
 *
 * <p><b>What is denied is measured, not read off the specification.</b> {@code tools/SecurityProbe}
 * generates a token, connects with it, and then sends the same battery of requests from that
 * connection and from a trusted one - because an error that appears on *both* is a request the probe
 * got wrong, and only a difference between the two is the trust level. Against a reference with
 * access control on, the differences are:
 *
 * <ul>
 *   <li>{@code GetImage}, {@code GetProperty}, {@code ChangeProperty}, {@code ConfigureWindow} and
 *       {@code SetInputFocus} are refused <b>{@code BadAccess}</b> when they name a window the client
 *       does not own. The root is not the client's, so these are the requests that read or move the
 *       world.</li>
 *   <li>{@code SetSelectionOwner} is refused the same way: a selection is shared state and does not
 *       belong to a drawable.</li>
 *   <li><b>The extension itself is hidden</b>: an untrusted client's {@code ListExtensions} lists two
 *       extensions on the reference where a trusted client sees twenty three - {@code BIG-REQUESTS}
 *       and {@code XC-MISC}, the two that only ask the server about itself - {@code QueryExtension}
 *       answers "not present" for every other, and a request to a hidden extension's opcode is
 *       {@code BadRequest} rather than {@code BadAccess}. That is what an untrusted client gets for
 *       {@code SecurityQueryVersion} and {@code SecurityGenerateAuthorization} too.</li>
 *   <li>What it may still do is as measured: create, map and draw its own windows and pixmaps, read
 *       its own pixels, properties and geometry, and ask anything about *geometry* - {@code
 *       GetWindowAttributes}, {@code QueryTree} and {@code GetGeometry} on any window are answered,
 *       which is what a toolkit needs to place itself. {@code GrabServer}, {@code SendEvent},
 *       {@code GetKeyboardMapping} and even {@code ChangeHosts} are accepted by that reference, and
 *       they are accepted here for the same reason: copying a rule the reference does not have is a
 *       difference a client can see.</li>
 * </ul>
 *
 * <p><b>A token is an ordinary credential.</b> Two names that seem right are not: the specification's
 * prose suggests {@code SECURITY} and the extension's own header defines
 * {@code XSecurityAuthorizationName} as "XC-QUERY-SECURITY-1", and a reference refuses **both** at the
 * connection setup ("Authorization protocol not supported by server"). What
 * {@code SecurityGenerateAuthorization} hands a client is the name the authorization was created for
 * (usually {@code MIT-MAGIC-COOKIE-1}) and a fresh sixteen byte cookie, and the trust level stored
 * with it on the server is what makes the connection untrusted - nothing the client says.
 *
 * <p><b>A trust level is not set by default.</b> A generated authorization is <b>untrusted</b> unless
 * its attributes say otherwise (measured: the first token this project generated, with no attributes,
 * produced a connection that could read the root's pixels no more than the measurements above allow).
 * The trusted half is an attribute: mask bit {@code XSecurityTrustLevel} with a value of
 * {@code XSecurityClientTrusted}.
 *
 * <p><b>Revoking closes the connection.</b> A revoked authorization cannot be used to connect again
 * (the reference refuses it at the setup, as a wrong cookie would be), and a client that is *still*
 * connected with it is disconnected - told with this extension's one event,
 * {@code AuthorizationRevoked}, if its authorization asked for it. Measured: the reference closed the
 * connection without an event when the authorization had no event mask, which is the case this
 * implements.
 *
 * <p><b>All of it needs {@code --auth}.</b> With access control off there is nothing to check, so a
 * connection offering a token is trusted - which is what a reference does with {@code -ac} (the usual
 * testbed runs Xvfb that way, which is why the F7 measurements were taken against a server started
 * with an authority file).
 */
public final class SecurityExtension implements Extension {

    private static final Logger log = LoggerFactory.getLogger(SecurityExtension.class);

    private static final int QUERY_VERSION = 0;
    private static final int GENERATE_AUTHORIZATION = 1;
    private static final int REVOKE_AUTHORIZATION = 2;

    /** {@code valueMask} bits of a generated authorization, from the extension's own header. */
    private static final int TIMEOUT_MASK = 1;
    private static final int TRUST_LEVEL_MASK = 2;
    private static final int GROUP_MASK = 4;
    private static final int EVENT_MASK_MASK = 8;

    /** Trust levels: what the client asks for, and what it gets when it does not ask. */
    private static final int TRUSTED = 0;
    private static final int UNTRUSTED = 1;

    /** The extension's one event, {@code AuthorizationRevoked}, at {@code firstEvent + 0}. */
    private static final int AUTHORIZATION_REVOKED = 0;

    /** The event mask bit that asks to be told when an authorization of yours is revoked. */
    private static final int AUTHORIZATION_REVOKED_MASK = 1;

    /** Measured: a reference answers 1.0 to a client asking for any version. */
    private static final int MAJOR_VERSION = 1;
    private static final int MINOR_VERSION = 0;

    /** The length of a generated cookie, which is the credential it becomes: sixteen bytes. */
    private static final int COOKIE_BYTES = 16;

    private final SecurityPlugin plugin = new SecurityPlugin();
    private final Map<Integer, Authorization> authorizations = new ConcurrentHashMap<>();
    private final AtomicInteger nextAuthorizationId = new AtomicInteger(1);
    private final SecureRandom random = new SecureRandom();

    private XServer server;

    /**
     * One authorization a trusted client asked for: the name and cookie it becomes, and the trust
     * level every connection made with it gets.
     */
    public record Authorization(int id, String protocol, byte[] data, boolean trusted, int eventMask) {
    }

    @Override
    public void init(XServer server) {
        this.server = server;
    }

    @Override
    public String name() {
        return "SECURITY";
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
    // Used by the rest of the server
    // ------------------------------------------------------------------

    /**
     * The authorization a connection is offering, or null when these credentials are not one this
     * server generated. Called from the connection setup, before the {@code .Xauthority} check: a
     * token this server minted is a credential of its own, and it carries the trust level with it.
     */
    public Authorization authorizationFor(String protocol, byte[] data) {
        if (protocol == null || data == null) {
            return null;
        }
        for (Authorization authorization : authorizations.values()) {
            if (authorization.protocol().equals(protocol)
                    && Arrays.equals(authorization.data(), data)) {
                return authorization;
            }
        }
        return null;
    }

    /**
     * Refuses a request from an untrusted client that names a window it does not own.
     *
     * <p>This is the whole of the untrusted rule for the core requests that have one: the five that
     * read or change somebody else's drawable are refused {@code BadAccess}, and everything else is
     * answered as it is for anybody. Measured on a reference - see the class comment for the list.
     */
    public static void requireOwned(ClientConnection client, XResource window, String what) {
        if (client.isTrusted() || window == null || window.owner() == client) {
            return;
        }
        throw new ProtocolException(ProtocolException.ACCESS, window.id(),
                what + " refused: the connection is untrusted and 0x"
                        + Integer.toHexString(window.id()) + " is not its own");
    }

    /** The same for a request that is refused outright, whatever it names - a selection. */
    public static void requireTrusted(ClientConnection client, String what) {
        if (client.isTrusted()) {
            return;
        }
        throw new ProtocolException(ProtocolException.ACCESS, 0,
                what + " refused: the connection is untrusted");
    }

    /**
     * Whether an extension is visible to an untrusted client: only the two that ask about the server
     * itself, which is what a reference's untrusted {@code ListExtensions} answers.
     */
    public static boolean visibleToUntrusted(Extension extension) {
        return extension.visibleToUntrustedClients();
    }

    // ------------------------------------------------------------------
    // Decoding
    // ------------------------------------------------------------------

    /**
     * Every SECURITY request, decoded here: the library generates no module for this extension.
     *
     * <p>The four header bytes are consumed the way every generated reader consumes them, so the
     * stream is left exactly where the next request begins.
     */
    @Override
    public XRequest decodeRequest(int minorOpcode, X11Input in) throws IOException {
        int minor = minorOpcode & 0xFF;
        if (minor == QUERY_VERSION) {
            in.readPad(1);
            in.readCard16();
            int clientMajor = in.readCard16();
            int clientMinor = in.readCard16();
            return new QueryVersion(clientMajor, clientMinor);
        }
        if (minor == GENERATE_AUTHORIZATION) {
            in.readPad(1);
            in.readCard16();
            int nameBytes = in.readCard16();
            int dataBytes = in.readCard16();
            int valueMask = in.readCard32();
            byte[] name = new byte[nameBytes];
            for (int i = 0; i < nameBytes; i++) {
                name[i] = (byte) in.readCard8();
            }
            in.readPadAlign(12 + nameBytes);
            byte[] data = new byte[dataBytes];
            for (int i = 0; i < dataBytes; i++) {
                data[i] = (byte) in.readCard8();
            }
            // The size to align *includes* the padding the name just took: twelve bytes of header,
            // the name padded to four, then the data. Passing the unpadded sums (12 + name + data)
            // reads one byte too far for the common case of a nineteen byte protocol name, which is
            // how the first version of this decoder failed every token request.
            in.readPadAlign(12 + ((nameBytes + 3) & ~3) + dataBytes);
            int[] values = new int[Integer.bitCount(valueMask)];
            for (int i = 0; i < values.length; i++) {
                values[i] = in.readCard32();
            }
            return new GenerateAuthorization(new String(name, StandardCharsets.ISO_8859_1), data,
                    valueMask, values);
        }
        if (minor == REVOKE_AUTHORIZATION) {
            in.readPad(1);
            in.readCard16();
            int authId = in.readCard32();
            return new RevokeAuthorization(authId);
        }
        throw new ProtocolException(ProtocolException.REQUEST, minor,
                "unknown SECURITY request " + minor);
    }

    @Override
    public void handleRequest(XServer server, ClientConnection client, XRequest request) {
        if (request instanceof QueryVersion) {
            client.sendReply(new VersionReply(client.sequenceNumber(), MAJOR_VERSION, MINOR_VERSION));
            return;
        }
        if (request instanceof GenerateAuthorization generate) {
            generate(client, generate);
            return;
        }
        if (request instanceof RevokeAuthorization revoke) {
            revoke(revoke.authorization());
            return;
        }
        throw new IllegalArgumentException("unhandled SECURITY request " + request.getClass());
    }

    /**
     * Mints a token: the client gets an id it can revoke by and the cookie itself, which is offered
     * back at a connection setup under the name the authorization was created for.
     */
    private void generate(ClientConnection client, GenerateAuthorization request) {
        int at = 0;
        int timeout = 0;
        boolean trusted = false;
        int group = 0;
        int eventMask = 0;
        if ((request.valueMask() & TIMEOUT_MASK) != 0) {
            timeout = request.values()[at++];
        }
        if ((request.valueMask() & TRUST_LEVEL_MASK) != 0) {
            // A trust level of Trusted (0) is what a client asks for when it wants an authorization
            // that produces *trusted* connections; anything else is the restricted kind. Without the
            // attribute a token is untrusted, which is what a reference was measured doing.
            trusted = request.values()[at++] == TRUSTED;
        }
        if ((request.valueMask() & GROUP_MASK) != 0) {
            group = request.values()[at++];
        }
        if ((request.valueMask() & EVENT_MASK_MASK) != 0) {
            eventMask = request.values()[at++];
        }
        byte[] cookie = new byte[COOKIE_BYTES];
        random.nextBytes(cookie);
        int id = nextAuthorizationId.getAndIncrement();
        authorizations.put(id, new Authorization(id, request.protocol(), cookie, trusted, eventMask));
        log.debug("SECURITY: authorization {} for {} (trusted={}, timeout={}, group={}, events={})",
                id, request.protocol(), trusted, timeout, group, eventMask);
        client.sendReply(new AuthorizationReply(client.sequenceNumber(), id, cookie));
    }

    /**
     * Revokes an authorization: it cannot be used to connect again, and every client that is still
     * connected with it is told (if it asked to be) and then closed - which is what a reference was
     * measured doing, and the reason this does not merely forget the token.
     */
    private void revoke(int authId) {
        Authorization authorization = authorizations.remove(authId);
        if (authorization == null) {
            return;
        }
        for (ClientConnection client : server.clients()) {
            if (client.authorizationId() != authId) {
                continue;
            }
            if ((authorization.eventMask() & AUTHORIZATION_REVOKED_MASK) != 0) {
                server.events().send(client, new AuthorizationRevokedEvent(firstEvent(),
                        client.sequenceNumber(), authId));
            }
            client.close();
        }
    }

    // ------------------------------------------------------------------
    // The requests, as objects
    // ------------------------------------------------------------------

    /** {@code SecurityQueryVersion}: two version words. The reply is the server's own. */
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
            return "security";
        }

        @Override
        public void write(byte majorOpcode, X11Output out) throws IOException {
            out.writeCard8(majorOpcode);
            out.writeCard8((byte) QUERY_VERSION);
            out.writeCard16((short) 2);
            out.writeCard16((short) clientMajor);
            out.writeCard16((short) clientMinor);
        }
    }

    /** {@code SecurityGenerateAuthorization}: the name and data to mint a token for, and its mask. */
    private record GenerateAuthorization(String protocol, byte[] data, int valueMask, int[] values)
            implements XRequest {

        @Override
        public byte getOpCode() {
            return (byte) GENERATE_AUTHORIZATION;
        }

        @Override
        public int getSize() {
            return 12 + ((protocol.length() + 3) & ~3) + ((data.length + 3) & ~3) + values.length * 4;
        }

        @Override
        public String getPluginName() {
            return "security";
        }

        @Override
        public void write(byte majorOpcode, X11Output out) throws IOException {
            out.writeCard8(majorOpcode);
            out.writeCard8((byte) GENERATE_AUTHORIZATION);
            out.writeCard16((short) (getSize() / 4));
            out.writeCard16((short) protocol.length());
            out.writeCard16((short) data.length);
            out.writeCard32(valueMask);
            for (byte b : protocol.getBytes(StandardCharsets.ISO_8859_1)) {
                out.writeCard8(b);
            }
            out.writePadAlign(12 + protocol.length());
            for (byte b : data) {
                out.writeCard8(b);
            }
            out.writePadAlign(12 + ((protocol.length() + 3) & ~3) + data.length);
            for (int value : values) {
                out.writeCard32(value);
            }
        }
    }

    /** {@code SecurityRevokeAuthorization}: the authorization's id. */
    private record RevokeAuthorization(int authorization) implements XRequest {

        @Override
        public byte getOpCode() {
            return (byte) REVOKE_AUTHORIZATION;
        }

        @Override
        public int getSize() {
            return 8;
        }

        @Override
        public String getPluginName() {
            return "security";
        }

        @Override
        public void write(byte majorOpcode, X11Output out) throws IOException {
            out.writeCard8(majorOpcode);
            out.writeCard8((byte) REVOKE_AUTHORIZATION);
            out.writeCard16((short) 2);
            out.writeCard32(authorization);
        }
    }

    // ------------------------------------------------------------------
    // The replies and the event
    // ------------------------------------------------------------------

    /** {@code SecurityQueryVersion}'s reply: two CARD16 version words at offsets 8 and 10. */
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
            return "security";
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
     * {@code SecurityGenerateAuthorization}'s reply: the id to revoke by, the cookie's length, and
     * the cookie itself as the reply's variable part.
     */
    private record AuthorizationReply(short sequenceNumber, int authorization, byte[] data)
            implements XReply {

        @Override
        public short getSequenceNumber() {
            return sequenceNumber;
        }

        @Override
        public int getSize() {
            return 32 + ((data.length + 3) & ~3);
        }

        @Override
        public String getPluginName() {
            return "security";
        }

        @Override
        public void write(X11Output out) throws IOException {
            out.writeCard8(getResponseCode());
            out.writePad(1);
            out.writeCard16(sequenceNumber);
            out.writeCard32(getLength());
            out.writeCard32(authorization);
            out.writeCard16((short) data.length);
            out.writePad(18);
            for (byte b : data) {
                out.writeCard8(b);
            }
            out.writePadAlign(32 + data.length);
        }
    }

    /**
     * {@code AuthorizationRevoked}: the extension's one event, sent to a client whose authorization
     * has just been revoked, immediately before the server closes it.
     *
     * <p>Written out rather than generated because the library has no module for this extension: the
     * event is thirty two bytes with the authorization's id at offset 4.
     */
    private record AuthorizationRevokedEvent(byte firstEvent, short sequenceNumber, int authorization)
            implements XEvent {

        @Override
        public byte getNumber() {
            return (byte) AUTHORIZATION_REVOKED;
        }

        @Override
        public short getSequenceNumber() {
            return sequenceNumber;
        }

        @Override
        public byte getFirstEventOffset() {
            return firstEvent;
        }

        @Override
        public byte getResponseCode() {
            return (byte) (firstEvent + AUTHORIZATION_REVOKED);
        }

        @Override
        public String getPluginName() {
            return "security";
        }

        @Override
        public int getSize() {
            return 32;
        }

        @Override
        public void write(X11Output out) throws IOException {
            out.writeCard8((byte) (firstEvent + AUTHORIZATION_REVOKED));
            out.writeCard8((byte) 0);
            out.writeCard16(sequenceNumber);
            out.writeCard32(authorization);
            out.writePad(24);
        }
    }

    /**
     * The plugin the registry needs for this extension's opcode, event and error ranges.
     *
     * <p>It is never asked to decode anything - {@link #decodeRequest} answers every request - so its
     * read methods refuse rather than pretend. SECURITY defines one event and no errors of its own:
     * what an untrusted client is refused is a *core* error ({@code BadAccess}), which is the
     * reference's own answer and the reason `firstError` is zero.
     */
    private static final class SecurityPlugin implements XProtocolPlugin {

        private byte majorOpcode;
        private byte firstEvent;
        private byte firstError;

        @Override
        public String getPluginName() {
            return "security";
        }

        @Override
        public Optional<String> getExtensionXName() {
            return Optional.of("SECURITY");
        }

        @Override
        public Optional<String> getExtensionName() {
            return Optional.of("Security");
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
            return request instanceof QueryVersion || request instanceof GenerateAuthorization
                    || request instanceof RevokeAuthorization;
        }

        @Override
        public boolean supportedRequest(byte majorOpcode, byte minorOpcode) {
            int minor = minorOpcode & 0xFF;
            return minor >= QUERY_VERSION && minor <= REVOKE_AUTHORIZATION;
        }

        @Override
        public boolean supportedEvent(byte number) {
            return (number & 0xFF) == firstEvent + AUTHORIZATION_REVOKED;
        }

        @Override
        public boolean supportedError(byte code) {
            return false;
        }

        @Override
        public <T extends XRequest> T readRequest(byte majorOpcode, byte minorOpcode, X11Input in) {
            throw new IllegalArgumentException("SECURITY requests are decoded by the extension");
        }

        @Override
        public <T extends XEvent> T readEvent(byte number, boolean sentEvent, X11Input in) {
            throw new IllegalArgumentException("SECURITY's event is written by the server");
        }

        @Override
        public <T extends XError> T readError(byte code, X11Input in) {
            throw new IllegalArgumentException("SECURITY defines no errors");
        }

        @Override
        public <T extends XGenericEvent> T readGenericEvent(byte firstEventOffset, boolean sentEvent,
                byte extension, short sequenceNumber, int length, short eventType, X11Input in) {
            throw new IllegalArgumentException("SECURITY defines no generic events");
        }
    }
}
