package cn.oyzh.fx.plus.format;

import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.common.util.StringUtil;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.util.Objects;

/**
 * 数字格式化器，支持按指定小数位数格式化数字，并去除千分位分隔符
 *
 * @author oyzh
 * @since 2024/5/15
 */
public class DigitalFormat extends DecimalFormat {

    /**
     * 保留小数位数
     */
    private Integer scaleLen;

    /**
     * 获取保留小数位数
     *
     * @return 保留小数位数
     */
    public Integer getScaleLen() {
        return scaleLen;
    }

    /**
     * 底层格式化器
     */
    private DecimalFormat format;

    /**
     * 构造数字格式对象。
     *
     * @param scaleLen 缩放Len
     */
    public DigitalFormat(Integer scaleLen) {
        this.setScaleLen(scaleLen);
    }

    /**
     * 构造数字格式对象。
     */
    public DigitalFormat() {
        this.setScaleLen(-1);
    }

    /**
     * 设置保留小数位数
     *
     * @param scaleLen 保留小数位数
     */
    public void setScaleLen(Integer scaleLen) {
        if (!Objects.equals(scaleLen, this.scaleLen)) {
            this.format = null;
        }
        this.scaleLen = scaleLen;
    }

    /**
     * 获取底层格式化器
     *
     * @return 底层格式化器
     */
    protected DecimalFormat format() {
        if (this.format == null) {
            if (scaleLen == null || scaleLen <= 0) {
                this.format = new DecimalFormat();
            } else {
                this.format = new DecimalFormat("0." + "0".repeat(this.scaleLen));
            }
        }
        return this.format;
    }

    /**
     * 格式化字符串数字
     *
     * @param sequence 字符串数字
     * @return 格式化结果
     */
    public String format(String sequence) {
        if (StringUtil.isNotBlank(sequence)) {
            try {
                Number number = NumberUtil.parseNumber(sequence);
                return this.format(number);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return "";
    }

    /**
     * 格式化数字
     *
     * @param number 数字
     * @return 格式化结果
     */
    public String format(Number number) {
        if (number != null) {
            try {
                String string = this.format().format(number);
                return string.replaceAll(",", "");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return "";
    }

    @Override
    public StringBuffer format(long number, StringBuffer result, FieldPosition fieldPosition) {
        return this.format().format(number, result, fieldPosition);
    }

    @Override
    public StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition) {
        return this.format().format(number, result, fieldPosition);
    }
}
