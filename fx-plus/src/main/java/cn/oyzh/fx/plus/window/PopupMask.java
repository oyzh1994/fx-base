package cn.oyzh.fx.plus.window;

import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.util.BooleanUtil;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.util.FXColorUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.PopupWindow;
import javafx.stage.Window;

/**
 * @author oyzh
 * @since 2026-04-15
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

    public PopupMask(Window target, Runnable callback) {
        // 初始化默认属性
        this.initDefault();
        this.target = target;
        this.callback = callback;

        // 遮罩板
        StackPane maskPane = new StackPane();
        // 设置位置
        this.setX(target.getX());
        this.setY(target.getY());
        // 设置大小
        maskPane.setPrefWidth(target.getWidth());
        maskPane.setPrefHeight(target.getHeight());

        // 设置透明度
        maskPane.setOpacity(0.3);
        maskPane.setFocusTraversable(false);
        // 半透明黑色背景‌
        maskPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        // 绑定大小、位置
        target.xProperty().addListener((observable, oldValue, newValue) -> this.setX(newValue.doubleValue()));
        target.yProperty().addListener((observable, oldValue, newValue) -> this.setY(newValue.doubleValue()));
        target.widthProperty().addListener((observable, oldValue, newValue) -> maskPane.setPrefWidth(newValue.doubleValue()));
        target.widthProperty().addListener((observable, oldValue, newValue) -> maskPane.setPrefHeight(newValue.doubleValue()));

        // 动画
        ProgressIndicator  indicator = new ProgressIndicator();
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
        // 监听显示属性
        this.showingProperty().addListener((observable, oldValue, newValue) -> {
            if (BooleanUtil.isTrue(newValue)) {
                TaskManager.startAsync(this::doCallback);
            }
        });
    }

    /**
     * 初始化默认属性
     */
    protected void initDefault() {
        this.setAutoFix(true);
        this.setAutoHide(false);
        this.setHideOnEscape(false);
    }

    /**
     * 执行回调
     */
    protected void doCallback() {
        // 执行回调
        if (this.callback != null) {
            this.callback.run();
        }
        // 执行业务
        FXUtil.runWait(() -> {
            // 关闭当前窗口
            this.hide();
            this.onWindowClosed();
            // 聚焦原窗口
            if (this.target != null) {
                this.target.requestFocus();
            }
        });
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
            PopupMask mask = new PopupMask(window, callback);
            mask.showPopup(window, window.getX(), window.getY());
        });
    }
}
