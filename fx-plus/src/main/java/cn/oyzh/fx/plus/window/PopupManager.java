package cn.oyzh.fx.plus.window;

import javafx.scene.Node;
import javafx.stage.Window;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * 弹窗工具类
 *
 * @author oyzh
 * @since 2024/07/12
 */
@UtilityClass
public class PopupManager {

    /**
     * 引用属性
     */
    public static final String REF_ATTR = "_popup_window_reference";

    /**
     * 获取弹窗
     *
     * @param controllerClass controller类
     * @return PopupWrapper
     */
    public static PopupWrapper getPopup(@NonNull Class<?> controllerClass) {
        for (Window window : Window.getWindows()) {
            Object reference = window.getProperties().get(REF_ATTR);
            if (reference instanceof PopupWrapper wrapper && wrapper.controllerClass() == controllerClass) {
                return wrapper;
            }
        }
        return null;
    }

    /**
     * 显示弹窗
     *
     * @param controllerClass controller类
     * @param owner           父组件
     */
    public static void showPopup(@NonNull Class<?> controllerClass, @NonNull Node owner) {
        PopupWrapper window = parsePopup(controllerClass);
        window.showPopup(owner);
    }

    /**
     * 解析弹窗
     *
     * @param clazz 弹窗类
     * @return PopupWrapper
     */
    public static PopupWrapper parsePopup(@NonNull Class<?> clazz) {
        PopupAttribute attribute = clazz.getAnnotation(PopupAttribute.class);
        if (attribute == null) {
            throw new RuntimeException("can not find annotation[" + PopupAttribute.class.getSimpleName() + "] from class: " + clazz.getName());
        }
        // 获取弹窗
        PopupWrapper window = getPopup(clazz);
        // 创建弹窗
        if (window != null) {
            window.disappear();
        }
        return new PopupExt(attribute);
    }
}
