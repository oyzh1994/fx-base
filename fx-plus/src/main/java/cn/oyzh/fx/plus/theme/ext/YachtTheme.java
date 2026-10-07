package cn.oyzh.fx.plus.theme.ext;

import com.dlsc.atlantafx.themes.Yacht;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * Yacht 扩展主题
 *
 * @author oyzh
 * @since 2026/10/6
 */
public class YachtTheme implements ThemeStyle {

    /** 底层主题实例 */
    private static final Yacht THEME = new Yacht();

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
        return Color.valueOf("#245f73");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#262928");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#f2f0ef");
    }
}
