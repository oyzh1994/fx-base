package cn.oyzh.fx.xserver.tools;

import cn.oyzh.fx.xserver.test.RawXClient;

import java.nio.charset.StandardCharsets;

/**
 * XVideo (Xv) as a client uses it - the harness for F6.
 *
 * <p>XVideo is the extension a video player hands frames to: it asks for the adaptors the server has,
 * their ports and the encodings each port can be fed, then uses a port to put a frame - from client
 * memory with {@code PutImage}, or from a drawable with {@code PutVideo}/{@code PutStill} - into a
 * window, scaled by whatever the hardware can do. That "whatever the hardware can do" is the whole
 * question for this server: there is none behind the host window, so what matters is what the
 * extension *says* it can do, and whether a player that asks gets an answer it can act on.
 *
 * <p>So the probe walks the whole query surface - the adaptors, their ports, the encodings, the port
 * attributes, the image formats and the image attributes - and then tries the port itself: grab,
 * attribute, put, stop, ungrab. Every field is printed whether or not it came back, because "this
 * server has no adaptors" and "this server refused the request" are different findings and a player
 * treats them differently (the first is a fallback, the second is a failure).
 *
 * <p>There is no generated protocol module for Xv in the x11 library, so the requests here are built
 * by hand from {@code X11/extensions/Xvproto.h} - which is what a client compiles against, and what
 * `tools/xv-probe` had to check rather than trust the specification's prose.
 *
 * <p>Usage: {@code XvProbe &lt;host&gt; &lt;port&gt;}.
 */
public final class XvProbe {

    /** Xv's own requests, in the protocol's order. */
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

    /** The colours that tell a video frame from a window's own background. */
    private static final int FRAME = 0x00FFFF;              // the frame the probe paints
    private static final int PLANES = 0xFFFFFF;

