package cn.oyzh.fx.plus.flex;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.adapter.NodeAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
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
public interface FlexAdapter extends NodeAdapter, StateAdapter, LayoutAdapter {

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
    default String flexWidth() {
        return this.getProp("flexWidth");
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
    default void flexWidth(String flexWidth) {
        this.setProp("flexWidth", flexWidth);
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
    default String flexHeight() {
        return this.getProp("flexHeight");
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
    default void flexHeight(String flexHeight) {
        this.setProp("flexHeight", flexHeight);
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
    default String flexX() {
        return this.getProp("flexX");
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
    default void flexX(String flexX) {
        this.setProp("flexX", flexX);
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
    default String flexY() {
        return this.getProp("flexY");
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
    default void flexY(String flexY) {
        this.setProp("flexY", flexY);
    }

    /**
     * 获取计算宽度值
     */
    default Double computeWidth() {
        return this.getProp("_computeWidth");
    }

    /**
     * 获取计算高度值
     */
    default Double computeHeight() {
        return this.getProp("_computeHeight");
    }

    /**
     * 计算大小
     *
     * @param width  宽度
     * @param height 高度
     * @return 计算后的大小值
     */
    default double[] computeSize(double width, double height) {
        double[] size = new double[2];
        // 获取父宽度
        double parentWidth = this.parentWidth();
        Double computeWidth1 = this.computeWidth();
        Double width1 = this.getProp("_width");
        Double parentWidth1 = this.getProp("_parentWidth");
        if (parentWidth1 == null || computeWidth1 == null || width1 == null || parentWidth1 != parentWidth || width1 != width) {
            double computeWidth;
            // 计算宽度值
            double flexValue = FlexUtil.computeFlexValue(this.getFlexWidth(), parentWidth);
            if (Double.isNaN(flexValue)) {
                computeWidth = width;
            } else {
                computeWidth = flexValue;
            }
            // 设置属性
            this.removeProp("_ignoreWidth");
            this.setProp("_width", width);
            this.setProp("_parentWidth", parentWidth);
            this.setProp("_computeWidth", computeWidth);
            size[0] = computeWidth;
        } else {
            size[0] = computeWidth1;
            this.setProp("_ignoreWidth", true);
        }

        // 获取父高度
        double parentHeight = this.parentHeight();
        Double height1 = this.getProp("_height");
        Double parentHeight1 = this.getProp("_parentHeight");
        Double computeHeight1 = this.computeHeight();
        if (parentHeight1 == null || computeHeight1 == null || height1 == null || parentHeight1 != parentHeight || height1 != height) {
            double computeHeight;
            // 计算高度值
            double flexValue = FlexUtil.computeFlexValue(this.getFlexHeight(), parentHeight);
            if (Double.isNaN(flexValue)) {
                computeHeight = height;
            } else {
                computeHeight = flexValue;
            }
            // 设置属性
            this.removeProp("_ignoreHeight");
            this.setProp("_height", height);
            this.setProp("_parentHeight", parentHeight);
            this.setProp("_computeHeight", computeHeight);
            size[1] = computeHeight;
            System.out.println("height=" + height + " height1=" + height1);
            System.out.println("parentHeight=" + parentHeight + " parentHeight1=" + parentHeight1);
            System.out.println("computeHeight=" + computeHeight + " computeHeight1=" + computeHeight1);
            System.out.println("+++++++++++++++++++++");
        } else {
            this.setProp("_ignoreHeight", true);
            size[1] = computeHeight1;
        }
        return size;
    }

    // /**
    //  * 计算宽度值
    //  *
    //  * @param width 宽度
    //  * @return 计算后的宽度
    //  */
    // default double computeWidth(double width) {
    //     // 获取父宽度
    //     double parentWidth = this.parentWidth();
    //     Double computeWidth1 = this.computeWidth();
    //     Double parentWidth1 = this.getProp("_parentWidth");
    //     if (parentWidth1 == null || computeWidth1 == null || parentWidth1 != parentWidth) {
    //         double computeWidth = width;
    //         // 计算宽度值
    //         double flexValue = FlexUtil.computeFlexValue(this.getFlexWidth(), parentWidth);
    //         if (!Double.isNaN(flexValue)) {
    //             computeWidth = flexValue;
    //         }
    //         // 设置属性
    //         this.removeProp("_ignoreWidth");
    //         this.setProp("_parentWidth", parentWidth);
    //         this.setProp("_computeWidth", computeWidth);
    //         return computeWidth;
    //     }
    //     this.setProp("_ignoreWidth", true);
    //     return computeWidth1;
    // }
    //
    // /**
    //  * 计算高度值
    //  *
    //  * @param height 高度
    //  * @return 计算后的高度
    //  */
    // default double computeHeight(double height) {
    //     // 获取父高度
    //     double parentHeight = this.parentHeight();
    //     Double parentHeight1 = this.getProp("_parentHeight");
    //     Double computeHeight1 = this.computeHeight();
    //     if (parentHeight1 == null || computeHeight1 == null || parentHeight1 != parentHeight) {
    //         double computeHeight = height;
    //         // 计算高度值
    //         double flexValue = FlexUtil.computeFlexValue(this.getFlexHeight(), parentHeight);
    //         if (!Double.isNaN(flexValue)) {
    //             computeHeight = flexValue;
    //         }
    //         // 设置属性
    //         this.removeProp("_ignoreHeight");
    //         this.setProp("_parentHeight", parentHeight);
    //         this.setProp("_computeHeight", computeHeight);
    //         return computeHeight;
    //     }
    //     this.setProp("_ignoreHeight", true);
    //     return computeHeight1;
    // }

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
        Double computeWidth;
        if (this.hasProp("_ignoreWidth")) {
            computeWidth = null;
        } else {
            computeWidth = this.computeWidth();
        }
        Double computeHeight;
        if (this.hasProp("_ignoreHeight")) {
            computeHeight = null;
        } else {
            computeHeight = this.computeHeight();
        }
        // System.out.println("computeWidth=" + computeWidth + " computeHeight=" + computeHeight);
        // 重新拉伸节点
        this.resizeNode(computeWidth, computeHeight);
    }

    /**
     * 重新拉伸节点
     *
     * @param width  新宽度
     * @param height 新高度
     */
    default void resizeNode(Double width, Double height) {
        // 处理宽度
        if (width != null && !Double.isNaN(width)) {
            this.setRealWidth(width);
            // TableView处理
            if (this instanceof TableView<?> tableView) {
                ObservableList<? extends TableColumn<?, ?>> columns = tableView.getVisibleLeafColumns();
                for (TableColumn<?, ?> column : columns) {
                    if (column instanceof FlexAdapter flexNode) {
                        flexNode.setRealWidth(FlexUtil.computeFlexValue(flexNode.getFlexWidth(), width));
                    }
                }
            } else if (this instanceof TabPane tabPane) {// TabPane处理
                ObservableList<Tab> tabs = tabPane.getTabs();
                for (Tab tab : tabs) {
                    if (tab.getContent() instanceof FlexAdapter flexNode) {
                        flexNode.setRealWidth(FlexUtil.computeFlexValue(flexNode.getFlexWidth(), width));
                    }
                }
            }
        }

        // 处理高度，y轴
        if (height != null && !Double.isNaN(height)) {
            this.setRealHeight(height);
            // TabPane处理
            if (this instanceof TabPane tabPane) {
                ObservableList<Tab> tabs = tabPane.getTabs();
                for (Tab tab : tabs) {
                    if (tab.getContent() instanceof FlexAdapter flexNode) {
                        flexNode.setRealHeight(FlexUtil.computeFlexValue(flexNode.getFlexHeight(), height));
                    }
                }
            }
            if (StrUtil.isNotBlank(this.getFlexY())) {
                this.setLayoutY(this.computeY());
            }
        }
    }
}
