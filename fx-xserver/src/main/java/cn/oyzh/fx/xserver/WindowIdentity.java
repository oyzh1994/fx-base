package cn.oyzh.fx.xserver;

import cn.oyzh.fx.xserver.resource.AtomTable;
import cn.oyzh.fx.xserver.resource.XProperty;
import cn.oyzh.fx.xserver.resource.XWindow;

import java.nio.charset.StandardCharsets;

/**
 * What the host window shows for the client whose screen it is: the title and icon that client
 * published on its own top level window.
 *
 * <p>Both are ordinary window properties, and copying them onto the frame drawn around the window
 * is a window manager's job. This server draws that frame, so the same two properties are what its
 * own window has to show - without this, every forwarded application arrives in a window called
 * "xserver" with no icon, whatever it calls itself.
 *
 * <p>They are re-read whenever they are written rather than read once when the window appears: a
 * terminal putting the current directory or the running program in its title does that constantly,
 * and the whole point is that the host window follows.
 */
public record WindowIdentity(String title, int[] iconArgb, int iconWidth, int iconHeight,
                             boolean fullScreen, boolean urgent) {

    /** The title a modern toolkit sets, in UTF-8. Preferred over {@code WM_NAME}. */
    public static final String NET_WM_NAME = "_NET_WM_NAME";

    /** The older title, which a client using Xlib sets by default. */
    public static final String WM_NAME = "WM_NAME";

    /** The icon a modern toolkit sets: repeated {@code width, height, ARGB pixels} blocks. */
    public static final String NET_WM_ICON = "_NET_WM_ICON";

    /** The list a client adds {@link #NET_WM_STATE_FULLSCREEN} to when it wants the whole screen. */
    public static final String NET_WM_STATE = "_NET_WM_STATE";

    /** The state a client asks for when it wants to fill the display. */
    public static final String NET_WM_STATE_FULLSCREEN = "_NET_WM_STATE_FULLSCREEN";

    /**
     * The list of EWMH features the window manager says it honours, on the root window: what a
     * toolkit reads before it asks for any of them (see {@code XServer.publishWindowManagerHints}).
     */
    public static final String NET_SUPPORTED = "_NET_SUPPORTED";

    /**
     * EWMH's "a window manager is here" pointer: the root's own answer is a window of the manager's,
     * and that window's answer is itself.
     */
    public static final String NET_SUPPORTING_WM_CHECK = "_NET_SUPPORTING_WM_CHECK";

    /** The older hints, one bit of whose flags says "the user should look at this". */
    public static final String WM_HINTS = "WM_HINTS";

    /** {@code UrgencyHint} in {@code WM_HINTS.flags}: the client is asking to be noticed. */
    private static final int URGENCY_HINT = 0x100;

    /** Icons bigger than this are not window icons; one is only used if nothing smaller exists. */
    private static final int LARGEST_SENSIBLE_ICON = 256;

    /** No title and no icon: the host window keeps the name the server was started with. */
    public static final WindowIdentity NONE = new WindowIdentity(null, null, 0, 0, false, false);

    /** True when a property is one the host window is built from. */
    public static boolean isIdentityProperty(String name) {
        return NET_WM_NAME.equals(name) || WM_NAME.equals(name) || NET_WM_ICON.equals(name)
                || NET_WM_STATE.equals(name) || WM_HINTS.equals(name);
    }

    /**
     * Reads what a window publishes about itself, or {@link #NONE} when it publishes neither a title
     * nor an icon.
     */
    public static WindowIdentity of(AtomTable atoms, XWindow window) {
        String title = title(atoms, window);
        int[] icon = null;
        int iconWidth = 0;
        int iconHeight = 0;
        XProperty property = propertyOf(atoms, window, NET_WM_ICON);
        if (property != null && property.format() == XProperty.FORMAT_32) {
            int[] best = bestIcon(property.data());
            if (best != null) {
                iconWidth = best[0];
                iconHeight = best[1];
                icon = java.util.Arrays.copyOfRange(best, 2, best.length);
            }
        }
        boolean fullScreen = wantsFullScreen(atoms, window);
        boolean urgent = isUrgent(atoms, window);
        if (title == null && icon == null && !fullScreen && !urgent) {
            return NONE;
        }
        return new WindowIdentity(title, icon, iconWidth, iconHeight, fullScreen, urgent);
    }

    /**
     * Whether the window asks to fill the display.
     *
     * <p>{@code _NET_WM_STATE} is a list a client adds to and removes from rather than a flag, which
     * is why this is a search: a client that also wants to be maximised and shaded has all three in
     * there, and the one being looked for can be anywhere.
     */
    private static boolean wantsFullScreen(AtomTable atoms, XWindow window) {
        XProperty state = propertyOf(atoms, window, NET_WM_STATE);
        if (state == null || state.format() != XProperty.FORMAT_32) {
            return false;
        }
        int wanted = atoms.intern(NET_WM_STATE_FULLSCREEN, true);
        if (wanted == AtomTable.NONE) {
            return false;
        }
        for (int atom : readWords(state.data())) {
            if (atom == wanted) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether the window set the urgency hint.
     *
     * <p>{@code WM_HINTS} is the older of the two vocabularies and the only place this bit lives: a
     * client that wants the user's attention sets it, and none of EWMH's replacements is as widely
     * implemented for the job.
     */
    private static boolean isUrgent(AtomTable atoms, XWindow window) {
        XProperty hints = propertyOf(atoms, window, WM_HINTS);
        if (hints == null || hints.format() != XProperty.FORMAT_32) {
            return false;
        }
        int[] words = readWords(hints.data());
        return words.length > 0 && (words[0] & URGENCY_HINT) != 0;
    }

    /** A {@code FORMAT_32} property's items, which are stored big-endian whatever wrote them. */
    public static int[] readWords(byte[] data) {
        int[] words = new int[data.length / 4];
        for (int i = 0; i < words.length; i++) {
            words[i] = ((data[i * 4] & 0xFF) << 24) | ((data[i * 4 + 1] & 0xFF) << 16)
                    | ((data[i * 4 + 2] & 0xFF) << 8) | (data[i * 4 + 3] & 0xFF);
        }
        return words;
    }

    /**
     * The window's title: {@code _NET_WM_NAME} when it is set, since that one is UTF-8 by
     * definition, otherwise {@code WM_NAME}.
     *
     * <p>{@code WM_NAME} is nominally Latin-1, but it is written by the same toolkits that write the
     * UTF-8 property and it carries the same bytes: decoding it as UTF-8 is right far more often
     * than decoding it as Latin-1, and falls back to Latin-1 when the bytes are not valid UTF-8,
     * which is what a client that really did mean Latin-1 sends.
     */
    private static String title(AtomTable atoms, XWindow window) {
        XProperty netName = propertyOf(atoms, window, NET_WM_NAME);
        if (netName != null && netName.format() == XProperty.FORMAT_8) {
            String decoded = decodeUtf8(netName.data());
            if (!decoded.isEmpty()) {
                return decoded;
            }
        }
        XProperty name = propertyOf(atoms, window, WM_NAME);
        if (name == null || name.format() != XProperty.FORMAT_8) {
            return null;
        }
        String decoded = decodeUtf8(name.data());
        return decoded.isEmpty() ? null : decoded;
    }

    /**
     * Decodes property bytes as UTF-8, falling back to Latin-1 when they are not UTF-8 at all.
     *
     * <p>A string property is padded with NULs by some clients, which are not part of the title.
     */
    private static String decodeUtf8(byte[] data) {
        int end = data.length;
        while (end > 0 && data[end - 1] == 0) {
            end--;
        }
        if (end == 0) {
            return "";
        }
        String utf8 = new String(data, 0, end, StandardCharsets.UTF_8);
        if (utf8.indexOf('�') < 0) {
            return utf8;
        }
        return new String(data, 0, end, StandardCharsets.ISO_8859_1);
    }

    /**
     * Picks the icon to use out of an {@code _NET_WM_ICON} property.
     *
     * <p>The property is a sequence of images, each {@code width, height} followed by that many
     * pixels, and a client is free to publish several sizes - a window manager picks. The largest
     * one that is still a sensible icon size wins, so a client that ships both a 16x16 and a 256x256
     * version is not shown the blurry one; if every image is oversized the smallest is used, since
     * showing something is better than showing nothing.
     *
     * @return {@code width, height, pixels...} or null when the property holds no usable image
     */
    private static int[] bestIcon(byte[] data) {
        // The stored order is big-endian whatever the writing client used, which is also the order
        // an ARGB pixel is meant to be read in.
        int[] words = readWords(data);
        int[] best = null;
        int offset = 0;
        while (offset + 2 <= words.length) {
            int width = words[offset];
            int height = words[offset + 1];
            if (width <= 0 || height <= 0 || width > 4096 || height > 4096) {
                break;
            }
            long pixels = (long) width * height;
            if (offset + 2 + pixels > words.length) {
                // Truncated: the header promised more pixels than the property holds.
                break;
            }
            if (best == null || better(width, height, best[0], best[1])) {
                best = new int[(int) pixels + 2];
                best[0] = width;
                best[1] = height;
                System.arraycopy(words, offset + 2, best, 2, (int) pixels);
            }
            offset += 2 + (int) pixels;
        }
        return best;
    }

    /** Whether an icon of {@code width x height} is the better choice out of two. */
    private static boolean better(int width, int height, int bestWidth, int bestHeight) {
        boolean fits = Math.max(width, height) <= LARGEST_SENSIBLE_ICON;
        boolean bestFits = Math.max(bestWidth, bestHeight) <= LARGEST_SENSIBLE_ICON;
        if (fits != bestFits) {
            return fits;
        }
        // Both are the same kind of usable: the larger one scales down better than the smaller
        // scales up.
        return (long) width * height > (long) bestWidth * bestHeight;
    }

    /** A property of the window, or null when it has never been set. */
    private static XProperty propertyOf(AtomTable atoms, XWindow window, String name) {
        int atom = atoms.intern(name, true);
        return atom == AtomTable.NONE ? null : window.properties().get(atom);
    }
}
