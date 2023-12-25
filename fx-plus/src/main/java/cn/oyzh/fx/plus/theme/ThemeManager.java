package cn.oyzh.fx.plus.theme;

import cn.oyzh.fx.plus.stage.StageUtil;
import cn.oyzh.fx.plus.stage.StageWrapper;
import javafx.scene.Node;
import javafx.scene.Parent;
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
    public static ThemeType defaultThemeType = ThemeType.PRIMER_LIGHT;

    /**
     * 当前主题
     */
    private static ThemeType current_ThemeType;

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
    public static ThemeType currentTheme() {
        if (current_ThemeType == null) {
            return defaultThemeType;
        }
        return current_ThemeType;
    }

    /**
     * 设置主题
     *
     * @param themeName 主题名称
     */
    public static void currentTheme(String themeName) {
        ThemeType themeType;
        if (themeName == null) {
            themeType = defaultThemeType;
        } else {
            themeType = ThemeType.valueOf(themeName.toUpperCase());
        }
        currentTheme(themeType);
    }

    /**
     * 设置主题
     *
     * @param themeType 主题
     */
    public static void currentTheme(ThemeType themeType) {
        try {
            current_ThemeType = themeType;
            List<StageWrapper> wrappers = StageUtil.allStages();
            for (StageWrapper wrapper : wrappers) {
                changeThemeCycle(wrapper.root(), themeType);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 更改主题，循环处理
     *
     * @param root  根节点
     * @param themeType 主题
     */
    private static void changeThemeCycle(Node root, ThemeType themeType) {
        if (root instanceof ThemeAdapter adapter) {
            adapter.changeTheme(themeType);
        }
        if (root instanceof Parent parent) {
            for (Node node : parent.getChildrenUnmodifiable()) {
                changeThemeCycle(node, themeType);
            }
        }
    }
}
