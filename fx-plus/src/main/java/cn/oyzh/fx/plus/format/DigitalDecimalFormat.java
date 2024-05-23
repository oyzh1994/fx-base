package cn.oyzh.fx.plus.format;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.util.Objects;

/**
 * @author oyzh
 * @since 2024/5/15
 */
public class DigitalDecimalFormat extends DecimalFormat {

    /**
     * 保留小数位数
     */
    @Getter
    private Integer scaleLen;

    private DecimalFormat format;

    public DigitalDecimalFormat(Integer scaleLen) {
        this.setScaleLen(scaleLen);
    }

    public DigitalDecimalFormat() {
        this.setScaleLen(-1);
    }

    public void setScaleLen(Integer scaleLen) {
        if (!Objects.equals(scaleLen, this.scaleLen)) {
            this.format = null;
        }
        this.scaleLen = scaleLen;
    }

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

    public String format(CharSequence sequence) {
        if (StrUtil.isNotBlank(sequence)) {
            try {
                Number number = NumberUtil.parseNumber(sequence.toString());
                return this.format(number);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return "";
    }

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
