package cn.oyzh.fx.plus.tray;

import cn.oyzh.fx.plus.controls.box.FXVBox;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import lombok.NonNull;

/**
 * 托盘菜单
 *
 * @author oyzh
 * @since 2023/3/2
 */
public class TrayMenu extends FXVBox {

    {
        // 初始化样式
        this.initStyle();
    }

    /**
     * 添加菜单项
     *
     * @param menuItem 菜单项
     */
    public void addItem(@NonNull TrayItem menuItem) {
        // 添加节点
        this.getChildren().add(menuItem);
        // 设置边距
        VBox.setMargin(menuItem, new Insets(3));
        // 计算高度
        double h = this.getChildren().size() * menuItem.getHeight() + 5;
        // 计算宽度
        double w = 0;
        for (Node child : this.getChildren()) {
            if (child instanceof TrayItem item && item.getRealWidth() > w) {
                w = item.getRealWidth();
            }
        }
        w += 30;
        // 重新设置宽高
        this.setPrefWidth(w);
        this.setPrefHeight(h);
    }

    /**
     * 初始化样式
     */
    private void initStyle() {
        this.setStyle(this.getStyle() +
                "-fx-border-style: solid;" +
                "-fx-border-width: 1;" +
                "-fx-padding: 5 10 5 10;" +
                "-fx-border-radius: 3;"
        );
    }
}
