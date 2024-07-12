package cn.oyzh.fx.plus.tray;

import cn.oyzh.fx.plus.window.StageExt;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.scene.Scene;
import javafx.scene.robot.Robot;
import javafx.stage.StageStyle;
import lombok.Setter;

import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * 托盘图标
 *
 * @author oyzh
 * @since 2023/3/2
 */
public class TrayImage extends TrayIcon implements ThemeAdapter {

    /**
     * 窗口
     */
    private StageExt stage;

    /**
     * 菜单
     */
    @Setter
    private TrayMenu menu;

    public TrayImage(Image image) {
        super(image);
    }

    /**
     * 获取菜单
     *
     * @return 菜单
     */
    public TrayMenu getMenu() {
        if (this.menu == null) {
            this.menu = new TrayMenu();
            // 监听鼠标
            TrayMouseListener listener = new TrayMouseListener();
            listener.setMouseClicked(e -> {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    FXUtil.runLater(this::showMenu);
                }
            });
            this.addMouseListener(listener);
        }
        return this.menu;
    }

    /**
     * 显示菜单
     */
    private void showMenu() {
        // 初始化窗口
        if (this.stage == null) {
            this.stage = StageManager.newStage(null);
            this.stage.setScene(new Scene(this.menu));
            this.stage.setWidth(this.menu.getPrefWidth());
            this.stage.setHeight(this.menu.getPrefHeight());
            this.stage.setMaximized(false);
            this.stage.setResizable(false);
            this.stage.initStyle(StageStyle.TRANSPARENT);
            // 窗口失焦时，隐藏窗口
            this.stage.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    this.stage.setAlwaysOnTop(false);
                    this.stage.hide();
                }
            });
            // 隐藏窗口的任务栏图标
            StageManager.hideTaskbar(this.stage);
        }
        // 显示窗口
        if (!this.stage.isShowing()) {
            Robot robot = FXUtil.getRobot();
            this.stage.setX(robot.getMouseX() - 5);
            this.stage.setY(robot.getMouseY() - this.stage.getHeight() - 15);
            this.stage.setAlwaysOnTop(true);
            this.menu.changeTheme(ThemeManager.currentTheme());
            this.stage.show();
        }
    }

    @Override
    public void changeTheme(ThemeStyle style) {
        ThemeAdapter.super.changeTheme(style);
        if (this.menu != null) {
            this.menu.changeTheme(style);
        }
    }
}
