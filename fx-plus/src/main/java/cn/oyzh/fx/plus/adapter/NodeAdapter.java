package cn.oyzh.fx.plus.adapter;

import cn.oyzh.fx.plus.util.FXUtil;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;


/**
 * 节点适配器
 *
 * @author oyzh
 * @since 2023/5/15
 */
public interface NodeAdapter {

    /**
     * 获取父节点
     *
     * @return 父节点
     */
    default Parent parent() {
        if (this instanceof Node node) {
            return node.getParent();
        } else if (this instanceof TableColumn<?, ?> column) {
            return column.getTableView();
        }
        return null;
    }

    /**
     * 获取上一个兄弟节点
     *
     * @return 上一个兄弟节点
     */
    default Node prevSibling() {
        Parent parent = this.parent();
        if (parent != null) {
            ObservableList<Node> childes = parent.getChildrenUnmodifiable();
            int index = childes.indexOf(this);
            if (index <= 0) {
                return null;
            }
            return childes.get(index - 1);
        }
        return null;
    }

    /**
     * 获取下一个兄弟节点
     *
     * @return 下一个兄弟节点
     */
    default Node nextSibling() {
        Parent parent = this.parent();
        if (parent != null) {
            ObservableList<Node> childes = parent.getChildrenUnmodifiable();
            int index = childes.indexOf(this);
            if (index == -1 || index == childes.size() - 1) {
                return null;
            }
            return childes.get(index + 1);
        }
        return null;
    }

    /**
     * 父节点重新布局
     */
    default void parentAutosize() {
        Parent parent = this.parent();
        if (parent != null) {
            parent.autosize();
        }
    }

    /**
     * 获取父节点宽度
     *
     * @return 结果
     */
    default double parentWidth() {
        Parent parent = this.parent();
        if (parent instanceof Region region) {
            return region.getWidth();
        }
        if (parent != null) {
            return Math.max(parent.prefWidth(-1), parent.minWidth(-1));
        }
        if (this instanceof Node node && node.getScene() != null) {
            return node.getScene().getWidth();
        }
        return Double.NaN;
    }

    /**
     * 获取父节点高度
     *
     * @return 结果
     */
    default double parentHeight() {
        Parent parent = this.parent();
        if (parent instanceof Region region) {
            return region.getHeight();
        }
        if (parent != null) {
            return Math.max(parent.prefHeight(-1), parent.minHeight(-1));
        }
        if (this instanceof Node node && node.getScene() != null) {
            return node.getScene().getHeight();
        }
        return Double.NaN;
    }

    default void addChild(Node node) {
        if (node != null) {
            FXUtil.runWait(() -> {
                if (this instanceof Pane pane) {
                    pane.getChildren().add(node);
                } else if (this instanceof Group group) {
                    group.getChildren().add(node);
                }
            });
        }
    }

    default void setChild(int index, Node node) {
        if (node != null && index >= 0) {
            FXUtil.runWait(() -> {
                if (this instanceof Pane pane) {
                    if (pane.getChildren().size() <= index) {
                        pane.getChildren().add(node);
                    } else {
                        pane.getChildren().set(index, node);
                    }
                } else if (this instanceof Group group) {
                    if (group.getChildren().size() <= index) {
                        group.getChildren().add(node);
                    } else {
                        group.getChildren().set(index, node);
                    }
                }
            });
        }
    }

    default Node getChild(int index) {
        if (index >= 0) {
            if (this instanceof Pane pane) {
                if (pane.getChildren().size() <= index) {
                    return null;
                }
                return pane.getChildren().get(index);
            } else if (this instanceof Group group) {
                if (group.getChildren().size() <= index) {
                    return null;
                }
                return group.getChildren().get(index);
            }
        }
        return null;
    }

    default void removeChild(int index) {
        if (index >= 0) {
            FXUtil.runWait(() -> {
                if (this instanceof Pane pane) {
                    if (pane.getChildren().size() >= index) {
                        pane.getChildren().remove(index);
                    }
                } else if (this instanceof Group group) {
                    if (group.getChildren().size() >= index) {
                        group.getChildren().remove(index);
                    }
                }
            });
        }
    }

    default void removeChild(Node child) {
        if (child != null) {
            FXUtil.runWait(() -> {
                if (this instanceof Pane pane) {
                    pane.getChildren().remove(child);
                } else if (this instanceof Group group) {
                    group.getChildren().remove(child);
                }
            });
        }
    }
}
