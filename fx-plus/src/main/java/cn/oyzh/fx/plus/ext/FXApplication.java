package cn.oyzh.fx.plus.ext;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.SystemUtil;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.fx.plus.window.WindowManager;
import com.sun.javafx.application.LauncherImpl;
import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.stage.Stage;
import lombok.NonNull;

import java.awt.*;
import java.util.Properties;

/**
 * fx 支持Spring启动的主入口
 *
 * @author oyzh
 * @since 2021/8/19
 */
public abstract class FXApplication extends Preloader {

    /**
     * 系统托盘
     */
    protected SystemTray systemTray;

    /**
     * 启动时间
     */
    protected final long startAt = System.currentTimeMillis();

    @Override
    public void start(Stage primaryStage) {
        // 设置stage全部关闭后不自动销毁进程
        Platform.setImplicitExit(false);
        // 设置主stage
        StageManager.setPrimaryStage(primaryStage);
        // 启动耗时
        long startCost = System.currentTimeMillis() - this.startAt;
        JulLog.info("启动耗时:{}ms-------------------------------", startCost);
        // 内存消耗
        double usedMemory = SystemUtil.getUsedMemory();
        JulLog.info("内存消耗:{}mb-------------------------------", usedMemory);
        // 启动结束以后业务
        this.afterStart();
    }

    /**
     * 启动完成之后的业务
     */
    protected void afterStart() {
        // 打印特性支持情况
        for (ConditionalFeature feature : ConditionalFeature.values()) {
            JulLog.info("{}={}", feature.name(), Platform.isSupported(feature) ? "支持" : "不支持");
        }
        // 初始化系统托盘
        this.systemTray = this.initSystemTray();
        // 开启定期gc
        SystemUtil.gcInterval(60_000);
    }

    @Override
    public void stop() {
        try {
            // 运行时间
            long runAlive = System.currentTimeMillis() - this.startAt;
            JulLog.info("运行时间:{}ms-------------------------------", runAlive);
            super.stop();
            WindowManager.closeWindows();
            System.exit(0);
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
        System.out.println(info.getProgress() + "------------------------------------");
    }

    /**
     * 初始化系统托盘
     */
    protected abstract SystemTray initSystemTray();

    /**
     * app图标
     *
     * @return app图标
     */
    protected abstract String appIcon();

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
            LauncherImpl.launchApplication(appClass, preloaderClass, args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
