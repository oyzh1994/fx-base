package cn.oyzh.fx.xserver.tools;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Sends a connection setup with chosen credentials and reports what the server answered.
 *
 * <p>Usage: {@code AuthProbe host port [authName [authDataHex]]}. With no arguments it offers
 * nothing - which is what a client with no {@code .Xauthority} entry sends, and what backlog item A7
 * is about: a server with access control on must refuse it, and an {@code -ac} server must answer it.
 *
 * <p>Prints the vendor string on a setup reply, or the reason on a refusal, on one line.
 */
public class AuthProbe {

    public static void main(String[] args) throws Exception {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String authName = args.length > 2 ? args[2] : null;
        byte[] authData = args.length > 3 ? hex(args[3]) : null;
        byte[] name = authName == null ? new byte[0] : authName.getBytes(StandardCharsets.ISO_8859_1);
        byte[] data = authData == null ? new byte[0] : authData;
        try (Socket socket = new Socket(host, port)) {
            socket.setSoTimeout(5000);
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            int body = 12 + pad4(name.length) + pad4(data.length);
            byte[] request = new byte[body];
            request[0] = 'l';
            request[2] = 11;
            // The two length fields are read in the byte order the client declared ('l' here), so
            // they are little endian even though the protocol version beside them is not.
            request[6] = (byte) name.length;
            request[7] = (byte) (name.length >> 8);
            request[8] = (byte) data.length;
            request[9] = (byte) (data.length >> 8);
            System.arraycopy(name, 0, request, 12, name.length);
            System.arraycopy(data, 0, request, 12 + pad4(name.length), data.length);
            out.write(request);
            out.flush();

            int first = in.readUnsignedByte();
            if (first != 1) {
                // Failed: status, reason length, the two version words, then the reason length in
                // four byte units - a header like the setup reply's, with the text after it.
                byte[] failure = new byte[7];
                in.readFully(failure);
                int reasonLength = failure[0] & 0xFF;
                byte[] reason = new byte[(reasonLength + 3) & ~3];
                in.readFully(reason);
                System.out.println("REFUSED: "
                        + new String(reason, 0, reasonLength, StandardCharsets.ISO_8859_1));
                return;
            }
            byte[] header = new byte[7];
            in.readFully(header);
            int length = (header[5] & 0xFF) | ((header[6] & 0xFF) << 8);
            byte[] reply = new byte[length * 4];
            in.readFully(reply);
            // Everything after the eight byte prefix is in the client's byte order.
            int vendorLength = (reply[16] & 0xFF) | ((reply[17] & 0xFF) << 8);
            System.out.println("SETUP: vendor=" + new String(reply, 32, vendorLength,
                    StandardCharsets.ISO_8859_1) + " screens=" + (reply[20] & 0xFF));
        }
    }

    private static byte[] hex(String text) {
        byte[] bytes = new byte[text.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(text.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }

    private static int pad4(int value) {
        return (value + 3) & ~3;
    }
}
