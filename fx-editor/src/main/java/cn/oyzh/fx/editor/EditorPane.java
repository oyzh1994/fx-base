package cn.oyzh.fx.editor;

import cn.oyzh.fx.plus.controls.swing.FXSwingNode;
import cn.oyzh.fx.plus.swing.SwingUtil;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.control.IndexRange;
import javafx.scene.text.Font;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.text.Caret;
import java.awt.Point;
import java.util.Optional;
import java.util.Set;

/**
 * 编辑器面板
 *
 * @author oyzh
 * @since 2025/07/30
 */
public class EditorPane extends FXSwingNode {

    {
        Editor editor = new Editor();
        editor.setLineWrap(true);

        RTextScrollPane scrollPane = new RTextScrollPane(editor);
        scrollPane.setLineNumbersEnabled(true);
        this.setContent(scrollPane);
    }

    public RTextScrollPane getScrollPane() {
        return (RTextScrollPane) super.getContent();
    }

    public Editor getEditor() {
        return (Editor) this.getScrollPane().getViewport().getComponents()[0];
    }

    protected Font initFont() {

        return null;
    }

    public void initPrompts() {

    }

    public void setPrompts(Set<String> prompts) {
        this.getEditor().setPrompts(prompts);
    }

    public void showData(Object data) {
        this.getEditor().showData(data);
    }

    public void showData(Object data, EditorFormatType formatType) {
        this.getEditor().showData(data, formatType);
    }

    public EditorFormatType showDetectData(Object rawData) {
        return this.getEditor().showDetectData(rawData);
    }

    public void showJsonData(Object rawData) {
        this.getEditor().showJsonData(rawData);
    }

    public void showXmlData(Object rawData) {
        this.getEditor().showXmlData(rawData);
    }

    public void showHtmlData(Object rawData) {
        this.getEditor().showHtmlData(rawData);
    }

    public void showYamlData(Object rawData) {
        this.getEditor().showYamlData(rawData);
    }

    public void showCssData(Object rawData) {
        this.getEditor().showCssData(rawData);
    }

    public void showPropertiesData(Object rawData) {
        this.getEditor().showPropertiesData(rawData);
    }

    public void showRawData(Object rawData) {
        this.getEditor().showRawData(rawData);
    }

    @Override
    public void initNode() {
        super.initNode();
        // 行号策略变化事件
        this.lineNumPolicyProperty().addListener((observableValue, editorLineNumPolicy, t1) -> {
            if (t1 == EditorLineNumPolicy.NONE) {
                this.hideLineNum();
            } else if (t1 == EditorLineNumPolicy.ALWAYS) {
                this.showLineNum();
            }
        });
    }

    public void hideLineNum() {
        this.getScrollPane().setLineNumbersEnabled(false);
    }

    public void showLineNum() {
        this.getScrollPane().setLineNumbersEnabled(true);
    }

    private ObjectProperty<EditorLineNumPolicy> lineNumPolicyProperty;

    public EditorLineNumPolicy getLineNumPolicy() {
        return this.lineNumPolicyProperty == null ? EditorLineNumPolicy.ALWAYS : this.lineNumPolicyProperty.get();
    }

    public void setLineNumPolicy(EditorLineNumPolicy lineNumPolicy) {
        this.lineNumPolicyProperty().set(lineNumPolicy);
    }

    public ObjectProperty<EditorLineNumPolicy> lineNumPolicyProperty() {
        if (this.lineNumPolicyProperty == null) {
            this.lineNumPolicyProperty = new SimpleObjectProperty<>(EditorLineNumPolicy.ALWAYS);
        }
        return this.lineNumPolicyProperty;
    }

    public StringProperty highlightTextProperty() {
        return this.getEditor().highlightTextProperty();
    }

    public void setFormatType(EditorFormatType formatType) {
        this.getEditor().setFormatType(formatType);
    }

    public String getText() {
        return this.getEditor().getText();
    }

    public void paste() {
        this.getEditor().paste();
    }

    public void clear() {
        this.getEditor().clear();
    }

    public void setText(String text) {

    }

    public String getTextTrim() {
        return this.getEditor().getTextTrim();
    }

    public void setHighlightText(String highlightText) {
        this.getEditor().setHighlightText(highlightText);
    }

    public void selectRangeAndGoto(int index, int i) {
    }

    public void undo() {
        this.getEditor().undo();
    }

    public void redo() {
        this.getEditor().redo();
    }

    public void forgetHistory() {
        this.getEditor().forgetHistory();
    }

    public BooleanProperty undoableProperty() {
        return this.getEditor().undoableProperty();
    }

    public BooleanProperty redoableProperty() {
        return this.getEditor().redoableProperty();
    }

    public BooleanProperty editableProperty() {
        return this.getEditor().editableProperty();
    }

    public void addTextChangeListener(ChangeListener<String> listener) {
        this.getEditor().addTextChangeListener(listener);
    }

    public void setPromptText(String promptText) {
    }

    public int getCaretPosition() {
        return this.getEditor().getCaretPosition();
    }

    public Optional<Bounds> getCaretBounds() {
        Caret caret = this.getEditor().getCaret();
        if (caret == null || !caret.isVisible()) {
            return Optional.empty();
        }
        Point point = caret.getMagicCaretPosition();
        int w = EditorUtil.getCaretWidth(this.getEditor());
        int h = EditorUtil.getCaretHeight(this.getEditor());
        BoundingBox bounds = new BoundingBox(point.x, point.y, w, h);
        return Optional.of(bounds);
    }

    public void replaceText(IndexRange range, String content) {
        this.getEditor().replaceText(range.getStart(), range.getEnd(), content);
    }

    public void scrollToTop() {
    }

    public boolean isEmpty() {
        return this.getEditor().isEmpty();
    }

    public ObjectProperty<EditorFormatType> formatTypeProperty() {
        return this.getEditor().formatTypeProperty();
    }

    @Override
    public void changeTheme(ThemeStyle style) {
        super.changeTheme(style);
        Editor editor = this.getEditor();
        if (ThemeManager.isDarkMode()) {
            editor.setCurrentLineHighlightColor(EditorUtil.CURRENT_LINE_HIGHLIGHT_COLOR_DARK);
        } else {
            editor.setCurrentLineHighlightColor(EditorUtil.CURRENT_LINE_HIGHLIGHT_COLOR_LIGHT);
        }
        SwingUtil.applyTheme(editor);
        RTextScrollPane scrollPane = this.getScrollPane();
        SwingUtil.applyTheme(scrollPane);
    }

    public void setEditable(boolean editable) {
        this.getEditor().setEditable(editable);
    }

    public boolean isEditable() {
        return this.getEditor().isEditable();
    }
}
