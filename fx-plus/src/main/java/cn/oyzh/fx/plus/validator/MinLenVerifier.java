package cn.oyzh.fx.plus.validator;

import lombok.Getter;
import lombok.Setter;

/**
 * 最小长度检验机
 *
 * @author oyzh
 * @since 2023/1/29
 */
public class MinLenVerifier extends BaseVerifier {

    @Setter
    @Getter
    private Long minLen;

    @Getter
    @Setter
    private int order = 2;

    @Setter
    private String failMsg = "内容长度不能小于";

    public MinLenVerifier(long minLen) {
        this.minLen = minLen;
    }

    @Override
    public boolean doVerify(Object obj) {
        if (this.minLen != null && obj != null) {
            if (obj instanceof String str) {
                return str.length() <= this.minLen;
            }
        }
        return true;
    }

    public String getFailMsg() {
        return this.failMsg + this.minLen;
    }
}
