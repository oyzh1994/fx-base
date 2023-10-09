package cn.oyzh.fx.plus.validator;

import javafx.scene.Node;

/**
 * 校验失败处理器
 *
 * @author oyzh
 * @since 2023/1/29
 */
public interface VerifyFailHandler {

    /**
     * 校验失败事件
     *
     * @param failMsg 失败信息
     * @param node    节点
     */
    void onVerifyFail(String failMsg, Node node);

}
