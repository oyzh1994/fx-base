package cn.oyzh.fx.plus.theme.custom;

import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

/**
 * 动漫温暖暗色主题
 * 特性：暖棕色调、柔和圆角、温馨氛围
 *
 * @author oyzh
 * @since 2026/6/27
 */
public class AnimeWarmDarkTheme implements ThemeStyle {

    @Override
    public String getName() {
        return "Anime Warm Dark";
    }

    @Override
    public String getUserAgentStylesheet() {
        return "/fx-plus/css/theme/anime-warm-dark.css";
    }

    @Override
    public boolean isDarkMode() {
        return true;
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
