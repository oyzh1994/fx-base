package cn.oyzh.fx.plus.theme.custom;

import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

/**
 * IntelliJ IDEA Light 主题
 *
 * @author oyzh
 * @since 2026/6/27
 */
public class IntelliJLightTheme implements ThemeStyle {

    @Override
    public String getName() {
        return "IntelliJ Light";
    }

    @Override
    public String getUserAgentStylesheet() {
        return "/fx-plus/css/theme/intellij-light.css";
    }

    @Override
    public boolean isDarkMode() {
        return false;
    }

    @Override
    public Color getAccentColor() {
        return Color.valueOf("#3489e3");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#2b313a");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#ffffff");
    }
}
