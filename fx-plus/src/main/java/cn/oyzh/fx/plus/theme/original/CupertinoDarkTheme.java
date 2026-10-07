package cn.oyzh.fx.plus.theme.original;

import atlantafx.base.theme.CupertinoDark;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * 库比蒂诺暗色主题
 *
 * @author oyzh
 * @since 2024/4/3
 */
public class CupertinoDarkTheme implements ThemeStyle {

    /** 底层主题实例 */
    private static final CupertinoDark THEME = new CupertinoDark();

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
        return Color.valueOf("#2f96ff");
    }

    @Override
    public Color getForegroundColor() {
        return Color.rgb(255, 255, 255);
    }

    @Override
    public Color getBackgroundColor() {
        return Color.rgb(28, 28, 30);
    }
}
