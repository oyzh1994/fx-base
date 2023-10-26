package cn.oyzh.fx.plus.flex;

import cn.hutool.core.util.StrUtil;
import lombok.experimental.UtilityClass;

/**
 * 流式布局工具类
 *
 * @author oyzh
 * @since 2022/12/31
 */
@UtilityClass
public class FlexUtil {

    /**
     * 计算流式值
     *
     * @param flexValue 流式值
     * @param value     真实值
     * @return 计算后的流式值
     */
    public static double computeFlexValue(String flexValue, double value) {
        if (StrUtil.isNotBlank(flexValue) && !Double.isNaN(value)) {
            try {
                flexValue = flexValue.trim();
                Double fixed = null;
                Double percent = null;
                Byte fixedOperator = null;
                // 包含百分比
                if (flexValue.contains("%")) {
                    String[] strArr = flexValue.split("%");
                    // 百分比值
                    percent = Double.parseDouble(strArr[0].trim()) * 0.01;
                    // 固定值
                    if (strArr.length > 1) {
                        String str2 = strArr[1].trim();
                        if (str2.contains("+")) {
                            fixed = Double.parseDouble(str2.replace("+", "").trim());
                            fixedOperator = 0;
                        } else if (str2.contains("-")) {
                            fixed = Double.parseDouble(str2.replace("-", "").trim());
                            fixedOperator = 1;
                        } else if (str2.contains("*")) {
                            fixed = Double.parseDouble(str2.replace("*", "").trim());
                            fixedOperator = 2;
                        } else if (str2.contains("/")) {
                            fixed = Double.parseDouble(str2.replace("/", "").trim());
                            fixedOperator = 3;
                        }
                    }
                } else {// 直接计算固定值
                    fixed = Double.parseDouble(flexValue);
                }
                double val = Double.NaN;
                if (percent != null) {
                    val = value * percent;
                }
                if (fixed != null && fixedOperator != null) {
                    if (fixedOperator == 0) {
                        val += fixed;
                    } else if (fixedOperator == 1) {
                        val -= fixed;
                    } else if (fixedOperator == 2) {
                        val *= fixed;
                    } else {
                        val /= fixed;
                    }
                }
                return val;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return Double.NaN;
        // FlexValue val = FlexValueParser.INSTANCE.apply(flexValue);
        // return val == null ? Double.NaN : val.computeValue(value);
    }
//
//    /**
//     * 设置宽度
//     *
//     * @param columnBase 组件
//     * @param width      宽度
//     */
//    public static void setWidth(@NonNull TableColumnBase<?, ?> columnBase, double width) {
//        if (Double.isNaN(width) || width <= 0) {
//            return;
//        }
//        if (!columnBase.prefWidthProperty().isBound() && columnBase.getPrefWidth() != width) {
//            columnBase.setPrefWidth(width);
//        }
//        //if (!columnBase.maxWidthProperty().isBound() && columnBase.getMaxWidth() != width) {
//        //    columnBase.setMaxWidth(width);
//        //}
//        if (!columnBase.minWidthProperty().isBound() && columnBase.getMinWidth() != width) {
//            columnBase.setMinWidth(width);
//        }
//    }
//
//    /**
//     * 设置宽度
//     *
//     * @param control 组件
//     * @param width   宽度
//     */
//    public static void setWidth(@NonNull PopupControl control, double width) {
//        if (Double.isNaN(width) || width <= 0) {
//            return;
//        }
//        if (!control.prefWidthProperty().isBound() && control.getPrefWidth() != width) {
//            control.setPrefWidth(width);
//        }
//        //if (!control.maxWidthProperty().isBound() && control.getMaxWidth() != width) {
//        //    control.setMaxWidth(width);
//        //}
//        if (!control.minWidthProperty().isBound() && control.getMinWidth() != width) {
//            control.setMinWidth(width);
//        }
//    }
//
//    /**
//     * 设置宽度
//     *
//     * @param region 组件
//     * @param width  宽度
//     */
//    public static void setWidth(@NonNull Region region, double width) {
//        if (Double.isNaN(width) || width <= 0) {
//            return;
//        }
//        if (!region.prefWidthProperty().isBound() && region.getPrefWidth() != width) {
//            region.setPrefWidth(width);
//        }
//        //if (!region.maxWidthProperty().isBound() && region.getMaxWidth() != width) {
//        //    region.setMaxWidth(width);
//        //}
//        if (!region.minWidthProperty().isBound() && region.getMinWidth() != width) {
//            region.setMinWidth(width);
//        }
//    }
//
//    /**
//     * 设置宽度
//     *
//     * @param shape 组件
//     * @param width 宽度
//     */
//    public static void setWidth(@NonNull Shape shape, double width) {
//        if (Double.isNaN(width) || width <= 0) {
//            return;
//        }
//        if (!shape.strokeWidthProperty().isBound() && shape.getStrokeWidth() != width) {
//            shape.setStrokeWidth(width);
//        }
//    }
//
//    /**
//     * 设置目标组件高度
//     *
//     * @param region 组件
//     * @param height 高度
//     */
//    public static void setHeight(@NonNull Region region, double height) {
//        if (Double.isNaN(height) || height <= 0) {
//            return;
//        }
//        // 设置高度
//        if (!region.prefHeightProperty().isBound() && region.getPrefHeight() != height) {
//            region.setPrefHeight(height);
//        }
//        //if (!region.maxHeightProperty().isBound() && region.getMaxHeight() != height) {
//        //    region.setMaxHeight(height);
//        //}
//        if (!region.minHeightProperty().isBound() && region.getMinHeight() != height) {
//            region.setMinHeight(height);
//        }
//    }
//
//    /**
//     * 设置目标组件高度
//     *
//     * @param control 组件
//     * @param height  高度
//     */
//    public static void setHeight(@NonNull PopupControl control, double height) {
//        if (Double.isNaN(height) || height <= 0) {
//            return;
//        }
//        // 设置高度
//        if (!control.prefHeightProperty().isBound() && control.getPrefHeight() != height) {
//            control.setPrefHeight(height);
//        }
//        //if (!control.maxHeightProperty().isBound() && control.getMaxHeight() != height) {
//        //    control.setMaxHeight(height);
//        //}
//        if (!control.minHeightProperty().isBound() && control.getMinHeight() != height) {
//            control.setMinHeight(height);
//        }
//    }

//    /**
//     * 设置目标组件layoutY
//     *
//     * @param node    组件
//     * @param layoutY layoutY
//     */
//    public static void setLayoutY(@NonNull Node node, double layoutY) {
//        if (Double.isNaN(layoutY) || layoutY <= 0) {
//            return;
//        }
//        if (!node.layoutYProperty().isBound() && node.getLayoutY() != layoutY) {
//            node.setLayoutY(layoutY);
//        }
//    }
}
