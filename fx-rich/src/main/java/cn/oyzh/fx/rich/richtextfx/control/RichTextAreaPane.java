package cn.oyzh.fx.rich.richtextfx.control;

import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.rich.RichTextStyle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.IndexRange;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.reactfx.value.Val;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author oyzh
 * @since 2024/5/17
 */
public abstract class RichTextAreaPane<E extends BaseRichTextArea> extends FXVirtualizedScrollPane<E> implements TipAdapter, FontAdapter, ThemeAdapter {

    public RichTextAreaPane(E content) {
        super(content);
//        this.initTextArea();
    }

    @Override
    public void initNode() {
        super.initNode();
        BaseRichTextArea area = this.getContent();
        area.disableProperty().bind(this.disableProperty());
        area.onKeyPressedProperty().bind(this.onKeyPressedProperty());
        this.initFont();
    }

//    protected void initTextArea() {
//        BaseRichTextArea area = this.getContent();
//        area.disableProperty().bind(this.disableProperty());
//        area.onKeyPressedProperty().bind(this.onKeyPressedProperty());
//    }

    @Override
    public void setFontSize(double fontSize) {
        this.getContent().setFontSize(fontSize);
    }

    @Override
    public double getFontSize() {
        return this.getContent().getFontSize();
    }

    @Override
    public void setFontFamily(String fontFamily) {
        this.getContent().setFontFamily(fontFamily);
    }

    @Override
    public String getFontFamily() {
        return this.getContent().getFontFamily();
    }

    @Override
    public void setFontWeight2(int fontWeight) {
        FontAdapter.super.setFontWeight2(fontWeight);
    }

    @Override
    public void setFontWeight(FontWeight fontWeight) {
        this.getContent().setFontWeight(fontWeight);
    }

    @Override
    public FontWeight getFontWeight() {
        return this.getContent().getFontWeight();
    }

    public void setEditable(boolean editable) {
        this.getContent().setEditable(editable);
    }

    public boolean isEditable() {
        return this.getContent().isEditable();
    }

    public void addTextChangeListener(ChangeListener<String> textChangeListener) {
        this.getContent().addTextChangeListener(textChangeListener);
    }

    public Val<Boolean> undoableProperty() {
        return this.getContent().undoableProperty();
    }

    public Val<Boolean> redoableProperty() {
        return this.getContent().redoableProperty();
    }

    public void undo() {
        this.getContent().undo();
    }

    public void redo() {
        this.getContent().redo();
    }

    public void paste() {
        this.getContent().paste();
    }

    public void clear() {
        this.getContent().clear();
    }

    public String getText() {
        return this.getContent().getText();
    }

    public void setText(String text) {
        this.getContent().setText(text);
    }

    public void forgetHistory() {
        this.getContent().forgetHistory();
    }

    public String getTextTrim() {
        return this.getContent().getTextTrim();
    }

    public void replaceSelection(String replacement) {
        this.getContent().replaceSelection(replacement);
    }

    public void selectRange(int index, int end) {
        this.getContent().selectRange(index, end);
    }

    public void selectRangeAndGoto(int index, int end) {
        this.getContent().selectRange(index, end);
        this.getContent().gotoSelection();
    }

    public ObservableValue<Integer> caretPositionProperty() {
        return this.getContent().caretPositionProperty();
    }

    public void setContextMenu(ContextMenu contextMenu) {
        this.getContent().setContextMenu(contextMenu);
    }

    public void showLineNum() {
        this.getContent().showLineNum();
    }

    public int getCaretPosition() {
        return this.getContent().getCaretPosition();
    }

    public void scrollYTo(double y) {
        super.scrollYBy(y);
        super.scrollYToPixel(y);
    }

    public void scrollXTo(double x) {
        super.scrollXBy(x);
        super.scrollXToPixel(x);
    }

    public void scrollToTop() {
        FXUtil.runPulse(() -> this.scrollYTo(0), 30);
    }

    public void scrollToEnd() {
        FXUtil.runPulse(() -> this.scrollYTo(Double.MAX_VALUE), 30);
    }

    public void replaceText(int start, int end, String text) {
        this.getContent().replaceText(start, end, text);
    }

    public void replaceText(IndexRange range, String text) {
        this.getContent().replaceText(range, text);
    }

    public int getLength() {
        return this.getContent().getLength();
    }

    public void deleteText(int start, int end) {
        this.getContent().deleteText(start, end);
    }

    public void appendLine(String output) {
        this.getContent().appendLine(output);
    }

    public void appendLine(String output, boolean endLine) {
        this.getContent().appendLine(output, endLine);
    }

    public void appendText(String output) {
        this.getContent().appendText(output);
    }

    public void positionCaret(int caretPosition) {
        this.getContent().positionCaret(caretPosition);
    }

    public void cut() {
        this.getContent().cut();
    }

    public void fontSizeIncr() {
        this.getContent().fontSizeIncr();
    }

    public void fontSizeDecr() {
        this.getContent().fontSizeDecr();
    }

    public void initTextStyle() {
        this.getContent().initTextStyle();
    }

    public void clearTextStyle() {
        this.getContent().clearTextStyle();
    }

    public void setStyle(RichTextStyle style) {
        this.getContent().setStyle(style);
    }

    public void setStyles(List<RichTextStyle> style) {
        this.getContent().setStyles(style);
    }

    @Override
    public void requestFocus() {
        this.getContent().requestFocus();
    }

    public String getPromptText() {
        return this.getContent().getPromptText();
    }

    public void setPromptText(String promptText) {
        this.getContent().setPromptText(promptText);
    }

    @Override
    public void changeTheme(ThemeStyle style) {
        this.getContent().changeTheme(style);
    }

    /**
     * 设置内容提示词
     *
     * @param prompts 内容提示词列表
     */
    public void setContentPrompts(Set<String> prompts) {
        this.getContent().setContentPrompts(prompts);
    }

    /**
     * 初始化内容提示词
     */
    public void initContentPrompts() {
        this.getContent().initContentPrompts();
    }

    public Optional<Bounds> getCaretBounds() {
        return this.getContent().getCaretBounds();
    }

    public void gotoSelection() {
        this.getContent().gotoSelection();
    }

    /**
     * 初始化字体
     */
    protected Font initFont() {
        return null;
    }

    @Override
    public void changeFont(Font font) {
        Font font1 = this.initFont();
        if (font1 != null) {
            FontAdapter.super.changeFont(font1);
        } else {
            FontAdapter.super.changeFont(font);
        }
    }

    public void setHighlightText(String highlightText) {
        this.getContent().setHighlightText(highlightText);
    }

    public String getHighlightText() {
        return this.getContent().getHighlightText();
    }

}
