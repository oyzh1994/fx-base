package cn.oyzh.fx.plus.flex;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.adapter.NodeAdapter;
import cn.oyzh.fx.plus.adapter.PropAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * 流式节点
 *
 * @author oyzh
 * @since 2022/1/18
 */
public interface FlexAdapter extends NodeAdapter, PropAdapter, StateAdapter, LayoutAdapter {

    /**
     * 获取流式宽度值
     *
     * @return 流式宽度值
     */
    String getFlexWidth();

    /**
     * 获取流式宽度值实现
     *
     * @return 流式宽度值
     */
    default String _getFlexWidth() {
        return this.flexWidthProperty().get();
    }

    /**
     * 设置流式宽度值
     *
     * @param flexWidth 流式宽度值
     */
    void setFlexWidth(String flexWidth);

    /**
     * 设置流式宽度值实现
     *
     * @param flexWidth 流式宽度值
     */
    default void _setFlexWidth(String flexWidth) {
        this.flexWidthProperty().set(flexWidth);
    }

    /**
     * 获取流式度值属性
     *
     * @return 流式度值属性
     */
    default SimpleStringProperty flexWidthProperty() {
        SimpleStringProperty property = this.getProp("_flexW");
        if (property == null) {
            property = new SimpleStringProperty();
            this.setProp("_flexW", property);
        }
        return property;
    }

    /**
     * 获取流式高度
     *
     * @return 流式高度
     */
    String getFlexHeight();

    /**
     * 获取流式高度实现
     *
     * @return 流式高度
     */
    default String _getFlexHeight() {
        return this.flexHeightProperty().get();
    }

    /**
     * 设置流式高度值
     *
     * @param flexHeight 流式高度值
     */
    void setFlexHeight(String flexHeight);

    /**
     * 设置流式高度值实现
     *
     * @param flexHeight 流式高度值
     */
    default void _setFlexHeight(String flexHeight) {
        this.flexHeightProperty().set(flexHeight);
    }

    /**
     * 获取流式高度属性
     *
     * @return 流式高度属性
     */
    default SimpleStringProperty flexHeightProperty() {
        SimpleStringProperty property = this.getProp("_flexH");
        if (property == null) {
            property = new SimpleStringProperty();
            this.setProp("_flexH", property);
        }
        return property;
    }

    /**
     * 获取流式x值
     *
     * @return 流式x值
     */
    String getFlexX();

    /**
     * 获取流式x值实现
     *
     * @return 流式x值
     */
    default String _getFlexX() {
        return this.flexXProperty().get();
    }

    /**
     * 设置流式x值
     *
     * @param flexX 流式x值
     */
    void setFlexX(String flexX);

    /**
     * 设置流式x值实现
     *
     * @param flexX 流式x值
     */
    default void _setFlexX(String flexX) {
        this.flexXProperty().set(flexX);
    }

    /**
     * 获取流式x值属性
     *
     * @return 流式x值属性
     */
    default SimpleStringProperty flexXProperty() {
        SimpleStringProperty property = this.getProp("_flexX");
        if (property == null) {
            property = new SimpleStringProperty();
            this.setProp("_flexX", property);
        }
        return property;
    }

    /**
     * 获取流式y值
     *
     * @return 流式y值
     */
    String getFlexY();

    /**
     * 获取流式y值实现
     *
     * @return 流式y值
     */
    default String _getFlexY() {
        return this.flexYProperty().get();
    }

    /**
     * 设置流式y值
     *
     * @param flexY 流式y值
     */
    void setFlexY(String flexY);

    /**
     * 设置流式y值实现
     *
     * @param flexY 流式y值
     */
    default void _setFlexY(String flexY) {
        this.flexYProperty().set(flexY);
    }

    /**
     * 获取流式y值属性
     *
     * @return 流式y值属性
     */
    default SimpleStringProperty flexYProperty() {
        SimpleStringProperty property = this.getProp("_flexY");
        if (property == null) {
            property = new SimpleStringProperty();
            this.setProp("_flexY", property);
        }
        return property;
    }

