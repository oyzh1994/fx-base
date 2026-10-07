package cn.oyzh.fx.plus.theme.ext;

import com.dlsc.atlantafx.themes.SpringDark;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * Spring Dark 扩展主题
 *
 * @author oyzh
 * @since 2026-10-06
 */
public class SpringDarkTheme implements ThemeStyle {

    /** 底层主题实例 */
    private static final SpringDark THEME = new SpringDark();

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
        return Color.valueOf("#f060b0");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#c8f0c8");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#0c1a10");
    }
}
