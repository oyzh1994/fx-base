package cn.oyzh.fx.gui.text.field;


import cn.oyzh.fx.gui.skin.PasswordTextFieldSkin;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TextAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.validator.Verifiable;
import javafx.scene.control.Skin;

/**
 * 密码文本域
 *
 * @author oyzh
 * @since 2025/04/02
 */
public class PasswordTextField extends atlantafx.base.controls.PasswordTextField implements FlexAdapter, Verifiable, NodeGroup, NodeAdapter, ThemeAdapter, FontAdapter, TextAdapter, TipAdapter, StateAdapter {

    {
        NodeManager.init(this);
    }

    /**
     * 是否必须
     */
    private boolean require;

    public boolean isRequire() {
        return require;
    }

    public void setRequire(boolean require) {
        this.require = require;
    }

    public PasswordTextField() {
        super.setText("");
    }

    public PasswordTextField(String text) {
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

    @Override
    public boolean validate() {
        if (this.require && this.isEmpty()) {
            this.requestFocus();
            return false;
        }
        return Verifiable.super.validate();
    }

    @Override
    public void initNode() {
        this.setPickOnBounds(true);
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

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }

    public void text(String text) {
        FXUtil.runWait(() -> super.setText(text));
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PasswordTextFieldSkin(this);
    }
}
