//package cn.oyzh.fx.common.dip;
//
//import cn.hutool.core.util.ReflectUtil;
//
//import java.util.List;
//
//public class BeanApplicationBuilder {
//
//    private String[] scanPackages;
//
//    private Class<?> mainClass;
//
//    public BeanApplicationBuilder scanPackages(String... scanPackages) {
//        this.scanPackages = scanPackages;
//        return this;
//    }
//
//    public BeanApplicationBuilder mainClass(Class<?> mainClass) {
//        this.mainClass = mainClass;
//        return this;
//    }
//
//    public BeanApplicationContext run(String... args) throws Exception {
//        BeanApplicationContext context = new BeanApplicationContext();
//        context.setMainClass(mainClass);
//        BeanApplication application = mainClass.getAnnotation(BeanApplication.class);
//        if (application != null && scanPackages == null) {
//            context.setScanPackages(application.scanBasePackages());
//        } else {
//            context.setScanPackages(scanPackages);
//        }
//        BeanManager.applicationContext = context;
//        for (String scanPackage : context.getScanPackages()) {
//            List<Class<?>> classes = BeanClassUtil.scanClasses(scanPackage);
//            for (Class<?> clazz : classes) {
//                if (BeanUtil.checkBean(clazz)) {
//                    BeanManager.registerBean(clazz);
//                }
//            }
//        }
//        long end = System.currentTimeMillis();
//        BeanManager.initBeans(10);
//        context.setStartupCostMillisecond((short) (end - context.getStartTime()));
//        return context;
//    }
//}
