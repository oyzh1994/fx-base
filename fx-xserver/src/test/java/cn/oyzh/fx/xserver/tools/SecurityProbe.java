package cn.oyzh.fx.xserver.tools;

import cn.oyzh.fx.xserver.test.RawXClient;

import java.nio.charset.StandardCharsets;

/**
 * The SECURITY extension (XSecurity) as a client uses it - the harness for F7.
 *
 * <p>Its subject is not a resource but a <em>connection</em>: a client can be handed an authorization
 * token, and the connection it makes with that token is an **untrusted** one - it may draw its own
 * window and ask the questions a client needs, and it may not read the keyboard, read other clients'
 * pixels, grab the server or reconfigure the machine for everybody. That is what "run a client I do
 * not trust" means, and this probe measures it rather than assuming it: it generates an authorization,
 * connects with it, and then walks a battery of requests printing which were refused and which were
 * not.
 *
 * <p>Two things this probe cannot do by itself, and both are in the F7 entry: the reference has to be
 * running **with access control on** for an offered token to mean anything (the usual testbed runs
 * Xvfb with `-ac`, which skips authentication - and therefore trust - entirely), and the client half
 * is checked the way the other extensions' are, with a real Xlib client
 * ({@code tools/xsecurity-client.c}) that uses {@code XSecurityGenerateAuthorization}.
 *
 * <p>Usage: {@code SecurityProbe &lt;host&gt; &lt;port&gt;}.
 */
public final class SecurityProbe {

    /** SECURITY's own requests, in protocol order. */
    private static final int QUERY_VERSION = 0;
    private static final int GENERATE_AUTHORIZATION = 1;
    private static final int REVOKE_AUTHORIZATION = 2;

    /** {@code valueMask} bits of an authorization, from the extension's own header. */
    private static final int TIMEOUT_MASK = 1;
    private static final int TRUST_LEVEL_MASK = 2;
    private static final int GROUP_MASK = 4;
    private static final int EVENT_MASK_MASK = 8;

    /**
     * The name a generated authorization is offered under, which is the same one it was created for.
     *
     * <p><b>There is no special protocol name</b>, and two attempts to find one are why this is
     * written down: the specification's prose suggests {@code SECURITY}, and the extension's own
     * header has an {@code XSecurityAuthorizationName} of "XC-QUERY-SECURITY-1" - and a reference
     * refuses **both** outright with "Authorization protocol not supported by server". What
     * {@code SecurityGenerateAuthorization} hands a client is an ordinary credential pair: the name
     * the authorization was created for (here {@code MIT-MAGIC-COOKIE-1}) and a fresh cookie, and
     * what makes the connection it produces untrusted is the trust level stored with that
     * authorization on the server, not anything the client says.
     */
    private static final String SECURITY_AUTH = "MIT-MAGIC-COOKIE-1";

    /** The authorization's own protocol name: what the client's own credentials are made of. */
    private static final String COOKIE_PROTOCOL = "MIT-MAGIC-COOKIE-1";

