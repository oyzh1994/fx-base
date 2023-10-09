package cn.oyzh.fx.plus.formatter;// package cn.oyzh.fx.plus.formatter;
//
// import lombok.Getter;
//
// import java.security.InvalidParameterException;
//
// /**
//  * @author oyzh
//  * @since 2023/8/15
//  */
// public class DecimalFormatterParam {
//
//     @Getter
//     private Double maxVal;
//
//     @Getter
//     private Double minVal;
//
//     public void setMaxVal(Number maxVal) {
//         if (this.minVal != null && maxVal.doubleValue() < this.minVal) {
//             throw new InvalidParameterException("maxVal不能小于minVal！");
//         }
//         this.maxVal = maxVal.doubleValue();
//     }
//
//     public void setMinVal(Number minVal) {
//         if (this.maxVal != null && minVal.doubleValue() > this.maxVal) {
//             throw new InvalidParameterException("minVal不能大于maxVal！");
//         }
//         this.minVal = minVal.doubleValue();
//     }
//
//     public boolean isEmpty() {
//         return this.minVal == null && this.maxVal == null;
//     }
//
//     public boolean checkMinVal(Number val) {
//         if (this.minVal == null) {
//             return true;
//         }
//         return val.doubleValue() >= this.minVal;
//     }
//
//     public boolean checkMaxVal(Number val) {
//         if (this.maxVal == null) {
//             return true;
//         }
//         return val.doubleValue() <= this.maxVal;
//     }
// }
