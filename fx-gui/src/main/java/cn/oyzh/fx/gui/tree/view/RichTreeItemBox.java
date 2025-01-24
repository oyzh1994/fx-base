package cn.oyzh.fx.gui.tree.view;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.controls.box.FlexHBox;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.rich.RichTextFlow;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * @author oyzh
 * @since 2025-01-22
 */
public class RichTreeItemBox extends FlexHBox {

    {
        this.disableFont();
        this.setPrefWidth(1000);
        this.setPadding(Insets.EMPTY);
    }

    /**
     * 默认节点编剧
     */
    private static final Insets DEFAULT_NODE_MARGIN = new Insets(1, 3, 0, 0);

    public RichTreeItemBox() {
        super();
    }

    public RichTreeItemBox(RichTreeItemValue value, String highlight, boolean highlightMatchCase) {
        super();
        this.init(value, highlight, highlightMatchCase);
    }

    public void init(RichTreeItemValue value, String highlight, boolean highlightMatchCase) {
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
            RichTextFlow nameText = new RichTextFlow(name, highlight, highlightMatchCase);
            nameText.initTextFlow();
            this.addChild(nameText);
            // 额外信息
            if (StringUtil.isNotBlank(extra)) {
                Text extraText = new Text(extra);
                if (extraColor != null) {
                    extraText.setFill(extraColor);
                }
                this.addChild(extraText);
            }
            HBox.setMargin(glyph, DEFAULT_NODE_MARGIN);
            HBox.setMargin(nameText, DEFAULT_NODE_MARGIN);
        } else {
            // 更新图标
            SVGGlyph graphic = this.getGraphic();
            if (graphic != glyph) {
                glyph.setColor(color);
                this.setGraphic(glyph);
                HBox.setMargin(glyph, DEFAULT_NODE_MARGIN);
            } else if (graphic.getColor() != color) {
                graphic.setColor(color);
            }
            // 更新名称
            RichTextFlow nameText = (RichTextFlow) this.getChild(1);
            boolean changed = false;
            if (StringUtil.notEquals(name, nameText.getText())) {
                nameText.setText(name);
                changed = true;
            }
            // 更新高亮
            if (StringUtil.notEquals(highlight, nameText.getHighlight())) {
                nameText.setHighlight(highlight);
                changed = true;
            }
            // 更新高亮
            if (highlightMatchCase != nameText.isHighlightMatchCase()) {
                nameText.setHighlightMatchCase(highlightMatchCase);
                changed = true;
            }
            // 执行初始化
            if (changed) {
                nameText.initTextFlow();
            }
            // 更新额外信息
            if (StringUtil.isNotBlank(extra)) {
                Text extraText = (Text) this.getChild(2);
                // 新增
                if (extraText == null) {
                    extraText = new Text(extra);
                    if (extraColor != null) {
                        extraText.setFill(extraColor);
                    }
                    this.addChild(extraText);
                } else {
                    // 更新文本
                    if (StringUtil.notEquals(extra, extraText.getText())) {
                        extraText.setText(extra);
                    }
                    // 更新颜色
                    if (extraText.getFill() != extraColor) {
                        extraText.setFill(extraColor);
                    }
                }
            } else {
                this.removeChild(2);
            }
        }
    }

    public SVGGlyph getGraphic() {
        Node node = this.getFirstChild();
        if (node instanceof SVGGlyph) {
            return (SVGGlyph) node;
        }
        return null;
    }

    public void setGraphic(SVGGlyph glyph) {
        if (this.getChild(0) instanceof SVGGlyph) {
            this.setChild(0, glyph);
        } else {
            this.addChild(0, glyph);
        }
    }
}
