package cn.oyzh.fx.plus.theme.custom;

import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

/**
 * VS Code Dark+ 主题
 *
 * @author oyzh
 * @since 2026-06-27
 */
public class VSCodeDarkTheme implements ThemeStyle {

    @Override
    public String getName() {
        return "VS Code Dark";
    }

    @Override
    public String getUserAgentStylesheet() {
        return "/fx-plus/css/theme/vscode-dark.css";
    }

    @Override
    public boolean isDarkMode() {
        return true;
    }

    @Override
    public Color getAccentColor() {
        return Color.valueOf("#4fc1ff");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#d4d4d4");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#1e1e1e");
    }
}
