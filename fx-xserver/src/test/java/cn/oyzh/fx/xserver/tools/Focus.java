package cn.oyzh.fx.xserver.tools;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

/**
 * Sets the input focus of a display from a bare-protocol client: the window manager's half of a
 * focus test, for a server that has no window manager.
 *
 * <p>Usage: {@code Focus host port windowId [revertTo]}, where the window id is the one the client
 * under test created ({@link WinTree} prints the tree) and {@code revertTo} is X's: 0 PointerRoot,
 * 1 None (the default here), 2 Parent.
 *
 * <p>Part of the verification kit ({@code docs/backlog.md} §1). It is how "the click was ignored
 * because the window was not focused" is separated from "the click never arrived" - set the focus
 * somewhere else, then click, and read the server log.
 */
public class Focus {

    public static void main(String[] args) throws Exception {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        long window = Long.decode(args[2]);
        int revertTo = args.length > 3 ? Integer.parseInt(args[3]) : 1;
        try (Socket socket = new Socket(host, port)) {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());
            setup(out, in);
            byte[] request = new byte[12];
            request[0] = 42;                        // SetInputFocus
            request[1] = (byte) revertTo;           // revert-to
            request[2] = 3;                         // length
            putInt(request, 4, (int) window);
            putInt(request, 8, 0);                  // time: CurrentTime
            out.write(request);
            out.flush();
            System.out.println("focus -> 0x" + Long.toHexString(window));
        }
    }

    static void setup(DataOutputStream out, DataInputStream in) throws Exception {
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

    static void putInt(byte[] data, int offset, int value) {
        data[offset] = (byte) value;
        data[offset + 1] = (byte) (value >> 8);
        data[offset + 2] = (byte) (value >> 16);
        data[offset + 3] = (byte) (value >> 24);
    }
}
