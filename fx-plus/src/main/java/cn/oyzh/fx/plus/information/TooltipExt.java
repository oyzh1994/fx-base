package cn.oyzh.fx.plus.information;

import cn.oyzh.fx.plus.adapter.PropAdapter;
import cn.oyzh.fx.plus.util.ControlUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.font.FontUtil;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import lombok.NonNull;

/**
 * 提示条扩展
 *
 * @author oyzh
 * @since 2023/10/24
 */
public class TooltipExt extends Tooltip implements PropAdapter {

    @Override
    public void hide() {
        FXUtil.runWait(super::hide);
    }

    /**
     * 在节点上显示
     *
     * @param node 节点
     */
    public void show(@NonNull Node node) {
        // 获取组件坐标
        Point2D point2D = node.localToScreen(node.getScaleX(), node.getScaleY());
        // 获取组件宽度
        double width = ControlUtil.boundedWidth(node);
        // 获取内容宽度
        double strWidth = FontUtil.stringWidth(this.getText(), this.getFont());
        // 显示提示条
        this.show(node, point2D.getX() + width - strWidth - 10, point2D.getY());
    }
}
