package cn.oyzh.fx.xserver.tools;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A byte-level X11 proxy that logs the protocol both ways, so a real client's request and event
 * streams can be read off a real server that cannot log them itself.
 *
 * <p>Usage: {@code XProxy listenPort targetHost targetPort logFile [rawFile]}. Point the client at
 * {@code DISPLAY=<host>:<listenPort - 6000>} and read the log. This is the only way to answer "does
 * the client send different requests to Xvfb than this server" - the question behind several
 * backlog items (A5, A4, B2) - because it puts the *reference* server's own traffic in a file.
 *
 * <p>Only enough of the framing is understood to walk the streams: a request's length is in bytes
 * 2..3 (in four byte units), a reply's extra data is in bytes 4..7, and everything else is a fixed
 * thirty two byte event or error. The one-line-per-message log carries no bodies; when the *bytes*
 * are what is being hunted, pass a fifth argument and every message, whole, is written there as
 * {@code <direction> <message hex>} - which is how B1's `GetKbdByName` reply was read off a
 * reference server rather than derived from a header (the same reason `XkbProbe` prints bytes).
 *
 * <p><b>Reconstructed 2026-09-24</b> after the original was lost with `/tmp`: the framing, the setup
 * exchange, the BigRequests handling and the request descriptions are the original's (they were read
 * back from a session log), but its opcode/event name table was longer - here anything not in the
 * short table prints as {@code op-<n>} (and every extension request carries its minor opcode, which is
 * the only label two servers' extension traffic can be compared by). The behaviour that matters (walk the byte stream, log one
 * line per message) is unchanged.
 */
public final class XProxy {

    static OutputStream log;
    static OutputStream raw;
    static final AtomicLong started = new AtomicLong();
    /**
     * Which connection a line belongs to.
     *
     * <p><b>Every line carries it, and that is not cosmetic.</b> A client may open several X
     * connections, and the two logs are written from all of them at once: without an id, messages
     * from different streams interleave and any reader that assumes one stream - a framer checking a
     * message against its own length field, say - reports a desync that is not there. Measured
     * 2026-09-27 the hard way: a "S2C misframe" that turned out to be the *other* connection's setup
     * reply arriving in the middle of the first one's stream.
     */
    static final AtomicLong connections = new AtomicLong();

    public static void main(String[] args) throws Exception {
        int listenPort = Integer.parseInt(args[0]);
        String targetHost = args[1];
        int targetPort = Integer.parseInt(args[2]);
        Path logPath = Path.of(args[3]);
        log = Files.newOutputStream(logPath);
        if (args.length > 4) {
            raw = Files.newOutputStream(Path.of(args[4]));
        }
        started.set(System.currentTimeMillis());
        try (ServerSocket server = new ServerSocket(listenPort)) {
            System.out.println("proxy on " + listenPort + " -> " + targetHost + ":" + targetPort);
            while (true) {
                Socket client = server.accept();
                Socket target = new Socket(targetHost, targetPort);
                System.out.println("client connected");
                String id = "c" + connections.incrementAndGet();
                Thread toServer = new Thread(() -> pump(client, target, true, id));
                Thread toClient = new Thread(() -> pump(target, client, false, id));
                toServer.start();
                toClient.start();
            }
        }
    }

