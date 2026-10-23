package cn.oyzh.fx.editor.incubator.control;

import cn.oyzh.fx.editor.incubator.EditorFormatType;
import cn.oyzh.fx.gui.skin.SelectTextFiledSkin;
import cn.oyzh.fx.plus.util.ListViewUtil;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Callback;

/**
 * 编辑器格式类型输入框皮肤，候选项同时显示扩展名，便于过滤选择。
 *
 * @author oyzh
 * @since 2026-10-24
 */
public class EditorFormatTypeTextFieldSkin extends SelectTextFiledSkin<EditorFormatType> {

    /**
     * 构造格式类型输入框皮肤
     *
     * @param textField 关联的文本框
     */
    public EditorFormatTypeTextFieldSkin(TextField textField) {
        super(textField);
    }

    @Override
    protected void initPopup() {
        super.initPopup();
        this.listView().setCellFactory(new Callback<>() {
            @Override
            public ListCell<EditorFormatType> call(ListView<EditorFormatType> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(EditorFormatType item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            this.setText(null);
                        } else {
                            String extension = item.getExtension();
                            this.setText(extension.isBlank() ? item.getName() : item.getName() + "  (" + extension + ")");
                            this.setHeight(getLineHeight());
                            this.setMaxHeight(getLineHeight());
                            this.setMinHeight(getLineHeight());
                            this.setPrefHeight(getLineHeight());
                            ListViewUtil.highlightCell(this);
                        }
                    }
                };
            }
        });
    }
}
