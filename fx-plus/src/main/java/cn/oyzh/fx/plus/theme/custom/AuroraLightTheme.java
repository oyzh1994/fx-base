package cn.oyzh.fx.plus.theme.custom;

import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

/**
 * Aurora Light 主题
 * 特性：明亮冷白背景、清晰层级、与 Aurora Dark 共用交互语言
 *
 * @author oyzh
 * @since 2026-11-05
 */
public class AuroraLightTheme implements ThemeStyle {

    @Override
    public String getName() {
        return "Aurora Light";
    }

    @Override
    public String getUserAgentStylesheet() {
        return "/fx-plus/css/theme/aurora-light.css";
    }

    @Override
    public Color getAccentColor() {
        return Color.valueOf("#247edb");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#1f2937");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#f7f9fc");
    }
}
