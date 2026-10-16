package cn.oyzh.fx.xserver;

import cn.oyzh.common.log.JulLevel;
import cn.oyzh.common.log.JulLog;

import java.io.IOException;

/**
 * Command line entry point.
 *
 * <p>This class deliberately does not extend {@code javafx.application.Application}: JavaFX is on
 * the classpath rather than the module path, and launching an {@code Application} subclass that
 * way is rejected by the runtime. The backend starts the toolkit itself.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws IOException {
        ServerOptions options = ServerOptions.parse(args);

        options.fitToDisplay();

        if (options.logRequests()) {
            JulLog.setLevel(JulLevel.DEBUG);
        }

        XServer server = new XServer(options);
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop();
            JulLog.info("fx-xserver: display :" + options.displayNumber() + " stopped");
        }, "fx-xserver-shutdown"));

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
