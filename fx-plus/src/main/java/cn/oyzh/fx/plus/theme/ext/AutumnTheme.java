package cn.oyzh.fx.plus.theme.ext;

import com.dlsc.atlantafx.themes.Autumn;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * Autumn 扩展主题
 *
 * @author oyzh
 * @since 2026/10/6
 */
public class AutumnTheme implements ThemeStyle {

    /** 底层主题实例 */
    private static final Autumn THEME = new Autumn();

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
        return Color.valueOf("#f9a86b");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#b8dee9");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#304c64");
    }
}
