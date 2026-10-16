package cn.oyzh.fx.xserver.drop;

import cn.oyzh.fx.xserver.drop.DropBridge;
import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * A file dragged from the host's desktop onto a forwarded application.
 *
 * <p>X's drag and drop is client to client and the <em>source</em> drives it, so the whole of what is
 * missing is a source on the X side that speaks for the host's drag: the application is a target that
 * has been dragging and dropping on real desktops all along. These tests play that target - a window
 * that publishes {@code XdndAware}, the messages a real one sends back, and the
 * {@code ConvertSelection} it makes when the drop arrives - because the platform's half of a drag
 * cannot be produced in-process.
 */
class DropBridgeTest {

    /** A display number the rest of the suite does not use. */
    private static final int DISPLAY = 68;

    private static final int CLIENT_MESSAGE = 33;
    private static final int SELECTION_REQUEST = 30;
    private static final int SELECTION_NOTIFY = 31;

    /** {@code _NET_WM_STATE}'s sibling: what XDND calls "I take drops". */
    private static final String XDND_AWARE = "XdndAware";
    private static final String XDND_SELECTION = "XdndSelection";
    private static final String URI_LIST = "text/uri-list";

    private XServer server;

    @BeforeEach
    void startServer() throws IOException {
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--geometry", "400x300", "--backend", "headless", "--single-window", "--no-unix"}));
        server.start();
        server.startEventLoop();
        // The bridge is not started for a headless server - there is no host to drag from - so the
        // test starts it with the same call an embedder would.
        server.startDropBridge();
    }

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void aDroppedFileIsOfferedToTheWindowUnderThePointer() throws IOException {
        try (RawXClient target = RawXClient.connect(DISPLAY)) {
            int window = takeDrops(target);
            pointAt(target, 200, 150);
            // Every atom before the drop: interning one reads a reply, and reading a reply throws
            // away whatever arrived before it - which would be the message being waited for.
            int enterAtom = target.internAtom("XdndEnter");
            int positionAtom = target.internAtom("XdndPosition");
            int uriListAtom = target.internAtom(URI_LIST);

            server.dropBridge().hostDropped(List.of("/tmp/readme.txt"), 200, 150);

            byte[] enter = readMessage(target, enterAtom);
            assertNotNull(enter, "the target has to be told a drag has arrived");
            assertEquals(server.dropBridge().windowId(), RawXClient.readInt(enter, 12),
                    "and which window the drag comes from, which is the bridge's own");
            // XdndEnter: the source at 12, the version at 16, the types it can offer from 20.
            assertEquals(5, RawXClient.readInt(enter, 16) >>> 24, "and the XDND version it speaks");
            assertEquals(uriListAtom, RawXClient.readInt(enter, 20), "and what it carries");

            byte[] position = readMessage(target, positionAtom);
            assertNotNull(position, "the target tracks a drag by where it is");
            // XdndPosition: the source at 12, flags at 16, the point at 20, the time at 24.
            assertEquals((200 << 16) | 150, RawXClient.readInt(position, 20),
                    "in root coordinates, as XDND wants them, packed two per word");
        }
    }

