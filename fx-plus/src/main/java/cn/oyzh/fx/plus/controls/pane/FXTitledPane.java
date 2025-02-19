package cn.oyzh.fx.plus.controls.pane;

import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.flex.FlexUtil;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.beans.value.ChangeListener;
import javafx.scene.Cursor;
import javafx.scene.control.TitledPane;

/**
 * @author oyzh
 * @since 2023/11/21
 */
public class FXTitledPane extends TitledPane implements FlexAdapter, NodeGroup, NodeAdapter, TipAdapter, StateAdapter, FontAdapter, ThemeAdapter {

    {
        NodeManager.init(this);
    }

    @Override
    public void initNode() {
        this.setAnimated(true);
        this.setCursor(Cursor.HAND);
        this.setPickOnBounds(true);
        this.setMnemonicParsing(false);
//        this.setFocusTraversable(false);
    }

    private ChangeListener<Boolean> autoHideListener;

    public void setAutoHide(boolean autoHide) {
        if (autoHide) {
            if (this.autoHideListener == null) {
                this.autoHideListener = (observable, oldValue, newValue) -> {
                    if (newValue) {
                        NodeUtil.display(this.getContent());
                    } else {
                        this.setHeight(0);
                        this.setMinHeight(0);
                        this.setMaxHeight(0);
                        this.setPrefHeight(0);
                        NodeUtil.disappear(this.getContent());
                    }
                };
                this.expandedProperty().addListener(this.autoHideListener);
//                this.expandedProperty().addListener(new WeakChangeListener<>(this.autoHideListener));
            }
        } else {
//            if (this.autoHideListener != null) {
            this.expandedProperty().unbind();
//                this.expandedProperty().removeListener(this.autoHideListener);
            this.autoHideListener = null;
//            }
        }
        this.setProp("autoHide", autoHide);
    }

    public boolean getAutoHide() {
        Object object = this.getProp("autoHide");
        if (object == null) {
            return false;
        }
        return (boolean) object;
    }

    public void appendText(String text) {
        if (text != null) {
            String titleText = this.getProp("titleText");
            if (titleText == null) {
                this.setProp("titleText", this.getText());
                titleText = this.getText();
            }
            this.setText(titleText + text);
        }
    }

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }

    @Override
    public void resizeNode(Double width, Double height) {
        FlexAdapter.super.resizeNode(width, height);
        if (this.getContent() instanceof FlexAdapter flexNode) {
            flexNode.setRealWidth(FlexUtil.compute(flexNode.getFlexWidth(), width));
            flexNode.setRealHeight(FlexUtil.compute(flexNode.getFlexHeight(), height));
        } else {
            NodeUtil.setWidth(this.getContent(), width);
            NodeUtil.setHeight(this.getContent(), height);
        }
    }
}
