package cn.oyzh.fx.plus.controls.textfield;

import cn.hutool.core.util.NumberUtil;
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
public abstract class DigitalTextField extends FlexTextField {

    /**
     * 最大值
     */
    @Getter
    @Setter
    protected Number max;

    /**
     * 最小值
     */
    @Getter
    @Setter
    protected Number min;

    /**
     * 递进值
     */
    @Getter
    @Setter
    protected Number step = 1L;

    /**
     * 默认值
     */
    @Getter
    @Setter
    protected Number defVal;

    /**
     * 获取byte值
     *
     * @return byte值
     */
    public abstract Byte getByteValue();

    /**
     * 获取short值
     *
     * @return short值
     */
    public abstract Short getShortValue();

    /**
     * 获取integer值
     *
     * @return integer值
     */
    public abstract Integer getIntegerValue();

    /**
     * 获取long值
     *
     * @return long值
     */
    public abstract Long getLongValue();

    /**
     * 获取float值
     *
     * @return float值
     */
    public abstract Float getFloatValue();

    /**
     * 获取double值
     *
     * @return double值
     */
    public abstract Double getDoubleValue();

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
     * 设置默认值
     *
     * @param defVal 默认值
     */
    public void defVal(Object defVal) {
        try {
            if (defVal instanceof Number number) {
                this.defVal = number;
            } else if (defVal instanceof CharSequence sequence) {
                this.defVal = NumberUtil.parseNumber(sequence.toString());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
