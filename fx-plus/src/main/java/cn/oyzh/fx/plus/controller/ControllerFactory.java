package cn.oyzh.fx.plus.controller;

import javafx.util.Callback;

/**
 * fx Controller 工厂
 *
 * @author oyzh
 * @since 2020/10/16
 */
public class ControllerFactory implements Callback<Class<?>, Object> {

    @Override
    public Object call(Class<?> clazz) {
        try {
            Object controller = clazz.getConstructor().newInstance();
            return controller;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage());
        }
    }
}
