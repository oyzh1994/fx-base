package cn.oyzh.fx.xserver.clipboard;

import cn.oyzh.fx.xserver.clipboard.ClipboardBridge;
import cn.oyzh.fx.xserver.clipboard.HostClipboard;
import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The host clipboard bridge, both directions.
 *
 * <p>The bridge is the one part of this server that talks to the machine it runs on rather than to a
 * client, so it is tested against a clipboard the test owns: a real one would rearrange whatever the
 * person running the suite had copied, and would make the test depend on a platform.
 *
 * <p>What the tests pin is the part that is not obvious from either side alone - that the bridge
 * takes the selection only when a client does not, that a client's copy reaches the host and leaves
 * the client owning the selection, and that the two encodings and the chunked transfer survive the
 * crossing.
 */
class ClipboardBridgeTest {

    /** A display number the rest of the suite does not use. */
    private static final int DISPLAY = 87;

    /** How long a wait may take before the test gives up: the poll is 200 ms. */
    private static final long TIMEOUT_MILLIS = 5000;

    private static XServer server;
    private static ClipboardBridge bridge;
    private static FakeClipboard host;

    @BeforeAll
    static void startServer() throws IOException {
        // No --single-window: the default mode gives every connection a screen of its own, which is
        // where the bridge's windows have to be per screen and where a client could not place the one
        // window it used to have. See theBridgeAsksFromAWindowOnTheOwnersScreen.
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--backend", "headless", "--no-unix"}));
        server.start();
        host = new FakeClipboard();
        bridge = server.startClipboardBridge(HostClipboard.of(host));
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @BeforeEach
    void resetHostClipboard() {
        // Something on the clipboard, because that is what makes the bridge hold the selection: with
        // nothing to serve it gives the selection up rather than answer a paste with an empty string.
        host.setText("clipboard reset");
        assertTrue(await("the bridge to take the selection",
                () -> selectionOwner() == bridge.windowId()), "the bridge never took the selection");
    }

    // ------------------------------------------------------------------
    // Host to X
    // ------------------------------------------------------------------

    @Test
    void theBridgeOwnsClipboardWhenNoClientDoes() throws IOException {
        assertEquals(bridge.windowId(), selectionOwner(),
                "a client that pastes into a fresh server must find an owner to ask");
    }

    @Test
    void aClientPastesWhatTheHostHolds() throws IOException {
        host.setText("hello from the Mac");
        await("the poll to see the new text", () -> "hello from the Mac".equals(served()));

        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = client.allocateId();
            client.createWindow(window, client.root(), 0, 0, 100, 100);
            String text = paste(client, window, "UTF8_STRING");
            assertEquals("hello from the Mac", text);
        }
    }

    @Test
    void theTargetsSayWhatCanBePasted() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = client.allocateId();
            client.createWindow(window, client.root(), 0, 0, 100, 100);
            int property = client.internAtom("XSERVER_TEST_TARGETS");
            int targets = client.internAtom("TARGETS");

            client.convertSelection(window, clipboardAtom(client), targets, property, 0);
            client.flush();
            assertNotNull(readSelectionNotify(client), "the owner must answer a TARGETS request");

