package cn.oyzh.fx.plus.node;

import atlantafx.base.theme.Styles;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.StageAdapter;
import javafx.event.EventTarget;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Labeled;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumnBase;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.media.MediaView;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * 节点工具类
 *
 * @author oyzh
 * @since 2023/05/15
 */
public class NodeUtil {

    /**
     * 递归布局
     *
     * @param node 节点
     */
    public static void layoutRecursive(EventTarget node) {
        if (node instanceof TabPane tabPane) {
            tabPane.requestLayout();
//            tabPane.layout();
            for (Tab tab : tabPane.getTabs()) {
                layoutRecursive(tab.getContent());
            }
        } else if (node instanceof Pane pane) {
            pane.requestLayout();
//            pane.layout();
            for (Node node1 : pane.getChildren()) {
                layoutRecursive(node1);
            }
        } else if (node instanceof Region region) {
            region.requestLayout();
//            region.layout();
            for (Node node1 : region.getChildrenUnmodifiable()) {
                layoutRecursive(node1);
            }
        } else if (node instanceof Parent parent) {
            parent.requestLayout();
//            parent.layout();
            for (Node node1 : parent.getChildrenUnmodifiable()) {
                layoutRecursive(node1);
            }
        } else if (node instanceof Node parent) {
            parent.autosize();
        }
    }

    /**
     * 获取属性
     *
     * @param node 节点
     * @param key  键
     * @return 值
     */
    public static Object getProperty(Node node, Object key) {
        if (node.hasProperties()) {
            return node.getProperties().get(key);
        }
        return null;
    }

    /**
     * 设置属性
     *
     * @param node  节点
     * @param key   键
     * @param value 值
     */
    public static void setProperty(Node node, Object key, Object value) {
        if (key != null && value != null) {
            node.getProperties().put(key, value);
        }
    }

    /**
     * 获取样式值
     *
     * @param node 节点
     * @param prop 属性
     * @return 样式值
     */
    public static String getStyle(Node node, String prop) {
        String currentStyle = node.getStyle();
        if (currentStyle != null && !currentStyle.isBlank()) {
            if (prop != null && !prop.isBlank()) {
                String[] stylePairs = currentStyle.split(";");
                for (String stylePair : stylePairs) {
                    String[] styleParts = stylePair.split(":");
                    if (styleParts[0].trim().equals(prop)) {
                        return styleParts[1];
                    }
                }
            }
        }
        return currentStyle;
    }

    /**
     * 替换样式
     *
     * @param node  节点
     * @param prop  属性
     * @param value 值
     */
    public static void replaceStyle(Node node, String prop, Object value) {
        Styles.removeStyle(node, prop);
        Styles.appendStyle(node, prop, value.toString());
    }

    /**
     * 获取宽度
     *
     * @param target 对象
     * @return 宽度
     */
    public static double getWidth(EventTarget target) {
//        if (target instanceof Node node) {
//            Bounds bounds = node.getLayoutBounds();
//            if (bounds != null) {
//                double w = bounds.getWidth();
//                if (w != 0) {
//                    return Math.abs(w);
//                }
//            }
//        }
        if (target instanceof Region region) {
            double w1 = region.getWidth();
            if (w1 >= 0) {
                return w1;
            }
            double w3 = region.getPrefWidth();
            if (w3 >= 0) {
                return w3;
            }
            double w2 = region.getMinWidth();
            if (w2 >= 0) {
                return w2;
            }
            double w4 = region.getMaxWidth();
            if (w4 >= 0) {
                return w4;
            }
            return Double.NaN;
        }

        // if (target instanceof Parent parent) {
        //     double w1 = parent.prefWidth(-1);
        //     double w2 = parent.minWidth(-1);
        //     return Math.max(w1, w2);
        // }

        if (target instanceof TableColumnBase<?, ?> columnBase) {
            double w1 = columnBase.getWidth();
            if (w1 >= 0) {
                return w1;
            }
            double w2 = columnBase.getMinWidth();
            if (w2 >= 0) {
                return w2;
            }
            double w3 = columnBase.getMaxWidth();
            if (w3 >= 0) {
                return w3;
            }
            return Double.NaN;
        }

        // if (target instanceof Shape shape) {
        //     double w1 = shape.prefWidth(-1);
        //     double w2 = shape.minWidth(-1);
        //     return Math.max(w1, w2);
        // }

        if (target instanceof Scene scene) {
            return scene.getWidth();
        }

        if (target instanceof Stage stage) {
            double w1 = stage.getWidth();
            if (w1 >= 0) {
                return w1;
            }
            double w2 = stage.getMinWidth();
            if (w2 >= 0) {
                return w2;
            }
            double w3 = stage.getMaxWidth();
            if (w3 >= 0) {
                return w3;
            }
        }

        if (target instanceof StageAdapter adapter) {
            return getWidth(adapter.stage());
        }

        if (target instanceof Window window) {
            return window.getWidth();
        }

        if (target instanceof Node node) {
            double w1 = node.prefWidth(-1);
            if (w1 >= 0) {
                return w1;
            }
            double w2 = node.minWidth(-1);
            if (w2 >= 0) {
                return w2;
            }
            double w3 = node.maxWidth(-1);
            if (w3 >= 0) {
                return w3;
            }
            return Double.NaN;
        }

        return Double.NaN;
    }

