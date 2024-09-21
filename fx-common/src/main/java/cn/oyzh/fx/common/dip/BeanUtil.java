//package cn.oyzh.fx.common.dip;
//
//import jakarta.annotation.PostConstruct;
//import jakarta.annotation.PreDestroy;
//import jakarta.annotation.Resource;
//import lombok.experimental.UtilityClass;
//
//import java.lang.reflect.Field;
//import java.lang.reflect.Method;
//
//@UtilityClass
//public class BeanUtil {
//
//    public static boolean checkBean(Class<?> clazz) {
//        if (clazz.isInterface()) {
//            return false;
//        }
//        if (clazz.isEnum()) {
//            return false;
//        }
//        if (clazz.isPrimitive()) {
//            return false;
//        }
//        return clazz.getAnnotation(Component.class) != null || clazz.getAnnotation(BeanApplication.class) != null;
//    }
//
//    public static boolean checkPropertySourceClass(Class<?> clazz) {
//        return clazz.getAnnotation(PropertySource.class) != null;
//    }
//
//    public static boolean checkInjectField(Field field) {
//        return field.getAnnotation(Resource.class) != null;
//    }
//
//    public static boolean checkValueField(Field field) {
//        return field.getAnnotation(Value.class) != null;
//    }
//
//    public static boolean checkInitMethod(Method method) {
//        return method.getAnnotation(PostConstruct.class) != null;
//    }
//
//    public static boolean checkDestroyMethod(Method method) {
//        return method.getAnnotation(PreDestroy.class) != null;
//    }
//
//    static void injectionField(BeanObject bean) throws Exception {
//        if (!bean.isInjectComplete()) {
//            for (BeanInjectField injectField : bean.getInjectFields()) {
//                if (injectField.isInjected()) {
//                    continue;
//                }
//                Field field = injectField.getField();
//                field.setAccessible(true);
//                Object val;
//                Resource resource = field.getAnnotation(Resource.class);
//                if (!resource.name().isEmpty()) {
//                    val = BeanManager.getBean(resource.name());
//                } else if (resource.type() != Object.class) {
//                    val = BeanManager.getBean(resource.type());
//                } else {
//                    val = BeanManager.getBean(field.getType());
//                }
//                if (val != null) {
//                    field.set(bean.getBean(), val);
//                    injectField.setInjected(true);
//                }
//            }
//        }
//    }
//
//    static void injectionValue(BeanObject bean) throws Exception {
////        if (!bean.isValueComplete()) {
////            for (BeanValueField valueField : bean.getValueFields()) {
////                if (valueField.isInjected()) {
////                    continue;
////                }
////                Field field = valueField.getField();
////                field.setAccessible(true);
////                Value resource = field.getAnnotation(Value.class);
////                Object val;
////                if (!resource.name().isEmpty()) {
////                    val = BeanManager.getBean(resource.name());
////                } else if (resource.type() != Object.class) {
////                    val = BeanManager.getBean(resource.type());
////                } else {
////                    val = BeanManager.getBean(field.getType());
////                }
////                if (val != null) {
////                    field.set(bean.getBean(), val);
////                    injectField.setInjected(true);
////                }
////            }
////        }
//    }
//
//    static void invokeInitMethod(BeanObject bean) throws Exception {
//        BeanInitMethod initMethod = bean.getInitMethod();
//        if (initMethod == null || initMethod.isInvoked()) {
//            return;
//        }
//        Method method = initMethod.getMethod();
//        if (method.getAnnotation(PostConstruct.class) != null) {
//            method.setAccessible(true);
//            method.invoke(bean.getBean());
//            initMethod.setInvoked(true);
//        }
//    }
//}
