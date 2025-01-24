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
import javafx.scene.text.FontWeight;
import lombok.NonNull;
import org.reactfx.value.Val;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author oyzh
 * @since 2024/5/17
 */
public abstract class RichTextAreaPane<E extends FlexRichTextArea> extends FlexVirtualizedScrollPane<E> implements TipAdapter, FontAdapter, ThemeAdapter {

    public RichTextAreaPane(E content) {
        super(content);
        this.initTextArea();
    }

    protected void initTextArea() {
        FlexRichTextArea area = this.getContent();
        area.disableProperty().bind(this.disableProperty());
        area.onKeyPressedProperty().bind(this.onKeyPressedProperty());
    }

    @Override
    public void setFontSize(double fontSize) {
        this.getContent().setFontSize(fontSize);
    }

    @Override
    public double getFontSize() {
        return this.getContent().getFontSize();
    }

    @Override
    public void setFontFamily(@NonNull String fontFamily) {
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

    public void addTextChangeListener(ChangeListener<String> dataListener) {
        this.getContent().addTextChangeListener(dataListener);
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

    public void scrollToEnd() {
        FXUtil.runPulse(() -> this.scrollYBy(Double.MAX_VALUE), 30);
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
//        if (this.contentPrompts != null) {
//            String text = this.getText();
//            Matcher matcher1 = this.contentPrompts.matcher(text);
//            List<RichTextStyle> styles = new ArrayList<>();
//            while (matcher1.find()) {
//                styles.add(new RichTextStyle(matcher1.start(), matcher1.end(), "-fx-fill: #008B45;"));
//            }
//            this.setStyles(styles);
//            this.forgetHistory();
//        }
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

//    /**
//     * 基础内容正则模式
//     */
//    private Pattern contentPrompts;

    /**
     * 设置内容提示词
     *
     * @param prompts 内容提示词列表
     */
    public void setContentPrompts(Set<String> prompts) {
//        if (prompts == null || prompts.isEmpty()) {
//            this.contentPrompts = null;
//        } else {
//            StringBuilder regex = new StringBuilder("\\b(");
//            for (String s : prompts) {
//                regex.append(s).append("|");
//            }
//            regex.append(")\\b");
//            this.contentPrompts = Pattern.compile(regex.toString().replaceFirst("\\|\\)", ")"), Pattern.CASE_INSENSITIVE);
//        }
//        this.initTextStyle();
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
}
