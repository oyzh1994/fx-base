package cn.oyzh.fx.plus.tray;

import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.theme.ThemeManager;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import lombok.NonNull;

/**
 * 托盘菜单
 *
 * @author oyzh
 * @since 2023/3/2
 */
public class TrayMenu extends FXVBox {

    {
        // // 初始化样式
        // this.initStyle();
        // 内边距
        this.setPadding(new Insets(5, 10, 5, 10));
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
    }

    /**
     * 初始化
     */
    public void init() {
        this.initSize();
        this.initBorder();
    }

    /**
     * 初始化大小
     */
    protected void initSize() {
        TrayItem trayItem = (TrayItem) this.getChild(0);
        // 计算高度
        double h = this.getChildren().size() * trayItem.realHeight() + 5;
        // 计算宽度
        double w = 0;
        for (Node child : this.getChildren()) {
            if (child instanceof TrayItem item) {
                double w1 = item.getRealWidth();
                if (w1 > w) {
                    w = w1;
                }
            }
        }
        w += 35;
        // 重新设置宽高
        this.setRealWidth(w);
        this.setRealHeight(h);
    }

    /**
     * 初始化边框
     */
    protected void initBorder() {
        // this.setStyle(this.getStyle() +
        //         "-fx-border-style: solid;" +
        //         "-fx-border-width: 1;" +
        //         "-fx-padding: 5 10 5 10;" +
        //         "-fx-border-radius: 3;"
        // );
        // 边框
        Color color = ThemeManager.currentAccentColor();
        CornerRadii radii = new CornerRadii(3);
        BorderStroke stroke = new BorderStroke(color, BorderStrokeStyle.SOLID, radii, new BorderWidths(1));
        Border border = new Border(stroke);
        this.setBorder(border);
    }
}
