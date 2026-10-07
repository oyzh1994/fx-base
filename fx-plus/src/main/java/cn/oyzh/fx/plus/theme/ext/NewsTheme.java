package cn.oyzh.fx.plus.theme.ext;

import com.dlsc.atlantafx.themes.News;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * News 扩展主题
 *
 * @author oyzh
 * @since 2026/10/6
 */
public class NewsTheme implements ThemeStyle {

    /** 底层主题实例 */
    private static final News THEME = new News();

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
        return Color.valueOf("#818cf8");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#f1f5f9");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#0f172a");
    }
}
