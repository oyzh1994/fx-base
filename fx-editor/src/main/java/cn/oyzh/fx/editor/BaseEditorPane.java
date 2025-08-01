package cn.oyzh.fx.editor;

import cn.oyzh.fx.plus.controls.pane.FXScrollPane;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.menu.FXContextMenu;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.rich.RichTextStyle;
import cn.oyzh.fx.rich.richtextfx.control.BaseRichTextArea;
import cn.oyzh.fx.rich.richtextfx.control.FXVirtualizedScrollPane;
import cn.oyzh.fx.rich.richtextfx.control.RichTextAreaPane;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.IndexRange;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.reactfx.value.Val;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author oyzh
 * @since 2025/07/30
 */
public class BaseEditorPane extends FXScrollPane implements NodeGroup, FontAdapter {

    public BaseEditorPane() {
        super(new Editor());
    }


    public Editor getEditor() {
        return (Editor) super.getContent();
    }

    public void showData(Object data) {
        this.getEditor().showData(data);
    }

    public EditorFormatType getFormatType() {
        return this.getEditor().getFormatType();
    }

    public BooleanProperty editableProperty() {
        return this.getEditor().editableProperty();
    }

    public boolean isEmpty() {
        return this.getEditor().isEmpty();
    }

    public void setLineNumPolicy(EditorLineNumPolicy lineNumPolicy) {
        this.getEditor().setLineNumPolicy(lineNumPolicy);
    }

    public StringProperty highlightTextProperty() {
        return this.getEditor().highlightTextProperty();
    }

    public ObjectProperty<EditorFormatType> formatTypeProperty() {
        return this.getEditor().formatTypeProperty();
    }

    public void forgetHistory() {
        this.getEditor().forgetHistory();
    }

    public void undo() {
        this.getEditor().undo();
    }

    public void redo() {
        this.getEditor().redo();
    }

    @Override
    public void initNode() {
        super.initNode();
        // 覆盖默认的菜单
        this.setContextMenu(FXContextMenu.EMPTY);
        BaseRichTextArea area = this.getEditor();
        area.disableProperty().bind(this.disableProperty());
        area.onKeyPressedProperty().bind(this.onKeyPressedProperty());
        area.prefWidthProperty().bind(this.widthProperty());
        area.prefHeightProperty().bind(this.widthProperty());
        this.initFont();
    }

    @Override
    public void setFontSize(double fontSize) {
        this.getEditor().setFontSize(fontSize);
    }

    @Override
    public double getFontSize() {
        return this.getEditor().getFontSize();
    }

    @Override
    public void setFontFamily(String fontFamily) {
        this.getEditor().setFontFamily(fontFamily);
    }

    @Override
    public String getFontFamily() {
        return this.getEditor().getFontFamily();
    }

    @Override
    public void setFontWeight2(int fontWeight) {
        FontAdapter.super.setFontWeight2(fontWeight);
    }

    @Override
    public void setFontWeight(FontWeight fontWeight) {
        this.getEditor().setFontWeight(fontWeight);
    }

    @Override
    public FontWeight getFontWeight() {
        return this.getEditor().getFontWeight();
    }

    public void setEditable(boolean editable) {
        this.getEditor().setEditable(editable);
    }

    public boolean isEditable() {
        return this.getEditor().isEditable();
    }

    public void addTextChangeListener(ChangeListener<String> textChangeListener) {
        this.getEditor().addTextChangeListener(textChangeListener);
    }

    public Val<Boolean> undoableProperty() {
        return this.getEditor().undoableProperty();
    }

    public Val<Boolean> redoableProperty() {
        return this.getEditor().redoableProperty();
    }

    public void paste() {
        this.getEditor().paste();
    }

    public void clear() {
        this.getEditor().clear();
    }

    public String getText() {
        return this.getEditor().getText();
    }

    public void setText(String text) {
        this.getEditor().setText(text);
    }

    public String getTextTrim() {
        return this.getEditor().getTextTrim();
    }

    public void replaceSelection(String replacement) {
        this.getEditor().replaceSelection(replacement);
    }

    public void selectRange(int index, int end) {
        this.getEditor().selectRange(index, end);
    }

    public void selectRangeAndGoto(int index, int end) {
        this.getEditor().selectRange(index, end);
        this.getEditor().gotoSelection();
    }

    public ObservableValue<Integer> caretPositionProperty() {
        return this.getEditor().caretPositionProperty();
    }

    public void showLineNum() {
        this.getEditor().showLineNum();
    }

    public int getCaretPosition() {
        return this.getEditor().getCaretPosition();
    }


    public void scrollToTop() {
    }

    public void scrollToEnd() {
    }

    public void replaceText(int start, int end, String text) {
        this.getEditor().replaceText(start, end, text);
    }

    public void replaceText(IndexRange range, String text) {
        this.getEditor().replaceText(range, text);
    }

    public int getLength() {
        return this.getEditor().getLength();
    }

    public void deleteText(int start, int end) {
        this.getEditor().deleteText(start, end);
    }

    public void appendLine(String output) {
        this.getEditor().appendLine(output);
    }

    public void appendLine(String output, boolean endLine) {
        this.getEditor().appendLine(output, endLine);
    }

    public void appendText(String output) {
        this.getEditor().appendText(output);
    }

    public void positionCaret(int caretPosition) {
        this.getEditor().positionCaret(caretPosition);
    }

    public void cut() {
        this.getEditor().cut();
    }

    public void fontSizeIncr() {
        this.getEditor().fontSizeIncr();
    }

    public void fontSizeDecr() {
        this.getEditor().fontSizeDecr();
    }

    public void initTextStyle() {
        this.getEditor().initTextStyle();
    }

    public void clearTextStyle() {
        this.getEditor().clearTextStyle();
    }

    public void setStyle(RichTextStyle style) {
        this.getEditor().setStyle(style);
    }

    public void setStyles(List<RichTextStyle> style) {
        this.getEditor().setStyles(style);
    }

    @Override
    public void requestFocus() {
        this.getEditor().requestFocus();
    }

    public String getPromptText() {
        return this.getEditor().getPromptText();
    }

    public void setPromptText(String promptText) {
        this.getEditor().setPromptText(promptText);
    }

    @Override
    public void changeTheme(ThemeStyle style) {
        this.getEditor().changeTheme(style);
    }

    /**
     * 设置提示词
     *
     * @param prompts 提示词列表
     */
    public void setPrompts(Set<String> prompts) {
        this.getEditor().setPrompts(prompts);
    }

    /**
     * 初始化提示词
     */
    public void initPrompts() {
    }

    public Optional<Bounds> getCaretBounds() {
        return this.getEditor().getCaretBounds();
    }

    public void gotoSelection() {
        this.getEditor().gotoSelection();
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
        this.getEditor().setHighlightText(highlightText);
    }

    public String getHighlightText() {
        return this.getEditor().getHighlightText();
    }

}
