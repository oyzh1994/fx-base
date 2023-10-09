package cn.oyzh.fx.plus.validator;

/**
 * 校验机
 *
 * @author oyzh
 * @since 2023/1/29
 */
public interface Verifier {

    /**
     * 执行校验
     *
     * @param obj 对象
     * @return 结果
     */
    boolean doVerify(Object obj);

    /**
     * 获取校验顺序
     *
     * @return 校验顺序
     */
    int getOrder();

    /**
     * 设置校验顺序
     *
     * @param verifyOrder 校验顺序
     */
    void setOrder(int verifyOrder);

    /**
     * 获取校验失败信息
     *
     * @return 校验失败信息
     */
    String getFailMsg();

    /**
     * 设置校验失败信息
     *
     * @param verifyFailMsg 校验失败信息
     */
    void setFailMsg(String verifyFailMsg);
}
