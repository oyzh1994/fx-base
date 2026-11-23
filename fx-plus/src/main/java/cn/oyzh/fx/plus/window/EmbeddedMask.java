package cn.oyzh.fx.plus.window;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.BooleanUtil;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.util.FXColorUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Window;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 内嵌舞台遮罩。遮罩作为节点直接加入目标窗口的场景，不再创建额外的透明窗口，
 * 避免 Windows 全屏下遮罩窗口被其他窗口遮挡的问题。
 *
 * <p>行为与 {@link StageMask} 保持一致：显示期间执行回调，按 ESC 取消并执行取消动作，
 * 关闭时执行清理回调。</p>
 *
 * @author oyzh
 * @since 2026-11-23
 */
public class EmbeddedMask extends StackPane {

    /**
     * 目标窗口
     */
    private Window target;

    /**
     * 目标场景
     */
    private Scene scene;

    /**
     * 异步任务
     */
    private volatile Thread task;

    /**
     * 回调
     */
    private Runnable callback;

    /**
     * 取消动作回调，仅在 ESC 或主动 cancel 时执行
     */
    private final Runnable cancelAction;

    /**
     * 遮罩关闭清理回调
     */
    private final Runnable closeCallback;

    /**
     * 是否已取消
     */
    private final AtomicBoolean cancelled = new AtomicBoolean();

    /**
     * 取消动作是否已执行
     */
    private final AtomicBoolean cancelActionInvoked = new AtomicBoolean();

    /**
     * 关闭清理是否已执行
     */
    private final AtomicBoolean closeCallbackInvoked = new AtomicBoolean();

    /**
     * 是否显示中
     */
    private volatile boolean showing;

    /**
     * 场景焦点节点
     */
    private Node previousFocusOwner;

    /**
     * 键盘事件拦截
     */
    private EventHandler<KeyEvent> keyFilter;

    /**
     * ESC 释放事件拦截，避免遮罩关闭后底层窗口响应 ESC
     */
    private EventHandler<KeyEvent> escapeReleaseFilter;

    /**
     * 场景尺寸监听
     */
    private ChangeListener<Number> sizeListener;

    /**
     * 目标窗口显示状态监听
     */
    private ChangeListener<Boolean> showingListener;

    /**
     * 构造舞台遮罩
     *
     * @param target   目标窗口
     * @param callback 回调
     */
    public EmbeddedMask(Window target, Runnable callback) {
        this(target, callback, null, null);
    }

    /**
     * 构造舞台遮罩
     *
     * @param target       目标窗口
     * @param callback     回调
     * @param cancelAction 取消动作
     */
    public EmbeddedMask(Window target, Runnable callback, Runnable cancelAction) {
        this(target, callback, cancelAction, null);
    }

    /**
     * 构造舞台遮罩
     *
     * @param target        目标窗口
     * @param callback      回调
     * @param cancelAction  取消动作
     * @param closeCallback 关闭清理回调
     */
    public EmbeddedMask(Window target, Runnable callback, Runnable cancelAction, Runnable closeCallback) {
        this.target = target;
        this.callback = callback;
        this.cancelAction = cancelAction;
        this.closeCallback = closeCallback;

        // 设置透明度
        this.setOpacity(0.3);
        this.setFocusTraversable(true);
        this.setPickOnBounds(true);
        this.setManaged(false);
        // 保证遮罩始终位于同级节点之上
        this.setViewOrder(-1);
        // 半透明黑色背景‌
        this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

        // 动画
        ProgressIndicator indicator = new ProgressIndicator();
        indicator.setFocusTraversable(false);
        Color color = ThemeManager.currentForegroundColor();
        String colorHex = FXColorUtil.getColorHex(color);
        indicator.setStyle("-fx-progress-color: " + colorHex);

        // 添加到遮罩板
        this.getChildren().add(indicator);
        // 居中显示‌
        StackPane.setAlignment(indicator, Pos.CENTER);
    }

