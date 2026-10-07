package cn.oyzh.fx.plus.theme.ext;

import com.dlsc.atlantafx.themes.FallDark;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * Fall Dark 扩展主题
 *
 * @author oyzh
 * @since 2026-10-06
 */
public class FallDarkTheme implements ThemeStyle {

    /** 底层主题实例 */
    private static final FallDark THEME = new FallDark();

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
        return Color.valueOf("#f09810");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#f8e0b8");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#1e0c06");
    }
}
