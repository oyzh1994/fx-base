package cn.oyzh.fx.xserver.tools;

import cn.oyzh.fx.xserver.test.RawXClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Plays one side of the host clipboard bridge, so a real pasteboard can be tested from a script.
 *
 * <p>The bridge's two sides are tested against each other by the suite, with a clipboard the test
 * owns; what that cannot show is whether the platform's own pasteboard is really being read and
 * written, which is the only part a user notices. This runs against a live server and lets
 * {@code pbcopy}/{@code pbpaste} on the host be the other end:
 *
 * <pre>
 *   ClipboardProbe &lt;host&gt; &lt;port&gt; read               # what a client would paste right now
 *   ClipboardProbe &lt;host&gt; &lt;port&gt; write &lt;text&gt;      # copy from "an application"
 *   ClipboardProbe &lt;host&gt; &lt;port&gt; owner              # who owns the CLIPBOARD selection
 * </pre>
 *
 * <p>{@code write} takes the selection and answers requests for it, then stays connected until it is
 * killed: an X selection is lazy, so the data has to still be there when the host asks for it.
 */
public final class ClipboardProbe {

    private static final int SELECTION_REQUEST = 30;
    private static final int SELECTION_NOTIFY = 31;

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("usage: ClipboardProbe <host> <port> read|write <text>|owner");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String action = args[2];

        try (RawXClient client = RawXClient.connect(host, port)) {
            int clipboard = client.internAtom("CLIPBOARD");
            switch (action) {
                case "owner" -> System.out.printf("0x%x%n", client.getSelectionOwner(clipboard));
                case "read" -> System.out.println(read(client, clipboard));
                case "write" -> {
                    String text = args.length > 3 ? args[3] : "";
                    serve(client, clipboard, text);
                }
                default -> System.out.println("unknown action " + action);
            }
        }
    }

    /** What a client gets if it pastes right now. */
    private static String read(RawXClient client, int clipboard) throws IOException {
        int property = client.internAtom("XSERVER_PROBE_DATA");
        int window = client.allocateId();
        client.createWindow(window, client.root(), 0, 0, 100, 100);
        client.convertSelection(window, clipboard, client.internAtom("UTF8_STRING"), property, 0);
        client.flush();

        client.readTimeoutMillis(3000);
        byte[] notify = client.readEvent(SELECTION_NOTIFY);
        if (notify == null) {
            return "<no answer: nobody owns the selection>";
        }
        int answered = RawXClient.readInt(notify, 20);
        if (answered == 0) {
            return "<refused>";
        }
        byte[] reply = client.getProperty(window, property, 0, true);
        int length = RawXClient.readInt(reply, 16) * (reply[1] & 0xFF) / 8;
        return new String(reply, 32, length, StandardCharsets.UTF_8);
    }

    /** Takes the selection and answers requests for it, until the process is killed. */
    private static void serve(RawXClient client, int clipboard, String text) throws IOException {
        int window = client.allocateId();
        client.createWindow(window, client.root(), 0, 0, 100, 100);
        client.setSelectionOwner(window, clipboard, 0);
        client.flush();
        System.out.printf("0x%x now owns CLIPBOARD with %d characters%n", window,
                text.length());
        System.out.flush();
        while (true) {
            client.readTimeoutMillis(60000);
            byte[] event;
            try {
                event = client.readEvent(SELECTION_REQUEST);
            } catch (java.net.SocketTimeoutException quiet) {
                continue;
            }
            if (event == null) {
                continue;
            }
            if (RawXClient.readInt(event, 16) != clipboard) {
                continue;
            }
            int requestor = RawXClient.readInt(event, 12);
            int target = RawXClient.readInt(event, 20);
            int property = RawXClient.readInt(event, 24);
            int utf8 = client.internAtom("UTF8_STRING");
            int answered = property;
            if (property == 0) {
                answered = 0;
            } else if (target == utf8) {
                client.setStringProperty(requestor, property, target, text);
                client.flush();
            } else if (target == client.internAtom("TARGETS")) {
                // TARGETS first, then the data, is how a real client reads a selection - and a real
                // owner that refuses TARGETS is one a toolkit cannot use at all, which is what this
                // probe was until a toolkit pointed it out.
                client.setCardinalProperty(requestor, property, client.internAtom("ATOM"),
                        client.internAtom("TARGETS"), utf8, client.internAtom("STRING"),
                        client.internAtom("TEXT"));
                client.flush();
            } else {
                answered = 0;
            }
            client.sendSelectionNotify(requestor, 0, requestor, clipboard, target, answered);
            client.flush();
        }
    }

    private ClipboardProbe() {
    }
}
