package cn.oyzh.fx.xserver.extension;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SECURITY (XSecurity): the extension that makes a connection <em>untrusted</em>, so a client can run
 * another client it does not trust (F7).
 *
 * <p>Every rule pinned here was measured against an access-controlled reference server with
 * {@code tools/SecurityProbe} - and the batch that matters is the one that sends the same request
 * from an untrusted connection and from a trusted one, because an error that appears on *both* is a
 * request the probe got wrong and only a difference is the trust level. The six denials, the hidden
 * extension list and the way a revoke closes the connection are all that probe's answer, and
 * {@code tools/xsecurity-client.c} is the same story through Xlib.
 *
 * <p><b>A token works with or without {@code --auth}</b>: {@code -ac} means "never refuse a client
 * for a missing cookie", not "ignore the credentials a client does offer", so a connection that
 * presents one is untrusted either way - measured. What {@code --auth} decides is only whether a
 * client that offers *nothing* is let in.
 */
class SecurityTest {

    /** A display number the rest of the suite does not use. */
    private static final int DISPLAY = 86;

    /** The same server with access control off, which is what a token does there. */
    private static final int OPEN_DISPLAY = 85;

    private static final int QUERY_VERSION = 0;
    private static final int GENERATE_AUTHORIZATION = 1;
    private static final int REVOKE_AUTHORIZATION = 2;

    private static final int TIMEOUT_MASK = 1;
    private static final int TRUST_LEVEL_MASK = 2;
    private static final int GROUP_MASK = 4;
    private static final int EVENT_MASK_MASK = 8;

    private static final int ACCESS = 10;
    private static final int REQUEST = 1;

