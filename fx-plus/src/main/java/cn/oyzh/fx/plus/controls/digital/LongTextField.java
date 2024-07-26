// package cn.oyzh.fx.plus.controls.digital;
//
// import java.math.BigDecimal;
//
// /**
//  * long文本域
//  *
//  * @author oyzh
//  * @since 2023/12/22
//  */
// public class LongTextField extends NumberTextField {
//
//     public LongTextField() {
//         super(false);
//     }
//
//     public LongTextField(boolean unsigned) {
//         super(unsigned);
//     }
//
//     public LongTextField(boolean unsigned, Long maxLen) {
//         super(unsigned, maxLen);
//     }
//
//     @Override
//     public void setUnsigned(boolean unsigned) {
//         super.setUnsigned(unsigned);
//         if (unsigned) {
//             super.setMin(0L);
//             super.setMaxVal(new BigDecimal("18446744073709551615"));
//         } else {
//             super.setMin(Long.MIN_VALUE);
//             super.setMax(Long.MAX_VALUE);
//         }
//     }
// }
