package cn.oyzh.fx.plus.theme;

import atlantafx.base.theme.Theme;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * IntelliJ IDEA Light 主题
 *
 * @author oyzh
 * @since 2026/6/27
 */
public class IntelliJLightTheme implements Theme, ThemeStyle {

    private static final Theme THEME = Theme.of("IntelliJ Light", "/fx-plus/css/theme/intellij-light.css", false);

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
