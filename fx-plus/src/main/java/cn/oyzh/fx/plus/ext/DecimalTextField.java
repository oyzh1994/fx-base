package cn.oyzh.fx.plus.ext;

/**
 * 小数文本域
 *
 * @author oyzh
 * @since 2023/08/28
 */
public class DecimalTextField extends BaseNumberTextField {

    public DecimalTextField() {
        super(true);
    }

    /**
     * 获取值
     *
     * @return 值
     */
    public double getValue() {
        return super._getValue().doubleValue();
    }

    /**
     * 设置值，如果超出最大值或最小值，将设置为最大值或最小值
     *
     * @param value 值
     */
    public void setValue(double value) {
        super.setValue(value);
    }

    /**
     * 获取最大值
     */
    public Double getMax() {
        return this.max == null ? null : this.max.doubleValue();
    }

    /**
     * 设置最大值，如果当前的数字值超出最大值，将设置为最大值
     *
     * @param max 最大值
     */
    public void setMax(Double max) {
        super.setMax(max);
    }

    /**
     * 获取最小值
     */
    public Double getMin() {
        return this.min == null ? null : this.min.doubleValue();
    }

    /**
     * 设置最小值，如果当前的数字值超出最小值，将设置为最小值
     *
     * @param min 最小值
     */
    public void setMin(Double min) {
        super.setMin(min);
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
