package cn.oyzh.fx.plus.theme.custom;

import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

/**
 * IntelliJ IDEA Dark 主题
 *
 * @author oyzh
 * @since 2026-06-27
 */
public class IntelliJDarkTheme implements ThemeStyle {

    @Override
    public String getName() {
        return "IntelliJ Dark";
    }

    @Override
    public String getUserAgentStylesheet() {
        return "/fx-plus/css/theme/intellij-dark.css";
    }

    @Override
    public boolean isDarkMode() {
        return true;
    }

    @Override
    public Color getAccentColor() {
        return Color.valueOf("#82b1ff");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#bbbbbb");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#2b2b2b");
    }
}