    static void pump(Socket from, Socket to, boolean clientToServer, String id) {
        try (InputStream in = from.getInputStream(); OutputStream out = to.getOutputStream()) {
            boolean first = true;
            while (true) {
                if (first) {
                    // The setup exchange is framed differently from everything after it: the
                    // request is twelve bytes with no length field, and the reply counts its extra
                    // data in a sixteen bit field.
                    first = false;
                    byte[] setup = readLogged(in, clientToServer ? 12 : 8, "setup");
                    if (setup == null) {
                        return;
                    }
                    int extra = 0;
                    if (clientToServer) {
                        logSetup(setup);
                    } else {
                        int length = (setup[6] & 0xFF) | ((setup[7] & 0xFF) << 8);
                        extra = Math.max(0, length * 4);
                    }
                    byte[] body = extra == 0 ? new byte[0] : readLogged(in, extra, "setup body");
                    if (body == null) {
                        return;
                    }
                    byte[] whole = new byte[setup.length + extra];
                    System.arraycopy(setup, 0, whole, 0, setup.length);
                    System.arraycopy(body, 0, whole, setup.length, extra);
                    dump(whole, clientToServer, id);
                    out.write(whole);
                    out.flush();
                    continue;
                }
                // **A request is not always eight bytes.** The head is read as four - the opcode, the
                // data byte and the length - and the rest follows from that length, because a request
                // whose length field says one unit is exactly those four bytes. That is not a corner
                // case: it is `BIG-REQUESTS`' `Enable`, which libxcb sends on *every* connection as
                // the third request, so a proxy that assumes eight desynchronises one message in and
                // hangs the client with no error on either side - which is what `XProxy` did to every
                // GTK client, and what the kit's notes recorded as "EasyShell hangs through it, the
                // reason was never found" (2026-09-27).
                byte[] head = readFully(in, 4);
                if (head == null) {
                    System.out.println((clientToServer ? "C>S" : "S>C") + " end of stream");
                    return;
                }
                int whole4 = clientToServer ? requestLength(head) : 0;
                byte[] header;
                // The four byte case is a *request* only: everything a server sends is a reply, an
                // error or an event, and all three have the full header.
                if (clientToServer && whole4 == 4) {
                    header = head;
                } else {
                    byte[] rest = readFully(in, 8 - 4);
                    if (rest == null) {
                        return;
                    }
                    header = new byte[8];
                    System.arraycopy(head, 0, header, 0, 4);
                    System.arraycopy(rest, 0, header, 4, 4);
                }
                // An extension request carries its own minor opcode in the second byte, and without
                // it two servers' extension traffic cannot be compared at all - every one of them
                // prints as the same `op-<major>` line. Which extensions a server numbers where also
                // differs, so the minor is the only common label.
                String minor = clientToServer && (header[0] & 0xFF) >= 128
                        ? " minor=" + (header[1] & 0xFF) : "";
                System.out.println((clientToServer ? "C>S" : "S>C") + " op=" + (header[0] & 0xFF)
                        + " b2b3=" + ((head[2] & 0xFF) | ((head[3] & 0xFF) << 8))
                        + (header.length > 4 ? " b4b7=" + card32(header, 4) : "")
                        + minor);
                int extra = 0;
                if (clientToServer) {
                    if (whole4 == 0) {
                        // BigRequests' extended form - see requestLength for what triggers it.
                        byte[] extended = readFully(in, 4);
                        if (extended == null) {
                            return;
                        }
                        int bigUnits = card32(extended, 0);
                        int body = Math.max(0, bigUnits * 4 - 8);
                        byte[] whole = new byte[8 + body];
                        System.arraycopy(header, 0, whole, 0, 8);
                        byte[] rest = body == 0 ? new byte[0] : readFully(in, body);
                        if (rest == null) {
                            return;
                        }
                        System.arraycopy(rest, 0, whole, 8, body);
                        dump(whole, clientToServer, id);
                        out.write(whole);
                        out.flush();
                        continue;
                    }
                    extra = Math.max(0, whole4 - 8);
                } else {
                    extra = serverMessageLength(header) - 8;
                }
                byte[] body = extra == 0 ? new byte[0] : readFully(in, extra);
                if (body == null) {
                    return;
                }
                byte[] whole = new byte[header.length + extra];
                System.arraycopy(header, 0, whole, 0, header.length);
                System.arraycopy(body, 0, whole, header.length, extra);
                describe(whole, clientToServer, id);
                dump(whole, clientToServer, id);
                out.write(whole);
                out.flush();
            }
        } catch (Exception e) {
            System.out.println((clientToServer ? "C>S" : "S>C") + " pump ended: " + e);
        }
    }

    /** Every message, whole, when the bytes are what is being hunted - a reply's shape, say. */
    static synchronized void dump(byte[] message, boolean clientToServer, String id) {
        if (raw == null) {
            return;
        }
        StringBuilder hex = new StringBuilder(message.length * 3);
        for (byte b : message) {
            hex.append(String.format("%02x", b & 0xFF));
        }
        try {
            raw.write((id + " " + (clientToServer ? "C>S " : "S>C ")).getBytes());
            raw.write(hex.toString().getBytes());
            raw.write('\n');
            raw.flush();
        } catch (Exception e) {
            // best effort
        }
    }

    static synchronized void logSetup(byte[] message) {
        try {
            log.write(String.format("%6d C>S setup byteOrder=%c protocol=%d.%d%n",
                    System.currentTimeMillis() - started.get(), (char) message[0],
                    message[2] & 0xFF, message[4] & 0xFF).getBytes());
            log.flush();
        } catch (Exception e) {
            // best effort
        }
    }

