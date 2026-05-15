package cn.oyzh.fx.plus.node;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.ReflectUtil;
import javafx.beans.property.Property;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * 节点销毁工具类
 *
 * @author oyzh
 * @since 2025-06-12
 */
public class NodeDestroyUtil {

    private static void destroy(Property<?> property) {
        // 解除单向绑定
        if (property != null) {
            property.unbind();
        }
    }

    /**
     * 销毁对象
     *
     * @param object 节点
     */
    public static void destroyObject(Object object) {
        // 异步执行
        ThreadUtil.startVirtual(() -> doDestroyObject(object));
    }

    /**
     * 销毁对象
     *
     * @param object 对象
     */
    private static void doDestroyObject(Object object) {
        if (object != null) {
            List<Object> handles = new ArrayList<>();
            doDestroyObject(object, handles);
            handles.clear();
        }
    }

    /**
     * 销毁对象
     *
     * @param object  对象
     * @param handles 已处理对象列表
     */
    private static void doDestroyObject(Object object, List<Object> handles) {
        if (object == null) {
            return;
        }
        // 已处理跳过
        if (handles.contains(object)) {
            return;
        }
        // 添加到列表
        handles.add(object);
//        if (object instanceof Parent parent) {
//            for (Node node : new ArrayList<>(parent.getChildrenUnmodifiable())) {
//                doDestroyObject(node, handles);
//            }
//        }
        Class<?> cType = object.getClass();
        // 获取所有字段
        Field[] fields = ReflectUtil.getFields(cType, true, true);
        for (Field field : fields) {
            try {
                // 修饰符
                int modifiers = field.getModifiers();
                boolean isFinal = Modifier.isFinal(modifiers);
                if (Modifier.isStatic(modifiers)) {
                    continue;
                }
                // 获取属性类型
                Class<?> clazz = field.getType();
                if (!field.trySetAccessible()) {
                    continue;
                }
                // 是否可以设置为null
                boolean setNullable = false;
                // 获取对象
                Object object1 = field.get(object);
                // 对象为null
                if (object1 == null) {
                    continue;
                }
//                // EventTarget对象
//                if (EventTarget.class.isAssignableFrom(clazz)) {
//                    doDestroyObject(object1, handles);
//                }
                // fmxl注入对象
                if (field.getAnnotation(FXML.class) != null) {
                    // EventTarget对象，递归销毁
                    if (EventTarget.class.isAssignableFrom(clazz)) {
                        doDestroyObject(object1, handles);
                    }
                    setNullable = true;
                }
                // 属性类型
                if (Property.class.isAssignableFrom(clazz)) {
                    // 获取属性值
                    Property<?> object2 = (Property<?>) object1;
                    destroy(object2);
                    if (field.getName().equals("parent")) {
                        setNullable = true;
                    }
                }
                if (setNullable && !isFinal) {
                    ReflectUtil.setFieldValue(field, null, object);
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }
}
