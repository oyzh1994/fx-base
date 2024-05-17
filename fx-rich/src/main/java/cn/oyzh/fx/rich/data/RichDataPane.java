package cn.oyzh.fx.rich.data;

import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.rich.control.FlexVirtualizedScrollPane;
import javafx.beans.value.ChangeListener;
import javafx.scene.text.FontWeight;
import lombok.NonNull;
import org.reactfx.value.Val;

/**
 * @author oyzh
 * @since 2024/5/17
 */
public class RichDataPane extends FlexVirtualizedScrollPane<RichDataTextArea> implements FontAdapter {

    public RichDataPane() {
        super(new RichDataTextArea());
        this.initDataTextArea();
    }

    protected void initDataTextArea() {
        RichDataTextArea area = this.getContent();
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

    public void showData(Object data) {
        this.getContent().showData(data);
    }

    public void showData(RichDataType dataType, Object rawData) {
        this.getContent().showData(dataType, rawData);
    }

    public void setDataType(RichDataType dataType) {
        this.getContent().setDataType(dataType);
    }

    public RichDataType getDataType() {
        return this.getContent().getDataType();
    }

    public String getTextTrim() {
        return this.getContent().getTextTrim();
    }

    public void setSearchText(String searchText) {
        this.getContent().setSearchText(searchText);
    }

    public String setSearchText() {
        return this.getContent().getSearchText();
    }

    public void replaceSelection(String replacement) {
        this.getContent().replaceSelection(replacement);
    }

    public void selectRange(int index, int end) {
        this.getContent().selectRange(index, end);
    }
}
