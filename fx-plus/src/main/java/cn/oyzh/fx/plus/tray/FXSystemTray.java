package cn.oyzh.fx.plus.tray;

import cn.hutool.core.util.ArrayUtil;
import cn.oyzh.fx.plus.util.ResourceUtil;
import javafx.scene.Node;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.function.Consumer;

/**
 * 系统托盘
 *
 * @author oyzh
 * @since 2022/8/24
 */
@Slf4j
public class FXSystemTray {

    /**
     * 系统托盘
     */
    private SystemTray tray;

    /**
     * 托盘图标
     */
    //private TrayIconExt trayIcon;
    private FXTrayIcon trayIcon;

    /**
     * icon鼠标事件
     */
    private FXTrayMouseListener trayMouseListener;

    public FXSystemTray(@NonNull URL iconUrl) {
        this.initTray();
        this.initIcon(iconUrl);
    }

    public FXSystemTray(@NonNull String iconUrl) {
        this.initTray();
        this.initIcon(iconUrl);
    }

    /**
     * 初始化托盘
     */
    private void initTray() {
        if (!SystemTray.isSupported()) {
            log.warn("SystemTray is not supported.");
            throw new RuntimeException("SystemTray is not supported.");
        }
        System.setProperty("java.awt.headless", "false");
        if (this.tray == null) {
            this.tray = SystemTray.getSystemTray();
        }
    }

    /**
     * 设置托盘图标
     *
     * @param iconPath 图标地址
     * @return 结果
     */
    private boolean initIcon(@NonNull String iconPath) {
        // 创建新系统托盘图标
        URL url = ResourceUtil.getResource(iconPath);
        if (url == null) {
            log.error("iconPath: {} is invalid.", iconPath);
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
    private boolean initIcon(@NonNull URL url) {
        try {
            Image image = Toolkit.getDefaultToolkit().getImage(url);
            // 系统托盘图标
            //this.trayIcon = new TrayIconExt(image);
            this.trayIcon = new FXTrayIcon(image);
            // 设置图标尺寸自动适应
            this.trayIcon.setImageAutoSize(true);
            // 设置鼠标事件
            this.trayMouseListener = new FXTrayMouseListener();
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
    public void setTitle(@NonNull String title) {
        this.trayIcon.setToolTip(title);
    }

    /**
     * 添加菜单项
     *
     * @param label  菜单名称
     * @param action 菜单业务
     */
    public void addMenuItem(@NonNull String label, Runnable action) {
        this.addMenuItem(label, null, action);
    }

    /**
     * 添加菜单项
     *
     * @param label  菜单名称
     * @param icon   菜单图标
     * @param action 菜单业务
     */
    public void addMenuItem(@NonNull String label, Node icon, Runnable action) {
        this.trayIcon.getMenu().addItem(new FXTrayItem(label, icon, action));
    }

    /**
     * 设置鼠标监听事件
     *
     * @param mouseListener 鼠标监听器
     */
    public void setMouseListener(@NonNull MouseListener mouseListener) {
        this.trayMouseListener.setMouseListener(mouseListener);
    }

    /**
     * 鼠标点击事件
     *
     * @param eventHandler 事件处理器
     */
    public void onMouseClicked(@NonNull Consumer<MouseEvent> eventHandler) {
        this.trayMouseListener.setMouseClicked(eventHandler);
    }

    /**
     * 显示托盘
     */
    public void show() {
        try {
            // 添加到托盘
            if (ArrayUtil.isEmpty(this.tray.getTrayIcons())) {
                this.tray.remove(this.trayIcon);
                this.tray.add(this.trayIcon);
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
            if (ArrayUtil.isNotEmpty(this.tray.getTrayIcons())) {
                this.tray.remove(this.trayIcon);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
