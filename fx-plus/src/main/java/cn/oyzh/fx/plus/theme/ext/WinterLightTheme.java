package cn.oyzh.fx.plus.theme.ext;

import com.dlsc.atlantafx.themes.WinterLight;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * Winter Light 扩展主题
 *
 * @author oyzh
 * @since 2026/10/6
 */
public class WinterLightTheme implements ThemeStyle {

    /** 底层主题实例 */
    private static final WinterLight THEME = new WinterLight();

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
        return Color.valueOf("#1a4cae");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#060e22");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#ffffff");
    }
}
