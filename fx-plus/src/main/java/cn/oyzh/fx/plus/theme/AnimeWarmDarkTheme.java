package cn.oyzh.fx.plus.theme;

import atlantafx.base.theme.Theme;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * 动漫温暖暗色主题
 * 特性：暖棕色调、柔和圆角、温馨氛围
 *
 * @author oyzh
 * @since 2026/6/27
 */
public class AnimeWarmDarkTheme implements Theme, ThemeStyle {

    private static final Theme THEME = Theme.of("Anime Warm Dark", "/fx-plus/css/theme/anime-warm-dark.css", true);

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
        return Color.valueOf("#ff8848");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#f0d8cc");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#1c100c");
    }
}
