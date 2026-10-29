package cn.oyzh.fx.plus.window;

import cn.oyzh.common.object.ObjectWatcherManager;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.util.BooleanUtil;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.util.FXColorUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.PopupWindow;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * 弹窗遮罩
 * 已废弃：PopupWindow 在 Linux 下会始终置顶，二次弹窗等场景无法保证层级，请使用 {@link StageMask}。
 * 注意：PopupWindow 本身不能获得焦点，无法可靠拦截键盘输入，需要拦截键盘时应使用 {@link StageMask}。
 *
 * @author oyzh
 * @since 2025-03-12
 */
@Deprecated
public class PopupMask extends Popup implements PopupAdapter {

    /**
     * 目标窗口
     */
    private Window target;

    /**
     * 回调
     */
    private Runnable callback;

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
     * 最小化状态变化监听
     */
    private ChangeListener<? super Boolean> iconifiedFunc;

    /**
     * 目标窗口显示状态变化监听
     */
    private ChangeListener<? super Boolean> showingFunc;

    /**
     * 是否正在刷新弹窗
     */
    private boolean refreshing;

    /**
     * 是否临时隐藏（目标窗口最小化、失焦或打开原生弹窗）
     */
    private boolean suspended;

    /**
     * 是否已经完成清理
     */
    private boolean closed;

    /**
     * 获取遮罩的目标窗口
     *
     * @return 目标窗口
     */
    Window targetWindow() {
        return this.target;
    }

