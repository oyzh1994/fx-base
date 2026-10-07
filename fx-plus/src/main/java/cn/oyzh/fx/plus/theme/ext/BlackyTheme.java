package cn.oyzh.fx.plus.theme.ext;

import com.dlsc.atlantafx.themes.Blacky;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * Blacky 扩展主题
 *
 * @author oyzh
 * @since 2023-12-25
 */
public class BlackyTheme implements ThemeStyle {

    /** 底层主题实例 */
    private static final Blacky THEME = new Blacky();

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
        return Color.valueOf("#D98C42");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#f2f2f2");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#000000");
    }
}
