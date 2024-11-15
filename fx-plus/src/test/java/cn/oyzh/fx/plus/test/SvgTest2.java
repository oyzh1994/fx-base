package cn.oyzh.fx.plus.test;

import cn.oyzh.fx.plus.controls.box.FXHBox;
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
public class SvgTest2 extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
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

    public static void main(String[] args) {
        launch(args);
    }


}

class SvgTest2Main {

    public static void main(String[] args) {
        SvgTest2.main(args);
    }
}
