package cn.oyzh.fx.plus.test;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;


/**
 * @author oyzh
 * @since 2024-11-15
 */
public class SvgTest5 extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
//        this.test1(primaryStage);
        this.test2(primaryStage);
    }

    private void test1(Stage stage) {
        SVGPath svgPath = new SVGPath();
        svgPath.setContent("M12 4.435c-1.989-5.399-12-4.597-12 3.568 0 4.068 3.06 9.481 12 14.997 8.94-5.516 12-10.929 12-14.997 0-8.165-10.011-8.967-12-3.568z");

        StackPane container = new StackPane(svgPath);
        container.setPrefSize(15, 15);

        // 动态绑定缩放比例
        svgPath.scaleXProperty().bind(
                container.widthProperty().divide(svgPath.getBoundsInLocal().getWidth())
        );
        svgPath.scaleYProperty().bind(
                container.heightProperty().divide(svgPath.getBoundsInLocal().getHeight())
        );

        stage.setScene(new Scene(container));
        stage.show();
    }

    private void test2(Stage stage) {
        SVGGlyph svgGlyph = new SVGGlyph("/fx-svg/export.svg");
        svgGlyph.setSize(15);
        stage.setScene(new Scene(svgGlyph));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

class SvgTest5Main {

    public static void main(String[] args) {
        SvgTest5.main(args);
    }
}
