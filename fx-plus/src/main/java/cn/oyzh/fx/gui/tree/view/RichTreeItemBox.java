package cn.oyzh.fx.gui.tree.view;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.controls.pane.FXPane;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.rich.RichTextFlow;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.util.List;

/**
 * 富功能树节点内容盒，展示图标、名称与额外信息
 *
 * @author oyzh
 * @since 2025-01-22
 */
public class RichTreeItemBox extends FXPane {

    /**
     * 构造富功能树节点内容盒
     */
    public RichTreeItemBox() {
        super();
    }

    /**
     * 构造富功能树节点内容盒
     *
     * @param value             节点值
     * @param highlight         高亮文本
     * @param highlightMatchCase 高亮是否匹配大小写
     */
    public RichTreeItemBox(RichTreeItemValue value, String highlight, boolean highlightMatchCase) {
        super();
        this.init(value, highlight, highlightMatchCase);
    }

    /**
     * 初始化内容盒，构建或更新图标、名称与额外信息节点。
     *
     * @param value             节点值
     * @param highlight         高亮文本
     * @param highlightMatchCase 高亮是否匹配大小写
     */
    public void init(RichTreeItemValue value, String highlight, boolean highlightMatchCase) {
        String name = value.name();
        String extra = value.extra();
        SVGGlyph glyph = value.graphic();
        Color color = value.graphicColor();
        Color extraColor = value.extraColor();
        List<Color> tagColors = value.tagColors();
        if (this.isChildEmpty()) {
            // 图标
            glyph.setColor(color);
            glyph.setId("graphic");
            this.addChild(glyph);

            // 名称
            RichTextFlow nameNode = new RichTextFlow(name, highlight, highlightMatchCase);
            nameNode.initTextFlow();
            nameNode.setId("name");
            this.addChild(nameNode);

            // 标签与额外信息
            this.updateTrailing(tagColors, extra, extraColor);
        } else {
            // 更新图标
            SVGGlyph graphicNode = this.getGraphic();
            if (graphicNode != glyph) {
                glyph.setColor(color);
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

            // 更新标签与额外信息
            this.updateTrailing(tagColors, extra, extraColor);
        }
    }

    private void updateTrailing(List<Color> colors, String extra, Color extraColor) {
        HBox trailing = this.getTrailing();
        List<Color> values = colors == null ? List.of() : colors.stream()
                .filter(color -> color != null)
                .toList();
        double height = this.contentHeight();
        this.setMinHeight(height);
        this.setPrefHeight(height);
        this.setMaxHeight(height);
        if (values.isEmpty() && StringUtil.isBlank(extra)) {
            if (trailing != null) {
                this.removeChild(trailing);
            }
            return;
        }
        if (trailing == null) {
            trailing = new HBox(4);
            trailing.setId("trailing");
            trailing.setAlignment(Pos.CENTER_LEFT);
            trailing.setMouseTransparent(true);
            trailing.setMinWidth(Region.USE_PREF_SIZE);
            trailing.setPrefWidth(Region.USE_PREF_SIZE);
            trailing.setMaxWidth(Region.USE_PREF_SIZE);
            this.addChild(2, trailing);
        }
        trailing.setMinHeight(height);
        trailing.setPrefHeight(height);
        trailing.setMaxHeight(height);
        List<Node> children = new java.util.ArrayList<>();
        if (!values.isEmpty()) {
            double spacing = 6;
            double diameter = 8.5;
            Pane tagStrip = new Pane();
            tagStrip.setId("tags");
            tagStrip.setMouseTransparent(true);
            double width = diameter + spacing * (values.size() - 1);
            tagStrip.setMinWidth(width);
            tagStrip.setPrefWidth(width);
            tagStrip.setMaxWidth(width);
            tagStrip.setMinHeight(diameter);
            tagStrip.setPrefHeight(diameter);
            tagStrip.setMaxHeight(diameter);

            List<Circle> tags = new java.util.ArrayList<>();
            for (int i = 0; i < values.size(); i++) {
                Circle circle = new Circle(4.25, values.get(i));
                circle.setStroke(Color.color(0, 0, 0, 0.08));
                circle.setLayoutX(i * spacing + 4.25);
                circle.setLayoutY(4.25);
                tags.add(circle);
            }
            // 反向加入渲染层，让第一个标签位于最上层。
            for (int i = tags.size() - 1; i >= 0; i--) {
                tagStrip.getChildren().add(tags.get(i));
            }
            children.add(tagStrip);
        }
        if (StringUtil.isNotBlank(extra)) {
            Text extraNode = new Text(extra);
            extraNode.setId("extra");
            if (extraColor != null) {
                extraNode.setFill(extraColor);
            }
            children.add(extraNode);
        }
        trailing.getChildren().setAll(children);
    }

    private double contentHeight() {
        double height = 16;
        RichTextFlow nameNode = this.getName();
        if (nameNode != null) {
            height = Math.max(height, nameNode.prefHeight(-1));
        }
        SVGGlyph graphicNode = this.getGraphic();
        if (graphicNode != null) {
            height = Math.max(height, graphicNode.prefHeight(-1) + 2);
        }
        return Math.ceil(height);
    }

    /**
     * 获取图标节点
     *
     * @return 图标节点
     */
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

    /**
     * 设置图标节点
     *
     * @param glyph 图标
     */
    public void setGraphic(SVGGlyph glyph) {
        glyph.setId("graphic");
        if (this.getFirstChild() instanceof SVGGlyph) {
            this.setChild(0, glyph);
        } else {
            this.addChild(0, glyph);
        }
    }

    /**
     * 获取名称节点
     *
     * @return 名称节点
     */
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

    /**
     * 设置名称节点
     *
     * @param name 名称节点
     */
    public void setName(RichTextFlow name) {
        name.setId("name");
        if (this.getChild(1) instanceof RichTextFlow) {
            this.setChild(1, name);
        } else {
            this.addChild(1, name);
        }
    }

    /**
     * 获取额外信息节点
     *
     * @return 额外信息节点
     */
    public Text getExtra() {
        Node node = this.lookup("#extra");
        if (node instanceof Text text) {
            return text;
        }
        return null;
    }

    /**
     * 获取标签节点
     */
    public HBox getTrailing() {
        Node node = this.lookup("#trailing");
        return node instanceof HBox trailing ? trailing : null;
    }

    /**
     * 设置额外信息节点
     *
     * @param extra 额外信息文本
     */
    public void setExtra(Text extra) {
        extra.setId("extra");
        Text current = this.getExtra();
        HBox trailing = this.getTrailing();
        if (current != null && trailing != null) {
            trailing.getChildren().set(trailing.getChildren().indexOf(current), extra);
        } else {
            this.addChild(extra);
        }
    }

    @Override
    protected void layoutChildren() {
        double x = 0;
        double centerY = 0;
        for (Node child : this.getChildren()) {
            child.autosize();
            child.setLayoutX(x);
            if ("graphic".equals(child.getId())) {
                x += NodeUtil.getWidth(child) + 4;
                child.setLayoutY(2);
            } else if ("name".equals(child.getId())) {
                centerY = NodeUtil.getHeight(child) / 2;
                child.setLayoutY(0);
                x += NodeUtil.getWidth(child) + 7;
            } else if ("trailing".equals(child.getId())) {
                child.setLayoutY(Math.max(0, centerY - NodeUtil.getHeight(child) / 2));
                x += NodeUtil.getWidth(child);
            }
        }
        this.setPrefWidth(x + 30);
    }

    @Override
    public void initNode() {
        this.setPadding(Insets.EMPTY);
        super.initNode();
    }
}
