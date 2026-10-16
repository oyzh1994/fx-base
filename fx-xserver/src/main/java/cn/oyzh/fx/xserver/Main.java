package cn.oyzh.fx.xserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command line entry point.
 *
 * <p>This class deliberately does not extend {@code javafx.application.Application}: JavaFX is on
 * the classpath rather than the module path, and launching an {@code Application} subclass that
 * way is rejected by the runtime. The backend starts the toolkit itself.
 */
public final class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private Main() {
    }

    public static void main(String[] args) {
        ServerOptions options;
        try {
            options = ServerOptions.parse(args);
        } catch (IllegalArgumentException e) {
            System.err.println("xserver: " + e.getMessage());
//            System.exit(2);
            return;
        }

        options.fitToDisplay();

        if (options.logRequests()) {
            // The request log is debug level, so the flag turns it on rather than leaving it to
            // the logging configuration.
            var logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("cn.oyzh.fx.xserver");
            logger.setLevel(ch.qos.logback.classic.Level.DEBUG);
        }

        XServer server = new XServer(options);
        try {
            server.start();
        } catch (Exception e) {
            log.error("failed to start", e);
            System.err.println("xserver: " + e.getMessage());
//            System.exit(1);
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop();
            System.out.println("xserver: display :" + options.displayNumber() + " stopped");
        }, "xserver-shutdown"));

        // The display loop runs on its own thread - and it is the server's life, so this one waits
        // for it and then ends the process. Returning from main is *not* enough: the JavaFX toolkit's
        // own keep-alive thread is not a daemon and outlives Platform.exit(), so a process whose
        // server had finished - "the window is closed", every client released - kept sitting in the
        // dock with no server thread in it at all (measured, 2026-10-01: the dock's Quit closed the
        // window and released the clients, and the JVM was still there twelve seconds later, its only
        // non-daemon threads "JavaFX-KeepAlive" and "DestroyJavaVM").
        Thread display = server.startEventLoop();
        try {
            display.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
//        System.exit(0);
    }
}
