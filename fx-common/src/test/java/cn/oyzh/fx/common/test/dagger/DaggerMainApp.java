//package cn.oyzh.fx.common.test.dagger;
//
//import dagger.Module;
//import jakarta.annotation.PostConstruct;
//
//@Module
//public class DaggerMainApp {
//
//    public static void main(String[] args) throws Exception {
//        System.out.println("DaggerMainApp");
//        //4.执行注入动作
//        ApplicationComponent applicationComponent = Daggera.create();
//        applicationComponent.inject(this);
//    }
//
//    @PostConstruct
//    public void init(){
//        System.out.println("DaggerMainApp init...");
//    }
//
//}
