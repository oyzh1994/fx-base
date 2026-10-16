package cn.oyzh.fx.xserver.tools;

import cn.oyzh.fx.xserver.test.RawXClient;

/**
 * Types a string into a server through XTEST, one key press and release per character.
 *
 * <p>Part of the verification kit ({@code docs/backlog.md} §1). {@link Drive} sends single keycodes
 * and {@link Shot} captures what came of them, but a gesture that means something - typing a path, a
 * search term, a name into a dialog - needs a character to become the keycode this server's keymap
 * gives it, which is what this does: it reads {@code GetKeyboardMapping} and resolves each character
 * the way a client would, rather than carrying a copy of the layout.
 *
 * <pre>
 *   Keys &lt;host&gt; &lt;port&gt; "hello world"
 *   Keys &lt;host&gt; &lt;port&gt; --delay 120 "a slower\ntypist"
 * </pre>
 *
 * <p>A character the keyboard has no key for - anything above ASCII, unless the server has claimed a
 * keycode for it (backlog item C10) - is reported and skipped rather than guessed at. Every key is
 * pressed as an unshifted or a shifted key according to the mapping read back, so case and the
 * punctuation above the number row arrive correctly. {@code \n}, {@code \t}, {@code \b} and
 * {@code \e} are the control keys a scripted gesture usually wants.
 *
 * <p>Like {@link Drive}, XTEST input is scoped to the screen of the client that sends it
 * ({@code XTestExtension} -&gt; {@code server.inputFor(client)}), so in the default
 * one-window-per-client mode this cannot type into another client; run the server with
 * {@code --single-window} to script a client's GUI.
 */
public final class Keys {

    /** The keysyms that are not their character: the control keys a scripted gesture reaches for. */
    private static final int RETURN = 0xFF0D;
    private static final int TAB = 0xFF09;
    private static final int BACK_SPACE = 0xFF08;
    private static final int ESCAPE = 0xFF1B;
    private static final int DELETE = 0xFFFF;

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("usage: Keys <host> <port> [--delay ms] <text>");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        int delay = 40;
        StringBuilder text = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            if (args[i].equals("--delay")) {
                delay = Integer.parseInt(args[++i]);
                continue;
            }
            text.append(text.isEmpty() ? "" : " ").append(args[i]);
        }

        try (RawXClient client = RawXClient.connect(host, port)) {
            int xtest = client.queryExtension("XTEST");
            if (xtest == 0) {
                throw new IllegalStateException("the server has no XTEST");
            }
            int[] keysyms = client.keyboardMapping(Keymap.MIN_KEYCODE, Keymap.SPAN);
            int perKeycode = keysyms.length / Keymap.SPAN;
            // The shift key is looked up rather than assumed: a server that moves it is read the same
            // way a client would read it.
            int shift = keycodeFor(keysyms, perKeycode, 0xFFE1);
            if (shift == 0) {
                throw new IllegalStateException("the server maps no Shift key");
            }

            int typed = 0;
            int skipped = 0;
            for (char c : text.toString().toCharArray()) {
                int keysym = keysymFor(c);
                int keycode = keycodeFor(keysyms, perKeycode, keysym);
                if (keycode == 0) {
                    System.out.println("no key carries " + describe(c) + " - skipped");
                    skipped++;
                    continue;
                }
                boolean shifted = shifted(keysyms, perKeycode, keycode, keysym);
                if (shifted) {
                    client.fakeKey(xtest, shift, true);
                }
                client.fakeKey(xtest, keycode, true);
                Thread.sleep(delay);
                client.fakeKey(xtest, keycode, false);
                if (shifted) {
                    client.fakeKey(xtest, shift, false);
                }
                typed++;
                Thread.sleep(delay);
            }
            System.out.println("typed " + typed + " character" + (typed == 1 ? "" : "s")
                    + (skipped > 0 ? ", skipped " + skipped : ""));
        }
    }

    /** The keysym a character stands for: its own code, or the control key it names. */
    private static int keysymFor(char c) {
        return switch (c) {
            case '\n', '\r' -> RETURN;
            case '\t' -> TAB;
            case '\b' -> BACK_SPACE;
            case 0x1B -> ESCAPE;
            case 0x7F -> DELETE;
            default -> c;
        };
    }

    /** The first keycode whose unshifted keysym is the wanted one, or 0 when the keyboard has none. */
    private static int keycodeFor(int[] keysyms, int perKeycode, int wanted) {
        for (int keycode = Keymap.MIN_KEYCODE; keycode < Keymap.MIN_KEYCODE + Keymap.SPAN; keycode++) {
            int at = (keycode - Keymap.MIN_KEYCODE) * perKeycode;
            for (int level = 0; level < perKeycode; level++) {
                if (keysyms[at + level] == wanted) {
                    return keycode;
                }
            }
        }
        return 0;
    }

    /** True when the wanted keysym sits above the first level, i.e. the key needs Shift. */
    private static boolean shifted(int[] keysyms, int perKeycode, int keycode, int wanted) {
        int at = (keycode - Keymap.MIN_KEYCODE) * perKeycode;
        return perKeycode > 1 && keysyms[at] != wanted && keysyms[at + 1] == wanted;
    }

    private static String describe(char c) {
        return c < 0x20 || c > 0x7E ? String.format("U+%04X", (int) c) : "'" + c + "'";
    }

    /**
     * The keycodes this tool scans, kept beside {@link Keys} rather than reaching into the server's
     * own {@code Keymap}: a probe against Xvfb has to use the same bounds a client would, and the
     * server's table is about the server.
     */
    private static final class Keymap {
        static final int MIN_KEYCODE = 8;
        static final int SPAN = 248;
    }

    private Keys() {
    }
}