    @Test
    void aTargetThatAcceptsIsDroppedOnAndCanAskForTheFiles() throws IOException {
        try (RawXClient target = RawXClient.connect(DISPLAY)) {
            int window = takeDrops(target);
            pointAt(target, 200, 150);
            int enterAtom = target.internAtom("XdndEnter");
            int dropAtom = target.internAtom("XdndDrop");
            server.dropBridge().hostDropped(List.of("/tmp/readme.txt", "/tmp/second.txt"), 200, 150);
            assertNotNull(readMessage(target, enterAtom));

            // The target answers the position, saying it will take the drop - which is what a real
            // toolkit sends, and what the source waits for before dropping.
            int source = server.dropBridge().windowId();
            target.sendClientMessage(source, target.internAtom("XdndStatus"), window, 1, 0, 0,
                    target.internAtom("XdndActionCopy"));

            assertNotNull(readMessage(target, dropAtom), "the drag ends now the target took it");

            // And now it asks for the data, through the selection the source owns.
            int property = target.internAtom("XSERVER_DROP_DATA");
            target.convertSelection(window, target.internAtom(XDND_SELECTION),
                    target.internAtom(URI_LIST), property, 0);
            target.flush();

            byte[] notify = target.readEvent(SELECTION_NOTIFY);
            assertNotNull(notify, "the source has to answer a target that asks for the drag");
            assertEquals(property, RawXClient.readInt(notify, 20), "the data is in the property");

            byte[] reply = target.getProperty(window, property, 0, true);
            int length = RawXClient.readInt(reply, 16);
            assertEquals("file:///tmp/readme.txt\r\nfile:///tmp/second.txt\r\n",
                    new String(reply, 32, length, StandardCharsets.UTF_8),
                    "one URI per line is what text/uri-list is, and what a file manager sends");
        }
    }

    /**
     * The URI list a target is handed escapes what a URI has to escape - not only the spaces the
     * first version handled.
     *
     * <p>Measured against a real drop: the bridge offers paths from the host, so a name with a `#`
     * (which starts a fragment), a `%` (which starts an escape) or a non-ASCII character (the common
     * case for this server's own user) has to be escaped or the target decodes a different file - or
     * none. The escape is UTF-8 byte by byte, which is what a toolkit decodes.
     */
    @Test
    void anEscapedNameSurvivesAsAURITheTargetCanDecode() throws IOException {
        try (RawXClient target = RawXClient.connect(DISPLAY)) {
            int window = takeDrops(target);
            pointAt(target, 200, 150);
            // The atom is interned first, like the test above: interning is a round trip, and a
            // message read before it would be the reply to it rather than the drag.
            int enterAtom = target.internAtom("XdndEnter");
            server.dropBridge().hostDropped(List.of("/tmp/a b#c%20.txt", "/tmp/中文.txt"), 200, 150);
            assertNotNull(readMessage(target, enterAtom));

            int property = target.internAtom("XSERVER_DROP_DATA");
            target.convertSelection(window, target.internAtom(XDND_SELECTION),
                    target.internAtom(URI_LIST), property, 0);
            target.flush();
            assertNotNull(target.readEvent(SELECTION_NOTIFY), "the source has to answer");

            byte[] reply = target.getProperty(window, property, 0, true);
            int length = RawXClient.readInt(reply, 16);
            assertEquals("file:///tmp/a%20b%23c%2520.txt\r\nfile:///tmp/%E4%B8%AD%E6%96%87.txt\r\n",
                    new String(reply, 32, length, StandardCharsets.UTF_8),
                    "every byte outside the unreserved set escaped, UTF-8, and `%` itself escaped -"
                            + " which is what makes the name come back as the file that was dropped");
        }
    }

    /**
     * macOS hands a dragged file over as its *resolved* path - {@code /private/tmp/...} for anything
     * under `/tmp`, which is a symlink - and a target that shares `/tmp` cannot open that. The bridge
     * reports it without the prefix when what is left is the same file, which is what a user dragged.
     */
    @Test
    void aResolvedPathIsReportedAsThePathTheUserSees() throws IOException {
        java.nio.file.Path real = java.nio.file.Files.createTempFile("drop", ".txt");
        try (RawXClient target = RawXClient.connect(DISPLAY)) {
            int window = takeDrops(target);
            pointAt(target, 200, 150);
            int enterAtom = target.internAtom("XdndEnter");
            server.dropBridge().hostDropped(List.of("/private" + real), 200, 150);
            assertNotNull(readMessage(target, enterAtom));

            int property = target.internAtom("XSERVER_DROP_DATA");
            target.convertSelection(window, target.internAtom(XDND_SELECTION),
                    target.internAtom(URI_LIST), property, 0);
            target.flush();
            assertNotNull(target.readEvent(SELECTION_NOTIFY));
            byte[] reply = target.getProperty(window, property, 0, true);
            // Only macOS has the resolved-path prefix; elsewhere the path is the path.
            String expected = new java.io.File("/private" + real).exists() && new java.io.File("/private").exists()
                    ? "file://" + real : "file://" + "/private" + real;
            assertEquals(expected + "\r\n",
                    new String(reply, 32, RawXClient.readInt(reply, 16), StandardCharsets.UTF_8),
                    "the path a client on the same filesystem can actually open");
        } finally {
            java.nio.file.Files.deleteIfExists(real);
        }
    }

