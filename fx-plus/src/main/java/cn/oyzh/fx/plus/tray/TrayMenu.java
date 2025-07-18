package cn.oyzh.fx.plus.tray;

import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import cn.oyzh.fx.plus.util.ControlUtil;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

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
    public void addItem(TrayItem menuItem) {
        // 添加节点
        this.getChildren().add(menuItem);
        // 初始化一次大小
        this.initSize();
        // 设置边距
        VBox.setMargin(menuItem, new Insets(3));
    }

    /**
     * 初始化
     */
    public void init() {
        this.initSize();
        this.initBorder();
        this.initBackground();
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
        this.setPrefWidth(w);
        this.setPrefHeight(h);
    }

    /**
     * 初始化边框
     */
    protected void initBorder() {
        // 边框
        Color color = ThemeManager.currentForegroundColor();
        CornerRadii radii = new CornerRadii(3);
        BorderStroke stroke = new BorderStroke(color, BorderStrokeStyle.SOLID, radii, ControlUtil.BW_HALF);
        Border border = new Border(stroke);
        this.setBorder(border);
    }

    /**
     * 初始化背景
     */
    protected void initBackground() {
        Border border = this.getBorder();
        CornerRadii radii = null;
        if (border != null && border.getStrokes() != null && !border.getStrokes().isEmpty()) {
            radii = border.getStrokes().getFirst().getRadii();
        }
        if (radii == null) {
            radii = new CornerRadii(3);
        }
        Color color = ThemeManager.currentBackgroundColor();
        // 背景
        Background background = new Background(new BackgroundFill(color, radii, Insets.EMPTY));
        this.setBackground(background);
    }

    @Override
    public void changeTheme(ThemeStyle style) {
        for (Node child : this.getChildren()) {
            if (child instanceof ThemeAdapter adapter) {
                adapter.changeTheme(style);
            }
        }
    }
}
