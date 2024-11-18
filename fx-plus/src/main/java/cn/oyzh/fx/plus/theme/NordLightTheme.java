package cn.oyzh.fx.plus.theme;

import atlantafx.base.theme.NordLight;
import atlantafx.base.theme.Theme;
import cn.oyzh.fx.plus.FXStyle;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.paint.Color;

import java.util.Locale;

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
    public String getDesc(Locale locale) {
        // if (locale == Locale.TRADITIONAL_CHINESE) {
        //     return "明亮北歐";
        // } else if (locale == Locale.SIMPLIFIED_CHINESE) {
        //     return "明亮北欧";
        // }
        // return "Nord Light";
        return I18nHelper.themeNordLight();
    }

    @Override
    public String getUserAgentStylesheet() {
        return THEME.getUserAgentStylesheet();
    }

    @Override
    public String getCompressedUserAgentStylesheet() {
        return FXStyle.ATLANTA_FX_NORD_LIGHT;
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
