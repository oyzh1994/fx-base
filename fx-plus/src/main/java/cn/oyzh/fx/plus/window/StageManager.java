package cn.oyzh.fx.plus.window;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.OSUtil;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;

/**
 * 舞台工具类
 *
 * @author oyzh
 * @since 2023/10/12
 */

public class StageManager {

    /**
     * 引用属性
     */
    public static final String REF_ATTR = "stage:ref";

    /**
     * 主舞台
     */
    private static Stage Primary_Stage;

    /**
     * 退出系统
     */
    public static void exit() {
        for (StageAdapter adapter : allStages()) {
            if (adapter.controller() instanceof StageListener listener) {
                try {
                    JulLog.info("exit listener:{}", listener.getClass());
                    listener.onSystemExit();
                    JulLog.info("exit listener:{} success.", listener.getClass());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        JulLog.info("system exit...");
        Platform.exit();
        System.exit(0);
    }

    /**
     * 获取所有stage
     */
    public static List<StageAdapter> allStages() {
        List<StageAdapter> list = new ArrayList<>();
        for (Window window : Window.getWindows()) {
            if (window.hasProperties() && window.getProperties().containsKey(REF_ATTR)) {
                list.add((StageAdapter) window.getProperties().get(REF_ATTR));
            }
        }
        return list;
    }

    /**
     * 获取舞台
     *
     * @param controllerClass controller类
     * @return StageAdapter
     */
    public static StageAdapter getStage(Class<?> controllerClass) {
        for (Window window : Window.getWindows()) {
            if (window.hasProperties() && window.getProperties().containsKey(REF_ATTR)) {
                StageAdapter adapter = (StageAdapter) window.getProperties().get(REF_ATTR);
                if (adapter.controllerClass() == controllerClass) {
                    return adapter;
                }
            }
        }
        return null;
    }

    /**
     * 获取舞台
     *
     * @param controllerClass controller类
     * @return List<StageAdapter>
     */
    public static List<StageAdapter> listStage(Class<?> controllerClass) {
        List<StageAdapter> list = new ArrayList<>();
        for (Window window : Window.getWindows()) {
            if (window.hasProperties() && window.getProperties().containsKey(REF_ATTR)) {
                StageAdapter adapter = (StageAdapter) window.getProperties().get(REF_ATTR);
                if (adapter.controllerClass() == controllerClass) {
                    list.add(adapter);
                }
            }
        }
        return list;
    }

    /**
     * 显示舞台
     *
     * @param clazz 舞台类
     */
    public static void showStage(Class<?> clazz) {
        showStage(clazz, (Window) null);
    }

    /**
     * 显示舞台
     *
     * @param clazz   舞台类
     * @param wrapper 舞台包装
     */
    public static void showStage(Class<?> clazz, StageAdapter wrapper) {
        showStage(clazz, wrapper == null ? null : wrapper.stage());
    }

    /**
     * 显示舞台
     *
     * @param clazz 舞台类
     * @param owner 父窗口
     */
    public static void showStage(Class<?> clazz, Window owner) {
        StageAdapter wrapper = parseStage(clazz, owner);
        wrapper.display();
    }

    /**
     * 创建舞台
     *
     * @return StageExt
     */
    public static StageExt newStage() {
        return new StageExt(null);
    }

    /**
     * 创建舞台
     *
     * @param owner 父窗口
     * @return StageExt
     */
    public static StageExt newStage(Window owner) {
        return new StageExt(owner);
    }

    /**
     * 解析舞台
     *
     * @param clazz 舞台类
     * @return StageAdapter
     */
    public static StageAdapter parseStage(Class<?> clazz) {
        if (Primary_Stage != null && Primary_Stage.isShowing()) {
            return parseStage(clazz, Primary_Stage);
        }
        return parseStage(clazz, null);
    }

    /**
     * 解析舞台
     *
     * @param clazz 舞台类
     * @param owner 父窗口
     * @return StageAdapter
     */
    public static StageAdapter parseStage(Class<?> clazz, Window owner) {
        StageAttribute attribute = clazz.getAnnotation(StageAttribute.class);
        if (attribute == null) {
            throw new RuntimeException("can not find annotation[" + StageAttribute.class.getSimpleName() + "] from class: " + clazz.getName());
        }
        // 获取舞台
        StageAdapter stage = getStage(clazz);
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

    public static void showMask(Runnable callback) {
        Window window = getFrontWindow();
        if (window != null) {
            StageMask.showMask(window, callback);
        } else {
            callback.run();
        }
    }

    public static Window getFrontWindow() {
        for (Window window : Window.getWindows()) {
            if (window.isShowing() && window.isFocused() && (!(window instanceof StageMask))) {
                return window;
            }
        }
        return null;
    }

    public static boolean hasFocusedWindow() {
        for (Window window : Window.getWindows()) {
            if (window.isShowing() && window.isFocused() && (!(window instanceof StageMask))) {
                return true;
            }
        }
        return false;
    }
}
