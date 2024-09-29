package cn.oyzh.fx.plus.controls.textfield;

import cn.oyzh.fx.common.util.NumberUtil;
import cn.oyzh.fx.common.util.RegexUtil;
import cn.oyzh.fx.common.util.StringUtil;
import cn.oyzh.fx.plus.converter.DigitalFormatStringConverter;
import javafx.scene.control.TextFormatter;

import java.util.function.UnaryOperator;

/**
 * 整数文本域
 *
 * @author oyzh
 * @since 2020/10/29
 */
public class NumberTextField extends DigitalTextField {

    public NumberTextField() {
        super(false, null);
    }

    public NumberTextField(boolean unsigned) {
        super(unsigned, null);
    }

    public NumberTextField(boolean unsigned, Long maxLen) {
        super(unsigned, maxLen);
    }

    public NumberTextField(boolean unsigned, Long maxLen, Long minVal, Long maxVal) {
        super(unsigned, maxLen);
        super.setMinVal(minVal);
        super.setMaxVal(maxVal);
    }

    private DigitalFormatStringConverter converter;

    @Override
    protected DigitalFormatStringConverter getConverter() {
        if (this.converter == null) {
            this.converter = new DigitalFormatStringConverter();
        }
        return this.converter;
    }

    @Override
    protected void incrValue() {
        // 设置值
        if (this.step != null) {
            this.setValue(this.getValue() + this.step.longValue());
        }
    }

    @Override
    protected void decrValue() {
        // 设置值
        if (this.step != null) {
            this.setValue(this.getValue() - this.step.longValue());
        }
    }

    @Override
    protected UnaryOperator<TextFormatter.Change> createFilter() {
        return change -> {
            if (change.isAdded() || change.isReplaced() || change.isContentChange()) {
                try {
                    String text = change.getControlNewText();
                    // 如果文本为空、"+"，则不进行任何操作，直接返回原change对象
                    if (StringUtil.isEmpty(text) || text.equals("+")) {
                        return change;
                    }
                    // 无符号判断
                    if (this.isUnsigned() && text.startsWith("-")) {
                        return null;
                    }
                    // 数字判断
                    if (!RegexUtil.isNumber(text)) {
                        return null;
                    }
                    // 长度判断
                    if (!super.checkLenLimit(change)) {
                        return null;
                    }
                    Number number = NumberUtil.parseNumber(text);
                    // 如果超过了最大值，则将组件值设置为最大值
                    if (this.maxVal != null && cn.oyzh.fx.common.util.NumberUtil.isGT(number.longValue(), this.maxVal)) {
                        this.setValue(this.maxVal.longValue());
                        return null;
                    }
                    // 如果小于了最小值，则将组件值设置为最小值
                    if (this.minVal != null && cn.oyzh.fx.common.util.NumberUtil.isLT(number.longValue(), this.minVal)) {
                        this.setValue(this.minVal.longValue());
                        return null;
                    }
                } catch (Exception ignored) {
                }
            }
            return change;
        };
    }

    /**
     * 获取值
     *
     * @return 值
     */
    public Long getValue() {
        Number val = this.value();
        // 否则，将文本转为Double类型并返回
        return val == null ? null : val.longValue();
    }

    @Override
    protected Number value() {
        String text = this.getText();
        if (StringUtil.equalsAny(text, "", "+", "-")) {
            return null;
        }
        return super.value();
    }

    /**
     * 设置值，如果超出最大值或最小值，将设置为最大值或最小值
     *
     * @param value 值
     */
    public void setValue(Long value) {
        if (value != null) {
            super.value(value);
        }
    }

    public static String format(Object val) {
        if (val instanceof CharSequence sequence) {
            return sequence.toString();
        }
        if (val instanceof Number number) {
            return number.longValue() + "";
        }
        return null;
    }

    /**
     * 设置值，如果超出最大值或最小值，将设置为最大值或最小值
     *
     * @param value 值
     */
    public void setValue(long value) {
        super.value(value);
    }

    public void setMin(Long minVal) {
        this.minVal = minVal;
    }

    public Long getMin() {
        return this.minVal == null ? null : this.minVal.longValue();
    }

    public void setMax(Long maxVal) {
        this.maxVal = maxVal;
    }

    public Long getMax() {
        return this.maxVal == null ? null : this.maxVal.longValue();
    }
}
