package cn.oyzh.fx.plus.theme.custom;

import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

/**
 * 液态玻璃暗色主题
 * 特性：半透明磨砂、柔和辉光、圆角玻璃质感
 *
 * @author oyzh
 * @since 2026/6/27
 */
public class LiquidGlassDarkTheme implements ThemeStyle {

    @Override
    public String getName() {
        return "Liquid Glass Dark";
    }

    @Override
    public String getUserAgentStylesheet() {
        return "/fx-plus/css/theme/liquid-glass-dark.css";
    }

    @Override
    public boolean isDarkMode() {
        return true;
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
