package cn.oyzh.fx.plus.theme;

import atlantafx.base.theme.Theme;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * Cyberpunk Light 主题
 *
 * @author oyzh
 * @since 2026/6/27
 */
public class CyberpunkLightTheme implements Theme, ThemeStyle {

    private static final Theme THEME = Theme.of("Cyberpunk Light", "/fx-plus/css/theme/cyberpunk-light.css", false);

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
        return Color.valueOf("#0097a7");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#1a2332");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#f5f7fa");
    }
}
