package cn.oyzh.fx.gui.text.field;


import cn.oyzh.common.util.RegexUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.converter.DigitalConverter;
import cn.oyzh.fx.plus.format.DigitalFormat;
import javafx.scene.control.TextFormatter;

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
    protected Integer scaleLen;

    /**
     * 获取小数位数
     *
     * @return 小数位数
     */
    public Integer getScaleLen() {
        return scaleLen;
    }

    /**
     * 设置小数位数
     *
     * @param scaleLen 小数位数
     */
    public void setScaleLen(Integer scaleLen) {
        this.scaleLen = scaleLen;
        this.format();
    }

    /**
     * 构造小数文本域
     */
    public DecimalTextField() {
        super(null);
    }

    //    public DecimalTextField(boolean unsigned) {
    //        super(unsigned, null);
    //    }

    /**
     * 构造小数文本域
     *
     * @param maxLen 最大长度
     */
    public DecimalTextField(Long maxLen) {
        super(maxLen);
    }

    /**
     * 构造小数文本域
     *
     * @param maxLen   最大长度
     * @param minVal   最小值
     * @param maxVal   最大值
     * @param scaleLen 小数位数
     */
    public DecimalTextField(Long maxLen, Long minVal, Long maxVal, Integer scaleLen) {
        super(maxLen);
        super.setMinVal(minVal);
        super.setMaxVal(maxVal);
        this.setScaleLen(scaleLen);
    }

    /**
     * 构造小数文本域
     *
     * @param maxLen   最大长度
     * @param scaleLen 小数位数
     */
    public DecimalTextField(Long maxLen, Integer scaleLen) {
        super(maxLen);
        this.setScaleLen(scaleLen);
    }

    /**
     * 数字转换器
     */
    private DigitalConverter converter;

    @Override
    protected DigitalConverter getConverter() {
        if (this.converter == null) {
            this.converter = new DigitalConverter(this.format());
        }
        return this.converter;
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
                    // 如果文本为空、"+"、"-"、"."，则不进行任何操作，直接返回原change对象
                    if (StringUtil.isEmpty(text) || StringUtil.equalsAny(text, "+", "-", ".")) {
                        return change;
                    }
                    // // 无符号判断
                    // if (this.isUnsigned() && text.startsWith("-")) {
                    //     return null;
                    // }
                    // 数字判断
                    if (!RegexUtil.isDecimal(text) && !RegexUtil.isNumber(text)) {
                        return null;
                    }
                    // 长度判断
                    if (!super.checkLenLimit(change)) {
                        return null;
                    }
                    // 判断小数位数
                    BigDecimal decimal = new BigDecimal(text);
                    // 如果小数位数超过了设定的最大位数，则将小数转换为最大位数，并设置组件值为转换后的结果
                    if (this.scaleLen != null && decimal.scale() > this.scaleLen) {
                        this.setValue(decimal.setScale(this.scaleLen, RoundingMode.HALF_UP).doubleValue());
                        return null;
                    }
                    // // 如果超过了最大值，则将组件值设置为最大值
                    // if (this.maxVal != null && cn.oyzh.common.util.NumberUtil.isGT(decimal.doubleValue(), this.maxVal)) {
                    //     this.setValue(this.maxVal.doubleValue());
                    //     return null;
                    // }
                    // // 如果小于了最小值，则将组件值设置为最小值
                    // if (this.minVal != null && cn.oyzh.common.util.NumberUtil.isLT(decimal.doubleValue(), this.minVal)) {
                    //     this.setValue(this.minVal.doubleValue());
                    //     return null;
                    // }
                } catch (Exception ignored) {
                }
            }
            return change;
        };
    }

    @Override
    public Double getValue() {
        Number val = this.value();
        // 否则，将文本转为Double类型并返回
        return val == null ? null : val.doubleValue();
    }

    @Override
    public Number value() {
        // 获取文本内容
        String text = this.getText();
        // 如果文本为空，或者为"-"，或者为"+"，或者为"."，则返回0.D
        if (StringUtil.equalsAny(text, "", "-", "+", ".")) {
            return 0D;
        }
        return super.value();
    }

    /**
     * 数字格式化器
     */
    private DigitalFormat format;

    /**
     * 获取数字格式化器，并按当前小数位数同步配置。
     *
     * @return 数字格式化器
     */
    private DigitalFormat format() {
        if (this.format == null) {
            this.format = new DigitalFormat(this.scaleLen);
        } else {
            this.format.setScaleLen(this.scaleLen);
        }
        return this.format;
    }

    /**
     * 将值格式化为小数字符串
     *
     * @param val 值
     * @return 小数字符串
     */
    public static String format(Object val) {
        if (val instanceof CharSequence sequence) {
            return sequence.toString();
        }
        if (val instanceof Number number) {
            return number.doubleValue() + "";
        }
        return null;
    }

    /**
     * 设置最小值
     *
     * @param minVal 最小值
     */
    public void setMin(Double minVal) {
        this.minVal = minVal;
    }

    /**
     * 获取最小值
     *
     * @return 最小值
     */
    public Double getMin() {
        return this.minVal == null ? null : this.minVal.doubleValue();
    }

    /**
     * 设置最大值
     *
     * @param maxVal 最大值
     */
    public void setMax(Double maxVal) {
        this.maxVal = maxVal;
    }

    /**
     * 获取最大值
     *
     * @return 最大值
     */
    public Double getMax() {
        return this.maxVal == null ? null : this.maxVal.doubleValue();
    }
}
