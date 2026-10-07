package cn.oyzh.fx.plus.theme.ext;

import com.dlsc.atlantafx.themes.GithubDarkColorblind;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * GitHub Dark Colorblind 扩展主题
 *
 * @author oyzh
 * @since 2023-12-25
 */
public class GithubDarkColorblindTheme implements ThemeStyle {

    /** 底层主题实例 */
    private static final GithubDarkColorblind THEME = new GithubDarkColorblind();

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
