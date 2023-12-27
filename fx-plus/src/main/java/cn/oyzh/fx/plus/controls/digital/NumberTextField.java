package cn.oyzh.fx.plus.controls.digital;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.plus.controls.digital.DigitalTextField;
import cn.oyzh.fx.plus.converter.LongConverter;
import cn.oyzh.fx.plus.skin.NumberTextFieldSkin;
import javafx.scene.control.TextFormatter;

import java.util.function.UnaryOperator;

/**
 * 整数文本域
 *
 * @author oyzh
 * @since 2020/10/29
 */
public class NumberTextField extends DigitalTextField {

    /**
     * 文本格式器
     */
    protected final TextFormatter<Long> textFormatter;

    /**
     * 数据转换器
     */
    protected final LongConverter converter = new LongConverter();

    public NumberTextField() {
        this.setSkin(new NumberTextFieldSkin(this, this::incrValue, this::decrValue));
        if (this.defVal != null) {
            this.textFormatter = new TextFormatter<>(this.converter, this.defVal.longValue(), this.createFilter());
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
                    if (StrUtil.isEmpty(text) || text.equals("-") || text.equals("+")) {
                        return change;
                    }
                    // 判断数字
                    Long l = this.converter.fromString(text);
                    if (l == null) {
                        return null;
                    }
                    // 判断最大值
                    if (this.max != null && l > this.max.longValue()) {
                        this.setValue(this.max.longValue());
                        return null;
                    }
                    // 判断最小值
                    if (this.min != null && l < this.min.longValue()) {
                        this.setValue(this.min.longValue());
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
    protected void valueChanged(Long newVal) {
        if (newVal != null) {
            NumberTextFieldSkin skin = (NumberTextFieldSkin) this.getSkin();
            if (this.min != null && newVal <= this.min.longValue()) {
                skin.disableDecrButton();
            } else {
                skin.enableDecrButton();
            }
            if (this.max != null && newVal >= this.max.longValue()) {
                skin.disableIncrButton();
            } else {
                skin.enableIncrButton();
            }
        }
    }

    @Override
    public Byte getByteValue() {
        Long val = this.getValue();
        return val == null ? null : val.byteValue();
    }

    @Override
    public Short getShortValue() {
        Long val = this.getValue();
        return val == null ? null : val.shortValue();
    }

    @Override
    public Integer getIntegerValue() {
        Long val = this.getValue();
        return val == null ? null : val.intValue();
    }

    @Override
    public Long getLongValue() {
        return this.getValue();
    }

    @Override
    public Float getFloatValue() {
        Long val = this.getValue();
        return val == null ? null : val.floatValue();
    }

    @Override
    public Double getDoubleValue() {
        Long val = this.getValue();
        return val == null ? null : val.doubleValue();
    }

    /**
     * 获取值
     *
     * @return 值
     */
    public Long getValue() {
        String text = this.getText();
        if (StrUtil.isEmpty(text) || "-".equals(text) || "+".equals(text)) {
            return 0L;
        }
        return NumberUtil.parseLong(text);
    }

    /**
     * 设置值，如果超出最大值或最小值，将设置为最大值或最小值
     *
     * @param value 值
     */
    public void setValue(Long value) {
        if (value != null) {
            if (this.max != null && value > this.max.longValue()) {
                value = this.max.longValue();
            } else if (this.min != null && value < this.min.longValue()) {
                value = this.min.longValue();
            }
            this.textFormatter.setValue(value);
        }
    }

    /**
     * 设置值，如果超出最大值或最小值，将设置为最大值或最小值
     *
     * @param value 值
     */
    public void setValue(long value) {
        this.setValue((Long) value);
    }

    @Override
    public void setDefVal(Number defVal) {
        super.setDefVal(defVal);
        if (this.getValue() == null && defVal != null) {
            this.setValue(defVal.longValue());
        }
    }

    @Override
    public void defVal(Object defVal) {
        super.defVal(defVal);
        if (this.getValue() == null && super.defVal != null) {
            this.setValue(super.defVal.longValue());
        }
    }
}
