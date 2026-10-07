package cn.oyzh.fx.plus.theme.custom;

import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

/**
 * 液态玻璃亮色主题
 * 特性：清澈磨砂、通透光影、优雅圆角
 *
 * @author oyzh
 * @since 2026-06-27
 */
public class LiquidGlassLightTheme implements ThemeStyle {

    @Override
    public String getName() {
        return "Liquid Glass Light";
    }

    @Override
    public String getUserAgentStylesheet() {
        return "/fx-plus/css/theme/liquid-glass-light.css";
    }

    @Override
    public boolean isDarkMode() {
        return false;
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
