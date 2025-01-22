package cn.oyzh.fx.plus.rich;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.adapter.PropAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author oyzh
 * @since 2025/01/22
 */
public class RichTextFlow extends TextFlow implements PropAdapter, FlexAdapter, ThemeAdapter {

    {
        NodeManager.init(this);
    }

    public RichTextFlow() {
        super();
    }

    public RichTextFlow(String text) {
        super();
        this.setText(text);
    }

    public RichTextFlow(String text, String highlight) {
        super();
        this.setProp("_text", text);
        this.setHighlight(highlight);
    }

    public void setText(String text) {
        this.setProp("_text", text);
        this.initTextFlow();
    }

    public String getText() {
        return this.getProp("_text");
    }

    protected void initTextFlow() {
        this.initTextFlow(this.getText());
    }

    protected void initTextFlow(String text) {
        String highlight = this.getHighlight();
        if (StringUtil.isNotBlank(highlight)) {
            List<Text> texts = new ArrayList<>();
            if (text.contains(highlight)) {
                String[] arr;
                if (this.isHighlightMatchCase()) {
                    arr = text.splitWithDelimiters(highlight, -1);
                } else {
                    arr = text.splitWithDelimiters("(?i)" + highlight, -1);
                }
                Color highlightColor = this.getHighlightColor();
                for (String s : arr) {
                    Text text1 = new Text(s);
                    texts.add(text1);
                    if (s.equals(highlight)) {
                        text1.setFill(highlightColor);
                    }
                }
            } else {
                texts.add(new Text(text));
            }
            this.initTextFlow(texts);
        } else if (StringUtil.isNotBlank(text)) {
            this.initTextFlow(new Text(text));
        } else {
            this.initTextFlow(Collections.emptyList());
        }
    }

    protected void initTextFlow(Text... texts) {
        this.getChildren().setAll(texts);
    }

    protected void initTextFlow(List<Text> texts) {
        this.getChildren().setAll(texts);
    }

    public void setHighlight(String highlight) {
        this.setProp("_highlight", highlight);
        this.initTextFlow();
    }

    public String getHighlight() {
        return this.getProp("_highlight");
    }

    public void setHighlightMatchCase(boolean highlightMatchCase) {
        this.setProp("_highlightMatchCase", highlightMatchCase);
    }

    public boolean isHighlightMatchCase() {
        Object obj = this.getProp("_highlightMatchCase");
        return obj != null && Boolean.parseBoolean(obj.toString());
    }

    public void setHighlightColor(Color color) {
        this.setProp("_highlightColor", color);
    }

    public Color getHighlightColor() {
        Color color = this.getProp("_highlightColor");
        if (color == null) {
            color = Color.ORANGERED;
        }
        return color;
    }
}