    static synchronized void describe(byte[] message, boolean clientToServer, String id) {
        StringBuilder line = new StringBuilder();
        line.append(String.format("%6d %s %s ", System.currentTimeMillis() - started.get(),
                id, clientToServer ? "C>S" : "S>C"));
        int type = message[0] & 0xFF;
        if (clientToServer) {
            line.append(String.format("req %-3d %-24s", type, name(type)));
            if (type >= 128 && message.length > 1) {
                // An extension request's minor opcode: the only label two servers' extension
                // traffic can be compared by, since they number the extensions differently.
                line.append(String.format(" minor=%-3d", message[1] & 0xFF));
            }
            if (message.length >= 12) {
                line.append(" win=0x").append(Integer.toHexString(card32(message, 4)));
            }
            if (message.length >= 28 && type == 26) {                   // GrabPointer
                line.append(" ownerEvents=").append(message[9] & 0xFF)
                        .append(" mask=0x").append(Integer.toHexString(
                                (message[10] & 0xFF) | ((message[11] & 0xFF) << 8)))
                        .append(" pointerMode=").append(message[12] & 0xFF);
            }
            if (type == 12 && message.length >= 20) {                   // ConfigureWindow
                int mask = (message[8] & 0xFF) | ((message[9] & 0xFF) << 8);
                line.append(" mask=0x").append(Integer.toHexString(mask));
                int at = 12;
                if ((mask & 1) != 0) {
                    line.append(" x=").append(card16(message, at));
                }
                if ((mask & 2) != 0) {
                    line.append(" y=").append(card16(message, at));
                }
                if ((mask & 4) != 0) {
                    line.append(" w=").append(card16(message, at));
                }
                if ((mask & 8) != 0) {
                    line.append(" h=").append(card16(message, at));
                }
                if ((mask & 0x10) != 0) {
                    line.append(" border=").append(card16(message, at));
                }
            }
            if (type == 1 && message.length >= 40) {                    // CreateWindow
                line.append(" parent=0x").append(Integer.toHexString(card32(message, 8)))
                        .append(" ").append(card16(message, 16)).append("x")
                        .append(card16(message, 18));
            }
        } else if (type == 1) {
            line.append(String.format("reply      %-20s", ""));
        } else if (type == 0) {
            line.append(String.format("error %-18s", "code=" + (message[1] & 0xFF)
                    + " request=" + (message[10] & 0xFF)));
        } else {
            line.append(String.format("evt %-3d %-20s", type, name(type)));
            if (message.length >= 32) {
                line.append(" win=0x").append(Integer.toHexString(card32(message, 12)));
                if (type == 7 || type == 8) {                           // EnterNotify/LeaveNotify
                    line.append(" mode=").append(message[30] & 0xFF)
                            .append(" detail=").append(message[1] & 0xFF)
                            .append(" at (").append((short) card16(message, 24)).append(",")
                            .append((short) card16(message, 26)).append(")");
                }
                if (type == 4 || type == 5) {                           // ButtonPress/Release
                    line.append(" button=").append(message[1] & 0xFF)
                            .append(" at (").append((short) card16(message, 24)).append(",")
                            .append((short) card16(message, 26)).append(")");
                }
            }
        }
        line.append('\n');
        try {
            log.write(line.toString().getBytes());
            log.flush();
        } catch (Exception e) {
            // Logging is best effort; the protocol stream must not depend on it.
        }
    }