    /**
     * 获取计算宽度值
     */
    default double computeWidth() {
        Double computeWidth = this.getProp("_computeW");
        return computeWidth == null ? Double.NaN : computeWidth;
    }

    /**
     * 获取计算高度值
     */
    default double computeHeight() {
        Double computeHeight = this.getProp("_computeH");
        return computeHeight == null ? Double.NaN : computeHeight;
    }

    /**
     * 计算宽度值
     *
     * @param width 宽度
     * @return 计算后的宽度
     */
    default double computeWidth(double width) {
        // 获取父宽度
        double parentWidth = this.parentWidth();
        double computeWidth = width;
        // 计算宽度值
        double flexValue = FlexUtil.computeFlexValue(this.getFlexWidth(), parentWidth);
        if (!Double.isNaN(flexValue)) {
            computeWidth = flexValue;
        }
        // 设置属性
        this.setProp("_computeW", computeWidth);
        return computeWidth;
    }

    /**
     * 计算高度值
     *
     * @param height 高度
     * @return 计算后的高度
     */
    default double computeHeight(double height) {
        // 获取父高度
        double parentHeight = this.parentHeight();
        double computeHeight = height;
        // 计算高度值
        double flexValue = FlexUtil.computeFlexValue(this.getFlexHeight(), parentHeight);
        if (!Double.isNaN(flexValue)) {
            computeHeight = flexValue;
        }
        // 设置属性
        this.setProp("_computeH", computeHeight);
        return computeHeight;
    }

    /**
     * 计算 X
     *
     * @return X
     */
    default double computeX() {
        double parentWidth = this.parentWidth();
        if (Double.isNaN(parentWidth)) {
            return Double.NaN;
        }
        return FlexUtil.computeFlexValue(this.getFlexX(), parentWidth);
    }

    /**
     * 计算 Y
     *
     * @return Y
     */
    private double computeY() {
        double parentHeight = this.parentHeight();
        if (Double.isNaN(parentHeight)) {
            return Double.NaN;
        }
        return FlexUtil.computeFlexValue(this.getFlexY(), parentHeight);
    }

    /**
     * 重新拉伸节点，自动计算
     */
    default void resizeNode() {
        // 计算的宽度
        double computeWidth = this.computeWidth();
        // 计算的高度
        double computeHeight = this.computeHeight();
        // 重新拉伸节点
        this.resizeNode(computeWidth, computeHeight);
    }

    /**
     * 重新拉伸节点
     *
     * @param width  新宽度
     * @param height 新高度
     */
    default void resizeNode(double width, double height) {
        // 处理宽度
        if (!Double.isNaN(width)) {
            this.setWidthAll(width);
            // TableView处理
            if (this instanceof TableView<?> tableView) {
                ObservableList<? extends TableColumn<?, ?>> columns = tableView.getVisibleLeafColumns();
                for (TableColumn<?, ?> column : columns) {
                    if (column instanceof FlexAdapter flexNode) {
                        flexNode.setWidthAll(FlexUtil.computeFlexValue(flexNode.getFlexWidth(), width));
                    }
                }
            } else if (this instanceof TabPane tabPane) {// TabPane处理
                ObservableList<Tab> tabs = tabPane.getTabs();
                for (Tab tab : tabs) {
                    if (tab.getContent() instanceof FlexAdapter flexNode) {
                        flexNode.setWidthAll(FlexUtil.computeFlexValue(flexNode.getFlexWidth(), width));
                    }
                }
            }
        }

        // 处理高度，y轴
        if (!Double.isNaN(height)) {
            this.setHeightAll(height);
            // TabPane处理
            if (this instanceof TabPane tabPane) {
                ObservableList<Tab> tabs = tabPane.getTabs();
                for (Tab tab : tabs) {
                    if (tab.getContent() instanceof FlexAdapter flexNode) {
                        flexNode.setHeightAll(FlexUtil.computeFlexValue(flexNode.getFlexHeight(), height));
                    }
                }
            }
            if (StrUtil.isNotBlank(this.getFlexY())) {
                this.setLayoutYAll(this.computeY());
            }
        }
    }
}
