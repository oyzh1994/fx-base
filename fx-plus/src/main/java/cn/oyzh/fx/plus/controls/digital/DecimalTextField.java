package cn.oyzh.fx.plus.controls.digital;


import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.plus.converter.DoubleConverter;
import cn.oyzh.fx.plus.skin.NumberTextFieldSkin;
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

    /**
     * 文本格式器
     */
    protected final TextFormatter<Double> textFormatter;

    /**
     * 数据转换器
     */
    protected final DoubleConverter converter = new DoubleConverter();

    public DecimalTextField() {
        this.setSkin(new NumberTextFieldSkin(this, this::incrValue, this::decrValue));
        if (this.defVal != null) {
            this.textFormatter = new TextFormatter<>(this.converter, this.defVal.doubleValue(), this.createFilter());
        } else {
            this.textFormatter = new TextFormatter<>(this.converter, null, this.createFilter());
        }
        // 将TextFormatter对象设置到文本字段中
        this.setTextFormatter(this.textFormatter);
        // 监听值变化
        this.textFormatter.valueProperty().addListener((observableValue, number, t1) -> this.valueChanged(t1));
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
                    if (this.max != null && decimal.doubleValue() > this.max.doubleValue()) {
                        this.setValue(this.max.doubleValue());
                        return null;
                    }
                    // 如果小于了最小值，则将组件值设置为最小值
                    if (this.min != null && decimal.doubleValue() < this.min.doubleValue()) {
                        this.setValue(this.min.doubleValue());
                        return null;
                    }
                } catch (Exception ignored) {
                }
            }
            return change;
        };
    }

    /**
     * 值变化
     *
     * @param newVal 新值
     */
    protected void valueChanged(Double newVal) {
        // 检查新值是否有效
        if (newVal != null && !Double.isNaN(newVal)) {
            // 获取当前皮肤
            NumberTextFieldSkin skin = (NumberTextFieldSkin) this.getSkin();
            // 判断新值是否小于等于最小值，如果是则禁用减号按钮
            if (this.min != null && newVal <= this.min.doubleValue()) {
                skin.disableDecrButton();
            } else {
                // 否则启用减号按钮
                skin.enableDecrButton();
            }
            // 判断新值是否大于等于最大值，如果是则禁用加号按钮
            if (this.max != null && newVal >= this.max.doubleValue()) {
                skin.disableIncrButton();
            } else {
                // 否则启用加号按钮
                skin.enableIncrButton();
            }
        }
    }

    @Override
    public Byte getByteValue() {
        Double val = this.getValue();
        return val == null ? null : val.byteValue();
    }

    @Override
    public Short getShortValue() {
        Double val = this.getValue();
        return val == null ? null : val.shortValue();
    }

    @Override
    public Integer getIntegerValue() {
        Double val = this.getValue();
        return val == null ? null : val.intValue();
    }

    @Override
    public Long getLongValue() {
        Double val = this.getValue();
        return val == null ? null : val.longValue();
    }

    @Override
    public Float getFloatValue() {
        Double val = this.getValue();
        return val == null ? null : val.floatValue();
    }

    @Override
    public Double getDoubleValue() {
        return this.getValue();
    }

    /**
     * 获取值
     *
     * @return 值
     */
    public Double getValue() {
        // 获取文本内容
        String text = this.getText();
        // 如果文本为空，或者为"-"，或者为"+"，或者为"."，则返回0.D
        if (StrUtil.isEmpty(text) || "-".equals(text) || "+".equals(text) || ".".equals(text)) {
            return 0.D;
        }
        // 否则，将文本转为Double类型并返回
        return NumberUtil.parseDouble(text);
    }

    /**
     * 设置值，如果超出最大值或最小值，将设置为最大值或最小值
     *
     * @param value 值
     */
    public void setValue(Double value) {
        if (value != null && !Double.isNaN(value)) {
            if (this.max != null && value > this.max.doubleValue()) {
                value = this.max.doubleValue();
            } else if (this.min != null && value < this.min.doubleValue()) {
                value = this.min.doubleValue();
            } else if (this.scaleLen != null) {
                BigDecimal decimal = new BigDecimal(value);
                if (decimal.scale() > this.scaleLen) {
                    value = decimal.setScale(this.scaleLen, RoundingMode.HALF_UP).doubleValue();
                }
            }
            this.textFormatter.setValue(value);
        }
    }

    @Override
    public void setDefVal(Number defVal) {
        super.setDefVal(defVal);
        if (this.getValue() == null && defVal != null) {
            this.setValue(defVal.doubleValue());
        }
    }

    @Override
    public void defVal(Object defVal) {
        super.defVal(defVal);
        if (this.getValue() == null && super.defVal != null) {
            this.setValue(super.defVal.doubleValue());
        }
    }
}