    /**
     * 获取高度
     *
     * @param target 对象
     * @return 高度
     */
    public static double getHeight(EventTarget target) {
//        if (target instanceof Node node) {
//            Bounds bounds = node.getLayoutBounds();
//            if (bounds != null) {
//                double h = bounds.getHeight();
//                if (h != 0) {
//                    return Math.abs(h);
//                }
//            }
//        }

        if (target instanceof Region region) {
            double w4 = region.getHeight();
            if (w4 >= 0) {
                return w4;
            }
            double w1 = region.getPrefHeight();
            if (w1 >= 0) {
                return w1;
            }
            double w2 = region.getMinHeight();
            if (w2 >= 0) {
                return w2;
            }
            double w3 = region.getMaxHeight();
            if (w3 >= 0) {
                return w3;
            }
            return Double.NaN;
        }

        // if (target instanceof Parent parent) {
        //     double w1 = parent.prefHeight(-1);
        //     double w2 = parent.minHeight(-1);
        //     return Math.max(w1, w2);
        // }

        if (target instanceof Scene scene) {
            return scene.getHeight();
        }

        if (target instanceof Stage stage) {
            double w1 = stage.getHeight();
            if (w1 >= 0) {
                return w1;
            }
            double w2 = stage.getMinHeight();
            if (w2 >= 0) {
                return w2;
            }
            double w3 = stage.getMaxHeight();
            if (w3 >= 0) {
                return w3;
            }
            return Double.NaN;
        }

        if (target instanceof StageAdapter adapter) {
            return getHeight(adapter.stage());
        }

        if (target instanceof Window window) {
            return window.getHeight();
        }

        if (target instanceof Node node) {
            double w1 = node.prefHeight(-1);
            if (w1 >= 0) {
                return w1;
            }
            double w2 = node.minHeight(-1);
            if (w2 >= 0) {
                return w2;
            }
            double w3 = node.maxHeight(-1);
            if (w3 >= 0) {
                return w3;
            }
            return Double.NaN;
        }

        return Double.NaN;
    }

    /**
     * 设置大小
     *
     * @param target 对象
     * @param width  宽度
     * @param height 高度
     */
    public static void setSize(Object target, Double width, Double height) {
        setWidth(target, width);
        setHeight(target, height);
    }

    /**
     * 设置宽度
     *
     * @param target 对象
     * @param width  宽度
     */
    public static void setWidth(Object target, Double width) {
        if (target == null || width == null || Double.isNaN(width) || width <= 0) {
            return;
        }
        if (target instanceof Node node && !node.isManaged()) {
            return;
        }
        switch (target) {
            case ImageView image -> {
                if (!image.fitWidthProperty().isBound()) {
                    image.setFitWidth(width);
                }
            }
            case MediaView media -> {
                if (!media.fitWidthProperty().isBound()) {
                    media.setFitWidth(width);
                }
            }
            case Labeled labeled -> {
                if (!labeled.prefWidthProperty().isBound()) {
                    labeled.setPrefWidth(width);
                }
                if (!labeled.minWidthProperty().isBound()) {
                    labeled.setMinWidth(width);
                }
                if (!labeled.maxWidthProperty().isBound()) {
                    labeled.setMaxWidth(width);
                }
            }
            case TableColumnBase<?, ?> columnBase -> {
                if (!columnBase.prefWidthProperty().isBound()) {
                    columnBase.setPrefWidth(width);
                }
                if (!columnBase.minWidthProperty().isBound()) {
                    columnBase.setMinWidth(width);
                }
                if (!columnBase.maxWidthProperty().isBound()) {
                    columnBase.setMaxWidth(width);
                }
            }
            case PopupControl control -> {
                if (!control.prefWidthProperty().isBound()) {
                    control.setPrefWidth(width);
                }
                if (!control.minWidthProperty().isBound()) {
                    control.setMinWidth(width);
                }
                if (!control.maxWidthProperty().isBound()) {
                    control.setMaxWidth(width);
                }
            }
            case Region region -> {
                if (!region.prefWidthProperty().isBound()) {
                    region.setPrefWidth(width);
                }
                if (!region.minWidthProperty().isBound()) {
                    region.setMinWidth(width);
                }
                if (!region.maxWidthProperty().isBound()) {
                    region.setMaxWidth(width);
                }
            }
            case Shape shape -> {
                if (!shape.strokeWidthProperty().isBound()) {
                    shape.setStrokeWidth(width);
                }
            }
            case Stage stage -> stage.setWidth(width);
            case Window window -> window.setWidth(width);
            case Scene scene -> {
                setWidth(scene.getWindow(), width);
            }
            default -> {
            }
        }
    }

