//package cn.oyzh.fx.plus.validator;
//
//import cn.oyzh.common.util.BooleanUtil;
//import javafx.beans.value.ObservableObjectValue;
//import javafx.scene.Node;
//import javafx.scene.control.ComboBoxBase;
//import javafx.scene.control.TextInputControl;
//import javafx.scene.text.Text;
//import lombok.Getter;
//import lombok.Setter;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Objects;
//
///**
// * 基础校验器
// *
// * @author oyzh
// * @since 2023/1/29
// */
//public class BaseValidator implements Validator {
//
//    /**
//     * 校验节点
//     */
//    @Getter
//    @Setter
//    private Node node;
//
//    /**
//     * 校验值
//     */
//    @Getter
//    @Setter
//    private ObservableObjectValue<?> value;
//
//    /**
//     * 校验机列表
//     */
//    @Getter
//    @Setter
//    private List<Verifier> verifiers = new ArrayList<>();
//
//    /**
//     * 校验失败处理器
//     */
//    @Getter
//    @Setter
//    private VerifyFailHandler failHandler = new TipVerifyFailHandler();
//
//    public BaseValidator() {
//    }
//
//    public BaseValidator(Node node) {
//        this.setNode(node);
//        if (node instanceof Text text) {
//            this.setValue(text.textProperty());
//        } else if (node instanceof TextInputControl text) {
//            this.setValue(text.textProperty());
//        } else if (node instanceof ComboBoxBase<?> comboBoxBase) {
//            this.setValue(comboBoxBase.valueProperty());
//        }
//    }
//
//    public BaseValidator(Node node, ObservableObjectValue<?> value) {
//        this.setNode(node);
//        this.setValue(value);
//    }
//
//    /**
//     * 添加或移除最大长度校验机
//     *
//     * @param maxLen 最大长度
//     * @param order  顺序
//     */
//    public void addMaxLenVerifier(Long maxLen, int order) {
//        if (maxLen != null) {
//            MaxLenVerifier verifier = this.getVerifier(MaxLenVerifier.class);
//            if (verifier == null) {
//                verifier = new MaxLenVerifier(maxLen);
//                verifier.setOrder(order);
//                this.addVerifier(verifier);
//            } else if (!Objects.equals(verifier.getMaxLen(), maxLen)) {
//                verifier.setMaxLen(maxLen);
//            } else if (verifier.getOrder() != order) {
//                verifier.setOrder(order);
//            }
//        } else {
//            this.removeVerifier(MaxLenVerifier.class);
//        }
//    }
//
//    /**
//     * 添加或移除最小长度校验机
//     *
//     * @param minLen 最小长度
//     * @param order  顺序
//     */
//    public void addMinLenVerifier(Long minLen, int order) {
//        if (minLen != null) {
//            MinLenVerifier verifier = this.getVerifier(MinLenVerifier.class);
//            if (verifier == null) {
//                verifier = new MinLenVerifier(minLen);
//                verifier.setOrder(order);
//                this.addVerifier(verifier);
//            } else if (!Objects.equals(verifier.getMinLen(), minLen)) {
//                verifier.setMinLen(minLen);
//            } else if (verifier.getOrder() != order) {
//                verifier.setOrder(order);
//            }
//        } else {
//            this.removeVerifier(MaxLenVerifier.class);
//        }
//    }
//
//    /**
//     * 添加或移除最大值校验机
//     *
//     * @param maxVal 最大值
//     * @param order  顺序
//     */
//    public void addMaxValVerifier(Number maxVal, int order) {
//        if (maxVal != null) {
//            MaxValVerifier verifier = this.getVerifier(MaxValVerifier.class);
//            if (verifier == null) {
//                verifier = new MaxValVerifier(maxVal);
//                verifier.setOrder(order);
//                this.addVerifier(verifier);
//            } else if (!Objects.equals(verifier.getMaxVal(), maxVal)) {
//                verifier.setMaxVal(maxVal);
//            } else if (verifier.getOrder() != order) {
//                verifier.setOrder(order);
//            }
//        } else {
//            this.removeVerifier(MaxValVerifier.class);
//        }
//    }
//
//    /**
//     * 添加或移除最小值校验机
//     *
//     * @param minVal 最小值
//     * @param order  顺序
//     */
//    public void addMinValVerifier(Number minVal, int order) {
//        if (minVal != null) {
//            MinValVerifier verifier = this.getVerifier(MinValVerifier.class);
//            if (verifier == null) {
//                verifier = new MinValVerifier(minVal);
//                verifier.setOrder(order);
//                this.addVerifier(verifier);
//            } else if (!Objects.equals(verifier.getMinVal(), minVal)) {
//                verifier.setMinVal(minVal);
//            } else if (verifier.getOrder() != order) {
//                verifier.setOrder(order);
//            }
//        } else {
//            this.removeVerifier(MinValVerifier.class);
//        }
//    }
//
//    /**
//     * 添加或移除不为空校验机
//     *
//     * @param require 是否必须
//     * @param order   结果
//     */
//    public void addRequiredVerifier(Boolean require, int order) {
//        if (BooleanUtil.isTrue(require)) {
//            RequiredVerifier verifier = this.getVerifier(RequiredVerifier.class);
//            if (verifier == null) {
//                verifier = new RequiredVerifier();
//                verifier.setOrder(order);
//                this.addVerifier(verifier);
//            } else if (verifier.getOrder() != order) {
//                verifier.setOrder(order);
//            }
//        } else {
//            this.removeVerifier(RequiredVerifier.class);
//        }
//    }
//
//    /**
//     * 获取最大长度校验机
//     */
//    public MaxLenVerifier getMaxLenVerifier() {
//        return this.getVerifier(MaxLenVerifier.class);
//    }
//
//    /**
//     * 获取最小长度校验机
//     */
//    public MinLenVerifier getMinLenVerifier() {
//        return this.getVerifier(MinLenVerifier.class);
//    }
//
//    /**
//     * 获取最大值校验机
//     */
//    public MaxValVerifier getMaxValVerifier() {
//        return this.getVerifier(MaxValVerifier.class);
//    }
//
//    /**
//     * 获取最小值校验机
//     */
//    public MinValVerifier getMinValVerifier() {
//        return this.getVerifier(MinValVerifier.class);
//    }
//
//    /**
//     * 获取不为空校验机
//     */
//    public RequiredVerifier getRequiredVerifier() {
//        return this.getVerifier(RequiredVerifier.class);
//    }
//}
