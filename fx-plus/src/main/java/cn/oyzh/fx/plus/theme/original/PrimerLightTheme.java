package cn.oyzh.fx.plus.theme.original;

import atlantafx.base.theme.PrimerLight;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * 基础亮色主题
 *
 * @author oyzh
 * @since 2023-12-25
 */
public class PrimerLightTheme implements ThemeStyle {

    /** 底层主题实例 */
    private static final PrimerLight THEME = new PrimerLight();

    @Override
    public String getName() {
        return THEME.getName();
    }

    @Override
    public String getUserAgentStylesheet() {
        return THEME.getUserAgentStylesheet();
    }

//    @Override
//    public String getCompressedUserAgentStylesheet() {
//        return FXStyle.ATLANTA_FX_PRIMER_LIGHT;
//    }

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