    /**
     * 设置高度
     *
     * @param target 对象
     * @param height 高度
     */
    public static void setHeight(Object target, Double height) {
        if (target == null || height == null || Double.isNaN(height) || height < 0) {
            return;
        }
        if (target instanceof Node node && !node.isManaged()) {
            return;
        }
        switch (target) {
            case ImageView image -> {
                if (!image.fitHeightProperty().isBound()) {
                    image.setFitHeight(height);
                }
            }
            case MediaView media -> {
                if (!media.fitHeightProperty().isBound()) {
                    media.setFitHeight(height);
                }
            }
            case Labeled labeled -> {
                if (!labeled.prefHeightProperty().isBound()) {
                    labeled.setPrefHeight(height);
                }
                if (!labeled.minHeightProperty().isBound()) {
                    labeled.setMinHeight(height);
                }
                if (!labeled.maxHeightProperty().isBound()) {
                    labeled.setMaxHeight(height);
                }
            }
            case PopupControl control -> {
                if (!control.prefHeightProperty().isBound()) {
                    control.setPrefHeight(height);
                }
                if (!control.minHeightProperty().isBound()) {
                    control.setMinHeight(height);
                }
                if (!control.maxHeightProperty().isBound()) {
                    control.setMaxHeight(height);
                }
            }
            case Region region -> {
                if (!region.prefHeightProperty().isBound()) {
                    region.setPrefHeight(height);
                }
                if (!region.minHeightProperty().isBound()) {
                    region.setMinHeight(height);
                }
                if (!region.maxHeightProperty().isBound()) {
                    region.setMaxHeight(height);
                }
            }
            case Stage stage -> stage.setHeight(height);
            case Window window -> window.setHeight(height);
            case Scene scene -> {
                setHeight(scene.getWindow(), height);
            }
            default -> {
            }
        }
    }

    /**
     * 设置y坐标
     *
     * @param target  对象
     * @param layoutY y坐标
     */
    public static void setLayoutY(EventTarget target, Double layoutY) {
        if (layoutY == null || Double.isNaN(layoutY) || layoutY <= 0) {
            return;
        }
        if (target instanceof Node node) {
            if (!node.isManaged()) {
                return;
            }
            if (!node.layoutYProperty().isBound() && node.getLayoutY() != layoutY) {
                node.setLayoutY(layoutY);
            }
        }
    }

    /**
     * 禁用组件
     *
     * @param obj 节点
     */
    public static void disable(Object obj) {
        if (obj instanceof Node node) {
            if (!node.disableProperty().isBound()) {
                node.setDisable(true);
            }
        } else if (obj instanceof MenuItem item) {
            if (!item.disableProperty().isBound()) {
                item.setDisable(true);
            }
        } else if (obj instanceof Tab tab) {
            if (!tab.disableProperty().isBound()) {
                tab.setDisable(true);
            }
        } else if (obj instanceof Stage stage) {
            Scene scene = stage.getScene();
            if (scene != null && scene.getRoot() != null && !scene.getRoot().disableProperty().isBound()) {
                scene.getRoot().setDisable(true);
            }
        } else if (obj instanceof StageAdapter stage) {
            if (stage.root() != null && !stage.root().disableProperty().isBound()) {
                stage.root().setDisable(true);
            }
        }
    }

