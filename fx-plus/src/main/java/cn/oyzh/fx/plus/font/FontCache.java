package cn.oyzh.fx.plus.font;

import javafx.scene.text.Font;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author oyzh
 * @since 2025-02-08
 */
public class FontCache {

    private final List<Font> fonts = new CopyOnWriteArrayList<>();

    public Font get(Font font) {
        for (Font font1 : this.fonts) {
            if (FontUtil.isSameFont(font1, font)) {
                return font1;
            }
        }
        return null;
    }

    public void clear() {
        this.fonts.clear();
    }

    public void add(Font font) {
        this.fonts.add(font);
    }

    public void remove(Font font) {
        this.fonts.removeIf(font1 -> FontUtil.isSameFont(font1, font));
    }

    public boolean contains(Font font) {
        for (Font font1 : this.fonts) {
            if (FontUtil.isSameFont(font1, font)) {
                return true;
            }
        }
        return false;
    }
}
