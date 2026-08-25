package cn.oyzh.fx.plus.theme;

import atlantafx.base.theme.Theme;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * 动漫温暖亮色主题
 * 特性：柔和暖白、奶油色调、温馨治愈
 *
 * @author oyzh
 * @since 2026/6/27
 */
public class AnimeWarmLightTheme implements Theme, ThemeStyle {

    private static final Theme THEME = Theme.of("Anime Warm Light", "/fx-plus/css/theme/anime-warm-light.css", false);

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
        return Color.valueOf("#d87830");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#382c20");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#fef8f4");
    }
}
