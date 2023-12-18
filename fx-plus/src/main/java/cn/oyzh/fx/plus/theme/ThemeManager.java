package cn.oyzh.fx.plus.theme;

import cn.oyzh.fx.plus.stage.StageUtil;
import cn.oyzh.fx.plus.stage.StageWrapper;

import java.util.List;

/**
 * 主题管理器
 *
 * @author oyzh
 * @since 2023/12/18
 */
public class ThemeManager {

    public static Theme DEFAULT_THEME = Theme.PRIMER_LIGHT;

    private static Theme Current_Theme;

    public static Theme getCurrentTheme() {
        if (Current_Theme == null) {
            return DEFAULT_THEME;
        }
        return Current_Theme;
    }

    public static void setCurrentTheme(String themeName) {
        Theme theme = Theme.valueOf(themeName.toUpperCase());
        setCurrentTheme(theme);
    }

    public static void setCurrentTheme(Theme theme) {
        try {
            Current_Theme = theme;
            List<StageWrapper> wrappers = StageUtil.allStages();
            for (StageWrapper wrapper : wrappers) {
                wrapper.changeTheme(Current_Theme);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
