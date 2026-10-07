package cn.oyzh.fx.plus.theme.ext;

import com.dlsc.atlantafx.themes.WinterDark;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * Winter Dark 扩展主题
 *
 * @author oyzh
 * @since 2026/10/6
 */
public class WinterDarkTheme implements ThemeStyle {

    /** 底层主题实例 */
    private static final WinterDark THEME = new WinterDark();

    @Override
    public String getName() {
        return THEME.getName();
    }

    @Override
    public String getUserAgentStylesheet() {
        return THEME.getUserAgentStylesheet();
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
        return Color.valueOf("#1898d4");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#d0eeff");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#080c18");
    }
}
