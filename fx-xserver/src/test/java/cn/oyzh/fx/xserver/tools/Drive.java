package cn.oyzh.fx.xserver.tools;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

/**
 * Bare-protocol client that drives a GUI with XTEST fake input.
 *
 * <p>Part of the verification kit described in {@code docs/backlog.md} §1. It exists because
 * {@code xdotool} segfaults against this server (backlog item A4), so scripted gestures have to be
 * sent by hand: a few dozen lines of protocol, no Xlib.
 *
 * <p>Usage: {@code Drive host port "M 100 200" "W 200" "P 1" "R 1" ...} with
 * <pre>
 *   M x y   motion to an absolute screen position
 *   W ms    wait
 *   P n     press button n
 *   R n     release button n
 *   K kc    key press, then release
 *   KD kc   key press only
 *   KU kc   key release only
 * </pre>
 *
 * <p>Two things to know when using it: XTEST input is scoped to the screen of the client that sends
 * it (`XTestExtension` -> {@code server.inputFor(client)}), so in the default one-window-per-client
 * mode it cannot drive another client - run the server with {@code --single-window} to script a
 * client's GUI. And a press or release with no preceding motion uses the pointer's current position,
 * which is how a click is sent without moving the pointer.
 */
public class Drive {

    public static void main(String[] args) throws Exception {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        try (Socket socket = new Socket(host, port)) {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());
            handshake(out, in);
            int xtest = queryExtension(out, in, "XTEST");
            if (xtest == 0) {
                throw new IllegalStateException("no XTEST");
            }
            for (int i = 2; i < args.length; i++) {
                String[] parts = args[i].trim().split("\\s+");
                switch (parts[0]) {
                    case "M" -> fakeInput(out, xtest, 6, 0, Integer.parseInt(parts[1]),
                            Integer.parseInt(parts[2]));
                    case "W" -> Thread.sleep(Long.parseLong(parts[1]));
                    case "P" -> fakeInput(out, xtest, 4, Integer.parseInt(parts[1]), 0, 0);
                    case "R" -> fakeInput(out, xtest, 5, Integer.parseInt(parts[1]), 0, 0);
                    case "K" -> {
                        fakeInput(out, xtest, 2, Integer.parseInt(parts[1]), 0, 0);
                        fakeInput(out, xtest, 3, Integer.parseInt(parts[1]), 0, 0);
                    }
                    case "KD" -> fakeInput(out, xtest, 2, Integer.parseInt(parts[1]), 0, 0);
                    case "KU" -> fakeInput(out, xtest, 3, Integer.parseInt(parts[1]), 0, 0);
                    default -> throw new IllegalArgumentException(args[i]);
                }
                System.out.println("did " + args[i]);
            }
        }
        System.out.println("done");
    }

    private static void fakeInput(DataOutputStream out, int opcode, int type, int detail, int x,
                                  int y) throws Exception {
        byte[] request = new byte[36];
        request[0] = (byte) opcode;
        request[1] = 2;                             // X_XTestFakeInput
        request[2] = 9;                             // length in four byte units
        request[4] = (byte) type;                   // 6 motion, 4 press, 5 release, 2/3 key press/release
        request[5] = (byte) detail;                 // button number, or 0 for absolute motion
        request[24] = (byte) x;                     // root-x
        request[25] = (byte) (x >> 8);
        request[26] = (byte) y;                     // root-y
        request[27] = (byte) (y >> 8);
        request[35] = 0;                            // device id 0: the core pointer
        out.write(request);
        out.flush();
        Thread.sleep(60);
    }

    private static void handshake(DataOutputStream out, DataInputStream in) throws Exception {
        byte[] setup = new byte[12];
        setup[0] = 'l';
        setup[2] = 11;
        out.write(setup);
        out.flush();
        byte[] header = new byte[8];
        in.readFully(header);
        if ((header[0] & 0xFF) != 1) {
            throw new IllegalStateException("setup failed");
        }
        int length = (header[6] & 0xFF) | ((header[7] & 0xFF) << 8);
        in.readFully(new byte[length * 4]);
    }

    private static int queryExtension(DataOutputStream out, DataInputStream in, String name)
            throws Exception {
        byte[] request = new byte[8 + name.length() + 3 & ~3];
        request[0] = 98;                            // QueryExtension
        request[1] = 0;
        request[2] = (byte) (request.length / 4);
        request[4] = (byte) name.length();
        for (int i = 0; i < name.length(); i++) {
            request[8 + i] = (byte) name.charAt(i);
        }
        out.write(request);
        out.flush();
        byte[] reply = new byte[32];
        in.readFully(reply);
        int present = reply[8] & 0xFF;
        return present == 0 ? 0 : reply[9] & 0xFF;
    }
}
