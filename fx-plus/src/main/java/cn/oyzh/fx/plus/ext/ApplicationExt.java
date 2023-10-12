package cn.oyzh.fx.plus.ext;

import cn.oyzh.fx.plus.stage.StageUtil;
import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

/**
 * fx 支持Spring启动的主入口
 *
 * @author oyzh
 * @since 2021/8/19
 */
@Slf4j
public abstract class ApplicationExt extends Application {

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
