package cn.oyzh.fx.plus.node;

import cn.oyzh.common.object.Destroyable;
import cn.oyzh.common.util.ReflectUtil;
import javafx.beans.property.Property;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Shape;
import javafx.stage.Window;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * 节点销毁工具类
 *
 * @author oyzh
 * @since 2025-06-12
 */
public class NodeDestroyUtil {

    /**
     * 销毁
     *
     * @param node 节点
     */
    public static void destroy(Object node) {
        if (node != null) {
            // System.out.println("destroy:" + node);
            // // 异步执行
            // ThreadUtil.startVirtual(() -> doDestroy(node));
            doDestroy(node);
        }
    }

    /**
     * 执行销毁
     *
     * @param node 节点
     */
    private static void doDestroy(Object node) {
        if (node instanceof Destroyable destroyable) {
            destroyable.destroy();
        }
        if (node instanceof Window window) {
            doDestroy(window.getScene());
        } else if (node instanceof Scene scene) {
            doDestroy(scene.getRoot());
        } else if (node instanceof TabPane pane) {
            for (Tab tab : pane.getTabs()) {
                doDestroy(tab);
            }
        } else if (node instanceof Pane pane) {
            for (Node item : pane.getChildren()) {
                doDestroy(item);
            }
        } else if (node instanceof ListView<?> view) {
            for (Object item : view.getItems()) {
                doDestroy(item);
            }
        } else if (node instanceof TableView<?> view) {
            for (Object item : view.getItems()) {
                doDestroy(item);
            }
        } else if (node instanceof ComboBox<?> comboBox) {
            for (Object item : comboBox.getItems()) {
                doDestroy(item);
            }
        } else if (node instanceof ChoiceBox<?> choiceBox) {
            for (Object item : choiceBox.getItems()) {
                doDestroy(item);
            }
        } else if (node instanceof TableColumnBase<?, ?> columnBase) {
            for (TableColumnBase<?, ?> item : columnBase.getColumns()) {
                destroyField(item);
            }
        } else if (node instanceof TreeView<?> view) {
            doDestroy(view.getRoot());
        } else if (node instanceof TreeItem<?> treeItem) {
            for (TreeItem<?> item : treeItem.getChildren()) {
                destroyField(item);
            }
        } else if (node instanceof Parent parent) {
            for (Node item : parent.getChildrenUnmodifiable()) {
                doDestroy(item);
            }
        } else if (node instanceof Tab tab) {
            doDestroy(tab.getContent());
        } else if (node instanceof Shape shape) {
            destroyField(shape);
        } else if (node instanceof Node node1) {
            destroyField(node1);
        } else {
            destroyField(node);
        }
    }

    /**
     * 销毁字段
     *
     * @param object 节点
     */
    private static void destroyField(Object object) {
        if (object == null) {
            return;
        }
        // 获取所有字段
        Field[] fields = ReflectUtil.getFields(object.getClass(), true, true);
        for (Field field : fields) {
            try {
                // 修饰符
                int modifiers = field.getModifiers();
                if (Modifier.isFinal(modifiers) ||
                        Modifier.isStatic(modifiers) ||
                        field.getType() == int.class ||
                        field.getType() == byte.class ||
                        field.getType() == char.class ||
                        field.getType() == long.class ||
                        field.getType() == float.class ||
                        field.getType() == double.class ||
                        field.getType() == boolean.class
                ) {
                    continue;
                }
                // 过滤属性类型
                Class<?> clazz = field.getType();
                // 设置可访问
                field.setAccessible(true);
                // 获取属性值
                Object value = field.get(object);
                if(value == null){
                    continue;
                }
                // // 可设置为null
                // boolean setNullable = false;
                // 属性类型
                if (Property.class.isAssignableFrom(clazz)) {
                    Property<?> property = (Property<?>) value;
                    // 解绑属性
                    if (property != null) {
                        property.unbind();
                    }
                    // } else if (Destroyable.class.isAssignableFrom(clazz)) {
                    //     Destroyable destroyable = (Destroyable) value;
                    //     // 销毁组件
                    //     if (destroyable != null) {
                    //         destroyable.destroy();
                    //     }
                    // } else if (Collection.class.isAssignableFrom(clazz)) {// 集合类型
                    //     Collection<?> collection = (Collection<?>) value;
                    //     // 清除结果
                    //     if (collection != null) {
                    //         for (Object o : collection) {
                    //             if (o != value && o != object) {
                    //                 destroyField(o);
                    //             }
                    //         }
                    //     }
                    // } else if (EventTarget.class.isAssignableFrom(clazz)) {// javafx
                    //     if (value != object) {
                    //         destroyField(value);
                    //     }
                    // } else if (String.class.isAssignableFrom(clazz)) {// 对象
                    //     if (value != null) {
                    //         setNullable = true;
                    //     }
                }
                // 设置为null
                // if (setNullable) {
                // field.set(object, null);
                // }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
