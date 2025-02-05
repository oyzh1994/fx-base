package cn.oyzh.fx.gui.tree.view;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.controls.box.FXHBox;
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
public class RichTreeItemBox extends FXHBox {

    {
        this.disableFont();
        this.setMaxHeight(18);
        this.setMinHeight(18);
        this.setPrefWidth(18);
        this.setPrefWidth(1000);
        this.setPadding(Insets.EMPTY);
    }

    /**
     * 默认图标边距
     */
    private static final Insets DEFAULT_GRAPHIC_MARGIN = new Insets(2, 3, 0, 0);

    /**
     * 默认名称边距
     */
    private static final Insets DEFAULT_NAME_MARGIN = new Insets(2, 1, 0, 0);

    /**
     * 默认扩展边距
     */
    private static final Insets DEFAULT_EXTRA_MARGIN = new Insets(2, 0, 0, 0);

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
            glyph.setId("graphic");
            HBox.setMargin(glyph, DEFAULT_GRAPHIC_MARGIN);
            this.addChild(glyph);

            // 名称
            RichTextFlow nameNode = new RichTextFlow(name, highlight, highlightMatchCase);
            nameNode.initTextFlow();
            nameNode.setId("name");
            HBox.setMargin(nameNode, DEFAULT_NAME_MARGIN);
            this.addChild(nameNode);

            // 额外信息
            if (StringUtil.isNotBlank(extra)) {
                Text extraNode = new Text(extra);
                extraNode.setId("extra");
                if (extraColor != null) {
                    extraNode.setFill(extraColor);
                }
                HBox.setMargin(extraNode, DEFAULT_EXTRA_MARGIN);
                this.addChild(extraNode);
            }
        } else {
            // 更新图标
            SVGGlyph graphicNode = this.getGraphic();
            if (graphicNode != glyph) {
                glyph.setColor(color);
                this.setGraphic(glyph);
                HBox.setMargin(glyph, DEFAULT_GRAPHIC_MARGIN);
            } else if (graphicNode.getColor() != color) {
                graphicNode.setColor(color);
            }

            // 更新名称
            RichTextFlow nameNode = this.getName();
            boolean changed = false;
            if (StringUtil.notEquals(name, nameNode.getText())) {
                nameNode.setText(name);
                changed = true;
            }
            // 更新高亮
            if (StringUtil.notEquals(highlight, nameNode.getHighlight())) {
                nameNode.setHighlight(highlight);
                changed = true;
            }
            // 更新高亮
            if (highlightMatchCase != nameNode.isHighlightMatchCase()) {
                nameNode.setHighlightMatchCase(highlightMatchCase);
                changed = true;
            }
            // 执行初始化
            if (changed) {
                nameNode.initTextFlow();
            }

            // 更新额外信息
            if (StringUtil.isNotBlank(extra)) {
                Text extraNode = this.getExtra();
                // 新增
                if (extraNode == null) {
                    extraNode = new Text(extra);
                    if (extraColor != null) {
                        extraNode.setFill(extraColor);
                    }
                    this.addChild(extraNode);
                } else {
                    // 更新文本
                    if (StringUtil.notEquals(extra, extraNode.getText())) {
                        extraNode.setText(extra);
                    }
                    // 更新颜色
                    if (extraNode.getFill() != extraColor) {
                        extraNode.setFill(extraColor);
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
        node = this.lookup("#graphic");
        if (node instanceof SVGGlyph) {
            return (SVGGlyph) node;
        }
        return null;
    }

    public void setGraphic(SVGGlyph glyph) {
        glyph.setId("graphic");
        if (this.getFirstChild() instanceof SVGGlyph) {
            this.setChild(0, glyph);
        } else {
            this.addChild(0, glyph);
        }
    }

    public RichTextFlow getName() {
        Node node = this.getChild(1);
        if (node instanceof RichTextFlow) {
            return (RichTextFlow) node;
        }
        node = this.lookup("#name");
        if (node instanceof RichTextFlow) {
            return (RichTextFlow) node;
        }
        return null;
    }

    public void setName(RichTextFlow name) {
        name.setId("name");
        if (this.getChild(1) instanceof RichTextFlow) {
            this.setChild(1, name);
        } else {
            this.addChild(1, name);
        }
    }

    public Text getExtra() {
        Node node = this.getChild(2);
        if (node instanceof Text) {
            return (Text) node;
        }
        node = this.lookup("#extra");
        if (node instanceof Text) {
            return (Text) node;
        }
        return null;
    }

    public void setExtra(Text extra) {
        extra.setId("extra");
        if (this.getChild(2) instanceof Text) {
            this.setChild(2, extra);
        } else {
            this.addChild(2, extra);
        }
    }
}
