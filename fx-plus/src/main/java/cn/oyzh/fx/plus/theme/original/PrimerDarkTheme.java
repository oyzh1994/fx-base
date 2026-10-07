package cn.oyzh.fx.plus.theme.original;

import atlantafx.base.theme.PrimerDark;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * 基础暗色主题
 *
 * @author oyzh
 * @since 2023-12-25
 */
public class PrimerDarkTheme implements ThemeStyle {

    /** 底层主题实例 */
    private static final PrimerDark THEME = new PrimerDark();

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
//        return FXStyle.ATLANTA_FX_PRIMER_DARK;
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
