package cn.oyzh.fx.plus.theme.custom;

import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

/**
 * Cyberpunk Dark 主题
 *
 * @author oyzh
 * @since 2026-06-27
 */
public class CyberpunkDarkTheme implements ThemeStyle {

    @Override
    public String getName() {
        return "Cyberpunk Dark";
    }

    @Override
    public String getUserAgentStylesheet() {
        return "/fx-plus/css/theme/cyberpunk-dark.css";
    }

    @Override
    public boolean isDarkMode() {
        return true;
    }

    @Override
    public Color getAccentColor() {
        return Color.valueOf("#00e5ff");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#e0e8f0");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#0a0e17");
    }
}
