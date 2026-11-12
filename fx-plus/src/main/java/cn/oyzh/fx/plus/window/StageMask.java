package cn.oyzh.fx.plus.window;

import cn.oyzh.common.object.ObjectWatcherManager;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.BooleanUtil;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.util.FXColorUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 舞台遮罩
 *
 * @author oyzh
 * @since 2025-03-12
 */
public class StageMask extends Stage implements StageAdapter {

    /**
     * 目标窗口
     */
    private Window target;

    /**
     * 回调
     */
    private Runnable callback;

    /**
     * 取消动作回调，仅在 ESC 或主动 cancel 时执行
     */
    private Runnable cancelAction;

    /**
     * 遮罩关闭清理回调
     */
    private Runnable closeCallback;

    /**
     * 异步任务
     */
    private Thread task;

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

    //    /**
    //     * 异步回调
    //     */
    //    private final Future<?> future;

    /**
     * x坐标变化监听
     */
    private ChangeListener<? super Number> xFunc;

    /**
     * y坐标变化监听
     */
    private ChangeListener<? super Number> yFunc;

    /**
     * 宽度变化监听
     */
    private ChangeListener<? super Number> wFunc;

    /**
     * 高度变化监听
     */
    private ChangeListener<? super Number> hFunc;

    /**
     * 焦点变化监听
     */
    private ChangeListener<? super Boolean> focusedFunc;

    /**
     * 构造舞台遮罩
     *
     * @param target   目标窗口
     * @param callback 回调
     */
    public StageMask(Window target, Runnable callback) {
        this(target, callback, null, null);
    }

    /**
     * 构造舞台遮罩
     *
     * @param target       目标窗口
     * @param callback     回调
     * @param cancelAction 取消动作
     */
    public StageMask(Window target, Runnable callback, Runnable cancelAction) {
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
    public StageMask(Window target, Runnable callback, Runnable cancelAction, Runnable closeCallback) {
        this.target = target;
        this.callback = callback;
        this.cancelAction = cancelAction;
        this.closeCallback = closeCallback;
        this.initStyle(StageStyle.TRANSPARENT);

        // 遮罩板
        StackPane maskPane = new StackPane();
        // 设置透明度
        maskPane.setOpacity(0.3);
        maskPane.setFocusTraversable(false);
        // 半透明黑色背景‌
        maskPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

        // 初始化
        if (target != null) {
            this.initOwner(target);
            // 设置位置
            this.setLocation(target.getX(), target.getY());
            this.setSize(target.getWidth(), target.getHeight());
            this.xFunc = (observable, oldValue, newValue) -> this.setX(newValue.doubleValue());
            this.yFunc = (observable, oldValue, newValue) -> this.setY(newValue.doubleValue());
            this.wFunc = (observable, oldValue, newValue) -> this.setWidth(newValue.doubleValue());
            this.hFunc = (observable, oldValue, newValue) -> this.setHeight(newValue.doubleValue());
            // 绑定大小、位置
            target.xProperty().addListener(this.xFunc);
            target.yProperty().addListener(this.yFunc);
            target.widthProperty().addListener(this.wFunc);
            target.heightProperty().addListener(this.hFunc);

            // 防止焦点被抢走
            this.focusedFunc = (observable, oldValue, newValue) -> {
                if (BooleanUtil.isTrue(newValue) && this.isShowing()) {
                    this.front();
                }
            };
            target.focusedProperty().addListener(this.focusedFunc);
        }

        // 动画
        ProgressIndicator indicator = new ProgressIndicator();
        indicator.setFocusTraversable(false);
        Color color = ThemeManager.currentForegroundColor();
        String colorHex = FXColorUtil.getColorHex(color);
        indicator.setStyle("-fx-progress-color: " + colorHex);

        // 添加到遮罩板
        maskPane.getChildren().add(indicator);
        // 居中显示‌
        StackPane.setAlignment(indicator, Pos.CENTER);
        maskPane.toFront();
        maskPane.setMouseTransparent(false);

        // 创建场景
        Scene scene = new Scene(maskPane);
        // 透明度
        scene.setFill(Color.TRANSPARENT);
        // ESC 打断当前任务，并取消回调
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                event.consume();
                this.cancel();
            }
        });
        this.setScene(scene);

        // 监听显示属性
        this.showingProperty().addListener((observable, oldValue, newValue) -> {
            if (BooleanUtil.isTrue(newValue)) {
                this.requestFocus();
                this.task = ThreadUtil.start(this::doCallback);
            } else {
                this.onWindowClosed();
            }
        });
        ObjectWatcherManager.watch(this);
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
            this.target = null;
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

    @Override
    public void onWindowClosed() {
        // 窗口被关闭时释放外部状态（例如 StageManager 的 mask 标志位）
        this.invokeCloseCallback();
        // 处理属性
        if (this.target != null) {
            this.target.xProperty().removeListener(this.xFunc);
            this.target.yProperty().removeListener(this.yFunc);
            this.target.widthProperty().removeListener(this.wFunc);
            this.target.heightProperty().removeListener(this.hFunc);
            this.target.focusedProperty().removeListener(this.focusedFunc);
            this.target.requestFocus();
            this.xFunc = null;
            this.yFunc = null;
            this.wFunc = null;
            this.hFunc = null;
            this.focusedFunc = null;
        }
        StageAdapter.super.onWindowClosed();
    }

    @Override
    public Stage stage() {
        return this;
    }

    /**
     * 显示遮罩板。显示期间按 ESC 会取消任务、关闭遮罩，未开始的 callback 不会执行。
     *
     * @param window   窗口
     * @param callback 回调
     */
    public static void showMask(Window window, Runnable callback) {
        showMask(window, callback, null, null);
    }

    /**
     * 显示遮罩板
     *
     * @param window       窗口
     * @param callback     回调
     * @param cancelAction 取消动作
     */
    public static void showMask(Window window, Runnable callback, Runnable cancelAction) {
        showMask(window, callback, cancelAction, null);
    }

    /**
     * 显示遮罩板
     *
     * @param window        窗口
     * @param callback      回调
     * @param cancelAction  取消动作
     * @param closeCallback 关闭清理回调
     */
    public static void showMask(Window window, Runnable callback, Runnable cancelAction, Runnable closeCallback) {
        FXUtil.runLater(() -> {
            StageMask mask = new StageMask(window, callback, cancelAction, closeCallback);
            mask.show();
        });
    }

    /**
     * 获取遮罩的目标窗口
     *
     * @return 目标窗口
     */
    Window targetWindow() {
        return this.target;
    }
}
