package cn.oyzh.fx.plus.validator;

import cn.oyzh.common.util.RegexUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * 最大值检验机
 *
 * @author oyzh
 * @since 2023/1/29
 */
public class MaxValVerifier extends BaseVerifier {

    @Setter
    @Getter
    private Number maxVal;

    @Getter
    @Setter
    private int order = 2;

    @Setter
    private String failMsg = "值不能大于";

    public MaxValVerifier(Number maxVal) {
        this.maxVal = maxVal;
    }

    @Override
    public boolean doVerify(Object obj) {
        if (this.maxVal != null && obj != null) {
            if (obj instanceof Number num) {
                return num.doubleValue() <= this.maxVal.doubleValue();
            }
            if (obj instanceof String str && RegexUtil.isNumber(str)) {
                return Double.parseDouble(str) <= this.maxVal.doubleValue();
            }
        }
        return true;
    }

    public String getFailMsg() {
        return this.failMsg + this.maxVal.doubleValue();
    }
}
