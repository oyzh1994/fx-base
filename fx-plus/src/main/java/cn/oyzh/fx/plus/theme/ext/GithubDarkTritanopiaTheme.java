package cn.oyzh.fx.plus.theme.ext;

import com.dlsc.atlantafx.themes.GithubDarkTritanopia;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * GitHub Dark Tritanopia 扩展主题
 *
 * @author oyzh
 * @since 2026-10-06
 */
public class GithubDarkTritanopiaTheme implements ThemeStyle {

    /** 底层主题实例 */
    private static final GithubDarkTritanopia THEME = new GithubDarkTritanopia();

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
        return Color.valueOf("#4493f8");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#f0f6fc");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#0d1117");
    }
}
