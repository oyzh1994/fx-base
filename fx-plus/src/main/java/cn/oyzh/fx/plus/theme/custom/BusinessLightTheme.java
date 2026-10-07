package cn.oyzh.fx.plus.theme.custom;

import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

/**
 * 商务亮色主题
 * 特性：清晰专业、简洁明快、企业质感
 *
 * @author oyzh
 * @since 2026-06-27
 */
public class BusinessLightTheme implements ThemeStyle {

    @Override
    public String getName() {
        return "Business Light";
    }

    @Override
    public String getUserAgentStylesheet() {
        return "/fx-plus/css/theme/business-light.css";
    }

    @Override
    public boolean isDarkMode() {
        return false;
    }

    @Override
    public Color getAccentColor() {
        return Color.valueOf("#2870b8");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#222a34");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#f8fafc");
    }
}
