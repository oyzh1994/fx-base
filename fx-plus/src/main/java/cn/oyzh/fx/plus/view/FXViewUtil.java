package cn.oyzh.fx.plus.view;

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
public class FXViewUtil {

    /**
     * 主要的stage
     */
    private static cn.oyzh.fx.plus.view.FXPrimaryStage Primary_Stage;

    //private static FXStage Primary_Stage;

    ///**
    // * 页面列表
    // */
    //private static final List<WeakReference<FXView>> VIEWS = new CopyOnWriteArrayList<>();

    /**
     * 退出系统
     */
    public static void exit() {
        for (Window window : Window.getWindows()) {
            Object object = window.getProperties().get("_reference");
            if (object instanceof cn.oyzh.fx.plus.view.FXView fxView && fxView.controller() instanceof FXViewListener listener) {
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
     * 获取页面
     *
     * @param controllerClass controller类
     * @return FXView
     */
    public static cn.oyzh.fx.plus.view.FXView getView(@NonNull Class<?> controllerClass) {
        for (Window window : Window.getWindows()) {
            Object _reference = window.getProperties().get("_reference");
            if (_reference instanceof cn.oyzh.fx.plus.view.FXView fxView && fxView.controllerClass() == controllerClass) {
                return fxView;
            }
        }
        //for (WeakReference<FXView> value : VIEWS) {
        //    if (value.get() != null && value.get().controllerClass() == controllerClass) {
        //        return value.get();
        //    }
        //}
        return null;
    }

    ///**
    // * 获取页面
    // *
    // * @param controller controller对象
    // * @return FXView
    // */
    //public static FXView getView(@NonNull Object controller) {
    //    for (WeakReference<FXView> value : VIEWS) {
    //        if (value.get() != null && value.get().controller() == controller) {
    //            return value.get();
    //        }
    //    }
    //    return null;
    //}

    ///**
    // * 移除页面
    // *
    // * @param view 页面
    // */
    //public static void remove(@NonNull FXView view) {
    //    VIEWS.removeIf(v -> v.get() == null || v.get() == view || !v.get().isShowing());
    //}

    /**
     * 显示页面
     *
     * @param clazz 页面类
     */
    public static void showView(@NonNull Class<?> clazz) {
        showView(clazz, (Window) null);
    }

    /**
     * 显示页面
     *
     * @param clazz 页面类
     * @param owner 父窗口
     */
    public static void showView(@NonNull Class<?> clazz, Window owner) {
        cn.oyzh.fx.plus.view.FXView fxView = parseView(clazz, owner);
        fxView.show();
    }

    /**
     * 显示页面
     *
     * @param clazz  页面类
     * @param fxView 父页面
     */
    public static void showView(@NonNull Class<?> clazz, cn.oyzh.fx.plus.view.FXView fxView) {
        Window owner = fxView == null ? null : fxView.getStage();
        parseView(clazz, owner).show();
    }

    /**
     * 解析页面
     *
     * @param clazz 页面类
     * @return FXView
     */
    public static cn.oyzh.fx.plus.view.FXView parseView(@NonNull Class<?> clazz) {
        return parseView(clazz, (Stage) null);
    }

    /**
     * 解析页面
     *
     * @param clazz  页面类
     * @param fxView 父页面
     * @return FXView
     */
    public static cn.oyzh.fx.plus.view.FXView parseView(@NonNull Class<?> clazz, cn.oyzh.fx.plus.view.FXView fxView) {
        Window owner = fxView == null ? null : fxView.getStage();
        return parseView(clazz, owner);
    }

    /**
     * 解析页面
     *
     * @param clazz 页面类
     * @param owner 父窗口
     * @return FXView
     */
    public static cn.oyzh.fx.plus.view.FXView parseView(@NonNull Class<?> clazz, Window owner) {
        cn.oyzh.fx.plus.view.FXWindow window = clazz.getAnnotation(cn.oyzh.fx.plus.view.FXWindow.class);
        if (window == null) {
            throw new RuntimeException("can not find annotation " + FXWindow.class.getSimpleName() + " from view class: " + clazz.getName());
        }
        // 获取页面
        cn.oyzh.fx.plus.view.FXView view = getView(clazz);
        // 创建页面
        if (view == null) {
            view = new FXView(window, owner);
        }
        return view;
    }

    ///**
    // * 窗口显示到前端
    // *
    // * @param fxView 页面
    // */
    //public static void frontView(FXView fxView) {
    //    if (fxView != null) {
    //        frontView(fxView.getStage());
    //    } else {
    //        log.warn("fxView is null!");
    //    }
    //}
    //
    ///**
    // * 窗口显示到前端
    // *
    // * @param stage 窗口
    // */
    //public static void frontView(Stage stage) {
    //    if (stage != null) {
    //        FXUtil.runLater(() -> {
    //            stage.toFront();
    //            stage.setIconified(false);
    //        });
    //    } else {
    //        log.warn("stage is null!");
    //    }
    //}

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
     * 获取主窗口
     *
     * @return 主窗口
     */
//    public static Stage getPrimaryStage() {
//        for (Window window : Window.getWindows()) {
//            if (window instanceof Stage stage) {
//                if ((Boolean) ReflectUtil.getFieldValue(stage, "primary")) {
//                    return stage;
//                }
//            }
//        }
//        return null;
//    }
    public static cn.oyzh.fx.plus.view.FXPrimaryStage getPrimaryStage() {
        return Primary_Stage;
    }

    /**
     * 设置主窗口
     *
     * @param primaryStage 主窗口
     */
    public static void setPrimaryStage(@NonNull Stage primaryStage) {
        Primary_Stage = new FXPrimaryStage(primaryStage);
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

    ///**
    // * 初始化stage
    // *
    // * @param window 窗口注解
    // * @return stage
    // */
    //public static Stage initStage(@NonNull FXWindow window) {
    //    AtomicReference<Stage> stage = new AtomicReference<>();
    //    // 使用原始stage
    //    if (window.usePrimary()) {
    //        //FXPrimaryStage FXPrimaryStage = FXViewUtil.getFXPrimaryStage();
    //        if (Primary_Stage == null) {
    //            log.error("PrimaryStage is null, please set PrimaryStage.");
    //            return null;
    //        }
    //        if (Primary_Stage.isShowing()) {
    //            log.error("PrimaryStage is showing.");
    //            return null;
    //        }
    //        stage.set(Primary_Stage.getStage());
    //        // 设置窗口样式
    //        if (!Primary_Stage.isUsed()) {
    //            stage.get().initStyle(window.stageStyle());
    //        }
    //    } else {// 创建stage
    //        FXUtil.runWait(() -> stage.set(new Stage()));
    //        // 初始化模态
    //        stage.get().initModality(window.modality());
    //        // 设置窗口样式
    //        stage.get().initStyle(window.stageStyle());
    //    }
    //    // 初始化stage
    //    stage.get().setTitle(window.title());
    //    stage.get().setMaximized(window.maximized());
    //    stage.get().setResizable(window.resizeable());
    //
    //    // 设置icon
    //    if (ArrayUtil.isNotEmpty(window.iconUrls())) {
    //        // linux不支持
    //        if (OSUtil.isLinux()) {
    //            log.warn("linux is not support icon!");
    //        } else {
    //            stage.get().getIcons().addAll(FXIconUtil.getIcons(window.iconUrls()));
    //        }
    //    }
    //    return stage.get();
    //}
}
