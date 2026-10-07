package cn.oyzh.fx.plus.rich;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.adapter.PropAdapter;
import cn.oyzh.fx.plus.controls.text.FXText;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.font.FontUtil;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 富文本流式布局控件，支持按关键字高亮显示文本
 *
 * @author oyzh
 * @since 2025/01/22
 */
public class RichTextFlow extends TextFlow implements PropAdapter, FlexAdapter, ThemeAdapter, FontAdapter {

    {
        NodeManager.init(this);
    }

    /**
     * 构造富文本流对象。
     */
    public RichTextFlow() {
        super();
    }

    /**
     * 构造富文本流控件，并设置文本内容
     *
     * @param text 文本内容
     */
    public RichTextFlow(String text) {
        super();
        this.setText(text);
    }

    /**
     * 构造富文本流控件，并设置文本内容、高亮关键字及是否区分大小写
     *
     * @param text               文本内容
     * @param highlight          高亮关键字
     * @param highlightMatchCase 高亮是否区分大小写
     */
    public RichTextFlow(String text, String highlight, boolean highlightMatchCase) {
        super();
        this.setText(text);
        this.setHighlight(highlight);
        this.setHighlightMatchCase(highlightMatchCase);
    }

    /**
     * 设置文本内容
     *
     * @param text 文本内容
     */
    public void setText(String text) {
        this.setProp("_text", text);
    }

    /**
     * 获取文本内容
     *
     * @return 文本内容
     */
    public String getText() {
        return this.getProp("_text");
    }

    /**
     * 根据当前文本内容初始化文本流
     */
    public void initTextFlow() {
        this.initTextFlow(this.getText());
    }

    /**
     * 根据文本内容初始化文本流，命中高亮关键字的部分单独着色
     *
     * @param text 文本内容
     */
    protected void initTextFlow(String text) {
        String highlight = this.getHighlight();
        if (StringUtil.isNotBlank(highlight)) {
            List<FXText> texts = new ArrayList<>();
            if (StringUtil.containsIgnoreCase(text, highlight)) {
                String[] arr;
                if (this.isHighlightMatchCase()) {
                    arr = text.splitWithDelimiters(highlight, -1);
                } else {
                    arr = text.splitWithDelimiters("(?i)" + highlight, -1);
                }
                Color highlightColor = this.getHighlightColor();
                for (String s : arr) {
                    FXText text1 = new FXText(s);
                    texts.add(text1);
                    if ((this.isHighlightMatchCase() && s.equals(highlight)) || s.equalsIgnoreCase(highlight)) {
                        text1.setFill(highlightColor);
                    }
                }
            } else {
                texts.add(new FXText(text));
            }
            this.initTextFlow(texts);
        } else if (StringUtil.isNotBlank(text)) {
            this.initTextFlow(new FXText(text));
        } else {
            this.initTextFlow(Collections.emptyList());
        }
    }

    /**
     * 使用文本节点初始化文本流
     *
     * @param texts 文本节点
     */
    protected void initTextFlow(FXText... texts) {
        this.getChildren().setAll(texts);
    }

    /**
     * 使用文本节点列表初始化文本流
     *
     * @param texts 文本节点列表
     */
    protected void initTextFlow(List<FXText> texts) {
        this.getChildren().setAll(texts);
    }

    /**
     * 设置高亮关键字
     *
     * @param highlight 高亮关键字
     */
    public void setHighlight(String highlight) {
        this.setProp("_highlight", highlight);
    }

    /**
     * 获取高亮关键字
     *
     * @return 高亮关键字
     */
    public String getHighlight() {
        return this.getProp("_highlight");
    }

    /**
     * 设置高亮是否区分大小写
     *
     * @param highlightMatchCase 高亮是否区分大小写
     */
    public void setHighlightMatchCase(boolean highlightMatchCase) {
        this.setProp("_highlightMatchCase", highlightMatchCase);
    }

    /**
     * 判断高亮是否区分大小写
     *
     * @return 高亮是否区分大小写
     */
    public boolean isHighlightMatchCase() {
        Object obj = this.getProp("_highlightMatchCase");
        return obj != null && Boolean.parseBoolean(obj.toString());
    }

    /**
     * 设置高亮颜色
     *
     * @param color 高亮颜色
     */
    public void setHighlightColor(Color color) {
        this.setProp("_highlightColor", color);
    }

    /**
     * 获取高亮颜色，未设置时返回默认色
     *
     * @return 高亮颜色
     */
    public Color getHighlightColor() {
        Color color = this.getProp("_highlightColor");
        if (color == null) {
            color = Color.valueOf("#FF6600");
        }
        return color;
    }

    /**
     * 当前字体
     */
    private Font font;

    @Override
    public void setFont(Font font) {
        this.font = font;
        List<Node> managed = this.getManagedChildren();
        for (Node node : managed) {
            FontUtil.setFont(node, font);
        }
    }

    @Override
    public Font getFont() {
        return this.font;
    }

    @Override
    public void setFontSize(double fontSize) {
        this.setFont(FontUtil.newFontBySize(this.getFont(), fontSize));
    }

    @Override
    public void setFontFamily(String fontFamily) {
        this.setFont(FontUtil.newFontByFamily(this.getFont(), fontFamily));
    }

    @Override
    public void setFontWeight(FontWeight fontWeight) {
        this.setFont(FontUtil.newFontByWeight(this.getFont(), fontWeight));
    }
}
