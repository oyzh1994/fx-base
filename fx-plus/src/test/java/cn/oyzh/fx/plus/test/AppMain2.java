package cn.oyzh.fx.plus.test;

import cn.oyzh.fx.plus.tree.AvdTreeView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;


/**
 * @author oyzh
 * @since 2022/5/18
 */
public class AppMain2 extends Application {

    public static void main(String[] args) {
        launch(AppMain2.class, args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.test1(stage);
    }

    private void test1(Stage stage) {

        AvdTreeView treeView = new AvdTreeView();


        HBox group = new HBox(treeView);
        stage.setScene(new Scene(group, 500, 500));
        stage.show();
    }


}
