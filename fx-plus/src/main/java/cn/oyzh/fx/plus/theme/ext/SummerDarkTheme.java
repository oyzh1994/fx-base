package cn.oyzh.fx.plus.theme.ext;

import com.dlsc.atlantafx.themes.SummerDark;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * Summer Dark 扩展主题
 *
 * @author oyzh
 * @since 2026/10/6
 */
public class SummerDarkTheme implements ThemeStyle {

    /** 底层主题实例 */
    private static final SummerDark THEME = new SummerDark();

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
        return Color.valueOf("#d48800");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#d0e8ff");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#081430");
    }
}
