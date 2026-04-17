package cn.oyzh.fx.plus.window;

import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.util.BooleanUtil;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.util.FXColorUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * @author oyzh
 * @see PopupMask
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

//    /**
//     * 异步回调
//     */
//    private final Future<?> future;

    private ChangeListener<? super Number> xFunc;

    private ChangeListener<? super Number> yFunc;

    private ChangeListener<? super Number> wFunc;

    private ChangeListener<? super Number> hFunc;

    public StageMask(Window target, Runnable callback) {
        this.target = target;
        this.callback = callback;
        // 初始化
        this.initOwner(target);
        this.initStyle(StageStyle.TRANSPARENT);

        // 遮罩板
        StackPane maskPane = new StackPane();
        this.xFunc = (observable, oldValue, newValue) -> this.setX(newValue.doubleValue());
        this.yFunc = (observable, oldValue, newValue) -> this.setY(newValue.doubleValue());
        this.wFunc = (observable, oldValue, newValue) -> this.setWidth(newValue.doubleValue());
        this.hFunc = (observable, oldValue, newValue) -> this.setHeight(newValue.doubleValue());
        // 设置位置
        this.setLocation(target.getX(), target.getY());
        this.setSize(target.getWidth(), target.getHeight());

        // 设置透明度
        maskPane.setOpacity(0.3);
        maskPane.setFocusTraversable(false);
        // 半透明黑色背景‌
        maskPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        // 绑定大小、位置
        target.xProperty().addListener(this.xFunc);
        target.yProperty().addListener(this.yFunc);
        target.widthProperty().addListener(this.wFunc);
        target.heightProperty().addListener(this.hFunc);

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
        this.setScene(scene);

//        // 取消操作
//        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
//            if (event.getCode() == KeyCode.ESCAPE) {
//                this.cancel();
//            }
//        });

        // 监听显示属性
        this.showingProperty().addListener((observable, oldValue, newValue) -> {
            if (BooleanUtil.isTrue(newValue)) {
                TaskManager.startAsync(this::doCallback);
            } else {
                this.onWindowClosed();
            }
        });

//        // 执行业务
//        this.future = TaskManager.startAsync(this::doCallback);
    }

//    /**
//     * 取消
//     */
//    public void cancel() {
//        if (this.future != null) {
//            ExecutorUtil.cancel(this.future);
//        }
//    }

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
        StageAdapter.super.onWindowClosed();
        // 清除属性
        this.setScene(null);
        // 处理属性
        if (this.target != null) {
            this.target.requestFocus();
            this.target.xProperty().removeListener(this.xFunc);
            this.target.yProperty().removeListener(this.yFunc);
            this.target.widthProperty().removeListener(this.wFunc);
            this.target.heightProperty().removeListener(this.hFunc);
            this.xFunc = null;
            this.yFunc = null;
            this.wFunc = null;
            this.hFunc = null;
        }
    }

    @Override
    public Stage stage() {
        return this;
    }

    /**
     * 显示遮罩板
     *
     * @param window   窗口
     * @param callback 回调
     */
    public static void showMask(Window window, Runnable callback) {
        FXUtil.runLater(() -> {
            StageMask mask = new StageMask(window, callback);
            mask.show();
        });
    }
}
