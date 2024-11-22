package cn.oyzh.fx.plus.controls.tree;

import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.flex.FlexUtil;
import cn.oyzh.fx.plus.util.NodeUtil;
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
    public String getFlexWidth() {
        return FlexAdapter.super.flexWidth();
    }

    @Override
    public void setFlexWidth(String flexWidth) {
        FlexAdapter.super.flexWidth(flexWidth);
    }

    public String getFlexHeight() {
        return FlexAdapter.super.flexHeight();
    }

    @Override
    public void setFlexHeight(String flexHeight) {
        FlexAdapter.super.flexHeight(flexHeight);
    }

    @Override
    public String getFlexX() {
        return FlexAdapter.super.flexX();
    }

    @Override
    public void setFlexX(String flexX) {
        FlexAdapter.super.flexX(flexX);
    }

    @Override
    public String getFlexY() {
        return FlexAdapter.super.flexY();
    }

    @Override
    public void setFlexY(String flexY) {
        FlexAdapter.super.flexY(flexY);
    }

    @Override
    public double getRealWidth() {
        return FlexAdapter.super.realWidth();
    }

    @Override
    public void setRealWidth(double width) {
        FlexAdapter.super.realWidth(width);
    }

    @Override
    public double getRealHeight() {
        return FlexAdapter.super.realHeight();
    }

    @Override
    public void setRealHeight(double height) {
        FlexAdapter.super.realHeight(height);
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
