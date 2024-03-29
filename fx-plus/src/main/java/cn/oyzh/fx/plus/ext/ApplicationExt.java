package cn.oyzh.fx.plus.ext;

import cn.hutool.log.StaticLog;
import cn.oyzh.fx.plus.stage.StageUtil;
import com.sun.javafx.application.LauncherImpl;
import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.stage.Stage;
import lombok.NonNull;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * fx 支持Spring启动的主入口
 *
 * @author oyzh
 * @since 2021/8/19
 */
public abstract class ApplicationExt extends Application {

    @Override
    public void start(Stage primaryStage) {
        if (Platform.isSupported(ConditionalFeature.SCENE3D)) {
            StaticLog.info("3D加速已开启.");
        } else {
            StaticLog.warn("3D加速不支持.");
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

    /**
     * 启动
     *
     * @param appClass app类
     * @param args     参数
     */
    public static void launch(@NonNull Class<? extends Application> appClass, String... args) {
        try {
            LauncherImpl.launchApplication(appClass, ApplicationPreloader.class, args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 启动
     *
     * @param appClass       app类
     * @param preloaderClass 预加载类
     * @param args           参数
     */
    public static void launch(@NonNull Class<? extends Application> appClass, Class<? extends Preloader> preloaderClass, String... args) {
        try {
            SpringApplicationBuilder builder = new SpringApplicationBuilder(appClass);
            builder.web(WebApplicationType.NONE).headless(false).run(args);
            LauncherImpl.launchApplication(appClass, preloaderClass, args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
