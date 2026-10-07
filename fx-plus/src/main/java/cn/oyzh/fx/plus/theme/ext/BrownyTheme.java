package cn.oyzh.fx.plus.theme.ext;

import com.dlsc.atlantafx.themes.Browny;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * Browny 扩展主题
 *
 * @author oyzh
 * @since 2026-10-06
 */
public class BrownyTheme implements ThemeStyle {

    /** 底层主题实例 */
    private static final Browny THEME = new Browny();

    @Override
    public String getName() {
        return THEME.getName();
    }

    @Override
    public String getUserAgentStylesheet() {
        return THEME.getUserAgentStylesheet();
    }

    @Override
    public String getUserAgentStylesheetBSS() {
        return THEME.getUserAgentStylesheetBSS();
    }

    @Override
    public boolean isDarkMode() {
        return THEME.isDarkMode();
    }

    @Override
    public Color getAccentColor() {
        return Color.valueOf("#f3da91");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#f5efe8");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#332524");
    }
}
