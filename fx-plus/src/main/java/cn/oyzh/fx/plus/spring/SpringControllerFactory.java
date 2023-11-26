package cn.oyzh.fx.plus.spring;

import cn.hutool.extra.spring.SpringUtil;
import cn.oyzh.fx.plus.stage.StageAttribute;
import javafx.util.Callback;
import org.springframework.stereotype.Component;

/**
 * fx Controller 工厂
 *
 * @author oyzh
 * @since 2020/10/16
 */
public class SpringControllerFactory implements Callback<Class<?>, Object> {

    @Override
    public Object call(Class<?> clazz) {
        try {
            Object controller = null;
            if (clazz.getAnnotation(StageAttribute.class) != null) {
                controller = SpringUtil.getBean(clazz);
            }
//            if (clazz.getAnnotation(FXWindow.class) != null) {
//                controller = SpringUtil.getBean(clazz);
//            }
            if (controller == null && clazz.getAnnotation(Component.class) != null) {
                controller = SpringUtil.getBean(clazz);
            }
            if (controller == null) {
                controller = SpringUtil.getBean(clazz);
            }
            if (controller == null) {
                controller = clazz.getDeclaredConstructor().newInstance();
            }
            return controller;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
}
