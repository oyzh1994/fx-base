package cn.oyzh.fx.plus.theme.ext;

import com.dlsc.atlantafx.themes.NavyDark;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * Navy Dark 扩展主题
 *
 * @author oyzh
 * @since 2026-10-06
 */
public class NavyDarkTheme implements ThemeStyle {

    /** 底层主题实例 */
    private static final NavyDark THEME = new NavyDark();

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
        return Color.valueOf("#e8cf82");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#eef0f6");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#1a2744");
    }
}