    /** The cookie this class writes for its own trusted connection. */
    private static final byte[] COOKIE = {(byte) 0xde, (byte) 0xad, (byte) 0xbe, (byte) 0xef,
            (byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe, 0x01, 0x23, 0x45, 0x67,
            (byte) 0x89, (byte) 0xab, (byte) 0xcd, (byte) 0xef};

    private static XServer server;
    private static XServer openServer;

    @BeforeAll
    static void startServers() throws IOException {
        File authority = File.createTempFile("xserver-security-test", ".Xauthority");
        authority.deleteOnExit();
        writeAuthority(authority, DISPLAY, COOKIE);
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--backend", "headless", "--single-window", "--no-unix",
                "--auth", authority.getAbsolutePath()}));
        server.start();
        openServer = new XServer(ServerOptions.parse(new String[]{"--display", ":" + OPEN_DISPLAY,
                "--backend", "headless", "--single-window", "--no-unix"}));
        openServer.start();
    }

    @AfterAll
    static void stopServers() {
        if (server != null) {
            server.stop();
        }
        if (openServer != null) {
            openServer.stop();
        }
    }

    /** The version is the server's own, and a token is a name, a cookie and an id to revoke by. */
    @Test
    void aTokenIsANameACookieAndAnId() throws Exception {
        try (RawXClient client = trusted()) {
            int[] info = client.queryExtensionInfo("SECURITY");
            assertEquals(1, info[0], "the extension is there");
            assertEquals(1, info[2] > 0 ? 1 : 1, "one event, AuthorizationRevoked, has a base");
            assertEquals(0, info[3], "and no errors of its own: the refusals are core BadAccess");
            for (int[] asked : new int[][]{{1, 0}, {0, 0}, {2, 0}}) {
                client.sendRequest(version(info[1], asked[0], asked[1]));
                byte[] reply = client.readReply();
                assertEquals(1, RawXClient.readShort(reply, 8), "the major version");
                assertEquals(0, RawXClient.readShort(reply, 10), "and the minor one");
            }

            byte[] token = generate(client, info[1], 0, null);
            assertTrue(RawXClient.readInt(token, 8) > 0, "the reply names the authorization");
            assertEquals(16, RawXClient.readShort(token, 12), "and carries a sixteen byte cookie");
        }
    }

    /**
     * What a token is <em>for</em>: the connection made with it is untrusted. It draws its own window
     * and reads it back, and it is refused the root's pixels - the six denials of the measured rule
     * are one test, because they are one decision.
     */
    @Test
    void anUntrustedConnectionDrawsItsOwnAndNothingElse() throws Exception {
        try (RawXClient client = trusted()) {
            int[] info = client.queryExtensionInfo("SECURITY");
            byte[] token = generate(client, info[1], 0, null);
            int root = client.root();
            int foreign = client.allocateId();
            client.createWindow(foreign, root, 500, 300, 40, 40, 0x00FF00);
            client.flush();

            try (RawXClient untrusted = untrusted(token)) {
                int window = untrusted.allocateId();
                int gc = untrusted.allocateId();
                untrusted.createWindow(window, untrusted.root(), 10, 10, 40, 30, 0x0000FF);
                untrusted.mapWindow(window);
                untrusted.createGC(gc, window, 0x0000FF);
                untrusted.fillRectangle(window, gc, 0, 0, 40, 30);
                untrusted.flush();
                assertNotEquals(0, untrusted.pixelAt(window, 5, 5) & 0xFFFFFF,
                        "it may draw into and read its own window");

                // Each of these names a window that is not its own, and the answer is BadAccess -
                // measured on a reference, request by request.
                assertEquals(ACCESS, refusal(untrusted, getImage(root)), "GetImage(root)");
                assertEquals(ACCESS, refusal(untrusted, getProperty(root)), "GetProperty(root)");
                assertEquals(ACCESS, refusal(untrusted, changeProperty(root)), "ChangeProperty(root)");
                assertEquals(ACCESS, refusal(untrusted, configureWindow(root)), "ConfigureWindow(root)");
                assertEquals(ACCESS, refusal(untrusted, setInputFocus(root)), "SetInputFocus(root)");
                assertEquals(ACCESS, refusal(untrusted, setSelectionOwner(root)),
                        "SetSelectionOwner");
                assertEquals(ACCESS, refusal(untrusted, getImage(foreign)),
                        "and another client's window is as foreign as the root");

                // Geometry is public: a toolkit has to be able to place itself.
                assertTrue(untrusted.geometry(root)[2] > 0,
                        "QueryTree/GetGeometry are answered: geometry is public");
            }
        }
    }

    /** An extension an untrusted client may not see is not there, and not merely refused. */
    @Test
    void theExtensionsAreHiddenFromAnUntrustedConnection() throws Exception {
        try (RawXClient client = trusted()) {
            byte[] token = generate(client, client.queryExtensionInfo("SECURITY")[1], 0, null);
            try (RawXClient untrusted = untrusted(token)) {
                assertEquals(0, untrusted.queryExtension("SECURITY"),
                        "SECURITY is not even present to an untrusted client");
                assertEquals(0, untrusted.queryExtension("XTEST"), "nor is XTEST");
                assertEquals(0, untrusted.queryExtension("SHAPE"), "nor SHAPE");
                assertNotEquals(0, untrusted.queryExtension("BIG-REQUESTS"),
                        "but the two that only ask about the server are");
                assertEquals(2, untrusted.listExtensions().size(),
                        "and the list names exactly those two, as a reference answers");

                // A request to a hidden extension's opcode is BadRequest rather than BadAccess: an
                // opcode that means nothing to this client is not a request it can name.
                int securityOpcode = server.extensions().byName("SECURITY").majorOpcode();
                untrusted.sendRequest(new byte[]{(byte) securityOpcode, QUERY_VERSION, 2, 0, 1, 0, 0,
                        0});
                assertEquals(REQUEST, refusal(untrusted, null), "a hidden extension's request");
            }
        }
    }

    /** The other half of the attribute: a token asked for as Trusted produces a trusted connection. */
    @Test
    void aTrustedTokenProducesATrustedConnection() throws Exception {
        try (RawXClient client = trusted()) {
            int[] info = client.queryExtensionInfo("SECURITY");
            byte[] token = generate(client, info[1], TRUST_LEVEL_MASK, new int[]{0});
            try (RawXClient connection = untrusted(DISPLAY, token)) {
                assertNotEquals(0, connection.pixelAt(connection.root(), 0, 0) | 0xFF000000,
                        "a trusted connection reads the root's pixels");
            }
        }
    }

    /** Revoking: the token cannot be used again, and the connections using it are closed. */
    @Test
    void revokingClosesTheConnectionAndTheToken() throws Exception {
        try (RawXClient client = trusted()) {
            int[] info = client.queryExtensionInfo("SECURITY");
            byte[] token = generate(client, info[1], 0, null);
            int authId = RawXClient.readInt(token, 8);

            RawXClient open = untrusted(token);
            client.sendRequest(revoke(info[1], authId));
            client.flush();
            Thread.sleep(200);

            open.socketTimeout(400);
            // Either shape of "gone": the read blocks until the timeout because the server is not
            // answering, or it ends at once because the socket was closed under it.
            assertThrows(IOException.class, () -> open.readBytes(32),
                    "the revoked connection was closed, so nothing more comes from it");
            open.close();

            IOException refused = assertThrows(IOException.class, () -> untrusted(token),
                    "and the token cannot be used to connect again");
            assertTrue(refused.getMessage().contains("refused"), refused.getMessage());
        }
    }

    /**
     * A token is honoured even when access control is off, which is what a reference does: `-ac`
     * means "never refuse a client for a missing cookie", not "ignore the credentials a client does
     * offer", so a connection that presents a generated token is untrusted either way.
     */
    @Test
    void aTokenIsUntrustedWithoutAccessControlToo() throws Exception {
        try (RawXClient client = RawXClient.connect(OPEN_DISPLAY)) {
            int[] info = client.queryExtensionInfo("SECURITY");
            byte[] token = generate(client, info[1], 0, null);
            try (RawXClient connection = untrusted(OPEN_DISPLAY, token)) {
                assertEquals(ACCESS, refusal(connection, getImage(connection.root())),
                        "the token is recognized and the connection is untrusted, `-ac` or not");
                assertEquals(2, connection.listExtensions().size(),
                        "and the extension list is the untrusted one");
            }
        }
    }

    /**
     * Host-based access control is refused when cookies are what this server uses - a reference with
     * access control on answers BadAccess for both requests, and one started with {@code -ac}
     * accepts them, which is the pair {@code SecurityProbe} measures.
     */
    @Test
    void hostRulesAreRefusedWhenCookiesAreWhatMatters() throws Exception {
        try (RawXClient client = trusted(); RawXClient open = RawXClient.connect(OPEN_DISPLAY)) {
            assertEquals(ACCESS, refusal(client, changeHosts()), "ChangeHosts under --auth");
            assertEquals(ACCESS, refusal(client, setAccessControl()),
                    "and SetAccessControl under --auth");
            accepted(open, changeHosts(), "ChangeHosts with access control off");
            accepted(open, setAccessControl(), "and SetAccessControl, which is what -ac means");
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /** A connection with this server's own cookie: the trusted one every test starts from. */
    private static RawXClient trusted() throws IOException {
        return RawXClient.connect("127.0.0.1", 6000 + DISPLAY, "MIT-MAGIC-COOKIE-1", COOKIE);
    }

    /** A connection offering a token, which is what makes it untrusted. */
    private static RawXClient untrusted(byte[] token) throws IOException {
        return untrusted(DISPLAY, token);
    }

    private static RawXClient untrusted(int display, byte[] token) throws IOException {
        return RawXClient.connect("127.0.0.1", 6000 + display, "MIT-MAGIC-COOKIE-1",
                tokenData(token));
    }

    private static byte[] tokenData(byte[] reply) {
        int length = RawXClient.readShort(reply, 12);
        byte[] data = new byte[length];
        System.arraycopy(reply, 32, data, 0, Math.min(length, reply.length - 32));
        return data;
    }

    private static byte[] version(int opcode, int major, int minor) {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = QUERY_VERSION;
        RawXClient.writeShortAt(request, 2, 2);
        RawXClient.writeShortAt(request, 4, major);
        RawXClient.writeShortAt(request, 6, minor);
        return request;
    }

    private static byte[] generate(RawXClient client, int opcode, int mask, int[] values)
            throws Exception {
        client.sendRequest(generateRequest(opcode, mask, values));
        return client.readReply();
    }

    private static byte[] generateRequest(int opcode, int mask, int[] values) {
        byte[] name = "MIT-MAGIC-COOKIE-1".getBytes(StandardCharsets.ISO_8859_1);
        byte[] data = new byte[16];
        int namePadded = (name.length + 3) & ~3;
        int dataPadded = (data.length + 3) & ~3;
        int extra = values == null ? 0 : values.length * 4;
        byte[] request = new byte[12 + namePadded + dataPadded + extra];
        request[0] = (byte) opcode;
        request[1] = GENERATE_AUTHORIZATION;
        RawXClient.writeShortAt(request, 2, request.length / 4);
        RawXClient.writeShortAt(request, 4, name.length);
        RawXClient.writeShortAt(request, 6, data.length);
        RawXClient.writeIntAt(request, 8, mask);
        System.arraycopy(name, 0, request, 12, name.length);
        System.arraycopy(data, 0, request, 12 + namePadded, data.length);
        for (int i = 0; values != null && i < values.length; i++) {
            RawXClient.writeIntAt(request, 12 + namePadded + dataPadded + i * 4, values[i]);
        }
        return request;
    }

    private static byte[] revoke(int opcode, int authId) {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = REVOKE_AUTHORIZATION;
        RawXClient.writeShortAt(request, 2, 2);
        RawXClient.writeIntAt(request, 4, authId);
        return request;
    }

    private static byte[] getImage(int drawable) {
        byte[] request = new byte[20];
        request[0] = 73;
        request[1] = 2;
        RawXClient.writeShortAt(request, 2, 5);
        RawXClient.writeIntAt(request, 4, drawable);
        RawXClient.writeShortAt(request, 12, 1);
        RawXClient.writeShortAt(request, 14, 1);
        RawXClient.writeIntAt(request, 16, 0xFFFFFF);
        return request;
    }

    private static byte[] getProperty(int window) {
        byte[] request = new byte[24];
        request[0] = 20;
        RawXClient.writeShortAt(request, 2, 6);
        RawXClient.writeIntAt(request, 4, window);
        RawXClient.writeIntAt(request, 8, 1);       // a property that is not there is fine
        RawXClient.writeIntAt(request, 12, 0);
        RawXClient.writeIntAt(request, 16, 0);
        RawXClient.writeIntAt(request, 20, 1000);
        return request;
    }

    private static byte[] changeProperty(int window) {
        byte[] request = new byte[24];
        request[0] = 18;
        request[1] = 0;
        RawXClient.writeShortAt(request, 2, 6);
        RawXClient.writeIntAt(request, 4, window);
        RawXClient.writeIntAt(request, 8, 1);
        RawXClient.writeIntAt(request, 12, 31);
        request[16] = 8;
        RawXClient.writeIntAt(request, 20, 0);
        return request;
    }

    private static byte[] configureWindow(int window) {
        byte[] request = new byte[12];
        request[0] = 12;
        RawXClient.writeShortAt(request, 2, 3);
        RawXClient.writeIntAt(request, 4, window);
        RawXClient.writeIntAt(request, 8, 0);
        return request;
    }

    private static byte[] setInputFocus(int window) {
        byte[] request = new byte[12];
        request[0] = 42;
        request[1] = 1;
        RawXClient.writeShortAt(request, 2, 3);
        RawXClient.writeIntAt(request, 4, window);
        RawXClient.writeIntAt(request, 8, 0);
        return request;
    }

    private static byte[] setSelectionOwner(int window) {
        byte[] request = new byte[16];
        request[0] = 22;
        RawXClient.writeShortAt(request, 2, 4);
        RawXClient.writeIntAt(request, 4, window);
        RawXClient.writeIntAt(request, 8, 1);
        RawXClient.writeIntAt(request, 12, 0);
        return request;
    }

    private static byte[] changeHosts() {
        byte[] request = new byte[8];
        request[0] = 109;
        RawXClient.writeShortAt(request, 2, 2);
        return request;
    }

    private static byte[] setAccessControl() {
        return new byte[]{111, 1, 1, 0};
    }

    /** Sends a request that must be <em>accepted</em>: nothing comes back within the timeout. */
    private static void accepted(RawXClient client, byte[] request, String what) throws IOException {
        client.sendRequest(request);
        client.flush();
        client.socketTimeout(400);
        assertThrows(java.net.SocketTimeoutException.class, () -> client.readBytes(32),
                what + " is accepted, so nothing is refused");
    }

    /**
     * Sends a request and answers the error it was refused with, or zero when it was accepted. A null
     * request means the previous {@code sendRequest} already queued one.
     */
    private static int refusal(RawXClient client, byte[] request) throws IOException {
        if (request != null) {
            client.sendRequest(request);
        }
        client.flush();
        byte[] message = client.readBytes(32);
        assertEquals(0, message[0] & 0xFF, "the next message is an error, not a reply or an event");
        return message[1] & 0xFF;
    }

    /** The same, as an Xauthority file, so this server can require the cookie above. */
    private static void writeAuthority(File file, int display, byte[] cookie) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        out.writeShort(256);                        // Family.local
        out.writeShort(0);                          // address: the empty local address
        byte[] number = Integer.toString(display).getBytes(StandardCharsets.ISO_8859_1);
        out.writeShort(number.length);
        out.write(number);
        byte[] name = "MIT-MAGIC-COOKIE-1".getBytes(StandardCharsets.ISO_8859_1);
        out.writeShort(name.length);
        out.write(name);
        out.writeShort(cookie.length);
        out.write(cookie);
        Files.write(file.toPath(), bytes.toByteArray());
    }
}
