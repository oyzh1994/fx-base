package cn.oyzh.fx.plus.theme.original;

import atlantafx.base.theme.NordLight;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * 北欧亮色主题
 *
 * @author oyzh
 * @since 2024/4/3
 */
public class NordLightTheme implements ThemeStyle {

    /** 底层主题实例 */
    private static final NordLight THEME = new NordLight();

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
//        return FXStyle.ATLANTA_FX_NORD_LIGHT;
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
