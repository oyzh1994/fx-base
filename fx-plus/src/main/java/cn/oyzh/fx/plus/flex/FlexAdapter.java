package cn.oyzh.fx.plus.flex;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;

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
        this.clearComputeSize();
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
        this.clearComputeSize();
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
        this.clearComputeSize();
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
        this.clearComputeSize();
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
     * 清除计算大小属性
     */
    private void clearComputeSize() {
        this.removeProp("_width");
        this.removeProp("_parentWidth");
        this.removeProp("_computeWidth");
        this.removeProp("_height");
        this.removeProp("_parentHeight");
        this.removeProp("_computeHeight");
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
            String flexWidth = this.getFlexWidth();
            // 计算宽度值
            double flexValue = FlexUtil.compute(flexWidth, parentWidth);
            if (Double.isNaN(flexValue)) {
                size[0] = width;
            } else {
                // 设置属性
                this.setProp("_width", width);
                this.setProp("_parentWidth", parentWidth);
                this.setProp("_computeWidth", flexValue);
                size[0] = flexValue;
            }
        } else {
            size[0] = computeWidth1;
        }

        // 获取父高度
        double parentHeight = this.parentHeight();
        Double height1 = this.getProp("_height");
        Double parentHeight1 = this.getProp("_parentHeight");
        Double computeHeight1 = this.computeHeight();
        if (parentHeight1 == null || computeHeight1 == null || height1 == null || parentHeight1 != parentHeight || height1 != height) {
            String flexHeight = this.getFlexHeight();
            // 计算高度值
            double flexValue = FlexUtil.compute(flexHeight, parentHeight);
            if (Double.isNaN(flexValue)) {
                size[1] = height;
            } else {
                // 设置属性
                this.setProp("_height", height);
                this.setProp("_parentHeight", parentHeight);
                this.setProp("_computeHeight", flexValue);
                size[1] = flexValue;
            }
        } else {
            size[1] = computeHeight1;
        }
        return size;
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
        return FlexUtil.compute(this.getFlexX(), parentWidth);
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
        return FlexUtil.compute(this.getFlexY(), parentHeight);
    }

    /**
     * 重新拉伸节点，自动计算
     */
    default void resizeNode() {
        Double computeWidth = this.computeWidth();
        Double computeHeight = this.computeHeight();
        // 重新拉伸节点
        this.resizeNode(computeWidth, computeHeight);
//        System.out.println("computeWidth=" + computeWidth + " computeHeight=" + computeHeight + " node=" + this);
    }

    /**
     * 重新拉伸节点
     *
     * @param width  新宽度
     * @param height 新高度
     */
    default void resizeNode(Double width, Double height) {
        boolean changeWidth = width != null && !Double.isNaN(width);
        boolean changeHeight = height != null && !Double.isNaN(height);
        // 处理宽度
        if (changeWidth) {
            this.setRealWidth(width);
        }

        // 处理高度，y轴
        if (changeHeight) {
            this.setRealHeight(height);
            if (StrUtil.isNotBlank(this.getFlexY())) {
                this.setLayoutY(this.computeY());
            }
        }
    }
}
