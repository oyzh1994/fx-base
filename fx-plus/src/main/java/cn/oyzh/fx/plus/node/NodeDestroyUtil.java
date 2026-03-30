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

    // /**
    //  * 销毁
    //  *
    //  * @param node 节点
    //  */
    // public static void destroy(Object node) {
    //     if (node != null) {
    //         // System.out.println("destroy:" + node);
    //         // // 异步执行
    //         // ThreadUtil.startVirtual(() -> doDestroy(node));
    //         doDestroy(node);
    //     }
    // }
    //
    // /**
    //  * 执行销毁
    //  *
    //  * @param node 节点
    //  */
    // private static void doDestroy(Object node) {
    //     if (node instanceof Destroyable destroyable) {
    //         destroyable.destroy();
    //     }
    //     if (node instanceof TableColumnBase<?, ?> columnBase) {
    //         destroyField(columnBase);
    //     } else if (node instanceof TreeItem<?> treeItem) {
    //         destroyField(treeItem);
    //     } else if (node instanceof Tab tab) {
    //         destroyField(tab);
    //     } else if (node instanceof Node node1) {
    //         destroyField(node1);
    //     } else {
    //         destroyField(node);
    //     }
    // }
    //
    // /**
    //  * 销毁字段
    //  *
    //  * @param object 节点
    //  */
    // private static void destroyField(Object object) {
    //     if (object == null) {
    //         return;
    //     }
    //     // 获取所有字段
    //     Field[] fields = ReflectUtil.getFields(object.getClass(), true, true);
    //     for (Field field : fields) {
    //         try {
    //             // 修饰符
    //             int modifiers = field.getModifiers();
    //             if (Modifier.isFinal(modifiers) ||
    //                     Modifier.isStatic(modifiers) ||
    //                     field.getType() == int.class ||
    //                     field.getType() == byte.class ||
    //                     field.getType() == char.class ||
    //                     field.getType() == long.class ||
    //                     field.getType() == float.class ||
    //                     field.getType() == double.class ||
    //                     field.getType() == boolean.class
    //             ) {
    //                 continue;
    //             }
    //             // 过滤属性类型
    //             Class<?> clazz = field.getType();
    //             if (!field.trySetAccessible()) {
    //                 continue;
    //             }
    //             // // 可设置为null
    //             boolean setNullable = false;
    //             Object object1 = field.get(object);
    //             if (object1 == null || object1 == object) {
    //                 //
    //             } else if (CharSequence.class.isAssignableFrom(clazz) ||
    //                     Character.class.isAssignableFrom(clazz)
    //                     || Double.class.isAssignableFrom(clazz)
    //                     || Float.class.isAssignableFrom(clazz)
    //                     || Integer.class.isAssignableFrom(clazz)
    //                     || Long.class.isAssignableFrom(clazz)
    //                     || Short.class.isAssignableFrom(clazz)
    //                     || Boolean.class.isAssignableFrom(clazz)
    //                     || Byte.class.isAssignableFrom(clazz)) {
    //                 setNullable = true;
    //             } else if (Collection.class.isAssignableFrom(clazz)) {
    //                 Collection<?> collection = (Collection<?>) object1;
    //                 for (Object o : collection) {
    //                     if (o != object && o != object1) {
    //                         destroyFieldShallow(o);
    //                     }
    //                 }
    //                 collection.clear();
    //                 setNullable = true;
    //             } else if (Property.class.isAssignableFrom(clazz)) {// 属性类型
    //                 // 设置可访问
    //                 field.setAccessible(true);
    //                 // 获取属性值
    //                 Property<?> property = (Property<?>) object1;
    //                 // 解绑属性
    //                 Object value = property.getValue();
    //                 if (value != object && value != object1) {
    //                     destroyFieldShallow(value);
    //                 }
    //                 property.unbind();
    //                 setNullable = true;
    //             }
    //             // 设置为null
    //             if (setNullable) {
    //                 field.set(object, null);
    //             }
    //         } catch (Throwable ex) {
    //             ex.printStackTrace();
    //         }
    //     }
    // }
    //
    // /**
    //  * 销毁字段
    //  *
    //  * @param object 节点
    //  */
    // private static void destroyFieldShallow(Object object) {
    //     if (object == null) {
    //         return;
    //     }
    //     // 获取所有字段
    //     Field[] fields = ReflectUtil.getFields(object.getClass(), true, true);
    //     for (Field field : fields) {
    //         try {
    //             // 修饰符
    //             int modifiers = field.getModifiers();
    //             if (Modifier.isFinal(modifiers) ||
    //                     Modifier.isStatic(modifiers) ||
    //                     field.getType() == int.class ||
    //                     field.getType() == byte.class ||
    //                     field.getType() == char.class ||
    //                     field.getType() == long.class ||
    //                     field.getType() == float.class ||
    //                     field.getType() == double.class ||
    //                     field.getType() == boolean.class
    //             ) {
    //                 continue;
    //             }
    //             // 过滤属性类型
    //             Class<?> clazz = field.getType();
    //             if (!field.trySetAccessible()) {
    //                 continue;
    //             }
    //             // // 可设置为null
    //             boolean setNullable = false;
    //             Object object1 = field.get(object);
    //             if (object1 == null || object1 == object) {
    //                 setNullable = false;
    //             } else if (CharSequence.class.isAssignableFrom(clazz) ||
    //                     Character.class.isAssignableFrom(clazz)
    //                     || Double.class.isAssignableFrom(clazz)
    //                     || Float.class.isAssignableFrom(clazz)
    //                     || Integer.class.isAssignableFrom(clazz)
    //                     || Long.class.isAssignableFrom(clazz)
    //                     || Short.class.isAssignableFrom(clazz)
    //                     || Boolean.class.isAssignableFrom(clazz)
    //                     || Byte.class.isAssignableFrom(clazz)) {
    //                 setNullable = true;
    //             } else if (Collection.class.isAssignableFrom(clazz)) {
    //                 Collection<?> collection = (Collection<?>) object1;
    //                 collection.clear();
    //             } else if (Property.class.isAssignableFrom(clazz)) {// 属性类型
    //                 // 获取属性值
    //                 Property<?> property = (Property<?>) object1;
    //                 property.unbind();
    //             }
    //             // 设置为null
    //             if (setNullable) {
    //                 field.set(object, null);
    //             }
    //         } catch (Throwable ex) {
    //             ex.printStackTrace();
    //         }
    //     }
    // }

    // private static void destroy(Collection<?> collection) {
    //     if (collection == null) {
    //         return;
    //     }
    //     // if (collection.getClass().getName().contains("FXCollections$UnmodifiableObservableSet")) {
    //     //     return;
    //     // }
    //     collection.clear();
    // }

    // private static void destroy(EventHandlerManager manager) {
    //     if (manager == null) {
    //         return;
    //     }
    //     Map<EventType<? extends Event>, CompositeEventHandler<? extends Event>> map = ReflectUtil.getFieldValue(manager, "eventHandlerMap");
    //     for (CompositeEventHandler<? extends Event> handler : map.values()) {
    //         handler.setEventHandler(null);
    //     }
    //     map.clear();
    //     ReflectUtil.setFieldValue("eventSource", null, manager);
    // }

    // private static void destroy(ObservableMap<?, ?> map) {
    //     if (map == null) {
    //         return;
    //     }
    //     map.clear();
    // }

    private static void destroy(Property<?> property) {
        if (property == null) {
            return;
        }
        // FXUtil.runWait(() -> property.setValue(null));
        // 解除单向绑定
        property.unbind();
    }

    // private static void destroy(Destroyable destroyable) {
    //     if (destroyable == null) {
    //         return;
    //     }
    //     destroyable.destroy();
    // }

    /**
     * 销毁对象
     *
     * @param object 节点
     */
    public static void destroyObject(Object object) {
        // if (object == null) {
        //     return;
        // }
        // Class<?> cType = object.getClass();
        // // 获取所有字段
        // Field[] fields = ReflectUtil.getFields(cType, true, true);
        // for (Field field : fields) {
        //     try {
        //         // 修饰符
        //         int modifiers = field.getModifiers();
        //         if (Modifier.isFinal(modifiers)) {
        //             continue;
        //         }
        //         if (Modifier.isStatic(modifiers)) {
        //             continue;
        //         }
        //         // 过滤属性类型
        //         Class<?> clazz = field.getType();
        //         if (!field.trySetAccessible()) {
        //             continue;
        //         }
        //         boolean setNullable = false;
        //         Object object1 = field.get(object);
        //         if (object1 == null || object1 == object) {
        //
        //         } else if (Property.class.isAssignableFrom(clazz)) { // 属性类型
        //             // 获取属性值
        //             Property<?> object2 = (Property<?>) object1;
        //             destroy(object2);
        //             // // 例外
        //             // if (field.getName().contains("id")
        //             //         || field.getName().contains("scene")
        //             //         || field.getName().contains("style")
        //             //         || field.getName().contains("focused")
        //             //         || field.getName().contains("layoutX")
        //             //         || field.getName().contains("layoutY")
        //             //         || field.getName().contains("disable")
        //             //         || field.getName().contains("disabled")
        //             //         || field.getName().contains("selectionModel")
        //             // ) {
        //             // } else if (field.getName().contains("parent")) {
        //             //     System.out.println(field.getName());
        //             //     setNullable = true;
        //             // }
        //
        //             if (field.getName().equals("parent")) {
        //                 setNullable = true;
        //             }
        //             // } else if (ObservableMap.class.isAssignableFrom(clazz)) {
        //             //     // 获取属性值
        //             //     ObservableMap<?, ?> object2 = (ObservableMap<?, ?>) object1;
        //             //     destroy(object2);
        //             //     setNullable = true;
        //             // } else if (Collection.class.isAssignableFrom(clazz)) {
        //             //     // 获取属性值
        //             //     Collection<?> object2 = (Collection<?>) object1;
        //             //     destroy(object2);
        //             // setNullable = true;
        //             // } else if (EventHandlerManager.class.isAssignableFrom(clazz)) {
        //             //     // 获取属性值
        //             //     EventHandlerManager object2 = (EventHandlerManager) object1;
        //             //     destroy(object2);
        //             //     setNullable = true;
        //             // } else if (Destroyable.class.isAssignableFrom(clazz)) {
        //             //     // 获取属性值
        //             //     Destroyable object2 = (Destroyable) object1;
        //             //     destroy(object2);
        //             //     setNullable = true;
        //             // } else if (InvalidationListener.class.isAssignableFrom(clazz)) {
        //             //     setNullable = true;
        //         // } else if (CharSequence.class.isAssignableFrom(clazz)
        //         //         || Long.class.isAssignableFrom(clazz)
        //         //         || Integer.class.isAssignableFrom(clazz)
        //         //         || Short.class.isAssignableFrom(clazz)
        //         //         || Character.class.isAssignableFrom(clazz)
        //         //         || Byte.class.isAssignableFrom(clazz)
        //         //         || Double.class.isAssignableFrom(clazz)
        //         //         || Float.class.isAssignableFrom(clazz)
        //         //         || Boolean.class.isAssignableFrom(clazz)
        //         //         || Collection.class.isAssignableFrom(clazz)
        //         //         || ObservableMap.class.isAssignableFrom(clazz)
        //         // ) {
        //         //     setNullable = true;
        //         } else if (field.getAnnotation(FXML.class) != null) {
        //             setNullable = true;
        //             // } else {
        //             // System.out.println(clazz);
        //         }
        //         if (setNullable) {
        //             ReflectUtil.setFieldValue(field, null, object);
        //         }
        //     } catch (Throwable ex) {
        //         ex.printStackTrace();
        //     }
        // }
//        doDestroyObject(object);
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
//        if (object instanceof Window window) {
//            Scene scene = window.getScene();
//            if (scene != null) {
//                doDestroyObject(scene.getRoot(), handles);
//                doDestroyObject(scene, handles);
//            }
//        }
        if (object instanceof Parent parent) {
            for (Node node : parent.getChildrenUnmodifiable()) {
                doDestroyObject(node, handles);
            }
        }
//        // 执行一次销毁
//        if (object instanceof Destroyable destroyable) {
//            if (!destroyable.isDestroyed()) {
//                destroyable.markDestroyed();
//                destroyable.destroy();
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
//                if (Modifier.isFinal(modifiers)) {
//                    continue;
//                }
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
                // EventTarget对象
                if (EventTarget.class.isAssignableFrom(clazz)) {
                    doDestroyObject(object1, handles);
                }
                // 属性类型
                if (Property.class.isAssignableFrom(clazz)) {
                    // 获取属性值
                    Property<?> object2 = (Property<?>) object1;
                    destroy(object2);
                    // // 例外
                    // if (field.getName().contains("id")
                    //         || field.getName().contains("scene")
                    //         || field.getName().contains("style")
                    //         || field.getName().contains("focused")
                    //         || field.getName().contains("layoutX")
                    //         || field.getName().contains("layoutY")
                    //         || field.getName().contains("disable")
                    //         || field.getName().contains("disabled")
                    //         || field.getName().contains("selectionModel")
                    // ) {
                    // } else if (field.getName().contains("parent")) {
                    //     System.out.println(field.getName());
                    //     setNullable = true;
                    // }

                    if (field.getName().equals("parent")) {
                        setNullable = true;
                    }
                    // } else if (ObservableMap.class.isAssignableFrom(clazz)) {
                    //     // 获取属性值
                    //     ObservableMap<?, ?> object2 = (ObservableMap<?, ?>) object1;
                    //     destroy(object2);
                    //     setNullable = true;
                    // } else if (Collection.class.isAssignableFrom(clazz)) {
                    //     // 获取属性值
                    //     Collection<?> object2 = (Collection<?>) object1;
                    //     destroy(object2);
                    // setNullable = true;
                    // } else if (EventHandlerManager.class.isAssignableFrom(clazz)) {
                    //     // 获取属性值
                    //     EventHandlerManager object2 = (EventHandlerManager) object1;
                    //     destroy(object2);
                    //     setNullable = true;
                    // } else if (Destroyable.class.isAssignableFrom(clazz)) {
                    //     // 获取属性值
                    //     Destroyable object2 = (Destroyable) object1;
                    //     destroy(object2);
                    //     setNullable = true;
                    // } else if (InvalidationListener.class.isAssignableFrom(clazz)) {
                    //     setNullable = true;
                    // } else if (CharSequence.class.isAssignableFrom(clazz)
                    //         || Long.class.isAssignableFrom(clazz)
                    //         || Integer.class.isAssignableFrom(clazz)
                    //         || Short.class.isAssignableFrom(clazz)
                    //         || Character.class.isAssignableFrom(clazz)
                    //         || Byte.class.isAssignableFrom(clazz)
                    //         || Double.class.isAssignableFrom(clazz)
                    //         || Float.class.isAssignableFrom(clazz)
                    //         || Boolean.class.isAssignableFrom(clazz)
                    //         || Collection.class.isAssignableFrom(clazz)
                    //         || ObservableMap.class.isAssignableFrom(clazz)
                    // ) {
                    //     setNullable = true;
                } else if (field.getAnnotation(FXML.class) != null) {
                    setNullable = true;
                    // } else {
                    // System.out.println(clazz);
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