            byte[] reply = client.getProperty(window, property, 0, true);
            assertEquals(32, reply[1] & 0xFF, "TARGETS is a list of atoms");
            int count = RawXClient.readInt(reply, 16);
            assertTrue(count >= 4, "TARGETS, UTF8_STRING, STRING and TEXT are all on offer");
            boolean utf8 = false;
            for (int i = 0; i < count; i++) {
                int atom = RawXClient.readInt(reply, 32 + i * 4);
                utf8 |= atom == client.internAtom("UTF8_STRING");
            }
            assertTrue(utf8, "a client has to be told UTF8_STRING is available, or it will not "
                    + "ask for the one encoding that carries anything but ASCII");
        }
    }

    @Test
    void aTargetTheBridgeCannotServeIsRefused() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = client.allocateId();
            client.createWindow(window, client.root(), 0, 0, 100, 100);
            int property = client.internAtom("XSERVER_TEST_REFUSED");
            int imagePng = client.internAtom("image/png");

            client.convertSelection(window, clipboardAtom(client), imagePng, property, 0);
            client.flush();
            byte[] notify = readSelectionNotify(client);
            assertNotNull(notify);
            assertEquals(0, RawXClient.readInt(notify, 20),
                    "property None is how an owner says it cannot serve that target");
        }
    }

    // ------------------------------------------------------------------
    // X to host
    // ------------------------------------------------------------------

    @Test
    void whatAClientCopiesReachesTheHost() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = client.allocateId();
            client.createWindow(window, client.root(), 0, 0, 100, 100);
            copy(client, window, "written in the application", false);

            assertTrue(awaitText("written in the application"),
                    "a copy inside a forwarded application has to reach the Mac's clipboard, or "
                            + "there is nothing to paste into a Mac application");
        }
    }

    @Test
    void aClientThatCopiesKeepsTheSelection() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = client.allocateId();
            client.createWindow(window, client.root(), 0, 0, 100, 100);
            copy(client, window, "mine", false);
            awaitText("mine");

            assertEquals(window, selectionOwner(),
                    "the bridge mirrors the host clipboard, it does not take the selection away "
                            + "from the client that just copied");
        }
    }

    @Test
    void chineseTextSurvivesBothWays() throws IOException {
        String chinese = "你好，世界 —— xserver 剪贴板";
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = client.allocateId();
            client.createWindow(window, client.root(), 0, 0, 100, 100);

            copy(client, window, chinese, false);
            assertTrue(awaitText(chinese),
                    "the application copied Chinese and the host clipboard must hold Chinese, not "
                            + "mojibake: " + host.text());

            // And back the other way, after the host takes the selection over.
            host.setText(chinese + " 2");
            await("the host's new text to be served", () -> (chinese + " 2").equals(served()));
            assertEquals(chinese + " 2", paste(client, window, "UTF8_STRING"));
        }
    }

    @Test
    void aChunkedTransferReachesTheHost() throws IOException {
        // What a source client does when the data does not fit in one property: it announces the
        // size, then sends it in pieces, each one only after the requestor has taken the last.
        String text = "chunked:".repeat(200);
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = client.allocateId();
            client.createWindow(window, client.root(), 0, 0, 100, 100);
            copy(client, window, text, true);

            assertTrue(awaitText(text),
                    "a transfer that used INCR has to arrive whole: " + host.text().length()
                            + " of " + text.length() + " characters");
        }
    }

    @Test
    void theBridgeAsksFromAWindowOnTheOwnersScreen() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = client.allocateId();
            client.createWindow(window, client.root(), 0, 0, 100, 100);
            int requestor = copy(client, window, "asked through", false);

            // The client wraps the window it is asked through before it answers, and a window whose
            // root is not among the screens its setup reply named is one it cannot place: GDK 3.24
            // crashes on one rather than refusing it (measured 2026-09-30 - EasyShell's toolkit died
            // in gdk_x11_window_foreign_new_for_display, in XScreenNumberOfScreen, on every copy).
            // The bridge is made before any client connects, so its own screen is the master one and
            // every id it names has to be a window of *this* client's screen instead.
            assertEquals(client.root(), client.rootOf(requestor),
                    "the bridge asked through 0x" + Integer.toHexString(requestor)
                            + ", which is not on the owner's own screen (root 0x"
                            + Integer.toHexString(client.root()) + ")");
            assertTrue(awaitText("asked through"),
                    "and the answer still has to reach the host clipboard");
        }
    }

    @Test
    void theHostWinsWhenSomebodyCopiesOnTheMac() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = client.allocateId();
            client.createWindow(window, client.root(), 0, 0, 100, 100);
            copy(client, window, "from the application", false);
            awaitText("from the application");
            assertEquals(window, selectionOwner(), "the client still owns what it copied");

            // Somebody copies in a Mac application: an X paste must get the new text, which means
            // the bridge has to take the selection away from the client that held it.
            host.setText("from the Mac");
            await("the bridge to take the selection over",
                    () -> selectionOwner() == bridge.windowId());
            assertEquals("from the Mac", paste(client, window, "UTF8_STRING"));
            assertNotEquals(0, bridge.windowId());
        }
    }

    // ------------------------------------------------------------------
    // Harness
    // ------------------------------------------------------------------

    /** Asks the owner for a target, reads the property it answered with, and decodes it. */
    private static String paste(RawXClient client, int window, String targetName)
            throws IOException {
        int property = client.internAtom("XSERVER_TEST_DATA");
        client.convertSelection(window, clipboardAtom(client), client.internAtom(targetName),
                property, 0);
        client.flush();
        byte[] notify = readSelectionNotify(client);
        assertNotNull(notify, "the owner answered nothing");
        assertEquals(property, RawXClient.readInt(notify, 20), "the data was not put anywhere");
        byte[] reply = client.getProperty(window, property, 0, true);
        int length = RawXClient.readInt(reply, 16) * (reply[1] & 0xFF) / 8;
        return new String(reply, 32, length, StandardCharsets.UTF_8);
    }

    /**
     * Copies from inside the application: the client takes the selection, answers the bridge's
     * request for it, and - when asked for a chunked transfer - feeds the data in pieces.
     *
     * @return the window the bridge asked through, which is the {@code requestor} of its request
     */
    private static int copy(RawXClient client, int window, String text, boolean chunked)
            throws IOException {
        int selection = clipboardAtom(client);
        client.setSelectionOwner(window, selection, 0);
        client.flush();

        byte[] request = readSelectionRequest(client, selection);
        int requestor = RawXClient.readInt(request, 12);
        int target = RawXClient.readInt(request, 20);
        int property = RawXClient.readInt(request, 24);

        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        if (!chunked) {
            client.setStringProperty(requestor, property, target, text);
            client.flush();
            client.sendSelectionNotify(requestor, 0, requestor, selection, target, property);
            client.flush();
            return requestor;
        }

        int incr = client.internAtom("INCR");
        client.setCardinalProperty(requestor, property, incr, data.length);
        client.flush();
        client.sendSelectionNotify(requestor, 0, requestor, selection, target, property);
        client.flush();

        // The requestor deletes the property to say "send the first piece", and deletes each piece
        // after taking it; an empty property is the end.
        int sent = 0;
        while (sent < data.length) {
            await("the requestor to take the last piece", () -> !hasProperty(client, requestor,
                    property));
            int chunk = Math.min(1024, data.length - sent);
            client.setStringProperty(requestor, property, target,
                    new String(data, sent, chunk, StandardCharsets.UTF_8));
            client.flush();
            sent += chunk;
        }
        await("the requestor to take the last piece", () ->
                !hasProperty(client, requestor, property));
        client.setStringProperty(requestor, property, target, "");
        client.flush();
        return requestor;
    }

    /** The bridge's request for the data, skipping anything else the server sent first. */
    private static byte[] readSelectionRequest(RawXClient client, int selection)
            throws IOException {
        client.readTimeoutMillis((int) TIMEOUT_MILLIS);
        try {
            for (byte[] event = client.readEvent(); event != null; event = client.readEvent()) {
                if ((event[0] & 0x7F) == 30 && RawXClient.readInt(event, 16) == selection) {
                    return event;
                }
            }
        } finally {
            client.readTimeoutMillis(5000);
        }
        throw new IOException("the bridge never asked for the selection");
    }

    private static byte[] readSelectionNotify(RawXClient client) throws IOException {
        client.readTimeoutMillis((int) TIMEOUT_MILLIS);
        try {
            return client.readEvent(31);
        } finally {
            client.readTimeoutMillis(5000);
        }
    }

    private static boolean hasProperty(RawXClient client, int window, int property)
            throws IOException {
        byte[] reply = client.getProperty(window, property, 0, false);
        return RawXClient.readInt(reply, 8) != 0;
    }

    private static int clipboardAtom(RawXClient client) throws IOException {
        return client.internAtom("CLIPBOARD");
    }

    private static int selectionOwner() {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            return client.getSelectionOwner(clipboardAtom(client));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * What a client would paste right now, read the way a client reads it - or null while the bridge
     * does not own the selection, which is the case until it notices that the host changed.
     */
    private static String served() throws IOException {
        if (selectionOwner() != bridge.windowId()) {
            return null;
        }
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = client.allocateId();
            client.createWindow(window, client.root(), 0, 0, 100, 100);
            return paste(client, window, "UTF8_STRING");
        }
    }

    private static boolean awaitText(String expected) {
        return await("the host clipboard to hold " + expected, () -> expected.equals(host.text()));
    }


    /** A condition that may have to talk to the server to answer, which most of these do. */
    private interface Check {
        boolean ok() throws IOException;
    }

    private static boolean await(String what, Check condition) {
        long deadline = System.currentTimeMillis() + TIMEOUT_MILLIS;
        while (System.currentTimeMillis() < deadline) {
            try {
                if (condition.ok()) {
                    return true;
                }
            } catch (IOException e) {
                throw new AssertionError("waiting for " + what + ": " + e, e);
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return false;
    }

    /** A clipboard the test owns, so nothing here touches the machine's own pasteboard. */
    private static final class FakeClipboard extends Clipboard {

        private String text;

        FakeClipboard() {
            super("xserver-test");
        }

        synchronized String text() {
            return text;
        }

        synchronized void setText(String text) {
            this.text = text;
        }

        @Override
        public synchronized void setContents(Transferable contents, ClipboardOwner owner) {
            try {
                text = contents == null ? null
                        : (String) contents.getTransferData(DataFlavor.stringFlavor);
            } catch (Exception e) {
                text = null;
            }
        }

        @Override
        public synchronized Transferable getContents(Object requestor) {
            return text == null ? null : new StringSelection(text);
        }

        @Override
        public synchronized DataFlavor[] getAvailableDataFlavors() {
            return text == null ? new DataFlavor[0] : new DataFlavor[] {DataFlavor.stringFlavor};
        }

        @Override
        public synchronized boolean isDataFlavorAvailable(DataFlavor flavor) {
            return text != null && DataFlavor.stringFlavor.equals(flavor);
        }

        @Override
        public synchronized Object getData(DataFlavor flavor) {
            if (text == null || !DataFlavor.stringFlavor.equals(flavor)) {
                throw new IllegalStateException("no text on the clipboard");
            }
            return text;
        }
    }
}