    /**
     * 构造弹窗遮罩
     *
     * @param target   目标窗口
     * @param callback 回调
     */
    public PopupMask(Window target, Runnable callback) {
        // 初始化默认属性
        this.initDefault();
        this.target = target;
        this.callback = callback;

        // 遮罩板
        StackPane maskPane = new StackPane();
        // 设置透明度
        maskPane.setOpacity(0.3);
        maskPane.setFocusTraversable(false);
        // 半透明黑色背景‌
        maskPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

        // 初始化
        if (target != null) {
            // 设置位置
            this.setX(target.getX());
            this.setY(target.getY());
            // 设置大小
            maskPane.setPrefWidth(target.getWidth());
            maskPane.setPrefHeight(target.getHeight());
            this.xFunc = (observable, oldValue, newValue) -> this.setX(newValue.doubleValue());
            this.yFunc = (observable, oldValue, newValue) -> this.setY(newValue.doubleValue());
            this.wFunc = (observable, oldValue, newValue) -> maskPane.setPrefWidth(newValue.doubleValue());
            this.hFunc = (observable, oldValue, newValue) -> maskPane.setPrefHeight(newValue.doubleValue());
            // 绑定大小、位置
            target.xProperty().addListener(this.xFunc);
            target.yProperty().addListener(this.yFunc);
            target.widthProperty().addListener(this.wFunc);
            target.heightProperty().addListener(this.hFunc);

            // 防止焦点被抢走
            this.focusedFunc = (observable, oldValue, newValue) -> {
                if (BooleanUtil.isTrue(newValue)) {
                    if (this.suspended) {
                        this.resume();
                    } else if (this.isShowing()) {
                        this.front();
                    }
                } else if (OSUtil.isLinux() && this.target != null && this.target.isShowing()) {
                    // 目标窗口失去焦点通常意味着打开了模态弹窗或切换了应用。
                    // 临时收起遮罩，避免 Linux 上 Popup 始终压在模态弹窗之上。
                    this.suspend();
                }
            };
            target.focusedProperty().addListener(this.focusedFunc);

            // 遮罩临时收起时 PopupWindow 会移除自身的 owner 监听，这里补一个独立监听
            // 保证目标窗口在此时关闭仍能完成清理。
            this.showingFunc = (observable, oldValue, newValue) -> {
                if (!BooleanUtil.isTrue(newValue) && !this.refreshing) {
                    this.onWindowClosed();
                }
            };
            target.showingProperty().addListener(this.showingFunc);

            // 目标窗口最小化时隐藏遮罩，恢复时重新显示
            if (target instanceof Stage stage) {
                this.iconifiedFunc = (observable, oldValue, newValue) -> {
                    if (BooleanUtil.isTrue(newValue)) {
                        this.suspend();
                    } else {
                        this.resume();
                    }
                };
                stage.iconifiedProperty().addListener(this.iconifiedFunc);
            }
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
        // 设置内容
        this.content(maskPane);
        // 场景透明
        if (this.getScene() != null) {
            this.getScene().setFill(Color.TRANSPARENT);
        }
        // 监听显示属性
        this.showingProperty().addListener((observable, oldValue, newValue) -> {
            if (BooleanUtil.isTrue(newValue)) {
                if (!this.refreshing && !this.suspended) {
                    TaskManager.startAsync(this::doCallback);
                }
            } else if (!this.refreshing && !this.suspended) {
                this.onWindowClosed();
            }
        });
        ObjectWatcherManager.watch(this);
    }

    /**
     * 初始化默认属性
     */
    protected void initDefault() {
        this.setAutoFix(false);
        this.setAutoHide(false);
        this.setHideOnEscape(false);
    }

    /**
     * 弹窗前置
     */
    protected void front() {
        if (this.target == null || !this.isShowing() || this.refreshing || this.suspended) {
            return;
        }
        if (this.target instanceof Stage stage && stage.isIconified()) {
            return;
        }
        FXUtil.runLater(() -> {
            if (this.target == null || this.closed || this.suspended
                    || !this.target.isShowing() || !this.target.isFocused()) {
                return;
            }
            // PopupWindow没有toFront，重新显示以刷新原生窗口层级
            this.refreshing = true;
            try {
                this.hide();
                this.showPopup(this.target, this.target.getX(), this.target.getY());
            } finally {
                this.refreshing = false;
            }
        });
    }

    /**
     * 临时隐藏遮罩
     */
    protected void suspend() {
        if (!this.isShowing() || this.suspended) {
            return;
        }
        this.suspended = true;
        this.hide();
    }

    /**
     * 恢复临时隐藏的遮罩
     */
    protected void resume() {
        if (!this.suspended || this.target == null || !this.target.isShowing()) {
            return;
        }
        if (this.target instanceof Stage stage && stage.isIconified()) {
            return;
        }
        if (!this.target.isFocused()) {
            return;
        }
        this.suspended = false;
        this.refreshing = true;
        try {
            this.showPopup(this.target, this.target.getX(), this.target.getY());
        } finally {
            this.refreshing = false;
        }
    }

    /**
     * 执行回调
     */
    protected void doCallback() {
        try {
            // 执行回调
            if (this.callback != null) {
                this.callback.run();
            }
        } finally {
            // 回调完成时必须完成清理，包括目标窗口已经最小化或关闭的情况
            FXUtil.runWait(() -> {
                this.suspended = false;
                this.hide();
                this.onWindowClosed();
            });
            this.target = null;
            this.callback = null;
        }
    }

    @Override
    public void onWindowClosed() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        // 处理属性
        if (this.target != null) {
            this.target.xProperty().removeListener(this.xFunc);
            this.target.yProperty().removeListener(this.yFunc);
            this.target.widthProperty().removeListener(this.wFunc);
            this.target.heightProperty().removeListener(this.hFunc);
            this.target.focusedProperty().removeListener(this.focusedFunc);
            this.target.showingProperty().removeListener(this.showingFunc);
            if (this.target instanceof Stage stage) {
                stage.iconifiedProperty().removeListener(this.iconifiedFunc);
            }
            this.xFunc = null;
            this.yFunc = null;
            this.wFunc = null;
            this.hFunc = null;
            this.focusedFunc = null;
            this.iconifiedFunc = null;
            this.showingFunc = null;
        }
        PopupAdapter.super.onWindowClosed();
        this.target = null;
        this.callback = null;
    }

    @Override
    public PopupWindow popup() {
        return this;
    }

    @Override
    public Node content() {
        return super.getContent().getFirst();
    }

    @Override
    public void content(Node content) {
        if (content == null) {
            super.getContent().clear();
        } else {
            super.getContent().setAll(content);
        }
    }

    /**
     * 显示遮罩板
     *
     * @param window   窗口
     * @param callback 回调
     */
    public static void showMask(Window window, Runnable callback) {
        FXUtil.runLater(() -> {
            Window owner = window == null ? StageManager.getPrimaryStage() : window;
            if (owner == null || !owner.isShowing()) {
                if (callback != null) {
                    callback.run();
                }
                return;
            }
            PopupMask mask = new PopupMask(owner, callback);
            mask.showPopup(owner, owner.getX(), owner.getY());
        });
    }
}
