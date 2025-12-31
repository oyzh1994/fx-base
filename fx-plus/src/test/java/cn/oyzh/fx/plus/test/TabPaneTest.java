package cn.oyzh.fx.plus.test;

import cn.oyzh.common.object.ObjectWatcher;
import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URISyntaxException;

public class TabPaneTest extends Application {

    @Override
    public void start(Stage primaryStage) {
       test2(primaryStage);
    }

    private void test1(Stage primaryStage) {
        VBox root = new VBox();
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab tab1 = new Tab("测试1");

        TabPane tabPane1 = new TabPane();
        Tab tab11 = new Tab("测试11");
        ObjectWatcher.watch(tab11);
        Tab tab12 = new Tab("测试12");
        Tab tab13 = new Tab("测试13");
        tabPane1.getTabs().add(tab11);
        tabPane1.getTabs().add(tab12);
        tabPane1.getTabs().add(tab13);
        tab1.setContent(tabPane1);

        Tab tab2 = new Tab("测试2");
        TabPane tabPane2 = new TabPane();
        Tab tab21 = new Tab("测试21");
        Tab tab22 = new Tab("测试22");
        Tab tab23 = new Tab("测试23");
        tabPane2.getTabs().add(tab21);
        tabPane2.getTabs().add(tab22);
        tabPane2.getTabs().add(tab23);
        tab2.setContent(tabPane2);

        Tab tab3 = new Tab("测试3");
        TabPane tabPane3 = new TabPane();
        Tab tab31 = new Tab("测试31");
        Tab tab32 = new Tab("测试32");
        Tab tab33 = new Tab("测试33");
        tabPane3.getTabs().add(tab31);
        tabPane3.getTabs().add(tab32);
        tabPane3.getTabs().add(tab33);
        tab3.setContent(tabPane3);

        tabPane.getTabs().add(tab1);
        tabPane.getTabs().add(tab2);
        tabPane.getTabs().add(tab3);
        root.getChildren().add(tabPane);

        // 创建场景并显示
        Scene scene = new Scene(root, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();

        ThreadUtil.start(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    ThreadUtil.sleep(1000);
                    SystemUtil.gc();
                    System.runFinalization();
                    System.out.println("----gc");
                }
            }
        });
    }

    private void test2(Stage primaryStage) {
        FXVBox root = new FXVBox();
        FXTabPane tabPane = new FXTabPane();

        FXTab tab11 = new FXTab("测试11");
        tab11.setClosable(true);
        ObjectWatcher.watch(tab11);
        Tab tab12 = new Tab("测试12");
        tabPane.getTabs().add(tab11);
        tabPane.getTabs().add(tab12);

        root.getChildren().add(tabPane);

        // 创建场景并显示
        Scene scene = new Scene(root, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();

        ThreadUtil.start(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    ThreadUtil.sleep(3000);
                    SystemUtil.gc();
                    System.runFinalization();
                    System.out.println("----gc");
                }
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class TabPaneTestStarter {

        public static void main(String[] args) throws URISyntaxException {
            TabPaneTest.main(args);
        }
    }
}