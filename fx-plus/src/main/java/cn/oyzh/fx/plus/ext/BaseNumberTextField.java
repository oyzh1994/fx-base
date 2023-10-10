package cn.oyzh.fx.plus.ext;

import cn.hutool.core.util.NumberUtil;
import cn.oyzh.fx.plus.controls.BaseTextField;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.NumberStringConverter;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.security.InvalidParameterException;
import java.util.function.UnaryOperator;

/**
 * @author oyzh
 * @since 2020/10/29
 */
public abstract class BaseNumberTextField extends BaseTextField {

    /**
     * 最大值
     */
    protected Number max;

    /**
     * 最小值
     */
    protected Number min;

    /**
     * 递进值
     */
    protected Number step;

    /**
     * 是否小数形式
     */
    protected final boolean decimal;

    /**
     * 当前值
     */
    @Getter
    @Accessors(fluent = true)
    protected ObjectProperty<Number> valueProperty;

    /**
     * 数据转换器
     */
    protected final NumberStringConverter converter;

    /**
     * 文本格式器
     */
    protected final TextFormatter<Number> textFormatter;

    {
        this.setSkin(new NumberTextFieldSkin(this, this::incrValue, this::decrValue));
    }

    // 构造函数
    public BaseNumberTextField(boolean decimal) {
        this.decimal = decimal;
        // 初始化转换器
        this.converter = new NumberStringConverterExt();
        // 创建TextFormatter对象
        if (decimal) {
            this.textFormatter = new TextFormatter<>(this.converter, 0.D, this.createFilter());
            this.max = Double.MAX_VALUE;
            this.min = Double.MIN_VALUE;
            this.step = 1.D;
        } else {
            this.textFormatter = new TextFormatter<>(this.converter, 0L, this.createFilter());
            this.max = Long.MAX_VALUE;
            this.min = Long.MIN_VALUE;
            this.step = 1L;
        }
        // 将TextFormatter对象设置到文本字段中
        this.setTextFormatter(this.textFormatter);
        // 属性值
        this.valueProperty = this.textFormatter.valueProperty();
        // 监听值变化
        this.valueProperty.addListener((observableValue, number, t1) -> this.valueChanged(t1));
    }

    /**
     * 值变化
     *
     * @param newVal 新值
     */
    protected void valueChanged(Number newVal) {
        if (newVal != null) {
            NumberTextFieldSkin skin = (NumberTextFieldSkin) this.getSkin();
            if (this.decimal) {
                if (newVal.doubleValue() <= this.min.doubleValue()) {
                    skin.disableDecrButton();
                } else {
                    skin.enableDecrButton();
                }
                if (newVal.doubleValue() >= this.max.doubleValue()) {
                    skin.disableIncrButton();
                } else {
                    skin.enableIncrButton();
                }
            } else {
                if (newVal.doubleValue() <= this.min.longValue()) {
                    skin.disableDecrButton();
                } else {
                    skin.enableDecrButton();
                }
                if (newVal.doubleValue() >= this.max.longValue()) {
                    skin.disableIncrButton();
                } else {
                    skin.enableIncrButton();
                }
            }
        }
    }

    /**
     * 获取值，内部
     *
     * @return 值
     */
    protected Number _getValue() {
        String text = this.getTextTrim();
        if (text != null) {
            if (this.decimal) {
                return this.converter.fromString(text).doubleValue();
            }
            return this.converter.fromString(text).longValue();
        }
        if (this.decimal) {
            return this.valueProperty.getValue().doubleValue();
        }
        return this.valueProperty.getValue().longValue();
    }

    /**
     * 设置值
     *
     * @param value 值
     */
    public void setValue(Number value) {
        if (this.max != null && value.doubleValue() > this.max.doubleValue()) {
            value = this.max;
        } else if (this.min != null && value.doubleValue() < this.min.doubleValue()) {
            value = this.min;
        }
        if (this.decimal) {
            this.textFormatter.setValue(value.doubleValue());
        } else {
            this.textFormatter.setValue(value.longValue());
        }
    }

    /**
     * 设置最大值，如果当前的数字值超出最大值，将设置为最大值
     *
     * @param max 最大值
     */
    public void setMax(Number max) {
        if (this.decimal) {
            if (this.min != null && max.doubleValue() < this.min.doubleValue()) {
                throw new InvalidParameterException("max不能小于min！");
            }
        } else {
            if (this.min != null && max.longValue() < this.min.longValue()) {
                throw new InvalidParameterException("max不能小于min！");
            }
        }
        this.max = max;
        this.setValue(max);
    }

    /**
     * 设置最小值，如果当前的数字值超出最小值，将设置为最小值
     *
     * @param min 最小值
     */
    public void setMin(Number min) {
        if (this.decimal) {
            if (this.max != null && min.doubleValue() > this.max.doubleValue()) {
                throw new InvalidParameterException("min不能大于max！");
            }
        } else {
            if (this.max != null && min.longValue() > this.max.longValue()) {
                throw new InvalidParameterException("min不能大于max！");
            }
        }
        this.min = min;
        this.setValue(min);
    }

    /**
     * 增加值
     */
    protected void incrValue() {
        if (this.decimal) {
            // 获取当前值
            double val = this._getValue().doubleValue() + this.step.doubleValue();
            // 设置值
            this.setValue(val);
        } else {
            // 获取当前值
            long val = this._getValue().longValue() + this.step.longValue();
            // 设置值
            this.setValue(val);
        }
    }

    /**
     * 减少值
     */
    protected void decrValue() {
        if (this.decimal) {
            // 获取当前值
            double val = this._getValue().doubleValue() - this.step.doubleValue();
            // 设置值
            this.setValue(val);
        } else {
            // 获取当前值
            long val = this._getValue().longValue() - this.step.longValue();
            // 计算值
            if (this.min != null) {
                val = Math.max(val, this.min.longValue());
            }
            // 设置值
            this.setValue(val);
        }
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
                    // 无内容、符号直接返回
                    if (text.isEmpty() || text.equals("+") || text.equals("-")) {
                        return change;
                    }
                    // 包含空格、以0开头，直接返回null
                    if (text.contains(" ") || text.startsWith("0")) {
                        return null;
                    }
                    // 检查文本
                    Number val = NumberUtil.parseNumber(text);
                    // 判断最大值
                    if (this.max != null && val.doubleValue() > this.max.doubleValue()) {
                        this.setValue(this.max);
                        return null;
                    }
                    // 判断最小值
                    if (this.min != null && val.doubleValue() < this.min.doubleValue()) {
                        this.setValue(this.min);
                        return null;
                    }
                } catch (Exception ignored) {
                    return null;
                }
            }
            return change;
        };
    }
}
