package cn.oyzh.fx.xserver.test;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.draw.Framebuffer;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Diagnostic harness: runs the server headless and writes the framebuffer itself to a PNG once a
 * second.
 *
 * <p>A headless server draws where nobody can see it, so a client that renders nothing is
 * indistinguishable from a server that lost the pixels. Reading the screen back with {@code xwd}
 * does not settle it either, because that goes through {@code GetImage} and so through the
 * encoder: if the two disagree, this side is the one telling the truth. Comparing the two is what
 * separates a drawing bug from a reading bug.
 *
 * <p>Usage: {@code FrameDump <display> <prefix> [--log-requests]}. The dump lands in
 * {@code <prefix>.png}; point any X client at the display while it runs.
 */
public final class FrameDump {

    public static void main(String[] args) throws Exception {
        String display = args.length > 0 ? args[0] : ":9";
        String prefix = args.length > 1 ? args[1] : "/tmp/frame";
        boolean logRequests = args.length > 2 && "--log-requests".equals(args[2]);
        ServerOptions options = logRequests
                ? ServerOptions.parse(new String[]{"--display", display, "--backend", "headless",
                        "--log-requests"})
                : ServerOptions.parse(new String[]{"--display", display, "--backend", "headless"});
        if (logRequests) {
            // The request log is debug level, so the flag turns it on rather than leaving it to the
            // logging configuration, exactly as Main does.
            ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
                    .getLogger("cn.oyzh.fx.xserver"))
                    .setLevel(ch.qos.logback.classic.Level.DEBUG);
        }
        XServer server = new XServer(options);
        server.start();
        Framebuffer framebuffer = server.screen().framebuffer();
        System.out.printf("framebuffer %dx%d, dumping to %s.png%n",
                framebuffer.width(), framebuffer.height(), prefix);
        Thread dump = new Thread(() -> dumpForever(server, framebuffer, prefix), "frame-dump");
        dump.setDaemon(true);
        dump.start();
        // Returns immediately; the display loop keeps the process alive.
        server.startEventLoop();
    }

    private static void dumpForever(XServer server, Framebuffer framebuffer, String prefix) {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            BufferedImage out = new BufferedImage(framebuffer.width(), framebuffer.height(),
                    BufferedImage.TYPE_INT_ARGB);
            // The drawing threads hold the server lock for the length of a request, so taking it
            // here is what keeps a frame from being copied halfway through one.
            server.lockServer();
            try {
                out.setRGB(0, 0, framebuffer.width(), framebuffer.height(), framebuffer.pixels(),
                        0, framebuffer.width());
            } finally {
                server.unlockServer();
            }
            try {
                javax.imageio.ImageIO.write(out, "png", new File(prefix + ".png"));
            } catch (Exception e) {
                System.out.println("frame dump failed: " + e);
            }
        }
    }
}
