package cn.oyzh.fx.plus.trees;

import cn.oyzh.fx.common.util.Destroyable;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.text.FXText;
import cn.oyzh.fx.plus.theme.ThemeManager;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.Objects;


/**
 * 富功能树节点值
 *
 * @author oyzh
 * @since 2023/11/10
 */
public class RichTreeItemValue extends FXHBox implements Destroyable {

    {
        this.setCache(false);
        this.setCacheShape(false);
        this.setCursor(Cursor.HAND);
    }

    /**
     * 刷新内容
     */
    public void flush() {
        this.flushGraphic();
        this.flushText();
        this.flushGraphicColor();
    }

    /**
     * 获取当前名称
     *
     * @return 当前名称
     */
    public String name() {
        String name = this.getProp("name");
        if (name != null) {
            return name;
        }
        Text text = this.text();
        if (text != null) {
            return text.getText();
        }
        return null;
    }

    /**
     * 设置当前名称
     *
     * @param name 名称
     */
    public void name(String name) {
        this.setProp("name", name);
        this.flushText();
    }

    /**
     * 获取当前文本
     *
     * @return 文本组件
     */
    public FXText text() {
        return (FXText) this.lookup("#name");
    }

    /**
     * 获取当前图标
     *
     * @return 当前图标
     */
    public Node graphic() {
        return this.lookup("#graphic");
    }

    /**
     * 设置当前图标
     *
     * @param graphic 当前图标
     */
    public void graphic(Node graphic) {
        if (graphic != null) {
            graphic.setId("graphic");
            this.setChild(0, graphic);
            HBox.setMargin(graphic, new Insets(0, 3, 0, 0));
        }
    }

    /**
     * 刷新文本
     */
    public void flushText() {
        FXText text = this.text();
        String name = this.name();
        if (text == null) {
            text = new FXText(name);
            text.setId("name");
            text.setFontSize(11);
            this.setChild(1, text);
            this.removeProp("name");
        } else if (name != null && !Objects.equals(text.getText(), name)) {
            text.setText(name);
        }
        if (ThemeManager.isDarkMode()) {
            if (text.getFill() != Color.WHITE) {
                text.setFill(Color.WHITE);
            }
        } else if (text.getFill() != Color.BLACK) {
            text.setFill(Color.BLACK);
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
        if (this.graphic() instanceof SVGGlyph glyph) {
            if (ThemeManager.isDarkMode()) {
                glyph.setColor(Color.WHITE);
            } else {
                glyph.setColor(Color.BLACK);
            }
            // 移除等待动画设置的颜色，避免被重复覆盖
            glyph.removeProp("_color");
        }
    }

    @Override
    public void destroy() {
        this.clearChild();
        this.clearProps();
    }
}
