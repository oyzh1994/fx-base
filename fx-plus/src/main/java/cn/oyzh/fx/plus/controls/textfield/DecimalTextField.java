package cn.oyzh.fx.plus.controls.textfield;


import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.plus.converter.DoubleConverter;
import cn.oyzh.fx.plus.skin.NumberTextFieldSkin;
import javafx.beans.value.WeakChangeListener;
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
public class DecimalTextField extends FlexTextField {

    /**
     * 最大值
     */
    @Getter
    @Setter
    protected Double max;

    /**
     * 最小值
     */
    @Getter
    @Setter
    protected Double min;

    /**
     * 递进值
     */
    @Getter
    @Setter
    protected Double step = 1.0D;

    /**
     * 小数位数
     */
    @Setter
    @Getter
    protected Integer scaleLen;

    /**
     * 数据转换器
     */
    protected final DoubleConverter converter = new DoubleConverter();

    /**
     * 文本格式器
     */
    protected final TextFormatter<Double> textFormatter = new TextFormatter<>(this.converter, 0.d, this.createFilter());

    {
        this.setSkin(new NumberTextFieldSkin(this, this::incrValue, this::decrValue));
    }

    /**
     * 增加值
     */
    protected void incrValue() {
        // 设置值
        if (this.step != null) {
            this.setValue(this.getValue() + this.step);
        }
    }

    /**
     * 减少值
     */
    protected void decrValue() {
        // 设置值
        if (this.step != null) {
            this.setValue(this.getValue() - this.step);
        }
    }

    public DecimalTextField() {
        // 将TextFormatter对象设置到文本字段中
        this.setTextFormatter(this.textFormatter);
        // 监听值变化
        this.textFormatter.valueProperty().addListener(new WeakChangeListener<>((observableValue, number, t1) -> this.valueChanged(t1)));
    }

    /**
     * 创建一个过滤器，用于限制文本输入
     *
     * @return 过滤器
     */
    protected UnaryOperator<TextFormatter.Change> createFilter() {
        return change -> {
            if (change.isAdded() || change.isReplaced() || change.isContentChange()) {
                try {
                    String text = change.getControlNewText();
                    if (StrUtil.isEmpty(text)) {
                        this.setValue(0.d);
                        return null;
                    }
                    // 判断数字
                    if (!NumberUtil.isDouble(text) && !NumberUtil.isNumber(text)) {
                        return null;
                    }
                    // 判断小数位数
                    BigDecimal decimal = new BigDecimal(text);
                    if (this.scaleLen != null && decimal.scale() > this.scaleLen) {
                        this.setValue(decimal.setScale(this.scaleLen, RoundingMode.HALF_UP).doubleValue());
                        return null;
                    }
                    // 判断最大值
                    if (this.max != null && decimal.doubleValue() > this.max) {
                        this.setValue(this.max);
                        return null;
                    }
                    // 判断最小值
                    if (this.min != null && decimal.doubleValue() < this.min) {
                        this.setValue(this.min);
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
        if (newVal != null && !Double.isNaN(newVal)) {
            NumberTextFieldSkin skin = (NumberTextFieldSkin) this.getSkin();
            if (this.min != null && newVal <= this.min) {
                skin.disableDecrButton();
            } else {
                skin.enableDecrButton();
            }
            if (this.max != null && newVal >= this.max) {
                skin.disableIncrButton();
            } else {
                skin.enableIncrButton();
            }
        }
    }

    /**
     * 获取值
     *
     * @return 值
     */
    public Double getValue() {
        return NumberUtil.parseDouble(this.getText());
    }

    /**
     * 设置值，如果超出最大值或最小值，将设置为最大值或最小值
     *
     * @param value 值
     */
    public void setValue(Double value) {
        if (value != null && !Double.isNaN(value)) {
            if (this.max != null && value > this.max) {
                value = this.max;
            } else if (this.min != null && value < this.min) {
                value = this.min;
            } else if (this.scaleLen != null) {
                BigDecimal decimal = new BigDecimal(value);
                if (decimal.scale() > this.scaleLen) {
                    value = decimal.setScale(this.scaleLen, RoundingMode.HALF_UP).doubleValue();
                }
            }
            this.textFormatter.setValue(value);
        }
    }
}
