package cn.oyzh.fx.plus.test;

import cn.oyzh.fx.plus.rich.RichTextFlow;
import javafx.application.Application;
import javafx.scene.Scene;
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
