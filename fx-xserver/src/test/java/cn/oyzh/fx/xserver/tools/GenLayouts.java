package cn.oyzh.fx.xserver.tools;

import cn.oyzh.fx.xserver.test.RawXClient;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reads the keyboard layouts off a reference server and writes {@code input/Layouts.java}.
 *
 * <p>Part of the verification kit, and the same trick {@code GenBitmapFont} uses for the standard X
 * fonts: a display server cannot compile a layout (that is a keymap compiler, and this is a display
 * server - the one thing B1 leaves out), so the layouts are **captured off a server that can** and
 * served from tables. {@code tools/gen-layouts.sh} runs {@code setxkbmap} on the VM's Xvfb and calls
 * this through {@code tools/kit.sh} for each name.
 *
 * <p>What it reads is the **core** mapping ({@code GetKeyboardMapping}), not the XKB one, and that is
 * the choice the feature rests on: a toolkit resolves a keycode through the core request - JavaFX
 * does, measured - and the level a modifier picks is the *client's* arithmetic. So a wider core
 * mapping is a layout a client can actually use, while the XKB map this server publishes has four
 * types and cannot express four levels whatever the table says.
 *
 * <p>Usage: {@code GenLayouts &lt;host&gt; &lt;port&gt; &lt;name&gt; [name...]} - writes Java source
 * to stdout, one table per name.
 */
public final class GenLayouts {

    /** The keycode range a reference describes, which is the whole client-visible one. */
    private static final int MIN_KEYCODE = 8;
    private static final int MAX_KEYCODE = 255;

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("usage: GenLayouts <host> <port> <name> [name...]");
            return;
        }
        Map<String, int[][]> layouts = new LinkedHashMap<>();
        try (RawXClient client = RawXClient.connect(args[0], Integer.parseInt(args[1]))) {
            for (int i = 2; i < args.length; i++) {
                byte[] request = new byte[8];
                request[0] = 101;                       // GetKeyboardMapping
                request[2] = 2;
                request[4] = (byte) MIN_KEYCODE;
                request[5] = (byte) (MAX_KEYCODE - MIN_KEYCODE + 1);
                client.sendRequest(request);
                byte[] reply = client.readReply();
                int perKeycode = reply[1] & 0xFF;
                int count = MAX_KEYCODE - MIN_KEYCODE + 1;
                int[][] rows = new int[count][];
                for (int row = 0; row < count; row++) {
                    int[] keysyms = new int[perKeycode];
                    for (int level = 0; level < perKeycode; level++) {
                        keysyms[level] = RawXClient.readInt(reply, 32 + (row * perKeycode + level) * 4);
                    }
                    rows[row] = keysyms;
                }
                layouts.put(args[i], rows);
                System.err.println(args[i] + ": " + perKeycode + " keysyms per keycode");
            }
        }
        // One method per layout, and not one initialiser for all of them: the JVM caps a method at
        // 64KB of bytecode, and twelve thousand int literals in one expression is well past it.
        for (Map.Entry<String, int[][]> layout : layouts.entrySet()) {
            int[][] rows = layout.getValue();
            StringBuilder out = new StringBuilder();
            out.append("    /** The ").append(layout.getKey()).append(" layout, captured off a reference. */\n");
            out.append("    private static int[][] ").append(layout.getKey()).append("() {\n");
            out.append("        return new int[][] {\n");
            for (int[] row : rows) {
                out.append("                {");
                for (int level = 0; level < row.length; level++) {
                    if (level > 0) {
                        out.append(", ");
                    }
                    out.append("0x").append(Integer.toHexString(row[level]));
                }
                out.append("},\n");
            }
            out.append("        };\n");
            out.append("    }\n");
            System.out.println(out);
        }
    }

    /** The keycode a table starts at, so the caller can write it down once. */
    static int firstKeycode() {
        return MIN_KEYCODE;
    }

    private GenLayouts() {
    }
}
