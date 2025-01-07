package cn.oyzh.fx.plus.test;

import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;


/**
 * @author oyzh
 * @since 2024-11-15
 */
public class SvgTest2 extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
//      this.test2(primaryStage);
//        this.test3(primaryStage);
//        this.test4(primaryStage);
        this.test5(primaryStage);
    }

    private void test1(Stage primaryStage) {
        SVGPath svgPath = new SVGPath();
        String svg1 = "M880 298.4H521L403.7 186.2c-1.5-1.4-3.5-2.2-5.5-2.2H144c-17.7 0-32 14.3-32 32v592c0 17.7 14.3 32 32 32h736c17.7 0 32-14.3 32-32V330.4c0-17.7-14.3-32-32-32zM840 768H184V256h188.5l119.6 114.4H840V768z";
        String svg2 = "M801.171 547.589H222.83c-17.673 0-32-14.327-32-32s14.327-32 32-32h578.341c17.673 0 32 14.327 32 32s-14.327 32-32 32z";
        svgPath.setContent(svg2);
        svgPath.setFill(Color.RED);

        Region region = new Region();
        region.setPrefWidth(100);
        region.setPrefHeight(100);
        region.setMinWidth(100);
        region.setMinHeight(100);
        region.setShape(svgPath);

        FXHBox box = new FXHBox(region);
        Scene scene = new Scene(box);
        primaryStage.setWidth(200);
        primaryStage.setHeight(200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void test2(Stage primaryStage) {
        String svg1 = "M929.8 528.1H93.5c-15.5 0-28-12.5-28-28s12.5-28 28-28h836.3c15.5 0 28 12.5 28 28s-12.5 28-28 28z";
        SVGPath svgPath = new SVGPath();
        svgPath.setContent(svg1);
        svgPath.setFill(Color.RED);

        Region region = new Region();
        region.setPrefWidth(1000);
        region.setPrefHeight(1000);
        region.setMinWidth(1000);
        region.setMinHeight(1000);
        region.setShape(svgPath);

        FXHBox box = new FXHBox(region);
        Scene scene = new Scene(box);
        primaryStage.setWidth(1000);
        primaryStage.setHeight(1000);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void test3(Stage primaryStage) {
        String svg1 = "M929.8 528.1H93.5c-15.5 0-28-12.5-28-28s12.5-28 28-28h836.3c15.5 0 28 12.5 28 28s-12.5 28-28 28z";
        SVGPath svgPath = new SVGPath();
        svgPath.setContent(svg1);
        svgPath.setFill(Color.RED);


//        svgPath.getTransforms().add(new Scale(0.1,0.1));


        // Create a StackPane and set its size
//        Pane pane = new Pane();
//        StackPane stackPane = new StackPane();
//        Pane pane = new HBox();
//        Pane pane = new VBox();
//        Pane pane = new FlowPane();
//        Pane pane = new BorderPane();
//        Pane pane = new AnchorPane();
//        Pane pane = new GridPane();
//        Label pane = new Label();
//        Text pane = new Text();
//        Pane pane = new TilePane();
        TextField pane = new TextField();
        pane.setPrefSize(200, 200); // Set preferred size of the StackPane
        pane.setMaxSize(200, 200); // Set preferred size of the StackPane
        pane.setMinSize(200, 200); // Set preferred size of the StackPane

//        pane.getChildren().setAll(svgPath);
        pane.setSkin(new TextFieldSkin(pane) {


            {
                this.getChildren().add(svgPath);
            }

            @Override
            protected void layoutChildren(double x, double y, double w, double h) {
                super.layoutChildren(x, y, w, h);
                // 文本域高度
                double height = this.getSkinnable().getHeight();
                // 按钮大小，规则 组件高*0.5-4
                double size = (Math.floor(height * 0.5) - 4);
                // 限制按钮大小
                size = NumberUtil.limit(size, 6, 15);
                // 计算按钮实际大小
                double btnSize = this.snapSizeX(size);
                // 位移的areaX值，规则 组件宽+x-按钮实际大小
                double areaX = w + x - btnSize - 10;
                // 位移的areaY1值
                double areaY1 = (h - btnSize - btnSize) / 2 + 2;
                // 位移的areaY2值
                double areaY2 = areaY1 + btnSize + 2;
                // 设置按钮位置
                super.positionInArea(svgPath, areaX, areaY1, btnSize, btnSize, 0, HPos.CENTER, VPos.CENTER);
            }
        });

        // Create a scene and add the StackPane to it
        Scene scene = new Scene(pane);

        primaryStage.setWidth(200);
        primaryStage.setHeight(200);
        primaryStage.setScene(scene);
        primaryStage.show();

//        svgPath.setScaleX(0);
//        svgPath.setScaleY(0);
//        svgPath.setLayoutX(0);
//        svgPath.setLayoutY(0);
        svgPath.setStrokeWidth(1);
        System.out.println(svgPath.getBoundsInLocal());
        System.out.println(svgPath.getBoundsInParent());
    }

    private void test4(Stage primaryStage) {
        String svg1 = "M929.8 528.1H93.5c-15.5 0-28-12.5-28-28s12.5-28 28-28h836.3c15.5 0 28 12.5 28 28s-12.5 28-28 28z";
        SVGPath svgPath = new SVGPath();
        svgPath.setContent(svg1);
        svgPath.setFill(Color.RED);

//        svgPath.getTransforms().add(new Scale(0.1,0.1,1,1));


        StackPane pane = new StackPane() {


            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                this.layoutInArea(svgPath, 0, 0, 20, 20, 0, HPos.CENTER, VPos.CENTER);
            }

            @Override
            protected void layoutInArea(Node child, double areaX, double areaY, double areaWidth, double areaHeight, double areaBaselineOffset, HPos halignment, VPos valignment) {
                if (child == svgPath) {
                    super.layoutInArea(child, 0, 0, 20, 20, areaBaselineOffset, halignment, valignment);
                } else {
                    super.layoutInArea(child, areaX, areaY, areaWidth, areaHeight, areaBaselineOffset, halignment, valignment);

                }
            }
        };
//        Tab pane = new Tab();
//        Button pane = new Button();
//        pane.setPrefSize(200, 200); // Set preferred size of the StackPane
//        pane.setMaxSize(200, 200); // Set preferred size of the StackPane
//        pane.setMinSize(200, 200); // Set preferred size of the StackPane
//        pane.getChildren().add(svgPath);

//        pane.setShape(svgPath);


        // Create a scene and add the StackPane to it
        Scene scene = new Scene(pane);

        primaryStage.setWidth(200);
        primaryStage.setHeight(200);
        primaryStage.setScene(scene);
        primaryStage.show();

//        svgPath.setScaleX(0);
//        svgPath.setScaleY(0);
//        svgPath.setLayoutX(0);
//        svgPath.setLayoutY(0);
        svgPath.setStrokeWidth(1);
        System.out.println(svgPath.getBoundsInLocal());
        System.out.println(svgPath.getBoundsInParent());
    }

    private void test5(Stage primaryStage) {
        String svg1 = "M929.8 528.1H93.5c-15.5 0-28-12.5-28-28s12.5-28 28-28h836.3c15.5 0 28 12.5 28 28s-12.5 28-28 28z";
        String svg2 = "M768 245.76a10.24 10.24 0 0 1 10.24 10.24v512a10.24 10.24 0 0 1-10.24 10.24H256a10.24 10.24 0 0 1-10.24-10.24V256a10.24 10.24 0 0 1 10.24-10.24z m-10.24 20.48H266.24v491.52h491.52V266.24zM645.12 655.36a10.24 10.24 0 0 1 1.19808 20.40832L645.12 675.84H378.88a10.24 10.24 0 0 1-1.19808-20.40832L378.88 655.36h266.24z";
        SVGPath svgPath = new SVGPath();
//        svgPath.setContent(svg2);
        svgPath.setContent(svg1);
        svgPath.setFill(Color.RED);
        Bounds bounds = svgPath.getBoundsInLocal();
        WritableImage writableImage = new WritableImage((int) bounds.getWidth(), (int) bounds.getHeight());
        SnapshotParameters snapshotParameters = new SnapshotParameters();
        snapshotParameters.setViewport(new Rectangle2D(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight()));

//        snapshotParameters.setDepthBuffer(true);
        WritableImage writableImage1 = svgPath.snapshot(new SnapshotParameters(), writableImage);
        ImageView imageView = new ImageView(writableImage);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(16);
//        imageView.setFitHeight(128);
        HBox stackPane = new HBox(imageView, new Button("aaa"));
        Scene scene = new Scene(stackPane);

        primaryStage.setWidth(200);
        primaryStage.setHeight(200);
        primaryStage.setScene(scene);
        primaryStage.show();

//        svgPath.setScaleX(0);
//        svgPath.setScaleY(0);
//        svgPath.setLayoutX(0);
//        svgPath.setLayoutY(0);
//        svgPath.setStrokeWidth(1);
        System.out.println(svgPath.getBoundsInLocal());
        System.out.println(svgPath.getBoundsInParent());
//        System.out.println(imageView.getFitWidth());
//        System.out.println(imageView.getFitHeight());
//        System.out.println(imageView.getBoundsInParent());
//        System.out.println(imageView.getBoundsInLocal());
    }

    public static void main(String[] args) {
        launch(args);
    }
}

class SvgTest2Main {

    public static void main(String[] args) {
        SvgTest2.main(args);
    }
}