    /** The names worth having in the log; anything else prints as {@code op-<n>}. */
    static String name(int opcode) {
        return switch (opcode) {
            case 1 -> "CreateWindow";
            case 2 -> "ChangeWindowAttributes";
            case 3 -> "GetWindowAttributes";
            case 4 -> "DestroyWindow";
            case 8 -> "MapWindow";
            case 10 -> "UnmapWindow";
            case 11 -> "UnmapSubwindows";
            case 12 -> "ConfigureWindow";
            case 13 -> "CirculateWindow";
            case 14 -> "GetGeometry";
            case 15 -> "QueryTree";
            case 16 -> "InternAtom";
            case 18 -> "ChangeProperty";
            case 19 -> "DeleteProperty";
            case 20 -> "GetProperty";
            case 22 -> "SetSelectionOwner";
            case 23 -> "GetSelectionOwner";
            case 24 -> "ConvertSelection";
            case 25 -> "SendEvent";
            case 26 -> "GrabPointer";
            case 27 -> "UngrabPointer";
            case 28 -> "GrabButton";
            // **The input range is off by one in the numbering people quote from memory**: 33 is
            // `GrabKey` (not `GrabKeyboard`), 36 is `GrabServer` (not 37), and 119 is
            // `GetModifierMapping`. Read off the protocol's own list rather than reconstructed: a
            // wrong name here reads as a wrong measurement, which is how an hour went on
            // 2026-09-27 comparing a profile that named the wrong requests.
            case 33 -> "GrabKey";
            case 34 -> "UngrabKey";
            case 35 -> "AllowEvents";
            case 36 -> "GrabServer";
            case 37 -> "UngrabServer";
            case 38 -> "QueryPointer";
            case 39 -> "GetMotionEvents";
            case 40 -> "TranslateCoordinates";
            case 41 -> "WarpPointer";
            case 42 -> "SetInputFocus";
            case 43 -> "GetInputFocus";
            case 44 -> "QueryKeymap";
            case 45 -> "OpenFont";
            case 47 -> "QueryFont";
            case 78 -> "CreateColormap";
            case 79 -> "FreeColormap";
            case 84 -> "AllocColor";
            case 88 -> "FreeColors";
            case 91 -> "QueryColors";
            case 93 -> "CreateCursor";
            case 94 -> "CreateGlyphCursor";
            case 95 -> "FreeCursor";
            case 100 -> "ChangeKeyboardMapping";
            case 102 -> "ChangeKeyboardControl";
            case 103 -> "GetKeyboardControl";
            case 105 -> "ChangePointerControl";
            case 106 -> "GetPointerControl";
            case 112 -> "SetCloseDownMode";
            case 113 -> "KillClient";
            case 116 -> "SetPointerMapping";
            case 117 -> "GetPointerMapping";
            case 118 -> "SetModifierMapping";
            case 119 -> "GetModifierMapping";
            case 53 -> "CreatePixmap";
            case 54 -> "FreePixmap";
            case 55 -> "CreateGC";
            case 56 -> "ChangeGC";
            case 60 -> "FreeGC";
            case 62 -> "CopyArea";
            case 63 -> "CopyPlane";
            case 64 -> "PolyPoint";
            case 65 -> "PolyLine";
            case 66 -> "PolySegment";
            case 67 -> "PolyRectangle";
            case 68 -> "PolyArc";
            case 69 -> "FillPoly";
            case 70 -> "PolyFillRectangle";
            case 71 -> "PolyFillArc";
            case 72 -> "PutImage";
            case 73 -> "GetImage";
            case 74 -> "PolyText8";
            case 75 -> "PolyText16";
            case 76 -> "ImageText8";
            case 77 -> "ImageText16";
            case 98 -> "QueryExtension";
            case 99 -> "ListExtensions";
            case 101 -> "GetKeyboardMapping";
            case 128 -> "BIG-REQUESTS Enable";
            case 139 -> "RENDER";
            default -> "op-" + opcode;
        };
    }

    static int card16(byte[] data, int offset) {
        return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8);
    }

    static int card32(byte[] data, int offset) {
        return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8)
                | ((data[offset + 2] & 0xFF) << 16) | ((data[offset + 3] & 0xFF) << 24);
    }

    /**
     * How many bytes a *request* occupies, from its first four, or **0 when the answer is four bytes
     * further on** - the BIG-REQUESTS form, whose length is the first word of the body.
     *
     * <p>Three silent bugs lived here, and each is one of these lines: a request is **not always
     * eight bytes** (a length field of one unit is exactly the four bytes already read - that is
     * `BIG-REQUESTS`' `Enable`, which libxcb sends on every connection, and assuming eight
     * desynchronised the whole stream and hung the client), and a length field of **zero means read
     * on**, not "empty".
     */
    static int requestLength(byte[] head) {
        int units = (head[2] & 0xFF) | ((head[3] & 0xFF) << 8);
        return units == 0 ? 0 : units * 4;
    }

    /**
     * How many bytes a *server's* message occupies, from its first eight.
     *
     * <p>A reply carries a length, and so does a **generic event** (type 35) - everything else is a
     * fixed thirty two bytes. Leaving the generic event out is the bug that hid longest: XI2's events
     * are generic events, GDK's input path is XI2, and a proxy that forwarded the first thirty two
     * bytes of each left the rest in the stream, so the client desynchronised at its first device
     * event and stalled before drawing - recorded for months as "EasyShell hangs through `XProxy`,
     * the reason was never found".
     */
    static int serverMessageLength(byte[] head) {
        int type = head[0] & 0x7F;
        return type == 1 || type == 35 ? 32 + card32(head, 4) * 4 : 32;
    }

    static byte[] readFully(InputStream in, int count) throws Exception {
        byte[] data = new byte[count];
        int read = 0;
        while (read < count) {
            int n = in.read(data, read, count - read);
            if (n < 0) {
                return null;
            }
            read += n;
        }
        return data;
    }

    /** Reads and hex-dumps, for the setup exchange a reader may want to see raw. */
    static byte[] readLogged(InputStream in, int count, String tag) throws Exception {
        byte[] data = readFully(in, count);
        if (data == null) {
            return null;
        }
        StringBuilder hex = new StringBuilder();
        for (byte b : data) {
            hex.append(String.format("%02x", b));
        }
        synchronized (XProxy.class) {
            log.write(String.format("%6d %s %d bytes %s%n", System.currentTimeMillis() - started.get(),
                    tag, count, hex).getBytes());
            log.flush();
        }
        return data;
    }
}
