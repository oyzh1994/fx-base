package cn.oyzh.fx.plus.validator;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * 不为空检验机
 *
 * @author oyzh
 * @since 2023/1/29
 */
public class RequiredVerifier extends BaseVerifier {

    @Getter
    @Setter
    private int order = Integer.MIN_VALUE;

    @Getter
    @Setter
    private String failMsg = "内容不能为空！";

    @Override
    public boolean doVerify(Object obj) {
        if (obj instanceof String str) {
            return StrUtil.isNotBlank(str);
        }
        return obj != null;
    }
}
