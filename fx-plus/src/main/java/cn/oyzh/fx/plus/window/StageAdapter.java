package cn.oyzh.fx.plus.window;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ExecutorUtil;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.BooleanUtil;
import cn.oyzh.common.util.ReflectUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.FXStyle;
import cn.oyzh.fx.plus.controls.FXHeaderBar;
import cn.oyzh.fx.plus.drag.DragFileHandler;
import cn.oyzh.fx.plus.drag.DragUtil;
import cn.oyzh.fx.plus.ext.FXMLLoaderExt;
import cn.oyzh.fx.plus.handler.EscHideHandler;
import cn.oyzh.fx.plus.handler.TabSwitchHandler;
import cn.oyzh.fx.plus.mouse.MouseUtil;
import cn.oyzh.fx.plus.node.NodeDisposeUtil;
import cn.oyzh.fx.plus.node.NodeLifeCycleUtil;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.util.CursorUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.util.HeaderBarUtil;
import cn.oyzh.fx.plus.util.IconUtil;
import cn.oyzh.fx.plus.util.StyleUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
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
            // 主页面不处理
            if (this instanceof PrimaryStage) {
                return;
            }
            Stage stage = this.stage();
            WindowAdapter.super.onWindowClosed();
            DragUtil.clearDragFile(this.scene());
            NodeLifeCycleUtil.onStageDestroy(stage);
            // 销毁节点
            NodeDisposeUtil.dispose(stage);
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
        Stage stage = this.stage();
        if (stage != null) {
            stage.setScene(null);
        }
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

    /**
     * 获取扩展标题栏
     *
     * @return 扩展标题栏
     */
    default FXHeaderBar getHeaderBar() {
        if (this.isExtendedHeader()) {
            return HeaderBarUtil.getHeaderBar(this.root());
        }
        return null;
    }

    /**
     * 是否扩展的标题栏
     *
     * @return 结果
     */
    default boolean isExtendedHeader() {
        return BooleanUtil.isTrue(this.getProp("extended:header"));
    }

    /**
     * 初始化舞台
     *
     * @param attribute 舞台属性
     * @param owner     父窗口
     */
    default void init(StageAttribute attribute, Window owner) {
        // 初始化加载器
        FXMLLoaderExt loader = new FXMLLoaderExt();
        // 加载根节点
        Parent root = loader.load(attribute.value());
        if (root == null) {
            throw new RuntimeException("load root fail");
        }
        // 扩展标题头类型
        if (attribute.stageStyle().isExtended()) {
            this.setProp("extended:header", true);
        }
        // 舞台
        Stage stage = this.stage();
        // 初始化场景
        Scene scene = new Scene(root);
        // 注意，扩展标题头类型不支持背景透明
        if (attribute.sceneTransparent() && !this.isExtendedHeader()) {
            scene.setFill(Color.TRANSPARENT);
        }
        // 设置scene
        FXUtil.runWait(() -> stage.setScene(scene));
        // 设置controller
        this.setProp("_controller", loader.getController());
        // 设置窗口样式
        if (!this.hasBeenVisible()) {
            stage.initStyle(attribute.stageStyle().toStageStyle());
        }
        // 初始化stage
        stage.setResizable(attribute.resizable());
        // 设置icon
        if (this.isExtendedHeader()) {
            // FXHeaderBar headerBar = HeaderBarUtil.getHeaderBar(root);
            // if (headerBar != null) {
            //     if (StringUtil.isNotEmpty(attribute.iconUrl())) {
            //         headerBar.setIcon(HeaderBarUtil.getIcon(attribute.iconUrl()));
            //     } else if (StringUtil.isNotEmpty(FXConst.appIcon())) {
            //         headerBar.setIcon(HeaderBarUtil.getIcon(FXConst.appIcon()));
            //     }
            // } else {
            //     JulLog.warn("headerBar is null");
            // }
        } else if (StringUtil.isNotEmpty(attribute.iconUrl())) {
            stage.getIcons().setAll(IconUtil.getIcon(attribute.iconUrl()));
        } else if (StringUtil.isNotEmpty(FXConst.appIcon())) {
            stage.getIcons().setAll(IconUtil.getIcon(FXConst.appIcon()));
        }
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
            // 显示监听
            stage.showingProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    if (!newValue) {
                        this.onWindowClosed();
                    } else {
                        this.changeTheme(ThemeManager.currentTheme());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JulLog.warn("showing error", ex);
                }
            });
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
            // 处理扩展标题栏事件
            if (this.isExtendedHeader()) {
                FXHeaderBar headerBar = this.getHeaderBar();
                // 标题为null
                if (headerBar == null) {
                    return;
                }
                // 鼠标按下事件
                stage.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                    if (MouseUtil.isPrimaryButton(event) && headerBar.checkBounds(event) && event.getTarget() == root) {
                        // 全屏则忽略
                        if (stage.isFullScreen()) {
                            return;
                        }
                        // 记录位置
                        if (event.getClickCount() == 1) {
                            headerBar.doRecordLocation();
                        } else if (event.getClickCount() == 2) {  // 最大化

                            stage.setMaximized(!stage.isMaximized());
                        }
                    }
                });
                // 鼠标拖动事件
                stage.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
                    if (MouseUtil.isPrimaryButton(event) && headerBar.checkBounds(event) && event.getTarget() == root) {
                        // 更新位置
                        headerBar.doUpdateLocation();
                    }
                });
                // 鼠标释放事件
                stage.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
                    if (MouseUtil.isPrimaryButton(event) && headerBar.checkBounds(event) && event.getTarget() == root) {
                        // 清除位置
                        headerBar.doClearLocation();
                    }
                });
            }
            // 初始化
            NodeManager.init(this);
        }
        // 加载自定义css文件
        if (ArrayUtil.isNotEmpty(attribute.cssUrls())) {
            root.getStylesheets().addAll(StyleUtil.split(attribute.cssUrls()));
        } else if (this.hasBeenVisible()) {// 显示过，则重新设置样式文件
            root.getStylesheets().removeAll(ThemeManager.currentUserAgentStylesheet(), FXStyle.FX_BASE);
            root.getStylesheets().addAll(ThemeManager.currentUserAgentStylesheet(), FXStyle.FX_BASE);
        }
        // 设置事件
        if (this.controller() instanceof StageListener listener) {
            this.initListener(listener);
        }
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
            if (this.isExtendedHeader()) {
                FXHeaderBar headerBar = this.getHeaderBar();
                if (headerBar != null) {
                    headerBar.setTitle(title);
                } else {
                    JulLog.warn("headerBar is null!");
                }
            } else {
                FXUtil.runWait(() -> this.stage().setTitle(title));
            }
        }
    }

    /**
     * 获取标题，扩展
     */
    default String title() {
        if (this.isExtendedHeader()) {
            FXHeaderBar headerBar = this.getHeaderBar();
            if (headerBar != null) {
                return headerBar.getTitle();
            }
            JulLog.warn("headerBar is null!");
            return null;
        }
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
                title = this.title();
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
