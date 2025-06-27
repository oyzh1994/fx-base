package cn.oyzh.fx.plus.controls.text.field;


import cn.oyzh.common.util.StringUtil;
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
import cn.oyzh.fx.plus.validator.ValidatorUtil;
import cn.oyzh.fx.plus.validator.Verifiable;
import javafx.scene.control.TextField;

/**
 * 基础文本域
 *
 * @author oyzh
 * @since 2023/08/15
 */
public class FXTextField extends TextField implements FlexAdapter, Verifiable, NodeGroup, NodeAdapter, ThemeAdapter, FontAdapter, TextAdapter, TipAdapter, StateAdapter {

    {
        NodeManager.init(this);
    }

    /**
     * 失焦事件
     */
    protected void onBlur() {

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

    /**
     * 是否不能为空或者空白
     */
    private boolean notEmpty;

    public boolean isNotEmpty() {
        return notEmpty;
    }

    public void setNotEmpty(boolean notEmpty) {
        this.notEmpty = notEmpty;
    }

    public FXTextField() {
        super.setText("");
    }

    public FXTextField(String text) {
        super.setText(text);
    }

    @Override
    public void setTipText(String tipText) {
        TipAdapter.super.setTipText(tipText);
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
//            this.requestFocus();
            ValidatorUtil.validFail(this);
            return false;
        }
        if (this.notEmpty && this.isEmpty() || StringUtil.isBlank(this.getText())) {
            ValidatorUtil.validFail(this);
            return false;
        }
        return Verifiable.super.validate();
    }

    @Override
    public void initNode() {
        this.setPickOnBounds(true);
//        this.setFocusTraversable(false);
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
}
