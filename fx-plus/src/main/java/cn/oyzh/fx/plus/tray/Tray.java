package cn.oyzh.fx.plus.tray;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.log.StaticLog;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import cn.oyzh.fx.plus.util.ResourceUtil;
import javafx.scene.Node;
import lombok.NonNull;

import java.awt.*;
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
public class Tray implements ThemeAdapter {

    /**
     * 托盘图标
     */
    private TrayImage trayIcon;

    /**
     * icon鼠标事件
     */
    private TrayMouseListener trayMouseListener;

    public Tray(@NonNull URL iconUrl) {
        this.initIcon(iconUrl);
    }

    public Tray(@NonNull String iconUrl) {
        this.initIcon(iconUrl);
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
            StaticLog.error("iconPath: {} is invalid.", iconPath);
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
        this.trayIcon.getMenu().addItem(new TrayItem(label, icon, action));
    }

    /**
     * 添加菜单项
     *
     * @param trayItem 托盘菜单
     */
    public void addMenuItem(@NonNull TrayItem trayItem) {
        this.trayIcon.getMenu().addItem(trayItem);
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
            if (SystemTray.isSupported()) {
                // 添加到托盘
                if (ArrayUtil.isEmpty(TrayManager.systemTray().getTrayIcons())) {
                    TrayManager.systemTray().remove(this.trayIcon);
                    TrayManager.systemTray().add(this.trayIcon);
                }
                this.trayIcon.changeTheme(ThemeManager.currentTheme());
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
            if (SystemTray.isSupported()) {
                if (ArrayUtil.isNotEmpty(TrayManager.systemTray().getTrayIcons())) {
                    TrayManager.systemTray().remove(this.trayIcon);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void changeTheme(ThemeStyle style) {
        ThemeAdapter.super.changeTheme(style);
        if (this.trayIcon != null) {
            this.trayIcon.changeTheme(style);
        }
    }
}
