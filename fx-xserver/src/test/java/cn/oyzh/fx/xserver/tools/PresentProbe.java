package cn.oyzh.fx.xserver.tools;

import com.github.moaxcp.x11.protocol.X11LittleEndianOutputStream;
import com.github.moaxcp.x11.protocol.present.NotifyMSC;
import com.github.moaxcp.x11.protocol.present.Pixmap;
import com.github.moaxcp.x11.protocol.present.QueryCapabilities;
import com.github.moaxcp.x11.protocol.present.QueryVersion;
import com.github.moaxcp.x11.protocol.present.SelectInput;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.eclipse.collections.impl.factory.Lists;

import java.io.ByteArrayOutputStream;

/**
 * The Present extension as a client uses it - the harness for the last of B5's three, and the path a
 * browser or a video player takes to hand a finished frame to the server.
 *
 * <p>What it does: asks the version (twice, because whether the answer is the server's version or the
 * client's is a rule the reply settles), asks for the capabilities, then paints a window one colour
 * and a pixmap another, subscribes to the completion event, presents the pixmap into the window, and
 * reads both the window's pixels and the event that came back. **Did the pixmap arrive in the window,
 * and what did the completion say** are the two questions the extension turns on.
 *
 * <p><b>This probe serialises its requests with the generated protocol classes rather than building
 * the bytes by hand, and that is the one place the kit does.</b> SYNC's probe earned it: the
 * generated class's getters listed a fence's fields in the opposite order to the wire, and a
 * hand-built request that believed them was answered `BadDrawable` on its own fence id. Here the
 * fields are a dozen and several are 64 bit, so the writer is used - and a wrong layout in the
 * writers is still caught, because the comparison is against a reference that refuses what it cannot
 * read.
 *
 * <p>Usage: {@code PresentProbe &lt;host&gt; &lt;port&gt;}.
 */
public final class PresentProbe {

