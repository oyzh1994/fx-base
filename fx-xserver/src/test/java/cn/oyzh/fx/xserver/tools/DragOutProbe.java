package cn.oyzh.fx.xserver.tools;

import cn.oyzh.fx.xserver.test.RawXClient;

import java.io.IOException;

/**
 * A forwarded application that is dragging files *out* of itself - the harness for C5's other half.
 *
 * <p>X gives no "I am about to drag" message, so what a server can see is the one thing a client does
 * when it decides to drag: **it takes the {@code XdndSelection}**. This probe does exactly that, and
 * then answers the {@code text/uri-list} request the server makes - which is the whole of the X side
 * of dragging out, and the half a platform cannot produce in-process.
 *
 * <p>It exists because the server's half cannot be driven from the real clients: whether a forwarded
 * application offers a drag is up to the application, and the two on this bed (EasyShell, termora)
 * may or may not. A probe that decides to drag is what makes the host's side measurable.
 *
 * <pre>
 *   DragOutProbe &lt;host&gt; &lt;port&gt; [path ...]      # takes the drag selection and offers these files
 * </pre>
 *
 * <p>It stays connected and keeps the selection until it is killed, which is what a drag in progress
 * looks like. The drag itself - the gesture, and the platform session it starts - is the part a
 * script drives from outside.
 */
public final class DragOutProbe {

    private static final String XDND_SELECTION = "XdndSelection";
    private static final String URI_LIST = "text/uri-list";

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("usage: DragOutProbe <host> <port> [path ...]");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        StringBuilder offered = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            offered.append("file://").append(args[i]).append("\r\n");
        }
        if (offered.length() == 0) {
            offered.append("file:///tmp/dragged-out.txt\r\n");
        }

        try (RawXClient client = RawXClient.connect(host, port)) {
            int window = client.allocateId();
            client.createWindow(window, client.root(), 40, 40, 300, 200);
            client.mapWindow(window);
            client.flush();

            // What a client does the moment the user starts dragging something out of it.
            client.setSelectionOwner(window, client.internAtom(XDND_SELECTION), 0);
            client.flush();
            // **No round trip here.** The server asks for the file list as part of *processing* the
            // SetSelectionOwner, so its request is already on the wire - and a `geometry()` call
            // reads replies by skipping events, which would eat the very request this probe exists
            // to answer. (It does: the first version of this probe never saw one.) The loop below is
            // the sync.
            System.out.println("dragging out: window=0x" + Integer.toHexString(window)
                    + " offering " + offered.toString().replace("\r\n", " "));

            // Then it answers the server's request for the file list, for as long as it is alive -
            // which, for a probe, is until somebody kills it.
            while (true) {
                byte[] request = client.readEvent(30);              // SelectionRequest
                if (request == null) {
                    continue;
                }
                int requestor = RawXClient.readInt(request, 12);
                int target = RawXClient.readInt(request, 20);
                int property = RawXClient.readInt(request, 24);
                if (target != client.internAtom(URI_LIST)) {
                    // This probe carries files and nothing else, and saying so is what an owner does.
                    client.sendSelectionNotify(requestor, 0, requestor,
                            client.internAtom(XDND_SELECTION), target, 0);
                    client.flush();
                    continue;
                }
                client.setStringProperty(requestor, property, client.internAtom(URI_LIST),
                        offered.toString());
                client.sendSelectionNotify(requestor, 0, requestor,
                        client.internAtom(XDND_SELECTION), target, property);
                client.flush();
                System.out.println("  answered a request for the file list");
            }
        } catch (IOException e) {
            System.out.println("disconnected: " + e.getMessage());
        }
    }

    private DragOutProbe() {
    }
}
