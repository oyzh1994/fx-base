package cn.oyzh.fx.plus.theme.original;

import atlantafx.base.theme.CupertinoLight;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * 库比蒂诺亮色主题
 *
 * @author oyzh
 * @since 2024-04-03
 */
public class CupertinoLightTheme implements ThemeStyle {

    /** 底层主题实例 */
    private static final CupertinoLight THEME = new CupertinoLight();

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
//        return FXStyle.ATLANTA_FX_CUPERTINO_LIGHT;
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
