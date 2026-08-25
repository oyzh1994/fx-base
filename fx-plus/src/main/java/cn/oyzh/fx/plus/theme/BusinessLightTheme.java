package cn.oyzh.fx.plus.theme;

import atlantafx.base.theme.Theme;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * 商务亮色主题
 * 特性：清晰专业、简洁明快、企业质感
 *
 * @author oyzh
 * @since 2026/6/27
 */
public class BusinessLightTheme implements Theme, ThemeStyle {

    private static final Theme THEME = Theme.of("Business Light", "/fx-plus/css/theme/business-light.css", false);

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
        return Color.valueOf("#2870b8");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#222a34");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#f8fafc");
    }
}
