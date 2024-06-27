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
     * 设置流式宽
     *
     * @param node      节点
     * @param flexValue 流式值
     */
    public static void flexWidth(Object node, String flexValue) {
        if (node instanceof FlexAdapter adapter) {
            adapter.setFlexWidth(flexValue);
        }
    }

    /**
     * 设置流式高
     *
     * @param node      节点
     * @param flexValue 流式值
     */
    public static void flexHeight(Object node, String flexValue) {
        if (node instanceof FlexAdapter adapter) {
            adapter.setFlexHeight(flexValue);
        }
    }

    /**
     * 计算流式值
     *
     * @param flexValue 流式值
     * @param value     真实值
     * @return 计算后的流式值
     */
    public static double compute(String flexValue, Double value) {
        if (value != null && StrUtil.isNotBlank(flexValue) && !Double.isNaN(value)) {
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
    }


}
