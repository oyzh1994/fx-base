package cn.oyzh.fx.plus.window;

import cn.oyzh.common.object.ObjectWatcherManager;
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
import javafx.stage.Window;

/**
 * 弹窗遮罩
 *
 * @author oyzh
 * @since 2025-03-12
 */
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
     * 是否正在刷新弹窗
     */
    private boolean refreshing;

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
        // 设置内容
        this.content(maskPane);
        // 场景透明
        if (this.getScene() != null) {
            this.getScene().setFill(Color.TRANSPARENT);
        }
        // 监听显示属性
        this.showingProperty().addListener((observable, oldValue, newValue) -> {
            if (BooleanUtil.isTrue(newValue)) {
                if (!this.refreshing) {
                    TaskManager.startAsync(this::doCallback);
                }
            } else if (!this.refreshing) {
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
        if (this.target == null || !this.isShowing() || this.refreshing) {
            return;
        }
        FXUtil.runLater(() -> {
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
     * 执行回调
     */
    protected void doCallback() {
        // 执行回调
        if (this.callback != null) {
            this.callback.run();
        }
        // 关闭当前窗口
        FXUtil.runWait(this::hide);
        this.target = null;
        this.callback = null;
    }

    @Override
    public void onWindowClosed() {
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
        PopupAdapter.super.onWindowClosed();
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
