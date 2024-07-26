// package cn.oyzh.fx.plus.controls.digital;
//
// /**
//  * double文本域
//  *
//  * @author oyzh
//  * @since 2023/12/22
//  */
// public class DoubleTextField extends DecimalTextField {
//
//     public DoubleTextField() {
//         super(false);
//     }
//
//     public DoubleTextField(boolean unsigned) {
//         super(unsigned);
//     }
//
//     public DoubleTextField(boolean unsigned, Long maxLen) {
//         super(unsigned, maxLen);
//     }
//
//     public DoubleTextField(boolean unsigned, Long maxLen, Integer scaleLen) {
//         super(unsigned, maxLen, scaleLen);
//     }
//
//     @Override
//     public void setUnsigned(boolean unsigned) {
//         super.setUnsigned(unsigned);
//         if (unsigned) {
//             super.setMin(0D);
//             super.setMax(Double.MAX_VALUE);
//         } else {
//             super.setMin(-Double.MAX_VALUE);
//             super.setMax(Double.MAX_VALUE);
//         }
//     }
// }
