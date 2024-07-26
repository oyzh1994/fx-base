// package cn.oyzh.fx.plus.controls.digital;
//
// /**
//  * byte文本域
//  *
//  * @author oyzh
//  * @since 2023/12/22
//  */
// public class ByteTextField extends NumberTextField {
//
//     public ByteTextField() {
//         super(false);
//     }
//
//     public ByteTextField(boolean unsigned) {
//         super(unsigned);
//     }
//
//     public ByteTextField(boolean unsigned, Long maxLen) {
//         super(unsigned, maxLen);
//     }
//
//     @Override
//     public void setUnsigned(boolean unsigned) {
//         super.setUnsigned(unsigned);
//         if (unsigned) {
//             super.setMin(0L);
//             super.setMaxVal(255);
//         } else {
//             super.setMin((long) Byte.MIN_VALUE);
//             super.setMax((long) Byte.MAX_VALUE);
//         }
//     }
// }
