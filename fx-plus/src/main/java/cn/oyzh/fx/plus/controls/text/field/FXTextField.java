package cn.oyzh.fx.plus.controls.text.field;


import cn.oyzh.common.object.Destroyable;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TextAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeDestroyUtil;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.validator.ValidatorUtil;
import cn.oyzh.fx.plus.validator.Verifiable;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;

/**
 * 基础文本域
 *
 * @author oyzh
 * @since 2023/08/15
 */
public class FXTextField extends TextField implements Destroyable, FlexAdapter, Verifiable, NodeGroup, NodeAdapter, ThemeAdapter, FontAdapter, TextAdapter, TipAdapter, StateAdapter {

    {
        NodeManager.init(this);
    }

    /**
     * 失焦事件
     */
    protected void onBlur() {

    }

    /**
     * 焦点事件
     */
    protected void onFocus() {

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
        FlexAdapter.super.initNode();
        this.setPickOnBounds(true);
        //        this.setFocusTraversable(false);
    }

    /**
     * 当前值
     */
    private Object value;

    /**
     * 获取值
     *
     * @return 结果
     */
    public Object getValue() {
        return this.value;
    }

    /**
     * 设置值
     *
     * @param value 值
     */
    public void setValue(Object value) {
        this.value = value;
        this.formatValue();
    }

    /**
     * 格式化值
     */
    public void formatValue() {
        this.setText(format(value));
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

    /**
     * 当前皮肤
     *
     * @return 皮肤
     */
    public Skin<?> skin() {
        if (this.getSkin() == null) {
            this.setSkin(this.createDefaultSkin());
        }
        return this.getSkin();
    }

    @Override
    public void destroy() {
        //        this.setTipText(null);
        //        this.setContextMenu(null);
        NodeDestroyUtil.destroyNode(this);
        NodeDestroyUtil.destroyObject(this);
    }
}