    private static final String[] CORE_ERRORS = {"none", "Request", "Value", "Window", "Pixmap",
            "Atom", "Cursor", "Font", "Match", "Drawable", "Access", "Alloc", "Colormap", "GContext",
            "IDChoice", "Name", "Length", "Implementation"};

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("usage: XvProbe <host> <port>");
            return;
        }
        try (RawXClient client = RawXClient.connect(args[0], Integer.parseInt(args[1]))) {
            int[] info = client.queryExtensionInfo("XVideo");
            System.out.println("XVideo present=" + info[0] + " major=" + info[1]
                    + " firstEvent=" + info[2] + " firstError=" + info[3]);
            if (info[0] == 0) {
                return;
            }
            int opcode = info[1];

            // Xv has a QueryExtension of its own, whose reply carries a version and a revision - the
            // *extension's* version, which is not the X protocol version the core query answers.
            client.sendRequest(header(opcode, QUERY_EXTENSION, 1));
            byte[] extension = client.readReply();
            System.out.println("  Xv QueryExtension -> " + RawXClient.readShort(extension, 8) + "."
                    + RawXClient.readShort(extension, 10));

            int root = client.root();
            client.sendRequest(request(opcode, QUERY_ADAPTORS, 2, root));
            byte[] adaptors = client.readReply();
            int count = RawXClient.readInt(adaptors, 8);
            System.out.println("  QueryAdaptors(root) -> " + count + " adaptor(s)");
            int port = 0;
            for (int i = 0, at = 32; i < count; i++) {
                int base = RawXClient.readInt(adaptors, at);
                int nameSize = RawXClient.readShort(adaptors, at + 4);
                int ports = RawXClient.readShort(adaptors, at + 6);
                int formats = RawXClient.readShort(adaptors, at + 8);
                String name = new String(adaptors, at + 12, nameSize, StandardCharsets.ISO_8859_1);
                System.out.println("    adaptor[" + i + "] base=0x" + Integer.toHexString(base)
                        + " name=\"" + name + "\" ports=" + ports + " formats=" + formats);
                // The formats follow the name, eight bytes each: visual and depth.
                for (int f = 0; f < formats; f++) {
                    int fat = at + 12 + nameSize + f * 8;
                    System.out.println("      format visual=0x"
                            + Integer.toHexString(RawXClient.readInt(adaptors, fat))
                            + " depth=" + (adaptors[fat + 4] & 0xFF));
                }
                at += 12 + nameSize + formats * 8;
                if (port == 0) {
                    port = base;
                }
            }

            // Every request that names a port, sent with one that cannot be a port. With no adaptors
            // there is no valid port at all, so this is what a client sees when it asks anyway - and
            // the answer has to be an error it can act on rather than silence or a crash.
            int unusable = client.allocateId();
            // A GC and a drawable of this client's own, so the port is the *only* thing wrong with a
            // request that names one: a reference checks the GC and the drawable first, and answers
            // BadGContext for a GC of None - which says nothing about the port at all (measured).
            int gc = client.allocateId();
            client.createGC(gc, root, 0);
            client.flush();
            System.out.println("  the requests that name a port, with 0x"
                    + Integer.toHexString(unusable) + " (no adaptor means no port is valid):");
            attempt(client, info, "QueryEncodings", request(opcode, QUERY_ENCODINGS, 2, unusable));
            attempt(client, info, "GrabPort", grabPort(opcode, unusable));
            attempt(client, info, "UngrabPort", grabPort(opcode, unusable));
            attempt(client, info, "PutVideo", put(opcode, PUT_VIDEO, unusable, client, root, gc));
            attempt(client, info, "PutStill", put(opcode, PUT_STILL, unusable, client, root, gc));
            attempt(client, info, "GetVideo", put(opcode, GET_VIDEO, unusable, client, root, gc));
            attempt(client, info, "GetStill", put(opcode, GET_STILL, unusable, client, root, gc));
            attempt(client, info, "StopVideo", stopVideo(opcode, unusable, root));
            byte[] selectVideo = header(opcode, SELECT_VIDEO_NOTIFY, 3);
            RawXClient.writeIntAt(selectVideo, 4, root);          // a real drawable, not None
            selectVideo[8] = 1;                                   // on
            attempt(client, info, "SelectVideoNotify(root)", selectVideo);
            byte[] selectPort = header(opcode, SELECT_PORT_NOTIFY, 3);
            RawXClient.writeIntAt(selectPort, 4, unusable);
            selectPort[8] = 1;                                    // on
            attempt(client, info, "SelectPortNotify", selectPort);
            attempt(client, info, "QueryBestSize", queryBestSize(opcode, unusable));
            attempt(client, info, "SetPortAttribute", setPortAttribute(opcode, unusable));
            attempt(client, info, "GetPortAttribute", getPortAttribute(opcode, unusable));
            attempt(client, info, "QueryPortAttributes",
                    request(opcode, QUERY_PORT_ATTRIBUTES, 2, unusable));
            attempt(client, info, "ListImageFormats",
                    request(opcode, LIST_IMAGE_FORMATS, 2, unusable));
            attempt(client, info, "QueryImageAttributes",
                    queryImageAttributes(opcode, unusable, 0x3, 64, 48));
            attempt(client, info, "PutImage", putImage(opcode, unusable, client, root, gc));
            attempt(client, info, "ShmPutImage", shmPutImage(opcode, unusable, client, root, gc));
        }
    }

    /**
     * Sends one request and answers what came back, which for a port request with no port is an
     * error: {@code BadPort} is the extension's own (at its error base), and a *core* error would
     * say something different about the same request.
     */
    private static void attempt(RawXClient client, int[] info, String name, byte[] request)
            throws Exception {
        client.sendRequest(request);
        client.flush();
        client.readTimeoutMillis(400);
        byte[] message;
        try {
            message = client.readBytes(32);
        } catch (java.net.SocketTimeoutException quiet) {
            System.out.println("    " + name + " -> accepted (nothing came back)");
            return;
        }
        int type = message[0] & 0x7F;
        if (type == 0) {
            int code = message[1] & 0xFF;
            String what = code >= info[3] && info[3] != 0
                    ? "extension error +" + (code - info[3])
                    : (code < CORE_ERRORS.length ? CORE_ERRORS[code] : "error " + code);
            System.out.println("    " + name + " -> refused: " + what);
            return;
        }
        int extra = RawXClient.readInt(message, 4) * 4;
        if (extra > 0) {
            client.readBytes(extra);
        }
        System.out.println("    " + name + " -> answered ("
                + (type == 1 ? "a reply" : "a message of type " + type) + ")");
    }

    private static byte[] grabPort(int opcode, int port) {
        byte[] request = header(opcode, GRAB_PORT, 3);
        RawXClient.writeIntAt(request, 4, port);
        RawXClient.writeIntAt(request, 8, 0);           // CurrentTime
        return request;
    }

    private static byte[] stopVideo(int opcode, int port, int drawable) {
        byte[] request = header(opcode, STOP_VIDEO, 3);
        RawXClient.writeIntAt(request, 4, port);
        RawXClient.writeIntAt(request, 8, drawable);
        return request;
    }

    /** PutVideo, PutStill, GetVideo and GetStill share one shape: a port, a drawable and a rect. */
    private static byte[] put(int opcode, int minor, int port, RawXClient client, int drawable,
                              int gc) {
        byte[] request = header(opcode, minor, 8);
        RawXClient.writeIntAt(request, 4, port);
        RawXClient.writeIntAt(request, 8, drawable);
        RawXClient.writeIntAt(request, 12, gc);
        RawXClient.writeShortAt(request, 16, 0);        // vid_x
        RawXClient.writeShortAt(request, 18, 0);        // vid_y
        RawXClient.writeShortAt(request, 20, 64);       // vid_w
        RawXClient.writeShortAt(request, 22, 48);       // vid_h
        RawXClient.writeShortAt(request, 24, 0);        // drw_x
        RawXClient.writeShortAt(request, 26, 0);        // drw_y
        RawXClient.writeShortAt(request, 28, 64);       // drw_w
        RawXClient.writeShortAt(request, 30, 48);       // drw_h
        return request;
    }

    private static byte[] queryBestSize(int opcode, int port) {
        byte[] request = header(opcode, QUERY_BEST_SIZE, 5);
        RawXClient.writeIntAt(request, 4, port);
        RawXClient.writeShortAt(request, 8, 64);        // vid_w
        RawXClient.writeShortAt(request, 10, 48);       // vid_h
        RawXClient.writeShortAt(request, 12, 128);      // drw_w
        RawXClient.writeShortAt(request, 14, 96);       // drw_h
        request[16] = 0;                                // motion: still
        return request;
    }

    private static byte[] setPortAttribute(int opcode, int port) {
        byte[] request = header(opcode, SET_PORT_ATTRIBUTE, 4);
        RawXClient.writeIntAt(request, 4, port);
        RawXClient.writeIntAt(request, 8, 1);           // an atom id: the server resolves it
        RawXClient.writeIntAt(request, 12, 0);          // the value
        return request;
    }

    private static byte[] getPortAttribute(int opcode, int port) {
        byte[] request = header(opcode, GET_PORT_ATTRIBUTE, 3);
        RawXClient.writeIntAt(request, 4, port);
        RawXClient.writeIntAt(request, 8, 1);
        return request;
    }

    private static byte[] putImage(int opcode, int port, RawXClient client, int drawable, int gc) {
        byte[] request = header(opcode, PUT_IMAGE, 10);
        RawXClient.writeIntAt(request, 4, port);
        RawXClient.writeIntAt(request, 8, drawable);
        RawXClient.writeIntAt(request, 12, gc);
        RawXClient.writeIntAt(request, 16, 0x3);        // the image format
        RawXClient.writeShortAt(request, 32, 64);       // width
        RawXClient.writeShortAt(request, 34, 48);       // height
        return request;
    }

    private static byte[] shmPutImage(int opcode, int port, RawXClient client, int drawable, int gc) {
        byte[] request = header(opcode, SHM_PUT_IMAGE, 13);
        RawXClient.writeIntAt(request, 4, port);
        RawXClient.writeIntAt(request, 8, drawable);
        RawXClient.writeIntAt(request, 12, gc);
        RawXClient.writeIntAt(request, 16, 0);          // shmseg
        RawXClient.writeIntAt(request, 20, 0x3);        // the image format
        RawXClient.writeIntAt(request, 24, 0);          // offset
        RawXClient.writeShortAt(request, 40, 64);       // width
        RawXClient.writeShortAt(request, 42, 48);       // height
        return request;
    }

    // ------------------------------------------------------------------
    // Requests
    // ------------------------------------------------------------------

    private static byte[] header(int opcode, int minor, int units) {
        byte[] request = new byte[units * 4];
        request[0] = (byte) opcode;
        request[1] = (byte) minor;
        RawXClient.writeShortAt(request, 2, units);
        return request;
    }

    private static byte[] request(int opcode, int minor, int units, int value) {
        byte[] request = header(opcode, minor, units);
        RawXClient.writeIntAt(request, 4, value);
        return request;
    }

    private static byte[] queryImageAttributes(int opcode, int port, int id, int width, int height) {
        byte[] request = header(opcode, QUERY_IMAGE_ATTRIBUTES, 4);
        RawXClient.writeIntAt(request, 4, port);
        RawXClient.writeIntAt(request, 8, id);
        RawXClient.writeShortAt(request, 12, width);
        RawXClient.writeShortAt(request, 14, height);
        return request;
    }

    private static String atomName(RawXClient client, int index, byte[] reply) {
        // The names are all in one block after the fixed part; the probe prints the block once.
        int bytes = RawXClient.readInt(reply, 12);
        if (bytes <= 0) {
            return "(none)";
        }
        int at = 32 + RawXClient.readInt(reply, 8) * 16;
        String all = new String(reply, at, Math.min(bytes, reply.length - at),
                StandardCharsets.ISO_8859_1);
        return index == 0 ? all : "...";
    }
}
