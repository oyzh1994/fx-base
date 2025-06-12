package cn.oyzh.fx.plus.test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class LightweightVBox extends javafx.scene.layout.Region {
    private final ObservableList<Node> children = FXCollections.observableArrayList();
    private double spacing = 0;
    private Insets padding = Insets.EMPTY;

    public LightweightVBox() {
    }

    public LightweightVBox(double spacing) {
        this.spacing = spacing;
    }

    public ObservableList<Node> getChildren() {
        return children;
    }

    public void setSpacing(double spacing) {
        this.spacing = spacing;
    }

    @Override
    protected double computeMinWidth(double height) {
        double minWidth = padding.getLeft() + padding.getRight();
        for (Node child : children) {
            if (child.isManaged()) {
                minWidth = Math.max(minWidth, child.minWidth(-1));
            }
        }
        return minWidth;
    }

    @Override
    protected double computeMinHeight(double width) {
        double minHeight = padding.getTop() + padding.getBottom();
        boolean first = true;
        for (Node child : children) {
            if (child.isManaged()) {
                if (!first) {
                    minHeight += spacing;
                }
                minHeight += child.minHeight(-1);
                first = false;
            }
        }
        return minHeight;
    }

    @Override
    protected double computePrefWidth(double height) {
        double prefWidth = padding.getLeft() + padding.getRight();
        for (Node child : children) {
            if (child.isManaged()) {
                prefWidth = Math.max(prefWidth, child.prefWidth(-1));
            }
        }
        return prefWidth;
    }

    @Override
    protected double computePrefHeight(double width) {
        double prefHeight = padding.getTop() + padding.getBottom();
        boolean first = true;
        for (Node child : children) {
            if (child.isManaged()) {
                if (!first) {
                    prefHeight += spacing;
                }
                prefHeight += child.prefHeight(-1);
                first = false;
            }
        }
        return prefHeight;
    }

    @Override
    protected void layoutChildren() {
        double y = padding.getTop();
        double width = getWidth() - padding.getLeft() - padding.getRight();

        // 计算需要分配的额外空间
        double extraSpace = getHeight() - computePrefHeight(-1);
        if (extraSpace < 0) extraSpace = 0;

        // 计算可以增长的子节点数量
        int growableCount = 0;
        for (Node child : children) {
            if (child.isManaged() && getVgrow(child) != Priority.NEVER) {
                growableCount++;
            }
        }

        // 每个可增长节点分配的额外空间
        double extraPerNode = growableCount > 0 ? extraSpace / growableCount : 0;

        for (Node child : children) {
            if (child.isManaged()) {
                double childHeight = child.prefHeight(-1);
                if (getVgrow(child) != Priority.NEVER) {
                    childHeight += extraPerNode;
                }

                child.resizeRelocate(padding.getLeft(), y, child.prefWidth(-1), childHeight);
                y += childHeight + spacing;
            }
        }
    }

    // 静态方法用于设置垂直增长优先级
    public static void setVgrow(Node child, Priority value) {
        if (value == null) value = Priority.NEVER;
        VBox.setVgrow(child, value);
    }

    public static Priority getVgrow(Node child) {
        Priority value = VBox.getVgrow(child);
        return value == null ? Priority.NEVER : value;
    }
}
