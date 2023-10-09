package cn.oyzh.fx.plus.adapter;

import cn.oyzh.fx.plus.util.FXUtil;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeView;
import lombok.NonNull;

import java.util.List;


/**
 * 选择组件适配器
 *
 * @param <T> 数据类型
 */
public interface SelectAdapter<T> extends PropAdapter {

    /**
     * 清空节点
     */
    default void clearItems() {
        FXUtil.runWait(() -> {
            if (this instanceof TreeView<?> view) {
                view.getRoot().getChildren().clear();
                view.setRoot(null);
            } else if (this instanceof TableView<?> view) {
                view.getItems().clear();
            } else if (this instanceof TabPane tabPane) {
                tabPane.getTabs().clear();
            } else if (this instanceof ComboBox<?> comboBox) {
                comboBox.getItems().clear();
            } else if (this instanceof ListView<?> listView) {
                listView.getItems().clear();
            }
        });
    }

    /**
     * 选中首个
     */
    default void selectFirst() {
        FXUtil.runWait(() -> {
            if (this instanceof TreeView<?> view) {
                view.getSelectionModel().selectFirst();
            } else if (this instanceof TableView<?> view) {
                view.getSelectionModel().selectFirst();
            } else if (this instanceof TabPane tabPane) {
                tabPane.getSelectionModel().selectFirst();
            } else if (this instanceof ComboBox<?> comboBox) {
                comboBox.getSelectionModel().selectFirst();
            } else if (this instanceof ListView<?> listView) {
                listView.getSelectionModel().selectFirst();
            }
        });
    }

    /**
     * 选中索引位置数据
     *
     * @param index 索引
     */
    default void select(int index) {
        FXUtil.runWait(() -> {
            if (this instanceof TreeView<?> view) {
                view.getSelectionModel().select(index);
            } else if (this instanceof TableView<?> view) {
                view.getSelectionModel().select(index);
            } else if (this instanceof TabPane tabPane) {
                tabPane.getSelectionModel().select(index);
            } else if (this instanceof ComboBox<?> comboBox) {
                comboBox.getSelectionModel().select(index);
            } else if (this instanceof ListView<?> listView) {
                listView.getSelectionModel().select(index);
            }
        });
    }

    /**
     * 选中数据
     *
     * @param obj 数据
     */
    default void select(T obj) {
        FXUtil.runWait(() -> {
            if (this instanceof TreeView view) {
                view.getSelectionModel().select(obj);
            } else if (this instanceof TableView view) {
                view.getSelectionModel().select(obj);
            } else if (this instanceof TabPane tabPane) {
                tabPane.getSelectionModel().select((Tab) obj);
            } else if (this instanceof ComboBox comboBox) {
                comboBox.getSelectionModel().select(obj);
            } else if (this instanceof ListView listView) {
                listView.getSelectionModel().select(obj);
            }
        });
    }

    /**
     * 获取已选择的数据
     *
     * @return 结果
     */
    default T getSelectedItem() {
        Object o = null;
        if (this instanceof TreeView<?> view) {
            o = view.getSelectionModel().getSelectedItem();
        } else if (this instanceof TableView<?> view) {
            o = view.getSelectionModel().getSelectedItem();
        } else if (this instanceof TabPane tabPane) {
            o = tabPane.getSelectionModel().getSelectedItem();
        } else if (this instanceof ComboBox<?> comboBox) {
            o = comboBox.getSelectionModel().getSelectedItem();
        } else if (this instanceof ListView<?> listView) {
            o = listView.getSelectionModel().getSelectedItem();
        }
        return (T) o;
    }

    /**
     * 获取已选择的数据列表
     *
     * @return 结果
     */
    default List<T> getSelectedItems() {
        List<?> o = null;
        if (this instanceof TreeView<?> view) {
            o = view.getSelectionModel().getSelectedItems();
        } else if (this instanceof TableView<?> view) {
            o = view.getSelectionModel().getSelectedItems();
        } else if (this instanceof ListView<?> listView) {
            o = listView.getSelectionModel().getSelectedItems();
        }
        return (List<T>) o;
    }

