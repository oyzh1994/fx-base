// package cn.oyzh.fx.plus.spring;
//
// import cn.hutool.extra.spring.SpringUtil;
// import javafx.util.Callback;
// import org.springframework.beans.BeansException;
//
// /**
//  * fx Controller 工厂
//  *
//  * @author oyzh
//  * @since 2020/10/16
//  */
// public class SpringControllerFactory implements Callback<Class<?>, Object> {
//
//     @Override
//     public Object call(Class<?> clazz) {
//         Object controller = null;
//         try {
//             controller = SpringUtil.getBean(clazz);
//         } catch (BeansException ignored) {
//         }
//         try {
//             if (controller == null) {
//                 controller = clazz.getDeclaredConstructor().newInstance();
//             }
//             return controller;
//         } catch (Exception ex) {
//             ex.printStackTrace();
//             throw new RuntimeException(ex.getMessage());
//         }
//     }
// }
