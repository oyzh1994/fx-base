package cn.oyzh.fx.plus.controls.tree.table;

import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.flex.FlexUtil;
import cn.oyzh.fx.plus.node.NodeUtil;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeTableColumn;
import lombok.ToString;

/**
 * 树形结构
 *
 * @author oyzh
 * @since 2022/1/19
 */
@ToString
public class FlexTreeTableView extends FXTreeTableView implements FlexAdapter {

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }

    @Override
    public void resizeNode(Double width, Double height) {
        // 调用父类的resizeNode方法来调整节点大小
        FlexAdapter.super.resizeNode(width, height);
        // 获取表格中的列
        ObservableList<? extends TreeTableColumn<?, ?>> columns = this.getColumns();
        // 遍历每一列
        for (TreeTableColumn<?, ?> column : columns) {
            // 判断列是否是FlexAdapter的实例
            if (column instanceof FlexAdapter flexNode) {
                // 如果列可见，则设置实际宽度为计算得到的弹性宽度
                if (column.isVisible()) {
                    flexNode.setRealWidth(FlexUtil.compute(flexNode.getFlexWidth(), width));
                } else {
                    // 否则将列宽度设置为0
                    NodeUtil.setWidth(column, 0D);
                }
            }
        }
    }
}
