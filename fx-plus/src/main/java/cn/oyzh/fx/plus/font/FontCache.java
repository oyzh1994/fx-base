package cn.oyzh.fx.plus.font;

import javafx.scene.text.Font;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 字体缓存
 *
 * @author oyzh
 * @since 2025-02-08
 */
public class FontCache implements AutoCloseable {

    /**
     * 字体列表
     */
    private final List<Font> fonts = new CopyOnWriteArrayList<>();

    /**
     * 获取字体
     *
     * @param font 字体
     * @return 缓存字体
     */
    public Font get(Font font) {
        for (Font font1 : this.fonts) {
            if (FontUtil.isSameFont(font1, font)) {
                return font1;
            }
        }
        return null;
    }

    /**
     * 添加字体
     *
     * @param font 字体
     */
    public void add(Font font) {
        this.fonts.add(font);
    }

    /**
     * 移除字体
     *
     * @param font 字体
     */
    public void remove(Font font) {
        this.fonts.removeIf(font1 -> FontUtil.isSameFont(font1, font));
    }

    /**
     * 是否包含字体
     *
     * @param font 字体
     * @return 结果
     */
    public boolean contains(Font font) {
        for (Font font1 : this.fonts) {
            if (FontUtil.isSameFont(font1, font)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void close() throws Exception {
        this.fonts.clear();
    }
}
