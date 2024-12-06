package cn.oyzh.fx.plus.controls;

import cn.oyzh.fx.plus.controls.box.FlexVBox;
import cn.oyzh.fx.plus.controls.text.FlexText;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import lombok.NonNull;

/**
 * 分割器
 *
 * @author oyzh
 * @since 2023/5/11
 */
public class FXSeparator extends FlexVBox {

    /**
     * 文字
     */
    private FlexText text;

    {
        // 初始化分割器
        FlexSeparator separator = new FlexSeparator();
        separator.setFlexWidth("100%");
        separator.setFlexHeight("50%");
        separator.setPadding(Insets.EMPTY);
        this.addChild(separator);
    }

    /**
     * 设置文本
     *
     * @param text 文本
     */
    public void setText(@NonNull String text) {
        if (this.text == null) {
            this.text = new FlexText();
            this.text.setFlexHeight("50%");
            this.addChild(0, this.text);
            HBox.setMargin(this.text, new Insets(0, 0, 0, 5));
        }
        this.text.setText(text);
    }

    /**
     * 获取文本
     *
     * @return 文本内容
     */
    public String getText() {
        if (this.text == null) {
            return null;
        }
        return this.text.getText();
    }
}
