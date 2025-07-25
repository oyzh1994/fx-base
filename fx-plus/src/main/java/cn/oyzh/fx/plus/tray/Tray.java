package cn.oyzh.fx.plus.tray;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.ResourceUtil;
import javafx.scene.Node;

import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.function.Consumer;

/**
 * 系统托盘扩展
 *
 * @author oyzh
 * @since 2022/8/24
 */
public class Tray {

    /**
     * 托盘图标
     */
    private TrayImage trayIcon;

    /**
     * icon鼠标事件
     */
    private TrayMouseListener trayMouseListener;

    public Tray(URL iconUrl) {
        this.initIcon(iconUrl);
    }

    public Tray(String iconUrl) {
        this.initIcon(iconUrl);
    }

    /**
     * 设置托盘图标
     *
     * @param iconPath 图标地址
     * @return 结果
     */
    private boolean initIcon(String iconPath) {
        // 创建新系统托盘图标
        URL url = ResourceUtil.getResource(iconPath);
        if (url == null) {
            JulLog.error("iconPath: {} is invalid.", iconPath);
            return false;
        }
        return this.initIcon(url);
    }

    /**
     * 设置托盘图标
     *
     * @param url 图标地址
     * @return 结果
     */
    private boolean initIcon(URL url) {
        try {
            Image image = Toolkit.getDefaultToolkit().getImage(url);
            // 系统托盘图标
            this.trayIcon = new TrayImage(image);
            // 设置图标尺寸自动适应
            this.trayIcon.setImageAutoSize(true);
            // 设置鼠标事件
            this.trayMouseListener = new TrayMouseListener();
            this.trayIcon.addMouseListener(this.trayMouseListener);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 设置标题
     *
     * @param title 标题
     */
    public void setTitle(String title) {
        this.trayIcon.setToolTip(title);
    }

    /**
     * 添加菜单项
     *
     * @param label  菜单名称
     * @param action 菜单业务
     */
    public void addMenuItem(String label, Runnable action) {
        this.addMenuItem(label, null, action);
    }

    /**
     * 添加菜单项
     *
     * @param label  菜单名称
     * @param icon   菜单图标
     * @param action 菜单业务
     */
    public void addMenuItem(String label, Node icon, Runnable action) {
        this.addMenuItem(new TrayItem(label, icon, action));
    }

    /**
     * 添加菜单项
     *
     * @param trayItem 托盘菜单
     */
    public void addMenuItem(TrayItem trayItem) {
        this.trayIcon.getMenu().addItem(trayItem);
    }

    /**
     * 设置鼠标监听事件
     *
     * @param mouseListener 鼠标监听器
     */
    public void setMouseListener(MouseListener mouseListener) {
        this.trayMouseListener.setMouseListener(mouseListener);
    }

    /**
     * 鼠标点击事件
     *
     * @param eventHandler 事件处理器
     */
    public void onMouseClicked(Consumer<MouseEvent> eventHandler) {
        this.trayMouseListener.setMouseClicked(eventHandler);
    }

    /**
     * 显示托盘
     */
    public void show() {
        try {
            if (SystemTray.isSupported() && ArrayUtil.isEmpty(TrayManager.systemTray().getTrayIcons())) {
                // TrayManager.systemTray().remove(this.trayIcon);
                TrayManager.systemTray().add(this.trayIcon);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 关闭托盘
     */
    public void close() {
        try {
            if (SystemTray.isSupported() && ArrayUtil.isNotEmpty(TrayManager.systemTray().getTrayIcons())) {
                TrayManager.systemTray().remove(this.trayIcon);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // @Override
    // public void changeTheme(ThemeStyle style) {
    //     ThemeAdapter.super.changeTheme(style);
    //     if (this.trayIcon != null) {
    //         this.trayIcon.changeTheme(style);
    //     }
    // }

    /**
     * 显示正常消息
     *
     * @param caption 标题
     * @param text    内容
     */
    public void displayInfoMessage(String caption, String text) {
        this.displayMessage(caption, text, TrayIcon.MessageType.INFO);
    }

    /**
     * 显示警告消息
     *
     * @param caption 标题
     * @param text    内容
     */
    public void displayWarnMessage(String caption, String text) {
        this.displayMessage(caption, text, TrayIcon.MessageType.WARNING);
    }

    /**
     * 显示错误消息
     *
     * @param caption 标题
     * @param text    内容
     */
    public void displayErrorMessage(String caption, String text) {
        this.displayMessage(caption, text, TrayIcon.MessageType.ERROR);
    }

    /**
     * 显示消息
     *
     * @param caption     标题
     * @param text        内容
     * @param messageType 类型
     */
    public void displayMessage(String caption, String text, TrayIcon.MessageType messageType) {
        this.trayIcon.displayMessage(caption, text, messageType);
    }
}
