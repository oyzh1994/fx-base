package cn.oyzh.fx.plus.theme.ext;

import com.dlsc.atlantafx.themes.GithubLightColorblind;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * GitHub Light Colorblind 扩展主题
 *
 * @author oyzh
 * @since 2026-10-06
 */
public class GithubLightColorblindTheme implements ThemeStyle {

    /** 底层主题实例 */
    private static final GithubLightColorblind THEME = new GithubLightColorblind();

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
        return Color.valueOf("#0969da");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#1f2328");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#ffffff");
    }
}
