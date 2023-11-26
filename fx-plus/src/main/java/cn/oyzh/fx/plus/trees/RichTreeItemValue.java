package cn.oyzh.fx.plus.trees;

import cn.oyzh.fx.plus.controls.FXHBox;
import cn.oyzh.fx.plus.controls.text.FXText;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import lombok.Getter;
import lombok.experimental.Accessors;


/**
 * 富功能树节点值
 *
 * @author oyzh
 * @since 2023/11/10
 */
//@Slf4j
public class RichTreeItemValue extends FXHBox {

    {
        this.setCursor(Cursor.HAND);
    }

    /**
     * 当前名称
     */
    @Getter
    @Accessors(fluent = true, chain = false)
    private String name;

    /**
     * 设置当前名称
     *
     * @param name 名称
     */
    public void name(String name) {
        this.name = name;
        this.flushText();
    }

    /**
     * 获取当前文本
     *
     * @return 文本组件
     */
    public FXText text() {
        if (this.getChild(1) instanceof FXText text) {
            return text;
        }
        return null;
    }

    /**
     * 获取当前图标
     *
     * @return 当前图标
     */
    public Node graphic() {
        return this.getChild(0);
    }

    /**
     * 设置当前图标
     *
     * @param graphic 当前图标
     */
    public void graphic(Node graphic) {
        if (graphic != null) {
            this.setChild(0, graphic);
            HBox.setMargin(graphic, new Insets(0, 3, 0, 0));
        }
    }

    /**
     * 刷新文本
     */
    public void flushText() {
        FXText text = this.text();
        if (text == null) {
            text = new FXText(this.name());
            text.setFontSize(11);
            this.setChild(1, text);
        } else {
            text.setText(this.name());
        }
    }

    /**
     * 刷新图标
     */
    public void flushGraphic() {

    }

    /**
     * 刷新图标颜色
     */
    public void flushGraphicColor() {

    }
}
