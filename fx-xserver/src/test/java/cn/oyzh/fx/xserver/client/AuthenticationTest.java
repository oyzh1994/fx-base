package cn.oyzh.fx.xserver.client;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What access control actually refuses.
 *
 * <p>{@code --auth} is only worth having if a client cannot skip it, and the setup request always has
 * room for credentials - so "offered nothing" has to be refused, not waved through. It used to be
 * waved through: {@code authenticate} accepted any client whose authorization protocol name was
 * empty, which meant that leaving the credentials out of the handshake was enough to reach a server
 * that had asked for them. The server listens on every interface by default, so that was the whole
 * of the access control.
 *
 * <p>The other half of the rule is that the flag still means something when it is *not* given: an
 * {@code -ac} server asks for nothing and takes anything, which is the default here and what every
 * other test in this suite relies on.
 */
class AuthenticationTest {

    /** A display number the rest of the suite does not use. */
    private static final int DISPLAY = 92;

    /**
     * And another one for the server without access control: two servers in one test class must not
     * share a port, and neither may collide with another test's display.
     */
    private static final int OPEN_DISPLAY = 89;

    /** Sixteen bytes, as a real cookie is; the value itself does not matter. */
    private static final byte[] COOKIE = {
            0x0f, 0x1e, 0x2d, 0x3c, 0x4b, 0x5a, 0x69, 0x78, 0x01, 0x23, 0x45, 0x67, 0x10, 0x32,
            0x54, 0x76};

    private static XServer server;

    @BeforeAll
    static void startServer(@TempDir Path directory) throws IOException {
        File authFile = directory.resolve("Xauthority").toFile();
        writeAuthority(authFile, DISPLAY, COOKIE);
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--backend", "headless", "--no-unix", "--auth", authFile.getAbsolutePath()}));
        server.start();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void aClientThatOffersNothingIsRefusedWhenAccessControlIsOn() throws IOException {
        assertEquals("", handshake(null, null),
                "a handshake with no credentials must not be answered with a setup reply");
    }

    @Test
    void theMatchingCookieIsAccepted() throws IOException {
        assertEquals("setup", handshake("MIT-MAGIC-COOKIE-1", COOKIE));
    }

    @Test
    void aWrongCookieIsRefused() throws IOException {
        byte[] wrong = COOKIE.clone();
        wrong[0] ^= 0x01;
        assertEquals("", handshake("MIT-MAGIC-COOKIE-1", wrong));
    }

    @Test
    void anUnknownAuthorizationProtocolIsRefused() throws IOException {
        assertEquals("", handshake("XDM-AUTHORIZATION-1", COOKIE));
    }

    @Test
    void anEmptyCookieIsNotTheSameAsOfferingNone() throws IOException {
        // A client with an Xauthority entry that happens to be empty offers the protocol with no
        // data. That is a wrong cookie, not a missing one, and it must not be the way in either.
        assertEquals("", handshake("MIT-MAGIC-COOKIE-1", new byte[0]));
    }

    @Test
    void aServerWithoutAccessControlAcceptsAClientThatOffersNothing() throws IOException {
        // The other half of the rule, and the path every other test takes: with --no-auth (the
        // default) an empty handshake is answered normally.
        XServer open = new XServer(ServerOptions.parse(new String[]{"--display", ":" + OPEN_DISPLAY,
                "--backend", "headless", "--no-unix"}));
        open.start();
        try {
            assertEquals("setup", handshakeAt(OPEN_DISPLAY, null, null));
        } finally {
            open.stop();
        }
    }

    /**
     * Sends a connection setup and returns {@code "setup"} when the server answered one, or
     * {@code ""} when it refused - which is the whole answer these tests need.
     */
    private static String handshake(String authName, byte[] authData) throws IOException {
        return handshakeAt(DISPLAY, authName, authData);
    }

    private static String handshakeAt(int display, String authName, byte[] authData)
            throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", 6000 + display), 5000);
            socket.setSoTimeout(5000);
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            byte[] name = authName == null ? new byte[0] : authName.getBytes(StandardCharsets.ISO_8859_1);
            byte[] data = authData == null ? new byte[0] : authData;
            // The setup request: order, protocol version, the two lengths and two bytes of padding,
            // then the name and the data each padded to four bytes. An empty name with empty data is
            // exactly what a client with no Xauthority entry sends.
            int body = 12 + pad4(name.length) + pad4(data.length);
            byte[] request = new byte[body];
            request[0] = 'l';
            request[2] = 11;
            // The length fields are read in the byte order the client declared ('l' here), so they
            // are little endian - big endian lengths make the server wait for thousands of bytes
            // that never come, which reads as a hung handshake rather than a bad request.
            request[6] = (byte) name.length;
            request[7] = (byte) (name.length >> 8);
            request[8] = (byte) data.length;
            request[9] = (byte) (data.length >> 8);
            System.arraycopy(name, 0, request, 12, name.length);
            System.arraycopy(data, 0, request, 12 + pad4(name.length), data.length);
            out.write(request);
            out.flush();

            // A failed setup is two bytes and a string, not a thirty two byte record: reading it as
            // a reply header hangs on the bytes that never come.
            int first = in.readUnsignedByte();
            if (first != 1) {
                byte[] failure = new byte[7];
                in.readFully(failure);
                int reasonLength = failure[0] & 0xFF;
                byte[] reason = new byte[(reasonLength + 3) & ~3];
                in.readFully(reason);
                assertTrue(reasonLength > 0, "a refusal has to say why");
                return "";
            }
            byte[] header = new byte[7];
            in.readFully(header);                   // unused, major, minor, then the length
            int length = (header[5] & 0xFF) | ((header[6] & 0xFF) << 8);
            in.readFully(new byte[length * 4]);
            return "setup";
        }
    }

    private static int pad4(int value) {
        return (value + 3) & ~3;
    }

    /**
     * Writes one {@code .Xauthority} entry, in the format Xlib reads: five counted fields, the
     * display number as a decimal string.
     */
    private static void writeAuthority(File file, int display, byte[] cookie) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        out.writeShort(256);                        // Family.local
        out.writeShort(0);                          // address: the empty local address
        byte[] number = Integer.toString(display).getBytes(StandardCharsets.ISO_8859_1);
        out.writeShort(number.length);
        out.write(number);
        byte[] name = ConnectionSetup.AUTH_COOKIE.getBytes(StandardCharsets.ISO_8859_1);
        out.writeShort(name.length);
        out.write(name);
        out.writeShort(cookie.length);
        out.write(cookie);
        Files.write(file.toPath(), bytes.toByteArray());
    }
}
