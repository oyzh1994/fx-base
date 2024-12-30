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

/**
 * @author oyzh
 * @since 2023/11/21
 */
public class FXTitledPane extends TitledPane implements NodeGroup, NodeAdapter, TipAdapter, StateAdapter, FontAdapter, ThemeAdapter {

    {
        NodeManager.init(this);
    }

    @Override
    public void initNode() {
        this.setAnimated(true);
        this.setCursor(Cursor.HAND);
        this.setPickOnBounds(true);
        this.setMnemonicParsing(false);
        this.setFocusTraversable(false);
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
