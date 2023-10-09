package cn.oyzh.fx.plus.tray;

import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.util.FontUtil;
import cn.oyzh.fx.plus.util.MouseUtil;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import lombok.NonNull;

/**
 * 托盘菜单项
 *
 * @author oyzh
 * @since 2023/3/2
 */
public class FXTrayItem extends Label {

    {
        this.setHeight(25);
        this.setCursor(Cursor.HAND);
    }

    public FXTrayItem(@NonNull String label, Runnable action) {
        this(label, null, action);
    }

    public FXTrayItem(@NonNull String label, Node icon, Runnable action) {
        super(label);
        if (icon != null) {
            this.setGraphic(icon);
        }
        if (action != null) {
            this.addActionHandler(e -> FXUtil.runLater(action));
        }
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

    /**
     * 获取真实宽度
     *
     * @return 真实宽度
     */
    public double getRealWidth() {
        double tLen = FontUtil.stringWidth(this.getText());
        if (this.getGraphic() != null) {
            return this.getGraphic().maxWidth(-1) + 3 + tLen;
        }
        return tLen;
    }
}
