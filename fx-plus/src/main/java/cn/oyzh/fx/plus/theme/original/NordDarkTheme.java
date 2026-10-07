package cn.oyzh.fx.plus.theme.original;

import atlantafx.base.theme.NordDark;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * 北欧暗色主题
 *
 * @author oyzh
 * @since 2023-12-25
 */
public class NordDarkTheme implements ThemeStyle {

    /** 底层主题实例 */
    private static final NordDark THEME = new NordDark();

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
//        return FXStyle.ATLANTA_FX_NORD_DARK;
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
