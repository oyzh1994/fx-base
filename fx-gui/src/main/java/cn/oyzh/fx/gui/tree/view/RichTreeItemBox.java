package cn.oyzh.fx.gui.tree.view;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.controls.box.FlexHBox;
import cn.oyzh.fx.plus.controls.pane.FlexFlowPane;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.rich.RichTextFlow;
import javafx.geometry.Insets;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * @author oyzh
 * @since 2025-01-22
 */
public class RichTreeItemBox extends FlexFlowPane {

    {
        this.setRealHeight(15);
        this.setPadding(Insets.EMPTY);
    }

    private static final Insets DEFAULT_NODE_MARGIN = new Insets(0, 3, 0, 0);

    public RichTreeItemBox() {
        super();
    }

    public RichTreeItemBox(RichTreeItemValue value, String highlight) {
        super();
        this.init(value, highlight);
    }

    public void init(RichTreeItemValue value, String highlight) {
        String name = value.name();
        String extra = value.extra();
        SVGGlyph glyph = value.graphic();
        Color color = value.graphicColor();
        Color extraColor = value.extraColor();
        if (this.isChildEmpty()) {
            // 图标
            glyph.setColor(color);
            this.addChild(glyph);
            // 名称
            RichTextFlow textFlow = new RichTextFlow(name, highlight);
            this.addChild(textFlow);
            // 额外信息
            if (StringUtil.isNotBlank(extra)) {
                Text extraText = new Text(extra);
                if (extraColor != null) {
                    extraText.setFill(extraColor);
                }
                this.addChild(extraText);
            }
            FlowPane.setMargin(glyph, DEFAULT_NODE_MARGIN);
            FlowPane.setMargin(textFlow, DEFAULT_NODE_MARGIN);
        } else {
            // 更新图标
            SVGGlyph graphic = this.getGraphic();
            if (graphic != glyph) {
                this.setGraphic(glyph);
                FlowPane.setMargin(glyph, DEFAULT_NODE_MARGIN);
            }
            if (graphic.getColor() != color) {
                graphic.setColor(color);
            }
            // 更新名称
            RichTextFlow text1 = (RichTextFlow) this.getChild(1);
            if (StringUtil.notEquals(name, text1.getText())) {
                text1.setText(name);
            }
            // 更新高亮
            if (StringUtil.notEquals(highlight, text1.getHighlight())) {
                text1.setHighlight(highlight);
            }
            // 更新额外信息
            if (StringUtil.isNotBlank(extra)) {
                Text text2 = (Text) this.getChild(2);
                // 新增
                if (text2 == null) {
                    Text extraText = new Text(extra);
                    if (extraColor != null) {
                        extraText.setFill(extraColor);
                    }
                    this.addChild(extraText);
                } else {
                    // 更新文本
                    if (StringUtil.notEquals(extra, text2.getText())) {
                        text2.setText(extra);
                    }
                    // 更新颜色
                    if (text2.getFill() != extraColor) {
                        text2.setFill(extraColor);
                    }
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
