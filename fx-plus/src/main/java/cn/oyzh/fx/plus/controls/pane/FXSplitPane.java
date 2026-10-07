package cn.oyzh.fx.plus.controls.pane;

import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.collections.ListChangeListener;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Region;

import java.util.List;

/**
 * 分割面板控件
 *
 * @author oyzh
 * @since 2023-10-09
 */
public class FXSplitPane extends SplitPane implements FlexAdapter, NodeAdapter, NodeGroup, TipAdapter, StateAdapter, FontAdapter, ThemeAdapter {

    {
        NodeManager.init(this);
    }

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        for (Node child : this.getChildren()) {
            if (child.getClass().getName().endsWith("Content") && child instanceof Region region) {
                if (this.getOrientation() == Orientation.VERTICAL) {
                    region.resize(this.getWidth(), region.getHeight());
                } else {
                    region.resize(region.getWidth(), this.getHeight());
                }
            }
        }
    }

    /**
     * 获取第一个分割条位置
     *
     * @return 第一个分割条位置，未记录时返回 null
     */
    public Double getPosition0() {
        return this.getProp("position_0");
    }

    /**
     * 获取第一个分割条位置，未记录时返回默认值
     *
     * @param defaultVal 默认值
     * @return 第一个分割条位置
     */
    public double getPosition0(double defaultVal) {
        Double v1 = this.getPosition0();
        return v1 == null ? defaultVal : v1;
    }

    /**
     * 记录第一个分割条位置
     */
    public void recordPosition0() {
        double[] positions = this.getDividerPositions();
        if (positions != null && positions.length >= 1) {
            this.setProp("position_0", positions[0]);
        } else {
            this.removeProp("position_0");
        }
    }

    /**
     * 是否显示分割条
     */
    private boolean showDivider = true;

    /**
     * 设置是否显示分割条
     *
     * @param showDivider 是否显示
     */
    public void setShowDivider(boolean showDivider) {
        this.showDivider = showDivider;
        for (Node child : this.getChildren()) {
            if (child.getClass().getName().endsWith("ContentDivider")) {
                child.setManaged(showDivider);
                child.setVisible(showDivider);
            }
        }
    }

    /**
     * 是否显示分割条
     *
     * @return 是否显示
     */
    public boolean isShowDivider() {
        return showDivider;
    }

    @Override
    public void initNode() {
        this.getItems().addListener((ListChangeListener<Node>) c -> {
            if (c.next()) {
                List<? extends Node> subs = c.getAddedSubList();
                for (Node node : subs) {
                    SplitPane.setResizableWithParent(node, false);
                }
            }
        });
        this.getChildren().addListener((ListChangeListener<Node>) c -> {
            if (c.next()) {
                List<? extends Node> subs = c.getAddedSubList();
                for (Node node : subs) {
                    if (node.getClass().getName().endsWith("ContentDivider")) {
                        node.setManaged(this.showDivider);
                        node.setVisible(this.showDivider);
                    }
                }
            }
        });
        FlexAdapter.super.initNode();
    }
}
