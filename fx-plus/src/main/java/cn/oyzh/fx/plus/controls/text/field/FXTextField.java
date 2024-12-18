package cn.oyzh.fx.plus.controls.text.field;


import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TextAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.scene.control.TextField;
import lombok.Getter;
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
    public void initNode() {
        this.setPickOnBounds(true);
        this.setFocusTraversable(false);
    }

    /**
     * 设置值
     *
     * @param val 值
     */
    public void setValue(Object val) {
        this.setText(format(val));
    }

    public static String format(Object val) {
        if (val instanceof CharSequence sequence) {
            return sequence.toString();
        }
        if (val instanceof byte[] bytes) {
            return new String(bytes);
        }
        if (val != null) {
            return val.toString();
        }
        return null;
    }
}
