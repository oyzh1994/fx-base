package cn.oyzh.fx.plus.window;

import javafx.stage.Window;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * fx窗口工具类
 *
 * @author oyzh
 * @since 2023/10/12
 */
@UtilityClass
public class PopupManager {

    public static final String REF_ATTR = "_popup_window_reference";

    /**
     * 获取窗口
     *
     * @param controllerClass controller类
     * @return PopupWrapper
     */
    public static PopupWrapper getWindow(@NonNull Class<?> controllerClass) {
        for (Window window : Window.getWindows()) {
            Object reference = window.getProperties().get(REF_ATTR);
            if (reference instanceof PopupWrapper wrapper && wrapper.controllerClass() == controllerClass) {
                return wrapper;
            }
        }
        return null;
    }

    /**
     * 显示窗口
     *
     * @param controllerClass controller类
     */
    public static void showWindow(@NonNull Class<?> controllerClass) {
        PopupWrapper window = parseWindow(controllerClass);
        if (window != null) {
            window.disable();
        }
    }

    /**
     * 解析窗口
     *
     * @param clazz 窗口类
     * @return PopupWrapper
     */
    public static PopupWrapper parseWindow(@NonNull Class<?> clazz) {
        PopupAttribute attribute = clazz.getAnnotation(PopupAttribute.class);
        if (attribute == null) {
            throw new RuntimeException("can not find annotation[" + PopupAttribute.class.getSimpleName() + "] from class: " + clazz.getName());
        }
        // 获取窗口
        PopupWrapper window = getWindow(clazz);
        // 创建窗口
        if (window == null) {
            window = new PopupExt(attribute);
        }
        return window;
    }
}