    private static final String[] CORE_ERRORS = {"none", "Request", "Value", "Window", "Pixmap",
            "Atom", "Cursor", "Font", "Match", "Drawable", "Access", "Alloc", "Colormap", "GContext",
            "IDChoice", "Name", "Length", "Implementation"};

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("usage: SecurityProbe <host> <port>");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        // A reference with access control on refuses a connection that offers nothing, so the
        // credentials for this probe's own (trusted) connection can be given: `SecurityProbe host
        // port MIT-MAGIC-COOKIE-1 <hex>`.
        String authName = args.length >= 4 ? args[2] : null;
        byte[] authData = args.length >= 4 ? hex(args[3]) : null;
        try (RawXClient client = RawXClient.connect(host, port, authName, authData)) {
            int[] info = client.queryExtensionInfo("SECURITY");
            System.out.println("SECURITY present=" + info[0] + " major=" + info[1]
                    + " firstEvent=" + info[2] + " firstError=" + info[3]);
            if (info[0] == 0) {
                return;
            }
            int opcode = info[1];

            for (int[] asked : new int[][]{{1, 0}, {0, 0}, {2, 0}}) {
                client.sendRequest(queryVersion(opcode, asked[0], asked[1]));
                byte[] reply = client.readReply();
                System.out.println("  SecurityQueryVersion(" + asked[0] + "." + asked[1] + ") -> "
                        + RawXClient.readShort(reply, 8) + "." + RawXClient.readShort(reply, 10));
            }

            // An authorization a client can be given: the token comes back in the reply, and the
            // mask says which attributes travel with it.
            byte[] token = generate(client, opcode, 0, 0, null);
            System.out.println("  SecurityGenerateAuthorization(no attributes) -> "
                    + describe(token));

            // A *trusted* one: the same request with the trust level set to Trusted (0), which is
            // how a client asks for a token that carries full trust rather than the restricted kind.
            byte[] trusted = generate(client, opcode, TRUST_LEVEL_MASK, 1, new int[]{0});
            System.out.println("  SecurityGenerateAuthorization(trustLevel=Trusted) -> "
                    + describe(trusted));

            // A window of this (trusted) connection's own, for the one request that needs another
            // client's resource to be aimed at.
            int trustedWindow = client.allocateId();
            client.createWindow(trustedWindow, client.root(), 500, 500, 20, 20, 0x00FF00);
            client.flush();

            if (token == null) {
                System.out.println("  (no token was generated, so the untrusted connections cannot be made)");
                return;
            }
            // The untrusted connections: the token is offered as the SECURITY protocol's own data.
            int authId = RawXClient.readInt(token, 8);
            byte[] data = tokenData(token);
            System.out.println("  connecting with the generated token (id=" + authId + ", "
                    + data.length + " bytes):");
            int other = 0;
            try (RawXClient untrusted = RawXClient.connect(host, port, SECURITY_AUTH, data)) {
                System.out.println("    connected - the connection was accepted");
                System.out.println("    what an untrusted client may still do:");
                other = allowed(untrusted);
            } catch (java.io.IOException refused) {
                // The wording of a refusal is the server's own ("Invalid MIT-MAGIC-COOKIE-1 key"
                // against "Authentication rejected"), so only the fact of it is printed.
                System.out.println("    the connection setup was refused");
            }

            // A second such connection for the second half, so a request this probe gets wrong cannot
            // desynchronise the other half's answers - which is exactly what the first version of
            // this battery did (its KillClient was aimed at this probe's own connection).
            try (RawXClient untrusted = RawXClient.connect(host, port, SECURITY_AUTH, data)) {
                System.out.println("    what it may not do:");
                refused(client, untrusted, info, other != 0 ? other : trustedWindow);
            } catch (java.io.IOException refused) {
                // The wording of a refusal is the server's own ("Invalid MIT-MAGIC-COOKIE-1 key"
                // against "Authentication rejected"), so only the fact of it is printed.
                System.out.println("    the connection setup was refused");
            }

            // The trusted token the same client asked for: a connection made with it is trusted.
            if (trusted != null) {
                byte[] trustedData = tokenData(trusted);
                try (RawXClient connection = RawXClient.connect(host, port, SECURITY_AUTH, trustedData)) {
                    System.out.println("  connecting with the Trusted token -> accepted; "
                            + "GetImage(root) -> " + outcome(connection, getImage(connection.root())));
                } catch (java.io.IOException refused) {
                    System.out.println("  connecting with the Trusted token -> the setup was refused");
                }
            }

            // The revoke: a client that is still connected with that authorization should be told,
            // and the event is the extension's one event.
            RawXClient listener = null;
            try {
                listener = RawXClient.connect(host, port, SECURITY_AUTH, data);
            } catch (java.io.IOException refused) {
                System.out.println("  (the revoke cannot be watched: the setup was refused)");
            }

            // Revoking it: the authorization is gone, and a client that used it should hear so.
            client.sendRequest(revoke(opcode, authId));
            System.out.println("  SecurityRevokeAuthorization -> accepted");
            if (listener != null) {
                listener.readTimeoutMillis(1500);
                String told;
                try {
                    byte[] event = listener.readBytes(32);
                    told = "yes, event " + (event[0] & 0x7F) + " authId="
                            + RawXClient.readInt(event, 4) + " matches="
                            + (RawXClient.readInt(event, 4) == authId);
                } catch (java.net.SocketTimeoutException quiet) {
                    told = "no (nothing came back)";
                } catch (java.io.EOFException closed) {
                    // The connection itself ended, which is what a reference was measured doing:
                    // revoking an authorization does not merely tell the client, it closes it.
                    told = "no, and the connection was closed";
                }
                System.out.println("  a client still using it: " + told);
                listener.close();
            }
            try (RawXClient after = RawXClient.connect(host, port, SECURITY_AUTH, data)) {
                System.out.println("  connecting with the revoked token -> still accepted");
            } catch (java.io.IOException refused) {
                System.out.println("  connecting with the revoked token -> the setup was refused");
            }
        }
    }

    /**
     * What an untrusted connection may still do, which is what it is for: draw its own window, ask
     * the questions every client asks, and read its own properties.
     *
     * @return the window it made, so the refused half can aim a kill at it
     */
    private static int allowed(RawXClient client) throws Exception {
        int root = client.root();
        int window = client.allocateId();
        int gc = client.allocateId();

        attempt(client, "CreateWindow(own)", createWindow(window, root));
        attempt(client, "MapWindow(own)", request(8, window));
        attempt(client, "CreateGC(own window)", createGc(gc, window));
        attempt(client, "PolyFillRectangle(own)", fillRectangle(window, gc));
        attempt(client, "InternAtom", internAtom("PROBE"));
        int atom = lastReply == null ? 1 : RawXClient.readInt(lastReply, 8);
        attempt(client, "GetProperty(own)", getProperty(window, atom));
        attempt(client, "ChangeProperty(own)", changeProperty(window, atom));
        attempt(client, "QueryExtension", request(98, 0));
        attempt(client, "ListExtensions", new byte[]{99, 0, 1, 0});
        String visible = extensionNames(client);
        System.out.println("      an untrusted client's ListExtensions: " + visible);
        for (String name : new String[]{"SECURITY", "XTEST", "SHAPE"}) {
            System.out.println("      QueryExtension(" + name + ") -> present="
                    + (client.queryExtension(name) != 0 ? 1 : 0));
        }
        attempt(client, "GetGeometry(own)", request(14, window));
        attempt(client, "GetWindowAttributes(own)", request(3, window));
        attempt(client, "GetImage(own)", getImage(window));
        attempt(client, "GetMotionEvents(own)", motionEvents(window));
        attempt(client, "QueryPointer(own)", request(38, window));
        attempt(client, "CreatePixmap", createPixmap(client.allocateId(), window));
        return window;
    }

    /**
     * What an untrusted connection may <em>not</em> do - and each request is sent from a **trusted**
     * connection too, because an error that appears on both sides is a request this probe got wrong
     * and only a difference between the two is the trust level. The first version of this battery
     * could not tell them apart, which is how a malformed request reads as a security rule.
     */
    private static void refused(RawXClient trusted, RawXClient untrusted, int[] info,
                                int otherWindow) throws Exception {
        int root = untrusted.root();
        int security = info[1];
        int xtest = untrusted.queryExtension("XTEST");

        // The extension's own requests first, so that anything this probe gets wrong below cannot
        // change what these two say.
        pair(trusted, untrusted, "SecurityQueryVersion", queryVersion(security, 1, 0));
        pair(trusted, untrusted, "SecurityGenerateAuthorization", generateBytes(security, 0, 0, null));

        pair(trusted, untrusted, "GetImage(root)", getImage(root));
        pair(trusted, untrusted, "SetSelectionOwner(PROBE)", setSelectionOwner(root, 1));
        pair(trusted, untrusted, "ChangeProperty(root)", changeProperty(root, 1));
        pair(trusted, untrusted, "GetProperty(root, PROBE)", getProperty(root, 1));
        pair(trusted, untrusted, "GetWindowAttributes(root)", request(3, root));
        pair(trusted, untrusted, "QueryTree(root)", request(15, root));
        pair(trusted, untrusted, "GetGeometry(root)", request(14, root));
        pair(trusted, untrusted, "ListProperties(root)", new byte[]{21, 0, 2, 0, 0, 0, 0, 0});
        pair(trusted, untrusted, "ChangeWindowAttributes(root)", changeAttributes(root));
        pair(trusted, untrusted, "ConfigureWindow(root)", configureWindow(root));
        // The next two name a window of the *untrusted* connection's own, so only that connection
        // can send them: from the trusted one the same id is not a window at all, and the pair would
        // read as a security rule when it is a probe artefact.
        int own = untrusted.allocateId();
        outcome(untrusted, createWindow(own, root));
        outcome(untrusted, request(8, own));
        solo(untrusted, "SetInputFocus(own window)", setInputFocus(own));
        solo(untrusted, "DestroyWindow(own window)", request(4, own));
        pair(trusted, untrusted, "SendEvent(root)", sendEvent(root));
        pair(trusted, untrusted, "SetInputFocus(root)", setInputFocus(root));
        pair(trusted, untrusted, "KillClient(another client's window)", request(4, otherWindow));
        pair(trusted, untrusted, "GetKeyboardMapping", getKeyboardMapping());
        pair(trusted, untrusted, "GetMotionEvents(root)", motionEvents(root));
        pair(trusted, untrusted, "GrabServer", new byte[]{36, 0, 1, 0});
        pair(trusted, untrusted, "UngrabServer", new byte[]{37, 0, 1, 0});
        pair(trusted, untrusted, "ChangeKeyboardControl", new byte[]{100, 0, 2, 0, 0, 0, 0, 0});
        pair(trusted, untrusted, "SetScreenSaver", new byte[]{107, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0});
        pair(trusted, untrusted, "ChangeHosts(insert)", changeHosts());
        // SetAccessControl is deliberately **not** sent, and this is the measurement that says why:
        // it changes the server for every client, and on a reference started with `-ac` it turns
        // access control *on* - after which that Xvfb refuses every unauthenticated connection,
        // which is what the next probe in a sweep, and the next run of this one, then sees. It was
        // measured before it was removed: accepted by an `-ac` reference and refused BadAccess by
        // one with an authority file, which is the pair the F7 entry records.
        // XTEST is hidden from an untrusted client (measured above), so there is nothing to send:
        // the opcode it would use is not even a request this client can name.
        pair(trusted, untrusted, "XTestGrabControl(a hidden extension's opcode)",
                new byte[]{(byte) xtest, 4, 2, 0, 1, 0, 0, 0});
    }

    /** A request only the untrusted connection can make, because it names its own resources. */
    private static void solo(RawXClient untrusted, String name, byte[] request) throws Exception {
        System.out.println("    " + name + " -> untrusted: " + outcome(untrusted, request));
    }

    /** One request from both connections, and what each was told. */
    private static void pair(RawXClient trusted, RawXClient untrusted, String name, byte[] request)
            throws Exception {
        String asTrusted = outcome(trusted, request);
        String asUntrusted = outcome(untrusted, request);
        String note = asTrusted.equals(asUntrusted) ? "  (the same both ways)"
                : "  <- the trust level is what differs";
        System.out.println("    " + name + " -> untrusted: " + asUntrusted + ", trusted: "
                + asTrusted + note);
    }

    /**
     * The names an untrusted client's {@code ListExtensions} answers with, which is where a server
     * hides an extension from a client it does not trust: the extension is not merely refused, it is
     * not mentioned.
     */
    private static String extensionNames(RawXClient client) throws Exception {
        client.sendRequest(new byte[]{99, 0, 1, 0});
        client.flush();
        client.readTimeoutMillis(2000);
        byte[] reply = client.readReply();
        int count = reply[1] & 0xFF;
        StringBuilder names = new StringBuilder();
        int at = 32;
        for (int i = 0; i < count; i++) {
            int length = reply[at] & 0xFF;
            names.append(new String(reply, at + 1, length,
                    StandardCharsets.ISO_8859_1)).append(' ');
            at += 1 + length;
        }
        return names.toString().trim();
    }

    private static byte[] changeAttributes(int window) {
        byte[] request = new byte[12];
        request[0] = 2;                             // ChangeWindowAttributes
        RawXClient.writeShortAt(request, 2, 3);
        RawXClient.writeIntAt(request, 4, window);
        RawXClient.writeIntAt(request, 8, 0);       // no attributes: a legal no-op to ask for
        return request;
    }

    private static byte[] configureWindow(int window) {
        byte[] request = new byte[12];
        request[0] = 12;                            // ConfigureWindow
        RawXClient.writeShortAt(request, 2, 3);
        RawXClient.writeIntAt(request, 4, window);
        RawXClient.writeIntAt(request, 8, 0);       // no values
        return request;
    }

    /** A pixmap of the client's own: another thing an untrusted client is allowed to make. */
    private static byte[] createPixmap(int pixmap, int drawable) {
        byte[] request = new byte[16];
        request[0] = 53;                            // CreatePixmap
        request[1] = 24;                            // depth
        RawXClient.writeShortAt(request, 2, 4);
        RawXClient.writeIntAt(request, 4, pixmap);
        RawXClient.writeIntAt(request, 8, drawable);
        RawXClient.writeShortAt(request, 12, 20);
        RawXClient.writeShortAt(request, 14, 20);
        return request;
    }

    // ------------------------------------------------------------------
    // Requests
    // ------------------------------------------------------------------

    private static byte[] queryVersion(int opcode, int major, int minor) {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = QUERY_VERSION;
        RawXClient.writeShortAt(request, 2, 2);
        RawXClient.writeShortAt(request, 4, major);
        RawXClient.writeShortAt(request, 6, minor);
        return request;
    }

    private static byte[] generateBytes(int opcode, int mask, int values, int[] valueList) {
        byte[] name = COOKIE_PROTOCOL.getBytes(StandardCharsets.ISO_8859_1);
        byte[] data = new byte[16];
        int namePadded = (name.length + 3) & ~3;
        int dataPadded = (data.length + 3) & ~3;
        int extra = valueList == null ? 0 : valueList.length * 4;
        byte[] request = new byte[12 + namePadded + dataPadded + extra];
        request[0] = (byte) opcode;
        request[1] = GENERATE_AUTHORIZATION;
        RawXClient.writeShortAt(request, 2, request.length / 4);
        RawXClient.writeShortAt(request, 4, name.length);
        RawXClient.writeShortAt(request, 6, data.length);
        RawXClient.writeIntAt(request, 8, mask);
        System.arraycopy(name, 0, request, 12, name.length);
        System.arraycopy(data, 0, request, 12 + namePadded, data.length);
        for (int i = 0; valueList != null && i < valueList.length; i++) {
            RawXClient.writeIntAt(request, 12 + namePadded + dataPadded + i * 4, valueList[i]);
        }
        return request;
    }

    private static byte[] generate(RawXClient client, int opcode, int mask, int values,
                                   int[] valueList) throws Exception {
        client.sendRequest(generateBytes(opcode, mask, values, valueList));
        client.flush();
        client.readTimeoutMillis(2000);
        try {
            return client.readReply();
        } catch (java.io.IOException refused) {
            System.out.println("    refused: " + refused.getMessage());
            return null;
        }
    }

    private static String describe(byte[] reply) {
        if (reply == null) {
            return "refused";
        }
        int authId = RawXClient.readInt(reply, 8);
        int length = RawXClient.readShort(reply, 12);
        return "authId=" + authId + " data=" + length + " bytes";
    }

    /** The token's data, which is the reply's variable part: the data the client offers back. */
    private static byte[] tokenData(byte[] reply) {
        int length = RawXClient.readShort(reply, 12);
        byte[] data = new byte[length];
        System.arraycopy(reply, 32, data, 0, Math.min(length, reply.length - 32));
        return data;
    }

    private static byte[] revoke(int opcode, int authId) {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = REVOKE_AUTHORIZATION;
        RawXClient.writeShortAt(request, 2, 2);
        RawXClient.writeIntAt(request, 4, authId);
        return request;
    }

    private static byte[] request(int opcode, int value) {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        RawXClient.writeShortAt(request, 2, 2);
        RawXClient.writeIntAt(request, 4, value);
        return request;
    }

    private static byte[] createWindow(int window, int root) {
        byte[] request = new byte[32];
        request[0] = 1;                             // CreateWindow
        request[1] = 0;                             // depth: CopyFromParent
        RawXClient.writeShortAt(request, 2, 8);
        RawXClient.writeIntAt(request, 4, window);
        RawXClient.writeIntAt(request, 8, root);
        RawXClient.writeShortAt(request, 12, 0);
        RawXClient.writeShortAt(request, 14, 0);
        RawXClient.writeShortAt(request, 16, 40);
        RawXClient.writeShortAt(request, 18, 30);
        RawXClient.writeShortAt(request, 20, 0);    // border width
        RawXClient.writeShortAt(request, 22, 1);    // InputOutput
        RawXClient.writeIntAt(request, 24, 0);      // no attributes: background None
        return request;
    }

    private static byte[] createGc(int gc, int drawable) {
        byte[] request = new byte[16];
        request[0] = 55;                            // CreateGC
        RawXClient.writeShortAt(request, 2, 4);
        RawXClient.writeIntAt(request, 4, gc);
        RawXClient.writeIntAt(request, 8, drawable);
        RawXClient.writeIntAt(request, 12, 0);      // no attributes
        return request;
    }

    private static byte[] fillRectangle(int window, int gc) {
        byte[] request = new byte[20];
        request[0] = 70;                            // PolyFillRectangle
        RawXClient.writeShortAt(request, 2, 5);     // one rectangle: 4 + 4 + 8 bytes
        RawXClient.writeIntAt(request, 4, window);
        RawXClient.writeIntAt(request, 8, gc);
        RawXClient.writeShortAt(request, 16, 40);   // the rectangle: 40x30 at 0,0
        RawXClient.writeShortAt(request, 18, 30);
        return request;
    }

    /** GetKeyboardMapping with a sane argument: two keycodes from the first real one. */
    private static byte[] getKeyboardMapping() {
        return new byte[]{101, 0, 2, 0, 8, 2, 0, 0};
    }

    /** XTestFakeInput's body is a whole event, so a truncated one is not a smaller request. */
    private static byte[] fakeMotion(int xtest, int root) {
        byte[] request = new byte[36];
        request[0] = (byte) xtest;
        request[1] = 2;                             // FakeInput
        RawXClient.writeShortAt(request, 2, 9);
        request[4] = 6;                             // MotionNotify
        request[5] = 0;                             // absolute
        RawXClient.writeIntAt(request, 12, root);
        RawXClient.writeShortAt(request, 16, 5);    // x
        RawXClient.writeShortAt(request, 18, 5);    // y
        return request;
    }

    private static byte[] getImage(int drawable) {
        byte[] request = new byte[20];
        request[0] = 73;                            // GetImage
        request[1] = 2;                             // ZPixmap
        RawXClient.writeShortAt(request, 2, 5);
        RawXClient.writeIntAt(request, 4, drawable);
        RawXClient.writeShortAt(request, 12, 1);
        RawXClient.writeShortAt(request, 14, 1);
        RawXClient.writeIntAt(request, 16, 0xFFFFFF);
        return request;
    }

    private static byte[] copyArea(int source, int destination, int gc) {
        byte[] request = new byte[28];
        request[0] = 62;                            // CopyArea
        RawXClient.writeShortAt(request, 2, 7);
        RawXClient.writeIntAt(request, 4, source);
        RawXClient.writeIntAt(request, 8, destination);
        RawXClient.writeIntAt(request, 12, gc);
        return request;
    }

    private static byte[] motionEvents(int window) {
        byte[] request = new byte[16];
        request[0] = 39;                            // GetMotionEvents
        RawXClient.writeShortAt(request, 2, 4);
        RawXClient.writeIntAt(request, 4, window);
        RawXClient.writeIntAt(request, 8, 0);
        RawXClient.writeIntAt(request, 12, 0);
        return request;
    }

    private static byte[] getProperty(int window, int property) {
        byte[] request = new byte[24];
        request[0] = 20;                            // GetProperty
        request[1] = 1;                             // delete = true
        RawXClient.writeShortAt(request, 2, 6);
        RawXClient.writeIntAt(request, 4, window);
        RawXClient.writeIntAt(request, 8, property);
        RawXClient.writeIntAt(request, 12, 0);      // type AnyPropertyType
        RawXClient.writeIntAt(request, 16, 0);
        RawXClient.writeIntAt(request, 20, 1000);
        return request;
    }

    private static byte[] changeProperty(int window, int property) {
        byte[] value = "probe".getBytes(StandardCharsets.ISO_8859_1);
        byte[] request = new byte[24 + ((value.length + 3) & ~3)];
        request[0] = 18;                            // ChangeProperty
        request[1] = 0;                             // replace
        RawXClient.writeShortAt(request, 2, request.length / 4);
        RawXClient.writeIntAt(request, 4, window);
        RawXClient.writeIntAt(request, 8, property);
        RawXClient.writeIntAt(request, 12, 31);     // type STRING
        request[16] = 8;                            // format
        RawXClient.writeIntAt(request, 20, value.length);
        System.arraycopy(value, 0, request, 24, value.length);
        return request;
    }

    private static byte[] internAtom(String text) {
        byte[] name = text.getBytes(StandardCharsets.ISO_8859_1);
        byte[] request = new byte[8 + ((name.length + 3) & ~3)];
        request[0] = 16;                            // InternAtom
        RawXClient.writeShortAt(request, 2, request.length / 4);
        RawXClient.writeShortAt(request, 4, name.length);
        System.arraycopy(name, 0, request, 8, name.length);
        return request;
    }

    private static byte[] setInputFocus(int window) {
        byte[] request = new byte[12];
        request[0] = 42;                            // SetInputFocus
        request[1] = 1;                             // revert to PointerRoot
        RawXClient.writeShortAt(request, 2, 3);
        RawXClient.writeIntAt(request, 4, window);
        RawXClient.writeIntAt(request, 8, 0);
        return request;
    }

    private static byte[] sendEvent(int window) {
        byte[] request = new byte[44];
        request[0] = 25;                            // SendEvent
        request[1] = 0;                             // propagate
        RawXClient.writeShortAt(request, 2, 11);
        RawXClient.writeIntAt(request, 4, window);
        RawXClient.writeIntAt(request, 8, 0xFFFFFF); // event mask: everybody
        byte[] event = new byte[32];
        event[0] = 33;                              // ClientMessage
        event[1] = 32;                              // format
        RawXClient.writeIntAt(event, 4, window);
        RawXClient.writeIntAt(event, 8, 0);         // message type: None
        System.arraycopy(event, 0, request, 12, 32);
        return request;
    }

    private static byte[] setSelectionOwner(int window, int selection) {
        byte[] request = new byte[16];
        request[0] = 22;                            // SetSelectionOwner
        RawXClient.writeShortAt(request, 2, 4);
        RawXClient.writeIntAt(request, 4, window);
        RawXClient.writeIntAt(request, 8, selection);
        RawXClient.writeIntAt(request, 12, 0);
        return request;
    }

    private static byte[] hex(String text) {
        byte[] bytes = new byte[text.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(text.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }

    /**
     * ChangeHosts (109) with a real Internet address: a request a manager could actually make.
     *
     * <p>An <em>empty</em> address is what the first version sent, and a reference answers
     * {@code BadValue} for it on both connections - so the line said "refused" for a reason that had
     * nothing to do with trust, and a measurement that cannot tell those apart is not a measurement.
     */
    private static byte[] changeHosts() {
        byte[] request = new byte[12];
        request[0] = 109;
        request[1] = 0;                             // insert
        RawXClient.writeShortAt(request, 2, 3);
        request[4] = 0;                             // family: Internet
        RawXClient.writeShortAt(request, 6, 4);     // an IPv4 address, four bytes
        RawXClient.writeIntAt(request, 8, 0x7F000001);// 127.0.0.1
        return request;
    }

    /**
     * Sends one request and says what came back: {@code accepted} when the stream stays quiet (or a
     * reply arrives, whose extra data is read whole so the stream stays framed), and the error that
     * was raised when one was.
     */
    private static String outcome(RawXClient client, byte[] request) throws Exception {
        client.sendRequest(request);
        client.flush();
        client.readTimeoutMillis(400);
        byte[] message;
        try {
            message = client.readBytes(32);
        } catch (java.net.SocketTimeoutException quiet) {
            // A request with no reply and no error: the only evidence a one-way request has.
            return "accepted";
        }
        int type = message[0] & 0x7F;
        if (type == 0) {
            int code = message[1] & 0xFF;
            return "refused: " + (code < CORE_ERRORS.length ? CORE_ERRORS[code] : "error " + code);
        }
        int extra = RawXClient.readInt(message, 4) * 4;
        if (extra > 0) {
            client.readBytes(extra);
        }
        if (type == 1) {
            lastReply = message;
        }
        return "accepted";
    }

    /** The last reply an {@link #outcome} read, for the few requests whose answer is the point. */
    private static byte[] lastReply;

    private static void attempt(RawXClient client, String name, byte[] request) throws Exception {
        System.out.println("    " + name + " -> " + outcome(client, request));
    }

}
