package cn.oyzh.fx.plus.font;

import javafx.scene.text.Font;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author oyzh
 * @since 2025-02-08
 */
public class FontCache {

    private final List<Font> cache = new CopyOnWriteArrayList<>();

    public Font get(Font font) {
        for (Font font1 : this.cache) {
            if (FontUtil.isSameFont(font1, font)) {
                return font1;
            }
        }
        return null;
    }

    public void clear() {
        this.cache.clear();
    }

    public void add(Font font) {
        this.cache.add(font);
    }

    public void remove(Font font) {
        this.cache.removeIf(font1 -> FontUtil.isSameFont(font1, font));
    }

    public boolean contains(Font font) {
        for (Font font1 : this.cache) {
            if (FontUtil.isSameFont(font1, font)) {
                return true;
            }
        }
        return false;
    }
}