    /**
     * 显示遮罩。必须在 JavaFX 应用线程调用。
     *
     * @return 是否显示成功
     */
    public boolean show() {
        if (this.showing) {
            return true;
        }
        if (this.target == null || this.target.getScene() == null) {
            return false;
        }
        Scene targetScene = this.target.getScene();
        Parent root = targetScene.getRoot();
        ObservableList<Node> children = this.children(root);
        if (children == null) {
            return false;
        }

        this.scene = targetScene;
        this.previousFocusOwner = targetScene.getFocusOwner();
        // 跟随场景大小
        this.prefWidthProperty().bind(targetScene.widthProperty());
        this.prefHeightProperty().bind(targetScene.heightProperty());
        this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        this.resizeToScene();

        // 键盘事件统一由遮罩处理，避免底层控件响应快捷键或输入
        this.keyFilter = event -> {
            event.consume();
            if (event.getEventType() == KeyEvent.KEY_PRESSED && event.getCode() == KeyCode.ESCAPE) {
                this.suppressEscapeRelease(targetScene);
                this.cancel();
            }
        };
        targetScene.addEventFilter(KeyEvent.KEY_PRESSED, this.keyFilter);
        targetScene.addEventFilter(KeyEvent.KEY_RELEASED, this.keyFilter);
        targetScene.addEventFilter(KeyEvent.KEY_TYPED, this.keyFilter);

        // 场景尺寸变化时同步遮罩大小
        this.sizeListener = (observable, oldValue, newValue) -> this.resizeToScene();
        targetScene.widthProperty().addListener(this.sizeListener);
        targetScene.heightProperty().addListener(this.sizeListener);

        // 目标窗口隐藏时关闭遮罩并清理状态
        this.showingListener = (observable, oldValue, newValue) -> {
            if (!BooleanUtil.isTrue(newValue)) {
                this.hide();
            }
        };
        this.target.showingProperty().addListener(this.showingListener);

        children.add(this);
        this.toFront();
        this.showing = true;
        this.requestFocus();
        this.task = ThreadUtil.start(this::doCallback);
        return true;
    }

    /**
     * 拦截 ESC 释放事件。遮罩关闭后按键释放仍可能传递给底层窗口，
     * 需要吞掉本次释放，避免触发窗口的 ESC 隐藏逻辑。
     *
     * @param targetScene 目标场景
     */
    private void suppressEscapeRelease(Scene targetScene) {
        if (this.escapeReleaseFilter != null) {
            return;
        }
        EventHandler<KeyEvent> filter = event -> {
            event.consume();
            if (event.getCode() == KeyCode.ESCAPE) {
                targetScene.removeEventFilter(KeyEvent.KEY_RELEASED, this.escapeReleaseFilter);
                this.escapeReleaseFilter = null;
            }
        };
        this.escapeReleaseFilter = filter;
        targetScene.addEventFilter(KeyEvent.KEY_RELEASED, filter);
        // 未收到按键释放时兜底清理
        FXUtil.runLater(() -> {
            if (this.escapeReleaseFilter == filter) {
                targetScene.removeEventFilter(KeyEvent.KEY_RELEASED, filter);
                this.escapeReleaseFilter = null;
            }
        }, 1000);
    }

    /**
     * 获取可添加遮罩的子节点列表
     *
     * @param root 根节点
     * @return 子节点列表
     */
    private ObservableList<Node> children(Parent root) {
        if (root instanceof Pane pane) {
            return pane.getChildren();
        }
        if (root instanceof Group group) {
            return group.getChildren();
        }
        return null;
    }

    /**
     * 调整遮罩大小
     */
    private void resizeToScene() {
        if (this.scene != null) {
            double width = this.scene.getWidth();
            double height = this.scene.getHeight();
            if (width > 0 && height > 0) {
                this.resizeRelocate(0, 0, width, height);
            }
        }
    }

    /**
     * 执行回调
     */
    protected void doCallback() {
        if (this.cancelled.get()) {
            return;
        }
        try {
            // 执行回调
            if (this.callback != null) {
                this.callback.run();
            }
        } finally {
            // 已被取消时由 cancel 负责关闭遮罩，避免重复执行
            if (!this.cancelled.get()) {
                FXUtil.runWait(this::hide);
            }
            this.task = null;
            this.callback = null;
        }
    }

    /**
     * 取消当前遮罩任务。
     *
     * <p>按下 ESC 或外部主动取消时调用。会中断异步任务、执行取消动作并关闭遮罩；
     * 回调是否会立即停止取决于任务本身是否响应线程中断。</p>
     */
    public void cancel() {
        if (!this.cancelled.compareAndSet(false, true)) {
            return;
        }
        this.cancelTask();
        this.invokeCancelAction();
        FXUtil.runLater(this::hide);
    }

