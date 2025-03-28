package cn.oyzh.fx.plus.ext;

import cn.oyzh.common.SysConst;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageManager;
import com.sun.javafx.application.LauncherImpl;
import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.stage.Stage;

import java.util.Properties;

/**
 * fx 支持Spring启动的主入口
 *
 * @author oyzh
 * @since 2021/8/19
 */
public abstract class FXApplication extends Application {

    /**
     * 启动时间
     */
    protected final long startAt = System.currentTimeMillis();

    @Override
    public void init() throws Exception {
        // 设置stage全部关闭后不自动销毁进程
        Platform.setImplicitExit(false);
        // 禁用css日志
        FXUtil.disableCSSLogger();
        // 调用父类
        super.init();
        JulLog.info("{} init finish.", SysConst.projectName());
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // 设置主stage
            StageManager.setPrimaryStage(primaryStage);
            // 显示主窗口
            this.showMainView();
            // 启动耗时
            long startCost = System.currentTimeMillis() - this.startAt;
            JulLog.info("启动耗时:{}ms-------------------------------", startCost);
            // 内存消耗
            double usedMemory = SystemUtil.getUsedMemory();
            JulLog.info("内存消耗:{}mb-------------------------------", usedMemory);
            // 打印特性支持情况
            for (ConditionalFeature feature : ConditionalFeature.values()) {
                JulLog.info("{}={}", feature.name(), Platform.isSupported(feature) ? "支持" : "不支持");
            }
            // 初始化系统托盘
            this.initSystemTray();
            // 开启定期gc
            SystemUtil.gcInterval(60_000);
            JulLog.info("{} start.", SysConst.projectName());
        } catch (Exception ex) {
            JulLog.error("start fail", ex);
        }
    }

    /**
     * 显示主窗口
     */
    protected void showMainView() {

    }

    @Override
    public void stop() {
        try {
            // 运行时间
            long runAlive = System.currentTimeMillis() - this.startAt;
            JulLog.info("运行时间:{}ms-------------------------------", runAlive);
            super.stop();
            JulLog.info("{} stop.", SysConst.projectName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 初始化系统托盘
     */
    protected abstract void initSystemTray();

    /**
     * 启动
     *
     * @param appClass app类
     * @param args     参数
     */
    public static void launch( Class<? extends Application> appClass, String... args) {
        try {
            JulLog.info("appClass:{}", appClass.getName());
            if (args != null && args.length > 0) {
                JulLog.info("=============launch args start---------->");
                for (String arg : args) {
                    JulLog.info("launch arg={}", arg);
                }
                JulLog.info("=============launch args end---------->");
            }
            // 移除可选属性
            SystemUtil.removeOptionalProperties();
            Properties properties = System.getProperties();
            if (!properties.isEmpty()) {
                JulLog.info("=============System Properties start---------->");
                for (String key : properties.stringPropertyNames()) {
                    JulLog.info("System Property {}={}", key, System.getProperty(key));
                }
                JulLog.info("=============System Properties end---------->");
            }
            // 启动工程
            JulLog.info("=============launchApplication start---------->");
            if (Preloader.class.isAssignableFrom(appClass)) {
                Class<Preloader> preloaderClass = (Class<Preloader>) appClass;
                LauncherImpl.launchApplication(appClass, preloaderClass, args);
            } else {
                LauncherImpl.launchApplication(appClass, args);
            }
            JulLog.info("=============launchApplication finish---------->");
        } catch (Exception ex) {
            JulLog.error("launch fail", ex);
            ex.printStackTrace();
        }
    }

    // @Override
    // public void handleApplicationNotification(PreloaderNotification info) {
    //     super.handleApplicationNotification(info);
    // }
    //
    // @Override
    // public void handleStateChangeNotification(StateChangeNotification info) {
    //     super.handleStateChangeNotification(info);
    // }
    //
    // @Override
    // public void handleProgressNotification(ProgressNotification info) {
    //     super.handleProgressNotification(info);
    // }
    //
    // @Override
    // public boolean handleErrorNotification(ErrorNotification info) {
    //     Throwable throwable = info == null ? null : info.getCause();
    //     if (throwable != null) {
    //         throwable.printStackTrace();
    //         return false;
    //     }
    //     return super.handleErrorNotification(info);
    // }
}
