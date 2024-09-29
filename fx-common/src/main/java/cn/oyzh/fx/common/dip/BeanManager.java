//package cn.oyzh.fx.common.dip;
//
//
//import cn.hutool.core.util.StrUtil;
//import lombok.experimental.UtilityClass;
//
//import java.lang.reflect.InvocationTargetException;
//import java.util.ArrayList;
//import java.util.List;
//
//@UtilityClass
//public class BeanManager {
//
//    static BeanApplicationContext applicationContext;
//
//    private final List<BeanObject> beans = new ArrayList<>();
//
//    public static Object getBean(String beanName) throws Exception {
//        for (BeanObject bean : beans) {
//            if (StringUtil.equals(bean.getBeanName(), beanName)) {
//                return bean.getBean();
//            }
//        }
//        return null;
//    }
//
//    public static Object getBean(Class<?> beanClass) throws Exception {
//        for (BeanObject bean : beans) {
//            if (bean.getBeanClass() == beanClass) {
//                return bean.getBean();
//            }
//        }
//        return null;
//    }
//
//    public static boolean containsBean(String beanName, Class<?> beanClass) throws Exception {
//        if (beanName != null) {
//            return getBean(beanName) != null;
//        }
//        return getBean(beanClass) != null;
//    }
//
//    public static void registerBean(Class<?> beanClass) throws Exception {
//        BeanObject beanObject = BeanGenerator.generatorBean(beanClass);
//        beans.add(beanObject);
//
//    }
//
////    public static void registerBean(BeanObject beanObject) throws Exception {
////       beans.add(beanObject);
////    }
//
//    static void initBeans(int maxCount) throws Exception {
//        int count = 0;
//        while (count++ < maxCount) {
//            for (BeanObject bean : beans) {
//                BeanUtil.injectionField(bean);
//                BeanUtil.injectionValue(bean);
//            }
//        }
//        for (BeanObject bean : beans) {
//            BeanUtil.invokeInitMethod(bean);
//        }
//    }
//
//}
