package cn.oyzh.fx.plus.controls.button;

import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.beans.value.ChangeListener;
import javafx.scene.Cursor;
import javafx.scene.control.CheckBox;
import lombok.NonNull;

/**
 * @author oyzh
 * @since 2020/10/29
 */
public class FXCheckBox extends CheckBox implements NodeGroup, NodeAdapter, ThemeAdapter, TipAdapter, StateAdapter, FontAdapter {

    {
        NodeManager.init(this);
    }

    public FXCheckBox() {
        super();
    }

    public FXCheckBox(String text) {
        super(text);
    }

//    @Override
//    public void setTipText(String tipText) {
//        TipAdapter.super.tipText(tipText);
//    }
//
//    @Override
//    public String getTipText() {
//        return TipAdapter.super.tipText();
//    }

    /**
     * 选中变更事件
     *
     * @param listener 监听器
     */
    public void selectedChanged(@NonNull ChangeListener<Boolean> listener) {
        this.selectedProperty().addListener(listener);
    }

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

//    @Override
//    public void setGroupId(String groupId) {
//        NodeGroup.super.groupId(groupId);
//    }
//
//    @Override
//    public String getGroupId() {
//        return NodeGroup.super.groupId();
//    }
}
