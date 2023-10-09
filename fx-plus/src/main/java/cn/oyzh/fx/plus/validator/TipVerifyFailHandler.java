package cn.oyzh.fx.plus.validator;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.plus.information.FXTipUtil;
import javafx.scene.Node;

/**
 * tip校验失败实现
 *
 * @author oyzh
 * @since 2023/1/29
 */
public class TipVerifyFailHandler implements VerifyFailHandler {

    @Override
    public void onVerifyFail(String failMsg, Node node) {
        if (StrUtil.isNotBlank(failMsg) && node != null) {
            FXTipUtil.tip(failMsg, node);
        }
    }
}