    /**
     * 启用组件
     *
     * @param obj 节点
     */
    public static void enable(Object obj) {
        if (obj instanceof Node node) {
            if (!node.disableProperty().isBound()) {
                node.setDisable(false);
            }
        } else if (obj instanceof MenuItem item) {
            if (!item.disableProperty().isBound()) {
                item.setDisable(false);
            }
        } else if (obj instanceof Tab tab) {
            if (!tab.disableProperty().isBound()) {
                tab.setDisable(false);
            }
        } else if (obj instanceof Stage stage) {
            Scene scene = stage.getScene();
            if (scene != null && scene.getRoot() != null && !scene.getRoot().disableProperty().isBound()) {
                scene.getRoot().setDisable(false);
            }
        } else if (obj instanceof StageAdapter stage) {
            if (stage.root() != null && !stage.root().disableProperty().isBound()) {
                stage.root().setDisable(false);
            }
        }
    }

    /**
     * 显示组件
     *
     * @param obj 节点
     */
    public static void display(Object obj) {
        if (obj instanceof Node node) {
            if (!node.visibleProperty().isBound()) {
                node.setVisible(true);
            }
            if (!node.managedProperty().isBound()) {
                node.setManaged(true);
            }
        } else if (obj instanceof MenuItem item) {
            if (!item.visibleProperty().isBound()) {
                item.setVisible(true);
            }
        } else if (obj instanceof Tab tab) {
            if (tab.getContent() != null && !tab.getContent().visibleProperty().isBound()) {
                tab.getContent().setVisible(true);
            }
        } else if (obj instanceof Stage stage) {
            if (!stage.isShowing()) {
                FXUtil.runWait(stage::show);
            }
        } else if (obj instanceof StageAdapter stage) {
            // if (!stage.stage().isShowing()) {
            //    FXUtil.runWait(stage.stage()::show);
            //}
            display(stage.stage());
        }
    }

    /**
     * 隐藏组件
     *
     * @param obj 节点
     */
    public static void disappear(Object obj) {
        if (obj instanceof Node node) {
            if (!node.visibleProperty().isBound()) {
                node.setVisible(false);
            }
            if (!node.managedProperty().isBound()) {
                node.setManaged(false);
            }
        } else if (obj instanceof MenuItem item) {
            if (!item.visibleProperty().isBound()) {
                item.setVisible(false);
            }
        } else if (obj instanceof Tab tab) {
            if (tab.getContent() != null && !tab.getContent().visibleProperty().isBound()) {
                tab.getContent().setVisible(false);
            }
        } else if (obj instanceof Stage stage) {
            if (stage.isShowing()) {
                FXUtil.runWait(stage::close);
            }
        } else if (obj instanceof Window window) {
            if (window.isShowing()) {
                FXUtil.runWait(window::hide);
            }
        } else if (obj instanceof PopupAdapter wrapper) {
            if (wrapper.popup().isShowing()) {
                FXUtil.runWait(wrapper.popup()::hide);
            }
        } else if (obj instanceof StageAdapter wrapper) {
            if (wrapper.stage().isShowing()) {
                FXUtil.runWait(wrapper.stage()::close);
            }
        }
    }

    /**
     * ctrl+s事件
     *
     * @param target 组件
     */
    public static void nodeOnCtrlS(Object target, Runnable action) {
        if (target != null) {
            switch (target) {
                case Node node -> node.setOnKeyPressed(event -> {
                    if (KeyboardUtil.isCtrlS(event)) {
                        action.run();
                    }
                });
                case Scene node -> node.setOnKeyPressed(event -> {
                    if (KeyboardUtil.isCtrlS(event)) {
                        action.run();
                    }
                });
                case Stage stage -> nodeOnCtrlS(stage.getScene(), action);
                case StageAdapter stage -> nodeOnCtrlS(stage.scene(), action);
                default -> {
                }
            }
        }
    }

    // /**
    //  * 取消焦点
    //  *
    //  * @param node
    //  */
    // public static void unFocus(Node node) {
    //     if (node != null && node.getParent() != null) {
    //         FXUtil.runWait(() -> {
    //             node.getScene().getRoot().requestFocus();
    //         });
    //     }
    // }
    //
    // /**
    //  * 清楚焦点
    //  *
    //  * @param node 节点
    //  */
    // public static void clearFocus(Node node) {
    //     if (node != null && node.getScene() != null) {
    //         Scene scene = node.getScene();
    //         FXUtil.runWait(() -> {
    //             Node focusOwner = scene.getFocusOwner();
    //             if (focusOwner != null) {
    //                 focusOwner.setFocusTraversable(false);
    //             }
    //             scene.getRoot().setFocusTraversable(true);
    //             scene.getRoot().requestFocus();
    //         });
    //     }
    // }

    /**
     * 是否从右到左布局
     *
     * @param node 节点
     * @return 结果
     */
    public static boolean isOrientationRightToLeft(Node node) {
        return node.getNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT || node.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;
    }
}
