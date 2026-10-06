package cn.oyzh.fx.plus.theme.ext;

import com.dlsc.atlantafx.themes.FallLight;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * Fall Light 扩展主题
 *
 * @author oyzh
 * @since 2026/10/6
 */
public class FallLightTheme implements ThemeStyle {

    private static final FallLight THEME = new FallLight();

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
        return Color.valueOf("#98300c");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#100604");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#fdf8f0");
    }
}
