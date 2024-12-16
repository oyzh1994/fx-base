package cn.oyzh.fx.plus.window;

import cn.oyzh.common.thread.ExecutorUtil;
import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.drag.DragFileHandler;
import cn.oyzh.fx.plus.drag.DragUtil;
import cn.oyzh.fx.plus.ext.FXMLLoaderExt;
import cn.oyzh.fx.plus.handler.EscHideHandler;
import cn.oyzh.fx.plus.handler.TabSwitchHandler;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.titlebar.TitleBar;
import cn.oyzh.fx.plus.titlebar.TitleBox;
import cn.oyzh.fx.plus.util.CursorUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.util.IconUtil;
import cn.oyzh.fx.plus.util.NodeUtil;
import cn.oyzh.fx.plus.util.StyleUtil;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import lombok.NonNull;

import java.io.File;
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
            WindowAdapter.super.onWindowClosed();
            DragUtil.clearDragFile(this.scene());
            this.setTitleExt(null);
            this.scene(null);
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
        Boolean hasBeenVisible = this.getProp("_hasBeenVisible");
        return hasBeenVisible != null && hasBeenVisible;
    }

    /**
     * 设置已经显示过标志位
     */
    default void setBeenVisible() {
        this.setProp("_hasBeenVisible", true);
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
     * 是否最大化
     *
     * @return 最大化
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

    default void init(@NonNull StageAttribute attribute, Window owner) {
        this.initByCustom(attribute, owner);
        // this.initByPlatform(attribute, owner);
    }

    /**
     * 初始化舞台，自定义
     *
     * @param attribute 舞台属性
     * @param owner     父窗口
     */
    private void initByCustom(StageAttribute attribute, Window owner) {
        // 初始化加载器
        FXMLLoaderExt loader = new FXMLLoaderExt();
        // 加载根节点
        Parent root = loader.load(attribute.value());
        if (root == null) {
            throw new RuntimeException("load root fail");
        }
        // 设置controller
        this.setProp("_controller", loader.getController());
        // 设置窗口样式
        if (!this.hasBeenVisible()) {
            this.stage().initStyle(StageStyle.UNDECORATED);
        }
        TitleBar.TitleBarConfig config = new TitleBar.TitleBarConfig();
        config.setTitle(attribute.title());
        config.setMaximized(attribute.maximized());
        config.setResizable(attribute.resizable());
        if (StringUtil.isNotEmpty(attribute.iconUrl())) {
            config.setIcon(attribute.iconUrl());
        }
        TitleBox titleBox = new TitleBox();
        TitleBar titleBar = new TitleBar(config);
        titleBox.setTitleBar(titleBar);
        titleBox.setContent(root);
        // this.stage().setTitle(attribute.title());
        // this.stage().setMaximized(attribute.maximized());
        // this.stage().setResizable(attribute.resizeable());
        // // 设置icon
        // if (ArrayUtil.isNotEmpty(attribute.iconUrls())) {
        //     this.stage().getIcons().addAll(IconUtil.getIcons(attribute.iconUrls()));
        // }
        // 设置scene
        FXUtil.runWait(() -> this.stage().setScene(new Scene(titleBox)));
        // 非主窗口
        if (!attribute.usePrimary() && !this.hasBeenVisible()) {
            // 初始化父窗口
            if (owner != null) {
                this.stage().initOwner(owner);
                config.setShowMinimum(false);
            }
            // 初始化模态
            this.stage().initModality(attribute.modality());
        }
        // 加载自定义css文件
        if (ArrayUtil.isNotEmpty(attribute.cssUrls())) {
            root.getStylesheets().addAll(StyleUtil.split(attribute.cssUrls()));
        }
        // 设置事件
        if (this.controller() instanceof StageListener listener) {
            this.initListener(listener);
        }
        // 监听显示属性
        this.stage().showingProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                this.onWindowClosed();
            }
        });
        NodeManager.init(this);
        // 监听最大化，处理内置内容大小
        this.stage().maximizedProperty().addListener((observableValue, aBoolean, t1) -> TaskManager.startDelay("_stage_resize", () -> this.root().resize(NodeUtil.getWidth(this.stage()) - 15, NodeUtil.getHeight(this.stage()) - 40), 1));
    }

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
        // 设置controller
        this.setProp("_controller", loader.getController());
        // 设置窗口样式
        if (!this.hasBeenVisible()) {
            this.stage().initStyle(StageStyle.DECORATED);
        }
        // 初始化stage
        this.stage().setTitle(attribute.title());
        this.stage().setMaximized(attribute.maximized());
        this.stage().setResizable(attribute.resizeable());
        // 设置icon
        if (ArrayUtil.isNotEmpty(attribute.iconUrls())) {
            this.stage().getIcons().addAll(IconUtil.getIcons(attribute.iconUrls()));
        }
        // 设置scene
        FXUtil.runWait(() -> this.stage().setScene(new Scene(root)));
        // 非主窗口
        if (!attribute.usePrimary() && !this.hasBeenVisible()) {
            // 初始化父窗口
            if (owner != null) {
                this.stage().initOwner(owner);
            }
            // 初始化模态
            this.stage().initModality(attribute.modality());
        }
        // 加载自定义css文件
        if (ArrayUtil.isNotEmpty(attribute.cssUrls())) {
            root.getStylesheets().addAll(StyleUtil.split(attribute.cssUrls()));
        }
        // 设置事件
        if (this.controller() instanceof StageListener listener) {
            this.initListener(listener);
        }
        // 监听显示属性
        this.stage().showingProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                this.onWindowClosed();
            }
        });
        NodeManager.init(this);
        // 监听最大化，处理内置内容大小
        this.stage().maximizedProperty().addListener((observableValue, aBoolean, t1) -> TaskManager.startDelay("_stage_resize", () -> this.root().resize(NodeUtil.getWidth(this.stage()) - 15, NodeUtil.getHeight(this.stage()) - 40), 1));
    }

    /**
     * 初始化监听器
     *
     * @param listener 舞台监听器
     */
    default void initListener(@NonNull StageListener listener) {
        // 设置事件
        listener.onStageInitialize(this);
        this.stage().setOnShown(listener::onStageShown);
        this.stage().setOnHiding(listener::onWindowHiding);
        this.stage().setOnHidden(listener::onWindowHidden);
        this.stage().setOnShowing(listener::onWindowShowing);
        this.stage().setOnCloseRequest(listener::onStageCloseRequest);
    }

    /**
     * 设置标题，扩展
     *
     * @param title 标题
     */
    default void setTitleExt(String title) {
        if (title != null) {
            FXUtil.runWait(() -> this.stage().setTitle(title));
        }
    }

    /**
     * 获取标题，扩展
     */
    default String getTitleExt() {
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
            this.setTitleExt(newTitle);
            if (liveTime >= 0) {
                ExecutorUtil.start(this::restoreTitle, liveTime);
            }
        }
    }

    /**
     * 恢复标题
     */
    default void restoreTitle() {
        this.setTitleExt(this.getProp("_title"));
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
        if (!EscHideHandler.exists(this.stage())) {
            EscHideHandler.init(this.stage());
        }
    }

    @Override
    default void unHideOnEscape() {
        EscHideHandler.destroy(this.stage());
    }

    @Override
    default boolean isHideOnEscape() {
        return EscHideHandler.exists(this.stage());
    }

    @Override
    default void switchOnTab() {
        if (!TabSwitchHandler.exists(this.stage())) {
            TabSwitchHandler.init(this.stage());
        }
    }

    @Override
    default void unSwitchOnTab() {
        TabSwitchHandler.destroy(this.stage());
    }

    @Override
    default boolean isSwitchOnTab() {
        return TabSwitchHandler.exists(this.stage());
    }

    /**
     * 初始化文件拖拽事件
     *
     * @param dragBoardContent 拖拽板内容
     * @param onDragFile       文件拖入处理
     */
    default void initDragFile(@NonNull String dragBoardContent, @NonNull Consumer<List<File>> onDragFile) {
        // 文件拖拽初始化
        DragUtil.initDragFile(new DragFileHandler() {

            @Override
            public boolean checkDragboard(Dragboard dragboard) {
                return dragboard == null || !Objects.equals(dragboard.getString(), dragBoardContent);
            }

            @Override
            protected void dragOver(DragEvent event) {
                disable();
                appendTitle("===松开鼠标以释放文件===");
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
        }, this.scene());
    }

    default boolean isShowing() {
        return this.stage().isShowing();
    }

    default void setIconified(boolean b) {
        this.stage().setIconified(b);
    }

    default boolean isIconified() {
        return this.stage().isIconified();
    }


}
