package cn.oyzh.fx.plus.test;

import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

/**
 * @author oyzh
 * @since 2024-11-15
 */
public class SvgTest1 extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        // SVGGlyph glyph = new SVGGlyph("/minus.svg");
        SVGGlyph glyph = new SVGGlyph("/folder.svg");


        FXHBox box = new FXHBox(glyph);
        Scene scene = new Scene(box);
        primaryStage.setWidth(200);
        primaryStage.setHeight(200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

class SvgTest1Main {

    public static void main(String[] args) {
        SvgTest1.main(args);
    }
}
