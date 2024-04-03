package cn.oyzh.fx.plus.theme;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.oyzh.fx.plus.stage.StageUtil;
import cn.oyzh.fx.plus.stage.StageWrapper;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 主题管理器
 *
 * @author oyzh
 * @since 2023/12/18
 */
@UtilityClass
public class ThemeManager {

    static {
        clearThemeTmp();
    }

    /**
     * 默认主题
     */
    public static ThemeStyle defaultTheme = Themes.PRIMER_LIGHT;

    /**
     * 当前主题
     */
    private static ThemeStyle currentTheme;

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
    public static ThemeStyle currentTheme() {
        if (currentTheme == null) {
            return defaultTheme;
        }
        return currentTheme;
    }

    /**
     * 设置主题
     *
     * @param themeName 主题名称
     */
    public static void changeTheme(String themeName) {
        changeTheme(Themes.getTheme(themeName));
    }

    /**
     * 变更主题
     *
     * @param style 主题风格
     */
    public static void changeTheme(ThemeStyle style) {
        if (style == null) {
            return;
        }
        try {
            // 变更颜色
            List<StageWrapper> wrappers = StageUtil.allStages();
            for (StageWrapper wrapper : wrappers) {
                changeThemeCycle(wrapper.root(), style);
            }
            // 监听系统主题
            if (style == Themes.SYSTEM) {
                Themes.SYSTEM.listener();
            } else {
                Themes.SYSTEM.unListener();
            }
            // 设置当前主题
            currentTheme = style;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 更改主题，循环处理
     *
     * @param root  根节点
     * @param style 主题风格
     */
    private static void changeThemeCycle(EventTarget root, ThemeStyle style) {
        if (root instanceof ThemeAdapter adapter) {
            adapter.changeTheme(style);
        }
        if (root instanceof Parent parent) {
            for (Node node : new CopyOnWriteArrayList<>(parent.getChildrenUnmodifiable())) {
                changeThemeCycle(node, style);
            }
        } else if (root instanceof Popup popup) {
            for (Node node : new CopyOnWriteArrayList<>(popup.getContent())) {
                changeThemeCycle(node, style);
            }
        } else if (root instanceof Stage stage) {
            changeThemeCycle(stage.getScene(), style);
        } else if (root instanceof Window window) {
            changeThemeCycle(window.getScene(), style);
        } else if (root instanceof Scene scene) {
            changeThemeCycle(scene.getRoot(), style);
        }
    }

    /**
     * 清理临时主题文件
     */
    public static void clearThemeTmp() {
        File[] files = FileUtil.ls(FXUtil.getAppStorePath());
        if (ArrayUtil.isNotEmpty(files)) {
            for (File file : files) {
                if (file.getName().startsWith("theme") && file.getName().endsWith(".css")) {
                    file.delete();
                }
            }
        }
    }
}
