package cn.oyzh.fx.rich.incubator;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.adapter.AreaAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TextAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.i18n.I18nAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.rich.RichTextStyle;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import jfx.incubator.scene.control.richtext.TextPos;
import jfx.incubator.scene.control.richtext.model.StyleAttributeMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseRichTextArea extends FXRichTextArea implements FlexAdapter, AreaAdapter, I18nAdapter, NodeAdapter, ThemeAdapter, FontAdapter, TextAdapter, TipAdapter, StateAdapter {

    {
        NodeManager.init(this);
        super.getModel().addListener(ch -> {
            this.textProperty().setValue(this.getText());
        });
    }

    /**
     * 基础内容正则模式
     */
    protected Pattern contentPrompts;

    /**
     * 高亮文本
     */
    private String highlightText;

    public String getHighlightText() {
        return highlightText;
    }

    /**
     * 设置高亮文本
     *
     * @param highlightText 高亮文本
     */
    public void setHighlightText(String highlightText) {
        this.highlightText = highlightText;
        this.initTextStyle();
    }

    /**
     * 高亮正则模式
     *
     * @return 高亮正则模式
     */
    protected Pattern highlightPattern() {
        return Pattern.compile(this.highlightText, Pattern.CASE_INSENSITIVE);
    }

    private TextPos[] getPosByIndex(int start, int end) {
        int length = 0;
        int endIndex = -1;
        int startIndex = -1;
        int pCount = super.getParagraphCount();
        for (int i = 0; i < pCount; i++) {
            int len = super.getModel().getParagraphLength(i);
            length += len;
            if (startIndex == -1 && length >= start) {
                startIndex = i;
            }
            if (length >= end) {
                endIndex = i;
                break;
            }
        }
        TextPos endPos = TextPos.ofLeading(endIndex, end);
        TextPos startPos = TextPos.ofLeading(startIndex, start);
        return new TextPos[]{startPos, endPos};
    }

    public void setStyle(int start, int end, Color color) {
        int length = 0;
        int endIndex = -1;
        int startIndex = -1;
        int pCount = super.getParagraphCount();
        for (int i = 0; i < pCount; i++) {
            int len = super.getModel().getParagraphLength(i);
            length += len;
            if (startIndex == -1 && length >= start) {
                startIndex = i;
            }
            if (length >= end) {
                endIndex = i;
                break;
            }
        }
        TextPos endPos = TextPos.ofLeading(endIndex, end);
        TextPos startPos = TextPos.ofLeading(startIndex, start);
        StyleAttributeMap attributeMap = StyleAttributeMap.of(StyleAttributeMap.TEXT_COLOR, color);
        super.setStyle(startPos, endPos, attributeMap);
    }

    @Override
    public void flushCaret() {
        this.requestFocus();
    }

    @Override
    public void changeLocale(Locale locale) {

    }

    public int getLength() {
        String text = super.getText();
        if (text == null) {
            return 0;
        }
        return text.length();
    }

    /**
     * 清除文字样式
     */
    public void clearTextStyle() {
        FXUtil.runWait(() -> this.setStyle(0, this.getLength(), null));
    }

    public void replaceText(int start, int end, String text) {
        TextPos[] pos = this.getPosByIndex(start, end);
        this.replaceText(pos[0], pos[1], text, true);
    }

    /**
     * 初始化文字样式
     */
    public void initTextStyle() {
        try {
            this.clearTextStyle();
            // 初始化颜色
            if (this.isEnableTheme()) {
//                Node placeholder = this.getPlaceholder();
//                CaretNode caretNode = this.getCaretSelectionBind().getUnderlyingCaret();
//                Color accentColor = ThemeManager.currentAccentColor();
//                Color foregroundColor = ThemeManager.currentForegroundColor();
//                Color backgroundColor = ThemeManager.currentBackgroundColor();
//                String fgColor = FXColorUtil.getColorHex(foregroundColor);
//                FXUtil.runWait(() -> {
//                    // 设置光标颜色
//                    this.setStyle(0, this.getLength(), "-fx-fill:" + fgColor);
//                    caretNode.setStroke(accentColor);
//                    // 设置背景文字颜色
//                    if (placeholder != null) {
//                        placeholder.setStyle("-fx-fill:" + fgColor);
//                    }
//                    // 设置背景色
//                    this.setBackground(ControlUtil.background(backgroundColor));
//                });
            }
            // 高亮
            if (StringUtil.isNotBlank(this.highlightText)) {
                String text = this.getText();
                Matcher matcher = this.highlightPattern().matcher(text);
                List<RichTextStyle> styles = new ArrayList<>();
                while (matcher.find()) {
                    styles.add(new RichTextStyle(matcher.start(), matcher.end(), Color.valueOf("#FF6600")));
                }
                this.setStyles(styles);
            }
            // 内容提示
            if (this.contentPrompts != null) {
                String text = this.getText();
                Matcher matcher1 = this.contentPrompts.matcher(text);
                List<RichTextStyle> styles = new ArrayList<>();
                while (matcher1.find()) {
                    styles.add(new RichTextStyle(matcher1.start(), matcher1.end(), Color.valueOf("#008B45")));
                }
                this.setStyles(styles);
                this.forgetHistory();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 设置样式
     *
     * @param styles 富文本样式
     */
    public void setStyles(List<RichTextStyle> styles) {
        if (CollectionUtil.isNotEmpty(styles)) {
            FXUtil.runWait(() -> {
                for (RichTextStyle style : styles) {
                    this.setStyle(style.start(), style.end(), style.color());
                }
            });
        }
    }

    public void forgetHistory() {
        this.clearUndoRedo();

    }

    private String promptText;

    protected void setPromptText(String promptText) {
        this.promptText = promptText;
    }

    public String getPromptText() {
        return promptText;
    }

    protected boolean isEmpty() {
        return StringUtil.isEmpty(getText());
    }

    protected void hideLineNum() {
        this.setLineNumbersEnabled(false);
    }

    protected void showLineNum() {
        this.setLineNumbersEnabled(true);
    }

    private SimpleStringProperty textProperty = new SimpleStringProperty();

    protected SimpleStringProperty textProperty() {
        return textProperty;
    }

    /**
     * 初始化字体
     */
    protected Font initFont() {
        return null;
    }

}
