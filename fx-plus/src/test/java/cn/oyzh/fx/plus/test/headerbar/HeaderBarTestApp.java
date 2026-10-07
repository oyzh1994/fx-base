package cn.oyzh.fx.plus.test.headerbar;// package cn.oyzh.fx.plus.test.treeview;

import cn.oyzh.fx.plus.window.StageManager;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * HeaderBar 测试应用入口，加载标题栏测试界面
 *
 * @author oyzh
 * @since 2023-11-21
 */
public class HeaderBarTestApp extends Application {

    public static void main(String[] args) {
        System.setProperty("javafx.enablePreview", "true");
        System.setProperty("javafx.suppressPreviewWarning", "true");
        launch(HeaderBarTestApp.class, args);
    }

    @Override
    public void start(Stage primaryStage) {
        // 显示主页面
        StageManager.showStage(HeaderBarTestController.class);
    }

}
