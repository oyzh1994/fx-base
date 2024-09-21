//package cn.oyzh.fx.common.test.dip;
//
//import cn.oyzh.fx.common.dip.Component;
//import jakarta.annotation.PostConstruct;
//import jakarta.annotation.Resource;
//import lombok.Getter;
//
//@Component
//public class DIPBean1 {
//
//    @Getter
//    private String name = "name";
//
//    @Getter
//    private String desc = "test";
//
//    @Getter
//    @Resource
//    private DIPBean2 DIPBean2;
//
//    @PostConstruct
//    public void init(){
//        System.out.println("DIPBean1 init...");
//    }
//}
