package cn.oyzh.fx.plus.ext;

import java.security.InvalidParameterException;

/**
 * @author oyzh
 * @since 2020/10/29
 */
public class NumberTextField extends BaseNumberTextField {

    @Override
    protected void incrValue() {
        // 获取当前值
        long val = this.valueProperty.getValue().longValue() + this.getStep();
        // 计算值
        if (this.max != null) {
            val = Math.min(val, this.max.longValue());
        }
        // 设置值
        this.textFormatter.setValue(val);
    }

    @Override
    protected void decrValue() {
        // 获取当前值
        long val = this.valueProperty.getValue().longValue() - this.getStep();
        // 计算值
        if (this.min != null) {
            val = Math.max(val, this.min.longValue());
        }
        // 设置值
        this.textFormatter.setValue(val);
    }

    /**
     * 获取值
     *
     * @return 值
     */
    public long getValue() {
        String text = this.getTextTrim();
        long value = this.valueProperty.getValue().longValue();
        if (text != null && text.length() != value + "".length()) {
            return this.converter.fromString(this.getTextTrim()).longValue();
        }
        return value;
    }

    /**
     * 设置值，如果超出最大值或最小值，将设置为最大值或最小值
     *
     * @param value 值
     */
    public void setValue(long value) {
        if (this.max != null && value > this.max.longValue()) {
            value = this.max.longValue();
        } else if (this.min != null && value < this.min.longValue()) {
            value = this.min.longValue();
        }
        this.textFormatter.setValue(value);
    }

    /**
     * 获取最大值
     */
    public Long getMax() {
        if (this.max == null) {
            return null;
        }
        return this.max.longValue();
    }

    /**
     * 设置最大值，如果当前的数字值超出最大值，将设置为最大值
     *
     * @param max 最大值
     */
    public void setMax(Long max) {
        if (max != null) {
            if (this.min != null && max < this.min.longValue()) {
                throw new InvalidParameterException("max不能小于min！");
            }
            if (this.valueProperty.getValue().longValue() > max) {
                this.textFormatter.setValue(max);
            }
        }
        this.max = max;
    }

    /**
     * 获取最小值
     */
    public Long getMin() {
        if (this.min == null) {
            return null;
        }
        return this.min.longValue();
    }

    /**
     * 设置最小值，如果当前的数字值超出最小值，将设置为最小值
     *
     * @param min 最小值
     */
    public void setMin(Long min) {
        if (min != null) {
            if (this.max != null && min > this.max.longValue()) {
                throw new InvalidParameterException("min不能大于max！");
            }
            if (this.valueProperty.getValue().longValue() < min) {
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
    public long getStep() {
        return this.step.longValue();
    }

    /**
     * 设置递进值
     *
     * @param step 递进值
     */
    public void setStep(long step) {
        this.step = step;
    }
}
