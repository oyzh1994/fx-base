package cn.oyzh.fx.xserver.resource;

import cn.oyzh.fx.xserver.client.ProtocolException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The global atom table.
 *
 * <p>Atoms are interned strings: clients turn a name into a small integer once and use that
 * everywhere afterwards. The first 68 are fixed by the protocol and every server must answer them
 * identically, so they are pre-populated in the order given by {@code Xatom.h}.
 */
public final class AtomTable {

    public static final int NONE = 0;

    private static final String[] PREDEFINED = {
            null,                        // 0 is None
            "PRIMARY", "SECONDARY", "ARC", "ATOM", "BITMAP", "CARDINAL", "COLORMAP", "CURSOR",
            "CUT_BUFFER0", "CUT_BUFFER1", "CUT_BUFFER2", "CUT_BUFFER3", "CUT_BUFFER4",
            "CUT_BUFFER5", "CUT_BUFFER6", "CUT_BUFFER7", "DRAWABLE", "FONT", "INTEGER", "PIXMAP",
            "POINT", "RECTANGLE", "RESOURCE_MANAGER", "RGB_COLOR_MAP", "RGB_BEST_MAP",
            "RGB_BLUE_MAP", "RGB_DEFAULT_MAP", "RGB_GRAY_MAP", "RGB_GREEN_MAP", "RGB_RED_MAP",
            "STRING", "VISUALID", "WINDOW", "WM_COMMAND", "WM_HINTS", "WM_CLIENT_MACHINE",
            "WM_ICON_NAME", "WM_ICON_SIZE", "WM_NAME", "WM_NORMAL_HINTS", "WM_SIZE_HINTS",
            "WM_ZOOM_HINTS", "MIN_SPACE", "NORM_SPACE", "MAX_SPACE", "END_SPACE",
            "SUPERSCRIPT_X", "SUPERSCRIPT_Y", "SUBSCRIPT_X", "SUBSCRIPT_Y", "UNDERLINE_POSITION",
            "UNDERLINE_THICKNESS", "STRIKEOUT_ASCENT", "STRIKEOUT_DESCENT", "ITALIC_ANGLE",
            "X_HEIGHT", "QUAD_WIDTH", "WEIGHT", "POINT_SIZE", "RESOLUTION", "COPYRIGHT",
            "NOTICE", "FONT_NAME", "FAMILY_NAME", "FULL_NAME", "CAP_HEIGHT", "WM_CLASS",
            "WM_TRANSIENT_FOR",
    };

    private final List<String> names = new ArrayList<>();
    private final Map<String, Integer> ids = new HashMap<>();

    public AtomTable() {
        names.add(null);
        for (int i = 1; i < PREDEFINED.length; i++) {
            names.add(PREDEFINED[i]);
            ids.put(PREDEFINED[i], i);
        }
    }

    /** Interns {@code name}, creating it unless {@code onlyIfExists} is set. */
    public synchronized int intern(String name, boolean onlyIfExists) {
        Integer existing = ids.get(name);
        if (existing != null) {
            return existing;
        }
        if (onlyIfExists) {
            return NONE;
        }
        int id = names.size();
        names.add(name);
        ids.put(name, id);
        return id;
    }

    public synchronized int intern(String name) {
        return intern(name, false);
    }

    /** The name of an atom, or null when it was never interned. */
    public synchronized String name(int atom) {
        if (atom <= 0 || atom >= names.size()) {
            return null;
        }
        return names.get(atom);
    }

    public synchronized String requireName(int atom) {
        String name = name(atom);
        if (name == null) {
            throw ProtocolException.badAtom(atom);
        }
        return name;
    }

    public synchronized int count() {
        return names.size();
    }

    /** Id of a predefined atom, looked up by name. Never zero. */
    public int predefined(String name) {
        Integer id = ids.get(name);
        if (id == null) {
            throw new IllegalArgumentException("no predefined atom " + name);
        }
        return id;
    }
}
