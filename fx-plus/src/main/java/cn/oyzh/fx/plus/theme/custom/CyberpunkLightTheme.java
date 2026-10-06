package cn.oyzh.fx.plus.theme.custom;

import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

/**
 * Cyberpunk Light 主题
 *
 * @author oyzh
 * @since 2026/6/27
 */
public class CyberpunkLightTheme implements ThemeStyle {

    @Override
    public String getName() {
        return "Cyberpunk Light";
    }

    @Override
    public String getUserAgentStylesheet() {
        return "/fx-plus/css/theme/cyberpunk-light.css";
    }

    @Override
    public boolean isDarkMode() {
        return false;
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
