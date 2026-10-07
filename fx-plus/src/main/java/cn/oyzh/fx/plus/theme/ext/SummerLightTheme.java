package cn.oyzh.fx.plus.theme.ext;

import com.dlsc.atlantafx.themes.SummerLight;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * Summer Light 扩展主题
 *
 * @author oyzh
 * @since 2023-12-25
 */
public class SummerLightTheme implements ThemeStyle {

    /** 底层主题实例 */
    private static final SummerLight THEME = new SummerLight();

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
        return Color.valueOf("#005aac");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#04102e");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#ffffff");
    }
}
