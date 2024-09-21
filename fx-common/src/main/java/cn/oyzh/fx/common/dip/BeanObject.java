//package cn.oyzh.fx.common.dip;
//
//import lombok.Data;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Data
//public class BeanObject {
//
//    private Object bean;
//
//    private String beanName;
//
//    private Class<?> beanClass;
//
//    private boolean propertySource;
//
//    private BeanInitMethod initMethod;
//
//    private BeanDestroyMethod destroyMethod;
//
//    private List<BeanValueField> valueFields;
//
//    private List<BeanInjectField> injectFields;
//
//    public void addInjectField(BeanInjectField injectField) {
//        if (injectFields == null) {
//            this.injectFields = new ArrayList<>();
//        }
//        this.injectFields.add(injectField);
//    }
//
//    public void addValueField(BeanValueField valueField) {
//        if (valueFields == null) {
//            this.valueFields = new ArrayList<>();
//        }
//        this.valueFields.add(valueField);
//    }
//
//    public boolean isInjectComplete() {
//        for (BeanInjectField injectField : injectFields) {
//            if (!injectField.isInjected()) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    public boolean isValueComplete() {
//        for (BeanValueField valueField : valueFields) {
//            if (!valueField.isInjected()) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//}
