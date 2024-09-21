//package cn.oyzh.fx.common.dip;
//
//import cn.hutool.core.util.ReflectUtil;
//import jakarta.annotation.PostConstruct;
//import jakarta.annotation.Resource;
//
//import java.lang.reflect.Field;
//import java.lang.reflect.Method;
//
//public class BeanGenerator {
//
//    public static final BeanGenerator INSTANCE = new BeanGenerator();
//
//    public BeanObject generator(Class<?> beanClass) throws Exception {
//        Object bean = beanClass.getConstructor().newInstance();
//        boolean propertySource=BeanUtil.checkPropertySourceClass(beanClass);
//        BeanObject beanObject = new BeanObject();
//        beanObject.setBean(bean);
//        beanObject.setBeanClass(beanClass);
//        beanObject.setPropertySource(propertySource);
//        Field[] fields = ReflectUtil.getFields(beanClass);
//        for (Field field : fields) {
//            if (BeanUtil.checkInjectField(field)) {
//                BeanInjectField injectField = new BeanInjectField();
//                injectField.setField(field);
//                beanObject.addInjectField(injectField);
//            }
//            if (propertySource&&BeanUtil.checkValueField(field)) {
//                BeanValueField valueField = new BeanValueField();
//                valueField.setField(field);
//                beanObject.addValueField(valueField);
//            }
//        }
//        Method[] methods = ReflectUtil.getMethods(beanClass);
//        for (Method method : methods) {
//            if (BeanUtil.checkInitMethod(method)) {
//                BeanInitMethod initMethod = new BeanInitMethod();
//                initMethod.setMethod(method);
//                beanObject.setInitMethod(initMethod);
//            }
//            if (BeanUtil.checkDestroyMethod(method)) {
//                BeanDestroyMethod destroyMethod = new BeanDestroyMethod();
//                destroyMethod.setMethod(method);
//                beanObject.setDestroyMethod(destroyMethod);
//            }
//        }
//        return beanObject;
//    }
//
//    public static BeanObject generatorBean(Class<?> beanClazz) throws Exception {
//        return INSTANCE.generator(beanClazz);
//    }
//}
