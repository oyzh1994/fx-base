package cn.oyzh.fx.plus.util;

import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PopupControl;
import javafx.scene.control.TableColumnBase;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * 节点工具类
 *
 * @author oyzh
 * @since 2023/05/15
 */
@UtilityClass
public class NodeUtil {

    /**
     * 获取宽度
     *
     * @param target 对象
     * @return 宽度
     */
    public static double getWidth(EventTarget target) {
        if (target instanceof Region region) {
            double w1 = region.getWidth();
            double w2 = region.getMinWidth();
            return Math.max(w1, w2);
        }

        if (target instanceof Parent parent) {
            double w1 = parent.prefWidth(-1);
            double w2 = parent.minWidth(-1);
            return Math.max(w1, w2);
        }

        if (target instanceof TableColumnBase<?, ?> columnBase) {
            double w1 = columnBase.getWidth();
            double w2 = columnBase.getMinWidth();
            return Math.max(w1, w2);
        }

        if (target instanceof Shape shape) {
            return shape.getStrokeWidth();
        }

        if (target instanceof Scene scene) {
            return scene.getWidth();
        }

        if (target instanceof Stage stage) {
            double w1 = stage.getWidth();
            double w2 = stage.getMinWidth();
            return Math.max(w1, w2);
        }

        if (target instanceof Window window) {
            return window.getWidth();
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
        if (target instanceof Region region) {
            double w1 = region.getPrefHeight();
            double w2 = region.getMinHeight();
            return Math.max(w1, w2);
        }

        if (target instanceof Parent parent) {
            double w1 = parent.prefHeight(-1);
            double w2 = parent.minHeight(-1);
            return Math.max(w1, w2);
        }

        if (target instanceof Scene scene) {
            return scene.getHeight();
        }

        if (target instanceof Stage stage) {
            double w1 = stage.getHeight();
            double w2 = stage.getMinHeight();
            return Math.max(w1, w2);
        }

        if (target instanceof Window window) {
            return window.getHeight();
        }

        return Double.NaN;
    }

    /**
     * 设置宽度
     *
     * @param target 对象
     * @param width  宽度
     */
    public static void setWidth(EventTarget target, double width) {
        if (target == null || Double.isNaN(width) || width <= 0) {
            return;
        }
        switch (target) {
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
                if (scene.getWindow() != null) {
                    scene.getWindow().setWidth(width);
                }
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
    public static void setHeight(EventTarget target, double height) {
        if (target == null || Double.isNaN(height) || height <= 0) {
            return;
        }
        switch (target) {
            case ImageView image -> {
                if (!image.fitHeightProperty().isBound()) {
                    image.setFitHeight(height);
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
            case Stage stage -> stage.setHeight(height);
            case Window window -> window.setHeight(height);
            case Scene scene -> {
                if (scene.getWindow() != null) {
                    scene.getWindow().setHeight(height);
                }
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
    public static void setLayoutY(@NonNull EventTarget target, double layoutY) {
        if (Double.isNaN(layoutY) || layoutY <= 0) {
            return;
        }
        if (target instanceof Node node) {
            if (!node.layoutYProperty().isBound() && node.getLayoutY() != layoutY) {
                node.setLayoutY(layoutY);
            }
        }
    }
}
