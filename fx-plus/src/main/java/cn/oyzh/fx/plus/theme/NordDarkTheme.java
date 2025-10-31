package cn.oyzh.fx.plus.theme;

import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.Theme;
import cn.oyzh.fx.plus.FXStyle;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * @author oyzh
 * @since 2024/4/3
 */
public class NordDarkTheme implements Theme, ThemeStyle {

    private static final NordDark THEME = new NordDark();

    @Override
    public String getName() {
        return THEME.getName();
    }

    @Override
    public String getDesc(Locale locale) {
        return THEME.getName();
        // return I18nHelper.themeNordDark();
    }

    @Override
    public String getUserAgentStylesheet() {
        return THEME.getUserAgentStylesheet();
    }

    @Override
    public String getCompressedUserAgentStylesheet() {
        return FXStyle.ATLANTA_FX_NORD_DARK;
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
        return Color.valueOf("#98aeca");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#ECEFF4");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#2E3440");
    }
}
