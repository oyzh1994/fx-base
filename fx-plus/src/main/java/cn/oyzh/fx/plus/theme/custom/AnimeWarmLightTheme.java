package cn.oyzh.fx.plus.theme.custom;

import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

/**
 * 动漫温暖亮色主题
 * 特性：柔和暖白、奶油色调、温馨治愈
 *
 * @author oyzh
 * @since 2026-06-27
 */
public class AnimeWarmLightTheme implements ThemeStyle {

    @Override
    public String getName() {
        return "Anime Warm Light";
    }

    @Override
    public String getUserAgentStylesheet() {
        return "/fx-plus/css/theme/anime-warm-light.css";
    }

    @Override
    public boolean isDarkMode() {
        return false;
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
