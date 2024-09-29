package cn.oyzh.fx.plus.validator;

import cn.oyzh.fx.common.util.StringUtil;
import cn.oyzh.fx.plus.information.MessageBox;
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
        if (StringUtil.isNotBlank(failMsg) && node != null) {
            MessageBox.tipMsg(failMsg, node);
        }
    }
}
