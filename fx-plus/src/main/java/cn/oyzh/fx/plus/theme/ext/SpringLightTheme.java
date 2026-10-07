package cn.oyzh.fx.plus.theme.ext;

import com.dlsc.atlantafx.themes.SpringLight;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * Spring Light 扩展主题
 *
 * @author oyzh
 * @since 2026-10-06
 */
public class SpringLightTheme implements ThemeStyle {

    /** 底层主题实例 */
    private static final SpringLight THEME = new SpringLight();

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
        return Color.valueOf("#8c2058");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#081408");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#ffffff");
    }
}