    /**
     * A drop onto a shared directory is handed the path the <em>client</em> has it at, which is the
     * one thing that makes the drag do something useful across machines - the application opens the
     * path it is given (see the C5 entry).
     */
    @Test
    void aPathUnderAMountIsHandedOverAtItsClientPath() throws IOException {
        // A display of its own: the suite's own server already holds DISPLAY, and two servers cannot
        // share a display number. 62 is below the range the suite binds.
        XServer mounted = new XServer(ServerOptions.parse(new String[]{"--display", ":62",
                "--geometry", "400x300", "--backend", "headless", "--single-window", "--no-unix",
                "--drop-mount", "/Users/me/Shared=/mnt/hgfs/Shared"}));
        mounted.start();
        mounted.startEventLoop();
        mounted.startDropBridge();
        try (RawXClient target = RawXClient.connect(62)) {
            try {
                int window = takeDrops(target);
                pointAt(target, 200, 150);
                int enterAtom = target.internAtom("XdndEnter");
                mounted.dropBridge().hostDropped(
                        List.of("/Users/me/Shared/note.txt", "/Users/me/elsewhere.txt"), 200, 150);
                assertNotNull(readMessage(target, enterAtom));

                int property = target.internAtom("XSERVER_DROP_DATA");
                target.convertSelection(window, target.internAtom(XDND_SELECTION),
                        target.internAtom(URI_LIST), property, 0);
                target.flush();
                assertNotNull(target.readEvent(SELECTION_NOTIFY));
                byte[] reply = target.getProperty(window, property, 0, true);
                assertEquals("file:///mnt/hgfs/Shared/note.txt\r\n"
                                + "file:///Users/me/elsewhere.txt\r\n",
                        new String(reply, 32, RawXClient.readInt(reply, 16), StandardCharsets.UTF_8),
                        "the mounted prefix is rewritten and everything else is left alone");
            } finally {
                mounted.stop();
            }
        }
    }

    @Test
    void aWindowThatDoesNotTakeDropsIsLeftAlone() throws IOException {
        try (RawXClient target = RawXClient.connect(DISPLAY)) {
            // No XdndAware on this one: an application that does not do drag and drop.
            int window = target.allocateId();
            target.createWindow(window, target.root(), 0, 0, 400, 300);
            target.mapWindow(window);
            target.flush();
            pointAt(target, 200, 150);

            server.dropBridge().hostDropped(List.of("/tmp/readme.txt"), 200, 150);

            target.readTimeoutMillis(500);
            try {
                assertNull(target.readEvent(CLIENT_MESSAGE),
                        "a drag onto an application that does not take drops does nothing");
            } finally {
                target.readTimeoutMillis(5000);
            }
        }
    }


