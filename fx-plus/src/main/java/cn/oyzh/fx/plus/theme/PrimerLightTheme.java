package cn.oyzh.fx.plus.theme;

import atlantafx.base.theme.PrimerLight;
import atlantafx.base.theme.Theme;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * @author oyzh
 * @since 2024/4/3
 */
public class PrimerLightTheme implements Theme, ThemeStyle {

    private static final PrimerLight THEME = new PrimerLight();

    @Override
    public String getName() {
        return THEME.getName();
    }

    @Override
    public String getDesc(Locale locale) {
        if (locale == Locale.TRADITIONAL_CHINESE) {
            return "明亮基礎";
        } else if (locale == Locale.SIMPLIFIED_CHINESE) {
            return "明亮基础";
        }
        return "Primer Light";
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
        return Color.valueOf("#0969da");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#24292f");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#ffffff");
    }
}