    /**
     * 获取已选择的索引
     *
     * @return 结果
     */
    default int getSelectedIndex() {
        if (this instanceof TreeView<?> view) {
            return view.getSelectionModel().getSelectedIndex();
        } else if (this instanceof TableView<?> view) {
            return view.getSelectionModel().getSelectedIndex();
        } else if (this instanceof TabPane tabPane) {
            return tabPane.getSelectionModel().getSelectedIndex();
        } else if (this instanceof ComboBox<?> comboBox) {
            return comboBox.getSelectionModel().getSelectedIndex();
        } else if (this instanceof ListView<?> listView) {
            return listView.getSelectionModel().getSelectedIndex();
        }
        return -1;
    }

    /**
     * 选中索引变化事件
     *
     * @param listener 监听器
     */
    default void selectedIndexChanged(@NonNull ChangeListener<Number> listener) {
        if (this instanceof TreeView<?> view) {
            view.getSelectionModel().selectedIndexProperty().addListener((observableValue, t, t1) -> {
                if (!this.isIgnoreChanged()) {
                    listener.changed(observableValue, t, t1);
                }
            });
        } else if (this instanceof TableView<?> view) {
            view.getSelectionModel().selectedIndexProperty().addListener((observableValue, t, t1) -> {
                if (!this.isIgnoreChanged()) {
                    listener.changed(observableValue, t, t1);
                }
            });
        } else if (this instanceof TabPane tabPane) {
            tabPane.getSelectionModel().selectedIndexProperty().addListener((observableValue, t, t1) -> {
                if (!this.isIgnoreChanged()) {
                    listener.changed(observableValue, t, t1);
                }
            });
        } else if (this instanceof ComboBox<?> comboBox) {
            comboBox.getSelectionModel().selectedIndexProperty().addListener((observableValue, t, t1) -> {
                if (!this.isIgnoreChanged()) {
                    listener.changed(observableValue, t, t1);
                }
            });
        } else if (this instanceof ListView<?> listView) {
            listView.getSelectionModel().selectedIndexProperty().addListener((observableValue, t, t1) -> {
                if (!this.isIgnoreChanged()) {
                    listener.changed(observableValue, t, t1);
                }
            });
        }
    }

    /**
     * 清除选中内容
     */
    default void clearSelection() {
        FXUtil.runWait(() -> {
            if (this instanceof TreeView<?> view) {
                view.getSelectionModel().clearSelection();
            } else if (this instanceof TableView<?> view) {
                view.getSelectionModel().clearSelection();
            } else if (this instanceof TabPane tabPane) {
                tabPane.getSelectionModel().clearSelection();
            } else if (this instanceof ComboBox<?> comboBox) {
                comboBox.getSelectionModel().clearSelection();
            } else if (this instanceof ListView<?> listView) {
                listView.getSelectionModel().clearSelection();
            }
        });
    }

    /**
     * 获取初始索引
     *
     * @return 初始索引
     */
    default int getInitIndex() {
        Object _initIndex = this.getProp("_initIndex");
        return _initIndex instanceof Integer ? (int) _initIndex : -1;
    }

    /**
     * 设置初始索引
     *
     * @param initIndex 初始索引
     */
    default void setInitIndex(int initIndex) {
        this.select(initIndex);
        this.setProp("_initIndex", initIndex);
    }

    /**
     * 是否忽略节点改变事件
     *
     * @return 结果
     */
    default boolean isIgnoreChanged() {
        Object _ignoreChanged = this.getProp("_ignoreChanged");
        return _ignoreChanged instanceof Boolean && (boolean) _ignoreChanged;
    }

    /**
     * 设置忽略节点改变事件
     *
     * @param ignoreChanged 忽略节点改变事件
     */
    default void setIgnoreChanged(boolean ignoreChanged) {
        this.setProp("_ignoreChanged", ignoreChanged);
    }


}

