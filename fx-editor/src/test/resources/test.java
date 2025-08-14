package cn.oyzh.fx.editor;

import cn.oyzh.fx.editor.rsyntaxtextarea.Editor;
import cn.oyzh.fx.editor.rsyntaxtextarea.EditorFormatType;
import cn.oyzh.fx.editor.rsyntaxtextarea.EditorLineNumPolicy;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.rich.richtextfx.control.RichTextAreaPane;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import org.fife.ui.rsyntaxtextarea.TextEditorPane;

/**
 * @author oyzh
 * @since 2025/07/30
 */
public class BaseEditorPane extends TextEditorPane implements NodeGroup, FontAdapter {

    public BaseEditorPane() {
        super(new Editor());
    }

    public Editor getEditor() {
        return super.getContent();
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

    @Override
    public void initNode() {
        super.initNode();
        super.scrollToTop();
    }
}
