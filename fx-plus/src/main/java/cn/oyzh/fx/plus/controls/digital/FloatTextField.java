// package cn.oyzh.fx.plus.controls.digital;
//
//
// /**
//  * float文本域
//  *
//  * @author oyzh
//  * @since 2023/12/22
//  */
// public class FloatTextField extends DecimalTextField {
//
//     public FloatTextField() {
//         super(false);
//     }
//
//     public FloatTextField(boolean unsigned) {
//         super(unsigned);
//     }
//
//     public FloatTextField(boolean unsigned, Long maxLen) {
//         super(unsigned, maxLen);
//     }
//
//     public FloatTextField(boolean unsigned, Long maxLen, Integer scaleLen) {
//         super(unsigned, maxLen, scaleLen);
//     }
//
//     @Override
//     public void setUnsigned(boolean unsigned) {
//         super.setUnsigned(unsigned);
//         if (unsigned) {
//             super.setMin(0D);
//             super.setMax((double) Float.MAX_VALUE);
//         } else {
//             super.setMin((double) -Float.MAX_VALUE);
//             super.setMax((double) Float.MAX_VALUE);
//         }
//     }
// }
