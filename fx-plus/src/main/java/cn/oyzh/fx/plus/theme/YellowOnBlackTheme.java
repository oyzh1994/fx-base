package cn.oyzh.fx.plus.theme;

import atlantafx.base.theme.Theme;
import javafx.scene.paint.Color;

/**
 * @author oyzh
 * @since 2024/4/3
 */
@Deprecated
public class YellowOnBlackTheme implements Theme, ThemeStyle {

    @Override
    public String getName() {
        return "Yellow On Black";
    }

//    @Override
//    public String getDesc(Locale locale) {
//        return this.getName();
//    }

    @Override
    public String getUserAgentStylesheet() {
        return "com/sun/javafx/scene/control/skin/caspian/caspian.css";
    }

    @Override
    public String getUserAgentStylesheetBSS() {
        return null;
    }

    @Override
    public boolean isDarkMode() {
        return true;
    }

    @Override
    public Color getAccentColor() {
        return Color.valueOf("#0096C9");
    }

    @Override
    public Color getForegroundColor() {
        return Color.YELLOW;
    }

    @Override
    public Color getBackgroundColor() {
        return Color.BLACK;
    }

    @Override
    public boolean isBuiltIn() {
        return true;
    }

    @Override
    public String getBuiltInName() {
        return "YELLOWONBLACK";
    }
}
