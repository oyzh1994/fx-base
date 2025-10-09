package cn.oyzh.fx.plus.test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class LightweightHBox extends javafx.scene.layout.Region {
    private final ObservableList<Node> children = FXCollections.observableArrayList();
    private double spacing = 0;
    private Insets padding = Insets.EMPTY;

    public LightweightHBox() {
    }

    public LightweightHBox(double spacing) {
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
        boolean first = true;
        for (Node child : children) {
            if (child.isManaged()) {
                if (!first) {
                    minWidth += spacing;
                }
                minWidth += child.minWidth(-1);
                first = false;
            }
        }
        return minWidth;
    }

    @Override
    protected double computeMinHeight(double width) {
        double minHeight = padding.getTop() + padding.getBottom();
        for (Node child : children) {
            if (child.isManaged()) {
                minHeight = Math.max(minHeight, child.minHeight(-1));
            }
        }
        return minHeight;
    }

    @Override
    protected double computePrefWidth(double height) {
        double prefWidth = padding.getLeft() + padding.getRight();
        boolean first = true;
        for (Node child : children) {
            if (child.isManaged()) {
                if (!first) {
                    prefWidth += spacing;
                }
                prefWidth += child.prefWidth(-1);
                first = false;
            }
        }
        return prefWidth;
    }

    @Override
    protected double computePrefHeight(double width) {
        double prefHeight = padding.getTop() + padding.getBottom();
        for (Node child : children) {
            if (child.isManaged()) {
                prefHeight = Math.max(prefHeight, child.prefHeight(-1));
            }
        }
        return prefHeight;
    }

    @Override
    protected void layoutChildren() {
        double x = padding.getLeft();
        double height = getHeight() - padding.getTop() - padding.getBottom();

        // 计算需要分配的额外空间
        double extraSpace = getWidth() - computePrefWidth(-1);
        if (extraSpace < 0) extraSpace = 0;

        // 计算可以增长的子节点数量
        int growableCount = 0;
        for (Node child : children) {
            if (child.isManaged() && getHgrow(child) != Priority.NEVER) {
                growableCount++;
            }
        }

        // 每个可增长节点分配的额外空间
        double extraPerNode = growableCount > 0 ? extraSpace / growableCount : 0;

        for (Node child : children) {
            if (child.isManaged()) {
                double childWidth = child.prefWidth(-1);
                if (getHgrow(child) != Priority.NEVER) {
                    childWidth += extraPerNode;
                }

                child.resizeRelocate(x, padding.getTop(), childWidth, child.prefHeight(-1));
                x += childWidth + spacing;
            }
        }
    }

    // 静态方法用于设置水平增长优先级
    public static void setHgrow(Node child, Priority value) {
        if (value == null) value = Priority.NEVER;
        HBox.setHgrow(child, value);
    }

    public static Priority getHgrow(Node child) {
        Priority value = HBox.getHgrow(child);
        return value == null ? Priority.NEVER : value;
    }
}
