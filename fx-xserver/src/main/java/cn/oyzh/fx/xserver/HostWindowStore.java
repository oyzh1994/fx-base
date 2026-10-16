package cn.oyzh.fx.xserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Where the host window was, the last time a given client was forwarded.
 *
 * <p>A user who arranges their forwarded windows once expects them to come back there, and nothing
 * about a client's own protocol traffic says where its window was - the position belongs to the host
 * window, which only this server knows about. So it is remembered here, keyed by the client's
 * {@code WM_CLASS}: the one name a client publishes that does not change when it retitles a window.
 *
 * <p><b>This is the first state this server keeps between runs</b>, and it is a plain properties file
 * rather than anything cleverer for that reason: it is small, a user can read and edit it, and
 * deleting it costs nothing but the remembered positions. It lives in {@code ~/.xserver/}.
 *
 * <p>Nothing here is allowed to be the reason a server fails to start: an unreadable file is a
 * server that has not remembered anything yet, and an unwritable one is a server that will not
 * remember anything this time.
 */
public final class HostWindowStore {

    private static final Logger log = LoggerFactory.getLogger(HostWindowStore.class);

    /** Where the positions are kept unless {@code -Dxserver.hostWindows} says otherwise. */
    private static final String DEFAULT_PATH = ".xserver/host-windows.properties";

    private final Path file;
    private final Properties entries = new Properties();
    private boolean loaded;

    private HostWindowStore(Path file) {
        this.file = file;
    }

    /** The store in the user's home directory. */
    public static HostWindowStore open() {
        String configured = System.getProperty("xserver.hostWindows");
        if (configured != null && !configured.isEmpty()) {
            return at(Path.of(configured));
        }
        return at(Path.of(System.getProperty("user.home", "."), DEFAULT_PATH));
    }

    /** The store in a given file, which is how a test gets one of its own. */
    public static HostWindowStore at(Path file) {
        return new HostWindowStore(file);
    }

    /**
     * Where a client's window was the last time, or null when it has not been seen before.
     *
     * <p>What comes back is what was written, not a position that is necessarily on screen now: a
     * display that has been unplugged, or a laptop docked to a different monitor, moves the usable
     * area out from under a remembered position. Clamping is the backends' business, because only
     * they know what is on screen - see {@link #clamp}.
     */
    public synchronized Point position(String key) {
        if (key == null) {
            return null;
        }
        load();
        String x = entries.getProperty(key + ".x");
        String y = entries.getProperty(key + ".y");
        if (x == null || y == null) {
            return null;
        }
        try {
            return new Point(Integer.parseInt(x.trim()), Integer.parseInt(y.trim()));
        } catch (NumberFormatException e) {
            // A hand edited file is allowed to be wrong; it just means nothing is remembered.
            log.debug("ignoring an unreadable remembered position for {}: {},{}", key, x, y);
            return null;
        }
    }

    /** Records where a client's window is now, writing the file when that is news. */
    public synchronized void rememberPosition(String key, int x, int y) {
        if (key == null) {
            return;
        }
        load();
        Point current = position(key);
        if (current != null && current.x == x && current.y == y) {
            return;
        }
        entries.setProperty(key + ".x", Integer.toString(x));
        entries.setProperty(key + ".y", Integer.toString(y));
        save();
    }

    /**
     * A remembered position moved so that the window's top left is somewhere the display can show.
     *
     * <p>A position is remembered in the coordinates of the display it was arranged on, and a window
     * placed back at a position that display no longer has is a window the user cannot reach at all.
     * The window is kept inside {@code usable} where it fits - a window larger than the display keeps
     * its top left corner, which is the part that carries the title bar and the controls.
     *
     * @param usable the part of the display windows may occupy, in the platform's own coordinates
     */
    public static Point clamp(Point position, int width, int height, Rectangle usable) {
        int maxX = Math.max(usable.x, usable.x + usable.width - width);
        int maxY = Math.max(usable.y, usable.y + usable.height - height);
        return new Point(Math.min(Math.max(position.x, usable.x), maxX),
                Math.min(Math.max(position.y, usable.y), maxY));
    }

    /** The file the positions are kept in, for a log line or a test. */
    public Path file() {
        return file;
    }

    private void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        if (!Files.isReadable(file)) {
            return;
        }
        try (InputStream in = Files.newInputStream(file)) {
            entries.load(in);
        } catch (IOException e) {
            log.info("no remembered window positions: {} could not be read ({})", file,
                    e.getMessage());
        }
    }

    private void save() {
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (OutputStream out = Files.newOutputStream(file)) {
                entries.store(out, "Where this server last put each client's window");
            }
        } catch (IOException e) {
            // Remembering is a convenience. A server that cannot write here still forwards windows.
            log.info("window positions will not be remembered: {} could not be written ({})", file,
                    e.getMessage());
        }
    }
}
