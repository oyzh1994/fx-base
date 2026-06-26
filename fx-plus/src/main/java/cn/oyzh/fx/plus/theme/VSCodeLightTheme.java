package cn.oyzh.fx.plus.theme;

import atlantafx.base.theme.Theme;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * VS Code Light+ 主题
 *
 * @author oyzh
 * @since 2026/6/27
 */
public class VSCodeLightTheme implements Theme, ThemeStyle {

    private static final Theme THEME = Theme.of("VS Code Light", "/fx-plus/css/theme/vscode-light.css", false);

    @Override
    public String getName() {
        return THEME.getName();
    }

    @Override
    public String getDesc(Locale locale) {
        return THEME.getName();
    }

    @Override
    public String getUserAgentStylesheet() {
        return THEME.getUserAgentStylesheet();
    }

    @Override
    public String getUserAgentStylesheetBSS() {
        return THEME.getUserAgentStylesheetBSS();
    }

    @Override
    public boolean isDarkMode() {
        return THEME.isDarkMode();
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
