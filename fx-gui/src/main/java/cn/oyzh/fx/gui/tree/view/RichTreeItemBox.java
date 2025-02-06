package cn.oyzh.fx.gui.tree.view;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.controls.pane.FXPane;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.rich.RichTextFlow;
import cn.oyzh.fx.plus.util.NodeUtil;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * @author oyzh
 * @since 2025-01-22
 */
public class RichTreeItemBox extends FXPane {

    {
        this.disableFont();
        this.setMaxHeight(15);
        this.setMinHeight(15);
        this.setPrefWidth(15);
//        this.setPrefWidth(1000);
        this.setPadding(Insets.EMPTY);
    }
//
//    /**
//     * 默认图标边距
//     */
//    private static final Insets DEFAULT_GRAPHIC_MARGIN = new Insets(0, 3, 0, 0);
//
//    /**
//     * 默认名称边距
//     */
//    private static final Insets DEFAULT_NAME_MARGIN = new Insets(0, 3, 0, 0);
//
//    /**
//     * 默认扩展边距
//     */
//    private static final Insets DEFAULT_EXTRA_MARGIN = new Insets(0, 0, 0, 0);

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
//            glyph.setLayoutY(DEFAULT_GRAPHIC_MARGIN.getTop());
            this.addChild(glyph);

            // 名称
            RichTextFlow nameNode = new RichTextFlow(name, highlight, highlightMatchCase);
            nameNode.initTextFlow();
            nameNode.setId("name");
//            nameNode.setLayoutY(DEFAULT_NAME_MARGIN.getTop());
            this.addChild(nameNode);

            // 额外信息
            if (StringUtil.isNotBlank(extra)) {
                Label extraNode = new Label(extra);
                extraNode.setId("extra");
                if (extraColor != null) {
                    extraNode.setTextFill(extraColor);
                }
//                extraNode.setLayoutY(DEFAULT_EXTRA_MARGIN.getTop());
                this.addChild(extraNode);
            }
        } else {
            // 更新图标
            SVGGlyph graphicNode = this.getGraphic();
            if (graphicNode != glyph) {
                glyph.setColor(color);
//                glyph.setLayoutY(DEFAULT_GRAPHIC_MARGIN.getTop());
                this.setGraphic(glyph);
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
                Label extraNode = this.getExtra();
                // 新增
                if (extraNode == null) {
                    extraNode = new Label(extra);
                    if (extraColor != null) {
                        extraNode.setTextFill(extraColor);
                    }
                    this.addChild(extraNode);
                } else {
                    // 更新文本
                    if (StringUtil.notEquals(extra, extraNode.getText())) {
                        extraNode.setText(extra);
                    }
                    // 更新颜色
                    if (extraNode.getTextFill() != extraColor) {
                        extraNode.setTextFill(extraColor);
                    }
                }
            } else {
                this.removeChild(2);
            }
        }
//        this.layoutChildren();
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

    public Label getExtra() {
        Node node = this.getChild(2);
        if (node instanceof Label) {
            return (Label) node;
        }
        node = this.lookup("#extra");
        if (node instanceof Label) {
            return (Label) node;
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

    @Override
    protected void layoutChildren() {
//        super.layoutChildren();
        double x = 0;
        for (Node child : this.getChildren()) {
            child.autosize();
            child.setLayoutX(x);
            if ("graphic".equals(child.getId())) {
                x += NodeUtil.getWidth(child) + 3;
//                child.setLayoutY(DEFAULT_GRAPHIC_MARGIN.getTop());
            } else if ("name".equals(child.getId())) {
                x += NodeUtil.getWidth(child) + 3;
//                child.setLayoutY(DEFAULT_NAME_MARGIN.getTop());
//            } else if ("extra".equals(child.getId())) {
//                x += NodeUtil.getWidth(child) + DEFAULT_EXTRA_MARGIN.getRight();
//                child.setLayoutY(DEFAULT_EXTRA_MARGIN.getTop());
            }
        }
        this.setPrefWidth(x + 30);
    }

//    @Override
//    public void resize(double width, double height) {
//        super.resize(width, height);
//        this.layoutChildren();
//    }
//
//    @Override
//    public void requestLayout() {
//        super.requestLayout();
//        this.layoutChildren();
//    }
}
