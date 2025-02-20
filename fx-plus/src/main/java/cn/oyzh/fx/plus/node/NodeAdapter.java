package cn.oyzh.fx.plus.node;

import cn.oyzh.fx.plus.util.FXUtil;
import javafx.collections.ObservableList;
import javafx.event.EventTarget;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.List;


/**
 * 节点适配器
 *
 * @author oyzh
 * @since 2023/5/15
 */
public interface NodeAdapter extends EventTarget {

    /**
     * 初始化节点
     */
    default void initNode() {

    }

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
//            return NodeUtil.getWidth(parent);
        }
        if (this instanceof Node node && node.getScene() != null) {
             return node.getScene().getWidth();
//            return NodeUtil.getWidth(node.getScene());
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
//            return NodeUtil.getHeight(parent);
        }
        if (this instanceof Node node && node.getScene() != null) {
             return node.getScene().getHeight();
//            return NodeUtil.getHeight(node.getScene());
        }
        return Double.NaN;
    }

    /**
     * 子节点是否为空
     *
     * @return 结果
     */
    default boolean isChildEmpty() {
        if (this instanceof Pane node) {
            return node.getChildren().isEmpty();
        } else if (this instanceof Group node) {
            return node.getChildren().isEmpty();
        }
        return true;
    }

    /**
     * 获取首个节点
     *
     * @return 节点
     */
    default Node firstChild() {
        switch (this) {
            case Pane pane -> {
                return pane.getChildren().getFirst();
            }
            case Group group -> {
                return group.getChildren().getFirst();
            }
            case Parent parent -> {
                return parent.getChildrenUnmodifiable().getFirst();
            }
            default -> {
                return null;
            }
        }
    }

    /**
     * 清除子节点
     */
    default void clearChild() {
        if (this instanceof Pane pane) {
            FXUtil.runWait(() -> pane.getChildren().clear());
        } else if (this instanceof Group group) {
            FXUtil.runWait(() -> group.getChildren().clear());
        } else if (this instanceof TabPane tabPane) {
            FXUtil.runWait(() -> tabPane.getTabs().clear());
        }
    }

    /**
     * 添加子节点
     *
     * @param node 子节点
     */
    default void addChild(Node node) {
        if (node != null) {
            if (this instanceof Pane pane) {
                FXUtil.runWait(() -> pane.getChildren().add(node));
            } else if (this instanceof Group group) {
                FXUtil.runWait(() -> group.getChildren().add(node));
            }
        }
    }

    /**
     * 添加子节点
     *
     * @param nodes 子节点列表
     */
    default void addChild(List<? extends Node> nodes) {
        if (nodes != null) {
            if (this instanceof Pane pane) {
                FXUtil.runWait(() -> pane.getChildren().addAll(nodes));
            } else if (this instanceof Group group) {
                FXUtil.runWait(() -> group.getChildren().addAll(nodes));
            }
        }
    }

    /**
     * 添加子节点
     *
     * @param index 下标
     * @param node  子节点
     */
    default void addChild(int index, Node node) {
        if (node != null) {
            if (this instanceof Pane pane) {
                FXUtil.runWait(() -> pane.getChildren().add(index, node));
            } else if (this instanceof Group group) {
                FXUtil.runWait(() -> group.getChildren().add(index, node));
            }
        }
    }

    /**
     * 设置子节点
     *
     * @param index 下标
     * @param node  子节点
     */
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

    /**
     * 设置子节点
     *
     * @param node 子节点
     */
    default void setChild(Node node) {
        if (node != null) {
            FXUtil.runWait(() -> {
                if (this instanceof Pane pane) {
                    pane.getChildren().setAll(node);
                } else if (this instanceof Group group) {
                    group.getChildren().setAll(node);
                }
            });
        }
    }

    /**
     * 设置子节点
     *
     * @param nodes 子节点列表
     */
    default void setChild(Node... nodes) {
        if (nodes != null && nodes.length != 0) {
            FXUtil.runWait(() -> {
                if (this instanceof Pane pane) {
                    pane.getChildren().setAll(nodes);
                } else if (this instanceof Group group) {
                    group.getChildren().setAll(nodes);
                }
            });
        }
    }

    /**
     * 设置子节点
     *
     * @param nodes 子节点列表
     */
    default void setChild(List<? extends Node> nodes) {
        if (nodes != null && !nodes.isEmpty()) {
            FXUtil.runWait(() -> {
                if (this instanceof Pane pane) {
                    pane.getChildren().setAll(nodes);
                } else if (this instanceof Group group) {
                    group.getChildren().setAll(nodes);
                }
            });
        }
    }

    /**
     * 获取首个子节点
     *
     * @return 子节点
     */
    default Node getFirstChild() {
        return this.getChild(0);
    }

    /**
     * 获取子节点
     *
     * @param index 下标
     * @return 子节点
     */
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

    /**
     * 移除子节点
     *
     * @param index 下标
     */
    default void removeChild(int index) {
        if (index >= 0) {
            if (this instanceof Pane pane) {
                if (pane.getChildren().size() > index) {
                    FXUtil.runWait(() -> pane.getChildren().remove(index));
                }
            } else if (this instanceof Group group) {
                if (group.getChildren().size() > index) {
                    FXUtil.runWait(() -> group.getChildren().remove(index));
                }
            }
        }
    }

    /**
     * 移除子节点
     *
     * @param child 子节点
     */
    default void removeChild(Node child) {
        if (child != null) {
            if (this instanceof Pane pane) {
                FXUtil.runWait(() -> pane.getChildren().remove(child));
            } else if (this instanceof Group group) {
                FXUtil.runWait(() -> group.getChildren().remove(child));
            }
        }
    }

    /**
     * 添加样式类
     *
     * @param styleClass 样式类
     */
    default void addClass(String styleClass) {
        if (styleClass != null) {
            if (this instanceof Node node) {
                node.getStyleClass().add(styleClass);
            }
        }
    }

    /**
     * 移除样式类
     *
     * @param styleClass 样式类
     */
    default void removeClass(String styleClass) {
        if (styleClass != null) {
            if (this instanceof Node node) {
                node.getStyleClass().remove(styleClass);
            }
        }
    }

    /**
     * 节点是否启用
     *
     * @return 结果
     */
    default boolean isEnable() {
        if (this instanceof Node node) {
            return !node.isDisable() && !node.isDisabled();
        }
        return false;
    }

    /**
     * 获取节点的舞台
     *
     * @return 舞台对象
     */
    default Stage stage() {
        Window window = this.window();
        return window instanceof Stage ? (Stage) window : null;
    }

    /**
     * 获取节点的场景对象
     *
     * @return 场景对象
     */
    default Scene scene() {
        Window window = this.window();
        if (window != null) {
            return window.getScene();
        }
        return null;
    }

    /**
     * 获取节点的窗口
     *
     * @return 窗口对象
     */
    default Window window() {
        if (this instanceof Node node) {
            Scene scene = node.getScene();
            if (scene != null) {
                return scene.getWindow();
            }
        }
        return null;
    }

    /**
     * 获取鼠标类型
     *
     * @return 鼠标类型
     */
    default String getCursorType() {
        if (this instanceof Node node) {
            return node.getCursor().toString();
        }
        return null;
    }

    /**
     * 设置鼠标类型
     *
     * @param cursorType 鼠标类型
     */
    default void setCursorType(String cursorType) {
        if (this instanceof Node node) {
            node.setCursor(Cursor.cursor(cursorType));
        }
    }

    /**
     * 清除焦点
     */
    default void clearFocus() {
        FXUtil.runWait(() -> this.scene().focusCleanup());
    }

    /**
     * 当前节点设置为焦点节点
     */
    default void focusNode() {
        if (this instanceof Node node) {
            FXUtil.runWait(() -> this.scene().setFocusOwner(node, true));
        }
    }
}
