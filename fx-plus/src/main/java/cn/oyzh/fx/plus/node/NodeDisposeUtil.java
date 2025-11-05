package cn.oyzh.fx.plus.node;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.object.Destroyable;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.ReflectUtil;
import javafx.beans.property.Property;
import javafx.event.EventTarget;
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
import javafx.stage.Window;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;

/**
 * 节点销毁工具类
 *
 * @author oyzh
 * @since 2025-06-12
 */
public class NodeDisposeUtil {

    /**
     * 销毁
     *
     * @param node 节点
     */
    public static void dispose(Object node) {
        // 异步执行
        ThreadUtil.startVirtual(() -> doDispose(node));
    }

    /**
     * 执行销毁
     *
     * @param node 节点
     */
    private static void doDispose(Object node) {
        if (node instanceof Window window) {
            dispose(window.getScene());
        } else if (node instanceof Scene scene) {
            dispose(scene.getRoot());
        } else if (node instanceof TabPane pane) {
            for (Tab tab : pane.getTabs()) {
                dispose(tab);
            }
        } else if (node instanceof Pane pane) {
            for (Node item : pane.getChildren()) {
                dispose(item);
            }
        } else if (node instanceof ListView<?> view) {
            for (Object item : view.getItems()) {
                dispose(item);
            }
        } else if (node instanceof TableView<?> view) {
            for (Object item : view.getItems()) {
                dispose(item);
            }
        } else if (node instanceof ComboBox<?> comboBox) {
            for (Object item : comboBox.getItems()) {
                dispose(item);
            }
        } else if (node instanceof ChoiceBox<?> choiceBox) {
            for (Object item : choiceBox.getItems()) {
                dispose(item);
            }
        } else if (node instanceof TableColumnBase<?, ?> columnBase) {
            for (TableColumnBase<?, ?> item : columnBase.getColumns()) {
                disposeField(item);
            }
        } else if (node instanceof TreeView<?> view) {
            dispose(view.getRoot());
        } else if (node instanceof TreeItem<?> treeItem) {
            for (TreeItem<?> item : treeItem.getChildren()) {
                disposeField(item);
            }
        } else if (node instanceof Parent parent) {
            for (Node item : parent.getChildrenUnmodifiable()) {
                dispose(item);
            }
        } else if (node instanceof Tab tab) {
            dispose(tab.getContent());
            // } else if (node instanceof Shape shape) {
            //     unbindProperty(shape);
        } else if (node instanceof Node node1) {
            disposeField(node1);
        } else if (!(node instanceof Destroyable)) {
            JulLog.warn("UnSupport type:{}", node.getClass());
        }
        // 自定义处理
        if (node instanceof Destroyable destroyable) {
            destroyable.destroy();
        }
        disposeField(node);
    }

    /**
     * 销毁字段
     *
     * @param object 节点
     */
    private static void disposeField(Object object) {
        if (object == null) {
            return;
        }
        // 获取所有字段
        Field[] fields = ReflectUtil.getFields(object.getClass(), true, true);
        for (Field field : fields) {
            try {
                // 修饰符
                int modifiers = field.getModifiers();
                if (!Modifier.isFinal(modifiers) ||
                        !Modifier.isStatic(modifiers) ||
                        field.getType() == byte.class ||
                        field.getType() == boolean.class ||
                        field.getType() == char.class ||
                        field.getType() == int.class ||
                        field.getType() == double.class ||
                        field.getType() == float.class ||
                        field.getType() == long.class
                ) {
                    continue;
                }
                // 可设置为null
                boolean setNullable = false;
                // 过滤属性类型
                Class<?> clazz = field.getType();
                // 设置可访问
                field.setAccessible(true);
                // 获取属性值
                Object value = field.get(object);
                // 属性类型
                if (Property.class.isAssignableFrom(clazz)) {
                    Property<?> property = (Property<?>) value;
                    // 解绑属性
                    if (property != null) {
                        disposeField(property.getBean());
                        disposeField(property.getValue());
                        property.unbind();
                    }
                    setNullable = true;
                } else if (Collection.class.isAssignableFrom(clazz)) {// 集合类型
                    Collection<?> collection = (Collection<?>) value;
                    // 清除结果
                    if (collection != null) {
                        for (Object o : collection) {
                            disposeField(o);
                        }
                        collection.clear();
                    }
                    setNullable = true;
                } else if (EventTarget.class.isAssignableFrom(clazz)) {// javafx
                    disposeField(value);
                    setNullable = true;
                } else if (Object.class.isAssignableFrom(clazz)) {// 对象
                    setNullable = true;
                }
                // 设置为null
                if (setNullable) {
                    field.set(object, null);
                }
            } catch (Exception ignore) {
            }
        }
    }
}
