package cn.oyzh.fx.plus.validator;

/**
 * 校验能力接口
 *
 * @author oyzh
 * @since 2023-01-29
 */
public interface Verifiable {
//public interface Verifiable<V extends Validator> {

//    /**
//     * 获取校验器
//     *
//     * @return 校验器
//     */
//    V getValidator();
//
//    /**
//     * 设置校验器
//     *
//     * @param validator 校验器
//     */
//    void setValidator(V validator);

    /**
     * 执行校验
     *
     * @return 是否通过校验
     */
    default boolean validate() {
//        return this.getValidator() != null && this.getValidator().doVerify();
        return true;
    }
}
