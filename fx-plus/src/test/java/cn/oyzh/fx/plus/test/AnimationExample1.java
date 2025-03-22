package cn.oyzh.fx.plus.test;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URISyntaxException;

public class AnimationExample1 extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 创建一个面板
        Pane root = new Pane();

        // 创建一个矩形作为动画对象
        Rectangle rectangle = new Rectangle(50, 50, Color.BLUE);
        // 设置 A 点位置
        rectangle.setX(50);
        rectangle.setY(50);
        root.getChildren().add(rectangle);

        // 定义 B 点位置
        double endX = 300;
        double endY = 300;

        // 为矩形添加点击事件处理
        rectangle.setOnMouseClicked(event -> {
            // 创建一个时间轴动画
            Timeline timeline = new Timeline();

            // 创建移动和缩小的关键帧
            KeyFrame startFrame = new KeyFrame(Duration.ZERO,
                    new KeyValue(rectangle.xProperty(), rectangle.getX()),
                    new KeyValue(rectangle.yProperty(), rectangle.getY()),
                    new KeyValue(rectangle.widthProperty(), rectangle.getWidth()),
                    new KeyValue(rectangle.heightProperty(), rectangle.getHeight())
            );

            KeyFrame endFrame = new KeyFrame(Duration.seconds(2),
                    new KeyValue(rectangle.xProperty(), endX),
                    new KeyValue(rectangle.yProperty(), endY),
                    new KeyValue(rectangle.widthProperty(), 0),
                    new KeyValue(rectangle.heightProperty(), 0)
            );

            // 将关键帧添加到时间轴
            timeline.getKeyFrames().addAll(startFrame, endFrame);
            // 动画结束后移除矩形
            timeline.setOnFinished(e -> root.getChildren().remove(rectangle));
            // 播放动画
            timeline.play();
        });

        // 创建场景
        Scene scene = new Scene(root, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("JavaFX Animation");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class AnimationExampleApp {

        public static void main(String[] args) throws URISyntaxException {
            AnimationExample1.main(args);
        }
    }
}    