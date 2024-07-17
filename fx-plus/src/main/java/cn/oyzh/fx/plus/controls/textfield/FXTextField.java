package cn.oyzh.fx.plus.controls.textfield;


import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TextAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.handler.StateManager;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.scene.control.TextField;
import javafx.scene.text.FontWeight;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * 基础文本域
 *
 * @author oyzh
 * @since 2023/08/15
 */
public class FXTextField extends TextField implements NodeGroup, NodeAdapter, ThemeAdapter, FontAdapter, TextAdapter, TipAdapter, StateAdapter {

    {
        NodeManager.init(this);
    }

    @Getter
    @Setter
    private boolean require;

    public FXTextField() {
        super.setText("");
    }

    public FXTextField(String text) {
        super.setText(text);
    }

    @Override
    public void setTipText(String tipText) {
        TipAdapter.super.tipText(tipText);
        if (this.getPromptText() == null || this.getPromptText().isEmpty()) {
            this.setPromptText(tipText);
        }
    }

    @Override
    public String getTipText() {
        return TipAdapter.super.tipText();
    }

    /**
     * 是否为空
     *
     * @return 结果
     */
    public boolean isEmpty() {
        return this.getLength() == 0 || this.getText().isEmpty();
    }

    /**
     * 校验数据
     *
     * @return 结果
     */
    public boolean validate() {
        if (this.require && this.isEmpty()) {
            this.requestFocus();
            return false;
        }
        return true;
    }

    @Override
    public void setStateManager(StateManager manager) {
        StateAdapter.super.stateManager(manager);
    }

    @Override
    public StateManager getStateManager() {
        return StateAdapter.super.stateManager();
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
    public void setFontFamily(@NonNull String fontFamily) {
        FontAdapter.super.fontFamily(fontFamily);
    }

    @Override
    public String getFontFamily() {
        return FontAdapter.super.fontFamily();
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
    public void initNode() {
        this.setPickOnBounds(true);
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

    /**
     * 设置值
     *
     * @param val 值
     */
    public void setValue(Object val) {
        if (val instanceof CharSequence sequence) {
            this.setText(sequence.toString());
        } else if (val instanceof byte[] bytes) {
            this.setText(new String(bytes));
        } else if (val != null) {
            this.setText(val.toString());
        }
    }
}
