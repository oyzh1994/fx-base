package cn.oyzh.fx.plus.stage;

import cn.hutool.core.util.ReflectUtil;
import cn.oyzh.fx.common.util.OSUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.NoSuchElementException;

/**
 * fx舞台工具类
 *
 * @author oyzh
 * @since 2023/10/12
 */
@Slf4j
@UtilityClass
public class StageUtil {

    /**
     * 主舞台
     */
    private static Stage Primary_Stage;

    /**
     * 退出系统
     */
    public static void exit() {
        for (Window window : Window.getWindows()) {
            if (window instanceof StageWrapper wrapper && wrapper.controller() instanceof StageListener listener) {
                try {
                    listener.onSystemExit();
                    if (log.isDebugEnabled()) {
                        log.debug("listener.onSystemExit() execute...");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        log.warn("system exit...");
        Platform.exit();
    }

    /**
     * 获取舞台
     *
     * @param controllerClass controller类
     * @return StageWrapper
     */
    public static StageWrapper getStage(@NonNull Class<?> controllerClass) {
        for (Window window : Window.getWindows()) {
            if (window instanceof StageWrapper wrapper && wrapper.controllerClass() == controllerClass) {
                return wrapper;
            }
        }
        return null;
    }

    /**
     * 显示舞台
     *
     * @param clazz 舞台类
     */
    public static void showStage(@NonNull Class<?> clazz) {
        showStage(clazz, (Window) null);
    }

    /**
     * 显示舞台
     *
     * @param clazz   舞台类
     * @param wrapper 舞台包装
     */
    public static void showStage(@NonNull Class<?> clazz, StageWrapper wrapper) {
        showStage(clazz, wrapper == null ? null : wrapper.stage());
    }

    /**
     * 显示舞台
     *
     * @param clazz 舞台类
     * @param owner 父窗口
     */
    public static void showStage(@NonNull Class<?> clazz, Window owner) {
        StageWrapper wrapper = parseStage(clazz, owner);
        wrapper.display();
    }

    /**
     * 解析舞台
     *
     * @param clazz 舞台类
     * @return StageWrapper
     */
    public static StageWrapper parseStage(@NonNull Class<?> clazz) {
        return parseStage(clazz, null);
    }

    /**
     * 解析舞台
     *
     * @param clazz 舞台类
     * @param owner 父窗口
     * @return StageWrapper
     */
    public static StageWrapper parseStage(@NonNull Class<?> clazz, Window owner) {
        StageAttribute attribute = clazz.getAnnotation(StageAttribute.class);
        if (attribute == null) {
            throw new RuntimeException("can not find annotation[" + StageAttribute.class.getSimpleName() + "] from class: " + clazz.getName());
        }
        // 获取舞台
        StageWrapper stage = getStage(clazz);
        // 创建舞台
        if (stage == null) {
            // 主舞台
            if (attribute.usePrimary()) {
                stage = new PrimaryStage(Primary_Stage, attribute, owner);
            } else {// 一般舞台
                stage = new StageExt(attribute, owner);
            }
        }
        return stage;
    }

    /**
     * 关闭所有窗口
     */
    public static void closeWindows() {
        try {
            ObservableList<Window> windows = Window.getWindows();
            for (Window window : windows) {
                FXUtil.runLater(window::hide);
            }
        } catch (NoSuchElementException ignore) {
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 获取主舞台
     *
     * @return Stage 主舞台
     */
    public static Stage getPrimaryStage() {
        return Primary_Stage;
    }

    /**
     * 设置主舞台
     *
     * @param primaryStage 主舞台
     */
    public static void setPrimaryStage(Stage primaryStage) {
        Primary_Stage = primaryStage;
    }

    /**
     * 是否显示过
     *
     * @param window 窗口
     */
    public static boolean hasBeenVisible(@NonNull Window window) {
        return (Boolean) ReflectUtil.getFieldValue(window, "hasBeenVisible");
    }

    /**
     * 窗口隐藏在任务栏
     *
     * @param stage 窗口
     */
    public static void hideTaskbar(Stage stage) {
        if (OSUtil.isLinux()) {
            return;
        }
        if (stage != null) {
            // 任务栏stage
            final Stage transparentStage = getTaskbarStage();
            // 设置拥有者
            stage.initOwner(transparentStage);
            // stage展示出来，此步骤不可少，缺少此步骤stage还是会存在任务栏
            transparentStage.show();
        }
    }

    /**
     * 获取任务栏舞台
     *
     * @return Stage
     */
    public static Stage getTaskbarStage() {
        // 新建一个stage
        final Stage transparentStage = new Stage();
        // stage采用UTILITY风格
        transparentStage.initStyle(StageStyle.UTILITY);
        transparentStage.setWidth(0);
        transparentStage.setMinWidth(0);
        transparentStage.setMaxWidth(0);
        transparentStage.setHeight(0);
        transparentStage.setMinHeight(0);
        transparentStage.setMaxHeight(0);
        transparentStage.setResizable(false);
        transparentStage.toBack();
        // 将stage的透明度设置为0
        transparentStage.setOpacity(0);
        transparentStage.setIconified(true);
        transparentStage.setX(-1000);
        transparentStage.setY(-1000);
        return transparentStage;
    }
}
