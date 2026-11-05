package cn.oyzh.fx.plus.theme.custom;

import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

/**
 * Aurora Dark 主题
 * 特性：石墨黑分层背景、冷蓝强调色、低干扰边框
 *
 * @author oyzh
 * @since 2026-11-05
 */
public class AuroraDarkTheme implements ThemeStyle {

    @Override
    public String getName() {
        return "Aurora Dark";
    }

    @Override
    public String getUserAgentStylesheet() {
        return "/fx-plus/css/theme/aurora-dark.css";
    }

    @Override
    public boolean isDarkMode() {
        return true;
    }

    @Override
    public Color getAccentColor() {
        return Color.valueOf("#6fb2ff");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#dce6f2");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#0f1620");
    }
}
