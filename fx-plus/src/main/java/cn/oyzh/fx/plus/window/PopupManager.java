package cn.oyzh.fx.plus.window;

import atlantafx.base.controls.Popover;
import javafx.scene.Node;
import javafx.stage.PopupWindow;
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
    public static PopupAdapter getPopup(@NonNull Class<?> controllerClass) {
        for (Window window : Window.getWindows()) {
            Object reference = window.getProperties().get(REF_ATTR);
            if (reference instanceof PopupAdapter wrapper && wrapper.controllerClass() == controllerClass) {
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
        PopupAdapter window = parsePopup(controllerClass);
        window.showPopup(owner);
    }

    /**
     * 解析弹窗
     *
     * @param clazz 弹窗类
     * @return PopupWrapper
     */
    public static PopupAdapter parsePopup(@NonNull Class<?> clazz) {
        return parsePopup(clazz, null, null);
    }

    /**
     * 解析弹窗
     *
     * @param clazz          弹窗类
     * @param arrowLocation  提示组件位置
     * @param anchorLocation 弹窗位置
     * @return PopupWrapper
     */
    public static PopupAdapter parsePopup(@NonNull Class<?> clazz, Popover.ArrowLocation arrowLocation, PopupWindow.AnchorLocation anchorLocation) {
        PopupAttribute attribute = clazz.getAnnotation(PopupAttribute.class);
        if (attribute == null) {
            throw new RuntimeException("can not find annotation[" + PopupAttribute.class.getSimpleName() + "] from class: " + clazz.getName());
        }
        // 获取弹窗
        PopupAdapter adapter = getPopup(clazz);
        if (adapter != null) {
            adapter.disappear();
        }
        // 创建弹窗
        PopupExt popup = new PopupExt(attribute);
        if (arrowLocation != null) {
            popup.setArrowLocation(arrowLocation);
        } else {
            popup.setArrowLocation(attribute.arrowLocation());
        }
        if (anchorLocation != null) {
            popup.setAnchorLocation(anchorLocation);
        } else {
            popup.setAnchorLocation(attribute.anchorLocation());
        }
        return popup;
    }
}
