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

    public StageMask(Window target, Runnable callback) {
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
        // 半透明黑色背景‌:ml-citation{ref="2,6" data="citationList"}
        maskPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        // 绑定大小、位置
        target.xProperty().addListener((observable, oldValue, newValue) -> this.setX(newValue.doubleValue()));
        target.yProperty().addListener((observable, oldValue, newValue) -> this.setY(newValue.doubleValue()));
        target.widthProperty().addListener((observable, oldValue, newValue) -> this.setWidth(newValue.doubleValue()));
        target.widthProperty().addListener((observable, oldValue, newValue) -> this.setHeight(newValue.doubleValue()));

        // 动画
        ProgressIndicator progress = new ProgressIndicator();
        progress.setFocusTraversable(false);
//        progress.setStyle("-fx-progress-color: white; -fx-pref-width: 50px; -fx-pref-height: 50px;");
        Color color = ThemeManager.currentForegroundColor();
        String colorHex = FXColorUtil.getColorHex(color);
        progress.setStyle("-fx-progress-color: " + colorHex);

        // 添加到遮罩板
        maskPane.getChildren().add(progress);
        // 居中显示‌:ml-citation{ref="1,4" data="citationList"}
        StackPane.setAlignment(progress, Pos.CENTER);
        maskPane.toFront();
        maskPane.setMouseTransparent(false);

        // 创建场景
        Scene scene = new Scene(maskPane);
        // 透明度
        scene.setFill(Color.TRANSPARENT);
        this.setScene(scene);

        // 执行业务
        TaskManager.startDelay(() -> {
            try {
                callback.run();
            } finally {
                FXUtil.runWait(() -> {
                    // 清除属性
                    this.setScene(null);
                    // 关闭当前窗口
                    this.close();
                    // 聚焦原窗口
                    target.requestFocus();
                });
            }
        }, 50);
    }

    @Override
    public Stage stage() {
        return this;
    }

    public static void showMask(Window window, Runnable callback) {
        FXUtil.runLater(() -> new StageMask(window, callback).show());
    }
}
