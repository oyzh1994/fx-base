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
 * fx页面工具类
 *
 * @author oyzh
 * @since 2021/8/19
 */
@Slf4j
@UtilityClass
public class StageUtil {

    /**
     * 主要的stage
     */
    private static PrimaryStage Primary_Stage;

    /**
     * 退出系统
     */
    public static void exit() {
        for (Window window : Window.getWindows()) {
            if (window instanceof StageExt stageExt && stageExt.controller() instanceof StageListener listener) {
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
     * @return StageExt
     */
    public static StageExt getStage(@NonNull Class<?> controllerClass) {
        for (Window window : Window.getWindows()) {
            if (window instanceof StageExt stageExt && stageExt.controllerClass() == controllerClass) {
                return stageExt;
            }
        }
        return null;
    }

    /**
     * 显示舞台
     *
     * @param clazz 页面类
     */
    public static void showStage(@NonNull Class<?> clazz) {
        showStage(clazz, null);
    }

    /**
     * 显示页面
     *
     * @param clazz 页面类
     * @param owner 父窗口
     */
    public static void showStage(@NonNull Class<?> clazz, Window owner) {
        StageExt stageExt = parseStage(clazz, owner);
        stageExt.showStage();
    }

    /**
     * 解析舞台
     *
     * @param clazz 页面类
     * @return StageExt
     */
    public static StageExt parseStage(@NonNull Class<?> clazz) {
        return parseStage(clazz, null);
    }

    /**
     * 解析页面
     *
     * @param clazz 页面类
     * @param owner 父窗口
     * @return FXView
     */
    public static StageExt parseStage(@NonNull Class<?> clazz, Window owner) {
        StageAttribute attribute = clazz.getAnnotation(StageAttribute.class);
        if (attribute == null) {
            throw new RuntimeException("can not find annotation " + StageAttribute.class.getSimpleName() + " from view class: " + clazz.getName());
        }
        // 获取页面
        StageExt stage = getStage(clazz);
        // 创建页面
        if (stage == null) {
            stage = new StageExt(attribute, owner);
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

    public static PrimaryStage getPrimaryStage() {
        return Primary_Stage;
    }

    /**
     * 设置主窗口
     *
     * @param primaryStage 主窗口
     */
    public static void setPrimaryStage(@NonNull Stage primaryStage) {
        Primary_Stage = new PrimaryStage(primaryStage);
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
     * 获取任务栏窗口
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
