package cn.oyzh.fx.editor.incubator.control;

import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.editor.incubator.EditorFormatType;
import cn.oyzh.fx.gui.skin.EnlargeTextFiledSkin;
import cn.oyzh.fx.gui.svg.glyph.CancelSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.SubmitSVGGlyph;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.window.PopupExt;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

/**
 * 可放大的编辑器输入框皮肤
 *
 * @author oyzh
 * @since 2026-07-23
 */
public class EditorEnlargeTextFiledSkin extends EnlargeTextFiledSkin {

    /**
     * 构造可放大的编辑器输入框皮肤
     *
     * @param textField 文本输入框
     */
    public EditorEnlargeTextFiledSkin(TextField textField) {
        super(textField);
    }

    /**
     * 编辑器
     */
    private Editor editor;

    @Override
    protected void onButtonClick(MouseEvent e) {
        if (this.popup == null) {
            this.popup = new PopupExt();
            this.popup.setWidth(this.enlargeWidth);
            this.popup.setHeight(this.enlargeHeight);
            this.popup.setOnHiding(windowEvent -> this.handleHide());
        }
        // 输入框
        TextField textField = this.getSkinnable();
        textField.setDisable(true);
        // 文本节点
        if (this.editor == null) {
            this.editor = new Editor();
        }
        this.editor.hideLineNum();
        this.editor.setText(this.getText());
        // 按钮
        SubmitSVGGlyph ok = new SubmitSVGGlyph();
        ok.setOnMousePrimaryClicked(event -> this.onSubmit(this.editor.getTextTrim()));
        CancelSVGGlyph cancel = new CancelSVGGlyph();
        cancel.setOnMousePrimaryClicked(event -> this.handleHide());
        HBox.setMargin(ok, new Insets(5, 0, 0, 5));
        HBox.setMargin(cancel, new Insets(5, 0, 0, 15));
        FXHBox hBox = new FXHBox(ok, cancel);
        // 组装阶段
        FXVBox vBox = new FXVBox(this.editor, hBox);
        this.popup.content(vBox);
        this.popup.setOnHiding(event -> this.onSubmit(this.editor.getTextTrim()));
        this.popup.showPopup(textField);
    }

    /**
     * 设置格式类型
     *
     * @param formatType 格式类型
     */
    public void setFormatType(EditorFormatType formatType) {
        if (this.editor == null) {
            this.editor = new Editor();
        }
        this.editor.setFormatType(formatType);
    }
}
