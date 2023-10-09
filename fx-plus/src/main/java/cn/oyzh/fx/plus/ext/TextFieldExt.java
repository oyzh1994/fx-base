// package cn.oyzh.fx.plus.ext;
//
// import cn.oyzh.fx.plus.controls.BaseTextField;
// import cn.oyzh.fx.plus.controls.FlexPane;
// import javafx.beans.value.ChangeListener;
// import lombok.Getter;
//
// /**
//  * @author oyzh
//  * @since 2020/10/29
//  */
// public abstract class TextFieldExt extends FlexPane {
//
//     /**
//      * 文本组件
//      */
//     @Getter
//     protected final BaseTextField textField;
//
//     public TextFieldExt(BaseTextField textField) {
//         this.textField = textField;
//         // 监听事件
//         this.textField.textProperty().addListener((observableValue, s, t1) -> this.showButton());
//         this.textField.disableProperty().addListener((observableValue, s, t1) -> this.showButton());
//         this.textField.focusedProperty().addListener((observableValue, s, t1) -> this.showButton());
//         this.textField.editableProperty().addListener((observableValue, s, t1) -> this.showButton());
//     }
//
//     /**
//      * 显示按钮
//      */
//     protected void showButton() {
//     }
//
//     public void setTipText(String tipText) {
//         this.textField.setTipText(tipText);
//     }
//
//     public String getTipText() {
//         return this.textField.getTipText();
//     }
//
//     public boolean isRequire() {
//         return this.textField.isRequire();
//     }
//
//     public void setRequire(boolean require) {
//         this.textField.setRequire(require);
//     }
//
//     public void addTextChangedListener(ChangeListener<String> listener) {
//         this.textField.addTextChangedListener(listener);
//     }
//
//     public void removeTextChangedListener(ChangeListener<String> listener) {
//         this.textField.removeTextChangedListener(listener);
//     }
//
//     public void clear() {
//         this.textField.clear();
//     }
//
//     public boolean validate() {
//         return textField.validate();
//     }
//
//     public String getTextTrim() {
//         return this.textField.getTextTrim();
//     }
//
//     /**
//      * 是否为空
//      *
//      * @return 结果
//      */
//     public boolean isEmpty() {
//         return this.textField.isEmpty();
//     }
//
//     /**
//      * 选中末尾
//      */
//     public void selectEnd() {
//         this.textField.selectEnd();
//     }
//
//     /**
//      * 是否可编辑
//      *
//      * @return 结果
//      */
//     public boolean isEditable() {
//         return this.textField.isEditable();
//     }
//
//     /**
//      * 设置是否可编辑
//      *
//      * @param editable 结果
//      */
//     public void setEditable(boolean editable) {
//         this.textField.setEditable(editable);
//     }
// }
