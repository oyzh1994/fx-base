package cn.oyzh.fx.plus.theme;

import atlantafx.base.theme.Theme;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * 液态玻璃亮色主题
 * 特性：清澈磨砂、通透光影、优雅圆角
 *
 * @author oyzh
 * @since 2026/6/27
 */
public class LiquidGlassLightTheme implements Theme, ThemeStyle {

    private static final Theme THEME = Theme.of("Liquid Glass Light", "/fx-plus/css/theme/liquid-glass-light.css", false);

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
        return Color.valueOf("#3078d8");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#1c2c48");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#f8fbff");
    }
}
