package cn.oyzh.fx.plus.test.webview;

import cn.hutool.extra.spring.EnableSpringUtil;
import cn.oyzh.fx.plus.spring.SpringApplication;
import cn.oyzh.fx.plus.window.StageManager;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author oyzh
 * @since 2023/11/21
 */
@EnableSpringUtil
@SpringBootApplication
public class WebViewTestApp extends SpringApplication {

    public static void main(String[] args) {
        launchSpring(WebViewTestApp.class, args);
    }

    @Override
    public void start(Stage primaryStage) {
        super.start(primaryStage);
        // 显示主页面
        StageManager.showStage(WebViewTestController.class);
    }

    @Override
    public void destroy() throws Exception {

    }

    @Override
    public void run(String... args) throws Exception {

    }
}
