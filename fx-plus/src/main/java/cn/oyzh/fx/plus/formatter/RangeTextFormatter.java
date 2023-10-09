package cn.oyzh.fx.plus.formatter;// package cn.oyzh.fx.plus.formatter;
//
// import javafx.scene.control.TextFormatter;
// import lombok.Getter;
//
// /**
//  * @author oyzh
//  * @since 2023/8/15
//  */
// public class RangeTextFormatter extends TextFormatter<String> {
//
//     /**
//      * 参数
//      */
//     @Getter
//     private final RangeFormatterParam param;
//
//     public RangeTextFormatter(RangeFormatterParam param) {
//         super(change -> {
//             if (param.isEmpty()) {
//                 return change;
//             }
//             try {
//                 String text = change.getControlNewText();
//                 if (change.isAdded() && !param.checkMaxLen(text)) {
//                     return null;
//                 }
//                 if (change.isReplaced() || change.isDeleted()) {
//                     if (!param.checkMinLen(text) || !param.checkMaxLen(text)) {
//                         return null;
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
