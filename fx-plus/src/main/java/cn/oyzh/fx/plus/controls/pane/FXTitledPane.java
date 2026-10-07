package cn.oyzh.fx.plus.controls.pane;

import cn.oyzh.common.util.StringUtil;
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
import javafx.scene.Cursor;
import javafx.scene.control.TitledPane;

/**
 * 可折叠标题面板控件
 *
 * @author oyzh
 * @since 2023-10-09
 */
public class FXTitledPane extends TitledPane implements FlexAdapter, NodeGroup, NodeAdapter, TipAdapter, StateAdapter, FontAdapter, ThemeAdapter {

    {
        NodeManager.init(this);
    }

    @Override
    public void initNode() {
        FlexAdapter.super.initNode();
        this.setAnimated(true);
        this.setCursor(Cursor.HAND);
        this.setPickOnBounds(true);
        this.setMnemonicParsing(false);
//        this.setFocusTraversable(false);
    }

//     private ChangeListener<Boolean> autoHideListener;
//
//     public void setAutoHide(boolean autoHide) {
//         if (autoHide) {
//             if (this.autoHideListener == null) {
//                 this.autoHideListener = (observable, oldValue, newValue) -> {
//                     if (newValue) {
//                         NodeUtil.display(this.getContent());
//                     } else {
//                         this.setHeight(0);
//                         this.setMinHeight(0);
//                         this.setMaxHeight(0);
//                         this.setPrefHeight(0);
//                         NodeUtil.disappear(this.getContent());
//                     }
//                 };
//                 this.expandedProperty().addListener(this.autoHideListener);
// //                this.expandedProperty().addListener(new WeakChangeListener<>(this.autoHideListener));
//             }
//         } else {
//             if (this.autoHideListener != null) {
//                 // this.expandedProperty().unbind();
//                 this.expandedProperty().removeListener(this.autoHideListener);
//                 this.autoHideListener = null;
//             }
//         }
//         this.setProp("autoHide", autoHide);
//     }
//
//     public boolean getAutoHide() {
//         Object object = this.getProp("autoHide");
//         if (object == null) {
//             return false;
//         }
//         return (boolean) object;
//     }

    // public void appendText(String text) {
    //     if (text != null) {
    //         String titleText = this.getProp("titleText");
    //         if (titleText == null) {
    //             this.setProp("titleText", this.getText());
    //             titleText = this.getText();
    //         }
    //         this.setText(titleText + text);
    //     }
    // }

    /**
     * 追加标题文本
     *
     * @param appendText 追加文本
     */
    public void setAppendText(String appendText) {
        if (StringUtil.isEmpty(appendText)) {
            return;
        }
        String text;
        if (this.hasProp("appendText")) {
            text = this.getProp("text");
        } else {
            text = this.getText();
        }
        if (StringUtil.isEmpty(text)) {
            this.setText(appendText);
        } else {
            this.setText(text + appendText);
        }
        this.setProp("text", text);
        this.setProp("appendText", appendText);
    }

    /**
     * 获取追加文本
     *
     * @return 追加文本
     */
    public String getAppendText() {
        return this.getProp("appendText");
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
