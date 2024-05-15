package cn.oyzh.fx.common.util;

import lombok.Getter;

import java.text.DecimalFormat;

/**
 * @author oyzh
 * @since 2024/5/15
 */
public class DecimalFormatWrapper extends DecimalFormat {

    /**
     * 保留小数位数
     */
    @Getter
    private final int scaleLen;

    public DecimalFormatWrapper(int scaleLen) {
        super("0." + "0".repeat(scaleLen));
        this.scaleLen = scaleLen;
    }

    public DecimalFormatWrapper() {
        super();
        this.scaleLen = -1;
    }

}
