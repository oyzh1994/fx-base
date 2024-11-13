package cn.oyzh.fx.plus.validator;

import cn.oyzh.common.util.RegexUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * 整数检验机
 *
 * @author oyzh
 * @since 2023/1/29
 */
public class NumberVerifier extends DecimalVerifier {

    @Getter
    @Setter
    private int verifyOrder = 1;

    @Getter
    @Setter
    private String verifyFailMsg = "请输入整数！";

    @Override
    public boolean doVerify(Object obj) {
        if (obj instanceof String str) {
            return RegexUtil.isNumber(str);
        }
        return true;
    }
}
