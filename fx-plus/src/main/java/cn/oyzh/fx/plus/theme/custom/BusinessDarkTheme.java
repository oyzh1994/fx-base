package cn.oyzh.fx.plus.theme.custom;

import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

/**
 * 商务暗色主题
 * 特性：专业沉稳、精确利落、低调内敛
 *
 * @author oyzh
 * @since 2023-12-25
 */
public class BusinessDarkTheme implements ThemeStyle {

    @Override
    public String getName() {
        return "Business Dark";
    }

    @Override
    public String getUserAgentStylesheet() {
        return "/fx-plus/css/theme/business-dark.css";
    }

    @Override
    public boolean isDarkMode() {
        return true;
    }

    @Override
    public Color getAccentColor() {
        return Color.valueOf("#4890d8");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#dce2e8");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#121a24");
    }
}
