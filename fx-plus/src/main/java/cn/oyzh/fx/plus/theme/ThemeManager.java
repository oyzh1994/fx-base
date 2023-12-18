package cn.oyzh.fx.plus.theme;

import cn.oyzh.fx.plus.stage.StageUtil;
import cn.oyzh.fx.plus.stage.StageWrapper;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * 主题管理器
 *
 * @author oyzh
 * @since 2023/12/18
 */
@UtilityClass
public class ThemeManager {

    /**
     * 默认主题
     */
    public static Theme DEFAULT_THEME = Theme.PRIMER_LIGHT;

    /**
     * 当前主题
     */
    private static Theme Current_Theme;

    /**
     * 是否暗黑模式
     *
     * @return 结果
     */
    public static boolean isDarkMode() {
        return currentTheme().isDarkMode();
    }

    /**
     * 获取主题
     *
     * @return 当前主题
     */
    public static Theme currentTheme() {
        if (Current_Theme == null) {
            return DEFAULT_THEME;
        }
        return Current_Theme;
    }

    /**
     * 设置主题
     *
     * @param themeName 主题名称
     */
    public static void currentTheme(String themeName) {
        Theme theme;
        if (themeName == null) {
            theme = DEFAULT_THEME;
        } else {
            theme = Theme.valueOf(themeName.toUpperCase());
        }
        currentTheme(theme);
    }

    /**
     * 设置主题
     *
     * @param theme 主题
     */
    public static void currentTheme(Theme theme) {
        try {
            Current_Theme = theme;
            List<StageWrapper> wrappers = StageUtil.allStages();
            for (StageWrapper wrapper : wrappers) {
                wrapper.changeTheme(theme);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
