package cn.oyzh.fx.plus.theme.ext;

import com.dlsc.atlantafx.themes.GithubSoftDark;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * GitHub Soft Dark 扩展主题
 *
 * @author oyzh
 * @since 2026-10-06
 */
public class GithubSoftDarkTheme implements ThemeStyle {

    /** 底层主题实例 */
    private static final GithubSoftDark THEME = new GithubSoftDark();

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
        return Color.valueOf("#539bf5");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#adbac7");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#22272e");
    }
}
