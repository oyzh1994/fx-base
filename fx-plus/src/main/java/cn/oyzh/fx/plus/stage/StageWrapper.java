package cn.oyzh.fx.plus.stage;

import cn.hutool.core.util.ArrayUtil;
import cn.oyzh.fx.common.thread.ExecutorUtil;
import cn.oyzh.fx.plus.adapter.PropAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.drag.DragFileHandler;
import cn.oyzh.fx.plus.drag.DragUtil;
import cn.oyzh.fx.plus.ext.FXMLLoaderExt;
import cn.oyzh.fx.plus.handler.EscHideHandler;
import cn.oyzh.fx.plus.handler.StateManager;
import cn.oyzh.fx.plus.handler.TabSwitchHandler;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.util.CursorUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.util.IconUtil;
import cn.oyzh.fx.plus.util.StyleUtil;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.NonNull;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 舞台包装
 *
 * @author oyzh
 * @since 2023/10/11
 */
public interface StageWrapper extends PropAdapter, StateAdapter, ThemeAdapter {

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

    /**
     * 初始化舞台
     *
     * @param attribute 舞台属性
     * @param owner     父窗口
     */
    default void init(@NonNull StageAttribute attribute, Window owner) {
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
        if (this.hasBeenVisible()) {
            this.stage().initStyle(attribute.stageStyle());
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
        // 设置主题
        this.changeTheme(ThemeManager.currentTheme());
        // 设置事件
        if (this.controller() instanceof StageListener listener) {
            this.initListener(listener);
        }
        // 监听显示属性
        this.stage().showingProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                this.onClosed();
            }
        });
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
        this.stage().setOnHiding(listener::onStageHiding);
        this.stage().setOnHidden(listener::onStageHidden);
        this.stage().setOnShowing(listener::onStageShowing);
        this.stage().setOnCloseRequest(listener::onStageCloseRequest);
    }

    /**
     * 关闭事件
     */
    default void onClosed() {
        try {
            this.unSwitchOnTab();
            this.unHideOnEscape();
            this.clearProps();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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

    /**
     * hand鼠标样式
     */
    default void handCursor() {
        CursorUtil.handCursor(this.stage());
    }

    /**
     * wait鼠标样式
     */
    default void waitCursor() {
        CursorUtil.waitCursor(this.stage());
    }

    /**
     * 默认鼠标样式
     */
    default void defaultCursor() {
        CursorUtil.defaultCursor(this.stage());
    }

    /**
     * 获取controller
     *
     * @return controller
     */
    default Object controller() {
        return this.getProp("_controller");
    }

    /**
     * 获取controller类
     *
     * @return controller类
     */
    default Class<?> controllerClass() {
        Object controller = this.controller();
        return controller == null ? null : controller.getClass();
    }

    /**
     * 设置按下eac时隐藏窗口
     */
    default void hideOnEscape() {
        if (!EscHideHandler.exists(this.stage())) {
            EscHideHandler.init(this.stage());
        }
    }

    /**
     * 取消按下eac时隐藏窗口
     */
    default void unHideOnEscape() {
        EscHideHandler.destroy(this.stage());
    }

    /**
     * 是否按下esc时隐藏窗口
     *
     * @return 结果
     */
    default boolean isHideOnEscape() {
        return EscHideHandler.exists(this.stage());
    }

    /**
     * 设置按下tab时切换组件
     */
    default void switchOnTab() {
        if (!TabSwitchHandler.exists(this.stage())) {
            TabSwitchHandler.init(this.stage());
        }
    }

    /**
     * 取消按下tab时切换组件
     */
    default void unSwitchOnTab() {
        TabSwitchHandler.destroy(this.stage());
    }

    /**
     * 是否按下tab时切换组件
     *
     * @return 结果
     */
    default boolean isSwitchOnTab() {
        return TabSwitchHandler.exists(this.stage());
    }

    /**
     * 初始化文件拖拽事件
     *
     * @param dragboardContent 拖拽板内容
     * @param onDragFile       文件拖入处理
     */
    default void initDragFile(@NonNull String dragboardContent, @NonNull Consumer<List<File>> onDragFile) {
        // 文件拖拽初始化
        DragUtil.initDragFile(new DragFileHandler() {

            @Override
            public boolean checkDragboard(Dragboard dragboard) {
                return dragboard == null || !Objects.equals(dragboard.getString(), dragboardContent);
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

    @Override
    default void setStateManager(StateManager manager) {
        StateAdapter.super.stateManager(manager);
    }

    @Override
    default StateManager getStateManager() {
        return StateAdapter.super.stateManager();
    }
}
