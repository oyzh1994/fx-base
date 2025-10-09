package cn.oyzh.fx.plus.information;

import cn.oyzh.fx.plus.adapter.PropAdapter;
import cn.oyzh.fx.plus.font.FontUtil;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;

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
    public void show( Node node) {
        // 获取组件坐标
        Point2D point2D = node.localToScreen(node.getScaleX(), node.getScaleY());
        // 获取组件宽度
        double width = NodeUtil.getWidth(node);
        // double width = ControlUtil.boundedWidth(node);
        // 获取内容宽度
        double strWidth = FontUtil.stringWidth(this.getText(), this.getFont());
        // 显示提示条
        this.show(node, point2D.getX() + width - strWidth - 10, point2D.getY());
    }
}
