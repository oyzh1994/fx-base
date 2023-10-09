package cn.oyzh.fx.plus.ext;

import java.security.InvalidParameterException;

/**
 * @author oyzh
 * @since 2023/08/28
 */
public class DecimalTextField extends BaseNumberTextField {

    @Override
    protected void incrValue() {
        // 获取当前值
        double val = this.valueProperty.getValue().doubleValue() + this.getStep();
        // 计算值
        if (this.max != null) {
            val = Math.min(val, this.max.doubleValue());
        }
        // 设置值
        this.textFormatter.setValue(val);
    }

    @Override
    protected void decrValue() {
        // 获取当前值
        double val = this.valueProperty.getValue().doubleValue() - this.getStep();
        // 计算值
        if (this.min != null) {
            val = Math.max(val, this.min.doubleValue());
        }
        // 设置值
        this.textFormatter.setValue(val);
    }

    /**
     * 获取值
     *
     * @return 值
     */
    public double getValue() {
        String text = this.getTextTrim();
        double value = this.valueProperty.getValue().doubleValue();
        if (text != null && text.length() != value + "".length()) {
            return this.converter.fromString(this.getTextTrim()).doubleValue();
        }
        return value;
    }

    /**
     * 设置值，如果超出最大值或最小值，将设置为最大值或最小值
     *
     * @param value 值
     */
    public void setValue(double value) {
        if (this.max != null && value > this.max.doubleValue()) {
            value = this.max.doubleValue();
        } else if (this.min != null && value < this.min.doubleValue()) {
            value = this.min.doubleValue();
        }
        this.textFormatter.setValue(value);
    }

    /**
     * 获取最大值
     */
    public Double getMax() {
        if (this.max == null) {
            return null;
        }
        return this.max.doubleValue();
    }

    /**
     * 设置最大值，如果当前的数字值超出最大值，将设置为最大值
     *
     * @param max 最大值
     */
    public void setMax(Double max) {
        if (max != null) {
            if (this.min != null && max < this.min.doubleValue()) {
                throw new InvalidParameterException("max不能小于min！");
            }
            if (this.valueProperty.getValue().doubleValue() > max) {
                this.textFormatter.setValue(max);
            }
        }
        this.max = max;
    }

    /**
     * 获取最小值
     */
    public Double getMin() {
        if (this.min == null) {
            return null;
        }
        return this.min.doubleValue();
    }

    /**
     * 设置最小值，如果当前的数字值超出最小值，将设置为最小值
     *
     * @param min 最小值
     */
    public void setMin(Double min) {
        if (min != null) {
            if (this.max != null && min > this.max.doubleValue()) {
                throw new InvalidParameterException("min不能大于max！");
            }
            if (this.valueProperty.getValue().doubleValue() < min) {
                this.textFormatter.setValue(min);
            }
        }
        this.min = min;
    }

    /**
     * 获取递进值
     *
     * @return 递进值
     */
    public double getStep() {
        return this.step.doubleValue();
    }

    /**
     * 设置递进值
     *
     * @param step 递进值
     */
    public void setStep(double step) {
        this.step = step;
    }
}
