package cn.oyzh.fx.xserver.clipboard;

import cn.oyzh.common.log.JulLog;

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

/**
 * The host's clipboard, as far as this server cares about it: one string.
 *
 * <p>Text only, deliberately. A clipboard that holds a picture or a file has no X11 equivalent this
 * server could offer - `TARGETS` would name something no forwarded application asked for - so it
 * reads as "nothing to serve" rather than as an empty string, which is the difference between an X
 * paste doing nothing and an X paste wiping what the user had.
 *
 * <p>Two platform facts shape the API. The whole thing is unavailable on a headless JVM, where
 * asking for the clipboard throws rather than returning nothing, so {@link #isAvailable()} is the
 * first question a caller asks. And a read is a round trip to the platform's clipboard service: it
 * can block, so it must never happen while the server lock is held - see {@link ClipboardBridge},
 * which keeps a copy precisely so that it never has to.
 */
public final class HostClipboard {


    /** Null when there is no clipboard to talk to: no display, or the platform refused. */
    private final Clipboard clipboard;

    private HostClipboard(Clipboard clipboard) {
        this.clipboard = clipboard;
    }

    /**
     * The bridge over a given clipboard, which is how a test supplies one it can control, and how an
     * embedder would bridge to a clipboard other than the system's.
     */
    public static HostClipboard of(Clipboard clipboard) {
        return new HostClipboard(clipboard);
    }

    /**
     * The host clipboard, or one that answers "nothing" when the platform has none.
     *
     * <p>A headless JVM is asked about first: {@code getSystemClipboard} throws there, and a server
     * with no window has no host to bridge to anyway.
     */
    public static HostClipboard open() {
        if (GraphicsEnvironment.isHeadless()) {
            JulLog.debug("no host clipboard: this JVM is headless");
            return new HostClipboard(null);
        }
        try {
            return new HostClipboard(Toolkit.getDefaultToolkit().getSystemClipboard());
        } catch (RuntimeException | Error e) {
            // A toolkit that cannot start is not this server's problem to solve; it just means the
            // bridge has nothing to bridge to.
            JulLog.info("no host clipboard: {}", e.toString());
            return new HostClipboard(null);
        }
    }

    public boolean isAvailable() {
        return clipboard != null;
    }

    /**
     * The clipboard's text, or null when it holds none this server can serve.
     *
     * <p>Null covers both "no clipboard at all" and "the clipboard holds something that is not
     * text", because the caller does the same thing with either: keep serving what it last had.
     */
    public String readText() {
        if (clipboard == null) {
            return null;
        }
        Transferable contents = clipboard.getContents(null);
        if (contents == null || !contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            return null;
        }
        try {
            return (String) contents.getTransferData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException | java.io.IOException e) {
            // A clipboard that advertises text and then cannot produce it - a source application
            // that has gone away mid-transfer, typically. Not an error worth breaking the bridge
            // over; the next poll asks again.
            JulLog.debug("the host clipboard offered no text: {}", e.toString());
            return null;
        }
    }

    /** Replaces the clipboard with text, which the next {@link #readText()} reads back. */
    public void writeText(String text) {
        if (clipboard == null) {
            return;
        }
        // A null owner: nothing here wants to be told the clipboard changed again, and claiming it
        // would make this JVM a clipboard owner that outlives the server.
        clipboard.setContents(new StringSelection(text), null);
    }
}
