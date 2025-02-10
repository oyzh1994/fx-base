package cn.oyzh.fx.plus.test;

import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.rich.RichTextFlow;
import cn.oyzh.fx.plus.util.ControlUtil;
import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;


/**
 * @author oyzh
 * @since 2024-11-15
 */
public class RichTextFlowTest extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.test1(primaryStage);
    }

    private void test1(Stage primaryStage) {
        RichTextFlow richTextFlow = new RichTextFlow();
        richTextFlow.setText("1a2a3a");
        richTextFlow.setHighlight("a");
        Scene scene = new Scene(richTextFlow);
        primaryStage.setWidth(200);
        primaryStage.setHeight(200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

class RichTextFlowTestMain {

    public static void main(String[] args) {
        RichTextFlowTest.main(args);
    }
}
