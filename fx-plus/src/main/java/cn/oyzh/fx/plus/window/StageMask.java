package cn.oyzh.fx.plus.window;

import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.util.FXColorUtil;
import cn.oyzh.fx.plus.util.FXUtil;
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

    public StageMask(Window target, Runnable callback) {
        this.target = target;
        this.callback = callback;
        // 初始化
        this.initOwner(target);
        this.initStyle(StageStyle.TRANSPARENT);
        this.setLocation(target.getX(), target.getY());
        this.setSize(target.getWidth(), target.getHeight());

        // 遮罩板
        StackPane maskPane = new StackPane();
        // 设置透明度
        maskPane.setOpacity(0.3);
        maskPane.setFocusTraversable(false);
        // 半透明黑色背景‌
        maskPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        // 绑定大小、位置
        target.xProperty().addListener((observable, oldValue, newValue) -> this.setX(newValue.doubleValue()));
        target.yProperty().addListener((observable, oldValue, newValue) -> this.setY(newValue.doubleValue()));
        target.widthProperty().addListener((observable, oldValue, newValue) -> this.setWidth(newValue.doubleValue()));
        target.widthProperty().addListener((observable, oldValue, newValue) -> this.setHeight(newValue.doubleValue()));

        // 动画
        ProgressIndicator progress = new ProgressIndicator();
        progress.setFocusTraversable(false);
        Color color = ThemeManager.currentForegroundColor();
        String colorHex = FXColorUtil.getColorHex(color);
        progress.setStyle("-fx-progress-color: " + colorHex);

        // 添加到遮罩板
        maskPane.getChildren().add(progress);
        // 居中显示‌
        StackPane.setAlignment(progress, Pos.CENTER);
        maskPane.toFront();
        maskPane.setMouseTransparent(false);

        // 创建场景
        Scene scene = new Scene(maskPane);
        // 透明度
        scene.setFill(Color.TRANSPARENT);
        this.setScene(scene);

        // 执行业务
        TaskManager.startDelay(this::close, 50);
    }

    @Override
    public void close() {
        if (this.callback != null) {
            try {
                this.callback.run();
                // 执行业务
                FXUtil.runWait(() -> {
                    // 清除属性
                    this.setScene(null);
                    // 关闭当前窗口
                    super.close();
                    // 聚焦原窗口
                    if (this.target != null) {
                        this.target.requestFocus();
                    }
                });
            } finally {
                this.target = null;
                this.callback = null;
            }
        }
    }

    @Override
    public Stage stage() {
        return this;
    }

    public static void showMask(Window window, Runnable callback) {
        FXUtil.runLater(() -> new StageMask(window, callback).show());
    }
}
