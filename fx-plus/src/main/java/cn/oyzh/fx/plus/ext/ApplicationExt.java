package cn.oyzh.fx.plus.ext;

import cn.oyzh.fx.plus.stage.StageUtil;
import cn.oyzh.fx.plus.view.FXViewUtil;
import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * fx 支持Spring启动的主入口
 *
 * @author oyzh
 * @since 2021/8/19
 */
@Slf4j
public abstract class ApplicationExt extends Application {

    public static void launchSpring(@NonNull Class<? extends Application> appClass, String... args) {
        try {
            SpringApplicationBuilder builder = new SpringApplicationBuilder(appClass);
            builder.web(WebApplicationType.NONE).headless(false).run(args);
            launch(appClass, args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        if (Platform.isSupported(ConditionalFeature.SCENE3D)) {
            log.info("3D加速已开启.");
        } else {
            log.warn("3D加速不支持.");
        }
        StageUtil.setPrimaryStage(primaryStage);
    }

    @Override
    public void stop() {
        try {
            super.stop();
            StageUtil.closeWindows();
            System.exit(0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
