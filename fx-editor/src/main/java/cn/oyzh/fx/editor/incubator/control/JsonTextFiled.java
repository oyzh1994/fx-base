package cn.oyzh.fx.editor.incubator.control;


import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.fx.gui.text.field.LimitTextField;

/**
 * @author oyzh
 * @since 2024/7/21
 */
public class JsonTextFiled extends LimitTextField {

    @Override
    public LongTextFiledSkin skin() {
        return (LongTextFiledSkin) super.skin();
    }

    @Override
    protected LongTextFiledSkin createDefaultSkin() {
        return new LongTextFiledSkin(this);
    }

    public void setEnlargeWidth(double width) {
        this.skin().setEnlargeWidth(width);
    }

    public double getEnlargeWidth() {
        return this.skin().getEnlargeWidth();
    }

    public void setEnlargeHeight(double height) {
        this.skin().setEnlargeHeight(height);
    }

    public double getEnlargeHeight() {
        return this.skin().getEnlargeHeight();
    }

    private boolean array;

    public boolean isArray() {
        return array;
    }

    public void setArray(boolean array) {
        this.array = array;
    }

    @Override
    public Object getValue() {
        String text = this.getText();
        return this.isArray() ? JSONUtil.parseArray(text) : JSONUtil.parseObject(text);
    }

    @Override
    public void formatValue() {
        this.setText(format(super.value()));
    }

    public static String format(Object val) {
        if (val instanceof CharSequence sequence) {
            return JSONUtil.toPretty(sequence.toString());
        }
        return JSONUtil.toPretty(val);
    }

}
