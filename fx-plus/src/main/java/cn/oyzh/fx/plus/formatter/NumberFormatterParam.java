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
// public class NumberFormatterParam {
//
//     @Getter
//     private Long maxVal;
//
//     @Getter
//     private Long minVal;
//
//     public void setMaxVal(Number maxVal) {
//         if (this.minVal != null && maxVal.longValue() < this.minVal) {
//             throw new InvalidParameterException("maxVal不能小于minVal！");
//         }
//         this.maxVal = maxVal.longValue();
//     }
//
//     public void setMinVal(Number minVal) {
//         if (this.maxVal != null && minVal.longValue() > this.maxVal) {
//             throw new InvalidParameterException("minVal不能大于maxVal！");
//         }
//         this.minVal = minVal.longValue();
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
//         return val.longValue() >= this.minVal;
//     }
//
//     public boolean checkMaxVal(Number val) {
//         if (this.maxVal == null) {
//             return true;
//         }
//         return val.longValue() <= this.maxVal;
//     }
// }
