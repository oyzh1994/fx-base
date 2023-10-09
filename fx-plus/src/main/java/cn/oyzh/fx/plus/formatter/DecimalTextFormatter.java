package cn.oyzh.fx.plus.formatter;// package cn.oyzh.fx.plus.formatter;
//
// import cn.hutool.core.util.NumberUtil;
// import javafx.scene.control.TextFormatter;
// import lombok.Getter;
//
// /**
//  * @author oyzh
//  * @since 2023/8/15
//  */
// public class DecimalTextFormatter extends TextFormatter<String> {
//
//     /**
//      * 参数
//      */
//     @Getter
//     private final DecimalFormatterParam param;
//
//     public DecimalTextFormatter(DecimalFormatterParam param) {
//         super(change -> {
//             try {
//                 if (change.isAdded() || change.isReplaced()) {
//                     String text = change.getControlNewText();
//                     if (!NumberUtil.isNumber(text)) {
//                         return null;
//                     }
//                     Double number = NumberUtil.parseDouble(text);
//                     char[] chars = text.toCharArray();
//                     if (change.getAnchor() == 0 && chars[0] == '.') {
//                         return null;
//                     }
//                     if (!param.isEmpty()) {
//                         if (!param.checkMinVal(number) || !param.checkMaxVal(number)) {
//                             return null;
//                         }
//                     }
//                 }
//                 return change;
//             } catch (Exception ignored) {
//             }
//             return null;
//         });
//         this.param = param;
//     }
//
//
// }
