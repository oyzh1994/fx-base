package cn.oyzh.fx.plus.theme.ext;

import com.dlsc.atlantafx.themes.BlueDark;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * Blue Dark 扩展主题
 *
 * @author oyzh
 * @since 2026/10/6
 */
public class BlueDarkTheme implements ThemeStyle {

    /** 底层主题实例 */
    private static final BlueDark THEME = new BlueDark();

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
        return Color.valueOf("#5aabdc");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#f4f6f9");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#142342");
    }
}
