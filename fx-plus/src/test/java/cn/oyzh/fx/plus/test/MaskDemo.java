package cn.oyzh.fx.plus.test;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;

import java.net.URISyntaxException;

public class MaskDemo extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 创建主内容布局
        StackPane mainPane = new StackPane();
        mainPane.getChildren().addAll(createContent(), createMaskLayer());
        
        Scene scene = new Scene(mainPane, 600, 400);
        primaryStage.setTitle("JavaFX 遮罩板示例");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // 创建示例内容界面
    private StackPane createContent() {
        StackPane contentPane = new StackPane();
        contentPane.setStyle("-fx-background-color: #f0f0f0;");
        
        Button btn = new Button("点击我！");
        btn.setStyle("-fx-font-size: 20px;");
        contentPane.getChildren().add(btn);
        
        return contentPane;
    }

    // 创建遮罩层
    private StackPane createMaskLayer() {
        StackPane maskLayer = new StackPane();
        maskLayer.setAlignment(Pos.TOP_LEFT);
        maskLayer.setMouseTransparent(true); // 允许鼠标穿透操作下方控件

        // 全屏半透明背景
        Rectangle background = new Rectangle(600, 400);
        background.setFill(Color.rgb(0, 0, 0, 0.7));
        
        // 需要高亮的区域（示例为圆形，可替换为其他形状）
        Circle highlight = new Circle(150, 100, 30);
        
        // 使用形状相减创建挖空效果
        Shape mask = Shape.subtract(background, highlight);
        mask.setFill(Color.BLACK); // 必须设置填充色才能显示
        
        // 添加文字提示
        Label label = new Label("请点击下方按钮");
        label.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        StackPane.setMargin(label, new Insets(highlight.getCenterY() + 50, 0, 0, highlight.getCenterX() - 60));

        maskLayer.getChildren().addAll(mask, label);
        return maskLayer;
    }

    public static void main(String[] args) {
        launch(args);
    }


    public static class MaskDemoApp {

        public static void main(String[] args) throws URISyntaxException {
            MaskDemo.main(args);
        }
    }
}
