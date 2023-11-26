//package cn.oyzh.fx.common.spring;
//
//import lombok.Getter;
//import lombok.NonNull;
//import org.springframework.beans.BeansException;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationContextAware;
//import org.springframework.stereotype.Component;
//
//import java.util.Collections;
//import java.util.Map;
//
///**
// * Spring工具类
// *
// * @author oyzh
// * @since 2019/9/24 0024 18:16
// */
//@Component
//public class SpringUtil implements ApplicationContextAware {
//
//    /**
//     * Spring上下文对象
//     */
//    @Getter
//    private static ApplicationContext context;
//
//    @Override
//    public void setApplicationContext(ApplicationContext context) throws BeansException {
//        SpringUtil.context = context;
//    }
//
//    /**
//     * 获取bean
//     *
//     * @param name 名称
//     * @param <T>  泛型
//     * @return bean
//     */
//    public static <T> T getBean(@NonNull String name) {
//        if (context == null) {
//            return null;
//        }
//        try {
//            return (T) context.getBean(name);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return null;
//    }
//
//    /**
//     * 根据类型获取bean集合
//     *
//     * @param type 类型
//     * @param <T>  泛型
//     * @return bean集合
//     */
//    public static <T> Map<String, T> getBeansOfType(@NonNull Class<T> type) {
//        if (context == null) {
//            return null;
//        }
//        try {
//            return context.getBeansOfType(type);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return Collections.emptyMap();
//    }
//
//    /**
//     * 获取bean
//     *
//     * @param clazz 类
//     * @param <T>   泛型
//     * @return bean
//     */
//    public static <T> T getBean(@NonNull Class<T> clazz) {
//        if (context == null) {
//            return null;
//        }
//        try {
//            return context.getBean(clazz);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return null;
//    }
//
//    /**
//     * 获取配置值
//     *
//     * @param name 配置名称
//     * @return 配置值
//     */
//    public static String getValue(@NonNull String name) {
//        if (context == null) {
//            return "";
//        }
//        try {
//            return context.getEnvironment().getProperty(name);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return "";
//    }
//}
