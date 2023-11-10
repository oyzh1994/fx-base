package cn.oyzh.fx.plus.controls;

import cn.oyzh.fx.plus.controls.text.FlexText;
import javafx.geometry.Insets;
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

    /**
     * 分割器
     */
    private final FlexSeparator separator;

    {
        // 初始化分割器
        this.separator = new FlexSeparator();
        this.separator.setFlexWidth("100%");
        this.separator.setFlexHeight("50%");
        this.getChildren().add(this.separator);
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
            this.getChildren().add(0, this.text);
            setMargin(this.text, new Insets(0, 0, 0, 5));
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
