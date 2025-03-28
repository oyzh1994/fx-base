package cn.oyzh.fx.plus.test;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URISyntaxException;

public class AnimationExample extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 创建一个面板
        Pane root = new Pane();

        // 创建上传图标
        Path uploadIcon = createUploadIcon();
        uploadIcon.setFill(Color.GREEN);
        // 设置 A 点位置
        uploadIcon.setTranslateX(50);
        uploadIcon.setTranslateY(50);
        root.getChildren().add(uploadIcon);

        // 创建下载图标
        Path downloadIcon = createDownloadIcon();
        downloadIcon.setFill(Color.RED);
        // 设置 A 点位置
        downloadIcon.setTranslateX(50);
        downloadIcon.setTranslateY(150);
        root.getChildren().add(downloadIcon);

        // 定义 B 点位置
        double endX = 300;
        double endY = 300;

        // 为上传图标添加点击事件处理
        uploadIcon.setOnMouseClicked(event -> {
            animateIcon(uploadIcon, endX, endY, root);
        });

        // 为下载图标添加点击事件处理
        downloadIcon.setOnMouseClicked(event -> {
            animateIcon(downloadIcon, endX, endY, root);
        });

        // 创建场景
        Scene scene = new Scene(root, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("JavaFX Animation");
        primaryStage.show();
    }

    // 创建上传图标
    private Path createUploadIcon() {
        Path path = new Path();
        path.getElements().add(new MoveTo(0, 20));
        path.getElements().add(new LineTo(20, 0));
        path.getElements().add(new LineTo(40, 20));
        path.getElements().add(new MoveTo(20, 0));
        path.getElements().add(new LineTo(20, 40));
        return path;
    }

    // 创建下载图标
    private Path createDownloadIcon() {
        Path path = new Path();
        path.getElements().add(new MoveTo(0, 0));
        path.getElements().add(new LineTo(20, 20));
        path.getElements().add(new LineTo(40, 0));
        path.getElements().add(new MoveTo(20, 20));
        path.getElements().add(new LineTo(20, 40));
        return path;
    }

    // 动画方法
    private void animateIcon(Path icon, double endX, double endY, Pane root) {
        // 创建一个时间轴动画
        Timeline timeline = new Timeline();

        // 创建移动和缩小的关键帧
        KeyFrame startFrame = new KeyFrame(Duration.ZERO,
                new KeyValue(icon.translateXProperty(), icon.getTranslateX()),
                new KeyValue(icon.translateYProperty(), icon.getTranslateY()),
                new KeyValue(icon.scaleXProperty(), 1),
                new KeyValue(icon.scaleYProperty(), 1)
        );

        KeyFrame endFrame = new KeyFrame(Duration.seconds(2),
                new KeyValue(icon.translateXProperty(), endX),
                new KeyValue(icon.translateYProperty(), endY),
                new KeyValue(icon.scaleXProperty(), 0),
                new KeyValue(icon.scaleYProperty(), 0)
        );

        // 将关键帧添加到时间轴
        timeline.getKeyFrames().addAll(startFrame, endFrame);
        // 动画结束后移除图标
        timeline.setOnFinished(e -> root.getChildren().remove(icon));
        // 播放动画
        timeline.play();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class AnimationExampleApp {

        public static void main(String[] args) throws URISyntaxException {
            AnimationExample.main(args);
        }
    }
}    