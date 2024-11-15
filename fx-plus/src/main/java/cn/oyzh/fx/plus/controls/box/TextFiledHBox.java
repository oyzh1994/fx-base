// package cn.oyzh.fx.plus.controls.box;
//
// import cn.oyzh.fx.plus.controls.textfield.ClearableTextField;
// import cn.oyzh.fx.plus.controls.textfield.FlexTextField;
// import cn.oyzh.fx.plus.theme.ThemeManager;
// import javafx.beans.value.ChangeListener;
// import javafx.geometry.Insets;
// import javafx.scene.paint.Color;
//
// /**
//  * @author oyzh
//  * @since 2024/7/9
//  */
// public class TextFiledHBox extends FlexHBox {
//
//     {
//         this.setPadding(new Insets(0));
//     }
//
//     protected FlexTextField textField;
//
//     public TextFiledHBox() {
//         this(new ClearableTextField());
//     }
//
//     public TextFiledHBox(FlexTextField textField) {
//         this.setTextField(textField);
//     }
//
//     protected void setTextField(FlexTextField textField) {
//         this.textField = textField;
//         this.getChildren().add(this.textField);
//     }
//
//     protected Color getButtonColor() {
//         if (!ThemeManager.isDarkMode()) {
//             return Color.valueOf("#696969");
//         }
//         return Color.WHITE;
//     }
//
//     public String getText() {
//         return this.textField.getText();
//     }
//
//     public void setText(String text) {
//         this.textField.setText(text);
//     }
//
//     public void addTextChangeListener(ChangeListener<String> listener) {
//         this.textField.addTextChangeListener(listener);
//     }
// }
