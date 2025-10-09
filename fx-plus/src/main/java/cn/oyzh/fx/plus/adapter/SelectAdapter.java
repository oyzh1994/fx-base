package cn.oyzh.fx.plus.adapter;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


/**
 * 选择组件适配器
 *
 * @author oyzh
 * @since 2023/4/11
 * @param <T> 数据类型
 */
public interface SelectAdapter<T> extends PropAdapter {

    /**
     * 清空节点
     */
    default void clearItems() {
        FXUtil.runWait(() -> {
            if (this instanceof TreeView<?> node) {
                node.getRoot().getChildren().clear();
                node.setRoot(null);
            } else if (this instanceof TreeTableView<?> node) {
                node.getRoot().getChildren().clear();
                node.setRoot(null);
            } else if (this instanceof TableView<?> node) {
                node.getItems().clear();
            } else if (this instanceof TabPane node) {
                node.getTabs().clear();
            } else if (this instanceof ComboBox<?> node) {
                node.getItems().clear();
            } else if (this instanceof ListView<?> node) {
                node.getItems().clear();
            }
        });
    }

    /**
     * 选中首个
     */
    default void selectFirst() {
        FXUtil.runWait(() -> {
            if (this instanceof TreeView<?> node) {
                node.getSelectionModel().selectFirst();
            } else if (this instanceof TreeTableView<?> node) {
                node.getSelectionModel().selectFirst();
            } else if (this instanceof TableView<?> node) {
                node.getSelectionModel().selectFirst();
            } else if (this instanceof TabPane node) {
                node.getSelectionModel().selectFirst();
            } else if (this instanceof ComboBox<?> node) {
                node.getSelectionModel().selectFirst();
            } else if (this instanceof ListView<?> node) {
                node.getSelectionModel().selectFirst();
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
            if (this instanceof TreeView<?> node) {
                node.getSelectionModel().select(index);
            } else if (this instanceof TreeTableView<?> node) {
                node.getSelectionModel().select(index);
            } else if (this instanceof TableView<?> node) {
                node.getSelectionModel().select(index);
            } else if (this instanceof TabPane node) {
                node.getSelectionModel().select(index);
                // TODO: 需要让内容获取焦点，不然可能会导致后续切换无效
                Tab tab = (Tab) this.getItem(index);
                if (tab != null && tab.getContent() != null) {
                    tab.getContent().requestFocus();
                }
            } else if (this instanceof ComboBox<?> node) {
                node.getSelectionModel().select(index);
            } else if (this instanceof ListView<?> node) {
                node.getSelectionModel().select(index);
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
            if (this instanceof TreeView node) {
                node.getSelectionModel().select(obj);
            } else if (this instanceof TreeTableView node) {
                node.getSelectionModel().select(obj);
            } else if (this instanceof TableView node) {
                node.getSelectionModel().select(obj);
            } else if (this instanceof TabPane node) {
                Tab tab = (Tab) obj;
                node.getSelectionModel().select(tab);
                // TODO: 需要让内容获取焦点，不然可能会导致后续切换无效
                if (tab != null && tab.getContent() != null) {
                    tab.getContent().requestFocus();
                }
            } else if (this instanceof ComboBox node) {
                node.getSelectionModel().select(obj);
            } else if (this instanceof ListView node) {
                node.getSelectionModel().select(obj);
            }
        });
    }

    /**
     * 如果为null，则选择首个元素
     *
     * @param obj 对象
     */
    default void selectFirstIfNull(T obj) {
        if (obj == null) {
            this.select(0);
        } else {
            this.select(obj);
        }
    }

    /**
     * 选中对象
     *
     * @param obj 对象
     */
    default void selectObj(Object obj) {
        FXUtil.runWait(() -> {
            if (this instanceof TreeView node) {
                node.getSelectionModel().select(obj);
            } else if (this instanceof TreeTableView node) {
                node.getSelectionModel().select(obj);
            } else if (this instanceof TableView node) {
                node.getSelectionModel().select(obj);
            } else if (this instanceof TabPane node) {
                node.getSelectionModel().select((Tab) obj);
            } else if (this instanceof ComboBox node) {
                node.getSelectionModel().select(obj);
            } else if (this instanceof ListView node) {
                node.getSelectionModel().select(obj);
            }
        });
    }

    /**
     * 获取已选择的数据
     *
     * @return 结果
     */
    default T getSelectedItem() {
        AtomicReference<T> ref = new AtomicReference<>();
        FXUtil.runWait(() -> {
            Object o = null;
            if (this instanceof TreeView<?> node) {
                o = node.getSelectionModel().getSelectedItem();
            } else if (this instanceof TreeTableView<?> node) {
                o = node.getSelectionModel().getSelectedItem();
            } else if (this instanceof TableView<?> node) {
                o = node.getSelectionModel().getSelectedItem();
            } else if (this instanceof TabPane node) {
                o = node.getSelectionModel().getSelectedItem();
            } else if (this instanceof ComboBox<?> node) {
                o = node.getSelectionModel().getSelectedItem();
            } else if (this instanceof ListView<?> node) {
                o = node.getSelectionModel().getSelectedItem();
            }
            ref.set((T) o);
        });
        return ref.get();
    }

    /**
     * 获取已选择的数据列表
     *
     * @return 结果
     */
    default List<T> getSelectedItems() {
        List<?> o = null;
        if (this instanceof TreeView<?> node) {
            o = node.getSelectionModel().getSelectedItems();
        } else if (this instanceof TreeTableView<?> node) {
            o = node.getSelectionModel().getSelectedItems();
        } else if (this instanceof TableView<?> node) {
            o = node.getSelectionModel().getSelectedItems();
        } else if (this instanceof ListView<?> node) {
            o = node.getSelectionModel().getSelectedItems();
        }
        return (List<T>) o;
    }

    /**
     * 获取已选择的索引
     *
     * @return 结果
     */
    default int getSelectedIndex() {
        if (this instanceof TreeView<?> node) {
            return node.getSelectionModel().getSelectedIndex();
        } else if (this instanceof TreeTableView<?> node) {
            return node.getSelectionModel().getSelectedIndex();
        } else if (this instanceof TableView<?> node) {
            return node.getSelectionModel().getSelectedIndex();
        } else if (this instanceof TabPane node) {
            return node.getSelectionModel().getSelectedIndex();
        } else if (this instanceof ComboBox<?> node) {
            return node.getSelectionModel().getSelectedIndex();
        } else if (this instanceof ListView<?> node) {
            return node.getSelectionModel().getSelectedIndex();
        }
        return -1;
    }

    /**
     * 获取节点数量
     *
     * @return 结果
     */
    default int getItemSize() {
        if (this instanceof TableView<?> node) {
            return node.getItems().size();
        } else if (this instanceof TabPane node) {
            return node.getTabs().size();
        } else if (this instanceof ComboBox<?> node) {
            return node.getItems().size();
        } else if (this instanceof ListView<?> node) {
            return node.getItems().size();
        }
        return -1;
    }

    /**
     * 选中索引变化事件
     *
     * @param listener 监听器
     */
    default void selectedIndexChanged(ChangeListener<Number> listener) {
        if (this instanceof TreeView<?> node) {
            node.getSelectionModel().selectedIndexProperty().addListener((observableValue, t, t1) -> {
                if (!this.isIgnoreChanged()) {
                    listener.changed(observableValue, t, t1);
                }
            });
        } else if (this instanceof TreeTableView<?> node) {
            node.getSelectionModel().selectedIndexProperty().addListener((observableValue, t, t1) -> {
                if (!this.isIgnoreChanged()) {
                    listener.changed(observableValue, t, t1);
                }
            });
        } else if (this instanceof TableView<?> node) {
            node.getSelectionModel().selectedIndexProperty().addListener((observableValue, t, t1) -> {
                if (!this.isIgnoreChanged()) {
                    listener.changed(observableValue, t, t1);
                }
            });
        } else if (this instanceof TabPane node) {
            node.getSelectionModel().selectedIndexProperty().addListener((observableValue, t, t1) -> {
                if (!this.isIgnoreChanged()) {
                    listener.changed(observableValue, t, t1);
                }
            });
        } else if (this instanceof ComboBox<?> node) {
            node.getSelectionModel().selectedIndexProperty().addListener((observableValue, t, t1) -> {
                if (!this.isIgnoreChanged()) {
                    listener.changed(observableValue, t, t1);
                }
            });
        } else if (this instanceof ListView<?> node) {
            node.getSelectionModel().selectedIndexProperty().addListener((observableValue, t, t1) -> {
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
            } else if (this instanceof TreeTableView<?> view) {
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
        if (this instanceof TableView<?> node) {
            return node.getItems().isEmpty();
        }
        if (this instanceof ComboBox<?> node) {
            return node.getItems().isEmpty();
        }
        if (this instanceof ListView<?> node) {
            return node.getItems().isEmpty();
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
            if (this instanceof TableView node) {
                FXUtil.runWait(() -> node.getItems().add(item));
            } else if (this instanceof ComboBox node) {
                FXUtil.runWait(() -> node.getItems().add(item));
            } else if (this instanceof ListView node) {
                FXUtil.runWait(() -> node.getItems().add(item));
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
            if (this instanceof TableView node) {
                FXUtil.runWait(() -> node.getItems().addAll(items));
            } else if (this instanceof ComboBox node) {
                FXUtil.runWait(() -> node.getItems().addAll(items));
            } else if (this instanceof ListView node) {
                FXUtil.runWait(() -> node.getItems().addAll(items));
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
            if (this instanceof TableView node) {
                FXUtil.runWait(() -> node.getItems().add(index, item));
            } else if (this instanceof ComboBox node) {
                FXUtil.runWait(() -> node.getItems().add(index, item));
            } else if (this instanceof ListView node) {
                FXUtil.runWait(() -> node.getItems().add(index, item));
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
                if (this instanceof TableView node) {
                    if (node.getItems().size() <= index) {
                        node.getItems().add(item);
                    } else {
                        node.getItems().set(index, item);
                    }
                } else if (this instanceof ComboBox node) {
                    if (node.getItems().size() <= index) {
                        node.getItems().add(item);
                    } else {
                        node.getItems().set(index, item);
                    }
                } else if (this instanceof ListView node) {
                    if (node.getItems().size() <= index) {
                        node.getItems().add(item);
                    } else {
                        node.getItems().set(index, item);
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
                if (this instanceof TableView node) {
                    node.getItems().setAll(item);
                } else if (this instanceof ComboBox node) {
                    node.getItems().setAll(item);
                } else if (this instanceof ListView node) {
                    node.getItems().setAll(item);
                }
            });
        }
    }

    /**
     * 设置子节点
     *
     * @param items 子节点列表
     */
    default void setItem(Collection<?> items) {
        if (items != null) {
            FXUtil.runWait(() -> {
                if (this instanceof TableView node) {
                    node.getItems().setAll(items);
                } else if (this instanceof ComboBox node) {
                    node.getItems().setAll(items);
                } else if (this instanceof ListView node) {
                    node.getItems().setAll(items);
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
            if (this instanceof TableView<?> node) {
                if (node.getItems().size() <= index) {
                    return null;
                }
                return node.getItems().get(index);
            } else if (this instanceof ComboBox<?> node) {
                if (node.getItems().size() <= index) {
                    return null;
                }
                return node.getItems().get(index);
            } else if (this instanceof ListView<?> node) {
                if (node.getItems().size() <= index) {
                    return null;
                }
                return node.getItems().get(index);
            } else if (this instanceof TabPane node) {
                if (node.getTabs().size() <= index) {
                    return null;
                }
                return node.getTabs().get(index);
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
            if (this instanceof TableView<?> node) {
                if (node.getItems().size() >= index) {
                    FXUtil.runWait(() -> node.getItems().remove(index));
                }
            } else if (this instanceof ComboBox<?> node) {
                if (node.getItems().size() >= index) {
                    FXUtil.runWait(() -> node.getItems().remove(index));
                }
            } else if (this instanceof ListView<?> node) {
                if (node.getItems().size() >= index) {
                    FXUtil.runWait(() -> node.getItems().remove(index));
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
            if (this instanceof TableView<?> node) {
                FXUtil.runWait(() -> node.getItems().remove(item));
            } else if (this instanceof ComboBox<?> node) {
                FXUtil.runWait(() -> node.getItems().remove(item));
            } else if (this instanceof ListView<?> node) {
                FXUtil.runWait(() -> node.getItems().remove(item));
            }
        }
    }

    /**
     * 移除子节点列表
     *
     * @param items 子节点列表
     */
    default void removeItem(List<?> items) {
        if (CollectionUtil.isNotEmpty(items)) {
            if (this instanceof TableView<?> node) {
                FXUtil.runWait(() -> node.getItems().removeAll(items));
            } else if (this instanceof ComboBox<?> node) {
                FXUtil.runWait(() -> node.getItems().removeAll(items));
            } else if (this instanceof ListView<?> node) {
                FXUtil.runWait(() -> node.getItems().removeAll(items));
            }
        }
    }

    /**
     * 移除已选中节点
     *
     * @return 选中的节点
     */
    default T removeSelectedItem() {
        T item = this.getSelectedItem();
        this.removeItem(item);
        return item;
    }

    /**
     * 获取末尾索引
     *
     * @return 末尾索引
     */
    default int getLastIndex() {
        return this.getItemSize() - 1;
    }

    /**
     * 选中末尾节点
     */
    default void selectLast() {
        if (this instanceof ListView<?> node) {
            node.getSelectionModel().selectLast();
            node.scrollTo(this.getItemSize());
        } else if (this instanceof TableView<?> node) {
            node.getSelectionModel().selectLast();
            node.scrollTo(this.getItemSize());
        } else if (this instanceof ComboBox<?> node) {
            node.getSelectionModel().selectLast();
        } else if (this instanceof TabPane node) {
            node.getSelectionModel().selectLast();
        }
    }
}

