package cn.oyzh.fx.plus.ext;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import javafx.scene.control.TextFormatter;

import java.security.InvalidParameterException;
import java.util.function.UnaryOperator;

/**
 * 整数文本域
 *
 * @author oyzh
 * @since 2020/10/29
 */
public class NumberTextField extends BaseNumberTextField {

    public NumberTextField() {
        super(false);
    }

    /**
     * 获取值
     *
     * @return 值
     */
    public long getValue() {
        return super._getValue().longValue();
    }

    /**
     * 设置值，如果超出最大值或最小值，将设置为最大值或最小值
     *
     * @param value 值
     */
    public void setValue(long value) {
        super.setValue(value);
    }

    /**
     * 获取最大值
     */
    public Long getMax() {
        return this.max == null ? null : this.max.longValue();
    }

    /**
     * 设置最大值，如果当前的数字值超出最大值，将设置为最大值
     *
     * @param max 最大值
     */
    public void setMax(Long max) {
        super.setMax(max);
    }

    /**
     * 获取最小值
     */
    public Long getMin() {
        return this.min == null ? null : this.min.longValue();
    }

    /**
     * 设置最小值，如果当前的数字值超出最小值，将设置为最小值
     *
     * @param min 最小值
     */
    public void setMin(Long min) {
        super.setMin(min);
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
