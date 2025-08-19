package cn.oyzh.fx.plus.tray;

import cn.oyzh.common.util.ResourceUtil;
import dorkbox.systemTray.SystemTray;
import javafx.scene.Node;

import java.awt.TrayIcon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.function.Consumer;

/**
 * dorkbox托盘
 *
 * @author oyzh
 * @since 2025/08/19
 */
public class DorkboxTray extends BaseTray {

    private SystemTray systemTray;

    public DorkboxTray(String iconUrl) {
        super(iconUrl);
    }

    @Override
    protected boolean initIcon(String url) {
        try {
            // 系统托盘图标
            this.systemTray = SystemTray.get();
            this.systemTray.setImage(new File(url));
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public void setTitle(String title) {
        this.systemTray.setTooltip(title);
    }

    @Override
    public void addMenuItem(String label, Runnable action) {
        this.addMenuItem(label, null, action);
    }

    @Override
    public void addMenuItem(String label, Node icon, Runnable action) {
        if (icon == null) {
            this.addMenuItem(new DorkboxTrayItem(label, action));
        } else {
            this.addMenuItem(new DorkboxTrayItem(label, icon, action));
        }
    }

    @Override
    public void addMenuItem(BaseTrayItem trayItem) {
        if (trayItem instanceof DorkboxTrayItem item) {
            this.systemTray.getMenu().add(item);
        } else if (trayItem != null) {
            this.systemTray.getMenu().add(trayItem.toDorkboxTrayItem());
        }
    }

    @Override
    public void setMouseListener(MouseListener mouseListener) {
    }

    @Override
    public void onMouseClicked(Consumer<MouseEvent> eventHandler) {
    }

    @Override
    public void show() {
    }

    @Override
    public void close() {
        try {
            this.systemTray.shutdown();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void displayMessage(String caption, String text, TrayIcon.MessageType messageType) {
    }

    @Override
    public boolean supported() {
        return this.systemTray != null;
    }
}
