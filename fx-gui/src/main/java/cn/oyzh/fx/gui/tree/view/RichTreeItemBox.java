package cn.oyzh.fx.gui.tree.view;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.controls.box.FlexHBox;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.rich.RichTextFlow;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * @author oyzh
 * @since 2025-01-22
 */
public class RichTreeItemBox extends FlexHBox {

    public RichTreeItemBox() {
        super();
    }

    public RichTreeItemBox(RichTreeItemValue value) {
        super();
        this.init(value);
    }

    public void init(RichTreeItemValue value) {
        String name = value.name();
        String extra = value.extra();
        SVGGlyph glyph = value.graphic();
        Color color = value.graphicColor();
        if (this.isChildEmpty()) {
            // 图标
            glyph.setColor(color);
            this.addChild(glyph);
            // 名称
            this.addChild(new RichTextFlow(name));
            // 额外信息
            if (StringUtil.isNotBlank(extra)) {
                this.addChild(new Text(extra));
            }
        } else {
            // 更新图标
            SVGGlyph graphic = this.getGraphic();
            if (graphic != glyph) {
                this.setGraphic(glyph);
            }
            if (graphic.getColor() != color) {
                graphic.setColor(color);
            }
            // 更新名称
            RichTextFlow text1 = (RichTextFlow) this.getChild(1);
            if (StringUtil.notEquals(name, text1.getText())) {
                text1.setText(name);
            }
            // 更新额外信息
            if (StringUtil.isNotBlank(extra)) {
                Text text2 = (Text) this.getChild(2);
                if (text2 == null) {
                    this.addChild(new Text(extra));
                } else if (StringUtil.notEquals(extra, text2.getText())) {
                    text2.setText(extra);
                }
            } else {
                this.removeChild(2);
            }
        }
    }

    public SVGGlyph getGraphic() {
        return (SVGGlyph) this.getFirstChild();
    }

    public void setGraphic(SVGGlyph glyph) {
        this.setChild(0, glyph);
    }
//
//    public void setContent(String text, String extra) {
//        RichTextFlow text1 = (RichTextFlow) this.getChild(1);
//        text1.setText(text);
//        Text text2 = (Text) this.getChild(2);
//        if (text2 != null) {
//            text2.setText(extra);
//        }
//    }
}
