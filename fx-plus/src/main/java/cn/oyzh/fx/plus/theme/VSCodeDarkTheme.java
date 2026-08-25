package cn.oyzh.fx.plus.theme;

import atlantafx.base.theme.Theme;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * VS Code Dark+ 主题
 *
 * @author oyzh
 * @since 2026/6/27
 */
public class VSCodeDarkTheme implements Theme, ThemeStyle {

    private static final Theme THEME = Theme.of("VS Code Dark", "/fx-plus/css/theme/vscode-dark.css", true);

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
