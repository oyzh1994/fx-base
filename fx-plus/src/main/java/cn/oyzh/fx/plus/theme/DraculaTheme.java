package cn.oyzh.fx.plus.theme;

import atlantafx.base.theme.Dracula;
import atlantafx.base.theme.Theme;
import javafx.scene.paint.Color;

/**
 * @author oyzh
 * @since 2024/4/3
 */
public class DraculaTheme implements Theme, ThemeStyle {

    private static final Dracula THEME = new Dracula();

    @Override
    public String getName() {
        return THEME.getName();
    }

    @Override
    public String getDesc() {
        return "暗黑德古拉";
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
        return Color.valueOf("#9580ff");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#f8f8f2");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#282a36");
    }
}
