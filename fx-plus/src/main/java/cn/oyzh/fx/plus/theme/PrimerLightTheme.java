package cn.oyzh.fx.plus.theme;

import atlantafx.base.theme.PrimerLight;
import atlantafx.base.theme.Theme;
import cn.oyzh.fx.plus.FXStyle;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * @author oyzh
 * @since 2024/4/3
 */
public class PrimerLightTheme implements Theme, ThemeStyle {

    private static final PrimerLight THEME = new PrimerLight();

    @Override
    public String getName() {
        return THEME.getName();
    }

    @Override
    public String getDesc(Locale locale) {
        return THEME.getName();
        // return I18nHelper.themePrimerLight();
    }

    @Override
    public String getUserAgentStylesheet() {
        return THEME.getUserAgentStylesheet();
    }

    @Override
    public String getCompressedUserAgentStylesheet() {
        return FXStyle.ATLANTA_FX_PRIMER_LIGHT;
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
        return Color.valueOf("#0969da");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#24292f");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#ffffff");
    }
}
