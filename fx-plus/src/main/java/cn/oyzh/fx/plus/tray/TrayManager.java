package cn.oyzh.fx.plus.tray;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.thread.TaskManager;
import javafx.scene.Node;

import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * 系统托盘管理器
 *
 * @author oyzh
 * @since 2023/12/21
 */
public class TrayManager {

    /**
     * 系统托盘对象，一般程序生命周期内就存在一个
     */
    private static BaseTray tray;

    /**
     * 初始化
     *
     * @param icon 图标地址
     * @return 托盘
     */
    public static BaseTray init(String icon) {
        try {
            if (tray == null) {
                if (OSUtil.isLinux()) {
                    synchronized (TrayManager.class) {
                        TrayManager.tray = new DorkboxTray(icon);
                    }
                } else {
                    synchronized (TrayManager.class) {
                        TrayManager.tray = new Tray(icon);
                    }
                }
            }
            return tray;
        } catch (Exception ex) {
            ex.printStackTrace();
            JulLog.error("init tray error, err:{}", ex.getMessage());
        }
        return null;
    }

    /**
     * 显示系统托盘
     */
    public static void show() {
        if (tray != null) {
            tray.show();
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
    public static void setTitle(String title) {
        if (tray != null) {
            tray.setTitle(title);
        }
    }

    /**
     * 系统托盘鼠标点击事件
     *
     * @param eventHandler 事件处理器
     */
    public static void onMouseClicked(Consumer<MouseEvent> eventHandler) {
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
    public static void addMenuItem(String label, Runnable action) {
        if (tray != null) {
            tray.addMenuItem(label, action);
        }
    }

    /**
     * 添加系统托盘菜单项
     *
     * @param label  菜单名称
     * @param icon   菜单图标
     * @param action 菜单业务
     */
    public static void addMenuItem(String label, Node icon, Runnable action) {
        if (tray != null) {
            tray.addMenuItem(label, icon, action);
        }
    }

    /**
     * 添加系统托盘菜单项
     *
     * @param trayItem 托盘菜单
     */
    public static void addMenuItem(BaseTrayItem trayItem) {
        if (tray != null) {
            tray.addMenuItem(trayItem);
        }
    }

    /**
     * 显示消息
     *
     * @param caption     标题
     * @param text        内容
     * @param messageType 类型
     */
    public static void displayMessage(String caption, String text, TrayIcon.MessageType messageType) {
        if (tray != null) {
            tray.displayMessage(caption, text, messageType);
        }
    }

    /**
     * 显示正常消息
     *
     * @param caption 标题
     * @param text    内容
     */
    public static void displayInfoMessage(String caption, String text) {
        if (tray != null) {
            tray.displayInfoMessage(caption, text);
        }
    }

    /**
     * 显示警告消息
     *
     * @param caption 标题
     * @param text    内容
     */
    public static void displayWarnMessage(String caption, String text) {
        if (tray != null) {
            tray.displayWarnMessage(caption, text);
        }
    }

    /**
     * 显示错误消息
     *
     * @param caption 标题
     * @param text    内容
     */
    public static void displayErrorMessage(String caption, String text) {
        if (tray != null) {
            tray.displayErrorMessage(caption, text);
        }
    }

    /**
     * 获取系统托盘扩展
     *
     * @return 系统托盘扩展
     */
    public static BaseTray tray() {
        return tray;
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
        if (!OSUtil.isLinux()) {
            return SystemTray.isSupported();
        }
        return true;
    }
}
