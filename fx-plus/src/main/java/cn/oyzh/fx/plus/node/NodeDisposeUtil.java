package cn.oyzh.fx.plus.node;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.object.Destroyable;
import cn.oyzh.common.util.ReflectUtil;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
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
import java.util.Collection;
import java.util.Collections;

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
                unbindProperty(item);
            }
        } else if (node instanceof TreeView<?> view) {
            dispose(view.getRoot());
        } else if (node instanceof TreeItem<?> treeItem) {
            for (TreeItem<?> item : treeItem.getChildren()) {
                unbindProperty(item);
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
            unbindProperty(node1);
        } else if (!(node instanceof Destroyable)) {
            JulLog.warn("UnSupport type:{}", node.getClass());
        }
        // 自定义处理
        if (node instanceof Destroyable destroyable) {
            destroyable.destroy();
        }
        unbindProperty(node);
    }

    /**
     * 解绑属性
     *
     * @param object 节点
     */
    public static void unbindProperty(Object object) {
        if (object == null) {
            return;
        }
        // 获取所有字段
        Field[] fields = ReflectUtil.getFields(object.getClass(), true, true);
        for (Field field : fields) {
            try {
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
                        property.unbind();
                    }
                } else if (FXCollections.class.isAssignableFrom(clazz)) {// 集合类型
                    Collection<?> collection = (Collection<?>) value;
                    // 清除结果
                    if (collection != null) {
                        collection.clear();
                    }
                } else if (Collection.class.isAssignableFrom(clazz)) {// 集合类型
                    Collection<?> collection = (Collection<?>) value;
                    // 清除结果
                    if (collection != null) {
                        collection.clear();
                    }
                // } else if (Observable.class.isAssignableFrom(clazz)) {// 可观察对象
                // } else if (Node.class.isAssignableFrom(clazz)) {// node
                // } else if (Object.class.isAssignableFrom(clazz)) {// 对象类型
                //     int modifiers = field.getModifiers();
                //     // 设置为null
                //     if (!Modifier.isFinal(modifiers) &&
                //             !Modifier.isStatic(modifiers) &&
                //             !field.isEnumConstant()
                //     ) {
                //         field.set(object, null);
                //     }
                    // } else {
                    //     JulLog.warn("UnSupport clazz:{}", clazz);
                }
            } catch (Exception ignore) {
            }
        }
    }
}
