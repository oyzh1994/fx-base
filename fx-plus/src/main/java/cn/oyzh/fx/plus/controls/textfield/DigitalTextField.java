package cn.oyzh.fx.plus.controls.textfield;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.plus.converter.DigitalFormatStringConverter;
import cn.oyzh.fx.plus.skin.DigitalTextFieldSkin;
import javafx.scene.control.Skin;
import javafx.scene.control.TextFormatter;
import lombok.Getter;
import lombok.Setter;

import java.util.function.UnaryOperator;

/**
 * 数字文本输入框
 *
 * @author oyzh
 * @since 2023/12/27
 */
public abstract class DigitalTextField extends LimitTextField {

    /**
     * 最大值
     */
    @Setter
    @Getter
    protected Number maxVal;

    /**
     * 最小值
     */
    @Setter
    @Getter
    protected Number minVal;

    /**
     * 递进值
     */
    @Getter
    @Setter
    protected Number step = 1L;

    /**
     * 无符号模式
     */
    @Getter
    @Setter
    private boolean unsigned;

    /**
     * 文本格式器
     */
    protected final TextFormatter<String> textFormatter;

    public DigitalTextField(boolean unsigned, Long maxLen) {
        // 创建文本格式化器
        this.textFormatter = new TextFormatter<>(this.getConverter(), null, this.createFilter());
        // 将TextFormatter对象设置到文本字段中
        this.setTextFormatter(this.textFormatter);
        // 监听值变化
        this.textFormatter.valueProperty().addListener((observableValue, number, t1) -> this.valueChanged(t1));
        this.setMaxLen(maxLen);
        this.setUnsigned(unsigned);
    }

    /**
     * 当前皮肤
     *
     * @return 皮肤
     */
    public DigitalTextFieldSkin skin() {
        DigitalTextFieldSkin skin = (DigitalTextFieldSkin) this.getSkin();
        if (skin == null) {
            this.setSkin(this.createDefaultSkin());
            skin = (DigitalTextFieldSkin) this.getSkin();
        }
        return skin;
    }

    protected abstract DigitalFormatStringConverter getConverter();

    /**
     * 值变化
     *
     * @param newVal 新值
     */
    protected void valueChanged(String newVal) {
        // 检查新值是否有效
        if (StrUtil.isNotBlank(newVal)) {
            // 获取当前皮肤
            DigitalTextFieldSkin skin =  this.skin();
            // 解析值
            Number number = NumberUtil.parseNumber(newVal);
            // 判断新值是否小于等于最小值，如果是则禁用减号按钮
            if (this.minVal != null && number.doubleValue() <= this.minVal.doubleValue()) {
                skin.disableDecrButton();
            } else {
                // 否则启用减号按钮
                skin.enableDecrButton();
            }
            // 判断新值是否大于等于最大值，如果是则禁用加号按钮
            if (this.maxVal != null && number.doubleValue() >= this.maxVal.doubleValue()) {
                skin.disableIncrButton();
            } else {
                // 否则启用加号按钮
                skin.enableIncrButton();
            }
        }
    }

    /**
     * 获取byte值
     *
     * @return byte值
     */
    public Byte getByteValue() {
        Number val = this.value();
        return val == null ? null : val.byteValue();
    }

    /**
     * 获取short值
     *
     * @return short值
     */
    public Short getShortValue() {
        Number val = this.value();
        return val == null ? null : val.shortValue();
    }

    /**
     * 获取int值
     *
     * @return int值
     */
    public Integer getIntValue() {
        Number val = this.value();
        return val == null ? null : val.intValue();
    }

    /**
     * 获取long值
     *
     * @return long值
     */
    public Long getLongValue() {
        Number val = this.value();
        return val == null ? null : val.longValue();
    }

    /**
     * 获取float值
     *
     * @return float值
     */
    public Float getFloatValue() {
        Number val = this.value();
        return val == null ? null : val.floatValue();
    }

    /**
     * 获取double值
     *
     * @return double值
     */
    public Double getDoubleValue() {
        Number val = this.value();
        return val == null ? null : val.doubleValue();
    }

    /**
     * 增加值
     */
    protected abstract void incrValue();

    /**
     * 减少值
     */
    protected abstract void decrValue();

    /**
     * 创建一个过滤器，用于限制文本输入
     *
     * @return 过滤器
     */
    protected abstract UnaryOperator<TextFormatter.Change> createFilter();

    /**
     * 获取值
     *
     * @return 值
     */
    protected Number value() {
        // 否则，将文本转为Double类型并返回
        return NumberUtil.parseNumber(this.getText());
    }

    /**
     * 设置值，如果超出最大值或最小值，将设置为最大值或最小值
     *
     * @param value 值
     */
    protected void value(Number value) {
        if (value != null) {
            if (this.maxVal != null && value.doubleValue() > this.maxVal.doubleValue()) {
                value = this.maxVal;
            } else if (this.minVal != null && value.doubleValue() < this.minVal.doubleValue()) {
                value = this.minVal;
            }
            this.textFormatter.setValue(value.toString());
        }
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof Number number) {
            this.value(number);
        } else if (value instanceof CharSequence sequence) {
            this.value(NumberUtil.parseNumber(sequence.toString()));
        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new DigitalTextFieldSkin(this, this::incrValue, this::decrValue);
    }
}
