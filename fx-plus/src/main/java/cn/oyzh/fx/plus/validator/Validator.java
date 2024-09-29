package cn.oyzh.fx.plus.validator;

import cn.oyzh.fx.common.util.CollectionUtil;
import javafx.beans.value.ObservableObjectValue;
import javafx.scene.Node;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 校验器
 *
 * @author oyzh
 * @since 2023/1/29
 */
public interface Validator {

    /**
     * 执行校验
     *
     * @return 结果
     */
    default boolean doVerify() {
        if (CollectionUtil.isNotEmpty(this.getVerifiers()) && this.getValue() != null) {
            Object obj = this.getValue().get();
            for (cn.oyzh.fx.plus.validator.Verifier verifier : this.getVerifiers()) {
                if (!verifier.doVerify(obj)) {
                    this.onVerifyFail(verifier);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 获取校验节点
     *
     * @return 节点
     */
    Node getNode();

    /**
     * 设置校验节点
     *
     * @param node 节点
     */
    void setNode(Node node);

    /**
     * 获取校验值
     *
     * @return 校验值
     */
    ObservableObjectValue<?> getValue();

    /**
     * 设置校验值
     *
     * @param value 校验值
     */
    void setValue(ObservableObjectValue<?> value);

    /**
     * 获取校验机列表
     *
     * @return 校验机列表
     */
    List<cn.oyzh.fx.plus.validator.Verifier> getVerifiers();

    /**
     * 设置校验机列表
     *
     * @param verifiers 校验机列表
     */
    void setVerifiers(List<cn.oyzh.fx.plus.validator.Verifier> verifiers);

    /**
     * 添加校验机
     *
     * @param verifier 校验机
     */
    default void addVerifier(cn.oyzh.fx.plus.validator.Verifier verifier) {
        if (verifier == null) {
            return;
        }
        if (this.getVerifiers() == null) {
            this.setVerifiers(new ArrayList<>());
        }
        this.getVerifiers().add(verifier);
        this.getVerifiers().sort(Comparator.comparingInt(cn.oyzh.fx.plus.validator.Verifier::getOrder));
    }

    /**
     * 是否存在校验机
     *
     * @param verifierClass 校验机类
     */
    default boolean existVerifier(Class<?> verifierClass) {
        if (verifierClass == null || this.getVerifiers() == null) {
            return false;
        }
        return this.getVerifiers().stream().anyMatch(v -> v.getClass() == verifierClass);
    }

    /**
     * 获取校验机
     *
     * @param verifierClass 校验机类
     */
    default <F extends cn.oyzh.fx.plus.validator.Verifier> F getVerifier(Class<F> verifierClass) {
        if (verifierClass == null || this.getVerifiers() == null) {
            return null;
        }
        return (F) this.getVerifiers().stream().filter(v -> v.getClass() == verifierClass).findFirst().orElse(null);
    }

    /**
     * 移除校验机
     *
     * @param verifierClass 校验机类
     */
    default void removeVerifier(Class<?> verifierClass) {
        if (verifierClass == null || this.getVerifiers() == null) {
            return;
        }
        this.getVerifiers().removeIf(verifier -> verifier.getClass() == verifierClass);
    }

    /**
     * 校验失败事件
     *
     * @param verifier 校验器
     */
    default void onVerifyFail(Verifier verifier) {
        if (this.getFailHandler() != null && verifier != null) {
            this.getFailHandler().onVerifyFail(verifier.getFailMsg(), this.getNode());
        }
    }

    /**
     * 获取验证失败处理器
     *
     * @return 失败处理器
     */
    VerifyFailHandler getFailHandler();

    /**
     * 设置验证失败处理器
     *
     * @param verifyFailHandler 失败处理器
     */
    void setFailHandler(VerifyFailHandler verifyFailHandler);
}
