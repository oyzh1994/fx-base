package cn.oyzh.fx.plus.tray;

import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.font.FontUtil;
import cn.oyzh.fx.plus.mouse.MouseUtil;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

/**
 * 托盘菜单项
 *
 * @author oyzh
 * @since 2023-03-02
 */
public class TrayItem extends FXLabel implements BaseTrayItem {

    {
        this.setHeight(25);
        this.setCursor(Cursor.HAND);
    }

    /**
     * 菜单业务
     */
    private final Runnable action;

    /**
     * 构造托盘菜单项
     *
     * @param label  菜单名称
     * @param action 菜单业务
     */
    public TrayItem(String label, Runnable action) {
        this(label, null, action);
    }

    /**
     * 构造托盘菜单项
     *
     * @param label  菜单名称
     * @param icon   菜单图标
     * @param action 菜单业务
     */
    public TrayItem(String label, Node icon, Runnable action) {
        super(label);
        if (icon != null) {
            this.setGraphic(icon);
        }
        if (action != null) {
            this.addActionHandler(e -> FXUtil.runLater(action));
        }
        this.action = action;
    }

    /**
     * 添加业务处理器
     *
     * @param handler 业务处理器
     */
    public void addActionHandler(EventHandler<MouseEvent> handler) {
        this.setOnMouseClicked(event -> {
            if (MouseUtil.isPrimaryButton(event) && MouseUtil.isSingleClick(event)) {
                handler.handle(event);
            }
        });
    }

    @Override
    public double getRealWidth() {
        double tLen = FontUtil.stringWidth(this.getText(), this.getFont());
        if (this.getGraphic() != null) {
            return NodeUtil.getWidth(this.getGraphic()) + 8 + tLen;
        }
        return tLen;
    }

    //@Override
    //public DorkboxTrayItem toDorkboxTrayItem() {
    //    return new DorkboxTrayItem(this.getText(), this.action);
    //}
}
