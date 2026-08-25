package cn.oyzh.fx.plus.theme;

import atlantafx.base.theme.Theme;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * 商务暗色主题
 * 特性：专业沉稳、精确利落、低调内敛
 *
 * @author oyzh
 * @since 2026/6/27
 */
public class BusinessDarkTheme implements Theme, ThemeStyle {

    private static final Theme THEME = Theme.of("Business Dark", "/fx-plus/css/theme/business-dark.css", true);

    @Override
    public String getName() {
        return THEME.getName();
    }

    @Override
    public String getDesc(Locale locale) {
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
        return Color.valueOf("#4890d8");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#dce2e8");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#121a24");
    }
}
