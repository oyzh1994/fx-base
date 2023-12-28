package cn.oyzh.fx.plus.controls.digital;


import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.common.util.NumUtil;
import javafx.scene.control.TextFormatter;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.UnaryOperator;

/**
 * 小数文本域
 *
 * @author oyzh
 * @since 2023/08/28
 */
public class DecimalTextField extends DigitalTextField {

    /**
     * 小数位数
     */
    @Setter
    @Getter
    protected Integer scaleLen;

    public DecimalTextField() {
        super();
    }

    @Override
    protected void incrValue() {
        // 设置值
        if (this.step != null) {
            this.setValue(this.getValue() + this.step.doubleValue());
        }
    }

    @Override
    protected void decrValue() {
        // 设置值
        if (this.step != null) {
            this.setValue(this.getValue() - this.step.doubleValue());
        }
    }

    @Override
    protected UnaryOperator<TextFormatter.Change> createFilter() {
        return change -> {
            if (change.isAdded() || change.isReplaced() || change.isContentChange()) {
                try {
                    String text = change.getControlNewText();
                    // 如果文本为空，或者为"-"、"+"或者"."，则不进行任何操作，直接返回原change对象
                    if (StrUtil.isEmpty(text) || text.equals("-") || text.equals("+") || ".".equals(text)) {
                        return change;
                    }
                    // 判断数字
                    if (!NumberUtil.isDouble(text) && !NumberUtil.isNumber(text)) {
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

    @Override
    protected void value(Number value) {
        if (value != null && this.scaleLen != null) {
            BigDecimal decimal = NumberUtil.toBigDecimal(value);
            if (decimal.scale() > this.scaleLen) {
                value = decimal.setScale(this.scaleLen, RoundingMode.HALF_UP);
                this.textFormatter.setValue(value);
            } else {
                super.value(value);
            }
        } else {
            super.value(value);
        }
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
