package cn.oyzh.fx.plus.tray;

import cn.hutool.log.StaticLog;
import cn.oyzh.fx.common.thread.TaskManager;
import javafx.scene.Node;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * 系统托盘管理器
 *
 * @author oyzh
 * @since 2023/12/21
 */
@UtilityClass
public class TrayManager {

    /**
     * 系统托盘对象，一般程序生命周期内就存在一个
     */
    private static Tray tray;

    /**
     * 初始化
     *
     * @param url 图标地址
     * @return 托盘
     */
    public static Tray init(String url) {
        if (tray == null) {
            TrayManager.tray = new Tray(url);
        }
        return tray;
    }

    /**
     * 显示系统托盘
     */
    public static void show() {
        if (tray != null) {
            TaskManager.startTimeout(tray::show, 100);
        }
    }

    /**
     * 销毁系统托盘
     */
    public static void destroy() {
        if (tray != null) {
            TaskManager.startTimeout(tray::close, 100);
            tray = null;
        }
    }

    /**
     * 设置系统托盘标题
     *
     * @param title 标题
     */
    public static void setTitle(@NonNull String title) {
        if (tray != null) {
            tray.setTitle(title);
        }
    }

    /**
     * 系统托盘鼠标点击事件
     *
     * @param eventHandler 事件处理器
     */
    public static void onMouseClicked(@NonNull Consumer<MouseEvent> eventHandler) {
        if (tray != null) {
            tray.onMouseClicked(eventHandler);
        }
    }

    /**
     * 添加系统托盘菜单项
     *
     * @param label  菜单名称
     * @param action 菜单业务
     */
    public static void addMenuItem(@NonNull String label, Runnable action) {
        if (tray != null) {
            tray.addMenuItem(label, null, action);
        }
    }

    /**
     * 添加系统托盘菜单项
     *
     * @param label  菜单名称
     * @param icon   菜单图标
     * @param action 菜单业务
     */
    public static void addMenuItem(@NonNull String label, Node icon, Runnable action) {
        if (tray != null) {
            tray.addMenuItem(label, icon, action);
        }
    }

    /**
     * 获取系统托盘扩展
     *
     * @return 系统托盘扩展
     */
    public static Tray tray() {
        return tray;
    }

    /**
     * 获取系统托盘
     *
     * @return 系统托盘
     */
    public static SystemTray systemTray() {
        if (!SystemTray.isSupported()) {
            StaticLog.warn("SystemTray is not supported.");
            throw new RuntimeException("SystemTray is not supported.");
        }
        System.setProperty("java.awt.headless", "false");
        return SystemTray.getSystemTray();
    }

    /**
     * 是否存在托盘
     *
     * @return 结果
     */
    public static boolean exist() {
        return tray != null;
    }

    /**
     * 是否支持托盘
     *
     * @return 结果
     */
    public static boolean supported() {
        return SystemTray.isSupported();
    }
}
