package cn.oyzh.fx.plus.controls.label;

import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.adapter.MouseAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TextAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.FontWeight;
import lombok.NonNull;

/**
 * @author oyzh
 * @since 2020/10/29
 */
public class FXLabel extends Label implements NodeGroup, ThemeAdapter, MouseAdapter, TextAdapter, TipAdapter, StateAdapter, FontAdapter, LayoutAdapter, NodeAdapter {

    {
        NodeManager.init(this);
    }

    public FXLabel() {
        super("");
    }

    public FXLabel(String text) {
        super(text);
    }

    public FXLabel(String text, Node graphic) {
        super(text, graphic);
    }

//    @Override
//    public void setOnMousePrimaryClicked(EventHandler<? super MouseEvent> handler) {
//        MouseAdapter.super.setOnMousePrimaryClicked(handler);
//    }
//
//    @Override
//    public EventHandler<? super MouseEvent> getOnMousePrimaryClicked() {
//        return MouseAdapter.super.getOnMousePrimaryClicked();
//    }
//
//    @Override
//    public void setOnMouseSecondClicked(EventHandler<? super MouseEvent> handler) {
//        MouseAdapter.super.setOnMouseSecondClicked(handler);
//    }
//
//    @Override
//    public EventHandler<? super MouseEvent> getOnMouseSecondClicked() {
//        return MouseAdapter.super.getOnMouseSecondClicked();
//    }

//    @Override
//    public void setTipText(String tipText) {
//        TipAdapter.super.tipText(tipText);
//    }
//
//    @Override
//    public String getTipText() {
//        return TipAdapter.super.tipText();
//    }
//
//    @Override
//    public void setFontSize(double fontSize) {
//        FontAdapter.super.fontSize(fontSize);
//    }
//
//    @Override
//    public double getFontSize() {
//        return FontAdapter.super.fontSize();
//    }
//
//    @Override
//    public void setFontFamily(@NonNull String fontFamily) {
//        FontAdapter.super.fontFamily(fontFamily);
//    }
//
//    @Override
//    public String getFontFamily() {
//        return FontAdapter.super.fontFamily();
//    }
//
//    @Override
//    public void setFontWeight(FontWeight fontWeight) {
//        FontAdapter.super.fontWeight(fontWeight);
//    }
//
//    @Override
//    public FontWeight getFontWeight() {
//        return FontAdapter.super.fontWeight();
//    }
//
//    @Override
//    public double getRealWidth() {
//        return LayoutAdapter.super.realWidth();
//    }
//
//    @Override
//    public void setRealWidth(double width) {
//        LayoutAdapter.super.realWidth(width);
//    }
//
//    @Override
//    public double getRealHeight() {
//        return LayoutAdapter.super.realHeight();
//    }
//
//    @Override
//    public void setRealHeight(double height) {
//        LayoutAdapter.super.realHeight(height);
//    }

    // @Override
    // public void setStateManager(StateManager manager) {
    //     StateAdapter.super.stateManager(manager);
    // }
    //
    // @Override
    // public StateManager getStateManager() {
    //     return StateAdapter.super.stateManager();
    // }

    @Override
    public boolean isEnableTheme() {
        return ThemeAdapter.super.isEnableTheme();
    }

    @Override
    public void setEnableTheme(boolean enableTheme) {
        ThemeAdapter.super.setEnableTheme(enableTheme);
    }

//    @Override
//    public void setGroupId(String groupId) {
//        NodeGroup.super.groupId(groupId);
//    }
//
//    @Override
//    public String getGroupId() {
//        return NodeGroup.super.groupId();
//    }

    @Override
    public void initNode() {

    }

    public void clear() {
        this.setText("");
    }

    public void text(String text) {
        if (text != null) {
            FXUtil.runWait(() -> super.setText(text));
        }
    }
}
