package cn.oyzh.fx.plus.controls.pane;

import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.util.NodeUtil;
import javafx.beans.value.ChangeListener;
import javafx.scene.Cursor;
import javafx.scene.control.TitledPane;
import javafx.scene.text.FontWeight;
import lombok.NonNull;

/**
 * @author oyzh
 * @since 2023/11/21
 */
public class FXTitledPane extends TitledPane implements NodeGroup, NodeAdapter, TipAdapter, StateAdapter, FontAdapter, ThemeAdapter {

    {
        NodeManager.init(this);
        this.setAnimated(true);
    }

    @Override
    public void setFontSize(double fontSize) {
        FontAdapter.super.fontSize(fontSize);
    }

    @Override
    public double getFontSize() {
        return FontAdapter.super.fontSize();
    }

    @Override
    public String getFontFamily() {
        return FontAdapter.super.fontFamily();
    }

    @Override
    public void setFontFamily(@NonNull String fontFamily) {
        FontAdapter.super.fontFamily(fontFamily);
    }

    @Override
    public void setFontWeight(FontWeight fontWeight) {
        FontAdapter.super.fontWeight(fontWeight);
    }

    @Override
    public FontWeight getFontWeight() {
        return FontAdapter.super.fontWeight();
    }

    @Override
    public void setTipText(String tipText) {
        TipAdapter.super.tipText(tipText);
    }

    @Override
    public String getTipText() {
        return TipAdapter.super.tipText();
    }

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
    public void initNode() {
        this.setCursor(Cursor.HAND);
        this.setPickOnBounds(true);
        this.setMnemonicParsing(false);
        this.setFocusTraversable(false);
    }

    @Override
    public void setGroupId(String groupId) {
        NodeGroup.super.groupId(groupId);
    }

    @Override
    public String getGroupId() {
        return NodeGroup.super.groupId();
    }

    private ChangeListener<Boolean> autoHideListener;

    public void setAutoHide(boolean autoHide) {
        if (autoHide) {
            if (this.autoHideListener == null) {
                this.autoHideListener = (observable, oldValue, newValue) -> {
                    if (newValue) {
                        NodeUtil.display(this.getContent());
                    } else {
                        this.setMaxHeight(0);
                        this.setPrefHeight(0);
                        NodeUtil.disappear(this.getContent());
                    }
                };
                this.expandedProperty().addListener(this.autoHideListener);
            }
        } else {
            if (this.autoHideListener != null) {
                this.expandedProperty().removeListener(this.autoHideListener);
                this.autoHideListener = null;
            }
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
}
