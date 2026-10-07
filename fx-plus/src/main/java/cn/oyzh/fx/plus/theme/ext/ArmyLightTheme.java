package cn.oyzh.fx.plus.theme.ext;

import com.dlsc.atlantafx.themes.ArmyLight;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * Army Light 扩展主题
 *
 * @author oyzh
 * @since 2026-10-06
 */
public class ArmyLightTheme implements ThemeStyle {

    /** 底层主题实例 */
    private static final ArmyLight THEME = new ArmyLight();

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
        return Color.valueOf("#486610");
    }

    @Override
    public Color getForegroundColor() {
        return Color.valueOf("#0e1208");
    }

    @Override
    public Color getBackgroundColor() {
        return Color.valueOf("#e8eccc");
    }
}
