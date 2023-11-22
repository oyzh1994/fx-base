package cn.oyzh.fx.plus.test.table;

import cn.oyzh.fx.plus.spring.SpringApplication;
import cn.oyzh.fx.plus.stage.StageUtil;
import javafx.stage.Stage;

/**
 * @author oyzh
 * @since 2023/11/21
 */
public class TableTestApp extends SpringApplication {

    public static void main(String[] args) {
        launchSpring(TableTestApp.class, args);
    }

    @Override
    public void start(Stage primaryStage) {
        super.start(primaryStage);
        // 显示主页面
        StageUtil.showStage(TableTestController.class);
    }

    @Override
    public void destroy() throws Exception {

    }

    @Override
    public void run(String... args) throws Exception {

    }
}
