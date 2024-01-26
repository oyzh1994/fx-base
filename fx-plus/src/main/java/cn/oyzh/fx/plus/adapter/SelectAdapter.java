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

    /**
     * 子节点是否为空
     *
     * @return 结果
     */
    default boolean isItemEmpty() {
        if (this instanceof TableView<?> view) {
            return view.getItems().isEmpty();
        }
        if (this instanceof ComboBox<?> comboBox) {
            return comboBox.getItems().isEmpty();
        }
        return true;
    }

    /**
     * 添加子节点
     *
     * @param item 子节点
     */
    default void addItem(Object item) {
        if (item != null) {
            if (this instanceof TableView view) {
                FXUtil.runWait(() -> view.getItems().add(item));
            } else if (this instanceof ComboBox comboBox) {
                FXUtil.runWait(() -> comboBox.getItems().add(item));
            }
        }
    }

    /**
     * 添加子节点
     *
     * @param items 子节点列表
     */
    default void addItem(List<?> items) {
        if (items != null && !items.isEmpty()) {
            if (this instanceof TableView view) {
                FXUtil.runWait(() -> view.getItems().addAll(items));
            } else if (this instanceof ComboBox comboBox) {
                FXUtil.runWait(() -> comboBox.getItems().addAll(items));
            }
        }
    }

    /**
     * 添加子节点
     *
     * @param index 下标
     * @param item  子节点
     */
    default void addItem(int index, Object item) {
        if (item != null) {
            if (this instanceof TableView view) {
                FXUtil.runWait(() -> view.getItems().add(index, item));
            } else if (this instanceof ComboBox comboBox) {
                FXUtil.runWait(() -> comboBox.getItems().add(index, item));
            }
        }
    }

    /**
     * 设置子节点
     *
     * @param index 下标
     * @param item  子节点
     */
    default void setItem(int index, Object item) {
        if (item != null && index >= 0) {
            FXUtil.runWait(() -> {
                if (this instanceof TableView view) {
                    if (view.getItems().size() <= index) {
                        view.getItems().add(item);
                    } else {
                        view.getItems().set(index, item);
                    }
                }
            });
        }
    }

    /**
     * 设置子节点
     *
     * @param item 子节点
     */
    default void setItem(Object item) {
        if (item != null) {
            FXUtil.runWait(() -> {
                if (this instanceof TableView view) {
                    view.getItems().setAll(item);
                } else if (this instanceof ComboBox comboBox) {
                    comboBox.getItems().setAll(item);
                }
            });
        }
    }

    /**
     * 设置子节点
     *
     * @param items 子节点列表
     */
    default void setItem(List<?> items) {
        if (items != null) {
            FXUtil.runWait(() -> {
                if (this instanceof TableView view) {
                    view.getItems().setAll(items);
                } else if (this instanceof ComboBox comboBox) {
                    comboBox.getItems().setAll(items);
                }
            });
        }
    }

    /**
     * 获取子节点
     *
     * @param index 下标
     * @return 子节点
     */
    default Object getItem(int index) {
        if (index >= 0) {
            if (this instanceof TableView<?> view) {
                if (view.getItems().size() <= index) {
                    return null;
                }
                return view.getItems().get(index);
            } else if (this instanceof ComboBox<?> comboBox) {
                if (comboBox.getItems().size() <= index) {
                    return null;
                }
                return comboBox.getItems().get(index);
            }
        }
        return null;
    }

    /**
     * 移除子节点
     *
     * @param index 下标
     */
    default void removeItem(int index) {
        if (index >= 0) {
            if (this instanceof TableView<?> view) {
                if (view.getItems().size() >= index) {
                    FXUtil.runWait(() -> view.getItems().remove(index));
                }
            } else if (this instanceof ComboBox<?> comboBox) {
                if (comboBox.getItems().size() >= index) {
                    FXUtil.runWait(() -> comboBox.getItems().remove(index));
                }
            }
        }
    }

    /**
     * 移除子节点
     *
     * @param item 子节点
     */
    default void removeItem(Object item) {
        if (item != null) {
            if (this instanceof TableView<?> view) {
                FXUtil.runWait(() -> view.getItems().remove(item));
            } else if (this instanceof ComboBox<?> comboBox) {
                FXUtil.runWait(() -> comboBox.getItems().remove(item));
            }
        }
    }
}

