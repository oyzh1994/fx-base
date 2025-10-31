package cn.oyzh.fx.plus.theme;

import atlantafx.base.theme.CupertinoLight;
import atlantafx.base.theme.Theme;
import cn.oyzh.fx.plus.FXStyle;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * @author oyzh
 * @since 2024/4/3
 */
public class CupertinoLightTheme implements Theme, ThemeStyle {

    private static final CupertinoLight THEME = new CupertinoLight();

    @Override
    public String getName() {
        return THEME.getName();
    }

    @Override
    public String getDesc(Locale locale) {
        return THEME.getName();
        // return I18nHelper.themeCupertinoLight();
    }

    @Override
    public String getUserAgentStylesheet() {
        return THEME.getUserAgentStylesheet();
    }

    @Override
    public String getCompressedUserAgentStylesheet() {
        return FXStyle.ATLANTA_FX_CUPERTINO_LIGHT;
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
        return Color.rgb(0, 122, 255);
    }

    @Override
    public Color getForegroundColor() {
        return Color.rgb(0, 0, 0);
    }

    @Override
    public Color getBackgroundColor() {
        return Color.rgb(255, 255, 255);
    }
}
