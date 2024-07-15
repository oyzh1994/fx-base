// package cn.oyzh.fx.plus.controls.select;
//
// import cn.hutool.core.util.NumberUtil;
// import cn.hutool.core.util.StrUtil;
// import cn.oyzh.fx.common.util.NumUtil;
// import cn.oyzh.fx.plus.converter.DigitalFormatStringConverter;
// import javafx.scene.control.TextFormatter;
// import lombok.Getter;
// import lombok.Setter;
//
// import java.util.function.UnaryOperator;
//
// /**
//  * @author oyzh
//  * @since 2024/07/12
//  */
// public class IntegerSelectTextFiled extends DigitalSelectTextField {
//
//     public IntegerSelectTextFiled(boolean unsigned ) {
//         super(unsigned, null);
//     }
//
//     public IntegerSelectTextFiled(boolean unsigned, Long maxLen) {
//         super(unsigned, maxLen);
//     }
//
//     @Override
//     protected DigitalFormatStringConverter getConverter() {
//         return new DigitalFormatStringConverter();
//     }
//
//     /**
//      * 创建一个过滤器，用于限制文本输入
//      *
//      * @return 过滤器
//      */
//     protected UnaryOperator<TextFormatter.Change> createFilter() {
//         return change -> {
//             if (change.isAdded() || change.isReplaced() || change.isContentChange()) {
//                 try {
//                     String text = change.getControlNewText();
//                     // 如果文本为空、"+"，则不进行任何操作，直接返回原change对象
//                     if (StrUtil.isEmpty(text) || text.equals("+")) {
//                         return change;
//                     }
//                     // 无符号判断
//                     if (this.isUnsigned() && text.startsWith("-")) {
//                         return null;
//                     }
//                     // 数字判断
//                     Number l = NumberUtil.parseNumber(text);
//                     // Number l = this.converter.fromString(text);
//                     if (l == null) {
//                         return null;
//                     }
//                     // 长度判断
//                     if (!super.checkLenLimit(change)) {
//                         return null;
//                     }
//                     // 如果超过了最大值，则将组件值设置为最大值
//                     if (this.maxVal != null && NumUtil.isGT(l.longValue(), this.maxVal)) {
//                         this.setValue(this.maxVal.longValue());
//                         return null;
//                     }
//                     // 如果小于了最小值，则将组件值设置为最小值
//                     if (this.minVal != null && NumUtil.isLT(l.longValue(), this.minVal)) {
//                         this.setValue(this.minVal.longValue());
//                         return null;
//                     }
//                 } catch (Exception ignored) {
//                 }
//             }
//             return change;
//         };
//     }
// }
