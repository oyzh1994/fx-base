package cn.oyzh.fx.gui.text.field;

import cn.oyzh.common.util.RegexUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.converter.DigitalConverter;
import javafx.scene.control.TextFormatter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.UnaryOperator;

/**
 * 整数文本域
 *
 * @author oyzh
 * @since 2023-10-09
 */
public class NumberTextField extends DigitalTextField {

    /**
     * 构造整数文本域
     */
    public NumberTextField() {
        super(null);
    }

    /**
     * 构造整数文本域
     *
     * @param maxLen 最大长度
     */
    public NumberTextField(Long maxLen) {
        super(maxLen);
    }

    /**
     * 构造整数文本域
     *
     * @param maxLen 最大长度
     * @param minVal 最小值
     * @param maxVal 最大值
     */
    public NumberTextField(Long maxLen, Long minVal, Long maxVal) {
        super(maxLen);
        this.setMin(minVal);
        this.setMax(maxVal);
    }

    /**
     * 数字转换器
     */
    private DigitalConverter converter;

    @Override
    protected DigitalConverter getConverter() {
        if (this.converter == null) {
            this.converter = new DigitalConverter();
        }
        return this.converter;
    }

    @Override
    protected void incrValue() {
        // 设置值
        BigInteger val = this.getValue();
        if (this.step != null && val != null) {
            this.setValue(val.add(new BigInteger(this.step.toString())));
        }
    }

    @Override
    protected void decrValue() {
        // 设置值
        BigInteger val = this.getValue();
        if (this.step != null && val != null) {
            this.setValue(val.subtract(BigInteger.valueOf(this.step.longValue())));
        }
    }

    @Override
    protected UnaryOperator<TextFormatter.Change> createFilter() {
        return change -> {
            if (change.isAdded() || change.isReplaced() || change.isContentChange()) {
                try {
                    String text = change.getControlNewText();
                    // 如果文本为空、"+"、"-"，则不进行任何操作，直接返回原change对象
                    if (StringUtil.isEmpty(text) || StringUtil.equalsAny(text, "+", "-")) {
                        return change;
                    }
                    // 数字判断
                    if (!RegexUtil.isNumber(text)) {
                        return null;
                    }
                    // 长度判断
                    if (!super.checkLenLimit(change)) {
                        return null;
                    }
                } catch (Exception ignored) {
                }
            }
            return change;
        };
    }

    @Override
    public BigInteger getValue() {
        Number val = this.value();
        if (val == null) {
            return null;
        }
        if (val instanceof BigInteger integer) {
            return integer;
        }
        return new BigInteger(val.toString());
    }

    @Override
    protected Number value() {
        String text = this.getText();
        if (StringUtil.equalsAny(text, "", "+", "-")) {
            return null;
        }
        return super.value();
    }

    /**
     * 将值格式化为整数字符串
     *
     * @param val 值
     * @return 整数字符串
     */
    public static String format(Object val) {
        if (val instanceof CharSequence sequence) {
            return sequence.toString();
        }
        if (val instanceof Number number) {
            return new BigDecimal(number.toString()).stripTrailingZeros().toPlainString();
        }
        return null;
    }

    /**
     * 设置最小值
     *
     * @param minVal 最小值
     */
    public void setMin(Long minVal) {
        if (minVal != null) {
            this.minVal = new BigInteger(minVal.toString());
        } else {
            this.minVal = null;
        }
    }

    /**
     * 获取最小值
     *
     * @return 最小值
     */
    public Long getMin() {
        return this.minVal == null ? null : this.minVal.longValue();
    }

    /**
     * 设置最大值
     *
     * @param maxVal 最大值
     */
    public void setMax(Long maxVal) {
        if (maxVal != null) {
            this.maxVal = new BigInteger(maxVal.toString());
        } else {
            this.maxVal = null;
        }
    }

    /**
     * 获取最大值
     *
     * @return 最大值
     */
    public Long getMax() {
        return this.maxVal == null ? null : this.maxVal.longValue();
    }
}
