package cn.oyzh.fx.plus.test;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * @author oyzh
 * @since 2025-06-12
 */
public class AppTestMain extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        test1(stage);
    }

    private void test1(Stage stage){
        HBox hBox = new HBox();
        ToolBar toolBar = new ToolBar();
        toolBar.getItems().add(new Button("test1"));
        toolBar.getItems().add(new Button("test2"));
        toolBar.getItems().add(new Button("test3"));
        toolBar.getItems().add(new Button("test4"));
        toolBar.getItems().add(new CheckBox("test5"));
        toolBar.getItems().add(new CheckBox("test6"));

        hBox.getChildren().add(toolBar);
        stage.setScene(new Scene(hBox));
        stage.show();
    }

    public static class AppTestMainApp{

        public static void main(String[] args) {
            AppTestMain.main(args);
        }
    }
}
