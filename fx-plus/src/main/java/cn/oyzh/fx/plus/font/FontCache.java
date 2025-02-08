package cn.oyzh.fx.plus.font;

import cn.oyzh.common.cache.Cache;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author oyzh
 * @since 2025-02-08
 */
public class FontCache implements Cache<String, Font> {

    private final Map<String, Font> cache = new ConcurrentHashMap<>();

    @Override
    public Font get(String key) {
        return this.cache.get(key);
    }

    public Font get(Font font) {
        return this.get(this.getKey(font));
    }

    @Override
    public void clear() {
        this.cache.clear();
    }

    @Override
    public void put(String key, Font value) {
        this.cache.put(key, value);
    }

    public void put(Font font) {
        this.put(this.getKey(font), font);
    }

    @Override
    public void remove(String key) {
        this.cache.remove(key);
    }

    public void remove(Font font) {
        this.remove(this.getKey(font));
    }

    @Override
    public boolean containsKey(String key) {
        return this.cache.containsKey(key);
    }

    public boolean containsKey(Font font) {
        return this.containsKey(this.getKey(font));
    }

    private String getKey(Font font) {
        FontWeight weight = FontUtil.getWeight(font.getStyle());
        return font.getFamily() + ":" + weight.getWeight() + ":" + font.getSize();
    }
}
