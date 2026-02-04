package cn.oyzh.fx.plus.theme;

import atlantafx.base.theme.Theme;
import javafx.application.Application;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * @author oyzh
 * @since 2024/4/3
 */
public class BlackOnWhiteTheme implements Theme, ThemeStyle {

    @Override
    public String getName() {
        return "Black On White";
    }

    @Override
    public String getDesc(Locale locale) {
        return this.getName();
    }

    @Override
    public String getUserAgentStylesheet() {
        return "com/sun/javafx/scene/control/skin/caspian/caspian.css";
    }

    @Override
    public String getUserAgentStylesheetBSS() {
        return null;
    }

    @Override
    public boolean isDarkMode() {
        return false;
    }

    @Override
    public Color getAccentColor() {
        return Color.valueOf("#0096C9");
    }

    @Override
    public Color getForegroundColor() {
        return Color.BLACK;
    }

    @Override
    public Color getBackgroundColor() {
        return Color.WHITE;
    }

    @Override
    public boolean isBuiltIn() {
        return true;
    }

    @Override
    public String getBuiltInName() {
        return Application.STYLESHEET_MODENA;
    }
}
