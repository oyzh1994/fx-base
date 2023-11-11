//package cn.oyzh.fx.plus.flex;
//
//import lombok.Data;
//import lombok.experimental.Accessors;
//import lombok.extern.slf4j.Slf4j;
//
///**
// * 流式布局值
// *
// * @author oyzh
// * @since 2022/3/14
// */
//@Deprecated
//@Slf4j
//@Data
//@Accessors(fluent = true)
//public class FlexValue {
//
//    /**
//     * 固定值部分
//     */
//    private Double fixed;
//
//    /**
//     * 百分值部分
//     */
//    private Double percent;
//
//    /**
//     * 固定值操作符
//     * 0: +
//     * 1: -
//     * 2: *
//     * 3: /
//     */
//    private Byte fixedOperator;
//
//    public FlexValue() {
//    }
//
//    public FlexValue(Double percent) {
//        this.percent = percent;
//    }
//
//    public FlexValue(Double fixed, Double percent, Byte fixedOperator) {
//        this.fixed = fixed;
//        this.percent = percent;
//        this.fixedOperator = fixedOperator;
//    }
//
//    /**
//     * 是否有固定部分
//     *
//     * @return 结果
//     */
//    public boolean hasFixed() {
//        return this.fixed != null && this.fixedOperator != null;
//    }
//
//    /**
//     * 计算流式值
//     *
//     * @param value 真实值
//     * @return 计算后的流式值
//     */
//    public double computeValue(double value) {
//        double flexValue = Double.NaN;
//        if (this.percent != null) {
//            flexValue = value * this.percent;
//        }
//        if (this.hasFixed()) {
//            if (this.fixedOperator == 0) {
//                flexValue += this.fixed;
//            } else if (this.fixedOperator == 1) {
//                flexValue -= this.fixed;
//            } else if (this.fixedOperator == 2) {
//                flexValue *= this.fixed;
//            } else if (this.fixedOperator == 4) {
//                flexValue /= this.fixed;
//            }
//        }
//        return flexValue;
//    }
//}
