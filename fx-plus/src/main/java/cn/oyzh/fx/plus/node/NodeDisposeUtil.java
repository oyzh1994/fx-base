package cn.oyzh.fx.plus.node;

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
import javafx.stage.Window;

import java.lang.reflect.Field;

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
        }
        unbindProperty(node);
    }

    /**
     * 解绑属性
     *
     * @param object 节点
     */
    public static void unbindProperty(Object object) {
        // 获取所有字段
        Field[] fields = ReflectUtil.getFields(object.getClass(), true, true);
        for (Field field : fields) {
            try {
                // 过滤属性类型
                Class<?> clazz = field.getType();
                // 属性类型
                if (Property.class.isAssignableFrom(clazz)) {
                    field.setAccessible(true);
                    // 获取属性值
                    Object value = field.get(object);
                    // 如果值为Property，解绑属性
                    if (value instanceof Property<?> property) {
                        property.unbind();
                        System.out.println("property:" + property.getName() + " unbind");
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
