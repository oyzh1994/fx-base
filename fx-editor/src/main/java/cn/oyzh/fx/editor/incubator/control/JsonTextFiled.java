package cn.oyzh.fx.editor.incubator.control;


import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.fx.gui.text.field.LimitTextField;

/**
 * JSON文本输入框
 *
 * @author oyzh
 * @since 2026-06-05
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

    /**
     * 设置展开宽度
     *
     * @param width 展开宽度
     */
    public void setEnlargeWidth(double width) {
        this.skin().setEnlargeWidth(width);
    }

    /**
     * 获取展开宽度
     *
     * @return 展开宽度
     */
    public double getEnlargeWidth() {
        return this.skin().getEnlargeWidth();
    }

    /**
     * 设置展开高度
     *
     * @param height 展开高度
     */
    public void setEnlargeHeight(double height) {
        this.skin().setEnlargeHeight(height);
    }

    /**
     * 获取展开高度
     *
     * @return 展开高度
     */
    public double getEnlargeHeight() {
        return this.skin().getEnlargeHeight();
    }

    /**
     * 是否为数组
     */
    private boolean array;

    /**
     * 是否为数组
     *
     * @return 结果
     */
    public boolean isArray() {
        return array;
    }

    /**
     * 设置是否为数组
     *
     * @param array 是否为数组
     */
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

    /**
     * 格式化对象
     *
     * @param val 对象
     * @return 格式化后的文本
     */
    public static String format(Object val) {
        if (val instanceof CharSequence sequence) {
            return JSONUtil.toPretty(sequence.toString());
        }
        return JSONUtil.toPretty(val);
    }

}
