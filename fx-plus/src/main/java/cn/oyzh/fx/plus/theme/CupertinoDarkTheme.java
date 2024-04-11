package cn.oyzh.fx.plus.theme;

import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.Theme;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * @author oyzh
 * @since 2024/4/3
 */
public class CupertinoDarkTheme implements Theme, ThemeStyle {

    private static final CupertinoDark THEME = new CupertinoDark();

    @Override
    public String getName() {
        return THEME.getName();
    }

    @Override
    public String getDesc(Locale locale) {
        if (locale == Locale.TRADITIONAL_CHINESE) {
            return "暗黑庫比蒂諾";
        } else if (locale == Locale.SIMPLIFIED_CHINESE) {
            return "暗黑库比蒂诺";
        }
        return "Cupertino Dark";
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
