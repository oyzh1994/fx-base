package cn.oyzh.fx.gui.test;

import cn.oyzh.common.object.ObjectWatcher;
import cn.oyzh.common.object.ObjectWatcherManager;
import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.fx.gui.tabs.RichTabPane;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.stage.Stage;

import java.net.URISyntaxException;

public class TabPaneTest extends Application {

    @Override
    public void start(Stage primaryStage) {
       test2(primaryStage);
    }

    private void test2(Stage primaryStage) {
        FXVBox root = new FXVBox();
        RichTabPane tabPane = new RichTabPane();

        FXTab tab11 = new FXTab("测试11");
        tab11.setClosable(true);
        ObjectWatcherManager.watch(tab11);
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