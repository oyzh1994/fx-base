package cn.oyzh.fx.plus.tray;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
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
public class Tray extends BaseTray {

    /**
     * 托盘图标
     */
    private TrayImage trayIcon;

    /**
     * icon鼠标事件
     */
    private TrayMouseListener trayMouseListener;

    public Tray(String iconUrl) {
        super(iconUrl);
    }

    @Override
    protected boolean initIcon(String iconPath) {
        // 创建新系统托盘图标
        URL url = ResourceUtil.getResource(iconPath);
        if (url == null) {
            JulLog.error("iconPath: {} is invalid.", iconPath);
            return false;
        }
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

    @Override
    public void setTitle(String title) {
        this.trayIcon.setToolTip(title);
    }

    @Override
    public void addMenuItem(String label, Runnable action) {
        this.addMenuItem(label, null, action);
    }

    @Override
    public void addMenuItem(String label, Node icon, Runnable action) {
        this.addMenuItem(new TrayItem(label, icon, action));
    }

    @Override
    public void addMenuItem(BaseTrayItem trayItem) {
        if (trayItem instanceof TrayItem item) {
            this.trayIcon.getMenu().addItem(item);
        }
    }

    @Override
    public void setMouseListener(MouseListener mouseListener) {
        this.trayMouseListener.setMouseListener(mouseListener);
    }

    @Override
    public void onMouseClicked(Consumer<MouseEvent> eventHandler) {
        this.trayMouseListener.setMouseClicked(eventHandler);
    }

    @Override
    public void show() {
        try {
            if (SystemTray.isSupported() && ArrayUtil.isEmpty( SystemTray.getSystemTray().getTrayIcons())) {
                SystemTray.getSystemTray().add(this.trayIcon);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            if (SystemTray.isSupported() && ArrayUtil.isNotEmpty( SystemTray.getSystemTray().getTrayIcons())) {
                 SystemTray.getSystemTray().remove(this.trayIcon);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void displayMessage(String caption, String text, TrayIcon.MessageType messageType) {
        this.trayIcon.displayMessage(caption, text, messageType);
    }

    @Override
    public boolean supported() {
        return SystemTray.isSupported();
    }
}
