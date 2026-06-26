package cn.oyzh.fx.plus.theme;

import atlantafx.base.theme.Theme;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * 液态玻璃暗色主题
 * 特性：半透明磨砂、柔和辉光、圆角玻璃质感
 *
 * @author oyzh
 * @since 2026/6/27
 */
public class LiquidGlassDarkTheme implements Theme, ThemeStyle {

    private static final Theme THEME = Theme.of("Liquid Glass Dark", "/fx-plus/css/theme/liquid-glass-dark.css", true);

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
        return Color.valueOf("#589cff");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#dce8f8");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#0f1820");
    }
}
