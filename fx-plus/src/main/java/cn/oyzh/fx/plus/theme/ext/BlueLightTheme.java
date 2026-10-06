package cn.oyzh.fx.plus.theme.ext;

import com.dlsc.atlantafx.themes.BlueLight;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * Blue Light 扩展主题
 *
 * @author oyzh
 * @since 2026/10/6
 */
public class BlueLightTheme implements ThemeStyle {

    private static final BlueLight THEME = new BlueLight();

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
        return Color.valueOf("#004273");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#0a1530");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#ffffff");
    }
}
