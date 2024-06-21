// package cn.oyzh.fx.plus.controls.area;
//
// import cn.oyzh.fx.plus.LimitLenControl;
// import cn.oyzh.fx.plus.operator.LimitOperator;
// import javafx.scene.control.TextFormatter;
// import lombok.Getter;
//
// import java.security.InvalidParameterException;
// import java.util.function.UnaryOperator;
//
// /**
//  * 限制文本域
//  *
//  * @author oyzh
//  * @since 2023/09/22
//  */
// public class LimitTextArea extends FlexTextArea implements LimitLenControl {
//
//     /**
//      * 最大长度
//      */
//     @Getter
//     private Long maxLen;
//
//     public LimitTextArea() {
//         super.setText("");
//     }
//
//     public LimitTextArea(String text) {
//         super.setText(text);
//     }
//
//     public LimitTextArea(Long maxLen) {
//         this.setMaxLen(maxLen);
//     }
//
//     public LimitTextArea(String text, Long maxLen) {
//         super.setText(text);
//         this.setMaxLen(maxLen);
//     }
//
//     @Override
//     public void setMaxLen(Long maxLen) {
//         this.maxLen = maxLen;
//         if (maxLen != null && this.getTextFormatter() == null) {
//             this.setTextFormatter(new TextFormatter<>(new LimitOperator()));
//         }
//     }
// }
