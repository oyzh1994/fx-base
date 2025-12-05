package cn.oyzh.fx.plus.test.table;

import cn.oyzh.fx.plus.ext.FXApplication;
import cn.oyzh.fx.plus.window.StageManager;
import javafx.stage.Stage;

/**
 * @author oyzh
 * @since 2023/11/21
 */
public class TableTestApp extends FXApplication {

    public static void main(String[] args) {
        launch(TableTestApp.class, args);
    }

    @Override
    public void start(Stage primaryStage) {
        super.start(primaryStage);
        // 显示主页面
        // StageManager.showStage(TableTestController.class);
        StageManager.showStage(TableTest1Controller.class);
    }

    @Override
    protected void initSystemTray() {

    }

}
