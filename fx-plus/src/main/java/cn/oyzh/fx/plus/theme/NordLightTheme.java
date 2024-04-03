package cn.oyzh.fx.plus.theme;

import atlantafx.base.theme.NordLight;
import atlantafx.base.theme.Theme;
import javafx.scene.paint.Color;

/**
 * @author oyzh
 * @since 2024/4/3
 */
public class NordLightTheme implements Theme, ThemeStyle {

    private static final NordLight THEME = new NordLight();

    @Override
    public String getName() {
        return THEME.getName();
    }

    @Override
    public String getDesc() {
        return "明亮北欧";
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
        return Color.valueOf("#537297");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#2E3440");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#fafafc");
    }
}
