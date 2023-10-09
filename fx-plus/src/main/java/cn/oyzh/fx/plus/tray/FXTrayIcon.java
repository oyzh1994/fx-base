package cn.oyzh.fx.plus.tray;

import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.view.FXViewUtil;
import javafx.scene.Scene;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;
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
public class FXTrayIcon extends TrayIcon {

    /**
     * 窗口
     */
    private Stage stage;

    /**
     * 菜单
     */
    @Setter
    private FXTrayMenu menu;

    public FXTrayIcon(Image image) {
        super(image);
    }

    /**
     * 获取菜单
     *
     * @return 菜单
     */
    public FXTrayMenu getMenu() {
        if (this.menu == null) {
            this.menu = new FXTrayMenu();
            // 监听鼠标
            FXTrayMouseListener listener = new FXTrayMouseListener();
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
            this.stage = new Stage();
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
            FXViewUtil.hideTaskbar(this.stage);
        }
        // 显示窗口
        if (!this.stage.isShowing()) {
            Robot robot = FXUtil.getRobot();
            this.stage.setX(robot.getMouseX() - 5);
            this.stage.setY(robot.getMouseY() - this.stage.getHeight() - 15);
            this.stage.setAlwaysOnTop(true);
            this.stage.show();
        }
    }
}
