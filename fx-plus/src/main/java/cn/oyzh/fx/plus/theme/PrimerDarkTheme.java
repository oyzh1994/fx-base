package cn.oyzh.fx.plus.theme;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.Theme;
import javafx.scene.paint.Color;

/**
 * @author oyzh
 * @since 2024/4/3
 */
public class PrimerDarkTheme implements Theme, ThemeStyle {

    private static final PrimerDark THEME = new PrimerDark();

    @Override
    public String getName() {
        return THEME.getName();
    }

    @Override
    public String getDesc() {
        return "暗黑基础";
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
        return Color.valueOf("#58a6ff");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#c9d1d9");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#0d1117");
    }
}
