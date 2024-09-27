package cn.oyzh.fx.plus.ext;

import cn.hutool.log.StaticLog;
import cn.oyzh.fx.common.log.JulLog;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.fx.plus.window.WindowManager;
import com.sun.javafx.application.LauncherImpl;
import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.stage.Stage;
import lombok.NonNull;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.Properties;

/**
 * fx 支持Spring启动的主入口
 *
 * @author oyzh
 * @since 2021/8/19
 */
public abstract class ApplicationExt extends Preloader {

    @Override
    public void start(Stage primaryStage) {
        if (Platform.isSupported(ConditionalFeature.SCENE3D)) {
            JulLog.info("3D加速已开启.");
        } else {
            JulLog.warn("3D加速不支持.");
        }
        StageManager.setPrimaryStage(primaryStage);

    }

    @Override
    public void stop() {
        try {
            super.stop();
            WindowManager.closeWindows();
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
            if (args != null && args.length > 0) {
                System.out.println("=============args start---------->");
                for (String arg : args) {
                    System.out.println("arg: " + arg);
                }
                System.out.println("=============args end---------->");
            }
            Properties properties = System.getProperties();
            if (!properties.isEmpty()) {
                System.out.println("=============props start---------->");
                for (String key : properties.stringPropertyNames()) {
                        System.out.println(key + "=" + System.getProperty(key));
                }
                System.out.println("=============props end---------->");
            }
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


    @Override
    public void handleStateChangeNotification(StateChangeNotification info) {
        super.handleStateChangeNotification(info);
    }

    @Override
    public void handleProgressNotification(ProgressNotification info) {
        super.handleProgressNotification(info);
        System.out.println(info.getProgress()+"------------------------------------");
    }
}
