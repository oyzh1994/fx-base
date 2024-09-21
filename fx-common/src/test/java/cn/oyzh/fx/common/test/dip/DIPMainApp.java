//package cn.oyzh.fx.common.test.dip;
//
//import cn.oyzh.fx.common.dip.BeanApplication;
//import cn.oyzh.fx.common.dip.BeanApplicationBuilder;
//import cn.oyzh.fx.common.dip.BeanApplicationContext;
//import jakarta.annotation.PostConstruct;
//
//@BeanApplication
//public class DIPMainApp {
//
//    public static void main(String[] args) throws Exception {
//        BeanApplicationBuilder builder = new BeanApplicationBuilder();
//        builder.mainClass(DIPMainApp.class);
//        builder.scanPackages("cn.oyzh");
//        BeanApplicationContext context= builder.run(args);
//        System.out.println(context.getStartupCostMillisecond());
//    }
//
//    @PostConstruct
//    public void init(){
//        System.out.println("DIPMainApp init...");
//    }
//
//}