    /**
     * 关闭遮罩并清理场景状态。
     */
    public void hide() {
        if (!Platform.isFxApplicationThread()) {
            FXUtil.runLater(this::hide);
            return;
        }
        if (!this.showing) {
            this.invokeCloseCallback();
            return;
        }
        this.showing = false;

        // 移除键盘拦截
        if (this.scene != null && this.keyFilter != null) {
            this.scene.removeEventFilter(KeyEvent.KEY_PRESSED, this.keyFilter);
            this.scene.removeEventFilter(KeyEvent.KEY_RELEASED, this.keyFilter);
            this.scene.removeEventFilter(KeyEvent.KEY_TYPED, this.keyFilter);
        }
        // 移除尺寸监听
        if (this.scene != null && this.sizeListener != null) {
            this.scene.widthProperty().removeListener(this.sizeListener);
            this.scene.heightProperty().removeListener(this.sizeListener);
        }
        // 移除显示状态监听
        if (this.target != null && this.showingListener != null) {
            this.target.showingProperty().removeListener(this.showingListener);
        }

        this.prefWidthProperty().unbind();
        this.prefHeightProperty().unbind();
        if (this.getParent() instanceof Pane pane) {
            pane.getChildren().remove(this);
        } else if (this.getParent() instanceof Group group) {
            group.getChildren().remove(this);
        }

        this.invokeCloseCallback();
        this.restoreFocus();
        this.scene = null;
        this.keyFilter = null;
        this.sizeListener = null;
        this.showingListener = null;
        this.target = null;
    }

    /**
     * 恢复遮罩显示前的焦点
     */
    private void restoreFocus() {
        Window window = this.target;
        if (window == null || !window.isFocused()) {
            return;
        }
        Node focusOwner = this.previousFocusOwner;
        if (focusOwner != null && focusOwner.getScene() == this.scene && focusOwner.isVisible()) {
            focusOwner.requestFocus();
        } else {
            window.requestFocus();
        }
        this.previousFocusOwner = null;
    }

    /**
     * 中断当前异步任务。
     */
    private void cancelTask() {
        Thread runningTask = this.task;
        if (runningTask != null) {
            ThreadUtil.interrupt(runningTask);
        }
    }

    /**
     * 执行取消动作，保证只执行一次。
     */
    private void invokeCancelAction() {
        if (this.cancelAction != null && this.cancelActionInvoked.compareAndSet(false, true)) {
            try {
                this.cancelAction.run();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 执行关闭清理回调，保证只执行一次。
     */
    private void invokeCloseCallback() {
        if (this.closeCallback != null && this.closeCallbackInvoked.compareAndSet(false, true)) {
            try {
                this.closeCallback.run();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 是否显示中
     *
     * @return 结果
     */
    public boolean isShowing() {
        return this.showing;
    }

    /**
     * 获取遮罩的目标窗口
     *
     * @return 目标窗口
     */
    Window targetWindow() {
        return this.target;
    }

    /**
     * 显示遮罩板。显示期间按 ESC 会取消任务、关闭遮罩，未开始的 callback 不会执行。
     *
     * @param window   窗口
     * @param callback 回调
     */
    public static void showMask(Window window, Runnable callback) {
        showMask(window, callback, null, null, null);
    }

    /**
     * 显示遮罩板
     *
     * @param window       窗口
     * @param callback     回调
     * @param cancelAction 取消动作
     */
    public static void showMask(Window window, Runnable callback, Runnable cancelAction) {
        showMask(window, callback, cancelAction, null, null);
    }

    /**
     * 显示遮罩板
     *
     * @param window        窗口
     * @param callback      回调
     * @param cancelAction  取消动作
     * @param closeCallback 关闭清理回调
     * @param fallback      兜底回调
     */
    public static void showMask(Window window, Runnable callback, Runnable cancelAction, Runnable closeCallback, Runnable fallback) {
        FXUtil.runLater(() -> {
            EmbeddedMask mask = new EmbeddedMask(window, callback, cancelAction, closeCallback);
            // 无法内嵌时回退到独立窗口遮罩
            if (!mask.show() && fallback != null) {
                fallback.run();
            }
        });
    }
}
