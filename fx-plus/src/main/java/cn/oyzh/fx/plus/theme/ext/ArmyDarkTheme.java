package cn.oyzh.fx.plus.theme.ext;

import com.dlsc.atlantafx.themes.ArmyDark;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * Army Dark 扩展主题
 *
 * @author oyzh
 * @since 2023-12-25
 */
public class ArmyDarkTheme implements ThemeStyle {

    /** 底层主题实例 */
    private static final ArmyDark THEME = new ArmyDark();

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
        return Color.valueOf("#7daa2a");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#d4d8b0");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#1c2214");
    }
}
