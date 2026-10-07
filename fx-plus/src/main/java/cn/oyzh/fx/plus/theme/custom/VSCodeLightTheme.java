package cn.oyzh.fx.plus.theme.custom;

import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

/**
 * VS Code Light+ 主题
 *
 * @author oyzh
 * @since 2023-12-25
 */
public class VSCodeLightTheme implements ThemeStyle {

    @Override
    public String getName() {
        return "VS Code Light";
    }

    @Override
    public String getUserAgentStylesheet() {
        return "/fx-plus/css/theme/vscode-light.css";
    }

    @Override
    public boolean isDarkMode() {
        return false;
    }

    @Override
    public Color getAccentColor() {
        return Color.valueOf("#0078d4");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#333333");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#ffffff");
    }
}
