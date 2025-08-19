package cn.oyzh.fx.plus.tray;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.ResourceUtil;
import dorkbox.systemTray.SystemTray;
import dorkbox.util.Sys;
import javafx.scene.Node;

import javax.swing.JMenuItem;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.net.URL;
import java.util.function.Consumer;

/**
 * 系统托盘扩展
 *
 * @author oyzh
 * @since 2022/8/24
 */
public class Tray2 {

    private SystemTray systemTray;

    public Tray2(String iconUrl) {
        this.initIcon(ResourceUtil.getPath(iconUrl));
    }

    /**
     * 设置托盘图标
     *
     * @param url 图标地址
     * @return 结果
     */
    private boolean initIcon(String url) {
        try {
            // 系统托盘图标
            this.systemTray  = SystemTray.get("SysTrayExample");
            // this.systemTray.setEnabled(true);
            this.systemTray.setImage(new File(url));
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
        this.systemTray.setTooltip(title);
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
        if (icon == null) {
            this.addMenuItem(new TrayItem2(label, action));
        } else {
            this.addMenuItem(new TrayItem2(label, icon, action));
        }
    }

    /**
     * 添加菜单项
     *
     * @param trayItem 托盘菜单
     */
    public void addMenuItem(TrayItem2 trayItem) {
        this.systemTray.getMenu().add(trayItem);
    }

    /**
     * 设置鼠标监听事件
     *
     * @param mouseListener 鼠标监听器
     */
    public void setMouseListener(MouseListener mouseListener) {
    }

    /**
     * 鼠标点击事件
     *
     * @param eventHandler 事件处理器
     */
    public void onMouseClicked(Consumer<MouseEvent> eventHandler) {
    }

    /**
     * 显示托盘
     */
    public void show() {
        try {
            // this.systemTray.setEnabled(true);
            // this.systemTray.getMenu().asSwingComponent().setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 关闭托盘
     */
    public void close() {
        try {
            this.systemTray.shutdown();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

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
    }
}
