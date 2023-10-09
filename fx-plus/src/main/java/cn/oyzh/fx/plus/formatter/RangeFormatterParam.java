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
// public class RangeFormatterParam {
//
//     @Getter
//     private Long maxLen;
//
//     @Getter
//     private Long minLen;
//
//     public void setMaxLen(Number maxLen) {
//         if (this.minLen != null && maxLen.longValue() < this.minLen) {
//             throw new InvalidParameterException("maxLen不能小于minLen！");
//         }
//         this.maxLen = maxLen.longValue();
//     }
//
//     public void setMinLen(Number minLen) {
//         if (this.maxLen != null && minLen.longValue() > this.maxLen) {
//             throw new InvalidParameterException("minLen不能大于maxLen！");
//         }
//         this.minLen = minLen.longValue();
//     }
//
//     public boolean isEmpty() {
//         return this.maxLen == null && this.minLen == null;
//     }
//
//     public boolean checkMinLen(String text) {
//         if (this.minLen == null || text == null) {
//             return true;
//         }
//         return text.length() >= this.minLen;
//     }
//
//     public boolean checkMaxLen(String text) {
//         if (this.maxLen == null || text == null) {
//             return true;
//         }
//         return text.length() <= this.maxLen;
//     }
// }
