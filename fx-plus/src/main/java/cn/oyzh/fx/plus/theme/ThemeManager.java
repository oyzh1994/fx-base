package cn.oyzh.fx.plus.theme;

import cn.oyzh.common.SysConst;
import cn.oyzh.common.file.FileUtil;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
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
    public final static ThemeStyle defaultTheme = Themes.PRIMER_LIGHT;

    /**
     * 当前主题属性
     */
    private static final ReadOnlyObjectWrapper<ThemeStyle> currentThemeProperty = new ReadOnlyObjectWrapper<>(defaultTheme);
//
//    /**
//     * 当前主题
//     */
//    private static ThemeStyle currentTheme;

    /**
     * 获取当前主题属性
     *
     * @return 当前主题属性
     */
    public static ReadOnlyObjectProperty<ThemeStyle> currentThemeProperty() {
        return currentThemeProperty.getReadOnlyProperty();
    }


    /**
     * 是否暗黑模式
     *
     * @return 结果
     */
    public static boolean isDarkMode() {
        return currentTheme().isDarkMode();
    }

    /**
     * 获取当前主题
     *
     * @return 当前主题
     */
    public static ThemeStyle currentTheme() {
        if (currentThemeProperty.get() == null) {
            return defaultTheme;
        }
        return currentThemeProperty.get();
    }

    /**
     * 应用主题
     *
     * @param config 主题配置
     */
    public static void apply(ThemeConfig config) {
        if (config == null) {
            apply(defaultTheme);
        } else if (config.isCustom()) {
            CustomTheme theme = Themes.CUSTOM;
            theme.updateTheme(config.getName(), config.getBgColor(), config.getFgColor(), config.getAccentColor());
            apply(theme);
        } else {
            apply(Themes.getTheme(config.getName()));
        }
    }

    /**
     * 设置主题
     *
     * @param style 主题风格
     */
    public static void apply(ThemeStyle style) {
        if (style == null) {
            return;
        }
        try {
            // 监听系统主题
            if (style == Themes.SYSTEM) {
                Themes.SYSTEM.listener();
            } else {
                Themes.SYSTEM.unListener();
            }
            // 设置当前主题
//            currentTheme = style;
            currentThemeProperty.set(style);
            // 设置应用样式
            Application.setUserAgentStylesheet(ThemeManager.currentUserAgentStylesheet());
            // 变更样式
            List<StageAdapter> wrappers = StageManager.allStages();
            for (StageAdapter wrapper : wrappers) {
                applyCycle(wrapper.root(), style);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 应用主题，循环处理
     *
     * @param root  根节点
     * @param style 主题风格
     */
    private static void applyCycle(EventTarget root, ThemeStyle style) {
        if (root instanceof ThemeAdapter adapter) {
            adapter.changeTheme(style);
        }
        if (root instanceof Parent parent) {
            for (Node node : new CopyOnWriteArrayList<>(parent.getChildrenUnmodifiable())) {
                applyCycle(node, style);
            }
        } else if (root instanceof Popup popup) {
            for (Node node : new CopyOnWriteArrayList<>(popup.getContent())) {
                applyCycle(node, style);
            }
        } else if (root instanceof Stage stage) {
            applyCycle(stage.getScene(), style);
        } else if (root instanceof Window window) {
            applyCycle(window.getScene(), style);
        } else if (root instanceof Scene scene) {
            applyCycle(scene.getRoot(), style);
        }
    }

    /**
     * 清理临时主题文件
     */
    public static void clearThemeTmp() {
        File[] files = FileUtil.ls(SysConst.storeDir());
        if (files != null) {
            for (File file : files) {
                if (file.getName().startsWith("theme") && file.getName().endsWith(".css")) {
                    file.delete();
                }
            }
        }
    }

    public static Color currentAccentColor() {
        return currentTheme().getAccentColor();
    }

    public static Color currentForegroundColor() {
        return currentTheme().getForegroundColor();
    }

    public static Color currentBackgroundColor() {
        return currentTheme().getBackgroundColor();
    }

    public static String currentUserAgentStylesheet() {
        return currentTheme().getUserAgentStylesheet();
    }

    public static String currentCompressedUserAgentStylesheet() {
        return currentTheme().getCompressedUserAgentStylesheet();
    }
}
