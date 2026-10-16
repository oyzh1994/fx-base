package cn.oyzh.fx.xserver.client;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The server's own "this request was refused" line has to name the request that was refused.
 *
 * <p>It is the only thing a person has to go on when a client's request is rejected: the client
 * sees an error code and a sequence number, and the log is where the request is named. Measured
 * against a live server on 2026-09-26, this is what it said for a request a real client (JavaFX)
 * makes as it starts:
 *
 * <pre>
 * client[2 /192.168.22.152:49496]: InternAtom rejected the request (error 17): not implemented:
 *     RANDR GetOutputProperty: this server's screen is the host window
 * </pre>
 *
 * <p>The name was the last <em>core</em> request the client had sent, because only core requests
 * set it - so the line named a request that had succeeded, and the one it was about could only be
 * worked out from the message. Both halves are asserted here: the request that is refused, and the
 * request before it, which must not be the one named.
 */
class RequestLogTest {

    /** A display number nothing else in the suite uses; the server listens on TCP 6000 + it. */
    private static final int DISPLAY = 72;

    private static XServer server;
    private static ListAppender<ILoggingEvent> logLines;

    @BeforeAll
    static void startServer() throws IOException {
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--backend", "headless", "--no-unix", "--single-window"}));
        server.start();
        logLines = new ListAppender<>();
        logLines.start();
        ((Logger) LoggerFactory.getLogger(XServer.class)).addAppender(logLines);
    }

    @AfterAll
    static void stopServer() {
        ((Logger) LoggerFactory.getLogger(XServer.class)).detachAppender(logLines);
        if (server != null) {
            server.stop();
        }
    }

    /**
     * A refused extension request is named as itself, and the core request before it is not named
     * in its place.
     *
     * <p>{@code GetProviderInfo} is the request to use because it is refused on purpose (this server
     * has no providers) and because it is an extension request, which is the half that went wrong.
     */
    @Test
    void aRefusedRequestIsNamedAsItself() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int randr = client.queryExtension("RANDR");
            assertTrue(randr != 0, "the extension this test is about");

            // A core request that succeeds, immediately before: it is the one the old code named.
            client.internAtom("EDID");

            logLines.list.clear();
            client.sendRequest(getProviderInfo(randr));
            client.flush();
            client.readEvent();                     // the error, which is what makes it "refused"

            List<String> refused = lines("rejected the request");
            assertTrue(refused.stream().anyMatch(line -> line.contains("GetProviderInfo")),
                    "the refused request has to be named; the log said " + refused);
            assertTrue(refused.stream().noneMatch(line -> line.contains("InternAtom")),
                    "and the request before it must not be named in its place; the log said "
                            + refused);
        }
    }

    /** The log lines that contain a marker, so a failure prints what was written. */
    private static List<String> lines(String marker) {
        return logLines.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .filter(message -> message.contains(marker))
                .toList();
    }

    /** {@code GetProviderInfo} for provider 0, which this server does not have. */
    private static byte[] getProviderInfo(int randr) {
        byte[] request = new byte[12];
        request[0] = (byte) randr;
        request[1] = 33;                            // GetProviderInfo
        RawXClient.writeShortAt(request, 2, 3);
        RawXClient.writeIntAt(request, 8, 0);       // config timestamp
        return request;
    }
}
