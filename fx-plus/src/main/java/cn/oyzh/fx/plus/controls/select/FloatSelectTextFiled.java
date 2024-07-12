package cn.oyzh.fx.plus.controls.select;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.common.util.NumUtil;
import cn.oyzh.fx.plus.controls.digital.FloatTextField;
import cn.oyzh.fx.plus.controls.digital.IntTextField;
import cn.oyzh.fx.plus.converter.DigitalFormatStringConverter;
import cn.oyzh.fx.plus.format.DigitalDecimalFormat;
import javafx.scene.control.TextFormatter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * @author oyzh
 * @since 2024/07/12
 */
public class FloatSelectTextFiled extends DigitalSelectTextField {

    /**
     * 小数位数
     */
    protected Integer scaleLen;

    public void setScaleLen(Integer scaleLen) {
        this.scaleLen = scaleLen;
        this.format();
    }

    public FloatSelectTextFiled() {
        super(false, null);
    }

    public FloatSelectTextFiled(boolean unsigned) {
        super(unsigned, null);
    }

    public FloatSelectTextFiled(boolean unsigned, Long maxLen) {
        super(unsigned, maxLen);
    }

    public FloatSelectTextFiled(boolean unsigned, Long maxLen, Integer scaleLen) {
        super(unsigned, maxLen);
        this.setScaleLen(scaleLen);
    }

    public FloatSelectTextFiled(boolean unsigned, Long maxLen, Long minVal, Long maxVal, Integer scaleLen) {
        super(unsigned, maxLen);
        super.setMinVal(minVal);
        super.setMaxVal(maxVal);
        this.setScaleLen(scaleLen);
    }

    @Override
    protected DigitalFormatStringConverter getConverter() {
        return new DigitalFormatStringConverter(this.format());
    }

    @Override
    protected UnaryOperator<TextFormatter.Change> createFilter() {
        return change -> {
            if (change.isAdded() || change.isReplaced() || change.isContentChange()) {
                try {
                    String text = change.getControlNewText();
                    // 如果文本为空、"+"、"."，则不进行任何操作，直接返回原change对象
                    if (StrUtil.isEmpty(text) || text.equals("+") || ".".equals(text)) {
                        return change;
                    }
                    // 无符号判断
                    if (this.isUnsigned() && text.startsWith("-")) {
                        return null;
                    }
                    // 数字判断
                    if (!NumberUtil.isDouble(text) && !NumberUtil.isNumber(text)) {
                        return null;
                    }
                    // 长度判断
                    if (!super.checkLenLimit(change)) {
                        return null;
                    }
                    // 判断小数位数
                    BigDecimal decimal = new BigDecimal(text);
                    // 如果小数位数超过了设定的最大位数，则将小数转换为最大位数，并设置组件值为转换后的结果
                    if (this.scaleLen != null && decimal.scale() > this.scaleLen) {
                        this.setValue(decimal.setScale(this.scaleLen, RoundingMode.HALF_UP).doubleValue());
                        return null;
                    }
                    // 如果超过了最大值，则将组件值设置为最大值
                    if (this.maxVal != null && NumUtil.isGT(decimal.doubleValue(), this.maxVal)) {
                        this.setValue(this.maxVal.doubleValue());
                        return null;
                    }
                    // 如果小于了最小值，则将组件值设置为最小值
                    if (this.minVal != null && NumUtil.isLT(decimal.doubleValue(), this.minVal)) {
                        this.setValue(this.minVal.doubleValue());
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
    public Double getValue() {
        Number val = this.value();
        // 否则，将文本转为Double类型并返回
        return val == null ? null : val.doubleValue();
    }

    @Override
    public Number value() {
        // 获取文本内容
        String text = this.getText();
        // 如果文本为空，或者为"-"，或者为"+"，或者为"."，则返回0.D
        if (StrUtil.equalsAny(text, "", "-", "+", ".")) {
            return 0D;
        }
        return super.value();
    }

    /**
     * 设置值，如果超出最大值或最小值，将设置为最大值或最小值
     *
     * @param value 值
     */
    public void setValue(Double value) {
        this.value(value);
    }

    private DigitalDecimalFormat format;

    private DigitalDecimalFormat format() {
        if (this.format == null) {
            this.format = new DigitalDecimalFormat(this.scaleLen);
        } else {
            this.format.setScaleLen(this.scaleLen);
        }
        return this.format;
    }

    public void setMin(Double minVal) {
        this.minVal = minVal;
    }

    public Double getMin() {
        return this.minVal == null ? null : this.minVal.doubleValue();
    }

    public void setMax(Double maxVal) {
        this.maxVal = maxVal;
    }

    public Double getMax() {
        return this.maxVal == null ? null : this.maxVal.doubleValue();
    }
}
