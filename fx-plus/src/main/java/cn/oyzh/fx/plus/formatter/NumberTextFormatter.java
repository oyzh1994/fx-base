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
// public class NumberTextFormatter extends TextFormatter<String> {
//
//     /**
//      * 参数
//      */
//     @Getter
//     private final NumberFormatterParam param;
//
//     public NumberTextFormatter(NumberFormatterParam param) {
//         super(change -> {
//             try {
//                 if (change.isAdded() || change.isReplaced()) {
//                     String text = change.getControlNewText();
//                     if (!NumberUtil.isNumber(text)) {
//                         return null;
//                     }
//                     char[] chars = text.toCharArray();
//                     if (change.getAnchor() == 0 && chars[0] == '0') {
//                         return null;
//                     }
//                     if (!param.isEmpty()) {
//                         Number number = NumberUtil.parseNumber(text);
//                         if (!param.checkMinVal(number) || !param.checkMaxVal(number)) {
//                             return null;
//                         }
//                     }
//                 }
//                 return change;
//             } catch (Exception ex) {
//                 ex.printStackTrace();
//             }
//             return null;
//         });
//         this.param = param;
//     }
//
// }
