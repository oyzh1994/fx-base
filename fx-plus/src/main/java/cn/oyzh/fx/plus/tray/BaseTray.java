package cn.oyzh.fx.plus.tray;

import javafx.scene.Node;

import java.awt.TrayIcon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.function.Consumer;

/**
 * 基础系统托盘
 *
 * @author oyzh
 * @since 2025/0/19
 */
public abstract class BaseTray {

    public BaseTray(String iconUrl) {
        this.initIcon(iconUrl);
    }

    /**
     * 设置托盘图标
     *
     * @param url 图标地址
     * @return 结果
     */
    protected abstract boolean initIcon(String url);

    /**
     * 设置标题
     *
     * @param title 标题
     */
    public void setTitle(String title) {
    }

    /**
     * 添加菜单项
     *
     * @param label  菜单名称
     * @param action 菜单业务
     */
    public abstract void addMenuItem(String label, Runnable action);

    /**
     * 添加菜单项
     *
     * @param label  菜单名称
     * @param icon   菜单图标
     * @param action 菜单业务
     */
    public abstract void addMenuItem(String label, Node icon, Runnable action);

    /**
     * 添加菜单项
     *
     * @param trayItem 托盘菜单
     */
    public abstract void addMenuItem(BaseTrayItem trayItem);

    /**
     * 设置鼠标监听事件
     *
     * @param mouseListener 鼠标监听器
     */
    public abstract void setMouseListener(MouseListener mouseListener);

    /**
     * 鼠标点击事件
     *
     * @param eventHandler 事件处理器
     */
    public abstract void onMouseClicked(Consumer<MouseEvent> eventHandler);

    /**
     * 显示托盘
     */
    public abstract void show();

    /**
     * 关闭托盘
     */
    public abstract void close();

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
    public abstract void displayMessage(String caption, String text, TrayIcon.MessageType messageType);

    /**
     * 是否支持托盘
     *
     * @return 结果
     */
    public abstract boolean supported();
}
