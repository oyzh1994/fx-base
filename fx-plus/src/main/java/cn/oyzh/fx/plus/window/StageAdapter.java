package cn.oyzh.fx.plus.window;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ExecutorUtil;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.ReflectUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.drag.DragFileHandler;
import cn.oyzh.fx.plus.drag.DragUtil;
import cn.oyzh.fx.plus.ext.FXMLLoaderExt;
import cn.oyzh.fx.plus.handler.EscHideHandler;
import cn.oyzh.fx.plus.handler.TabSwitchHandler;
import cn.oyzh.fx.plus.node.NodeLifeCycleUtil;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.util.CursorUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.util.IconUtil;
import cn.oyzh.fx.plus.util.StyleUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 舞台适配器
 *
 * @author oyzh
 * @since 2023/10/11
 */
public interface StageAdapter extends WindowAdapter {

    @Override
    default void onWindowClosed() {
        try {
            Stage stage = this.stage();
            WindowAdapter.super.onWindowClosed();
            DragUtil.clearDragFile(this.scene());
            NodeLifeCycleUtil.onStageDestroy(stage);
            this.clearTitle();
            this.clearScene();
            this.clearListener();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 获取舞台
     *
     * @return 舞台
     */
    Stage stage();

    /**
     * 是否已经显示过了
     *
     * @return 结果
     */
    default boolean hasBeenVisible() {
        Stage stage = this.stage();
        if (stage != null) {
            Field field = ReflectUtil.getField(Window.class, "hasBeenVisible");
            return ReflectUtil.getFieldValue(field, stage);
        }
        return false;
    }

    /**
     * 获取场景
     *
     * @return 场景
     */
    default Scene scene() {
        if (this.stage() != null) {
            return this.stage().getScene();
        }
        return null;
    }

    /**
     * 清除场景
     */
    default void clearScene() {
        this.stage().setScene(null);
    }

    /**
     * 设置场景
     *
     * @param scene 场景
     */
    default void scene(Scene scene) {
        if (scene != null) {
            this.stage().setScene(scene);
        }
    }

    /**
     * 获取根节点
     *
     * @return 根节点
     */
    default Parent root() {
        if (this.scene() != null) {
            return this.scene().getRoot();
        }
        return null;
    }

    /**
     * 设置根节点
     *
     * @param root 根节点
     */
    default void root(Parent root) {
        if (this.scene() != null) {
            this.scene().setRoot(root);
        } else {
            this.stage().setScene(new Scene(root));
        }
    }

    /**
     * 获取x值
     *
     * @return x值
     */
    default double getX() {
        return this.stage().getX();
    }

    /**
     * 设置x值
     *
     * @param x x值
     */
    default void setX(double x) {
        this.stage().setX(x);
    }

    /**
     * 获取y值
     *
     * @return y值
     */
    default double getY() {
        return this.stage().getY();
    }

    /**
     * 设置y值
     *
     * @param y y值
     */
    default void setY(double y) {
        this.stage().setY(y);
    }

    /**
     * 设置位置
     *
     * @param x x值
     * @param y y值
     */
    default void setLocation(double x, double y) {
        this.setX(x);
        this.setY(y);
    }

    /**
     * 获取宽
     *
     * @return 宽
     */
    default double getWidth() {
        return this.stage().getWidth();
    }

    /**
     * 设置宽
     *
     * @param width 宽
     */
    default void setWidth(double width) {
        this.stage().setWidth(width);
    }

    /**
     * 设置大小
     *
     * @param width  宽
     * @param height 高
     */
    default void setSize(double width, double height) {
        this.setWidth(width);
        this.setHeight(height);
    }

    /**
     * 获取高
     *
     * @return 高
     */
    default double getHeight() {
        return this.stage().getHeight();
    }

    /**
     * 设置高
     *
     * @param height 高
     */
    default void setHeight(double height) {
        this.stage().setHeight(height);
    }

    /**
     * 是否全屏
     *
     * @return 结果
     */
    default boolean isFullScreen() {
        return this.stage().isFullScreen();
    }

    /**
     * 是否最大化
     *
     * @return 结果
     */
    default boolean isMaximized() {
        return this.stage().isMaximized();
    }

    /**
     * 设置最大化
     *
     * @param maximized 最大化
     */
    default void setMaximized(boolean maximized) {
        this.stage().setMaximized(maximized);
    }

//    /**
//     * 获取自定义标题栏
//     *
//     * @return 标题栏
//     */
//    default TitleBar getTitleBar() {
//        if (BooleanUtil.isTrue(this.getProp("_custom"))) {
//            Parent parent = this.root();
//            if (parent != null) {
//                return (TitleBar) parent.lookup("#titleBar");
//            }
//        }
//        return null;
//    }

    /**
     * 初始化舞台
     *
     * @param attribute 舞台属性
     * @param owner     父窗口
     */
    default void init(StageAttribute attribute, Window owner) {
//        if (attribute.stageStyle().isCustom()) {
//            this.initByCustom(attribute, owner);
//        } else {
        this.initByPlatform(attribute, owner);
//        }
    }

//    /**
//     * 初始化舞台，自定义
//     *
//     * @param attribute 舞台属性
//     * @param owner     父窗口
//     */
//    private void initByCustom(StageAttribute attribute, Window owner) {
//        // 初始化加载器
//        FXMLLoaderExt loader = new FXMLLoaderExt();
//        // 加载根节点
//        Parent root = loader.load(attribute.value());
//        if (root == null) {
//            throw new RuntimeException("load root fail");
//        }
//        // 设置controller
//        this.setProp("_controller", loader.getController());
//        // 定制的
//        this.setProp("_custom", true);
//        // 舞台
//        Stage stage = this.stage();
//        // 创建配置
//        TitleBar.TitleBarConfig config = new TitleBar.TitleBarConfig();
//        // 不可拉伸
//        if (!attribute.resizable()) {
//            config.setMaximum(false).setMinimum(false);
//            stage.setResizable(false);
//        } else if (!attribute.maximumAble()) {// 不可最大化
//            config.setMaximum(false);
//        }
//        // 可全屏
//        if (attribute.fullScreenAble()) {
//            config.setFullScreen(true);
//        }
//        // 可置顶
//        if (attribute.alwaysOnTopAble()) {
//            config.setAlwaysOnTop(true);
//        }
//        // 设置窗口样式
//        if (!this.hasBeenVisible()) {
//            stage.initStyle(StageStyle.UNDECORATED);
//        }
//        // 自定义icon
//        if (StringUtil.isNotEmpty(attribute.iconUrl())) {
//            if (OSUtil.isWindows()) {
//                config.setIcon(attribute.iconUrl());
//            }
//            stage.getIcons().setAll(IconUtil.getIcon(attribute.iconUrl()));
//        } else if (StringUtil.isNotEmpty(FXConst.appIcon())) {// 全局icon
//            if (OSUtil.isWindows()) {
//                config.setIcon(FXConst.appIcon());
//            }
//            stage.getIcons().setAll(IconUtil.getIcon(FXConst.appIcon()));
//        }
//        // 窗口模态
//        Modality modality = attribute.modality();
//        // 非主窗口
//        if (!attribute.usePrimary() && !this.hasBeenVisible()) {
//            // 初始化父窗口
//            if (owner != null) {
//                stage.initOwner(owner);
//            }
//            // 初始化模态
//            stage.initModality(modality);
//        }
//        // 最小化处理
//        if (modality != Modality.NONE || owner != null) {
//            // 不启用最小化
//            config.setMinimum(false);
//        }
//        // 标题栏
//        TitleBar titleBar = new TitleBar(config);
//        // 标题组件
//        TitleBox titleBox = new TitleBox(titleBar, root);
//        // 绑定大小，因为有边框，需要总高度/宽度+4
//        titleBox.prefWidthProperty().bind(stage.widthProperty().add(4));
//        titleBox.prefHeightProperty().bind(stage.heightProperty().add(4));
//        // 显示监听
//        stage.showingProperty().addListener((observable, oldValue, newValue) -> {
//            try {
//                if (!newValue) {
//                    this.onWindowClosed();
//                } else {
//                    // 最大化处理
//                    titleBar.doMaximum(stage.isMaximized());
//                    // 初始化标题
//                    titleBar.initTitle();
//                }
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                JulLog.warn("showing error", ex);
//            }
//        });
//        // 非主窗口或者未显示过
//        if (!attribute.usePrimary() || !this.hasBeenVisible()) {
//            // 标题
//            stage.titleProperty().addListener((observable, oldValue, newValue) -> titleBar.initTitle());
//            if (!OSUtil.isMacOS()) {
//                // 最大化
//                stage.maximizedProperty().addListener((observable, oldValue, newValue) -> {
//                    if (BooleanUtil.isFalse(newValue)) {
//                        TaskManager.startDelay(titleBox::updateContent, 10);
//                    }
//                });
//                // 全屏
//                stage.fullScreenProperty().addListener((observable, oldValue, newValue) -> {
//                    if (BooleanUtil.isFalse(newValue)) {
//                        TaskManager.startDelay(titleBox::updateContent, 10);
//                    }
//                });
//                // 焦点
//                stage.focusedProperty().addListener((observable, oldValue, newValue) -> {
//                    if (BooleanUtil.isTrue(newValue) && stage.isMaximized()) {
//                        TaskManager.startDelay(titleBox::updateContent, 10);
//                    }
//                });
//            } else {
//                // 最大化
//                stage.maximizedProperty().addListener((observable, oldValue, newValue) -> {
//                    titleBar.doMaximum(newValue);
//                });
//                // 全屏
//                stage.fullScreenProperty().addListener((observable, oldValue, newValue) -> {
//                    titleBar.doFullScreen(newValue);
//                });
//            }
//            // 初始化
//            NodeManager.init(this);
//        }
//        // 加载自定义css文件
//        if (ArrayUtil.isNotEmpty(attribute.cssUrls())) {
//            root.getStylesheets().addAll(StyleUtil.split(attribute.cssUrls()));
//        }
//        // 设置事件
//        if (this.controller() instanceof StageListener listener) {
//            this.initListener(listener);
//        }
//        // 设置scene
//        FXUtil.runWait(() -> stage.setScene(new Scene(titleBox)));
//    }

    /**
     * 初始化舞台，平台
     *
     * @param attribute 舞台属性
     * @param owner     父窗口
     */
    private void initByPlatform(StageAttribute attribute, Window owner) {
        // 初始化加载器
        FXMLLoaderExt loader = new FXMLLoaderExt();
        // 加载根节点
        Parent root = loader.load(attribute.value());
        if (root == null) {
            throw new RuntimeException("load root fail");
        }
        Stage stage = this.stage();
        // 设置controller
        this.setProp("_controller", loader.getController());
        // 设置窗口样式
        if (!this.hasBeenVisible()) {
            stage.initStyle(attribute.stageStyle().toStageStyle());
        }
        // 初始化stage
//        stage.setTitle(attribute.title());
        stage.setResizable(attribute.resizable());
        // 设置icon
        if (StringUtil.isNotEmpty(attribute.iconUrl())) {
            stage.getIcons().setAll(IconUtil.getIcon(attribute.iconUrl()));
        }
        // 自定义icon
        if (StringUtil.isNotEmpty(attribute.iconUrl())) {
            stage.getIcons().setAll(IconUtil.getIcon(attribute.iconUrl()));
        } else if (StringUtil.isNotEmpty(FXConst.appIcon())) {// 全局icon
            stage.getIcons().setAll(IconUtil.getIcon(FXConst.appIcon()));
        }
        // 显示监听
        stage.showingProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue) {
                    this.onWindowClosed();
//                } else {
//                    this.updateContentInner();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JulLog.warn("showing error", ex);
            }
        });
        // 非主窗口
        if (!attribute.usePrimary() && !this.hasBeenVisible()) {
            // 初始化父窗口
            if (owner != null) {
                stage.initOwner(owner);
            }
            // 初始化模态
            stage.initModality(attribute.modality());
        }
        // 非主窗口或者未显示过
        if (!attribute.usePrimary() || !this.hasBeenVisible()) {
            // 最大化
            stage.maximizedProperty().addListener((observableValue, aBoolean, t1) -> {
                if (t1) {
                    this.updateContent();
                }
            });
            // 全屏
            stage.fullScreenProperty().addListener((observableValue, aBoolean, t1) -> {
                if (t1) {
                    this.updateContent();
                }
            });
            // 初始化
            NodeManager.init(this);
        }
        // 加载自定义css文件
        if (ArrayUtil.isNotEmpty(attribute.cssUrls())) {
            root.getStylesheets().addAll(StyleUtil.split(attribute.cssUrls()));
//        } else {// 默认样式文件
//            root.getStylesheets().addAll(ThemeManager.currentUserAgentStylesheet(), FXStyle.FX_BASE);
        }
        // 设置事件
        if (this.controller() instanceof StageListener listener) {
            this.initListener(listener);
        }
        // 初始化场景
        Scene scene = new Scene(root);
        if (attribute.sceneTransparent()) {
            scene.setFill(Color.TRANSPARENT);
        }
        // 设置scene
        FXUtil.runWait(() -> stage.setScene(scene));
//        stage.getScene().focusOwnerProperty().addListener((observable, oldValue, newValue) -> {
//            if (newValue != null) {
//                Light.Distant light = new Light.Distant();
//                light.setAzimuth(-135.0);
//                light.setElevation(30.0);
//                light.setColor(Color.ALICEBLUE);
//                Lighting lighting = new Lighting();
//                lighting.setLight(light);
//                newValue.setEffect(lighting);
//            }
//            if (oldValue != null) {
//                oldValue.setEffect(null);
//            }
//        });
    }

    /**
     * 更新内容，延迟处理
     */
    default void updateContentLater() {
        // 更新内容
        FXUtil.runPulse(this::updateContent);
        // this.updateContent();
    }

    /**
     * 更新内容
     */
    default void updateContent() {
        Parent root = this.root();
        if (root != null) {
            double width = NodeUtil.getWidth(root);
            double height = NodeUtil.getHeight(root);
            if (this.isFullScreen() || this.isMaximized()) {
                // 先减再加，因为全屏和最大化这个宽高已经最大了
                this.resizeRoot(width - 1, height - 1);
                this.resizeRoot(width + 1, height + 1);
                // this.resizeStage(width - 1, height - 1);
                // this.resizeStage(width + 1, height + 1);
            } else {
                // 先加再减，避免边框异常
                this.resizeRoot(width + 1, height + 1);
                this.resizeRoot(width - 1, height - 1);
                // this.resizeStage(width + 1, height + 1);
                // this.resizeStage(width - 1, height - 1);
            }
//            // 递归布局
//            NodeUtil.layoutRecursive(root);
        }
    }

    /**
     * 修改页面大小
     *
     * @param width  宽
     * @param height 高
     */
    default void resizeStage(double width, double height) {
        Stage stage = this.stage();
        if (stage != null) {
            stage.setWidth(width);
            stage.setHeight(height);
        }
    }

    /**
     * 修改根节点大小
     *
     * @param width  宽
     * @param height 高
     */
    default void resizeRoot(double width, double height) {
        Parent parent = this.root();
        if (parent != null) {
            parent.resize(width, height);
        }
    }

    /**
     * 清理监听器
     */
    default void clearListener() {
        Stage stage = this.stage();
        if (stage != null) {
            stage.setOnShown(null);
            stage.setOnHiding(null);
            stage.setOnHidden(null);
            stage.setOnShowing(null);
            stage.setOnCloseRequest(null);
        }
    }

    /**
     * 初始化监听器
     *
     * @param listener 舞台监听器
     */
    default void initListener(StageListener listener) {
        // 设置事件
        listener.onStageInitialize(this);
        Stage stage = this.stage();
        if (stage != null) {
            stage.setOnShown(listener::onWindowShown);
            stage.setOnHiding(listener::onWindowHiding);
            stage.setOnHidden(listener::onWindowHidden);
            stage.setOnShowing(listener::onWindowShowing);
            stage.setOnCloseRequest(listener::onWindowCloseRequest);
        }
    }

    /**
     * 清除标题
     */
    default void clearTitle() {
        FXUtil.runWait(() -> this.stage().setTitle(null));
    }

    /**
     * 设置标题，扩展
     *
     * @param title 标题
     */
    default void title(String title) {
        if (title != null) {
            FXUtil.runWait(() -> this.stage().setTitle(title));
        }
    }

    /**
     * 获取标题，扩展
     */
    default String title() {
        return this.stage() == null ? null : this.stage().getTitle();
    }

    /**
     * 追加标题
     *
     * @param append 追加内容
     */
    default void appendTitle(String append) {
        this.appendTitle(append, -1);
    }

    /**
     * 追加标题
     *
     * @param append   追加内容
     * @param liveTime 追加内容存活时间
     */
    default void appendTitle(String append, int liveTime) {
        if (append != null && !append.isEmpty()) {
            String title = this.getProp("_title");
            if (title == null) {
                title = this.stage().getTitle();
                this.setProp("_title", title);
            }
            String newTitle = title + append;
            this.title(newTitle);
            if (liveTime > 0) {
                ExecutorUtil.start(this::restoreTitle, liveTime);
            }
        }
    }

    /**
     * 恢复标题
     */
    default void restoreTitle() {
        this.title(this.getProp("_title"));
    }

    /**
     * 舞台前置
     */
    default void toFront() {
        FXUtil.runLater(() -> {
            this.stage().toFront();
            this.stage().setIconified(false);
        });
    }

    @Override
    default void handCursor() {
        CursorUtil.handCursor(this.stage());
    }

    @Override
    default void waitCursor() {
        CursorUtil.waitCursor(this.stage());
    }

    @Override
    default void defaultCursor() {
        CursorUtil.defaultCursor(this.stage());
    }

    @Override
    default void hideOnEscape() {
        this.setProp("escHideHandler", new EscHideHandler(this.stage()));
    }

    @Override
    default void unHideOnEscape() {
        EscHideHandler escHideHandler = this.removeProp("escHideHandler");
        if (escHideHandler != null) {
            escHideHandler.destroy();
        }
    }

    @Override
    default boolean isHideOnEscape() {
        return this.hasProp("escHideHandler");
    }

    @Override
    default void switchOnTab() {
        this.setProp("tabSwitchHandler", new TabSwitchHandler(this.stage()));
    }

    @Override
    default void unSwitchOnTab() {
        TabSwitchHandler tabSwitchHandler = this.removeProp("tabSwitchHandler");
        if (tabSwitchHandler != null) {
            tabSwitchHandler.destroy();
        }
    }

    @Override
    default boolean isSwitchOnTab() {
        return this.hasProp("tabSwitchHandler");
    }

    /**
     * 初始化文件拖拽事件
     *
     * @param dragBoardContent 拖拽板内容
     * @param onDragFile       文件拖入处理
     */
    default void initDragFile(String dragBoardContent, Consumer<List<File>> onDragFile) {
        // 文件拖拽初始化
        DragFileHandler dragFileHandler = new DragFileHandler() {

            @Override
            protected boolean checkDragboard(Dragboard dragboard) {
                return dragboard == null || !Objects.equals(dragboard.getString(), dragBoardContent);
            }

            @Override
            protected void dragOver(DragEvent event) {
                disable();
                appendTitle("===" + I18nHelper.dragTip1());
            }

            @Override
            public void dragExited(DragEvent event) {
                enable();
                restoreTitle();
            }

            @Override
            public void dragDropped(DragEvent event) {
                if (event.getDragboard() != null && event.getDragboard().getFiles() != null) {
                    onDragFile.accept(event.getDragboard().getFiles());
                }
            }
        };
        // 初始化事件
        dragFileHandler.initEvent(this.scene());
        // // 文件拖拽初始化
        // DragUtil.initDragFile(new DragFileHandler() {
        //
        //     @Override
        //     public boolean checkDragboard(Dragboard dragboard) {
        //         return dragboard == null || !Objects.equals(dragboard.getString(), dragBoardContent);
        //     }
        //
        //     @Override
        //     protected void dragOver(DragEvent event) {
        //         disable();
        //         appendTitle("===松开鼠标以释放文件===");
        //     }
        //
        //     @Override
        //     public void dragExited(DragEvent event) {
        //         enable();
        //         restoreTitle();
        //     }
        //
        //     @Override
        //     public void dragDropped(DragEvent event) {
        //         if (event.getDragboard() != null && event.getDragboard().getFiles() != null) {
        //             onDragFile.accept(event.getDragboard().getFiles());
        //         }
        //     }
        // }, this.scene());
    }

    default boolean isShowing() {
        return this.stage().isShowing();
    }

    default void setIconified(boolean iconified) {
        this.stage().setIconified(iconified);
    }

    default boolean isIconified() {
        return this.stage().isIconified();
    }

    default boolean isFocused() {
        return this.stage().isFocused();
    }

    default void requestFocus() {
        this.stage().requestFocus();
    }

    default double getOpacity() {
        return this.stage().getOpacity();
    }

    default void setOpacity(double opacity) {
        this.stage().setOpacity(opacity);
    }

    default void hide() {
        this.stage().hide();
    }

    default void close() {
        this.stage().close();
    }

    default void show() {
        this.stage().show();
    }

    default void showAndWait() {
        this.stage().showAndWait();
    }
}
