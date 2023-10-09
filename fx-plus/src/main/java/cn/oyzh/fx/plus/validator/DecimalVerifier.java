package cn.oyzh.fx.plus.validator;

import cn.oyzh.fx.common.util.RegexUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * 数字检验机
 *
 * @author oyzh
 * @since 2023/1/29
 */
public class DecimalVerifier extends BaseVerifier {

    @Getter
    @Setter
    private int order = 1;

    @Getter
    @Setter
    private String failMsg = "请输入数字！";

    @Override
    public boolean doVerify(Object obj) {
        if (obj instanceof String str) {
            return RegexUtil.isDecimal(str.trim());
        }
        return true;
    }
}
