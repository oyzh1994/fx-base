package cn.oyzh.fx.xserver.tools;

import cn.oyzh.fx.xserver.test.RawXClient;

/**
 * Asks a server for a window state, the way a toolkit does - the harness for backlog item C3.
 *
 * <p>A client does not set {@code _NET_WM_STATE}; it sends a {@code ClientMessage} to the root and
 * waits for the window manager to put the state into the window's property, which is why this exists
 * as a tool of its own: what the server does with that message is the whole of the feature, and the
 * property it writes back is the only place it can be seen from outside.
 *
 * <pre>
 *   WindowStateProbe &lt;host&gt; &lt;port&gt; fullscreen on
 *   WindowStateProbe &lt;host&gt; &lt;port&gt; fullscreen on,off     # asked for, then given back
 *   WindowStateProbe &lt;host&gt; &lt;port&gt; fullscreen state       # what the window carries now
 * </pre>
 *
 * <p>The actions are a comma separated list because they share one window: a window that asks to be
 * fullscreen and then asks not to is a sequence on one connection, and a fresh connection each time
 * would be asking about a different window.
 */
public final class WindowStateProbe {

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("usage: WindowStateProbe <host> <port> fullscreen on|off|toggle|state");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String subject = args[2];
        String actions = args.length > 3 ? args[3] : "state";

        try (RawXClient client = RawXClient.connect(host, port)) {
            int window = client.allocateId();
            client.createWindow(window, client.root(), 0, 0, 400, 300);
            client.mapWindow(window);
            client.flush();
            int fullScreen = client.internAtom("_NET_WM_STATE_FULLSCREEN");

            for (String action : actions.split(",")) {
                int stateAction = switch (action.trim()) {
                    case "on" -> 1;                 // _NET_WM_STATE_ADD
                    case "off" -> 0;                // _NET_WM_STATE_REMOVE
                    case "toggle" -> 2;             // _NET_WM_STATE_TOGGLE
                    default -> -1;
                };
                if (stateAction < 0) {
                    continue;
                }
                client.sendWindowState(window, stateAction, fullScreen, 0);
                client.flush();
                // The message is answered by the display tick, so the property is written a frame
                // later rather than before the request returns.
                Thread.sleep(600);
            }

            int[] states = client.windowState(window);
            StringBuilder names = new StringBuilder();
            for (int atom : states) {
                names.append(names.isEmpty() ? "" : ", ").append(client.atomName(atom));
            }
            System.out.println(subject + " window 0x" + Integer.toHexString(window)
                    + " carries [" + names + "]");
        }
    }

    private WindowStateProbe() {
    }
}
