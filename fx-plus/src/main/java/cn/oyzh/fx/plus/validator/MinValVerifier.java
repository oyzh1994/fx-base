package cn.oyzh.fx.plus.validator;

import cn.oyzh.fx.common.util.RegexUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * 最小值检验机
 *
 * @author oyzh
 * @since 2023/1/29
 */
public class MinValVerifier extends BaseVerifier {

    @Setter
    @Getter
    private Number minVal;

    @Getter
    @Setter
    private int order = 2;

    @Setter
    private String failMsg = "值不能大于";

    public MinValVerifier(Number minVal) {
        this.minVal = minVal;
    }

    @Override
    public boolean doVerify(Object obj) {
        if (this.minVal != null && obj != null) {
            if (obj instanceof Number num) {
                return num.doubleValue() >= this.minVal.doubleValue();
            }
            if (obj instanceof String str && RegexUtil.isNumber(str)) {
                return Double.parseDouble(str) >= this.minVal.doubleValue();
            }
        }
        return true;
    }

    public String getFailMsg() {
        return this.failMsg + this.minVal.doubleValue();
    }
}
