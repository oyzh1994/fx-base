package cn.oyzh.fx.gui.font;

import cn.oyzh.fx.gui.skin.SelectTextFiledSkin;
import cn.oyzh.fx.plus.controls.list.FXListView;
import cn.oyzh.fx.plus.font.FontManager;
import cn.oyzh.fx.plus.util.ListViewUtil;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Callback;

/**
 * 字体选择下拉皮肤，每个选项以自身字体渲染
 *
 * @author oyzh
 * @since 2026/06/20
 */
public class FontFamilyTextFieldSkin extends SelectTextFiledSkin<String> {

    public FontFamilyTextFieldSkin(TextField textField) {
        super(textField);
    }

    @Override
    protected void initPopup() {
        super.initPopup();
        FXListView<String> listView = this.listView();
        double fontSize = FontManager.currentFontSize();
        listView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            this.setText(null);
                            this.setFont(Font.getDefault());
                        } else {
                            this.setText(item);
                            this.setFont(Font.font(item, FontWeight.NORMAL, fontSize));
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