    private static final int WINDOW_COLOR = 0x00AA22;
    private static final int PIXMAP_COLOR = 0x0000FF;

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("usage: PresentProbe <host> <port>");
            return;
        }
        try (RawXClient client = RawXClient.connect(args[0], Integer.parseInt(args[1]))) {
            int[] info = client.queryExtensionInfo("Present");
            System.out.println("Present present=" + info[0] + " major=" + info[1]
                    + " firstEvent=" + info[2] + " firstError=" + info[3]);
            if (info[0] == 0) {
                return;
            }
            int opcode = info[1];

            for (int minor : new int[]{2, 0}) {
                send(client, opcode, QueryVersion.builder()
                        .majorVersion(1).minorVersion(minor).build());
                byte[] reply = client.readReply();
                System.out.println("  QueryVersion(1." + minor + ") -> " + RawXClient.readInt(reply, 8)
                        + "." + RawXClient.readInt(reply, 12));
            }

            send(client, opcode, QueryCapabilities.builder().target(client.root()).build());
            byte[] capabilities = client.readReply();
            System.out.println("  QueryCapabilities(root) -> 0x"
                    + Integer.toHexString(RawXClient.readInt(capabilities, 8)));

            int window = client.allocateId();
            client.createWindow(window, client.root(), 20, 20, 100, 80);
            client.mapWindow(window);
            int windowGc = client.createGC(client.allocateId(), client.root(), WINDOW_COLOR);
            client.fillRectangle(window, windowGc, 0, 0, 100, 80);

            int pixmap = client.allocateId();
            client.createPixmap(pixmap, client.root(), 100, 80, 24);
            int pixmapGc = client.createGC(client.allocateId(), client.root(), PIXMAP_COLOR);
            client.fillRectangle(pixmap, pixmapGc, 0, 0, 100, 80);
            client.flush();
            System.out.println("  window painted #" + String.format("%06X", WINDOW_COLOR)
                    + ", pixmap painted #" + String.format("%06X", PIXMAP_COLOR));

            // Subscribe before presenting, or the completion event has nowhere to go. The event
            // context id is the client's own, as every id a client names is.
            send(client, opcode, SelectInput.builder()
                    .eid(client.allocateId())
                    .window(window)
                    .eventMask(1 << 1)                          // CompleteNotify
                    .build());
            client.flush();

            send(client, opcode, Pixmap.builder()
                    .window(window)
                    .pixmap(pixmap)
                    .serial(7)
                    // Both are *regions*, not flags, and None (0) is the whole of each. A first
                    // version passed update=1 - a region id that does not exist - and the reference
                    // answered BadPresent with the value 0x1.
                    .valid(0)                                   // the whole pixmap
                    .update(0)                                  // and the whole window
                    .xOff((short) 0).yOff((short) 0)
                    .targetCrtc(0)
                    .waitFence(0).idleFence(0)
                    .options(0)
                    .targetMsc(0).divisor(0).remainder(0)
                    .notifies(Lists.immutable.empty())
                    .build());
            client.flush();
            client.readTimeoutMillis(1500);

            // The event first, then the pixels. This server finishes the present before it reads the
            // next request, so the completion is already in the stream when the GetImage is sent; a
            // reference produces it later, and reading the event first is the order that works on
            // both. A client that handles events asynchronously does not care - a probe reads the
            // stream in order and does.
            byte[] event = client.readEvent();
            if (event == null) {
                System.out.println("  no completion event arrived");
            } else if (event.length < 32) {
                System.out.println("  a " + event.length + " byte event arrived, too short to be a "
                        + "completion notification");
            } else {
                // The four fields that are the server's decision, and not the three that are not: the
                // body starts after the ten byte GenericEvent header, and `window`, `event`, `ust`
                // and `msc` are an id and two clocks, which differ between two servers by
                // construction. Offsets read off the generated reader rather than counted here.
                System.out.println("  CompleteNotify length=" + event.length + " eventType=" + (event[8] & 0xFF) + " kind=" + event[10]
                        + " mode=" + event[11] + " serial=" + RawXClient.readInt(event, 20));
            }
            client.readTimeoutMillis(1500);
            int pixel = client.pixelAt(window, 50, 40);
            System.out.println("  window at 20,20 now reads " + String.format("#%08X", pixel)
                    + (pixel == (0xFF000000 | PIXMAP_COLOR)
                            ? " (the pixmap arrived)" : " (the pixmap did not arrive)"));

            // NotifyMSC: the client asks to be told when the display reaches a given media stream
            // counter, which is how a compositor or a video player paces itself. Nothing is drawn,
            // so the whole measurement is the completion that comes back - its kind, mode and serial.
            send(client, opcode, NotifyMSC.builder()
                    .window(window)
                    .serial(9)
                    .targetMsc(0).divisor(0).remainder(0)
                    .build());
            client.flush();
            client.readTimeoutMillis(1500);
            byte[] msc = client.readEvent();
            if (msc == null) {
                System.out.println("  NotifyMSC -> no completion event arrived");
            } else if (msc.length < 32) {
                System.out.println("  NotifyMSC -> a " + msc.length + " byte message arrived");
            } else {
                System.out.println("  NotifyMSC length=" + msc.length + " eventType="
                        + (msc[8] & 0xFF) + " kind=" + msc[10] + " mode=" + msc[11]
                        + " serial=" + RawXClient.readInt(msc, 20));
            }
        }
    }

    /** Serialises a generated request and sends it, so no field order is written down twice. */
    private static void send(RawXClient client, int opcode, com.github.moaxcp.x11.protocol.XRequest
            request) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        X11LittleEndianOutputStream out = new X11LittleEndianOutputStream(bytes);
        request.write((byte) opcode, out);
        out.flush();
        client.sendRequest(bytes.toByteArray());
    }

    private PresentProbe() {
    }
}