    /**
     * The other direction: a drag that starts inside a forwarded application, and the server finding
     * out what it is offering.
     *
     * <p>X gives no "I am dragging" message, so the signal is the one thing a server can see before
     * the application has committed to a target: **the client takes the {@code XdndSelection}**. The
     * bridge watches it, and can then ask the owner for {@code text/uri-list} the same way a target
     * does - as a {@code SelectionRequest} event, because here the bridge is the requestor and the
     * client is the owner. This test plays the source, which is the half the platform cannot
     * produce in-process.
     */
    @Test
    void aClientThatTakesTheDragSelectionIsDraggingOutAndCanBeAskedForItsFiles()
            throws IOException {
        try (RawXClient source = RawXClient.connect(DISPLAY)) {
            int window = source.allocateId();
            source.createWindow(window, source.root(), 0, 0, 200, 100);
            source.mapWindow(window);
            source.flush();

            DropBridge bridge = server.dropBridge();
            assertEquals(false, bridge.isDraggingOut(),
                    "nothing is being dragged before a client says so");

            // What a client does when the user drags something out of it.
            source.setSelectionOwner(window, source.internAtom(XDND_SELECTION), 0);
            source.flush();
            source.geometry(window);                    // a round trip, so the server has run it
            assertEquals(true, bridge.isDraggingOut(),
                    "the drag selection having an owner is how a drag inside an application is seen");

            bridge.askDraggingClient();

            // The bridge asks as an event, and the owner answers the way any selection owner does.
            byte[] request = source.readEvent(SELECTION_REQUEST);
            assertNotNull(request, "the bridge has to ask the dragging client what it offers");
            int requestor = RawXClient.readInt(request, 12);
            assertEquals(bridge.windowId(), requestor, "the answer goes back to the bridge's own window");
            // The property the data goes in is the one the *request* names - a client does not get to
            // choose it, and a test that invented its own name would be testing itself.
            int property = RawXClient.readInt(request, 24);
            assertEquals(source.internAtom(URI_LIST), RawXClient.readInt(request, 20),
                    "and what is asked for is text/uri-list, which is what a file drag carries");

            // An escaped name on the way in too: a client that offers a file with a space or a
            // non-ASCII character sends it per cent encoded, and the path this side hands on has to
            // be the file it names.
            source.setStringProperty(bridge.windowId(), property, source.internAtom(URI_LIST),
                    "file:///root/notes.txt\r\nfile:///root/%E4%B8%AD%E6%96%87%20notes.png\r\n");
            source.sendSelectionNotify(bridge.windowId(), 0, bridge.windowId(),
                    source.internAtom(XDND_SELECTION), source.internAtom(URI_LIST), property);
            source.flush();
            source.geometry(window);

            assertEquals(List.of("/root/notes.txt", "/root/中文 notes.png"), bridge.dragOutPaths(),
                    "one URI per line is what text/uri-list is, and the paths come back in order");

            // A client that gives the selection up is no longer dragging.
            source.setSelectionOwner(0, source.internAtom(XDND_SELECTION), 0);
            source.flush();
            source.geometry(window);
            assertEquals(false, bridge.isDraggingOut(), "and letting it go ends the drag");
        }
    }

    // ------------------------------------------------------------------
    // 辅助
    // ------------------------------------------------------------------

    /** A window that takes drops: mapped, aware, and with the pointer over it. */
    private int takeDrops(RawXClient client) throws IOException {
        int window = client.allocateId();
        client.createWindow(window, client.root(), 0, 0, 400, 300);
        // XdndAware is a version number; 5 is what toolkits have spoken for twenty years.
        client.setCardinalProperty(window, client.internAtom(XDND_AWARE),
                client.internAtom("ATOM"), 5);
        client.mapWindow(window);
        client.flush();
        return window;
    }

    /**
     * Puts the pointer over the window, which is how the bridge knows where a drop landed: the
     * platform reports the point, and the window is whatever is under it.
     */
    private void pointAt(RawXClient client, int x, int y) throws IOException {
        int xtest = client.queryExtension("XTEST");
        client.fakeMotion(xtest, x, y);
        client.flush();
        // The pointer's window is worked out as the motion is delivered, so give the server a moment
        // to have done it before the drop arrives.
        client.queryInputFocus();
    }

    /** The next {@code ClientMessage} of a given type, or null when none arrives. */
    private static byte[] readMessage(RawXClient client, int atom) throws IOException {
        client.readTimeoutMillis(1500);
        try {
            for (byte[] event = client.readEvent(); event != null; event = client.readEvent()) {
                if ((event[0] & 0x7F) == CLIENT_MESSAGE
                        && RawXClient.readInt(event, 8) == atom) {
                    return event;
                }
            }
            return null;
        } finally {
            client.readTimeoutMillis(5000);
        }
    }
}
